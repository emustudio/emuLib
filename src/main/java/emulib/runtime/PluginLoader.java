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
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * This class provides methods for dynamic loading of emuStudio plug-ins (which in turn are JAR files.)
 *
 */
@NotThreadSafe
public class PluginLoader extends URLClassLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private final static EMULIB_VERSION CURRENT_EMULIB_VERSION = EMULIB_VERSION.VERSION_9;

    private final Map<File, List<String>> fileNameToClassesList = new HashMap<>();

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
    
    private List<File> getDependencies(File plugin, String dependencyBasePath) throws IOException {
        JarFile pluginJar = new JarFile(plugin);
        Manifest manifest = pluginJar.getManifest();
        
        String classPath = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
        List<File> dependencies = new ArrayList<>();
        if (classPath != null) {
            StringTokenizer tokenizer = new StringTokenizer(classPath);
            while (tokenizer.hasMoreTokens()) {
                dependencies.add(new File(
                        dependencyBasePath,
                        tokenizer.nextToken())
                );
            }
        }
        return dependencies;
    }

    private URL toJarURL(File plugin) throws MalformedURLException {
        String urlName = plugin.toURI().toURL().toString();
        urlName = "jar:" + urlName + "!/";
        return new URL(urlName);
    }

    /**
     * Method loads emuStudio plugin into memory.
     *
     * The plug-in should be in JAR format.
     *
     * @param file file name of the plugin (relative path is accepted, too).
     * If the filename does not contain '.jar' suffix, it will be added automatically.
     * @param password emuStudio password.
     * @return Plugin main class
     * @throws emulib.runtime.InvalidPasswordException if given password is invalid
     * @throws emulib.runtime.InvalidPluginException if main class could not be found.
     */
    public Class<Plugin> loadPlugin(File file, String password) throws InvalidPasswordException, InvalidPluginException {
       return loadPlugin(file, password, System.getProperty("user.dir"));
    }

    /**
     * Method loads emuStudio plugin into memory.
     *
     * The plug-in should be in JAR format.
     *
     * @param file file name of the plugin (relative path is accepted, too).
     * If the filename does not contain '.jar' suffix, it will be added automatically.
     * @param password emuStudio password.
     * @param dependenciesBasePath Base directory for loading plugin dependencies
     * @return Plugin main class
     * @throws emulib.runtime.InvalidPasswordException if given password is invalid
     * @throws emulib.runtime.InvalidPluginException if main class could not be found.
     */
    public Class<Plugin> loadPlugin(File file, String password, String dependenciesBasePath) throws InvalidPasswordException, InvalidPluginException {
        API.testPassword(password);

        Objects.requireNonNull(file);
        Objects.requireNonNull(dependenciesBasePath);
        try {
            addURL(toJarURL(file));

            List<File> dependencies = getDependencies(file, dependenciesBasePath);
            List<String> dependenciesStrings = new ArrayList<>();
            for (File dependency : dependencies) {
                addURL(toJarURL(dependency));
                dependenciesStrings.add(dependency.getAbsolutePath());
            }
            LOGGER.debug("[plugin={}] List of dependencies: {}",
                    file, Arrays.toString(dependenciesStrings.toArray()));
            scanFileForClasses(file);
        } catch (IOException e) {
            throw new InvalidPluginException("Could not load JAR file", e);
        }
        Class<Plugin> mainClass = findPluginMainClass(file);
        return mainClass;
    }

    private void scanFileForClasses(File file) throws IOException {
        JarInputStream jis = new JarInputStream(new FileInputStream(file));
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
                List<String> classesList = fileNameToClassesList.get(file);
                if (classesList == null) {
                    classesList = new ArrayList<>();
                    fileNameToClassesList.put(file, classesList);
                }
                String className = getValidClassName(jarEntryName);
                classesList.add(className);
            }
        } finally {
            jis.close();
        }
    }

    private Class<Plugin> findPluginMainClass(File file) throws InvalidPluginException {
        List<String> classes = fileNameToClassesList.get(file);

        for (String className : classes) {
            try {
                Class definedClass = findLoadedClass(className);
                if (definedClass == null) {
                    definedClass = findClass(className);
                }
                if (definedClass != null && trustedPlugin(definedClass)) {
                    return (Class<Plugin>) definedClass;
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                throw new InvalidPluginException("Could not find loaded class: " + className, e);
            }
        }
        throw new InvalidPluginException("Could not find plug-in main class");
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
