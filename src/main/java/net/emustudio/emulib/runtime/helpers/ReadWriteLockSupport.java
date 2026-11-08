/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Runs actions under a {@link ReentrantReadWriteLock}.
 * <p>
 * Actions can throw checked exceptions; they are rethrown unchanged (see {@link Unchecked}).
 */
@ThreadSafe
public final class ReadWriteLockSupport {
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Runs the action under the write lock.
     *
     * @param r the action
     */
    public void lockWrite(Unchecked.RunnableWhichCanThrow r) {
        locked(rwl.writeLock(), Unchecked.callable(r));
    }

    /**
     * Calls the action under the write lock.
     *
     * @param c   the action
     * @param <T> the result type
     * @return the action result
     */
    public <T> T lockWrite(Unchecked.CallableWithCanThrow<T> c) {
        return locked(rwl.writeLock(), c);
    }

    /**
     * Runs the action under the read lock.
     *
     * @param r the action
     */
    public void lockRead(Unchecked.RunnableWhichCanThrow r) {
        locked(rwl.readLock(), Unchecked.callable(r));
    }

    /**
     * Calls the action under the read lock.
     *
     * @param c   the action
     * @param <T> the result type
     * @return the action result
     */
    public <T> T lockRead(Unchecked.CallableWithCanThrow<T> c) {
        return locked(rwl.readLock(), c);
    }

    private static <T> T locked(Lock lock, Unchecked.CallableWithCanThrow<T> c) {
        lock.lock();
        try {
            return Unchecked.call(c);
        } finally {
            lock.unlock();
        }
    }
}
