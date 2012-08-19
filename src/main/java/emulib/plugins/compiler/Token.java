/*
 * Token.java
 * 
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2008-2012, Peter Jakubčo
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.plugins.compiler;

/**
 * Interface that identifies a token.
 */
public interface Token {

    /**
     * Token is a reserved word.
     */
    public static final int RESERVED = 0x100;

    /**
     * Token is a preprocessor keyword.
     */
    public static final int PREPROCESSOR = 0x200;

    /**
     * Token is a CPU register.
     */
    public static final int REGISTER = 0x300;

    /**
     * Token is a separator (e.g. ' ', tab, '\n', ...).
     */
    public static final int SEPARATOR = 0x400;

    /**
     * Token is a operator (e.g. +, -, *, /, ...).
     */
    public static final int OPERATOR = 0x500;

    /**
     * Token is a comment. Most assemblers used semicolon (";") as start of a comment.
     */
    public static final int COMMENT = 0x600;

    /**
     * Token is a literal (e.g. number, string, char, ...).
     */
    public static final int LITERAL = 0x700;

    /**
     * Token is an identifier (e.g. name of variable, name of macro...).
     */
    public static final int IDENTIFIER = 0x800;

    /**
     * Token is an label identifier.
     */
    public static final int LABEL = 0x900;

    /**
     * Token is of unknown type.
     */
    public static final int ERROR = 1;

    /**
     * Token represents end-of-file. This token should be the last found token.
     */
    public static final int TEOF = 0;

    /**
     * Get token ID.
     * 
     * The token ID is compiler-specific value. Each token must have unique ID.
     * 
     * @return ID of the token
     */
    public int getID ();

    /**
     * Get token type.
     * 
     * The token type mus be one of the pre-defined constants located inside this interface (e.g. <code>RESERVED</code>,
     * <code>PREPROCESSOR</code>, etc.).
     * 
     * @return type of the token
     */
    public int getType ();

    /**
     * Get 0-based starting line (row) of token position.
     * 
     * @return start line number of the token
     */
    public int getLine ();

    /**
     * Get 0-based starting column of token position.
     * 
     * @return start column number of the token
     */
    public int getColumn ();

    /**
     * Get 0-based starting offset of token position.
     * 
     * @return starting offset of the token in the source code
     */
    public int getOffset ();

    /**
     * Get length of the token.
     * 
     * @return length of the token
     */
    public int getLength ();
    
    /**
     * Get error string for this token.
     * 
     * If the token is not errorneous, return empty string ("").
     * 
     * @return error string message for this token
     */
    public String getErrorString ();
    
    /**
     * Get token value.
     * 
     * @return token value
     */
    public String getText ();
    
    /**
     * Check if the token is in initial lexical state.
     * 
     * Initial lexical state represents a position in the source code, from what the lexical analyzer can safely start
     * parsing the source code - as it would parse it from the start of the document. They are useful especially for
     * syntax highlighting.
     *  
     * @return true if token is in initial lexical state; false otherwise.
     */
    public boolean isInitialLexicalState();

}

