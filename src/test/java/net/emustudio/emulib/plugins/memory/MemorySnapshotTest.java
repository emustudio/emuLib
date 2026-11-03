/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.SnapshotDisassembler;
import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MemorySnapshotTest {
    @Test public void explicitBankAndCommonCellsAreFrozenWithoutChangingSelectedBank() {
        TestMemory<Byte> memory = new TestMemory<>(Byte.class, new Byte[][] {{1, 2, 3, 4}, {5, 6, 7, 8}}, 2);
        MemorySnapshot<Byte> captured = MemorySnapshot.capture(memory, 1, value -> value);
        memory.cells[1][0] = 99;
        memory.cells[0][2] = 88;
        memory.bank = 0;
        assertEquals(Byte.valueOf((byte) 5), captured.read(0));
        assertEquals(Byte.valueOf((byte) 3), captured.readBank(2, 0));
        assertEquals(Byte.valueOf((byte) 3), captured.readBank(2, 1));
        assertEquals(1, captured.getSelectedBank());
        assertEquals(0, memory.bank);
        assertThrows(IllegalArgumentException.class, () -> captured.readBank(0, 0));
        Byte[] copy = captured.read(0, 4);
        copy[0] = 12;
        assertEquals(Byte.valueOf((byte) 5), captured.read(0));
        assertThrows(UnsupportedOperationException.class, () -> captured.write(0, (byte) 1));
        assertThrows(UnsupportedOperationException.class, captured::clear);
    }

    @Test public void wordCellsRetainTheirTypeAndValue() {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class,
                new Integer[][] {{Integer.MIN_VALUE, 1000, Integer.MAX_VALUE}}, 0);
        MemorySnapshot<Integer> captured = MemorySnapshot.capture(memory, 0, value -> value);
        assertArrayEquals(new Integer[] {Integer.MIN_VALUE, 1000, Integer.MAX_VALUE}, captured.read(0, 10));
        assertEquals(Integer.class, captured.getCellTypeClass());
        assertThrows(IndexOutOfBoundsException.class, () -> captured.read(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> captured.read(3));
    }

    @Test public void explicitFreezerRemovesMutablePayloadReferences() {
        StringBuilder mutable = new StringBuilder("old");
        TestMemory<Object> memory = new TestMemory<>(Object.class, new Object[][] {{mutable}}, 0);
        MemorySnapshot<Object> captured = MemorySnapshot.capture(memory, 0, Object::toString);
        mutable.replace(0, mutable.length(), "changed");
        assertEquals("old", captured.read(0));
    }

    @Test public void decoderFactoryUsesOnlyCapturedCellsAfterCapture() throws Exception {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class, new Integer[][] {{42}}, 0);
        SnapshotDisassembler<Integer> live = new SnapshotDisassembler<>(memory, value -> value,
                cells -> new Disassembler() {
                    @Override public DisassembledInstruction disassemble(int address) {
                        return new DisassembledInstruction(address, cells.read(address).toString(), "");
                    }
                    @Override public int getNextInstructionPosition(int address) { return address + 1; }
                });
        Disassembler captured = live.capture(0).orElseThrow();
        int reads = memory.reads;
        memory.cells[0][0] = 99;
        assertEquals("42", captured.disassemble(0).mnemo);
        assertEquals(reads, memory.reads);
        assertEquals("99", live.disassemble(0).mnemo);
    }

    @Test public void invalidBankAndHugeDomainRejectBeforeReadingCells() {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class, new Integer[][] {{1}}, 0);
        assertThrows(IllegalArgumentException.class, () -> MemorySnapshot.capture(memory, -1, value -> value));
        assertThrows(IllegalArgumentException.class, () -> MemorySnapshot.capture(memory, 1, value -> value));
        memory.size = Integer.MAX_VALUE;
        assertThrows(IllegalArgumentException.class, () -> MemorySnapshot.capture(memory, 0, value -> value));
        assertEquals(0, memory.reads);
    }

    private static final class TestMemory<T> implements MemoryContext<T> {
        final Class<T> type;
        final T[][] cells;
        final int common;
        int bank, size, reads;
        TestMemory(Class<T> type, T[][] cells, int common) {
            this.type = type;
            this.cells = cells;
            this.common = common;
            size = cells[0].length;
        }
        @Override public T read(int address) { return readBank(address, bank); }
        @Override public T readBank(int address, int requestedBank) {
            reads++;
            return cells[address < common ? requestedBank : 0][address];
        }
        @Override public T[] read(int address, int count) { return Arrays.copyOfRange(cells[bank], address, address + count); }
        @Override public int getSize() { return size; }
        @Override public Class<T> getCellTypeClass() { return type; }
        @Override public int getSelectedBank() { return bank; }
        @Override public int getBanksCount() { return cells.length; }
        @Override public int getCommonBoundary() { return common; }
        @Override public void write(int address, T value) { cells[bank][address] = value; }
        @Override public void write(int address, T[] values, int count) { System.arraycopy(values, 0, cells[bank], address, count); }
        @Override public void clear() { throw new UnsupportedOperationException(); }
        @Override public MemoryContextAnnotations annotations() { return null; }
        @Override public void addMemoryListener(MemoryListener listener) { }
        @Override public void removeMemoryListener(MemoryListener listener) { }
        @Override public void setMemoryNotificationsEnabled(boolean enabled) { }
        @Override public boolean areMemoryNotificationsEnabled() { return false; }
    }
}
