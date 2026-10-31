/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.ui.debugger.CannotSetDebuggerValueException;
import org.junit.Test;

import static org.junit.Assert.*;

public class DebuggerColumnContractTest {
    @Test
    public void testPluginsCanCreateEditErrorsWithAndWithoutCauses() {
        CannotSetDebuggerValueException error = new CannotSetDebuggerValueException("Register is read-only");
        assertEquals("Register is read-only", error.getMessage());
        assertNull(error.getCause());

        Exception cause = new Exception("Invalid register value");
        error = new CannotSetDebuggerValueException("Cannot update register", cause);
        assertEquals("Cannot update register", error.getMessage());
        assertSame(cause, error.getCause());
    }
}
