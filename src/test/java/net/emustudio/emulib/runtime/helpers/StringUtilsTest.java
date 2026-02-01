/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void testTextIsShortened() {
        String result = StringUtils.shorten("This is a long text", 10);
        assertEquals("Thi...text", result);
    }

    @Test
    public void testTextIsPreserved() {
        String result = StringUtils.shorten("This is a long text", 30);
        assertEquals("This is a long text", result);
    }
}
