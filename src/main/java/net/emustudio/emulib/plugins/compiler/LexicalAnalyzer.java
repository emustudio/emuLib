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
     * All state should be reset.
     * @param input new program source code
     * @throws IOException when input cannot be read
     */
    void reset(InputStream input) throws IOException;

    @Override
    default Iterator<Token> iterator() {
        List<Token> tokens = new ArrayList<>();
        while (!isAtEOF()) {
            tokens.add(nextToken());
        }
        return tokens.iterator();
    }
}
