/*
 * PluginLoader.java
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

import emulib.annotations.EMULIB_VERSION;
import emulib.annotations.PluginType;
import emulib.emustudio.API;
import emulib.plugins.Plugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods for dynamic loading of emuStudio plug-ins (which in turn are JAR files.)
 *
 * The class is not thread safe.
 * @author vbmacher
 */
public class PluginLoader extends URLClassLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private final static EMULIB_VERSION CURRENT_EMULIB_VERSION = EMULIB_VERSION.VERSION_9;

    private final Map<String, List<String>> fileNameToClassesList = new HashMap<>();

    /**
     * Create an instance of the PluginLoader.
     *
     * @param libDirectoryURL URL with default libraries location
     */
    public PluginLoader(URL libDirectoryURL) {
        super(new URL[] { libDirectoryURL }, PluginLoader.class.getClassLoader());
    }

    public PluginLoader() {
        super(new URL[] { }, PluginLoader.class.getClassLoader());
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
            Class<?>[] interfaces = theClass.getInterfaces();
            for (Class<?> tmpInterface : interfaces) {
                if (tmpInterface.isInterface() && tmpInterface.equals(theInterface)) {
                    return true;
                } else {
                    if (doesImplement(tmpInterface, theInterface)) {
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
     * @param filename file name of the plugin (relative path is accepted, too).
     * If the filename does not contain '.jar' suffix, it will be added automatically.
     * @param password emuStudio password.
     * @return Plugin main class, or null when an error occured or the main class is not found
     * @throws emulib.runtime.InvalidPasswordException
     * @throws emulib.runtime.InvalidPluginException
     * @throws java.io.IOException
     */
    public Class<Plugin> loadPlugin(String filename, String password) throws InvalidPasswordException, InvalidPluginException, IOException {
        API.testPassword(password);

        if (filename == null) {
            throw new InvalidPluginException("File name cannot be null");
        }
        try {
            File tmpFile = new File(filename);
            addURL(new URL("jar:file:/" + tmpFile.getAbsolutePath() + "!/"));
        } catch (MalformedURLException e) {
            throw new InvalidPluginException("Could not open JAR file", e);
        }
        scanFileForClasses(filename);
        Class<Plugin> mainClass = findPluginMainClass(filename);
        if (mainClass == null) {
            throw new InvalidPluginException("Could not find main class of the plugin");
        }
        return mainClass;
    }

    private void scanFileForClasses(String filename) throws IOException {
        JarInputStream jis = new JarInputStream(new FileInputStream(filename));
        JarEntry jarEntry;

        try {
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String jarEntryName = jarEntry.getName();
                if (!jarEntryName.toLowerCase().endsWith(".class")) {
                    continue;
                }                
                List<String> classesList = fileNameToClassesList.get(filename);
                if (classesList == null) {
                    classesList = new ArrayList<>();
                    fileNameToClassesList.put(filename, classesList);
                }
                String className = getValidClassName(jarEntryName);
                classesList.add(className);
                try {
                    loadClass(className);
                } catch (ClassNotFoundException e) {}
            }
        } finally {
            jis.close();
        }
    }

    private Class<Plugin> findPluginMainClass(String filename) {
        List<String> classes = fileNameToClassesList.get(filename);
        for (String className : classes) {
            try {
                Class definedClass = loadClass(className);
                if (trustedPlugin(definedClass)) {
                    return (Class<Plugin>) definedClass;
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not find loaded class: " + className, e);
            }
        }
        return null;
    }

    /**
     * Transform a relative file name into valid Java class name.
     *
     * For example, if the class file name is "somepackage/nextpackage/SomeClass.class", the method
     * will transform it to the format "somepackage.nextpackage.SomeClass".
     *
     * It doesnt't work for absolute file names.
     *
     * It doesn't hurt if the class name is already in valid Java format.
     *
     * @param classFileName File name defining class
     * @return valid Java class name
     */
    private String getValidClassName(String classFileName) {
        if (classFileName.toLowerCase().endsWith(".class")) {
            classFileName = classFileName.substring(0, classFileName.length() - 6);
        }
        classFileName = classFileName.replace("\\\\", "/").replace('/', '.');
        return classFileName.replace(File.separatorChar, '.');
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
        return doesImplement(pluginClass, Plugin.class);
    }

}
