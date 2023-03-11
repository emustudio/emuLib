/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lexical analyzer.
 */
@SuppressWarnings("unused")
public interface LexicalAnalyzer extends Iterable<Token> {

    /**
     * Get next token.
     * <p>
     * Tokens are retrieved as in "iterator" style - the actual position is a mutable state stored in the lexer.
     *
     * @return next token
     */
    Token nextToken();

    /**
     * Determines if this lexer is at EOF (at the end of parsing)
     * @return true if the EOF was hit
     */
    boolean isAtEOF();

    /**
     * Reset this lexical analyzer with new input.
     * All state are reset.
     * @param input new program source code
     * @throws IOException when input cannot be read
     */
    void reset(InputStream input) throws IOException;

    /**
     * Reset this lexical analyzer with new input.
     * All state are reset.
     * @param input new program source code
     */
    void reset(String input);

    @Override
    default Iterator<Token> iterator() {
        List<Token> tokens = new ArrayList<>();
        while (!isAtEOF()) {
            tokens.add(nextToken());
        }
        return tokens.iterator();
    }
}
