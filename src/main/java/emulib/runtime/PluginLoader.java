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
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods for dynamic loading JAR files (e.g.
 * plug-ins or specific plug-in libraries)
 *
 * @author vbmacher
 */
public class PluginLoader extends ClassLoader {
    private final static Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private final static EMULIB_VERSION CURRENT_EMULIB_VERSION = EMULIB_VERSION.VERSION_9;
    
    // Instance of this class
    private static PluginLoader instance = new PluginLoader();
    
    // loaded resources of all classes
    private Map<String, URL> allResources = new HashMap<String, URL>();
    
    private List<String> knownDependencies = new ArrayList<String>();
    
    // security manager for this class loader
    private PluginSecurityManager securityManager;
    private List<Class<?>> classesToResolve = new ArrayList<Class<?>>();
    private List<NotLoadedJAR> undoneClassesToLoad = new ArrayList<NotLoadedJAR>();
    
    private static class LoadedJAR {
        String filename;
        private Manifest manifest;
        private Map<String, byte[]> classesData;
        
        public LoadedJAR(String filename, Manifest manifest, Map<String, byte[]> classesData) {
            this.filename = filename;
            this.manifest = manifest;
            this.classesData = classesData;
        }
        
        public String getFileName() {
            return filename;
        }
        
        public Manifest getManifest() {
            return manifest;
        }
        
        public Map<String, byte[]> getClassesData() {
            return classesData;
        }
    }
    
    private static class NotLoadedJAR {
        private List<String> undone;
        private String filename;
        
        public NotLoadedJAR(List<String> undone, String filename) {
            this.undone = undone;
            this.filename = filename;
        }
        
        public List<String> getUndone() {
            return undone;
        }
        
        public String getFilename() {
            return filename;
        }
        
        @Override
        public String toString() {
            return "NLC[file=" + filename + "; undone=" + undone + "]";
        }
    }

    /**
     * Private constructor. Other objects could not create the instance in
     * classic way.
     */
    private PluginLoader() {
        securityManager = new PluginSecurityManager();
        System.setSecurityManager(securityManager);
        registerKnownDependencies(getClass());
    }

