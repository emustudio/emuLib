/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void testFontSizeConstants() {
        assertEquals(12, Constants.FONT_DEFAULT_SIZE);
        assertEquals(18, Constants.FONT_BIG_SIZE);
    }

    @Test
    public void testFontMonospaced() {
        assertNotNull(Constants.FONT_MONOSPACED);
        assertEquals(Font.MONOSPACED, Constants.FONT_MONOSPACED.getFamily());
        assertEquals(Font.PLAIN, Constants.FONT_MONOSPACED.getStyle());
        assertEquals(Constants.FONT_DEFAULT_SIZE, Constants.FONT_MONOSPACED.getSize());
    }

    @Test
    public void testFontMonospacedBold() {
        assertNotNull(Constants.FONT_MONOSPACED_BOLD);
        assertEquals(Font.MONOSPACED, Constants.FONT_MONOSPACED_BOLD.getFamily());
        assertEquals(Font.BOLD, Constants.FONT_MONOSPACED_BOLD.getStyle());
        assertEquals(Constants.FONT_DEFAULT_SIZE, Constants.FONT_MONOSPACED_BOLD.getSize());
    }

    @Test
    public void testFontMonospacedBigBold() {
        assertNotNull(Constants.FONT_MONOSPACED_BIG_BOLD);
        assertEquals(Font.MONOSPACED, Constants.FONT_MONOSPACED_BIG_BOLD.getFamily());
        assertEquals(Font.BOLD, Constants.FONT_MONOSPACED_BIG_BOLD.getStyle());
        assertEquals(Constants.FONT_BIG_SIZE, Constants.FONT_MONOSPACED_BIG_BOLD.getSize());
    }

    @Test
    public void testFontCommon() {
        assertNotNull(Constants.FONT_COMMON);
        assertEquals(Font.DIALOG, Constants.FONT_COMMON.getFamily());
        assertEquals(Font.PLAIN, Constants.FONT_COMMON.getStyle());
        assertEquals(Constants.FONT_DEFAULT_SIZE, Constants.FONT_COMMON.getSize());
    }

    @Test
    public void testFontTitleBorder() {
        assertNotNull(Constants.FONT_TITLE_BORDER);
        assertEquals(Font.SANS_SERIF, Constants.FONT_TITLE_BORDER.getFamily());
        assertEquals(Font.BOLD, Constants.FONT_TITLE_BORDER.getStyle());
        assertEquals(Constants.FONT_DEFAULT_SIZE, Constants.FONT_TITLE_BORDER.getSize());
    }

    @Test
    public void testTableColors() {
        assertNotNull(Constants.TABLE_COLOR_ROW_EVEN);
        assertEquals(new Color(241, 245, 250), Constants.TABLE_COLOR_ROW_EVEN);
        
        assertNotNull(Constants.TABLE_COLOR_ROW_ODD);
        assertEquals(Color.WHITE, Constants.TABLE_COLOR_ROW_ODD);
        
        assertNotNull(Constants.TABLE_COLOR_TABLE_GRID);
        assertEquals(new Color(0xd9d9d9), Constants.TABLE_COLOR_TABLE_GRID);
    }

    @Test
    public void testCpuRunStateColor() {
        assertNotNull(Constants.CPU_RUN_STATE_COLOR);
        assertEquals(new Color(0, 153, 51), Constants.CPU_RUN_STATE_COLOR);
    }

    @Test
    public void testAllConstantsNotNull() {
        assertNotNull(Constants.FONT_MONOSPACED);
        assertNotNull(Constants.FONT_MONOSPACED_BOLD);
        assertNotNull(Constants.FONT_MONOSPACED_BIG_BOLD);
        assertNotNull(Constants.FONT_COMMON);
        assertNotNull(Constants.FONT_TITLE_BORDER);
        assertNotNull(Constants.CPU_RUN_STATE_COLOR);
        assertNotNull(Constants.TABLE_COLOR_ROW_EVEN);
        assertNotNull(Constants.TABLE_COLOR_ROW_ODD);
        assertNotNull(Constants.TABLE_COLOR_TABLE_GRID);
    }
}
