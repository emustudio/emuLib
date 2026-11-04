/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.cpu.BankedDisassembler;
import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryContextBankTest {
    @Test public void explicitBankAndCommonCellsStayLiveWithoutSelectingTheBank() {
        TestMemory<Byte> memory = new TestMemory<>(Byte.class, new Byte[][] {{1, 2, 3, 4}, {5, 6, 7, 8}}, 2);
        MemoryContext<Byte> bank = memory.forBank(1);
        assertEquals(0, memory.reads);
        assertEquals(Byte.valueOf((byte) 5), bank.read(0));
        assertEquals(Byte.valueOf((byte) 3), bank.read(2));
        memory.cells[1][0] = 99;
        memory.cells[0][2] = 88;
        assertEquals(Byte.valueOf((byte) 99), bank.read(0));
        assertEquals(Byte.valueOf((byte) 88), bank.readBank(2, 1));
        assertEquals(Byte.valueOf((byte) 88), bank.readBank(2, 0));
        assertEquals(1, bank.getSelectedBank());
        assertEquals(0, bank.getBank(2));
        assertEquals(0, memory.bank);
        memory.bank = 1;
        assertEquals(Byte.valueOf((byte) 99), bank.read(0));
    }

    @Test public void blockReadsRetainCellTypeAndRespectAddressCapacityWithoutOverflow() {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class,
                new Integer[][] {{Integer.MIN_VALUE, 1000, Integer.MAX_VALUE}}, 0);
        memory.size = 1; // Sparse occupancy is different from address capacity.
        MemoryContext<Integer> bank = memory.forBank(0);
        assertEquals(1, bank.getSize());
        assertEquals(3, bank.getAddressSpaceSize());
        assertArrayEquals(new Integer[] {Integer.MIN_VALUE, 1000, Integer.MAX_VALUE}, bank.read(0, Integer.MAX_VALUE));
        assertArrayEquals(new Integer[] {Integer.MAX_VALUE}, bank.read(2, 2));
        assertEquals(Integer.class, bank.getCellTypeClass());
        assertEquals(0, bank.read(3, 10).length);
        assertThrows(IndexOutOfBoundsException.class, () -> bank.read(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> bank.read(3));
        assertThrows(IndexOutOfBoundsException.class, () -> bank.read(-1, 1));
    }

    @Test public void hugeSparseLayoutDoesNotCopyOrReadCellsWhenCreatingABankView() {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class, new Integer[][] {{42}}, 0);
        memory.capacity = Integer.MAX_VALUE;
        MemoryContext<Integer> bank = memory.forBank(0);
        assertEquals(Integer.MAX_VALUE, bank.getAddressSpaceSize());
        assertEquals(0, memory.reads);
        assertArrayEquals(new Integer[] {42}, bank.read(0, 1));
        assertEquals(1, memory.reads);
    }

    @Test public void invalidBanksRejectBeforeReadingAndDecoderViewCannotWrite() {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class, new Integer[][] {{42}}, 0);
        assertThrows(IllegalArgumentException.class, () -> memory.forBank(-1));
        assertThrows(IllegalArgumentException.class, () -> memory.forBank(1));
        MemoryContext<Integer> bank = memory.forBank(0);
        assertThrows(IllegalArgumentException.class, () -> bank.readBank(0, 1));
        assertEquals(0, memory.reads);
        assertThrows(UnsupportedOperationException.class, () -> bank.write(0, 99));
        assertThrows(UnsupportedOperationException.class, () -> bank.write(0, new Integer[] {99}, 1));
        assertThrows(UnsupportedOperationException.class, bank::clear);
        assertThrows(UnsupportedOperationException.class, bank::annotations);
        assertEquals(Integer.valueOf(42), memory.cells[0][0]);
    }

    @Test public void decoderFactoryKeepsLogicalReadsAndExplicitBankReadsIndependentAndLive() throws Exception {
        TestMemory<Integer> memory = new TestMemory<>(Integer.class, new Integer[][] {{42}, {99}}, 1);
        BankedDisassembler<Integer> logical = new BankedDisassembler<>(memory, cells -> new Disassembler() {
            @Override public DisassembledInstruction disassemble(int address) {
                return new DisassembledInstruction(address, cells.read(address).toString(), "");
            }
            @Override public int getNextInstructionPosition(int address) { return address + 1; }
        });
        Disassembler bank0 = logical.forBank(0).orElseThrow();
        Disassembler bank1 = logical.forBank(1).orElseThrow();
        assertEquals(0, memory.reads);
        assertEquals("42", logical.disassemble(0).mnemo);
        assertEquals("99", bank1.disassemble(0).mnemo);
        memory.bank = 1;
        memory.cells[0][0] = 12;
        assertEquals("99", logical.disassemble(0).mnemo);
        assertEquals("12", bank0.disassemble(0).mnemo);
        assertEquals("99", bank1.disassemble(0).mnemo);
        assertEquals(1, bank1.getNextInstructionPosition(0));
        assertTrue(bank1.forBank(0).isEmpty());
    }

    private static final class TestMemory<T> implements MemoryContext<T> {
        final Class<T> type;
        final T[][] cells;
        final int common;
        int bank, size, capacity, reads;
        TestMemory(Class<T> type, T[][] cells, int common) {
            this.type = type;
            this.cells = cells;
            this.common = common;
            size = capacity = cells[0].length;
        }
        @Override public T read(int address) { return readBank(address, bank); }
        @Override public T readBank(int address, int requestedBank) {
            reads++;
            return cells[address < common ? requestedBank : 0][address];
        }
        @Override public T[] read(int address, int count) { throw new AssertionError("View must read the requested bank"); }
        @Override public int getSize() { return size; }
        @Override public int getAddressSpaceSize() { return capacity; }
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
