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
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Read/write lock support for more comfortable work with a ReentrantReadWriteLock.
 * <p>
 * Runnables/callables can throw any exceptions.
 */
@SuppressWarnings("unused")
@ThreadSafe
public class ReadWriteLockSupport {
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    public void lockWrite(Unchecked.RunnableWhichCanThrow r) {
        rwl.writeLock().lock();
        try {
            Unchecked.run(r);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public <T> T lockWrite(Unchecked.CallableWithCanThrow<T> r) {
        rwl.writeLock().lock();
        try {
            return Unchecked.call(r);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void lockRead(Unchecked.RunnableWhichCanThrow r) {
        rwl.readLock().lock();
        try {
            Unchecked.run(r);
        } finally {
            rwl.readLock().unlock();
        }
    }

    public <T> T lockRead(Unchecked.CallableWithCanThrow<T> r) {
        rwl.readLock().lock();
        try {
            return Unchecked.call(r);
        } finally {
            rwl.readLock().unlock();
        }
    }
}
