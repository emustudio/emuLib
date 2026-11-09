/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application;

import net.emustudio.emulib.plugin.Context;
import net.emustudio.emulib.plugin.Plugin;
import net.emustudio.emulib.plugin.device.DeviceContext;
import net.emustudio.emulib.plugin.memory.MemoryContext;

/**
 * Context pool manages contexts of all plugins.
 * <p>
 * A plugin registers its contexts in its constructor and obtains contexts of other (connected) plugins in
 * {@link Plugin#initialize()}.
 */
public interface ContextPool {

    /**
     * Register plugin context.
     * <p>
     * Context interface is the "key" under which the context object is registered. Lookup is by exact interface, so
     * memory and device contexts should be registered also under {@link MemoryContext} or {@link DeviceContext}.
     * Contexts registered under the same interface (by any plugin) are indexed from 0 in registration order.
     *
     * @param pluginID         owner plugin ID
     * @param context          the context object
     * @param contextInterface context interface implemented by the context object
     * @throws IllegalArgumentException if contextInterface is not an interface or context does not implement it
     * @throws IllegalStateException    if the context is already registered
     */
    void register(long pluginID, Context context, Class<? extends Context> contextInterface);

    /**
     * Unregisters all contexts of the plugin registered under given context interface.
     *
     * @param pluginID         owner plugin ID
     * @param contextInterface context interface
     * @return true if contexts were unregistered, false if there was nothing to unregister
     */
    boolean unregister(long pluginID, Class<? extends Context> contextInterface);

    /**
     * Get registered context of a plugin connected to the requesting plugin.
     *
     * @param <T>              context type
     * @param pluginID         requesting plugin ID
     * @param contextInterface context interface
     * @param index            0-based index of the context object, or -1 for the first accessible one
     * @return plugin context
     * @throws ContextNotFoundException if the context does not exist or the plugin is not allowed to get it
     */
    <T extends Context> T get(long pluginID, Class<T> contextInterface, int index) throws ContextNotFoundException;

    /**
     * Get first accessible registered context. Same as {@code get(pluginID, contextInterface, -1)}.
     *
     * @param <T>              context type
     * @param pluginID         requesting plugin ID
     * @param contextInterface context interface
     * @return plugin context
     * @throws ContextNotFoundException if the context does not exist or the plugin is not allowed to get it
     */
    default <T extends Context> T get(long pluginID, Class<T> contextInterface) throws ContextNotFoundException {
        return get(pluginID, contextInterface, -1);
    }

    /**
     * Get first accessible memory context with given cell type.
     *
     * @param <C>      memory cell type
     * @param pluginID requesting plugin ID
     * @param cellType expected memory cell type
     * @return memory context
     * @throws ContextNotFoundException if the memory does not exist, is not accessible or has different cell type
     */
    @SuppressWarnings("unchecked")
    default <C> MemoryContext<C> getMemory(long pluginID, Class<C> cellType) throws ContextNotFoundException {
        MemoryContext<?> memory = get(pluginID, MemoryContext.class);
        if (memory.getCellTypeClass() != cellType) {
            throw new ContextNotFoundException(
                    "Unexpected memory cell type. Expected " + cellType + " but was: " + memory.getCellTypeClass());
        }
        return (MemoryContext<C>) memory;
    }

    /**
     * Get first accessible device context with given data type.
     *
     * @param <D>      device data type
     * @param pluginID requesting plugin ID
     * @param dataType expected device data type
     * @return device context
     * @throws ContextNotFoundException if the device does not exist, is not accessible or has different data type
     */
    @SuppressWarnings("unchecked")
    default <D> DeviceContext<D> getDevice(long pluginID, Class<D> dataType) throws ContextNotFoundException {
        DeviceContext<?> device = get(pluginID, DeviceContext.class);
        if (device.getDataType() != dataType) {
            throw new ContextNotFoundException(
                    "Unexpected device data type. Expected " + dataType + " but was: " + device.getDataType());
        }
        return (DeviceContext<D>) device;
    }
}
