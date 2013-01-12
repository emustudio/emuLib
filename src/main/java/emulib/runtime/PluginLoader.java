/*
 * Loader.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2012, Peter Jakubčo
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

import emulib.annotations.EMULIB_VERSION;
import emulib.annotations.PluginType;
import emulib.emustudio.API;
import emulib.plugins.Plugin;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods for dynamic loading JAR files (e.g.
 * plug-ins or specific plug-in libraries)
 *
 * @author vbmacher
 */
public class PluginLoader extends ClassLoader {
    private final static Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    
    private final static EMULIB_VERSION CURRENT_EMULIB_VERSION = EMULIB_VERSION.VERSION_9;
    
    // Instance of this class
    private static PluginLoader instance = new PluginLoader();
    
    // loaded resources of all classes
    private Map<Object, URL> resources;
    
    // security manager for this class loader
    private PluginSecurityManager securityManager;
    private List<Class<?>> classesToResolve;
    private List<NotLoadedClass> undoneClassesToLoad;
    
    private class NotLoadedClass {
        private List<String> undone;
        private Map<String, Integer> sizes;
        private String filename;
        
        public NotLoadedClass(List<String> undone, Map<String, Integer> sizes,
            String filename) {
            this.undone = undone;
            this.sizes = sizes;
            this.filename = filename;
        }
        
        public List<String> getUndone() {
            return undone;
        }
        
        public Map<String, Integer> getSizes() {
            return sizes;
        }
        
        public String getFilename() {
            return filename;
        }
        
        @Override
        public String toString() {
            return "NLC[file=" + filename + "; undone=" + undone + "]";
        }
    }

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

