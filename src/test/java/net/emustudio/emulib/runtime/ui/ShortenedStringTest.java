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
}
