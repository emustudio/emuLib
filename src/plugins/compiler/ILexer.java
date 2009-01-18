/**
 * ILexer.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo
 * 
 * KISS, YAGNI
 * 
 */
package plugins.compiler;


/**
 * Interface that implements lexical analyzer
 */
public interface ILexer {

    /**
     * Performs reset of the analyzer. Internal counters (actual position, actual
     * column, row, etc.) should be cleared. Lexical analyzer should prepare itself
     * for start from beginning of the document.
     */
    public void reset ();

    /**
     * Performs reset of the analyzer. Internal counters (actual position, actual
     * column, row, etc.) should be cleared. Lexical analyzer should prepare itself
     * for start from beginning of the document.
     * @param yyline    from this line should lexical analyser start, usually 0
     * @param yychar    from this char should lexical analyser start, usually 0
     * @param yycolumn  from this column should lexical analyser start, usually 0
     */
    public void reset (int yyline, int yychar, int yycolumn);

    /**
     * Gets next lexical symbol from source code, from actual position. This is
     * real implementation of lexical analyzer. After this call it should
     * increase internal counters to next unread text (actual position, actual
     * row, column, etc.)
     * 
     * @return next found token
     */
    public IToken getSymbol () throws java.io.IOException;

}

