/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

public class GUIBackgroundTaskTest {
    private final Action action = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent event) {
        }
    };

    @Test
    public void testWorkerCallKeepsActionChangesAndCallbacksOnEdt() throws Exception {
        CountDownLatch restored = new CountDownLatch(1);
        AtomicBoolean changesOnEdt = new AtomicBoolean(true);
        AtomicBoolean taskOffEdt = new AtomicBoolean();
        AtomicBoolean successOnEdt = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        action.addPropertyChangeListener(event -> {
            if ("enabled".equals(event.getPropertyName())) {
                if (!SwingUtilities.isEventDispatchThread()) changesOnEdt.set(false);
                if (Boolean.TRUE.equals(event.getNewValue())) restored.countDown();
            }
        });

        GUI.runInBackground(action, () -> taskOffEdt.set(!SwingUtilities.isEventDispatchThread()),
                () -> successOnEdt.set(SwingUtilities.isEventDispatchThread()), failure::set);

        assertTrue(restored.await(5, SECONDS));
        assertTrue(changesOnEdt.get());
        assertTrue(taskOffEdt.get());
        assertTrue(successOnEdt.get());
        assertNull(failure.get());
        assertTrue(action.isEnabled());
    }

    @Test
    public void testBusyActionDoesNotStartAnotherTask() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch duplicateStarted = new CountDownLatch(1);
        CountDownLatch restored = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        action.addPropertyChangeListener(event -> {
            if ("enabled".equals(event.getPropertyName()) && Boolean.TRUE.equals(event.getNewValue())) {
                restored.countDown();
            }
        });
        try {
            SwingUtilities.invokeAndWait(() -> {
                GUI.runInBackground(action, () -> {
                    started.countDown();
                    if (!release.await(5, SECONDS)) throw new AssertionError("Task was not released");
                }, () -> {}, failure::set);
                GUI.runInBackground(action, duplicateStarted::countDown, () -> {}, failure::set);
            });
            assertTrue(started.await(5, SECONDS));
            assertFalse(duplicateStarted.await(200, java.util.concurrent.TimeUnit.MILLISECONDS));
            assertFalse(action.isEnabled());
        } finally {
            release.countDown();
        }
        assertTrue(restored.await(5, SECONDS));
        assertNull(failure.get());
    }

    @Test
    public void testTaskFailureReportsOriginalCauseAndRestoresAction() throws Exception {
        Exception original = new Exception("Cannot read the selected file", new Exception("nested cause"));
        CountDownLatch restored = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean errorOnEdt = new AtomicBoolean();
        AtomicBoolean succeeded = new AtomicBoolean();
        action.addPropertyChangeListener(event -> {
            if ("enabled".equals(event.getPropertyName()) && Boolean.TRUE.equals(event.getNewValue())) {
                restored.countDown();
            }
        });
        SwingUtilities.invokeAndWait(() -> GUI.runInBackground(action, () -> { throw original; },
                () -> succeeded.set(true), error -> {
                    failure.set(error);
                    errorOnEdt.set(SwingUtilities.isEventDispatchThread());
                }));
        assertTrue(restored.await(5, SECONDS));
        assertSame(original, failure.get());
        assertTrue(errorOnEdt.get());
        assertFalse(succeeded.get());
        assertTrue(action.isEnabled());
    }

    @Test
    public void testSuccessCallbackFailureIsNotReplacedByItsCause() throws Exception {
        RuntimeException original = new RuntimeException("Cannot refresh the view", new Exception("nested cause"));
        CountDownLatch restored = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        action.addPropertyChangeListener(event -> {
            if ("enabled".equals(event.getPropertyName()) && Boolean.TRUE.equals(event.getNewValue())) {
                restored.countDown();
            }
        });
        SwingUtilities.invokeAndWait(() -> GUI.runInBackground(action, () -> {},
                () -> { throw original; }, failure::set));
        assertTrue(restored.await(5, SECONDS));
        assertSame(original, failure.get());
        assertTrue(action.isEnabled());
    }
}
