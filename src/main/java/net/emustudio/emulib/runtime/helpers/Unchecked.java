/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

/**
 * Unchecked running/ calling.
 * Unchecked means Runnable or Callable can throw exceptions (which don't need to extend RuntimeException).
 * <p>
 * This code was borrowed from:
 * <p>
 * <a href="http://stackoverflow.com/questions/19757300/java-8-lambda-streams-filter-by-method-with-exception">exceptions in lambda</a>
 */
public class Unchecked {

    public static void run(RunnableWhichCanThrow r) {
        try {
            r.run();
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }

    public static <T> T call(CallableWithCanThrow<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            sneakyThrow(e);
        }
        return null; // never called
    }

    public static <T> T sneakyThrow(Throwable e) {
        return Unchecked.<RuntimeException, T>sneakyThrow0(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }

    @FunctionalInterface
    public interface RunnableWhichCanThrow {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CallableWithCanThrow<T> {
        T call() throws Exception;
    }
}
