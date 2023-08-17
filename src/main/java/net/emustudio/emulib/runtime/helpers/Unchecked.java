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
