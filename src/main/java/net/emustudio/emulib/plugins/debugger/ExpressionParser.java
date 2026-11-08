/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Common syntax only. Plugins resolve all machine names and functions without reading guest state. */
public final class ExpressionParser {
    private static final String[][] OPERATORS = {
            {"||"}, {"&&"}, {"|"}, {"^"}, {"&"}, {"==", "!="},
            {"<=", ">=", "<", ">"}, {"<<", ">>"}, {"+", "-"}, {"*", "/", "%"}
    };
    private final String source;
    private final Function<String, Supplier<Object>> variables;
    private final Function<String, Function<List<Object>, Object>> functions;
    private int at, depth;

    private ExpressionParser(String source, Function<String, Supplier<Object>> variables,
                             Function<String, Function<List<Object>, Object>> functions) {
        if (source == null || source.length() > 1024) throw new IllegalArgumentException("Expression too long");
        this.source = source;
        this.variables = Objects.requireNonNull(variables);
        this.functions = Objects.requireNonNull(functions);
    }

    public static Debugging.Expression parse(String source, Function<String, Supplier<Object>> variables,
                                            Function<String, Function<List<Object>, Object>> functions) {
        ExpressionParser parser = new ExpressionParser(source, variables, functions);
        Supplier<Object> expression = parser.expression(0);
        parser.space();
        if (parser.at != source.length()) throw parser.error("Unexpected input");
        return new Debugging.Expression() {
            private boolean seen;
            private Object previous;
            @Override public boolean test() { return truth(expression.get()); }
            @Override public ValueView display() {
                Object value = Objects.requireNonNull(expression.get(), "Expression value unavailable");
                String text = String.valueOf(value);
                List<ValueView.Format> formats = List.of();
                if (value instanceof Number) {
                    long number = ((Number) value).longValue();
                    formats = List.of(new ValueView.Format("Decimal", Long.toString(number)),
                            new ValueView.Format("Hexadecimal", "0x" + Long.toHexString(number).toUpperCase()),
                            new ValueView.Format("Binary", Long.toBinaryString(number)),
                            new ValueView.Format("ASCII", number >= 32 && number <= 126
                                    ? Character.toString((char) number) : "."));
                }
                boolean changed = seen && !Objects.equals(previous, value);
                previous = value;
                seen = true;
                return new ValueView(text, formats, changed);
            }
        };
    }

    private Supplier<Object> expression(int level) {
        if (level == OPERATORS.length) return primary();
        Supplier<Object> left = expression(level + 1);
        while (true) {
            String operator = null;
            space();
            for (String candidate : OPERATORS[level]) {
                if (source.startsWith(candidate, at)
                        && !(candidate.length() == 1 && at + 1 < source.length()
                        && source.charAt(at + 1) == candidate.charAt(0) && "&|<>".contains(candidate))) {
                    operator = candidate;
                    at += candidate.length();
                    break;
                }
            }
            if (operator == null) return left;
            Supplier<Object> a = left, b = expression(level + 1);
            String operation = operator;
            left = () -> apply(operation, a, b);
        }
    }

    private Supplier<Object> primary() {
        if (++depth > 64) throw error("Expression nesting too deep");
        try { return atom(); }
        finally { depth--; }
    }

