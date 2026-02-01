/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ToolbarButtonTest {

    @Test
    public void testConstructorWithActionOnly() {
        AtomicInteger counter = new AtomicInteger(0);
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {
                counter.incrementAndGet();
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Test tooltip");
        
        ToolbarButton button = new ToolbarButton(action);
        
        assertNotNull(button);
        assertFalse(button.isFocusable());
        assertEquals("Test tooltip", button.getToolTipText());
        assertEquals(action, button.getAction());
    }

    @Test
    public void testConstructorWithIconResource() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        Action action = new AbstractAction("Test Action") {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCounter.incrementAndGet();
            }
        };

        ToolbarButton button = new ToolbarButton(action, "/icon.png", "Test tooltip");
        
        assertNotNull(button);
        assertFalse(button.isFocusable());
        assertEquals("Test tooltip", button.getToolTipText());
    }

    @Test
    public void testActionIsTriggered() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        Action action = new AbstractAction("Test Action") {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCounter.incrementAndGet();
            }
        };

        ToolbarButton button = new ToolbarButton(action);
        button.doClick();
        
        assertEquals(1, actionCounter.get());
    }

    @Test
    public void testMultipleClicks() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        Action action = new AbstractAction("Test Action") {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCounter.incrementAndGet();
            }
        };

        ToolbarButton button = new ToolbarButton(action);
        button.doClick();
        button.doClick();
        button.doClick();
        
        assertEquals(3, actionCounter.get());
    }

    @Test
    public void testConstructorWithConsumer() {
        AtomicInteger actionCounter = new AtomicInteger(0);

        ToolbarButton button = new ToolbarButton(e -> actionCounter.incrementAndGet(), "/icon.png", "Test");
        
        assertNotNull(button);
        assertEquals("Test", button.getToolTipText());
        assertFalse(button.isFocusable());
        
        button.doClick();
        assertEquals(1, actionCounter.get());
    }

    @Test
    public void testClientProperty() {
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };

        ToolbarButton button = new ToolbarButton(action);
        
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testNotFocusable() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        ToolbarButton button = new ToolbarButton(action);
        assertFalse(button.isFocusable());
        
        button = new ToolbarButton(e -> {}, "/icon.png", "Test");
        assertFalse(button.isFocusable());
    }
}
