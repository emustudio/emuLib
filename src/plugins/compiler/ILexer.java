package plugins.compiler;

import java.io.Reader; 
import java.io.IOException;

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.CEA43A20-271F-D7E3-B3F7-7C6385F73BB5]
// </editor-fold> 
/**
 * Interface that implements lexical analyzer
 */
public interface ILexer {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C1D437B7-60A8-7A4E-8B61-ABD08513AE38]
    // </editor-fold> 
    /**
     * Performs reset of the analyzer. Internal counters (actual position, actual
     * column, row, etc.) should be cleared. Lexical analyzer should prepare itself
     * for start from beginning of the document.
     */
    public void reset ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B571DEE6-59C2-785A-658B-22E14CD213D1]
    // </editor-fold> 
    /**
     * Performs reset of the analyzer. Internal counters (actual position, actual
     * column, row, etc.) should be cleared. Lexical analyzer should prepare itself
     * for start from beginning of the document.
     * @param reader    this reader of the document should be used in next lexical
     *                  analysis
     * @param yyline    from this line should lexical analyser start, usually 0
     * @param yychar    from this char should lexical analyser start, usually 0
     * @param yycolumn  from this column should lexical analyser start, usually 0
     */
    public void reset (Reader reader, int yyline, int yychar, int yycolumn) throws IOException;

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.707EECD5-1C93-D97D-8B2E-FEB122541D96]
    // </editor-fold> 
    /**
     * Gets next lexical symbol from source code, from actual position. This is
     * real implementation of lexical analyzer. After this call it should
     * increase internal counters to next unread text (actual position, actual
     * row, column, etc.)
     * @return found token
     */
    public IToken getSymbol () throws java.io.IOException;

}

