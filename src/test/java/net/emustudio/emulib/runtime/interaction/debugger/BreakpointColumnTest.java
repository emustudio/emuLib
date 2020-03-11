/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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
package net.emustudio.emulib.runtime.interaction.debugger;

import net.emustudio.emulib.plugins.cpu.CPU;
import org.junit.Test;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

public class BreakpointColumnTest {

    @Test(expected = NullPointerException.class)
    public void testNullCpuThrows() {
        new BreakpointColumn(null);
    }

    @Test
    public void testSetBreakpointIfCpuSupportsIt() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true).anyTimes();
        cpu.setBreakpoint(0x53);
        expectLastCall().once();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0x53, true);

        verify(cpu);
    }

    @Test
    public void testSetBreakpointIfCpuDoesNotSupportIt() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(false).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0x53, true);

        verify(cpu);
    }

    @Test
    public void testUnsetBreakpointIfCpuSupportsIt() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true).anyTimes();
        cpu.unsetBreakpoint(0x53);
        expectLastCall().once();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0x53, false);

        verify(cpu);
    }

    @Test
    public void testUnsetBreakpointIfCpuDoesNotSupportIt() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(false).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0x53, false);

        verify(cpu);
    }

    @Test(expected = NullPointerException.class)
    public void testUnsetBreakpointWhenUnparseableValueIsPassed() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0x53, null);
    }

    @Test
    public void testClassTypeIsString() {
        BreakpointColumn column = new BreakpointColumn(createMock(CPU.class));

        assertEquals(Boolean.class, column.getClassType());
    }

    @Test
    public void testBreakpointColumnTitleIsCorrect() {
        BreakpointColumn column = new BreakpointColumn(createMock(CPU.class));

        assertEquals("bp", column.getTitle());
    }

    @Test
    public void testGetBreakpointIfCpuSupportsIt() {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true).anyTimes();
        expect(cpu.isBreakpointSet(0x53)).andReturn(true).once();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        assertEquals(true, column.getValue(0x53));

        verify(cpu);
    }

    @Test
    public void testGetBreakpointIfCpuDoesNotSupportIt() {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(false).anyTimes();
        expect(cpu.isBreakpointSet(0x53)).andReturn(true).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        assertEquals(false, column.getValue(0x53));

        verify(cpu);
    }

    @Test
    public void testBreakpointColumnIsEditableIfCpuSupportsIt() {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        assertTrue(column.isEditable());

        verify(cpu);
    }

    @Test
    public void testBreakpointColumnIsNotEditableIfCpuDoesNotSupportIt() {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(false).anyTimes();
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        assertFalse(column.isEditable());

        verify(cpu);
    }

    @Test
    public void testDefaultWidthIsPredefined() {
        assertNotEquals(-1, new BreakpointColumn(createMock(CPU.class)).getDefaultWidth());
    }

    @Test(expected = CannotSetDebuggerValueException.class)
    public void testSetValueOnInvalidLocationThrows() throws Exception {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true);
        cpu.setBreakpoint(anyInt());
        expectLastCall().andThrow(new IndexOutOfBoundsException());
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        column.setValue(0, true);

        verify(cpu);
    }

    @Test
    public void testGetDebugValueOnInvalidLocationDoesNotThrow() {
        CPU cpu = createMock(CPU.class);
        expect(cpu.isBreakpointSupported()).andReturn(true);
        expect(cpu.isBreakpointSet(anyInt())).andThrow(new IndexOutOfBoundsException());
        replay(cpu);

        BreakpointColumn column = new BreakpointColumn(cpu);
        assertFalse(column.getValue(0));

        verify(cpu);
    }
}
