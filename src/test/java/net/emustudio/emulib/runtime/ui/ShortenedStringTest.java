/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShortenedStringTest {
    private ShortenedString<String> shortenedString;
    private JLabel component;

    @Before
    public void setUp() {
        shortenedString = new ShortenedString<>("HelloWorld", String::toUpperCase);
        component = new JLabel();
        component.setFont(new Font("Monospaced", Font.PLAIN, 12));
        component.setSize(100, 20);
    }

    @Test
    public void testGetValue() {
        assertEquals("HelloWorld", shortenedString.getValue());
    }

    @Test
    public void testGetFullString() {
        assertEquals("HELLOWORLD", shortenedString.getFullString());
    }

    @Test
    public void testDeriveMaxStringLength() {
        shortenedString.deriveMaxStringLength(component);
        assertTrue(shortenedString.getMaxStringLength() >= 0);
        assertTrue(shortenedString.getMaxStringLength() <= "HELLOWORLD".length());
    }

    @Test
    public void testDeriveMaxStringLengthWithCustomWidth() {
        shortenedString.deriveMaxStringLength(component, 50);
        assertTrue(shortenedString.getMaxStringLength() >= 0);
        assertTrue(shortenedString.getMaxStringLength() <= "HELLOWORLD".length());
    }

    @Test
    public void testGetMaxStringLength() {
        assertEquals(0, shortenedString.getMaxStringLength());
        shortenedString.deriveMaxStringLength(component);
        assertTrue(shortenedString.getMaxStringLength() > 0);
    }

    @Test
    public void testGetShortenedString() {
        shortenedString.deriveMaxStringLength(component);
        String shortened = shortenedString.getShortenedString();
        assertTrue(shortened.length() <= "HELLOWORLD".length());
    }

    @Test
    public void testToString() {
        shortenedString.deriveMaxStringLength(component);
        String toString = shortenedString.toString();
        assertEquals(shortenedString.getShortenedString(), toString);
    }

    @Test
    public void testShortenedStringWithLongValue() {
        ShortenedString<String> longString = new ShortenedString<>(
                "This is a very long string that needs to be shortened",
                s -> s
        );
        longString.deriveMaxStringLength(component, 100);
        String shortened = longString.getShortenedString();
        assertTrue(shortened.length() <= longString.getFullString().length());
    }

    @Test
    public void testShortenedStringWithCustomFunction() {
        ShortenedString<Integer> intString = new ShortenedString<>(
                12345,
                i -> "Number: " + i
        );
        assertEquals("Number: 12345", intString.getFullString());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullValue() {
        new ShortenedString<>(null, String::toString);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullFunction() {
        new ShortenedString<>("test", null);
    }

    @Test
    public void testTextFitsEveryAvailableWidth() {
        component.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        FontMetrics metrics = component.getFontMetrics(component.getFont());
        for (String text : new String[]{"WWWiiiiiiiiiiiiiiiiWWW", "文件名称需要缩短", "مرحبا بالعالم", "", "W"}) {
            ShortenedString<String> value = new ShortenedString<>(text, s -> s);
            for (int width = 0; width <= 160; width++) {
                value.deriveMaxStringLength(component, width);
                assertTrue("Text exceeds width " + width + ": " + value,
                        metrics.stringWidth(value.getShortenedString()) <= width);
            }
            value.deriveMaxStringLength(component, metrics.stringWidth(text));
            assertEquals(text, value.getShortenedString());
        }
    }

    @Test
    public void testNegativeWidthProducesEmptyText() {
        shortenedString.deriveMaxStringLength(component, -36);
        assertEquals(0, shortenedString.getMaxStringLength());
        assertEquals("", shortenedString.getShortenedString());
    }

    @Test
    public void testShorteningPreservesCompleteUnicodeCharacters() {
        for (String character : new String[]{"\uD83D\uDE00", "e\u0301", "\uD83D\uDC69\u200D\uD83D\uDCBB"}) {
            ShortenedString<String> value = new ShortenedString<>(character.repeat(12), s -> s);
            for (int width = 0; width < 120; width++) {
                value.deriveMaxStringLength(component, width);
                String text = value.getShortenedString();
                int dots = text.indexOf("...");
                if (dots >= 0) {
                    assertEquals(0, dots % character.length());
                    assertEquals(0, (text.length() - dots - 3) % character.length());
                } else {
                    assertTrue(text.equals(value.getFullString()) || text.isEmpty() || text.equals(".") || text.equals(".."));
                }
            }
        }
    }

    @Test
    public void testLongTextDoesNotOverflowLengthCalculation() {
        ShortenedString<String> value = new ShortenedString<>("W".repeat(100_000), s -> s);
        value.deriveMaxStringLength(component, 30_000);
        assertTrue(value.getMaxStringLength() > 0);
        assertTrue(component.getFontMetrics(component.getFont()).stringWidth(value.toString()) <= 30_000);
    }
}
