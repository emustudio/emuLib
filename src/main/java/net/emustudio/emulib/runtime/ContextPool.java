/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.runtime;

import net.emustudio.emulib.internal.Reflection;
import net.emustudio.emulib.internal.TokenVerifier;
import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.compiler.CompilerContext;
import net.emustudio.emulib.plugins.cpu.CPUContext;
import net.emustudio.emulib.plugins.device.DeviceContext;
import net.emustudio.emulib.plugins.memory.MemoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.emustudio.emulib.internal.Hashing.SHA1;

/**
 * This class manages all plugin contexts.
 *
 * plugins should register their contexts manually. Other plugins that have permissions, can gather contexts by
 * querying this pool.
 *
 * Context pool is not thread safe.
 *
 */
@SuppressWarnings("unused")
public class ContextPool {
    private final static Logger LOGGER = LoggerFactory.getLogger(ContextPool.class);

    /**
     * The following map stores all registered contexts.
     *
     * Contexts implementing the same context interfaces are stored to the end of the list under the same map key
     */
    private final Map<String,List<Context>> allContexts = new HashMap<>();

    /**
     * This map represents owners of registered contexts (these are keys).
     * It is used for checking the plugin permissions.
     */
    private final Map<Long,List<Context>> contextOwners = new HashMap<>();

    /**
     * Virtual computer loaded by emuStudio
     */
    private final AtomicReference<PluginConnections> computer = new AtomicReference<>();

    private final ReadWriteLock registeringLock = new ReentrantReadWriteLock();
    private final TokenVerifier tokenVerifier;

    ContextPool(TokenVerifier tokenVerifier) {
        this.tokenVerifier = Objects.requireNonNull(tokenVerifier);
    }


    /**
     * This method registers plugin's context interface.
     *
     * The registration is needed because of contexts centralization. Other plugins can get a context by
     * querying the pool. Usually, emuStudio does the job during loading of the virtual computer.
     *
     * Requirements for the context are:
     *   - It is allowed to implement one and only one context
     *   - context interface must extend Context interface provided by emuLib
     *   - context interface must be annotated with @ContextType annotation
     *
     * @param pluginID owner plugin ID of the context contextsByOwner
     * @param context The context object that the plugin want to register
     * @param contextInterface The interface that the context has to implement
     * @throws ContextAlreadyRegisteredException Raised when a plugin tries to register context that is already registered.
     * @throws InvalidContextException Raised when a class does not implement given interface, or it is not
     *         annotated, or if the context interface does not fulfill context requirements.
     */
    public void register(long pluginID, Context context, Class<? extends Context> contextInterface)
            throws ContextAlreadyRegisteredException, InvalidContextException {
        verifyPluginContext(contextInterface);
        String contextHash = computeHash(contextInterface);

        // check if the contextInterface is implemented by the context
        if (!Reflection.doesImplement(context.getClass(), contextInterface)) {
           throw new InvalidContextException("Context does not implement context interface");
        }

        registeringLock.writeLock().lock();
        try {
            // check if the context is already registered
            List<Context> contextsByHash = allContexts.get(contextHash);
            if (contextsByHash != null) {
                // Test if the context instance is already there
                if (contextsByHash.contains(context)) {
                    throw new ContextAlreadyRegisteredException();
                }
            }

            // finally register the context
            List<Context> contextsByOwner = contextOwners.computeIfAbsent(pluginID, k -> new ArrayList<>());
            contextsByOwner.add(context);

            if (contextsByHash == null) {
                contextsByHash = new ArrayList<>();
                allContexts.put(contextHash, contextsByHash);
            }
            contextsByHash.add(context);
        } finally {
            registeringLock.writeLock().unlock();
        }
    }

    /**
     * Check if the provided class is a valid plugin context.
     *
     * @param contextInterface the context interface
     */
    private void verifyPluginContext(Class<? extends Context> contextInterface) throws InvalidContextException {
        Objects.requireNonNull(contextInterface);

        if (!contextInterface.isInterface()) {
            throw new InvalidContextException("Given class is not interface");
        }
        if (!contextInterface.isAnnotationPresent(PluginContext.class)) {
            throw new InvalidContextException("Interface is not annotated as context");
        }
    }

