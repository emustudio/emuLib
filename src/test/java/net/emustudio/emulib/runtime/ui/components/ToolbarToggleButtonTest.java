/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ToolbarToggleButtonTest extends AssertJSwingJUnitTestCase {

    @Override
    protected void onSetUp() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    public void testConstructorWithAction() {
        Action action = new AbstractAction("Toggle Test") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Toggle tooltip");

        ToolbarToggleButton button = GuiActionRunner.execute(() -> new ToolbarToggleButton(action));

        assertNotNull(button);
        assertEquals("Toggle tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testConstructorWithActionAndIcon() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarToggleButton button = GuiActionRunner.execute(() ->
            new ToolbarToggleButton(action, "test.png", "Toggle tooltip")
        );

        assertNotNull(button);
        assertEquals("Toggle tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
    }

    @Test
    public void testConstructorWithConsumers() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        AtomicInteger itemCounter = new AtomicInteger(0);

        ToolbarToggleButton button = GuiActionRunner.execute(() ->
            new ToolbarToggleButton(
                e -> actionCounter.incrementAndGet(),
                e -> itemCounter.incrementAndGet(),
                "test.png",
                "Toggle with consumers"
            )
        );

        assertNotNull(button);
        assertEquals("Toggle with consumers", button.getToolTipText());

        // Click to select
        GuiActionRunner.execute(() -> button.doClick());
        assertTrue(actionCounter.get() > 0);
        assertTrue(itemCounter.get() > 0);
    }

    @Test
    public void testToggleState() {
        ToolbarToggleButton button = GuiActionRunner.execute(() ->
            new ToolbarToggleButton(e -> {}, "test.png", "Toggle state test")
        );

        assertFalse(button.isSelected());

        // Toggle on
        GuiActionRunner.execute(() -> button.setSelected(true));
        assertTrue(button.isSelected());

        // Toggle off
        GuiActionRunner.execute(() -> button.setSelected(false));
        assertFalse(button.isSelected());
    }

    @Test
    public void testItemStateChange() {
        AtomicBoolean selected = new AtomicBoolean(false);
        AtomicBoolean deselected = new AtomicBoolean(false);

        ToolbarToggleButton button = GuiActionRunner.execute(() ->
            new ToolbarToggleButton(
                e -> {},
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        selected.set(true);
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        deselected.set(true);
                    }
                },
                "test.png",
                "Item state test"
            )
        );

        // Select
        GuiActionRunner.execute(() -> button.setSelected(true));
        assertTrue(selected.get());

        // Deselect
        GuiActionRunner.execute(() -> button.setSelected(false));
        assertTrue(deselected.get());
    }

    @Test
    public void testButtonIsNotFocusable() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarToggleButton button = GuiActionRunner.execute(() -> new ToolbarToggleButton(action));
        assertFalse(button.isFocusable());
    }

    @Test
    public void testButtonHidesText() {
        Action action = new AbstractAction("Visible Text") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarToggleButton button = GuiActionRunner.execute(() -> new ToolbarToggleButton(action));
        assertTrue(button.getHideActionText());
    }

    @Test
    public void testMultipleToggles() {
        AtomicInteger toggleCount = new AtomicInteger(0);

        ToolbarToggleButton button = GuiActionRunner.execute(() ->
            new ToolbarToggleButton(
                e -> {},
                e -> toggleCount.incrementAndGet(),
                "test.png",
                "Multiple toggles"
            )
        );

        GuiActionRunner.execute(() -> {
            button.doClick(); // Select
            button.doClick(); // Deselect
            button.doClick(); // Select
            button.doClick(); // Deselect
        });

        assertEquals(4, toggleCount.get());
    }
}
