/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
