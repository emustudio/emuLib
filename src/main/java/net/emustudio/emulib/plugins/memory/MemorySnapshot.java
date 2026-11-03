/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.UnaryOperator;

/**
 * Read-only, explicit-bank cell capture for decoder adapters. No reference to live memory remains.
 * The copier must return deeply immutable cells; an identity copier is valid for Byte/Integer.
 * Callers coordinate execution and validate memory/layout revisions around capture. Capturing
 * cells does not lock an arbitrary provider or establish a simultaneous whole-memory read.
 */
public final class MemorySnapshot<T> implements MemoryContext<T> {
    /** Bounded v1 capture: at most 128 MiB of cell references, excluding immutable payloads. */
    private static final int MAX_CAPTURE_CELLS = 16 * 1024 * 1024;
    private final T[] cells;
    private final Class<T> cellType;
    private final int bank, bankCount, common;

    private MemorySnapshot(T[] cells, Class<T> cellType, int bank, int bankCount, int common) {
        this.cells = cells;
        this.cellType = cellType;
        this.bank = bank;
        this.bankCount = bankCount;
        this.common = common;
    }

    public static <T> MemorySnapshot<T> capture(MemoryContext<T> memory, int bank, UnaryOperator<T> immutableCopy) {
        Objects.requireNonNull(memory);
        Objects.requireNonNull(immutableCopy);
        int size = memory.getAddressSpaceSize(), count = memory.getBanksCount(), common = memory.getCommonBoundary();
        if (size < 0 || count < 1 || bank < 0 || bank >= count || common < 0 || common > size)
            throw new IllegalArgumentException("Invalid memory capture layout/bank");
        if (size > MAX_CAPTURE_CELLS) throw new IllegalArgumentException("Memory exceeds the 16M-cell capture budget");
        Class<T> cellType = Objects.requireNonNull(memory.getCellTypeClass());
        @SuppressWarnings("unchecked") T[] cells = (T[]) Array.newInstance(cellType, size);
        for (int address = 0; address < size; address++) {
            if (Thread.currentThread().isInterrupted()) throw new CancellationException();
            T value = memory.readBank(address, bank);
            cells[address] = value == null ? null : immutableCopy.apply(value);
        }
        if (memory.getAddressSpaceSize() != size || memory.getBanksCount() != count || memory.getCommonBoundary() != common)
            throw new IllegalStateException("Memory layout changed during capture");
        return new MemorySnapshot<>(cells, cellType, bank, count, common);
    }

    @Override public T read(int address) { return cells[address]; }
    @Override public T[] read(int address, int count) {
        if (address < 0 || address > cells.length || count < 0) throw new IndexOutOfBoundsException();
        return Arrays.copyOfRange(cells, address, address + Math.min(count, cells.length - address));
    }
    @Override public T readBank(int address, int requestedBank) {
        if (requestedBank < 0 || requestedBank >= bankCount || (address < common && requestedBank != bank))
            throw new IllegalArgumentException("Bank was not captured");
        return read(address);
    }
    @Override public int getSize() { return cells.length; }
    @Override public Class<T> getCellTypeClass() { return cellType; }
    @Override public int getBanksCount() { return bankCount; }
    @Override public int getSelectedBank() { return bank; }
    @Override public int getCommonBoundary() { return common; }
    @Override public boolean areMemoryNotificationsEnabled() { return false; }
    @Override public void addMemoryListener(MemoryListener listener) { Objects.requireNonNull(listener); }
    @Override public void removeMemoryListener(MemoryListener listener) { Objects.requireNonNull(listener); }
    @Override public void setMemoryNotificationsEnabled(boolean enabled) { }
    @Override public MemoryContextAnnotations annotations() { throw readOnly(); }
    @Override public void write(int address, T value) { throw readOnly(); }
    @Override public void write(int address, T[] values, int count) { throw readOnly(); }
    @Override public void clear() { throw readOnly(); }

    private static UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("Captured memory contains read-only cells; no live annotations");
    }
}
