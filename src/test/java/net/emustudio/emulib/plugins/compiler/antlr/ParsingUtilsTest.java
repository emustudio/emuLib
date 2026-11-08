/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler.antlr;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.junit.Test;

import static net.emustudio.emulib.plugins.compiler.antlr.ParsingUtils.*;
import static org.junit.Assert.assertEquals;

public class ParsingUtilsTest {

    private static Token token(String text) {
        return new CommonToken(0, text);
    }

    @Test
    public void testParseNumber() {
        assertEquals(0x22F, parseNumber(token("0x22F")));
        assertEquals(-0xAA55, parseNumber(token("-0AA55h")));
        assertEquals(10, parseNumber(token("12q")));
        assertEquals(-22, parseNumber(token("-22d")));
        assertEquals(22, parseNumber(token("+22")));
        assertEquals(45, parseNumber(token("101101b")));
    }

    @Test
    public void testStringsAndIds() {
        assertEquals("te\"x\"t", parseLitString(token("'te\"x\"t'")));
        assertEquals("", parseLitString(token("\"\"")));
        assertEquals("label", parseLabel(token("label:")));
        assertEquals("label", normalizeId("LaBeL"));
    }
}