    /**
     * Unregisters all contexts of given context interface.
     *
     * It will do it only if the plugin has the permission. The permission is approved if and only if the contexts are
     * implemented inside the plugin.
     *
     * @param pluginID plugin ID of the context owner
     * @param contextInterface the context interface
     * @return true if at least one context was unregistered successfully, false otherwise.
     * @throws InvalidContextException Raised when context interface is not annotated, or if the context interface does
     *         not fulfill context requirements.
     *
     */
    public boolean unregister(long pluginID, Class<? extends Context> contextInterface) throws InvalidContextException {
        verifyPluginContext(contextInterface);
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

            Iterator<Context> contextIterator = contextsByHash.iterator();
            while (contextIterator.hasNext()) {
                Context context = contextIterator.next();
                if (contextsByOwner.contains(context)) {
                    contextsByOwner.remove(context);
                    contextIterator.remove();
                }
            }
            if (contextsByHash.isEmpty()) {
                allContexts.remove(contextHash);
            }
            return true;
        } finally {
            registeringLock.writeLock().unlock();
        }
    }

    /**
     * Get plugin context.
     *
     * @param <T> Specific context type
     * @param pluginID plugin ID
     * @param contextInterface type of requested context
     * @param index
     *   the index if more than one context are found. If -1 is
     *   provided, any matched context is used
     * @return requested context, which plugin owner != pluginId
     * @throws InvalidContextException
     *   if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    @SuppressWarnings("unchecked")
    public <T extends Context> T getContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException {

        verifyPluginContext(contextInterface);
        registeringLock.readLock().lock();
        try {
            // find the requested context
            List<Context> contextsByHash = allContexts.get(computeHash(contextInterface));
            if ((contextsByHash == null) || contextsByHash.isEmpty()) {
                throw new ContextNotFoundException("Context "
                        + contextInterface
                        + " is not found in registered contexts list.");
            }
            LOGGER.debug("Matching context " + contextInterface + " from " + contextsByHash.size() + " options...");

            // find context based on contextID
            int currentIndex = 0;
            for (Context context : contextsByHash) {
                if (hasPermission(pluginID, context)) {
                    if ((index == -1) || (currentIndex == index)) {
                        LOGGER.debug("Found context with index " + currentIndex);
                        return (T)context;
                    }
                }
                currentIndex++;
            }
            throw new ContextNotFoundException("The plugin with ID "
                    + pluginID
                    + " has no permission to access context "
                    + contextInterface
            );
        } finally {
            registeringLock.readLock().unlock();
        }
    }

    /**
     * Get registered CPU context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the CPU in the abstract schema.
     *
     * If the CPU has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCPUContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plugin requesting the CPU context
     * @return CPUContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public CPUContext getCPUContext(long pluginID) throws InvalidContextException, ContextNotFoundException {
        return getContext(pluginID, CPUContext.class, -1);
    }

    /**
     * Get registered CPU context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the CPU in the abstract schema.
     *
     * If the CPU has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCPUContext(pluginID, contextInterface, -1);</code>
     *
     * @param <T> Specific CPU context
     * @param pluginID plugin requesting the CPU context
     * @param contextInterface Interface of the context
     * @return CPUContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends CPUContext> T getCPUContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered CPU context (extended version).
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the CPU in the abstract schema.
     *
     * If the CPU has more than one context implementing required context interface, it returns context indexed by index
     * parameter.
     *
     * @param <T> Specific CPU Context
     * @param pluginID plugin requesting the CPU context
     * @param contextInterface Interface of the context
     * @param index 0-based the order of the context if they are more than one. Does nothing if the index is out of
     *        the bounds. If index is -1, it uses any found context.
     * @return CPUContext object if it is found and the plugin has the permission; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends CPUContext> T getCPUContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered compiler context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the compiler in the abstract schema.
     *
     * If the compiler has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCompilerContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plugin requesting the compiler context
     * @return CompilerContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public CompilerContext getCompilerContext(long pluginID) throws InvalidContextException, ContextNotFoundException {
        return getContext(pluginID, CompilerContext.class, -1);
    }

    /**
     * Get registered compiler context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the compiler in the abstract schema.
     *
     * If the compiler has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getCompilerContext(pluginID, contextInterface, -1);</code>
     *
     * @param <T> Specific compiler context
     * @param pluginID plugin requesting the compiler context
     * @param contextInterface Interface of the context, if requesting plugin has permission to acccess it
     * @return CompilerContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends CompilerContext> T getCompilerContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered compiler context (extended version).
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the compiler in the abstract schema.
     *
     * If the compiler has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param <T> Specific compiler context
     * @param pluginID plugin requesting the Compiler context
     * @param contextInterface Interface of the context
     * @param index the order of the context if they are more than one. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return CompilerContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends CompilerContext> T getCompilerContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered memory context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the memory in the abstract schema.
     *
     * If the memory has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getMemoryContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plugin requesting the memory context
     * @return MemoryContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public MemoryContext<?> getMemoryContext(long pluginID) throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, MemoryContext.class, -1);
    }

    /**
     * Get registered memory context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the memory in the abstract schema.
     *
     * If the memory has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getMemoryContext(pluginID, contextInterface, -1);</code>
     *
     * @param <T> Specific memory context
     * @param pluginID plugin requesting the memory context
     * @param contextInterface Interface of the context
     * @return MemoryContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends MemoryContext<?>> T getMemoryContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered memory context (extended version).
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the memory in the abstract schema.
     *
     * If the memory has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param <T> Specific memory context
     * @param pluginID plugin requesting the memory context
     * @param contextInterface Interface of the context
     * @param index the index of the context if they are more than one. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return MemoryContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends MemoryContext<?>> T getMemoryContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, index);
    }

    /**
     * Get registered device context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the device in the abstract schema.
     *
     * If the device has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getDeviceContext(pluginID, contextInterface, -1);</code>
     *
     * @param pluginID plugin requesting the device context
     * @return DeviceContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public DeviceContext<?> getDeviceContext(long pluginID) throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, DeviceContext.class, -1);
    }

    /**
     * Get registered device context.
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the device in the abstract schema.
     *
     * If the device has more than one context implementing required context interface, the first one is returned. To
     * access other ones, use extended version of the method.
     *
     * This method call is equivalent to the call of <code>getDeviceContext(pluginID, contextInterface, -1);</code>
     *
     * @param <T> Specific device context
     * @param pluginID plugin requesting the device context
     * @param contextInterface Interface of the context
     * @return DeviceContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends DeviceContext<?>> T getDeviceContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, -1);
    }

    /**
     * Get registered device context (extended version).
     *
     * If plugin doesn't have the permission to access it, return null. The permission is approved, when the
     * plugin is connected to the device in the abstract schema.
     *
     * If the device has more than one context implementing required context interface, it returns context indexed by
     * index parameter.
     *
     * @param <T> Specific device context
     * @param pluginID plugin requesting the device context
     * @param contextInterface Interface of the context
     * @param index index of the context implementation. Does nothing if the index is out of bounds.
     * If the index is -1, it uses any found context.
     * @return DeviceContext object if it is found and the plugin has the permission to access it; null otherwise
     * @throws InvalidContextException if the context interface does not fulfil context requirements
     * @throws ContextNotFoundException
     *   if the context does not exist or the plugin is not allowed to get it
     */
    public <T extends DeviceContext<?>> T getDeviceContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException {

        return getContext(pluginID, contextInterface, index);
    }

    /**
     * Set a computer, represented as plugin connections, loaded by emuStudio.
     *
     * This method should be called only by the emuStudio.
     *
     * @param emustudioToken emuStudio token
     * @param computer virtual computer, loaded by emuStudio
     * @return true if computer was set successfully; false otherwise.
     * @throws InvalidTokenException if the emustudioToken was incorrect
     * @throws NullPointerException if computer is null
     */
    public boolean setComputer(String emustudioToken, PluginConnections computer) throws InvalidTokenException {
        tokenVerifier.verifyToken(emustudioToken);
        this.computer.set(Objects.requireNonNull(computer));
        return true;
    }

    /**
     * Checks if a plugin has permission to access a context.
     *
     * The permission is granted if and only if the context is connected to the plugin inside virtual computer.
     *
     * Note: it can be called only when registeringLock is held.
     *
     * @param pluginID plugin to check
     * @param context requested context (of another plugin)
     * @return true if the plugin is permitted to access the context; false otherwise
     */
    private boolean hasPermission(long pluginID, Context context) {
        PluginConnections tmpComputer = Objects.requireNonNull(computer.get(), "Computer is not set");

        Optional<Long> contextOwner = findContextOwner(context);
        return contextOwner.filter(co -> tmpComputer.isConnected(pluginID, co)).isPresent();
    }

    private Optional<Long> findContextOwner(Context context) {
        Long contextOwner = null;
        for (Map.Entry<Long, List<Context>> owner : contextOwners.entrySet()) {
            List<Context> contextsByOwner = owner.getValue();
            if (contextsByOwner.contains(context)) {
                contextOwner = owner.getKey();
                break;
            }
        }
        return Optional.ofNullable(contextOwner);
    }

    /**
     * Compute emuStudio-specific hash of the context interface.
     * The name of the interface is not important, only method names and their signatures.
     *
     * @param contextInterface interface to compute hash of
     * @return hash representing the interface
     */
    private static String computeHash(Class<? extends Context> contextInterface) throws InvalidContextException {
        List<Method> contextMethods = Arrays.asList(contextInterface.getMethods());
        contextMethods.sort(Comparator.comparing(Method::getName));

        StringBuilder hash = new StringBuilder();
        for (Method method : contextMethods.toArray(new Method[0])) {
            hash.append(method.getGenericReturnType().toString()).append(" ").append(method.getName()).append("(");
            for (Class<?> param : method.getParameterTypes()) {
                hash.append(param.getName()).append(",");
            }
            hash.append(");");
        }
        try {
            return SHA1(hash.toString());
        } catch(NoSuchAlgorithmException e) {
            throw new InvalidContextException("Could not compute hash for interface " + contextInterface, e);
        }
    }
}
