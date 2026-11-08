/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler.antlr;

import net.emustudio.emulib.plugins.compiler.LexicalAnalyzer;
import net.emustudio.emulib.plugins.compiler.Token;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Objects;

/**
 * Lexical analyzer backed by ANTLR lexer. ANTLR token types are mapped to emuLib {@link Token} types.
 */
public class AntlrLexicalAnalyzer implements LexicalAnalyzer {
    protected final Lexer lexer;
    private final String lineCommentStart;

    /**
     * @param lexer            ANTLR lexer
     * @param tokenMap         mapping from ANTLR token type (index) to {@link Token} type
     * @param lineCommentStart line comment opening text, or null if unsupported
     */
    public AntlrLexicalAnalyzer(Lexer lexer, int[] tokenMap, String lineCommentStart) {
        this.lexer = Objects.requireNonNull(lexer);
        this.lineCommentStart = lineCommentStart;
        lexer.setTokenFactory(new EmuTokenFactory(Objects.requireNonNull(tokenMap)));
    }

    @Override
    public String[] getLineCommentStartAndEnd() {
        return lineCommentStart == null ? null : new String[]{lineCommentStart, null};
    }

    @Override
    public Token next() {
        return (Token) lexer.nextToken();
    }

    @Override
    public boolean hasNext() {
        return !lexer._hitEOF;
    }

    @Override
    public void reset(char[] array, int offset, int length) {
        lexer.setInputStream(new CharArrayCharStream(array, offset, length));
    }

    /**
     * ANTLR token implementing emuLib {@link Token}.
     */
    public static class EmuToken extends CommonToken implements Token {
        EmuToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
            super(source, type, channel, start, stop);
        }

        EmuToken(int type, String text) {
            super(type, text);
        }

        @Override
        public int getOffset() {
            return getStartIndex();
        }
    }

    private static final class EmuTokenFactory implements TokenFactory<EmuToken> {
        private final int[] tokenMap;

        EmuTokenFactory(int[] tokenMap) {
            this.tokenMap = tokenMap;
        }

        private int convertType(int antlrType) {
            return antlrType == org.antlr.v4.runtime.Token.EOF ? Token.EOF : tokenMap[antlrType];
        }

        @Override
        public EmuToken create(Pair<TokenSource, CharStream> source, int type, String text,
                               int channel, int start, int stop, int line, int charPositionInLine) {
            EmuToken t = new EmuToken(source, convertType(type), channel, start, stop);
            t.setLine(line);
            t.setCharPositionInLine(charPositionInLine);
            if (text != null) {
                t.setText(text);
            } else if (source.b != null) {
                t.setText(source.b.getText(Interval.of(start, stop)));
            }
            return t;
        }

        @Override
        public EmuToken create(int type, String text) {
            return new EmuToken(convertType(type), text);
        }
    }
}