    /**
     * Registers all known dependencies for specified class.
     * 
     * These dependencies will be ignored during loading plug-in dependencies - they will be considered as already
     * loaded.
     * 
     * @param className Class for which we determine dependencies. This method should be called only once per
     * class-loader.
     * @return true if some dependencies were registered; false if no new dependencies were recognized.
     */
    private boolean registerKnownDependencies(Class className) {
        boolean dependencyAdded = false;
        try {
            Enumeration<URL> resources = className.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                LOGGER.debug("Found manifest: " + resource);
                try {
                    Manifest manifest = new Manifest(resource.openStream());
                    String classPath;
                    classPath = manifest.getMainAttributes().getValue("Class-Path");
                    if (classPath != null) {
                        String[] classPathList = classPath.split(" ");
                        for (String dependency : classPathList) {
                            dependency = getRelativeName(dependency);
                            if (dependency != null && !knownDependencies.contains(dependency)) {
                                LOGGER.debug("Registering known dependency: " + dependency);
                                knownDependencies.add(dependency);
                                dependencyAdded = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to determine dependencies for manifest: " + resource, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not register known dependencies for class " + className, e);
        }
        return dependencyAdded;
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
        allResources.clear();
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

    private String getRelativeName(String absoluteName) {
        absoluteName = absoluteName.replaceAll("\\\\", "/");
        int lastSlash = absoluteName.lastIndexOf("/");
        if (lastSlash > 0) {
            absoluteName = absoluteName.substring(lastSlash + 1, absoluteName.length());
        }
        if (absoluteName.trim().equals("")) {
            return null;
        }
        return absoluteName;
    }

    private void loadResource(String jarFileName, String resourceFileName) throws UnsupportedEncodingException,
            MalformedURLException {
        //for windows: "jar:file:/D:/JavaApplicat%20ion12/dist/JavaApplication12.jar!/resources/Find%2024.gif";
        //for linux:   "jar:file:/home/vbmacher/dev/school%20projects/shit.jar!/resources/Find%2024.gif";
        String jarName = jarFileName.replaceAll("\\\\", "/");
        String resourceName = resourceFileName.replaceAll("\\\\", "/");
        if (!jarName.startsWith("/")) {
            jarName = "/" + jarName;
        }
        String resourceURL = URLEncoder.encode("jar:file:" + jarName + "!/" + resourceName, "UTF-8");
        resourceURL = resourceURL.replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%21", "!").replaceAll("\\+", "%20");
        allResources.put("/" + resourceFileName, new URL(resourceURL));
    }
    
    private byte[] loadJARData(JarEntry jarEntry, JarInputStream jis) throws IOException {
        long size = jarEntry.getSize();
        int intSize = (int)size;
        if (size == -1) {
            LOGGER.error("Could not determine size of JAR entry: " + jarEntry.getName());
            return null;
        }
        if (size != (long)intSize) {
            LOGGER.error("Could not load JAR entry, it is too big: " + jarEntry.getName());
            return null;
        }
        byte[] classData = new byte[intSize];
        int offset = 0;
        int bytesRead;
        while ((intSize - offset) > 0) {
            bytesRead = jis.read(classData, offset, intSize - offset);
            if (bytesRead == -1) {
                break;
            }
            offset += bytesRead;
        }
        return classData;
    }

    private LoadedJAR loadJARFile(String filename) throws IOException {
        return loadJARFile(filename, null, true);
    }
    
    private LoadedJAR loadJARFile(String filename, List<String> onlyClasses) throws IOException {
        return loadJARFile(filename, onlyClasses, false);
    }

    private LoadedJAR loadJARFile(String filename, List<String> onlyClasses, boolean loadResources) throws IOException {
        Map<String, byte[]> classesData = new HashMap<String, byte[]>();
        JarFile jarFile = new JarFile(filename);
        FileInputStream fis = new FileInputStream(jarFile.getName());
        BufferedInputStream bis = new BufferedInputStream(fis);
        JarInputStream jis = new JarInputStream(bis);
        JarEntry jarEntry;
        Manifest manifest = jis.getManifest();

        while ((jarEntry = jis.getNextJarEntry()) != null) {
            if (jarEntry.isDirectory()) {
                continue;
            }
            String jarEntryName = jarEntry.getName();
            if (!jarEntryName.toLowerCase().endsWith(".class")) {
                if (loadResources) {
                    loadResource(filename, jarEntryName);
                }
                continue;
            }
            if (onlyClasses != null && !onlyClasses.contains(jarEntryName)) {
                continue;
            }
            try {
                if (findSystemClass(getValidClassName(jarEntryName)) != null) {
                    continue;
                }
            } catch (ClassNotFoundException e) {
            }
            // load class data
            byte[] classData = loadJARData(jarEntry, jis);
            if (classData == null) {
                LOGGER.error("Could not load class: " + jarEntryName + " from JAR: " + filename);
                continue;
            }
            classesData.put(jarEntryName, classData);
        }
        jis.close();
        bis.close();
        fis.close();
        jarFile.close();
        
        return new LoadedJAR(filename, manifest, classesData);
    }

    /**
     * Loads all dependencies for the loaded JAR file.
     * 
     * @param loadedJAR loaded JAR file
     * @throws InvalidPasswordException 
     */
    private void loadDependencies(LoadedJAR loadedJAR) throws InvalidPasswordException {
        // look for manifest and try to load dependencies
        Manifest manifest = loadedJAR.getManifest();
        if (manifest == null) {
            return;
        }
        String classPath = manifest.getMainAttributes().getValue("Class-Path");
        if (classPath == null) {
            return;
        }
        String[] classPathList = classPath.split(" ");
        for (String dependency : classPathList) {
            String relativeDependency = getRelativeName(dependency);
            if (relativeDependency != null) {
                if (!knownDependencies.contains(relativeDependency)) {
                    LOGGER.debug("Loading " + getRelativeName(loadedJAR.getFileName()) + " depencency: " + relativeDependency);
                    try {
                        loadJAR(dependency);
                        knownDependencies.add(relativeDependency);
                    } catch (InvalidPluginException e) {
                        LOGGER.warn("Could not load dependency: " + relativeDependency);
                    }
                }

            }
        }
    }

    /**
     * Method loads emuStudio plugin into memory.
     * 
     * The plug-in should be in JAR format. The loaded classes are not resolved. After this method call
     * (and all possible multiple calls), 
     * {@link emulib.runtime.PluginLoader#resolveLoadedClasses() PluginLoader.resolveLoadedClasses} method must be
     * called.
     * 
     * @param filename name of the plugin (absolute path is better). If the filename does not contain '.jar' suffix,
     *                 it will be added automatically.
     * @param password emuStudio password.
     * @return Plugin main class, or null when an error occured or the main class is not found
     */
    public Class<Plugin> loadPlugin(String filename, String password) throws InvalidPasswordException, InvalidPluginException {
        API.testPassword(password);
        
        Class<Plugin> mainClass = loadJAR(filename);
        if (mainClass == null) {
            throw new InvalidPluginException();
        }
        return mainClass;
    }
    
    /**
     * Loads a JAR file into memory - all its classes and resources.
     * 
     * @param filename Absolute or relative path to the file
     * @return Class representing plug-in main class if the JAR represents an emuStudio plug-in; null otherwise.
     * @throws InvalidPluginException If something goes wrong
     */
    private Class<Plugin> loadJAR(String filename) throws InvalidPluginException {
        List<String> undoneClasses = new ArrayList<String>();
        LoadedJAR loadedJAR;
        Class<Plugin> mainClass = null;

        try {
            // load all classes in jar
            LOGGER.debug("Loading JAR file: " + filename);
            loadedJAR = loadJARFile(filename);
            loadDependencies(loadedJAR);

            for (Map.Entry<String, byte[]> classData : loadedJAR.getClassesData().entrySet()) {
                try {
                    // try to define class
                    Class<?> definedClass = defineLoadedClass(classData.getKey(), classData.getValue());
                    if (trustedPlugin(definedClass)) {
                        mainClass = (Class<Plugin>)definedClass;
                    }
                    classesToResolve.add(definedClass);
                } catch (Exception nf) {
                    undoneClasses.add(classData.getKey());
                }
            }
            
            // try to load all undone classes
            if (!undoneClasses.isEmpty()) {
                List<Class<?>> definedClasses;
                do {
                    definedClasses = loadUndoneClasses(undoneClasses, filename);
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
                    undoneClassesToLoad.add(new NotLoadedJAR(undoneClasses, filename));
                }
            }
        } catch (Exception e) {
            throw new InvalidPluginException("Could not load the plug-in.", e);
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
        for (NotLoadedJAR nlc : undoneClassesToLoad) {
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
        if (allResources.containsKey(name)) {
            URL url = (URL) allResources.get(name);
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
        if (allResources.containsKey(name)) {
            URL url = (URL) allResources.get(name);
            try {
                return url != null ? url.openStream() : null;
            } catch (Exception e) {
                LOGGER.debug(new StringBuilder().append("Could not get resource as stream.")
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
     * @param filename File name of the JAR file.
     * @return list of all classes that were loaded successfully
     */
    private List<Class<?>> loadUndoneClasses(List<String> undone, String filename) {
        List<Class<?>> resultClasses = new ArrayList<Class<?>>();
        LoadedJAR loadedJAR;
        
        try {
            loadedJAR = loadJARFile(filename, undone);
            for (Map.Entry<String, byte[]> classData : loadedJAR.getClassesData().entrySet()) {
                try {
                    // try load class data
                    Class<?> cl = defineLoadedClass(classData.getKey(), classData.getValue());
                    classesToResolve.add(cl);
                    resultClasses.add(cl);
                    undone.remove(classData.getKey());
                } catch (ClassNotFoundException nf) {
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not load undone classes.", e);
        }
        return resultClasses;
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
     * Method define loaded class (transform class data to class object).
     *
     * @param classname Name of the class.
     *        Should be in the form "package.ClassName", but it doesn't matter
     *        if it has the form "package/ClassName.class"
     * @param classbytes Class data
     * @return Class object
     * @throws ClassNotFoundException
     */
    private Class<?> defineLoadedClass(String classname, byte[] classbytes) throws ClassNotFoundException {
        classname = getValidClassName(classname);
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
                classToLoad = defineClass(null, classbytes, 0, classbytes.length);
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
        List<NotLoadedJAR> stillNotLoaded = new ArrayList<NotLoadedJAR>();
        do {
            NotLoadedJAR classToLoad = undoneClassesToLoad.get(0);
            List<String> undone = classToLoad.getUndone();
            boolean somethingLoaded;
            do {
                somethingLoaded = !loadUndoneClasses(undone, classToLoad.getFilename()).isEmpty();
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
     * {@link emulib.runtime.PluginLoader#loadJARFile(String) PluginLoader.loadJAR} method.
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
