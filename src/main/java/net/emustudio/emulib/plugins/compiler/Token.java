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

/**
 * Interface that identifies a token.
 */
@SuppressWarnings("unused")
public interface Token {

    /**
     * Token is a reserved word.
     */
    int RESERVED = 0x100;

    /**
     * Token is a preprocessor keyword.
     */
    int PREPROCESSOR = 0x200;

    /**
     * Token is a CPU register.
     */
    int REGISTER = 0x300;

    /**
     * Token is a separator (e.g. ' ', tab, '\n', ...).
     */
    int SEPARATOR = 0x400;

    /**
     * Token is a operator (e.g. +, -, *, /, ...).
     */
    int OPERATOR = 0x500;

    /**
     * Token is a comment. Most assemblers used semicolon (";") as start of a comment.
     */
    int COMMENT = 0x600;

    /**
     * Token is a literal (e.g. number, string, char, ...).
     */
    int LITERAL = 0x700;

    /**
     * Token is an identifier (e.g. name of variable, name of macro...).
     */
    int IDENTIFIER = 0x800;

    /**
     * Token is an label identifier.
     */
    int LABEL = 0x900;

    /**
     * Token is a whitespace
     */
    int WHITESPACE = 0x1000;

    /**
     * Token is of unknown type.
     */
    int ERROR = 1;

    /**
     * Token represents end-of-file. This token should be the last found token.
     */
    int EOF = 0;

    /**
     * Get token type.
     * <p>
     * The token type mus be one of the pre-defined constants located inside this interface (e.g. <code>RESERVED</code>,
     * <code>PREPROCESSOR</code>, etc.).
     *
     * @return type of the token
     */
    int getType();

    /**
     * Get 0-based starting offset of token position.
     *
     * @return starting offset of the token in the source code
     */
    int getOffset();

    /**
     * Get token value.
     *
     * @return token value
     */
    String getText();
}

