/*
 * API.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2012, Peter Jakubčo
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

import emulib.runtime.Context;
import emulib.runtime.IDebugTable;

/**
 * This class represents public API of emuStudio offered to plug-ins.
 * 
 * Through this class, plug-ins are enabled to call emuStudio directly.
 * It is a singleton class.
 * 
 * @author vbmacher
 */
public class API {
    // emuStudio password for further emuLib communication
    private static String emuStudioPassword = null;
    
    // instance of this singleton
    private static API instance;
    
    /**
     * Debug table updater object
     */
    private IDebugTable debug;
    
    private API() {
    }
    
    /**
     * Get singleton instance of this API object.
     * 
     * @return instance of this class
     */
    public static API getInstance() {
        if (instance == null) {
            instance = new API();
        }
        return instance;
    }
    
    
    /**
     * Assigns a password for most crucial emuLib operations.
     * 
     * This password should be a hash string by which the emuStudio is allowed to
     * perform crucial operations in the emuLib, such as plug-in loading, or providing
     * information about plug-in connections. These operations must be strictly proteted
     * from plug-ins.
     *
     * This method is called only once, and by the emuStudio. After each next call,
     * it does nothing and returns false.
     *
     * @param password emuStudio hash string, the "password".
     * @return true if the assignment was successfull (first call), false
     *         otherwise.
     */
    public static boolean assignPassword(String password) {
        if (emuStudioPassword == null) {
            emuStudioPassword = password;
            Context.setEmuStudioPassword(password);
            return true;
        }
        return false;
    }
    
    /**
     * Determines if given password matches with password already set-up by emuStudio.
     * If the password is correct, it does nothing. Otherwise it throws an exception.
     * 
     * @param password The password
     * @throws InvalidPasswordException thrown if password is wrong
     */
    public void testPassword(String password) throws InvalidPasswordException {
        if ((password == null) || (emuStudioPassword == null)) {
            throw new InvalidPasswordException();
        }
        if (!password.equals(emuStudioPassword)) {
            throw new InvalidPasswordException();
        }
    }
    
    /**
     * Set object with method of updating debug table in emuStudio. It should
     * be called once by emuStudio.
     * 
     * @param debug The IDebugTable object
     * @param password password that was assigned to the emuLib. It prevents
     * from misuse of this method by other plugins.
     */
    public void setDebugTableInterfaceObject(IDebugTable debug, String password) {
        if ((emuStudioPassword == null) || (!emuStudioPassword.equals(password)))
            return;
        this.debug = debug;
    }
    
    /**
     * Update debug table in emuStudio. If IDebugTableObject was not set,
     * then it does nothing.
     */
    public void updateDebugTable() {
        if (debug == null) {
            return;
        }
        debug.updateDebugTable();
    }
    
}
