/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ReadWriteLockSupportTest {
    private final ReadWriteLockSupport rwl = new ReadWriteLockSupport();

    @Test
    public void testActionsRunAndReturnResults() {
        AtomicInteger value = new AtomicInteger();
        rwl.lockWrite(() -> value.set(5));
        rwl.lockRead(() -> value.incrementAndGet());
        assertEquals(6, (int) rwl.lockRead(value::get));
        assertEquals(7, (int) rwl.lockWrite(value::incrementAndGet));
    }

    @Test
    public void testCheckedExceptionIsRethrownUnchanged() {
        IOException failure = new IOException("test");
        try {
            rwl.lockWrite(() -> {
                throw failure;
            });
            fail("Exception expected");
        } catch (Exception e) {
            assertSame(failure, e);
        }
    }
}
