/*
 * ContextPool.java
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

import emulib.emustudio.API;
import emulib.plugins.Context;
import emulib.plugins.compiler.CompilerContext;
import emulib.plugins.cpu.CPUContext;
import emulib.plugins.device.DeviceContext;
import emulib.plugins.memory.MemoryContext;
import emulib.runtime.interfaces.PluginConnections;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages all plug-in contexts.
 *
 * Plug-ins should register their contexts manually. Other plug-ins that have permissions, can gather contexts by
 * querying this pool.
 *
 * @author vbmacher
 */
public class ContextPool {
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextPool.class);

    /**
     * The following map stores all registered contexts.
     *
     * Contexts implementing the same context interfaces are stored to the end of the list under the same map key
     */
    private final Map<String,List<Context>> allContexts = new HashMap<String, List<Context>>();

    /**
     * This map represents owners of registered contexts (these are keys).
     * It is used for checking the plug-in permissions.
     */
    private final Map<Long,List<Context>> contextOwners = new HashMap<Long, List<Context>>();

    private final static ContextPool instance = new ContextPool();

    /**
     * Virtual computer loaded by emuStudio
     */
    private final AtomicReference<PluginConnections> computer = new AtomicReference<PluginConnections>();

    private final ReadWriteLock registeringLock = new ReentrantReadWriteLock();

    /**
     * Private constructor.
     */
    private ContextPool() {
    }

    /**
     * Return an instance of this class. By calling more than 1 time, the same
     * instance is returned.
     *
     * @return ContextPool instance
     */
    public static ContextPool getInstance() {
        return instance;
    }

    /**
     * This method registers plug-in's context interface.
     *
     * The registration is needed because of contexts centralization. Other plug-ins can get a context by
     * querying the pool. Usually, emuStudio does the job during loading of the virtual computer.
     *
     * Requirements for the context are:
     *   - It is allowed to implement one and only one context
     *   - context interface must extend Context interface provided by emuLib
     *   - context interface must be annotated with @ContextType annotation
     *
     * @param pluginID owner plugin ID of the context contextsByOwner
     * @param context The context object that the plug-in want to register
     * @param contextInterface The interface that the context has to implement
     * @throws AlreadyRegisteredException Raised when a plug-in tries to register context that is already registered.
     * @throws InvalidContextException Raised when a class does not implement given interface, or it is not
     *         annotated, or if the context interface does not fulfill context requirements.
     */
    public void register(long pluginID, Context context, Class<? extends Context> contextInterface)
            throws AlreadyRegisteredException, InvalidContextException {
        trustedContext(contextInterface);
        String contextHash = computeHash(contextInterface);

        // check if the contextInterface is implemented by the context
        if (!PluginLoader.doesImplement(context.getClass(), contextInterface)) {
           throw new InvalidContextException("Context does not implement context interface");
        }

        registeringLock.writeLock().lock();
        try {
            // check if the context is already registered
            List<Context> contextsByHash = allContexts.get(contextHash);
            List<Context> contextsByOwner = contextOwners.get(pluginID);

            if (contextsByHash != null) {
                // Test if the context instance is already there
                if (contextsByHash.contains(context)) {
                    throw new AlreadyRegisteredException();
                }
                if (contextsByOwner != null) {
                    // Test if the context owner already has a context with identical hash
                    for (Context contextByHash : contextsByHash) {
                        if (contextsByOwner.contains(contextByHash)) {
                            throw new AlreadyRegisteredException();
                        }
                    }
                }
            }

            // finally register the context
            if (contextsByOwner == null) {
                contextsByOwner = new ArrayList<Context>();
                contextOwners.put(pluginID, contextsByOwner);
            }
            contextsByOwner.add(context);

            if (contextsByHash == null) {
                contextsByHash = new ArrayList<Context>();
                allContexts.put(contextHash, contextsByHash);
            }
            contextsByHash.add(context);
        } finally {
            registeringLock.writeLock().unlock();
        }
    }

    /**
     * Check if the provided class is a context.
     *
     * @param contextInterface the context interface
     */
    private void trustedContext(Class<? extends Context> contextInterface) throws InvalidContextException {
        if (contextInterface == null) {
            throw new InvalidContextException("Interface is null");
        }
        if (!contextInterface.isInterface()) {
            throw new InvalidContextException("Given class is not interface");
        }
        if (!contextInterface.isAnnotationPresent(emulib.annotations.ContextType.class)) {
            throw new InvalidContextException("Interface is not annotated as context");
        }
    }

    /**
     * Unregisters all contexts of given context interface.
     *
     * It will do it only if the plug-in has the permission. The permission is approved if and only if the contexts are
     * implemented inside the plug-in.
     *
     * @param pluginID plugin ID of the context owner
     * @param contextInterface the context interface
     * @return true if at least one context was unregistered successfully, false otherwise.
     * @throws InvalidContextException Raised when context interface is not annotated, or if the context interface does
     *         not fulfill context requirements.
     *
     */
    public boolean unregister(long pluginID, Class<? extends Context> contextInterface) throws InvalidContextException {
        trustedContext(contextInterface);
        String contextHash = computeHash(contextInterface);

        registeringLock.writeLock().lock();
        try {
            List<Context> contextsByOwner = contextOwners.get(pluginID);
            if (contextsByOwner == null) {
                return false;
            }

            List<Context> contextsByHash = allContexts.get(contextHash);

            if (contextsByHash == null) {
                return false;
            }

            boolean result = true;
            Iterator<Context> contextIterator = contextsByHash.iterator();
            while (contextIterator.hasNext()) {
                Context context = contextIterator.next();
                if (contextsByOwner.contains(context)) {
                    result = result && contextsByOwner.remove(context);
                    contextIterator.remove();
                }
            }
            if (contextsByHash.isEmpty()) {
                allContexts.remove(contextHash);
            }
            return result;
        } finally {
            registeringLock.writeLock().unlock();
        }
    }

    /**
     * Set a computer, represented as plug-in connections, loaded by emuStudio.
     *
     * This method should be called only by the emuStudio.
     *
     * @param password emuStudio password
     * @param computer virtual computer, loaded by emuStudio
     * @return true if computer was set successfully; false otherwise.
     * @throws InvalidPasswordException if the password was incorrect
     */
    public boolean setComputer(String password, PluginConnections computer) throws InvalidPasswordException {
        API.testPassword(password);
        this.computer.set(computer);
        return true;
    }

    /**
     * Clear the context instance.
     *
     * @param password emuStudio password
     * @throws InvalidPasswordException if the password was incorrect
     */
    public void clearAll(String password) throws InvalidPasswordException {
        API.testPassword(password);
        this.computer.set(null);
        registeringLock.writeLock().lock();
        try {
            allContexts.clear();
            contextOwners.clear();
        } finally {
            registeringLock.writeLock().unlock();
        }
    }

    /**
     * Get plug-in context.
     *
     * @param pluginID ID of requesting plug-in
     * @param contextInterface wanted context interface (implemented by the plug-in)
     * @param index the index if more than one context are found. If -1 is provided, any matched context is used
     * @return requested context; null if the context does not exist or the plug-in is not allowed to get it
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public Context getContext(long pluginID, Class<? extends Context> contextInterface, int index) throws InvalidContextException {
        trustedContext(contextInterface);
        registeringLock.readLock().lock();
        try {
            // find the requested context
            List<Context> contextsByHash = allContexts.get(computeHash(contextInterface));
            if ((contextsByHash == null) || contextsByHash.isEmpty()) {
                LOGGER.debug("Context " + contextInterface + " is not found in registered contexts list.");
                return null;
            }
            LOGGER.debug("Matching context " + contextInterface + " from " + contextsByHash.size() + " options...");

            // find context based on contextID
            int j = 0;
            for (Context context : contextsByHash) {
                if (checkPermission(pluginID, context)) {
                    if ((index == -1) || (j == index)) {
                        LOGGER.debug("Found context with index " + j);
                        return context;
                    }
                }
                j++;
            }
            LOGGER.error("The plugin with ID " + pluginID + " has no permission to access context " + contextInterface);
            return null;
        } finally {
            registeringLock.readLock().unlock();
        }
    }

    /**
     * Get registered CPU context.
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the CPU in the abstract schema.
     *
     * If the CPU has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCPUContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @return CPUContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public CPUContext getCPUContext(long pluginID, Class<? extends CPUContext> contextInterface) throws InvalidContextException {
        return (CPUContext)getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered CPU context (extended version).
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the CPU in the abstract schema.
     *
     * If the CPU has more than one context implementing required context interface, it returns context indexed by index
     * parameter.
     *
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @param index 0-based the order of the context if they are more than one. Does nothing if the index is out of
     *        the bounds. If index is -1, it uses any found context.
     * @return CPUContext object if it is found and the plug-in has the permission; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public CPUContext getCPUContext(long pluginID, Class<? extends CPUContext> contextInterface, int index) throws InvalidContextException {
        return (CPUContext)getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered compiler context.
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the compiler in the abstract schema.
     *
     * If the compiler has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCompilerContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plug-in requesting the compiler context
     * @param contextInterface Interface of the context, if requesting plugin has permission to acccess it
     * @return CompilerContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public CompilerContext getCompilerContext(long pluginID, Class<? extends CompilerContext> contextInterface) throws InvalidContextException {
        return (CompilerContext)getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered compiler context (extended version).
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the compiler in the abstract schema.
     *
     * If the compiler has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param pluginID plug-in requesting the Compiler context
     * @param contextInterface Interface of the context
     * @param index the order of the context if they are more than one. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return CompilerContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public CompilerContext getCompilerContext(long pluginID, Class<? extends CompilerContext> contextInterface, int index) throws InvalidContextException {
        return (CompilerContext)getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered memory context.
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the memory in the abstract schema.
     *
     * If the memory has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getMemoryContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @return MemoryContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public MemoryContext getMemoryContext(long pluginID, Class<? extends MemoryContext> contextInterface) throws InvalidContextException {
        return (MemoryContext)getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered memory context (extended version).
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the memory in the abstract schema.
     *
     * If the memory has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @param index the index of the context if they are more than one. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return MemoryContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public MemoryContext getMemoryContext(long pluginID, Class<? extends MemoryContext> contextInterface, int index) throws InvalidContextException {
        return (MemoryContext)getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered device context.
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the device in the abstract schema.
     *
     * If the device has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getDeviceContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @return DeviceContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public DeviceContext getDeviceContext(long pluginID, Class<? extends DeviceContext> contextInterface) throws InvalidContextException {
        return (DeviceContext)getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered device context (extended version).
     *
     * If plug-in doesn't have the permission to access it, return null. The permission is approved, when the
     * plug-in is connected to the device in the abstract schema.
     *
     * If the device has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @param index index of the context implementation. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return DeviceContext object if it is found and the plug-in has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fullfill context requirements
     */
    public DeviceContext getDeviceContext(long pluginID, Class<? extends DeviceContext> contextInterface, int index) throws InvalidContextException {
        return (DeviceContext)getContext(pluginID, contextInterface, index);
    }

    /**
     * This method check if the plug-in has the permission to access specified context.
     *
     * The permission is granted if and only if the context is connected to the plug-in inside virtual computer.
     *
     * Note: it can be called only when registeringLock is held.
     *
     * @param pluginID plug-in to check
     * @param context requested context
     * @return true if the plug-in is approved to access the context; false otherwise
     */
    private boolean checkPermission(long pluginID, Context context) {
        // check if it is possible to check the plug-in for the permission
        PluginConnections tmpComputer = computer.get();
        if (tmpComputer == null) {
            LOGGER.debug("Plugin with ID=" + pluginID + " cannot have access to context " + context + ": Computer is not set.");
            return false;
        }
        // first it must be found the contextsByOwner of the ContextPool.
        Long contextOwner = null;
        for (Map.Entry<Long, List<Context>> owner : contextOwners.entrySet()) {
            List<Context> contextsByOwner = owner.getValue();
            if (contextsByOwner == null) {
                continue;
            }
            if (contextsByOwner.contains(context)) {
                contextOwner = owner.getKey();
                break;
            }
        }
        if (contextOwner == null) {
            LOGGER.debug("Plugin with ID=" + pluginID + " cannot have access to context " + context + ": Could not find context owner.");
            return false;
        }
        // THIS is the permission check
        LOGGER.debug("Checking permission of plugin with ID=" + pluginID + " to context owner with ID=" + contextOwner
                + " (" + context + ")");
        return tmpComputer.isConnected(pluginID, contextOwner);
    }

    /**
     * Compute emuStudio-specific hash of the context interface.
     * The name of the interface is not important, only method names and their
     * signatures.
     *
     * The final processing uses SHA-1 method.
     *
     * @param contextInterface interface to compute hash of
     * @return SHA-1 hash string of the interface
     */
    public static String computeHash(Class<? extends Context> contextInterface) {
        List<Method> contextMethods = Arrays.asList(contextInterface.getMethods());
        Collections.<Method>sort(contextMethods, new Comparator<Method>() {

            @Override
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }

        });

        String hash = "";
        for (Method method : contextMethods.toArray(new Method[0])) {
            hash += method.getGenericReturnType().toString() + " " + method.getName() + "(";
            for (Class<?> param : method.getParameterTypes()) {
                hash += param.getName() + ",";
            }
            hash += ");";
        }
        LOGGER.info(contextInterface.getSimpleName() + ": " + hash);
        try {
            return SHA1(hash);
        } catch(Exception e) {
            LOGGER.error("Could not compute hash for interface " + contextInterface, e);
            return null;
        }
    }

    /**
     * Compute SHA-1 hash string.
     *
     * Letters in the hash string will be in upper-case.
     *
     * @param text Data to make hash from
     * @return SHA-1 hash Hexadecimal string, null if there was some error
     */
    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash;
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return RadixUtils.convertToRadix(sha1hash, 16, false);
    }
}
