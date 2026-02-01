/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

public class FadingBorderTest {

    @Test
    public void testConstructor() {
        FadingBorder border = new FadingBorder(10, Color.WHITE);
        assertNotNull(border);
    }

    @Test
    public void testGetBorderInsets() {
        FadingBorder border = new FadingBorder(10, Color.WHITE);
        Insets insets = border.getBorderInsets(null);

        assertEquals(10, insets.top);
        assertEquals(10, insets.left);
        assertEquals(10, insets.bottom);
        assertEquals(10, insets.right);
    }

    @Test
    public void testGetBorderInsetsWithDifferentThickness() {
        FadingBorder border = new FadingBorder(5, Color.RED);
        Insets insets = border.getBorderInsets(null);

        assertEquals(5, insets.top);
        assertEquals(5, insets.left);
        assertEquals(5, insets.bottom);
        assertEquals(5, insets.right);
    }

    @Test
    public void testIsBorderOpaque() {
        FadingBorder border = new FadingBorder(10, Color.WHITE);
        assertFalse(border.isBorderOpaque());
    }

    @Test
    public void testPaintBorderCreatesImage() {
        FadingBorder border = new FadingBorder(10, Color.BLUE);

        // Create a test graphics context
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        // Paint the border
        border.paintBorder(null, g, 0, 0, 100, 100);

        // Verify the image was modified (not all pixels are transparent)
        boolean hasPixels = false;
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) != 0) {
                    hasPixels = true;
                    break;
                }
            }
            if (hasPixels) break;
        }

        assertTrue("Border should paint pixels", hasPixels);
        g.dispose();
    }

    @Test
    public void testPaintBorderCaching() {
        FadingBorder border = new FadingBorder(5, Color.RED);

        BufferedImage image1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics g1 = image1.getGraphics();

        // Paint twice with same dimensions
        border.paintBorder(null, g1, 0, 0, 50, 50);
        int[][] pixels1 = capturePixels(image1, 50, 50);

        BufferedImage image2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics g2 = image2.getGraphics();
        border.paintBorder(null, g2, 0, 0, 50, 50);
        int[][] pixels2 = capturePixels(image2, 50, 50);

        // Results should be identical (cached)
        assertArrayEquals(pixels1, pixels2);

        g1.dispose();
        g2.dispose();
    }

    @Test
    public void testBorderWithZeroThickness() {
        FadingBorder border = new FadingBorder(0, Color.BLACK);
        Insets insets = border.getBorderInsets(null);

        assertEquals(0, insets.top);
        assertEquals(0, insets.left);
        assertEquals(0, insets.bottom);
        assertEquals(0, insets.right);
    }

    @Test
    public void testBorderWithDifferentColors() {
        FadingBorder whiteBorder = new FadingBorder(10, Color.WHITE);
        FadingBorder blackBorder = new FadingBorder(10, Color.BLACK);

        BufferedImage whiteImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blackImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);

        whiteBorder.paintBorder(null, whiteImage.getGraphics(), 0, 0, 50, 50);
        blackBorder.paintBorder(null, blackImage.getGraphics(), 0, 0, 50, 50);

        // Border colors should produce different results
        boolean different = false;
        for (int x = 0; x < 50 && !different; x++) {
            for (int y = 0; y < 50 && !different; y++) {
                int argb1 = whiteImage.getRGB(x, y);
                int argb2 = blackImage.getRGB(x, y);
                if ((argb1 & 0x00FFFFFF) != (argb2 & 0x00FFFFFF)) {
                    different = true;
                }
            }
        }

        assertTrue("Different colors should produce different borders", different);
    }

    private int[][] capturePixels(BufferedImage image, int width, int height) {
        int[][] pixels = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[x][y] = image.getRGB(x, y);
            }
        }
        return pixels;
    }
}
