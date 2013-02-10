/*
 * PluginSecurityManager.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2013, Peter Jakubčo
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
package emulib.runtime;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

class PluginSecurityManager extends SecurityManager {

    /**
     * This is the basic method that tests whether there is a class loaded by a ClassLoader anywhere on the stack. If
     * so, it means that that untrusted code is trying to perform some kind of sensitive operation. We prevent it from
     * performing that operation by throwing an exception. trusted() is called by most of the check...() methods below.
     */
    protected void trusted() {
        if (inClassLoader()) {
            throw new SecurityException();
        }
    }

    /*
     void	checkDelete(String file)
     void	checkSetFactory()
     */
    @Override
    public void checkAwtEventQueueAccess() {
    }

    @Override
    public void checkPackageAccess(String pkg) {
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkExec(String cmd) {
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
    }

    @Override
    public void checkLink(String lib) {
    }

    @Override
    public void checkListen(int port) {
    }

    @Override
    public void checkConnect(String host, int port) {
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
    }

    @Override
    public void checkAccept(String host, int port) {
    }

    @Override
    public void checkAccess(Thread t) {
    }

    @Override
    public void checkAccess(ThreadGroup g) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
    }

    @Override
    public void checkSystemClipboardAccess() {
    }

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        trusted();
    }

    @Override
    public void checkPropertiesAccess() {
        super.checkPropertiesAccess();
        trusted();
    }

    @Override
    public void checkPropertyAccess(String key) {
    }

    @Override
    public void checkSecurityAccess(String provider) {
        super.checkSecurityAccess(provider);
        trusted();
    }

    @Override
    public void checkDelete(String file) {
    }

    @Override
    public void checkRead(FileDescriptor fd) {
    }

    @Override
    public void checkRead(String file) {
    }

    @Override
    public void checkRead(String file, Object context) {
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
    }

    @Override
    public void checkWrite(String file) {
    }

    /**
     * Loaded code can't define classes in java.* or javax.* or sun.* packages.
     *
     * @param pkg package definition name
     * @throws SecurityException
     */
    @Override
    public void checkPackageDefinition(String pkg) {
        if (this.inClassLoader()
                && ((pkg.startsWith("java.") || pkg.startsWith("javax.") || pkg.startsWith("sun.")))) {
            throw new SecurityException();
        }
    }

    /**
     * This is the one SecurityManager method that is different from the others. It indicates whether a top-level window
     * should display an "untrusted" warning. The window is always allowed to be created, so this method is not normally
     * meant to throw an exception. It should return true if the window does not need to display the warning, and false
     * if it does. In this example, however, our text-based Service classes should never need to create windows, so we
     * will actually throw an exception to prevent any windows from being opened.
         *
     */
    @Override
    public boolean checkTopLevelWindow(Object window) {
        //trusted();
        return true;
    }

    @Override
    public void checkPermission(Permission perm) {
    }
}
