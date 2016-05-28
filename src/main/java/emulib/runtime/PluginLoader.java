/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2012-2016, Peter Jakubčo
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
import emulib.runtime.internal.Unchecked;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static java.util.stream.Collectors.toSet;

/**
 * This class provides methods for dynamic loading of emuStudio plug-ins (which in turn are JAR files.)
 *
 */
@NotThreadSafe
public class PluginLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private final static EMULIB_VERSION CURRENT_EMULIB_VERSION = EMULIB_VERSION.VERSION_9;

    private final String dependencyBasePath;

    PluginLoader(String dependencyBasePath) {
        this.dependencyBasePath = Objects.requireNonNull(dependencyBasePath);
    }

    public PluginLoader() {
        this(System.getProperty("user.dir"));
    }

    /**
     * Loads emuStudio plugins.
     *
     * The plug-ins are loaded into separate class loader.
     *
     * @param password emuStudio password.
     * @param pluginFiles plugin files.
     * @return List of plugins main classes
     * @throws emulib.runtime.InvalidPasswordException if given password is invalid
     * @throws emulib.runtime.InvalidPluginException if main class could not be found.
     */
    public Set<Class<Plugin>> loadPlugins(String password, File... pluginFiles) throws InvalidPasswordException,
            IOException {
        API.testPassword(password);

        Objects.requireNonNull(pluginFiles);

        final Set<URL> urlsToLoad = new HashSet<>();
        for (File pluginFile : pluginFiles) {
            urlsToLoad.add(pluginFile.toURI().toURL());
         //   urlsToLoad.addAll(findDependencies(pluginFile));
        }

        LOGGER.debug("Loading {} plugin files", urlsToLoad.size());
        URLClassLoader pluginsClassLoader = new URLClassLoader(urlsToLoad.toArray(new URL[urlsToLoad.size()]));

        try {
            return Arrays.asList(pluginFiles).stream()
                    .map(this::findClassesInJAR)
                    .map(l -> findMainClass(pluginsClassLoader, l))
                    .collect(toSet());
        } catch (Exception e) {
            if (e instanceof InvalidPluginException || e instanceof IOException) {
                throw e;
            }
            throw new IOException(e);
        }
    }

    /**
     * Checks if a class implements given interface.
     *
     * @param theClass class that will be tested
     * @param theInterface interface that the class should implement
     * @return true if the class implements given interface, false otherwise
     */
    static boolean doesImplement(Class<?> theClass, Class<?> theInterface) {
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

    private List<String> findClassesInJAR(File file) {
        List<String> classes = new ArrayList<>();

        try (JarInputStream jis = new JarInputStream(new FileInputStream(file))) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String jarEntryName = jarEntry.getName();
                if (!jarEntryName.toLowerCase().endsWith(".class")) {
                    continue;
                }
                String className = getValidClassName(jarEntryName);
                classes.add(className);
            }
        } catch (IOException e) {
            Unchecked.sneakyThrow(e);
        }

        return classes;
    }

    private Class<Plugin> findMainClass(ClassLoader classLoader, List<String> classes) {
        for (String className : classes) {
            try {
                Class definedClass = classLoader.loadClass(className);

                if (definedClass != null && trustedPlugin(definedClass)) {
                    return (Class<Plugin>) definedClass;
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                Unchecked.sneakyThrow(new InvalidPluginException("Could not find loaded class: " + className, e));
            }
        }
        Unchecked.sneakyThrow(new InvalidPluginException("Could not find plug-in main class"));
        return null; // never goes here
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
    static boolean trustedPlugin(Class<?> pluginClass) {
        Objects.requireNonNull(pluginClass);

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
