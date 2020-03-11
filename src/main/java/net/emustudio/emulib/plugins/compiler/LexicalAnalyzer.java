// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import java.io.IOException;
import java.io.Reader;


/**
 * Interface that implements lexical analyzer
 */
@SuppressWarnings("unused")
public interface LexicalAnalyzer {

    /**
     * Performs reset of the analyzer.
     * <p>
     * Internal counters (actual position, actual column, row, etc.) should be cleared. Lexical analyzer should prepare
     * itself to start parsing from the beginning of the document.
     */
    void reset();

    /**
     * Performs reset of the analyzer.
     * <p>
     * Internal counters (actual position, actual column, row, etc.) should be cleared. Lexical analyzer should prepare
     * itself to start parsing from the beginning of the document.
     *
     * @param sourceCode  <code>Reader</code> of the source code.
     * @param startLine   from this line should lexical analyzer start, usually 0
     * @param startChar   from this char should lexical analyzer start, usually 0
     * @param startColumn from this column should lexical analyzer start, usually 0
     */
    void reset(Reader sourceCode, int startLine, int startChar, int startColumn);

    /**
     * Gets next lexical symbol from source code, from actual position. This is
     * real implementation of lexical analyzer. After this call it should
     * increase internal counters to next unread text (actual position, actual
     * row, column, etc.)
     *
     * @return next found token
     * @throws java.io.IOException is thrown when the token could not be read
     */
    Token getSymbol() throws IOException;

}

