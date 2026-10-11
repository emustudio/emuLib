/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import org.junit.Test;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class GUIIconTest {
    private static final String ICON = "/net/emustudio/emulib/runtime/ui/icons/icon.png";

    @Test
    public void testHighResolutionImageIsUsedWhenPaintingAtDoubleScale() {
        ImageIcon icon = GUI.loadIcon(ICON, GUIIconTest.class);
        assertEquals(8, icon.getIconWidth());
        assertEquals(8, icon.getIconHeight());
        assertTrue(icon.getImage() instanceof MultiResolutionImage);
        assertEquals(2, ((MultiResolutionImage) icon.getImage()).getResolutionVariants().size());

        BufferedImage output = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        try {
            icon.paintIcon(null, graphics, 0, 0);
            assertEquals(Color.RED.getRGB(), output.getRGB(4, 4));
            graphics.scale(2, 2);
            icon.paintIcon(null, graphics, 0, 0);
            assertEquals(Color.BLUE.getRGB(), output.getRGB(8, 8));
        } finally {
            graphics.dispose();
        }
    }

    @Test
    public void testCallingClassIsUsedForRelativeResources() {
        ImageIcon icon = GUI.loadIcon("icons/icon.png");
        assertTrue(icon.getImage() instanceof MultiResolutionImage);
        assertEquals(8, icon.getIconWidth());
    }

    @Test
    public void testNullCallerUsesGuiClass() {
        ImageIcon icon = GUI.loadIcon("icons/icon.png", null);
        assertTrue(icon.getImage() instanceof MultiResolutionImage);
    }

    @Test
    public void testMissingHighResolutionResourceUsesBaseImage() {
        ImageIcon icon = GUI.loadIcon("icons/single.png", GUIIconTest.class);
        assertEquals(8, icon.getIconWidth());
        assertEquals(8, icon.getIconHeight());
        assertFalse(icon.getImage() instanceof MultiResolutionImage);
    }

    @Test
    public void testMissingBaseResourceReturnsNull() {
        assertNull(GUI.loadIcon("icons/missing.png", GUIIconTest.class));
    }

    @Test
    public void testStandardIconsHaveHighResolutionVariants() throws IllegalAccessException {
        for (Field field : StandardIcons.class.getFields()) {
            if (!field.getName().startsWith("ICON_")) {
                continue;
            }
            ImageIcon icon = GUI.loadIcon((String) field.get(null), StandardIcons.class);
            assertNotNull(field.getName(), icon);
            assertEquals(field.getName(), 22, icon.getIconWidth());
            assertEquals(field.getName(), 22, icon.getIconHeight());
            assertTrue(field.getName(), icon.getImage() instanceof MultiResolutionImage);
        }
    }

    @Test
    public void testHighResolutionResourceAloneDoesNotReplaceMissingBase() {
        assertNotNull(GUIIconTest.class.getResource("icons/orphan@2x.png"));
        assertNull(GUI.loadIcon("icons/orphan.png", GUIIconTest.class));
    }
}
