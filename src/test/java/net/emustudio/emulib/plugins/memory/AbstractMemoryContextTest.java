/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.memory;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class AbstractMemoryContextTest {
    private AbstractMemoryContext memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryContextStub();
    }

    @Test
    public void testListenerIsCalledAfterNotifyChange() {
        int memoryPosition = 199;

        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        listener.memoryChanged(eq(memoryPosition));
        expectLastCall().once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemoryChanged(memoryPosition);

        verify(listener);
    }

    @Test
    public void testListenerIsNotCalledAfterItsRemoval() {
        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        replay(listener);

        memory.addMemoryListener(listener);
        memory.removeMemoryListener(listener);
        memory.notifyMemoryChanged(234);

        verify(listener);
    }

    @Test
    public void testNotifyMemChangedDoesNotThrow() throws Exception {
        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        listener.memoryChanged(anyInt());
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemoryChanged(234);

        verify(listener);
    }

    @Test
    public void testNotifyMemSizeChangedDoesNotThrow() throws Exception {
        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        listener.memorySizeChanged();
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemorySizeChanged();

        verify(listener);
    }

}
