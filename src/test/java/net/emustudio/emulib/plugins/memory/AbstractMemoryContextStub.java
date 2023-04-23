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

import net.emustudio.emulib.plugins.memory.annotations.Annotations;

public class AbstractMemoryContextStub extends AbstractMemoryContext<Object> {

    public AbstractMemoryContextStub() {
        super(new Annotations());
    }

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
}