        /**
         * Loaded code can't define classes in java.* or javax.* or sun.* packages.
         * 
         * @param pkg package definition name
         * @throws SecurityException
         */
        @Override
        public void checkPackageDefinition(String pkg) {
            if (this.inClassLoader()
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

    /**
     * Private constructor. Other objects could not create the instance in
     * classic way.
     */
    private PluginLoader() {
        resources = new HashMap<Object, URL>();
        classesToResolve = new ArrayList<Class<?>>();
        undoneClassesToLoad = new ArrayList<NotLoadedClass>();
        securityManager = new PluginSecurityManager();
        System.setSecurityManager(securityManager);
    }

    /**
     * Get the PluginLoader instance.
     * 
     * The instance is created only in the first call, then the same instance is returned (singleton).
     *
     * @return instance of this class (singleton)
     */
    public static PluginLoader getInstance() {
        return instance;
    }
        
    /**
     * This method clears all information regarding to plug-ins loading.
     * 
     * However, this does not mean that every loaded class is forgotten by JVM.
     * In order to do that, you must create new instance of PluginLoader.
     * 
     * @param password emuStudio password.
     * @throws InvalidPasswordException if the password is wrong
     */
    public void forgetAllLoaded(String password) throws InvalidPasswordException {
        API.testPassword(password);
        resources.clear();
        undoneClassesToLoad.clear();
        classesToResolve.clear();
    }
    
    /**
     * Checks if a class implements given interface.
     *
     * @param theClass class that will be tested
     * @param theInterface interface that the class should implement
     * @return true if the class implements given interface, false otherwise
     */
    public static boolean doesImplement(Class<?> theClass, Class<?> theInterface) {
        do {
            Class<?>[] intf = theClass.getInterfaces();
            for (int j = 0; j < intf.length; j++) {
                if (intf[j].isInterface() && intf[j].equals(theInterface)) {
                    return true;
                } else {
                    if (doesImplement(intf[j], theInterface)) {
                        return true;
                    }
                }
            }
            theClass = theClass.getSuperclass();
        } while ((theClass != null) && !theClass.equals(Object.class));

        return false;
    }


    /**
     * Method loads emuStudio plugin into memory.
     * 
     * The plug-in should be in JAR format. The loaded classes are not resolved. After this method call
     * (and all possible multiple calls), 
     * {@link emulib.runtime.PluginLoader#resolveLoadedClasses() PluginLoader.resolveLoadedClasses} method must be
     * called.
     * 
     * @param filename name of the plugin (absolute path is better).
     *        If the filename does not contain '.jar' suffix, it will be
     *        added automatically.
     * @param password emuStudio password.
     * @return Plugin main class, or null when an error occured or the main class is not found
     */
    public Class<Plugin> loadPlugin(String filename, String password) throws InvalidPasswordException, InvalidPluginException {
        API.testPassword(password);
        Map<String, Integer> sizes = new HashMap<String, Integer>();
        List<String> undoneClasses = new ArrayList<String>();
        Class<Plugin> mainClass = null;

        try {
            // load all classes in jar
            logger.debug("Loading JAR file: " + filename);
            JarFile jarFile = new JarFile(filename);
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                sizes.put(jarEntry.getName(), new Integer((int) jarEntry.getSize()));
            }
            FileInputStream fis = new FileInputStream(jarFile.getName());
            BufferedInputStream bis = new BufferedInputStream(fis);
            JarInputStream zis = new JarInputStream(bis);
            JarEntry jarEntry;

            while ((jarEntry = zis.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) {
                    continue;
                }
                if (!jarEntry.getName().toLowerCase().endsWith(".class")) {
                    //for windows: "jar:file:/D:/JavaApplicat%20ion12/dist/JavaApplication12.jar!/resources/Find%2024.gif";
                    //for linux:   "jar:file:/home/vbmacher/dev/school%20projects/shit.jar!/resources/Find%2024.gif";
                    String resourceName = jarFile.getName().replaceAll("\\\\", "/");
                    if (!resourceName.startsWith("/")) {
                        resourceName = "/" + resourceName;
                    }
                    String resourceURL = URLEncoder.encode("jar:file:" + resourceName + "!/" +
                            jarEntry.getName().replaceAll("\\\\", "/"), "UTF-8");
                    resourceURL = resourceURL.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%21", "!")
                            .replaceAll("\\+", "%20");
                    resources.put("/" + jarEntry.getName(), new URL(resourceURL));
                    continue;
                }
                // load class data
                int size = (int) jarEntry.getSize();
                if (size == -1) {
                    size = ((Integer) sizes.get(jarEntry.getName())).intValue();
                }

                byte[] classData = new byte[(int) size];
                int offset = 0;
                int bytesRead;
                while (((int) size - offset) > 0) {
                    bytesRead = zis.read(classData, offset, (int) size - offset);
                    if (bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                }
                try {
                    // try to define class
                    Class<?> definedClass = defineLoadedClass(jarEntry.getName(), classData, size);
                    if (trustedPlugin(definedClass)) {
                        mainClass = (Class<Plugin>)definedClass;
                    }
                    classesToResolve.add(definedClass);
                } catch (Exception nf) {
                    undoneClasses.add(jarEntry.getName());
                }
            }
            zis.close();
            bis.close();
            fis.close();
            jarFile.close();
            // try to load all undone classes
            if (!undoneClasses.isEmpty()) {
                List<Class<?>> definedClasses;
                do {
                    definedClasses = loadUndoneClasses(undoneClasses, sizes, filename);
                    if (mainClass == null) {
                        for (Class<?> definedClass : definedClasses) {
                            if (trustedPlugin(definedClass)) {
                                mainClass = (Class<Plugin>) definedClass;
                                break;
                            }
                        }
                    }
                } while (!(definedClasses.isEmpty() || undoneClasses.isEmpty()));
                if (!undoneClasses.isEmpty()) {
                    // if a jar file contains some error
                    undoneClassesToLoad.add(new NotLoadedClass(undoneClasses, sizes, filename));
                }
            }
        } catch (Exception e) {
            throw new InvalidPluginException("Could not load the plug-in.", e);
        }
        if (mainClass == null) {
            throw new InvalidPluginException();
        }
        return mainClass;
    }
    
    /**
     * Get list of not (yet) loaded classes.
     * 
     * @param password emuStudio password
     * @return array of string describing not loaded classes.
     * @throws InvalidPasswordException if the password is wrong
     */
    public String[] getUnloadedClassesList(String password) throws InvalidPasswordException {
        API.testPassword(password);
        List<String> classes = new ArrayList<String>();
        for (NotLoadedClass nlc : undoneClassesToLoad) {
          classes.add(nlc.toString());
        }
        return classes.toArray(new String[0]);
    }
    
    /**
     * Check if provided class meets plug-in requirements.
     * 
     * @param pluginClass the main class of the plug-in
     * @return true if the class meets plug-in requirements; false otherwise
     */
    public static boolean trustedPlugin(Class<?> pluginClass) {
        if (pluginClass.isInterface()) {
            return false;
        }
        if (!pluginClass.isAnnotationPresent(PluginType.class)) {
            return false;
        }
        PluginType pluginType = pluginClass.getAnnotation(PluginType.class);
        if (pluginType.emuLibVersion() != CURRENT_EMULIB_VERSION) {
            return false;
        }
        if (!doesImplement(pluginClass, Plugin.class)) {
            return false;
        }
        return true;
    }
    
    /**
     * Reads a file content.
     *
     * @param fileName The name of the file to be read
     * @return The contents of the file
     * @throws IOException if there was an error during the read
     */
    public static String loadFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName)));
        StringBuilder sb = new StringBuilder();
        String line;
        while (true) {
            line = br.readLine();
            if (line == null) {
                break;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Overrided method.
     * 
     * @param name
     * @return 
     */
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

    /**
     * Overrided method.
     * 
     * @param name
     * @return 
     */
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
                logger.debug(new StringBuilder().append("Could not get resource as stream.")
                        .append(url.toString()).toString(), e);
            }
        }
        return null;
    }

    /**
     * This method tries to load specified classes.
     * 
     * The method is called by loadJAR method, if some classes could not be loaded, because of possible
     * forwad references.
     * 
     * For example we cannot load a class that extends not yet loaded class. This method tries to resolve
     * this problem by hope that all till now loaded classes are sufficient for
     * defining these "undone" classes.
     *
     * @param undone Vector of "undone" classes
     * @param sizes How much size has each class in bytes
     * @param filename File name of the JAR file.
     * @return list of all classes that were loaded successfully
     */
    private List<Class<?>> loadUndoneClasses(List<String> undone, Map<String, Integer> sizes, String filename) {
        JarEntry jarEntry;
        List<Class<?>> resultClasses = new ArrayList<Class<?>>();
//        List<String> stillNotLoaded = new ArrayList<String>();
        
        try {
            FileInputStream fis = new FileInputStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            JarInputStream zis = new JarInputStream(bis);
            while ((jarEntry = zis.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) {
                    continue;
                }
                if (!jarEntry.getName().toLowerCase().endsWith(".class")) {
                    continue;
                }
                if (!undone.contains(jarEntry.getName())) {
                    continue;
                }
                // load class data
                int size = (int) jarEntry.getSize();
                if (size == -1) {
                    size = ((Integer) sizes.get(jarEntry.getName())).intValue();
                }
                byte[] b = new byte[(int) size];
                int rb = 0, chunk;
                while (((int) size - rb) > 0) {
                    chunk = zis.read(b, rb, (int) size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                try {
                    // try load class data
                    Class<?> cl = defineLoadedClass(jarEntry.getName(), b, size);
                    classesToResolve.add(cl);
                    resultClasses.add(cl);
                    undone.remove(jarEntry.getName());
                } catch (ClassNotFoundException nf) {
//                    stillNotLoaded.add(jarEntry.getName());
                }
            }
        } catch (Exception e) {
            logger.debug("Could not load undone classes.", e);
        }
        return resultClasses;
    }

    /**
     * Method define loaded class (transform class data to class object).
     *
     * @param classname Name of the class.
     *        Should be in the form "package.ClassName", but it doesn't matter
     *        if it has the form "package/ClassName.class"
     * @param classbytes Class data
     * @param length Size of the class data in bytes
     * @return Class object
     * @throws ClassNotFoundException
     */
    private Class<?> defineLoadedClass(String classname, byte[] classbytes, int length)
            throws ClassNotFoundException {
        if (classname.toLowerCase().endsWith(".class")) {
            classname = classname.substring(0, classname.length() - 6);
        }
        classname = classname.replace('/', '.');
        classname = classname.replace(File.separatorChar, '.');
        try {
            Class<?> classToLoad;
            classToLoad = findLoadedClass(classname);
            if (classToLoad == null) {
                try {
                    classToLoad = findSystemClass(classname);
                } catch (Exception e) {
                }
            }
            if (classToLoad == null) {
                classToLoad = defineClass(null, classbytes, 0, length);
            }
            return classToLoad;
        } catch (Error err) {
            throw new ClassNotFoundException(err.getMessage());
        } catch (Exception ex) {
            throw new ClassNotFoundException(ex.toString());
        }
    }
    
    /**
     * Determine if there are no classes that weren't successfully loaded.
     * 
     * If there are classes that weren't loaded, resolving process may not be
     * successful. Therefore, if this method returns false, there should be called
     * method {@link emulib.runtime.PluginLoader#loadUndoneClasses() PluginLoader.loadUndoneClasses}
     * 
     * @param password emuStudio password.
     * @return true if all classes were successfully loaded, false otherwise
     * @throws InvalidPasswordException if the password is wrong
     */
    public boolean canResolveClasses(String password) throws InvalidPasswordException {
        API.testPassword(password);
        return undoneClassesToLoad.isEmpty();
    }
    
    /**
     * This method tries to load all not successfully loaded classes.
     * 
     * It should be called only when
     * {@link emulib.runtime.PluginLoader#canResolveClasses() PluginLoader.canResolveClasses} returns false.
     * 
     * @param password emuStudio password.
     * @return true if all undone classes were successfully loaded, false otherwise
     * @throws InvalidPasswordException if the password is wrong
     */
    public boolean loadUndoneClasses(String password) throws InvalidPasswordException {
        API.testPassword(password);
        if (undoneClassesToLoad.isEmpty()) {
            return true;
        }
        List<NotLoadedClass> stillNotLoaded = new ArrayList<NotLoadedClass>();
        do {
            NotLoadedClass classToLoad = undoneClassesToLoad.get(0);
            List<String> undone = classToLoad.getUndone();
            boolean somethingLoaded;
            do {
                somethingLoaded = !loadUndoneClasses(undone, classToLoad.getSizes(), classToLoad.getFilename())
                        .isEmpty();
            } while (!undone.isEmpty() && somethingLoaded);
            undoneClassesToLoad.remove(0);
            if (!undone.isEmpty()) {
                stillNotLoaded.add(classToLoad);
            }
        } while (!undoneClassesToLoad.isEmpty());
        
        if (stillNotLoaded.isEmpty()) {
            return true;
        }
        undoneClassesToLoad.addAll(stillNotLoaded);
        return false;
    }


    /**
     * Make all loaded classes to be usable in Java.
     * 
     * This method should be called only once, after all calls of
     * {@link emulib.runtime.PluginLoader#loadJAR(String) PluginLoader.loadJAR} method.
     * 
     * You can check if the classes can be resolved by calling 
     * {@link emulib.runtime.PluginLoader#canResolveClasses() PluginLoader.canResolveClasses} method.
     * @param password emuStudio password.
     * @throws InvalidPasswordException if the password is wrong
     */
    public void resolveLoadedClasses(String password) throws InvalidPasswordException {
        API.testPassword(password);
        if (classesToResolve.isEmpty()) {
            return;
        }

        boolean done = false;
        do {
            boolean wasE = false;
            for (Class<?> classToResolve : classesToResolve) {
                try {
                    resolveClass(classToResolve);
                } catch (Exception e) {
                    wasE = true;
                }
            }
            if (!wasE) {
                done = true;
            }
        } while (!done);
        classesToResolve.clear();
    }
}
