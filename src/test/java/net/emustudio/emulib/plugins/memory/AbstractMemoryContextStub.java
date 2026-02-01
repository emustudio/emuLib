/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

public class AbstractMemoryContextStub extends AbstractMemoryContext<Object> {

    @Override
    public Object read(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] read(int location, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int location, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int location, Object[] values, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<Object> getCellTypeClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryContextAnnotations annotations() {
        throw new UnsupportedOperationException();
    }
}
