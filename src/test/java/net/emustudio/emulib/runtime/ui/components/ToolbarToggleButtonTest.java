/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ToolbarToggleButtonTest {

    @Test
    public void testConstructorWithActionOnly() {
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Test tooltip");
        
        ToolbarToggleButton button = new ToolbarToggleButton(action);
        
        assertNotNull(button);
        assertEquals("Test tooltip", button.getToolTipText());
        assertFalse(button.isSelected());
        assertFalse(button.isFocusable());
    }

    @Test
    public void testConstructorWithIconResource() {
        AtomicInteger actionCounter = new AtomicInteger(0);
        
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCounter.incrementAndGet();
            }
        };

        ToolbarToggleButton button = new ToolbarToggleButton(action, "/icon.png", "Test tooltip");
        
        assertNotNull(button);
        assertEquals("Test tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
    }

    @Test
    public void testToggle() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        ToolbarToggleButton button = new ToolbarToggleButton(action);
        
        assertFalse(button.isSelected());
        
        button.doClick();
        assertTrue(button.isSelected());
        
        button.doClick();
        assertFalse(button.isSelected());
    }

    @Test
    public void testItemStateChange() {
        AtomicReference<ItemEvent> capturedEvent = new AtomicReference<>();
        Consumer<ItemEvent> itemConsumer = capturedEvent::set;

        ToolbarToggleButton button = new ToolbarToggleButton(e -> {}, itemConsumer, "/icon.png", "Test");
        
        button.doClick();
        
        ItemEvent event = capturedEvent.get();
        assertNotNull(event);
        assertEquals(ItemEvent.SELECTED, event.getStateChange());
        assertEquals(button, event.getSource());
    }

    @Test
    public void testItemStateChangeDeselected() {
        AtomicReference<ItemEvent> capturedEvent = new AtomicReference<>();
        Consumer<ItemEvent> itemConsumer = capturedEvent::set;

        ToolbarToggleButton button = new ToolbarToggleButton(e -> {}, itemConsumer, "/icon.png", "Test");
        
        button.setSelected(true);
        button.doClick();
        
        ItemEvent event = capturedEvent.get();
        assertNotNull(event);
        assertEquals(ItemEvent.DESELECTED, event.getStateChange());
    }

    @Test
    public void testConstructorWithConsumer() {
        AtomicInteger counter = new AtomicInteger(0);
        
        ToolbarToggleButton button = new ToolbarToggleButton(e -> counter.incrementAndGet(), "/icon.png", "Test");
        
        assertNotNull(button);
        assertEquals("Test", button.getToolTipText());
        assertFalse(button.isFocusable());
        
        button.doClick();
        assertEquals(1, counter.get());
    }

    @Test
    public void testClientProperty() {
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        
        ToolbarToggleButton button = new ToolbarToggleButton(action);
        assertEquals("toolBarButton", button.getClientProperty("JButton.buttonType"));
    }

    @Test
    public void testNotFocusable() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {}
        };
        ToolbarToggleButton button = new ToolbarToggleButton(action);
        assertFalse(button.isFocusable());
        
        button = new ToolbarToggleButton(e -> {}, "/icon.png", "Test");
        assertFalse(button.isFocusable());
    }
}
