/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.runtime.internal;

/**
 * This code was borrowed from:
 *
 * http://stackoverflow.com/questions/19757300/java-8-lambda-streams-filter-by-method-with-exception
 */
public class Unchecked {

    public static void run(RunnableWhichCanThrow r) {
        try {
            r.run();
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }

    public static <T> T sneakyThrow(Throwable e) {
        return Unchecked.<RuntimeException, T>sneakyThrow0(e);
    }

    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }

    @FunctionalInterface
    public interface RunnableWhichCanThrow {
        void run() throws Exception;
    }
}
