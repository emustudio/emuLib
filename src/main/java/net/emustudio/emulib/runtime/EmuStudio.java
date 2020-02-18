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
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.runtime.interaction.debugger.DebuggerTable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import net.emustudio.emulib.internal.TokenVerifier;
import net.jcip.annotations.ThreadSafe;

/**
 * EmuStudio API for plugins.
 *
 * Plugins can use it for the communication with emuStudio.
 */
@SuppressWarnings("unused")
@ThreadSafe
public final class EmuStudio {
    private final AtomicReference<DebuggerTable> debuggerTable = new AtomicReference<>();
    private final String token;
    private final TokenVerifier tokenVerifier = new TokenVerifierImpl();
    private final ContextPool contextPool = new ContextPool(tokenVerifier);

    private class TokenVerifierImpl implements TokenVerifier {

        @Override
        public void verifyToken(String token) throws InvalidTokenException {
            if ((token == null) || (EmuStudio.this.token == null)) {
                throw new InvalidTokenException();
            }
            if (!token.equals(EmuStudio.this.token)) {
                throw new InvalidTokenException();
            }
        }
    }

    /**
     * Creates new instance of the class.
     *
     * It is called by emuStudio.
     *
     * @param token emuStudio token
     */
    public EmuStudio(String token) {
        this.token = Objects.requireNonNull(token);
    }

    /**
     * Set debugger table.
     *
     * It is called by emuStudio.
     *
     * @param debuggerTable The debug table
     * @param token emuStudio token
     * @throws InvalidTokenException if provided token was wrong, or if no emustudio token was assigned.
     */
    public void setDebugTable(DebuggerTable debuggerTable, String token) throws InvalidTokenException {
        tokenVerifier.verifyToken(token);
        this.debuggerTable.set(debuggerTable);
    }

    /**
     * Get debugger table.
     *
     * @return debugger table in emuStudio.
     */
    public Optional<DebuggerTable> getDebuggerTable() {
        return Optional.ofNullable(debuggerTable.get());
    }

    /**
     * Get context pool.
     *
     * Context pool is used for registration and obtaining plugin contexts, which are used in plugin communication.
     *
     * @return context pool
     */
    public ContextPool getContextPool() {
        return contextPool;
    }
}
