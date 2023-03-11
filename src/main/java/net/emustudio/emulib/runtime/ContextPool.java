/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.Plugin;
import net.emustudio.emulib.plugins.compiler.CompilerContext;
import net.emustudio.emulib.plugins.cpu.CPUContext;
import net.emustudio.emulib.plugins.device.DeviceContext;
import net.emustudio.emulib.plugins.memory.MemoryContext;

/**
 * Context pool manages contexts of all plugins.
 * <p>
 * Plugin context must be first registered by a plugin during its construction. Obtaining plugin contexts is reliable
 * only during plugin initialization, performed after the construction.
 * <p>
 * Context pool does not have to be thread safe.
 */
@SuppressWarnings("unused")
public interface ContextPool {

    /**
     * Register plugin context.
     * <p>
     * Method should be called during plugin construction, not later.
     * <p>
     * There are several requirements for plugin contexts, for more information {@link Context} documentation.
     * <p>
     * Context interface is used as "key" under which the context object will be registered. In later time, the context
     * can be obtained by this context interface "key". A plugin can register multiple context objects under the same
     * context interface. In that case, each context will have assigned an index, starting from 0, in order of
     * context registration.
     *
     * @param pluginID         owner plugin ID
     * @param context          The context object that the plugin want to register
     * @param contextInterface plugin context interface
     * @throws ContextAlreadyRegisteredException Raised when given plugin context is already registered.
     * @throws InvalidContextException           Raised when given plugin context and its interface does not fulfill
     *                                           the requirements.
     */
    void register(long pluginID, Context context, Class<? extends Context> contextInterface)
        throws ContextAlreadyRegisteredException, InvalidContextException;

    /**
     * Unregisters all contexts registered under given context interface.
     * <p>
     * It will do it only if the plugin has the permission. The permission is approved if and only if the contexts are
     * implemented inside the plugin.
     *
     * @param pluginID         owner plugin ID
     * @param contextInterface context interface class
     * @return true if all plugin contexts registered under provided context interface were unregistered successfully,
     * false otherwise.
     * @throws InvalidContextException Raised when given plugin context interface does not fulfill the requirements.
     */
    boolean unregister(long pluginID, Class<? extends Context> contextInterface) throws InvalidContextException;

    /**
     * Get registered plugin context.
     * <p>
     * NOTE: it can be reliably called in {@link Plugin#initialize()}.
     *
     * @param <T>              Specific context type
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @param index            0-based index of the context object.
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <T extends Context> T getContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered CPU context.
     * <p>
     * If the plugin has registered more than one {@link CPUContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param pluginID plugin ID
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    CPUContext getCPUContext(long pluginID) throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered CPU context.
     * <p>
     * If the plugin has registered more than one {@link CPUContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <T>              Specific CPU context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <T extends CPUContext> T getCPUContext(long pluginID, Class<T> contextInterface) throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered CPU context.
     * <p>
     * If the plugin has registered more than one {@link CPUContext}, the index specifies the one in order as they were
     * registered. This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <T>              Specific CPU Context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @param index            0-based index of the context object.
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <T extends CPUContext> T getCPUContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered Compiler context.
     * <p>
     * If the plugin has registered more than one {@link CompilerContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param pluginID plugin ID
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    CompilerContext getCompilerContext(long pluginID) throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered Compiler context.
     * <p>
     * If the plugin has registered more than one {@link CompilerContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <T>              Specific compiler context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <T extends CompilerContext> T getCompilerContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered compiler context.
     * <p>
     * If the plugin has registered more than one {@link CompilerContext}, the index specifies the one in order as they were
     * registered. This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <T>              Specific compiler context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @param index            0-based index of the context object.
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <T extends CompilerContext> T getCompilerContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered Memory context.
     * <p>
     * If the plugin has registered more than one {@link MemoryContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <CellType>       Cell type
     * @param <T>              Specific memory context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <CellType, T extends MemoryContext<CellType>> T getMemoryContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered memory context.
     * <p>
     * If the plugin has registered more than one {@link MemoryContext}, the index specifies the one in order as they were
     * registered. This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <CellType>       Cell type
     * @param <T>              Specific memory context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @param index            0-based index of the context object.
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <CellType, T extends MemoryContext<CellType>> T getMemoryContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered Device context.
     * <p>
     * If the plugin has registered more than one {@link DeviceContext}, the first one is returned.
     * This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <DataType>       Data type
     * @param <T>              Specific device context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <DataType, T extends DeviceContext<DataType>> T getDeviceContext(long pluginID, Class<T> contextInterface)
        throws InvalidContextException, ContextNotFoundException;

    /**
     * Get registered Device context.
     * <p>
     * If the plugin has registered more than one {@link DeviceContext}, the index specifies the one in order as they were
     * registered. This method can be reliably called in the implementation of {@link Plugin#initialize()} method.
     *
     * @param <DataType>       Data type
     * @param <T>              Specific device context
     * @param pluginID         plugin ID
     * @param contextInterface context interface class
     * @param index            0-based index of the context object.
     * @return plugin context object
     * @throws InvalidContextException  Raised when given plugin context and its interface does not fulfill
     *                                  the requirements.
     * @throws ContextNotFoundException Raised when requested plugin context does not exist or if the plugin is not
     *                                  allowed to get it
     */
    <DataType, T extends DeviceContext<DataType>> T getDeviceContext(long pluginID, Class<T> contextInterface, int index)
        throws InvalidContextException, ContextNotFoundException;
}
