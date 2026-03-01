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

    /**
     * Constructs a new ReadWriteLockSupport.
     */
    public ReadWriteLockSupport() {
    }

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Acquires write lock and runs the given runnable.
     *
     * @param r the runnable to run under write lock
     */
    public void lockWrite(Unchecked.RunnableWhichCanThrow r) {
        rwl.writeLock().lock();
        try {
            Unchecked.run(r);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Acquires write lock and calls the given callable.
     *
     * @param r   the callable to call under write lock
     * @param <T> the return type
     * @return the result of the callable
     */
    public <T> T lockWrite(Unchecked.CallableWithCanThrow<T> r) {
        rwl.writeLock().lock();
        try {
            return Unchecked.call(r);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Acquires read lock and runs the given runnable.
     *
     * @param r the runnable to run under read lock
     */
    public void lockRead(Unchecked.RunnableWhichCanThrow r) {
        rwl.readLock().lock();
        try {
            Unchecked.run(r);
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Acquires read lock and calls the given callable.
     *
     * @param r   the callable to call under read lock
     * @param <T> the return type
     * @return the result of the callable
     */
    public <T> T lockRead(Unchecked.CallableWithCanThrow<T> r) {
        rwl.readLock().lock();
        try {
            return Unchecked.call(r);
        } finally {
            rwl.readLock().unlock();
        }
    }
}
