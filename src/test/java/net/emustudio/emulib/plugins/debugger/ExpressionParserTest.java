package net.emustudio.emulib.plugins.debugger;

import org.junit.Test;
import java.util.*;
import java.util.function.*;
import static org.junit.Assert.*;

public class ExpressionParserTest {
    @Test public void pluginNamesResolveDuringCompileWithoutReadingMachineState() {
        int[] reads = {0};
        Function<String, Supplier<Object>> variables = name -> {
            if (!name.equals("cell")) throw new IllegalArgumentException("Unknown name: " + name);
            return () -> { reads[0]++; return "hello"; };
        };
        Debugging.Expression expression = ExpressionParser.parse("cell == 'hello'", variables, name -> null);

        assertEquals(0, reads[0]);
        assertTrue(expression.test());
        assertEquals(1, reads[0]);
        assertThrows(IllegalArgumentException.class, () -> ExpressionParser.parse("missing", variables, name -> null));
        assertThrows(IllegalArgumentException.class, () -> ExpressionParser.parse("missing(1)", variables, name -> null));
    }

    @Test public void pluginFunctionsReceiveEvaluatedArgumentsAndLogicalOperationsShortCircuit() {
        List<Object> arguments = new ArrayList<>();
        Function<String, Function<List<Object>, Object>> functions = name -> {
            if (!name.equals("lookup")) return null;
            return values -> { arguments.addAll(values); return "written"; };
        };
        Debugging.Expression skipped = ExpressionParser.parse("false && lookup[1 / 0]", name -> null, functions);
        Debugging.Expression tested = ExpressionParser.parse("lookup[2 + 3, 'tape'] == 'written'", name -> null, functions);

        assertFalse(skipped.test());
        assertTrue(arguments.isEmpty());
        assertTrue(tested.test());
        assertEquals(List.of(5L, "tape"), arguments);
    }

    @Test public void numericFormatsAndChangeMarkersFollowEvaluatedValues() {
        long[] value = {65};
        Debugging.Expression expression = ExpressionParser.parse("A", name -> () -> value[0], name -> null);
        ValueView first = expression.display();
        value[0] = 66;
        ValueView second = expression.display();

        assertFalse(first.changed);
        assertTrue(second.changed);
        assertEquals("65", first.text);
        assertEquals("0x41", first.formats.get(1).text);
        assertEquals("A", first.formats.get(3).text);
        assertFalse(expression.display().changed);
    }

    @Test public void arithmeticRetainsPrecedenceWidthsAndUnsignedRightShift() {
        assertEquals("14", parse("2 + 3 * 4").display().text);
        assertEquals("255", parse("0xF0 | 15").display().text);
        assertEquals("9223372036854775807", parse("-1 >> 1").display().text);
        assertTrue(parse("!(2 > 3) && 5 != 6").test());
        assertTrue(parse("'a\\n' == \"a\\n\"").test());
    }

    @Test public void malformedOrUnavailableExpressionsFailExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> parse("1 +"));
        assertThrows(IllegalArgumentException.class, () -> parse("'unterminated"));
        assertThrows(IllegalArgumentException.class, () -> parse("(".repeat(70) + "1" + ")".repeat(70)));
        assertThrows(IllegalArgumentException.class, () -> parse("x".repeat(1025)));
        assertThrows(IllegalArgumentException.class, () -> parse("'hello'").test());
        assertThrows(ArithmeticException.class, () -> parse("1 / 0").display());
    }

    private static Debugging.Expression parse(String source) {
        return ExpressionParser.parse(source, name -> null, name -> null);
    }
}