    private Supplier<Object> atom() {
        if (take("(")) {
            Supplier<Object> value = expression(0);
            expect(")");
            return value;
        }
        if (take("-")) { Supplier<Object> value = primary(); return () -> -number(value.get()); }
        if (take("+")) { Supplier<Object> value = primary(); return () -> number(value.get()); }
        if (take("~")) { Supplier<Object> value = primary(); return () -> ~number(value.get()); }
        if (take("!")) { Supplier<Object> value = primary(); return () -> !truth(value.get()); }
        space();
        if (at < source.length() && (source.charAt(at) == '\'' || source.charAt(at) == '"')) {
            String value = string();
            return () -> value;
        }
        int start = at;
        while (at < source.length() && (Character.isLetterOrDigit(source.charAt(at)) || source.charAt(at) == '_')) at++;
        if (start == at) throw error("Expected value");
        String token = source.substring(start, at);
        if (Character.isDigit(token.charAt(0))) {
            try {
                long value = token.startsWith("0x") || token.startsWith("0X")
                        ? Long.parseUnsignedLong(token.substring(2), 16)
                        : token.startsWith("0b") || token.startsWith("0B")
                        ? Long.parseUnsignedLong(token.substring(2), 2) : Long.parseLong(token);
                return () -> value;
            } catch (NumberFormatException invalid) { throw error("Invalid number"); }
        }
        if (token.equalsIgnoreCase("true")) return () -> true;
        if (token.equalsIgnoreCase("false")) return () -> false;
        boolean bracket = take("[");
        if (bracket || take("(")) {
            Function<List<Object>, Object> function = functions.apply(token);
            if (function == null) throw error("Unknown function: " + token);
            List<Supplier<Object>> arguments = new ArrayList<>();
            String close = bracket ? "]" : ")";
            if (!take(close)) {
                do { arguments.add(expression(0)); } while (take(","));
                expect(close);
            }
            return () -> {
                List<Object> values = new ArrayList<>(arguments.size());
                for (Supplier<Object> argument : arguments) values.add(argument.get());
                return function.apply(List.copyOf(values));
            };
        }
        Supplier<Object> variable = variables.apply(token);
        if (variable == null) throw error("Unknown name: " + token);
        return variable;
    }

    private String string() {
        char quote = source.charAt(at++);
        StringBuilder result = new StringBuilder();
        while (at < source.length()) {
            char next = source.charAt(at++);
            if (next == quote) return result.toString();
            if (next == '\\') {
                if (at == source.length()) throw error("Unterminated escape");
                next = source.charAt(at++);
                switch (next) {
                    case 'n': next = '\n'; break;
                    case 'r': next = '\r'; break;
                    case 't': next = '\t'; break;
                    case '\\': case '\'': case '"': break;
                    default: throw error("Unknown escape");
                }
            }
            result.append(next);
        }
        throw error("Unterminated string");
    }

    private static Object apply(String operator, Supplier<Object> a, Supplier<Object> b) {
        Object x = a.get();
        if (operator.equals("&&")) return truth(x) && truth(b.get());
        if (operator.equals("||")) return truth(x) || truth(b.get());
        Object y = b.get();
        if (operator.equals("==") || operator.equals("!=")) {
            boolean equal = x instanceof Number && y instanceof Number
                    ? number(x) == number(y) : Objects.equals(x, y);
            return operator.equals("==") ? equal : !equal;
        }
        long left = number(x), right = number(y);
        switch (operator) {
            case "+": return left + right;
            case "-": return left - right;
            case "*": return left * right;
            case "/": return left / right;
            case "%": return left % right;
            case "&": return left & right;
            case "|": return left | right;
            case "^": return left ^ right;
            case "<<": return left << right;
            case ">>": return left >>> right;
            case "<": return left < right;
            case ">": return left > right;
            case "<=": return left <= right;
            case ">=": return left >= right;
            default: throw new AssertionError(operator);
        }
    }

    private static long number(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof Boolean) return (Boolean) value ? 1 : 0;
        throw new IllegalArgumentException("Numeric value unavailable");
    }

    private static boolean truth(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).longValue() != 0;
        throw new IllegalArgumentException("Condition requires a boolean or number");
    }

    private void space() { while (at < source.length() && Character.isWhitespace(source.charAt(at))) at++; }
    private boolean take(String token) {
        space();
        if (!source.startsWith(token, at)) return false;
        at += token.length();
        return true;
    }
    private void expect(String token) { if (!take(token)) throw error("Expected " + token); }
    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at column " + (at + 1));
    }
}
