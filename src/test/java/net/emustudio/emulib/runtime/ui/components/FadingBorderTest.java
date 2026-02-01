/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

public class FadingBorderTest {

    @Test
    public void testConstructorDefault() {
        FadingBorder border = new FadingBorder(1, Color.BLACK);
        
        assertNotNull(border);
        Insets insets = border.getBorderInsets(null);
        assertEquals(1, insets.top);
        assertEquals(1, insets.left);
        assertEquals(1, insets.bottom);
        assertEquals(1, insets.right);
    }

    @Test
    public void testConstructorWithThickness() {
        FadingBorder border = new FadingBorder(5, Color.RED);
        
        Insets insets = border.getBorderInsets(null);
        assertEquals(5, insets.top);
        assertEquals(5, insets.left);
        assertEquals(5, insets.bottom);
        assertEquals(5, insets.right);
    }

    @Test
    public void testBorderInsets() {
        FadingBorder border = new FadingBorder(3, Color.BLUE);
        Component component = new java.awt.Canvas();
        
        Insets insets = border.getBorderInsets(component);
        assertEquals(3, insets.top);
        assertEquals(3, insets.left);
        assertEquals(3, insets.bottom);
        assertEquals(3, insets.right);
    }

    @Test
    public void testBorderInsetsConsistency() {
        FadingBorder border = new FadingBorder(2, Color.GREEN);
        
        Insets insets1 = border.getBorderInsets(null);
        Insets insets2 = border.getBorderInsets(new java.awt.Canvas());
        
        assertEquals(insets1.top, insets2.top);
        assertEquals(insets1.left, insets2.left);
        assertEquals(insets1.bottom, insets2.bottom);
        assertEquals(insets1.right, insets2.right);
    }

    @Test
    public void testOpaque() {
        FadingBorder border = new FadingBorder(1, Color.BLACK);
        
        assertFalse(border.isBorderOpaque());
    }

    @Test
    public void testPaintBorder() {
        FadingBorder border = new FadingBorder(2, Color.RED);
        
        // Create a BufferedImage for graphics context
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        // Paint the border
        border.paintBorder(null, g, 0, 0, 100, 100);
        
        // Verify that pixels are painted (not all transparent)
        boolean hasColoredPixels = false;
        for (int y = 0; y < 2 && !hasColoredPixels; y++) {
            for (int x = 0; x < 100; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                if (alpha > 0) {
                    hasColoredPixels = true;
                    break;
                }
            }
        }
        
        assertTrue("Border should paint colored pixels", hasColoredPixels);
        g.dispose();
    }

    @Test
    public void testPaintBorderCaching() {
        FadingBorder border = new FadingBorder(3, Color.BLUE);
        
        BufferedImage image1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = image1.createGraphics();
        border.paintBorder(null, g1, 0, 0, 100, 100);
        g1.dispose();
        
        BufferedImage image2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image2.createGraphics();
        border.paintBorder(null, g2, 0, 0, 100, 100);
        g2.dispose();
        
        // Both images should have painted pixels
        assertTrue(hasPixelsWithAlpha(image1));
        assertTrue(hasPixelsWithAlpha(image2));
    }

    @Test
    public void testDifferentSizes() {
        FadingBorder border = new FadingBorder(1, Color.GREEN);
        
        // Just test that paintBorder doesn't throw an exception with different sizes
        BufferedImage smallImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = smallImage.createGraphics();
        try {
            border.paintBorder(null, g1, 0, 0, 50, 50);
        } finally {
            g1.dispose();
        }
        
        BufferedImage largeImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = largeImage.createGraphics();
        try {
            border.paintBorder(null, g2, 0, 0, 200, 200);
        } finally {
            g2.dispose();
        }
        
        // If we got here without exceptions, the test passes
        assertTrue(true);
    }

    @Test
    public void testDifferentColors() {
        FadingBorder blackBorder = new FadingBorder(1, Color.BLACK);
        FadingBorder redBorder = new FadingBorder(1, Color.RED);
        
        // Just test that paintBorder doesn't throw an exception
        BufferedImage blackImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = blackImage.createGraphics();
        try {
            blackBorder.paintBorder(null, g1, 0, 0, 100, 100);
        } finally {
            g1.dispose();
        }
        
        BufferedImage redImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = redImage.createGraphics();
        try {
            redBorder.paintBorder(null, g2, 0, 0, 100, 100);
        } finally {
            g2.dispose();
        }
        
        // If we got here without exceptions, the test passes
        assertTrue(true);
    }

    private boolean hasPixelsWithAlpha(BufferedImage image) {
        for (int y = 0; y < Math.min(10, image.getHeight()); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                if (alpha > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
