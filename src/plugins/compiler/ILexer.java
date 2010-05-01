/**
 * ILexer.java
 * 
 * (c) Copyright 2008-2010, P. Jakubčo <pjakubco@gmail.com>
 * 
 * KISS, YAGNI
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
package plugins.compiler;

import java.io.Reader;


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
     * @param in        <code>Reader</code> object of the document - source code.
     * @param yyline    from this line should lexical analyser start, usually 0
     * @param yychar    from this char should lexical analyser start, usually 0
     * @param yycolumn  from this column should lexical analyser start, usually 0
     */
    public void reset (Reader in, int yyline, int yychar, int yycolumn);

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

