/*
 * Loader.java
 *
 * (c) Copyright 2010, P. Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
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
package emuLib8.runtime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * This class provides static methods for dynamic loading JAR files (e.g.
 * plug-ins or specific plug-in libraries)
 *
 * @author vbmacher
 */
public class Loader extends ClassLoader {

    private class PluginSecurityManager extends SecurityManager {

        /**
         * This is the basic method that tests whether there is a class loaded
         * by a ClassLoader anywhere on the stack. If so, it means that that
         * untrusted code is trying to perform some kind of sensitive operation.
         * We prevent it from performing that operation by throwing an exception.
         * trusted() is called by most of the check...() methods below.
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

        /** Loaded code can't define classes in java.* or javax.* or sun.*
         * packages
         * @param pkg package definition name
         * @throws SecurityException
         */
        @Override
        public void checkPackageDefinition(String pkg) {
            if (inClassLoader()
                    && ((pkg.startsWith("java.") || pkg.startsWith("javax.")
                    || pkg.startsWith("sun.")))) {
                throw new SecurityException();
            }
        }

        /**
         * This is the one SecurityManager method that is different from the
         * others. It indicates whether a top-level window should display an
         * "untrusted" warning. The window is always allowed to be created, so
         * this method is not normally meant to throw an exception. It should
         * return true if the window does not need to display the warning, and
         * false if it does. In this example, however, our text-based Service
         * classes should never need to create windows, so we will actually
         * throw an exception to prevent any windows from being opened.
         **/
        @Override
        public boolean checkTopLevelWindow(Object window) {
            //trusted();
            return true;
        }

        @Override
        public void checkPermission(Permission perm) {
            //          if (!(perm instanceof java.awt.AWTPermission))
            //            super.checkPermission(perm);
        }
    }
    // Instance of this class
    private static Loader instance = null;
    // loaded resources of classes
    private Hashtable<Object, URL> resources;
    // security manager for plug-ins
    private PluginSecurityManager securityManager;

    /**
     * Private constructor. Other objects could not create the instance in
     * classic way.
     */
    private Loader() {
        resources = new Hashtable<Object, URL>();
        securityManager = new PluginSecurityManager();
        System.setSecurityManager(securityManager);
    }

    /**
     * Get the Loader instance. The instance is created only in the first
     * call, then the same instance is returned.
     *
     * @return instance of the Loader class
     */
    public static Loader getInstance() {
        if (instance == null) {
            instance = new Loader();
        }
        return instance;
    }

