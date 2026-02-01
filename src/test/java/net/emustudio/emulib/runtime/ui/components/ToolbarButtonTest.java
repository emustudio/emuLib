/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ToolbarButtonTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        // Enable headless mode for testing
        System.setProperty("java.awt.headless", "false");
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            window.cleanUp();
        }
    }

    @Test
    public void testConstructorWithAction() {
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCalled.set(true);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Test tooltip");

        ToolbarButton button = GuiActionRunner.execute(() -> new ToolbarButton(action));

        assertNotNull(button);
        assertEquals("Test tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testConstructorWithActionAndIcon() {
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCalled.set(true);
            }
        };

        ToolbarButton button = GuiActionRunner.execute(() ->
            new ToolbarButton(action, "test.png", "Custom tooltip")
        );

        assertNotNull(button);
        assertEquals("Custom tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testConstructorWithConsumer() {
        AtomicInteger counter = new AtomicInteger(0);

        ToolbarButton button = GuiActionRunner.execute(() ->
            new ToolbarButton(e -> counter.incrementAndGet(), "test.png", "Counter button")
        );

        assertNotNull(button);
        assertEquals("Counter button", button.getToolTipText());
        assertFalse(button.isFocusable());

        // Trigger action
        GuiActionRunner.execute(() -> button.doClick());
        assertEquals(1, counter.get());
    }

    @Test
    public void testButtonIsNotFocusable() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarButton button = GuiActionRunner.execute(() -> new ToolbarButton(action));
        assertFalse(button.isFocusable());
    }

    @Test
    public void testButtonHidesText() {
        Action action = new AbstractAction("Visible Text") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarButton button = GuiActionRunner.execute(() -> new ToolbarButton(action));
        assertTrue(button.getHideActionText());
    }

    @Test
    public void testButtonClientProperty() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarButton button = GuiActionRunner.execute(() -> new ToolbarButton(action));
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testActionIsTriggered() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCounter.incrementAndGet();
            }
        };

        ToolbarButton button = GuiActionRunner.execute(() -> new ToolbarButton(action));

        // Click the button multiple times
        GuiActionRunner.execute(() -> {
            button.doClick();
            button.doClick();
            button.doClick();
        });

        assertEquals(3, actionCounter.get());
    }
}
