/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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

    /** Opening and closing tokens of a foldable source block. */
    int BLOCK_START = 0xA00;
    int BLOCK_END = 0xB00;

    /** A declared symbol and a reference to a symbol, as identified by source analysis. */
    int SYMBOL_DEFINITION = 0xC00;
    int SYMBOL_REFERENCE = 0xD00;
    /** A token naming another source file. */
    int INCLUDE_PATH = 0xE00;

    /** Referenced source file path, or null when the token is not an include path. */
    default String getReferencedFile() { return null; }

    /** Symbol spelling, excluding punctuation, or null when this token has no symbol metadata. */
    default String getSymbolName() { return null; }

    /** Lexical scope identity. Empty denotes the file/include-global scope; null means unknown. */
    default String getSymbolScope() { return null; }

    /** Scope searched for the referenced declaration. Defaults to the token's lexical scope. */
    default String getReferenceScope() { return getSymbolScope(); }

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
     * Get 0-based starting offset in UTF-16 code units (Java String indices).
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

