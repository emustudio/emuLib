/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class EditingMetadataTest {
    @Test public void legacyLexersRemainUsableAndSnapshotsCopyMutableTokens() {
        LexicalAnalyzer lexer = new LexicalAnalyzer() {
            String value; boolean available;
            final Token token = new Token() {
                public int getType() { return Token.IDENTIFIER; }
                public int getOffset() { return 0; }
                public String getText() { return value; }
            };
            public Token next() { available = false; return token; }
            public boolean hasNext() { return available; }
            public void reset(char[] input, int offset, int length) { value = new String(input, offset, length); available = true; }
        };
        List<Token> snapshot = lexer.analyze("first");
        lexer.analyze("second");
        assertEquals("first", snapshot.get(0).getText());
        assertNull(snapshot.get(0).getSymbolName());
        assertNull(lexer.getLineCommentStartAndEnd());
        assertTrue(lexer.getExtractionKinds().isEmpty());
        assertFalse(lexer.isValidSymbolName("name"));
        assertThrows(UnsupportedOperationException.class, () -> lexer.extract("text", 0, 4, "name", "Macro"));
    }
    @Test public void sourceTokensPreserveOptionalScopeAndIncludeMetadata() {
        SourceToken token = new SourceToken(Token.INCLUDE_PATH, 12, "'other'", null, "scope", null, "other");
        assertEquals("other", token.getReferencedFile());
        assertEquals("scope", token.getSymbolScope());
        assertNull(token.getReferenceScope());
        assertEquals(12, token.getOffset());
    }
}
