/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

import java.lang.reflect.Array;

/**
 * Memory context
 * <p>
 * It provides the basic memory functionality, such as reading/writing memory cells. The memory cell can be of any Java
 * type, thus a "byte", as the lowest addressable unit, is not enforced.
 * <p>
 * The memory context must be extended in a memory plugin in order to support additional functionality (e.g. bank-switching
 * or memory-mapped I/O).
 * <p>
 * Memory context supports annotations - a possibility to annotate any memory cell with supplemental information. Usual
 * annotations are: breakpoints, text information, position in source code, or any custom annotation. Annotation API
 * is accessible by using {@link #annotations()} method.
 *
 * @param <CellType> memory cell type
 */
@SuppressWarnings("unused")
@PluginContext
public interface MemoryContext<CellType> extends Context {
    /**
     * Exclusive upper bound of valid cell addresses. Unlike getSize(), which some sparse
     * providers use for loaded occupancy/high-water information, this describes the memory
     * layout and remains stable across loading, writing, and clearing cells. Dense providers
     * normally use getSize(); sparse providers must override it with their address capacity.
     */
    default int getAddressSpaceSize() { return getSize(); }

    /** Optional bank inspection. Defaults describe a single unbanked address space. */
    default int getBanksCount() { return 1; }
    default int getSelectedBank() { return 0; }
    default int getCommonBoundary() { return 0; }
    default int getBank(int address) { return address < getCommonBoundary() ? getSelectedBank() : 0; }
    /** Inspect a bank without selecting it. Implementations should avoid guest timing/I/O effects. */
    default CellType readBank(int address, int bank) {
        if (bank != 0) throw new IllegalArgumentException("Bank inspection unsupported");
        return read(address);
    }

    /**
     * Read-only live context for an explicit inspection bank. Shared cells resolve to bank zero.
     * Does not select a CPU bank, copy cells, or freeze memory. Callers coordinate paused reads
     * and revision checks; later cell changes are visible through the returned context.
     *
     * @param bank inspection bank in [0, getBanksCount())
     * @return a context whose logical reads use the specified bank
     */
    default MemoryContext<CellType> forBank(int bank) {
        if (bank < 0 || bank >= getBanksCount()) throw new IllegalArgumentException("Bank outside memory");
        MemoryContext<CellType> memory = this;
        return new MemoryContext<CellType>() {
            @Override public CellType read(int address) { return readBank(address, bank); }
            @Override public CellType readBank(int address, int requestedBank) {
                validateBank(requestedBank);
                if (address < 0 || address >= getAddressSpaceSize()) throw new IndexOutOfBoundsException();
                return memory.readBank(address, address < getCommonBoundary() ? requestedBank : 0);
            }
            @Override public CellType[] read(int address, int count) {
                int size = getAddressSpaceSize();
                if (address < 0 || address > size || count < 0) throw new IndexOutOfBoundsException();
                int length = Math.min(count, size - address);
                @SuppressWarnings("unchecked") CellType[] values = (CellType[]) Array.newInstance(getCellTypeClass(), length);
                for (int i = 0; i < length; i++) values[i] = read(address + i);
                return values;
            }
            @Override public int getSize() { return memory.getSize(); }
            @Override public int getAddressSpaceSize() { return memory.getAddressSpaceSize(); }
            @Override public Class<CellType> getCellTypeClass() { return memory.getCellTypeClass(); }
            @Override public int getBanksCount() { return memory.getBanksCount(); }
            @Override public int getSelectedBank() { return bank; }
            @Override public int getCommonBoundary() { return memory.getCommonBoundary(); }
            @Override public boolean areMemoryNotificationsEnabled() { return memory.areMemoryNotificationsEnabled(); }
            @Override public void addMemoryListener(MemoryListener listener) { memory.addMemoryListener(listener); }
            @Override public void removeMemoryListener(MemoryListener listener) { memory.removeMemoryListener(listener); }
            @Override public void setMemoryNotificationsEnabled(boolean enabled) { throw readOnly(); }
            @Override public MemoryContextAnnotations annotations() { throw readOnly(); }
            @Override public void write(int address, CellType value) { throw readOnly(); }
            @Override public void write(int address, CellType[] values, int count) { throw readOnly(); }
            @Override public void clear() { throw readOnly(); }

            private void validateBank(int requestedBank) {
                if (requestedBank < 0 || requestedBank >= memory.getBanksCount())
                    throw new IllegalArgumentException("Bank outside memory");
            }

            private UnsupportedOperationException readOnly() {
                return new UnsupportedOperationException("Explicit-bank decoder memory is read-only");
            }
        };
    }

