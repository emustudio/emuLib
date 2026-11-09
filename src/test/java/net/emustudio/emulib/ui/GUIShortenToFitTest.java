/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.ui;

import net.emustudio.emulib.application.ui.GUI;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GUIShortenToFitTest {
    private JLabel component;
    private FontMetrics metrics;

    @Before
    public void setUp() {
        component = new JLabel();
        component.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        metrics = component.getFontMetrics(component.getFont());
    }

    @Test
    public void testTextThatFitsIsUnchanged() {
        String text = "HelloWorld";
        assertEquals(text, GUI.shortenToFit(text, component, metrics.stringWidth(text)));
    }

    @Test
    public void testTextIsCutInTheMiddle() {
        String text = "This is a very long string that needs to be shortened";
        String shortened = GUI.shortenToFit(text, component, metrics.stringWidth(text) / 2);
        int dots = shortened.indexOf("...");
        assertTrue(dots > 0);
        assertTrue(text.startsWith(shortened.substring(0, dots)));
        assertTrue(text.endsWith(shortened.substring(dots + 3)));
    }

    @Test
    public void testTextFitsEveryAvailableWidth() {
        for (String text : new String[]{"WWWiiiiiiiiiiiiiiiiWWW", "文件名称需要缩短", "مرحبا بالعالم", "", "W"}) {
            for (int width = 0; width <= 160; width++) {
                String shortened = GUI.shortenToFit(text, component, width);
                assertTrue("Text exceeds width " + width + ": " + shortened, metrics.stringWidth(shortened) <= width);
            }
        }
    }

    @Test
    public void testNonPositiveWidthProducesEmptyText() {
        assertEquals("", GUI.shortenToFit("HelloWorld", component, 0));
        assertEquals("", GUI.shortenToFit("HelloWorld", component, -36));
    }

    @Test
    public void testShorteningPreservesCompleteUnicodeCharacters() {
        for (String character : new String[]{"\uD83D\uDE00", "e\u0301", "\uD83D\uDC69\u200D\uD83D\uDCBB"}) {
            String full = character.repeat(12);
            for (int width = 0; width < 120; width++) {
                String text = GUI.shortenToFit(full, component, width);
                int dots = text.indexOf("...");
                if (dots >= 0) {
                    assertEquals(0, dots % character.length());
                    assertEquals(0, (text.length() - dots - 3) % character.length());
                } else {
                    assertTrue(text.equals(full) || text.isEmpty() || text.equals(".") || text.equals(".."));
                }
            }
        }
    }

    @Test
    public void testLongTextIsShortened() {
        String shortened = GUI.shortenToFit("W".repeat(100_000), component, 30_000);
        assertTrue(shortened.contains("..."));
        assertTrue(metrics.stringWidth(shortened) <= 30_000);
    }
}
