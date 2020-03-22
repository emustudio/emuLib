/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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
import java.io.Reader;

/**
 * Lexical analyzer.
 */
@SuppressWarnings("unused")
public interface LexicalAnalyzer {

    /**
     * Resets lexer.
     * <p>
     * Internal counters (actual position, actual column, row, etc.) should be cleared. Lexical analyzer should prepare
     * itself to start parsing from the beginning of the document.
     *
     * @param reader Source code reader
     * @param line   from this line should lexical analyzer start, usually 0
     * @param offset from this char should lexical analyzer start, usually 0
     * @param column from this column should lexical analyzer start, usually 0
     */
    void reset(Reader reader, int line, int offset, int column);

    /**
     * Resets lexer.
     *
     * @param reader     Source code reader
     * @param line       from this line should lexical analyzer start, usually 0
     * @param offset     from this char should lexical analyzer start, usually 0
     * @param column     from this column should lexical analyzer start, usually 0
     * @param lexerState lexer state
     */
    void reset(Reader reader, int line, int offset, int column, int lexerState);

    /**
     * Get next token.
     * <p>
     * Tokens are retrieved as in "iterator" style - the actual position is a mutable state stored in the lexer.
     *
     * @return next token
     * @throws java.io.IOException is thrown when the token could not be read
     */
    Token getToken() throws IOException;
}
