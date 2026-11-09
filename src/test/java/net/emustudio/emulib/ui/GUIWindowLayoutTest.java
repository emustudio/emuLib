/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.ui;

import net.emustudio.emulib.application.ui.GUI;

import net.emustudio.emulib.ui.components.DialogBase;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class GUIWindowLayoutTest {
    private Window window;

    @Before
    public void setUp() {
        Assume.assumeFalse("Skipping: headless environment", GraphicsEnvironment.isHeadless());
    }

    @After
    public void tearDown() throws Exception {
        if (window != null) SwingUtilities.invokeAndWait(window::dispose);
    }

    @Test(timeout = 10000)
    public void frameUsesActualInsetsAsSoonAsShowReturns() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = createFrame();
            frame.setVisible(true);

            assertRootLayout(frame, frame.getRootPane());
        });
    }

    @Test(timeout = 10000)
    public void frameRelayoutsAfterHidingAndAfterDisposal() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = createFrame();
            frame.setVisible(true);
            frame.setVisible(false);
            frame.setVisible(true);
            assertRootLayout(frame, frame.getRootPane());

            frame.dispose();
            frame.setVisible(true);
            assertRootLayout(frame, frame.getRootPane());
        });
    }

    @Test(timeout = 10000)
    public void hidingDoesNotRelayoutUsingHiddenWindowInsets() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = createFrame();
            frame.setVisible(true);
            Rectangle shownBounds = frame.getRootPane().getBounds();

            frame.setVisible(false);

            assertEquals(shownBounds, frame.getRootPane().getBounds());
        });
    }

    @Test(timeout = 10000)
    public void modelessDialogUsesActualInsetsIncludingAfterDisposal() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TestDialog dialog = new TestDialog(false);
            window = dialog;
            dialog.setVisible(true);
            assertRootLayout(dialog, dialog.getRootPane());

            dialog.dispose();
            dialog.setVisible(true);
            assertRootLayout(dialog, dialog.getRootPane());
        });
    }

    @Test(timeout = 10000)
    public void modalDialogRelayoutsBeforeShowReturnsIncludingAfterDisposal() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TestDialog dialog = new TestDialog(true);
            window = dialog;
            for (int show = 0; show < 2; show++) {
                AtomicReference<AssertionError> failure = new AtomicReference<>();
                SwingUtilities.invokeLater(() -> {
                    try {
                        assertTrue(dialog.isShowing());
                        assertRootLayout(dialog, dialog.getRootPane());
                    } catch (AssertionError error) {
                        failure.set(error);
                    } finally {
                        dialog.dispose();
                    }
                });

                dialog.setVisible(true);

                assertFalse(dialog.isShowing());
                if (failure.get() != null) throw failure.get();
            }
        });
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Window layout test") {
            @Override public Insets getInsets() {
                return changingInsets(this);
            }
        };
        window = frame;
        frame.add(new JLabel("Content"));
        frame.pack();
        GUI.relayoutOnShow(frame);
        return frame;
    }

    private static void assertRootLayout(Window window, JRootPane root) {
        Insets insets = window.getInsets();
        assertEquals(new Point(insets.left, insets.top), root.getLocation());
    }

    private static Insets changingInsets(Window window) {
        return window.isVisible() ? new Insets(2, 1, 1, 1) : new Insets(25, 5, 5, 5);
    }

    private static class TestDialog extends DialogBase {
        TestDialog(boolean modal) {
            super(null, "Window layout test", modal);
            buildContent();
        }

        @Override public Insets getInsets() {
            return changingInsets(this);
        }

        @Override protected JComponent initializeComponents() {
            return new JLabel("Content");
        }
    }
}
