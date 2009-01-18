/**
 * IToken.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo
 * 
 * KISS, YAGNI
 * 
 */
package plugins.compiler;

/**
 * Interface that identifies a token.
 */
public interface IToken {

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
     * Gets ID of the token. ID should be used for identifying not the type of 
     * the token (e.g. reserved word, etc.) but for concrete token of given
     * token type got from <code>getType()</code> method. E.g. "mvi" is concrete
     * token of type <code>RESERVED</code> and this method should return ID
     * for "mvi" token. This is primary method for identifying tokens.
     * 
     * @return ID of the token
     */
    public int getID ();

    /**
     * Gets type of the token. Type is represented by pre-defined constants in
     * this interface (e.g. reserved words, preprocessor, ...).
     * @return type of the token
     */
    public int getType ();

    /**
     * Gets 0-based line (row) number of start of the token.
     * @return start line number of the token
     */
    public int getLine ();

    /**
     * Gets 0-based column number of start of the token.
     * @return start column number of the token
     */
    public int getColumn ();

    /**
     * Gets 0-based offset from the start of the token.
     * @return starting offset of the token in the source code
     */
    public int getOffset ();

    /**
     * Gets length of the token.
     * @return length of the token
     */
    public int getLength ();

}

