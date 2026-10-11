/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lexical analyzer.
 * <p>
 * It is not meant to be thread-safe.
 * Before using, make sure the lexer is reset.
 */
@SuppressWarnings("unused")
@NotThreadSafe
public interface LexicalAnalyzer extends Iterable<Token> {

    /**
     * Parse next token.
     *
     * @return next token, or Token.EOF if no token is available on the input
     */
    Token next();

    /**
     * Determines if a next token is available on the input at the current lexer position.
     *
     * @return true if the lexer has a next token to be parsed
     */
    boolean hasNext();

    /**
     * Reset this lexical analyzer with new input.
     *
     * @param array  new program source code
     * @param offset the index of the first character to read
     * @param length length of how much to read
     */
    void reset(char[] array, int offset, int length);

    /**
     * Line comment opening and optional closing text, or null if unsupported.
     */
    default String[] getLineCommentStartAndEnd() {
        return null;
    }

    /**
     * Analyze an entire source snapshot for editing. The result must contain immutable tokens.
     * Implementations may supply SYMBOL_DEFINITION/REFERENCE with scope metadata.
     * Implementations must reject invalid or ambiguous source before providing semantic editing metadata.
     */
    default List<Token> analyze(String source) {
        char[] array = source.toCharArray();
        reset(array, 0, array.length);
        List<Token> result = new ArrayList<>();
        while (hasNext()) {
            Token token = next();
            result.add(new SourceToken(token.getType(), token.getOffset(), token.getText(), token.getSymbolName(),
                    token.getSymbolScope(), token.getReferenceScope(), token.getReferencedFile()));
        }
        return List.copyOf(result);
    }

    /**
     * Supported extraction operations (for example, macro or subroutine).
     */
    default List<String> getExtractionKinds() {
        return List.of();
    }

    /**
     * Compiler-specific identifier validation and comparison, used by generic rename.
     */
    default boolean isValidSymbolName(String name) {
        return false;
    }

    default String normalizeSymbolName(String name) {
        return name;
    }

    /**
     * Return an edited source snapshot, or reject an unsafe/unsupported extraction.
     */
    default String extract(String source, int start, int end, String name, String kind) {
        throw new UnsupportedOperationException("Source extraction is unavailable for this language");
    }

    @Override
    default Iterator<Token> iterator() {
        List<Token> tokens = new ArrayList<>();
        while (hasNext()) {
            tokens.add(next());
        }
        return tokens.iterator();
    }
}
