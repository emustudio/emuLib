/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.internal;

import net.emustudio.emulib.runtime.InvalidTokenException;

public interface TokenVerifier {

    /**
     * Verifies that given token matches with the token assigned by emuStudio.
     *
     * If tokens do not match, an exception is thrown.
     *
     * @param emustudioToken emuStudio token (only emuStudio has it)
     * @throws InvalidTokenException if provided token was wrong, or if no emustudio token was assigned.
     */
    void verifyToken(String emustudioToken) throws InvalidTokenException;
}
