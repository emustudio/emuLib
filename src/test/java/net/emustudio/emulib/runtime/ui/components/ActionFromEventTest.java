/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ActionFromEventTest {

    @Test
    public void testActionPerformedWithName() {
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        ActionFromEvent action = new ActionFromEvent(
                e -> actionCalled.set(true),
                "Test Action",
                "test.png",
                "Test tooltip"
        );

        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test"));
        assertTrue(actionCalled.get());
    }

    @Test
    public void testActionPerformedWithoutName() {
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        ActionFromEvent action = new ActionFromEvent(
                e -> actionCalled.set(true),
                "test.png",
                "Test tooltip"
        );

        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test"));
        assertTrue(actionCalled.get());
    }

    @Test
    public void testActionReceivesEvent() {
        AtomicReference<ActionEvent> receivedEvent = new AtomicReference<>();
        ActionFromEvent action = new ActionFromEvent(
                e -> receivedEvent.set(e),
                "Test Action",
                "test.png",
                "Test tooltip"
        );

        ActionEvent testEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test");
        action.actionPerformed(testEvent);

        assertNotNull(receivedEvent.get());
        assertEquals(testEvent, receivedEvent.get());
    }

    @Test
    public void testGetValue() {
        ActionFromEvent action = new ActionFromEvent(
                e -> {},
                "Test Action",
                "test.png",
                "Test tooltip"
        );

        assertEquals("Test Action", action.getValue(Action.NAME));
        assertEquals("Test tooltip", action.getValue(Action.SHORT_DESCRIPTION));
        // Icon may be null in test environment where resources are not available
    }

    @Test
    public void testGetValueWithoutName() {
        ActionFromEvent action = new ActionFromEvent(
                e -> {},
                "test.png",
                "Test tooltip"
        );

        assertNull(action.getValue(Action.NAME));
        assertEquals("Test tooltip", action.getValue(Action.SHORT_DESCRIPTION));
        // Icon may be null in test environment where resources are not available
    }

    @Test
    public void testMultipleInvocations() {
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        ActionFromEvent action = new ActionFromEvent(
                e -> counter.set(counter.get() + 1),
                "test.png",
                "Test tooltip"
        );

        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test1"));
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test2"));
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test3"));

        assertEquals(3, counter.get().intValue());
    }

    @Test
    public void testActionWithComplexLogic() {
        AtomicReference<String> result = new AtomicReference<>("");
        ActionFromEvent action = new ActionFromEvent(
                e -> {
                    String command = e.getActionCommand();
                    result.set("Processed: " + command);
                },
                "Complex Action",
                "test.png",
                "Complex tooltip"
        );

        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "data"));
        assertEquals("Processed: data", result.get());
    }
}
