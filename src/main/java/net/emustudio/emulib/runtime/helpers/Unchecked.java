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

    /**
     * Constructs a new Unchecked instance.
     */
    private Unchecked() {
    }

    /**
     * Runs a runnable that can throw checked exceptions.
     *
     * @param r the runnable
     */
    public static void run(RunnableWhichCanThrow r) {
        try {
            r.run();
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }

    /**
     * Calls a callable that can throw checked exceptions.
     *
     * @param c   the callable
     * @param <T> the return type
     * @return the result of the callable
     */
    public static <T> T call(CallableWithCanThrow<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            sneakyThrow(e);
        }
        return null; // never called
    }

    /**
     * Throws a checked exception as an unchecked one.
     *
     * @param e   the exception
     * @param <T> the return type
     * @return never returns, always throws
     */
    public static <T> T sneakyThrow(Throwable e) {
        return Unchecked.<RuntimeException, T>sneakyThrow0(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * A runnable that can throw checked exceptions.
     */
    @FunctionalInterface
    public interface RunnableWhichCanThrow {
        /**
         * Runs the action.
         *
         * @throws Exception if an error occurs
         */
        void run() throws Exception;
    }

    /**
     * A callable that can throw checked exceptions.
     *
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface CallableWithCanThrow<T> {
        /**
         * Calls the action.
         *
         * @return the result
         * @throws Exception if an error occurs
         */
        T call() throws Exception;
    }
}
