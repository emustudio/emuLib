/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
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
package emulib.emustudio;

import emulib.emustudio.debugtable.DebugTable;
import emulib.runtime.exceptions.InvalidPasswordException;
import java.util.concurrent.atomic.AtomicReference;
import net.jcip.annotations.ThreadSafe;

/**
 * This class represents public API of emuStudio offered to plug-ins.
 *
 * Through this class, plug-ins are enabled to call emuStudio directly.
 * It is a singleton class.
 *
 */
@ThreadSafe
public class API {
    private final static AtomicReference<String> EMUSTUDIO_PASSWORD = new AtomicReference<>();
    private final static API INSTANCE = new API();

    private final AtomicReference<DebugTable> debugTable = new AtomicReference<>();

    private API() {
    }

    /**
     * Get singleton instance of this API object.
     *
     * @return instance of this class
     */
    public static API getInstance() {
        return INSTANCE;
    }

    public synchronized void clearAll(String password) throws InvalidPasswordException {
        testPassword(password);
        debugTable.set(null);
    }

    /**
     * Assigns a password for most crucial emuLib operations.
     *
     * This password should be a hash string by which the emuStudio is allowed to
     * perform crucial operations in the emuLib, such as plug-in loading, or providing
     * information about plug-in connections. These operations must be strictly protected
     * from plug-ins.
     *
     * This method is called only once, and by the emuStudio. After each next call,
     * it does nothing and returns false.
     *
     * @param password emuStudio hash string, the "password".
     * @return true if the assignment was successful (first call), false
     *         otherwise.
     */
    public static boolean assignPassword(String password) {
        return EMUSTUDIO_PASSWORD.compareAndSet(null, password);
    }

    /**
     * Determines if given password matches with password already set-up by emuStudio.
     * If the password is correct, it does nothing. Otherwise it throws an exception.
     *
     * WARNING: Everyone can call this method.
     *
     * @param password The password
     * @throws InvalidPasswordException thrown if password is wrong or trustedInstance is not trusted
     */
    public static void testPassword(String password) throws InvalidPasswordException {
        String tmpPassword = EMUSTUDIO_PASSWORD.get();
        if ((password == null) || (tmpPassword == null)) {
            throw new InvalidPasswordException();
        }
        if (!password.equals(tmpPassword)) {
            throw new InvalidPasswordException();
        }
    }

    public static boolean testPassword(Long hashCode) {
        String tmpPassword = EMUSTUDIO_PASSWORD.get();
        return !(hashCode == null || tmpPassword == null) && hashCode == tmpPassword.hashCode();
    }

    /**
     * Set debug table.
     *
     * It should be called by emuStudio only.
     *
     * @param debugTable The debug table
     * @param password password that was assigned to the emuLib. It prevents
     * from misuse of this method by other plug-ins.
     * @throws InvalidPasswordException if the password do not match with the password set by emuStudio
     */
    public void setDebugTable(DebugTable debugTable, String password) throws InvalidPasswordException {
        testPassword(password);
        this.debugTable.set(debugTable);
    }

    /**
     * Gets debug table from emuStudio.
     *
     * @return debug table in emuStudio. If debug table was not set by emuStudio earlier, returns null.
     */
    public DebugTable getDebugTable() {
        return debugTable.get();
    }

}