    /**
     * Method loads JAR file into memory and define all found classes.
     *
     * @param filename name of the plugin (absolute path is better).
     *        If the filename does not contain '.jar' suffix, it will be
     *        added automatically.
     * @return list of loaded classes, or null when an error occured
     */
    public ArrayList<Class<?>> loadJAR(String filename) {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        Hashtable<String, Integer> sizes = new Hashtable<String, Integer>();
        Vector<String> undone = new Vector<String>();

        if (!filename.toLowerCase().endsWith(".jar")) {
            filename += ".jar";
        }
        try {
            // load all classes in jar
            JarFile zf = new JarFile(filename);
            Enumeration<JarEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                JarEntry ze = (JarEntry) e.nextElement();
                sizes.put(ze.getName(), new Integer((int) ze.getSize()));
            }
            FileInputStream fis = new FileInputStream(zf.getName());
            BufferedInputStream bis = new BufferedInputStream(fis);
            JarInputStream zis = new JarInputStream(bis);
            JarEntry ze = null;

            while ((ze = zis.getNextJarEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (!ze.getName().toLowerCase().endsWith(".class")) {
                    //for windows: "jar:file:/D:/JavaApplicat%20ion12/dist/JavaApplication12.jar!/resources/Find%2024.gif";
                    //for linux:   "jar:file:/home/vbmacher/dev/school%20projects/shit.jar!/resources/Find%2024.gif";
                    String fN = zf.getName().replaceAll("\\\\", "/");
                    if (!fN.startsWith("/")) {
                        fN = "/" + fN;
                    }
                    String URLstr = URLEncoder.encode("jar:file:" + fN
                            + "!/" + ze.getName().replaceAll("\\\\", "/"), "UTF-8");
                    URLstr = URLstr.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%21", "!").replaceAll("\\+", "%20");
                    resources.put("/" + ze.getName(), new URL(URLstr));
                    continue;
                }
                // load class data
                int size = (int) ze.getSize();
                if (size == -1) {
                    size = ((Integer) sizes.get(ze.getName())).intValue();
                }

                byte[] b = new byte[(int) size];
                int rb = 0;
                int chunk = 0;
                while (((int) size - rb) > 0) {
                    chunk = zis.read(b, rb, (int) size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                try {
                    // try to define class
                    Class<?> cl = defineLoadedClass(ze.getName(), b, size, true);
                    classes.add(cl);
                } catch (Exception nf) {
                    undone.addElement(ze.getName());
                }
            }
            zis.close();
            bis.close();
            fis.close();
            zf.close();
            // try to load all undone classes
            if (undone.size() > 0) {
                boolean res = loadUndoneClasses(undone, classes, sizes, filename);
                while ((res == true) && (undone.size() > 0)) {
                    res = loadUndoneClasses(undone, classes, sizes, filename);
                }
                if (undone.size() > 0) {
                    // if a jar file contains some error
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * This method reads the file with the given name and returns
     * the contents of this file as a String.
     *
     * @param fileName The name of the file to read.
     * @return The contents of the file, or null if an exception is catched
     * during the read process.
     */
    public static String loadFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName)));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected URL findResource(String name) {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        if (resources.containsKey(name)) {
            URL url = (URL) resources.get(name);
            return url;
        } else {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        if (resources.containsKey(name)) {
            URL url = (URL) resources.get(name);
            try {
                return url != null ? url.openStream() : null;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * This method tries to load all classes that could not be loaded
     * in the loadJAR or loadPlugin method. For example we cannot load a
     * class that extends not yet loaded class. This method tries to resolve
     * this problem by hope that all till now loaded classes are sufficient for
     * defining these "undone" classes.
     *
     * @param undone Vector of "undone" classes
     * @param classes Where to put loaded classes objects
     * @param sizes How much size has each class in bytes
     * @param filename File name of the JAR file.
     * @return true if at least 1 class was loaded successfully
     */
    private boolean loadUndoneClasses(Vector<String> undone, ArrayList<Class<?>> classes,
            Hashtable<String, Integer> sizes, String filename) {
        JarEntry ze = null;
        boolean result = false;
        try {
            FileInputStream fis = new FileInputStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            JarInputStream zis = new JarInputStream(bis);
            while ((ze = zis.getNextJarEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (!ze.getName().toLowerCase().endsWith(".class")) {
                    continue;
                }
                if (!undone.contains(ze.getName())) {
                    continue;
                }
                // load class data
                int size = (int) ze.getSize();
                if (size == -1) {
                    size = ((Integer) sizes.get(ze.getName())).intValue();
                }
                byte[] b = new byte[(int) size];
                int rb = 0, chunk = 0;
                while (((int) size - rb) > 0) {
                    chunk = zis.read(b, rb, (int) size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                try {
                    // try load class data
                    Class<?> cl = defineLoadedClass(ze.getName(), b, size, true);
                    classes.add(cl);
                    undone.removeElement(ze.getName());
                    result = true;
                } catch (ClassNotFoundException nf) {
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Method define loaded class (transform class data to class object).
     *
     * @param classname Name of the class.
     *        Should be in the form "package.ClassName", but it doesn't matter
     *        if it has the form "package/ClassName.class"
     * @param classbytes Class data
     * @param length Size of the class data in bytes
     * @param resolve Whether to link this class
     * @return Class object
     * @throws ClassNotFoundException
     */
    private Class<?> defineLoadedClass(String classname,
            byte[] classbytes, int length, boolean resolve)
            throws ClassNotFoundException {
        if (classname.toLowerCase().endsWith(".class")) {
            classname = classname.substring(0, classname.length() - 6);
        }
        classname = classname.replace('/', '.');
        classname = classname.replace(File.separatorChar, '.');
        try {
            Class<?> c = null;
            c = findLoadedClass(classname);
            if (c == null) {
                try {
                    c = findSystemClass(classname);
                } catch (Exception e) {
                }
            }
            if (c == null) {
                c = defineClass(null, classbytes, 0, length);
            }
            if (resolve && (c != null)) {
                resolveClass(c);
            }
            return c;
        } catch (Error err) {
            throw new ClassNotFoundException(err.getMessage());
        } catch (Exception ex) {
            throw new ClassNotFoundException(ex.toString());
        }
    }
}
