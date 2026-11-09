/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

/**
 * Runs actions which can throw checked exceptions, rethrowing them unchanged without declaring them ("sneaky throw").
 */
public final class Unchecked {

    private Unchecked() {
    }

    /**
     * Runs the action; checked exceptions are rethrown unchanged.
     *
     * @param r the action
     */
    public static void run(RunnableWhichCanThrow r) {
        call(callable(r));
    }

    /**
     * Calls the action; checked exceptions are rethrown unchanged.
     *
     * @param c   the action
     * @param <T> the result type
     * @return the action result
     */
    public static <T> T call(CallableWithCanThrow<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            return sneakyThrow(e);
        }
    }

    /**
     * Throws any exception without declaring it.
     *
     * @param e   the exception
     * @param <T> the "return" type, allowing {@code return sneakyThrow(e);}
     * @return never returns
     */
    public static <T> T sneakyThrow(Throwable e) {
        return Unchecked.<RuntimeException, T>sneakyThrow0(e);
    }

    static CallableWithCanThrow<Void> callable(RunnableWhichCanThrow r) {
        return () -> {
            r.run();
            return null;
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * An action which can throw checked exceptions.
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
     * An action with a result which can throw checked exceptions.
     *
     * @param <T> the result type
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
