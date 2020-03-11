// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

public class AbstractMemoryContextStub extends AbstractMemoryContext {

    @Override
    public Object read(int memoryPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] readWord(int memoryPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int memoryPosition, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeWord(int memoryPosition, Object[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getDataType() {
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
}
