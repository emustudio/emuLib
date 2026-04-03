/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lexical analyzer.
 * <p>
 * It is not meant to be thread-safe.
 * Before using, make sure the lexer is reset.
 */
@SuppressWarnings("unused")
@NotThreadSafe
public interface LexicalAnalyzer extends Iterable<Token> {

    /**
     * Parse next token.
     *
     * @return next token, or Token.EOF if no token is available on the input
     */
    Token next();

    /**
     * Determines if a next token is available on the input at the current lexer position.
     *
     * @return true if the lexer has a next token to be parsed
     */
    boolean hasNext();

    /**
     * Reset this lexical analyzer with new input.
     *
     * @param array new program source code
     * @param offset the index of the first character to read
     * @param length length of how much to read
     */
    void reset(char[] array, int offset, int length);

    @Override
    default Iterator<Token> iterator() {
        List<Token> tokens = new ArrayList<>();
        while (hasNext()) {
            tokens.add(next());
        }
        return tokens.iterator();
    }
}
