// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractMemoryTest {
    private AbstractMemoryStub memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryStub(0L);
    }

    @Test
    public void testProgramStartIsZeroAfterStart() {
        assertEquals(0, memory.getProgramLocation());
    }

    @Test
    public void testSetAndGetProgramStart() {
        memory.setProgramLocation(555);
        assertEquals(555, memory.getProgramLocation());
    }
}