    /**
     * A listener for receiving memory related events.
     * <p>
     * A class interested in processing memory event implements this interface, and the object created with
     * that class is registered with the memory context (with {@link #addMemoryListener(MemoryListener)} method).
     */
    interface MemoryListener {
        /**
         * Invoked when memory content changed.
         *
         * @param fromLocation from location
         * @param toLocation   to location
         */
        void memoryContentChanged(int fromLocation, int toLocation);

        /**
         * Invoked when memory size changed
         */
        void memorySizeChanged();
    }

    /**
     * Reads one memory cell at given location.
     *
     * @param location memory location of the memory cell
     * @return cell at given location
     */
    CellType read(int location);

    /**
     * Reads one or more adjacent cells from a memory.
     * <p>
     * Implementation of return value is up to plugin programmer (e.g. ordering of cells or atomicity).
     * If cells in memory are pure bytes, concatenation can be realized as (in small endian):
     *
     * <pre>
     * {@code
     * result = (mem[from]&0xFF) | ((mem[from+1]<<8)&0xFF) | ...;
     * }
     * </pre>
     * <p>
     * and in big endian as:
     *
     * <pre>
     * {@code
     * result = ((mem[from]<<(count *8 ))&0xFF) | (mem[from+1]<<((count-1)*8)&0xFF) | ...;
     * }
     * </pre>
     * <p>
     * If memory size is smaller than (location + count), then only available cells are returned - returned
     * array size can be less than <code>count</code>.
     *
     * @param location location of the first cell
     * @param count    number of cells to read
     * @return array of cells at given location
     * @throws RuntimeException if memory size is smaller than (location + count)
     */
    CellType[] read(int location, int count);

    /**
     * Write a value to a memory cell at given location.
     *
     * @param location location of the cell
     * @param value    data to be written
     */
    void write(int location, CellType value);

    /**
     * Write several cell values at once to a given location.
     * The ordering of values, as well as atomicity is implementation-specific.
     *
     * @param location location of the first cell
     * @param values   data to be written
     * @param count    number of values taken from given array
     * @throws RuntimeException if memory size is less than (location + count) or if (count &gt; values.length) or if (count &lt; 0)
     */
    void write(int location, CellType[] values, int count);

    /**
     * Write several cell values at once to a given location.
     * The ordering of values, as well as atomicity is implementation-specific.
     *
     * @param location location of the first cell
     * @param values   data to be written
     * @throws RuntimeException if memory size is less than (location + count)
     */
    default void write(int location, CellType[] values) {
        write(location, values, values.length);
    }

    /**
     * Get memory cell type class.
     *
     * @return Java data type of memory cells
     */
    Class<CellType> getCellTypeClass();

    /**
     * Clears the memory content.
     */
    void clear();

    /**
     * Get memory size
     * <p>
     * A memory size is basically a number of cells.
     * <p>
     * If the memory supports bank-switching, it is up to the implementing memory how this is interpreted. Usually
     * it returns size of the active (alternatively the most "common") bank.
     *
     * @return memory size (number of cells)
     */
    int getSize();

    /**
     * Adds a memory listener
     *
     * @param listener the memory listener (non-null)
     */
    void addMemoryListener(MemoryListener listener);

    /**
     * Removes a memory listener
     *
     * @param listener the memory listener to be removed (non-null)
     */
    void removeMemoryListener(MemoryListener listener);

    /**
     * Enable/disable notifications of memory changes globally.
     * <p>
     * Enabled by default. Usually it is disabled by CPU when in a "running" state.
     *
     * @param enabled - true if enabled, false if disabled.
     */
    void setMemoryNotificationsEnabled(boolean enabled);

    /**
     * Determine if notifications of memory changes are globally enabled or disabled.
     *
     * @return true if notifications are enabled, false if disabled.
     */
    boolean areMemoryNotificationsEnabled();

    /**
     * Get memory context annotations API.
     * <p>
     * Annotations is a possibility to annotate any memory cell with supplemental information. Usual
     * annotations are: breakpoints, text information, position in source code, or any custom annotation.
     *
     * @return memory context annotations API
     */
    MemoryContextAnnotations annotations();
}

