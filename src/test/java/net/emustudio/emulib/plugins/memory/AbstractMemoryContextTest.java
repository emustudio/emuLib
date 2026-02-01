/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class AbstractMemoryContextTest {
    private AbstractMemoryContext<?> memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryContextStub();
    }

    @Test
    public void testListenerIsCalledAfterNotifyChange() {
        int memoryPosition = 199;

        MemoryContext.MemoryListener listener = EasyMock.createNiceMock(MemoryContext.MemoryListener.class);
        listener.memoryContentChanged(eq(memoryPosition), eq(memoryPosition));
        expectLastCall().once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemoryContentChanged(memoryPosition);

        verify(listener);
    }

    @Test
    public void testListenerIsNotCalledAfterItsRemoval() {
        MemoryContext.MemoryListener listener = EasyMock.createNiceMock(MemoryContext.MemoryListener.class);
        replay(listener);

        memory.addMemoryListener(listener);
        memory.removeMemoryListener(listener);
        memory.notifyMemoryContentChanged(234);

        verify(listener);
    }

    @Test
    public void testNotifyMemChangedDoesNotThrow() {
        MemoryContext.MemoryListener listener = EasyMock.createNiceMock(MemoryContext.MemoryListener.class);
        listener.memoryContentChanged(234, 236);
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemoryContentChanged(234, 236);

        verify(listener);
    }

    @Test
    public void testNotifyMemSizeChangedDoesNotThrow() {
        MemoryContext.MemoryListener listener = EasyMock.createNiceMock(MemoryContext.MemoryListener.class);
        listener.memorySizeChanged();
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemorySizeChanged();

        verify(listener);
    }
}
