/*
 * Context.java
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

import emulib.plugins.IContext;
import emulib.plugins.compiler.ICompilerContext;
import emulib.plugins.cpu.ICPUContext;
import emulib.plugins.device.IDeviceContext;
import emulib.plugins.memory.IMemoryContext;
import emulib.runtime.interfaces.IConnections;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages all contexts. Plug-ins should register their contexts
 * manually. This class also checks the correct hash of the context interfaces
 *
 * @author vbmacher
 */
public class Context {
    private final static Logger logger = LoggerFactory.getLogger(Context.class);
    /**
     * The following map store all registered contexts.
     * Contexts implementing the same context interfaces are stored
     * to the end of the arraylist under the same map key
     */
    private Map<Class<?>,List<IContext>> contexts;

    // This map represents owners of registered contexts (keys).
    // It is used for checking the plug-in permissions to access them
    private Map<Long,List<IContext>> contextOwners;

    // instance of this class
    private static Context instance = null;

    // emuStudio communication
    private static String emuStudioPassword = null;
    private IConnections computer;
    
    /**
     * Debug table updater object
     */
    private IDebugTable debug;

    /**
     * Private constructor.
     */
    private Context() {
        contexts = new HashMap<Class<?>,
                List<IContext>>();
        contextOwners = new HashMap<Long,List<IContext>>();
        computer = null;
    }
    
    /**
     * Method determines if given password matches with password already set-up by emuStudio.
     * 
     * @param password The password
     * @return true if passwords match, false otherwise
     */
    public boolean testPassword(String password) {
        if ((password == null) || (emuStudioPassword == null)) {
            return false;
        }
        return password.equals(emuStudioPassword);
    }
    
    /**
     * Return an instance of this class. By calling more than 1 time, the same
     * instance is returned.
     *
     * @return Context instance
     */
    public static Context getInstance() {
        if (instance == null)
            instance = new Context();
        return instance;
    }

    /**
     * This method registers context interface implemented by plug-in.
     * The registration is needed of two reasons:
     *  - for connections realized by emuStudio
     *  - for check of correctness of the context hash
     *
     * If the plug-in asks for a context, it has to know the hash of it.
     *
     * Requirements for the context:
     *   It is allowed (and required) to implement one and the only one
     *   interface of a plug-in context type (ie. one of the ICPUContext,
     *   IMemoryContext, ICompilerContext, or IDeviceContext)
     *
     * @param pluginID
     *        ID of the plugin
     * @param context
     *        The context that the plug-in want to register. It HAS TO
     *        be a class, not an interface.
     * @param contextInterface
     *        The interface that the context has to implement. This is the
     *        extended context. It HAS TO be an interface, not a class.
     * @return true if the registration is successful, false if it fails.
     * @throws AlreadyRegisteredException Raised when a plug-in tries to register context that is already
     *         registered.
     * @throws InvalidImplementationException Raised when a class does not implement given interface.
     * @throws InvalidHashException Raised when context's methods does not match to computed hash.
     */
    public boolean register(long pluginID, IContext context,
            Class<?> contextInterface) throws AlreadyRegisteredException, InvalidImplementationException,
            InvalidHashException {

        // check if the context is class
        if (context.getClass().isInterface()) {
            throw new InvalidImplementationException();
        }
        // check if the contextInterface is interface
        if (!contextInterface.isInterface()) {
            throw new InvalidImplementationException();
        }

        // if the context is already registered, return false
        List conList = contexts.get(contextInterface);
        if ((conList != null) && conList.contains(context)) {
            throw new AlreadyRegisteredException();
        }

        // check if the contextInterface is implemented by context
        PluginLoader pLoader = PluginLoader.getInstance(emuStudioPassword);
        if (pLoader == null) {
            // emuStudio did not assign password??
            return false;
        }
        if (!pLoader.doesImplement(context.getClass(), contextInterface)) {
            throw new InvalidImplementationException();
        }
        if (!pLoader.doesImplement(contextInterface, IContext.class)) {
            throw new InvalidImplementationException();
        }
        
        // check hash of the interface
        String hash;
        String contextIName = contextInterface.getSimpleName();

        if (contextIName.equals(ICPUContext.class.getSimpleName())
                && (context instanceof ICPUContext))
            hash = computeHash(emuStudioPassword, ICPUContext.class);
        else if (contextIName.equals(ICompilerContext.class.getSimpleName())
                && (context instanceof ICompilerContext))
            hash = computeHash(emuStudioPassword, ICompilerContext.class);
        else if (contextIName.equals(IDeviceContext.class.getSimpleName())
                && (context instanceof IDeviceContext))
            hash = computeHash(emuStudioPassword, IDeviceContext.class);
        else if (contextIName.equals(IMemoryContext.class.getSimpleName())
                && (context instanceof IMemoryContext))
            hash = computeHash(emuStudioPassword, IMemoryContext.class);
        else {
            // extract hash from interface name
            hash = contextIName.substring(1);
        }
        if (checkHash(contextInterface, hash) == false) {
            throw new InvalidHashException();
        }

        // finally register context
        List<IContext> ar = contextOwners.get(pluginID);
        if (ar == null) {
            ar = new ArrayList<IContext>();
            contextOwners.put(pluginID, ar);
        }
        ar.add(context);

        List<IContext> arc = contexts.get(contextInterface);
        if (arc == null) {
            arc = new ArrayList<IContext>();
            contexts.put(contextInterface, arc);
        }
        arc.add((IContext)context);
        return true;
    }

    /**
     * Method removes all contexts from a context map.
     * It removes also the key from the map.
     *
     * @return true if all contexts were removed (and was found in the map)
     */
    private boolean removeAllContexts(Class<?> contextInterface, List<IContext> owner) {
        if (contextInterface == null)
            return false;

        List<?> ar = (List<?>)contexts.get(contextInterface);
        if (ar == null)
            return false;

        boolean result = false;
        for (int i = ar.size()-1; i >= 0; i--) {
            IContext context = (IContext)ar.get(i);
            if (owner.contains(context)) {
                result |= ar.remove(context);
                owner.remove(context);
            }
        }
        if (ar.isEmpty())
            contexts.remove(contextInterface);
        return result;
    }

    /**
     * This method unregisters all contexts that implements given interface,
     * if the plug-in has permission for it. The permission is approved if and
     * only if the contexts are implemented inside the plug-in.
     * 
     * @param pluginID ID of the plug-in
     * @param contextInterface Interface that should be unregistered
     * @return true if almost one context has been unregistered, false instead.
     */
    public boolean unregister(long pluginID, Class<?> contextInterface) {
        // check if the context is class
        if (!contextInterface.isInterface()) {
            return false;
        }

        // check for permission
        List<IContext> owner = contextOwners.get(pluginID);
        if (owner == null) {
            return false;
        }
        return removeAllContexts(contextInterface, owner);
    }

    /**
     * This method should be called by the emuStudio. If the password is
     * not correct, it does nothing. It should be called after assignPassword()
     * method.
     *
     * @param password
     * @param computer
     * @return true if computer was assigned successfully; false otherwise. If it returns false, it can mean either
     * the password is incorrect or null.
     */
    public boolean assignComputer(String password, IConnections computer) {
        if (!testPassword(password)) {
            return false;
        }
        this.computer = computer;
        return true;
    }

    /**
     * Get specific context for given input data and map of specific
     * plug-in type.
     *
     * This method is used in all get...Context() methods.
     *
     * @param pluginID ID of requesting plug-in
     * @param contextInferfaceMain wanted context interface (main). E.g.: ICPUContext, IMemoryContext, ...
     * @param contextInterface wanted context interface (implemented by the plug-in)
     * @param contextID specific context ID (if is not required, should be null)
     * @param index the index if more than one contexts are found
     * @return requested context or null, if the plug-in is not allowed to
     * get the context
     */
    private IContext getContext(long pluginID, Class<?> contextInferfaceMain,
            Class<?> contextInterface, String contextID, int index) {
        // find the requested context
        List ar = (List)contexts.get(contextInterface);
        if ((ar == null) || ar.isEmpty())
            return null;
        
        PluginLoader pLoader = PluginLoader.getInstance(emuStudioPassword);
        if (pLoader == null) {
            logger.error("Could not get PluginLoader instance. emuStudio probably didn't assign password.");
            // emuStudio did not assign password??
            return null;
        }

        // find context based on contextID
        IContext context;
        for (int i = 0, j = 0; i < ar.size(); i++) {
            context = (IContext)ar.get(i);
            if (!pLoader.doesImplement(context.getClass(), contextInferfaceMain)) {
                continue;
            }
            if ((contextID != null) && !context.getID().equals(contextID)) {
                continue;
            }
            if (checkPermission(pluginID, context)) {
                if (j == index) {
                    return context;
                } else {
                    j++;
                }
            }
        }
        return null;
    }

    /**
     * Get specific context for given input data and map of specific
     * plug-in type.
     *
     * This method is used in all get...Context() methods.
     *
     * @param pluginID ID of requesting plug-in
     * @param contextInterface wanted context interface (implemented by the plug-in)
     * @param contextID specific context ID (if is not required, should be null)
     * @param index the index if more than one contexts are found
     * @return requested context or null, if the plug-in is not allowed to
     * get the context
     */
    public IContext getContext(long pluginID, Class<?> contextInterface, String contextID, int index) {
        // find the requested context
        List ar = (List)contexts.get(contextInterface);
        if ((ar == null) || ar.isEmpty())
            return null;
        
        PluginLoader pLoader = PluginLoader.getInstance(emuStudioPassword);
        if (pLoader == null) {
            logger.error("Could not get PluginLoader instance. emuStudio probably didn't assign password.");
            // emuStudio did not assign password??
            return null;
        }

        // find context based on contextID
        IContext context;
        for (int i = 0, j = 0; i < ar.size(); i++) {
            context = (IContext)ar.get(i);
            if ((contextID != null) && !context.getID().equals(contextID)) {
                continue;
            }
            if (checkPermission(pluginID, context)) {
                if (j == index) {
                    return context;
                } else {
                    j++;
                }
            }
        }
        return null;
    }
    
    /**
     * Get registered CPU context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to CPU in
     * the abstract schema.
     *
     * If CPU has more than one context implementing required context interface,
     * the first one is returned that is allowed of access.
     * For specific context, use method
     * getCPUContext(pluginID,contextInterface,contextID).
     *
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @return ICPUContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID,
            Class<?> contextInterface) {
        return getCPUContext(pluginID, contextInterface, 0);
    }

    /**
     * Get registered CPU context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to CPU in
     * the abstract schema.
     *
     * If CPU has more than one context implementing required context interface,
     * the one is returned that has the order of the index parameter. For
     * specific context, use method
     * getCPUContext(pluginID,contextInterface,contextID,index).
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all CPU contexts.
     *
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @param index 0-based the order of the context if they are more than one.
     * Does nothing if the index is out of the bounds.
     * @return ICPUContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID,
            Class<?> contextInterface, int index) {
        return (ICPUContext)getContext(pluginID, ICPUContext.class,
                contextInterface, null, index);
    }

    /**
     * Get registered CPU context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to CPU in the abstract schema.
     *
     * This method should be used when requested CPU has more than one
     * context implementing the same interface.
     *
     * If there exist more than one interfaces that has the same ID, the first
     * of them is returned that is allowed of access.
     * 
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @return ICPUContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID,
            Class<?> contextInterface, String contextID) {
        return getCPUContext(pluginID, contextInterface, contextID, 0);
    }

    /**
     * Get registered CPU context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to CPU in the abstract schema.
     *
     * This method should be used when requested CPU has more than one
     * context implementing the same interface.
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all CPU contexts.
     *
     * @param pluginID plug-in requesting the CPU context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @param index 0-based the order of the context if they are more than one
     * with the same ID. Does nothing if the index is out of bounds.
     * @return ICPUContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID,
            Class<?> contextInterface, String contextID, int index) {
        return (ICPUContext)getContext(pluginID, ICPUContext.class,
                contextInterface, contextID, index);
    }

    /**
     * Get registered Compiler context.
     *
     * If the compiler has more than one context implementing required context
     * interface, the first one is returned that is allowed of access.
     * For specific context, use method
     * getCompilerContext(pluginID,contextInterface,contextID).
     *
     * @param pluginID plug-in requesting the Compiler context
     * @param contextInterface Interface of the context, if requesting plugin
     * has permission to acccess it
     * @return ICompilerContext object if it is found, null otherwise
     */
    public ICompilerContext getCompilerContext(long pluginID,
            Class<?> contextInterface) {
        return getCompilerContext(pluginID, contextInterface, 0);
    }

    /**
     * Get registered Compiler context.
     *
     * If the compiler has more than one context implementing required context
     * interface, the one is returned that has the order given by the index
     * parameter.
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all compiler contexts.
     *
     * For specific context, use method
     * getCompilerContext(pluginID,contextInterface,contextID,index).
     *
     * @param pluginID plug-in requesting the Compiler context
     * @param contextInterface Interface of the context
     * @param index the order of the context if they are more than one. Does
     * nothing if the index is out of bounds.
     * @return ICompilerContext object if it is found, null otherwise
     */
    public ICompilerContext getCompilerContext(long pluginID,
            Class<?> contextInterface, int index) {
        return (ICompilerContext)getContext(pluginID, ICompilerContext.class,
                contextInterface, null, index);
    }

    /**
     * Get registered Compiler context, with specific ID.
     *
     * This method should be used when requested compiler has more than one
     * context implementing the same interface.
     *
     * If there exist more than one interfaces that has the same ID, the first
     * of them is returned that is allowed of access.
     * 
     * @param pluginID plug-in requesting the Compiler context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @return ICompilerContext object if it is found, null otherwise
     */
    public ICompilerContext getCompilerContext(long pluginID,
            Class<?> contextInterface, String contextID) {
        return getCompilerContext(pluginID, contextInterface, contextID, 0);
    }

    /**
     * Get registered Compiler context, with specific ID.
     *
     * This method should be used when requested compiler has more than one
     * context implementing the same interface.
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all compiler contexts.
     *
     * @param pluginID plug-in requesting the Compiler context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @param index the order of the context if they are more than one with the
     * same ID. Does nothing if the index is out of bounds.
     * @return ICompilerContext object if it is found, null otherwise
     */
    public ICompilerContext getCompilerContext(long pluginID,
            Class<?> contextInterface, String contextID, int index) {
        return (ICompilerContext)getContext(pluginID, ICompilerContext.class,
                contextInterface, contextID, index);
    }

    /**
     * Get registered memory context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to memory in
     * the abstract schema.
     *
     * If the memory has more than one context implementing required context
     * interface, the first one is returned that is allowed of access.
     * For specific context, use method
     * getMemoryContext(pluginID,contextInterface,contextID).
     *
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @return IMemoryContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IMemoryContext getMemoryContext(long pluginID,
            Class<?> contextInterface) {
        return getMemoryContext(pluginID, contextInterface, 0);
    }

    /**
     * Get registered memory context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to memory in
     * the abstract schema.
     *
     * If the memory has more than one context implementing required context
     * interface, the one is returned that has the order given by the index
     * parameter. For specific context, use method
     * getMemoryContext(pluginID,contextInterface,contextID,index).
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all memory contexts.
     *
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @param index the index of the context if they are more than one. Does
     * nothing if the index is out of bounds
     * @return IMemoryContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IMemoryContext getMemoryContext(long pluginID,
            Class<?> contextInterface, int index) {
        return (IMemoryContext)getContext(pluginID, IMemoryContext.class,
                contextInterface, null, index);
    }

    /**
     * Get registered memory context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to memory in the abstract schema.
     *
     * This method should be used when requested memory has more than one
     * context implementing the same interface.
     *
     * If there exist more than one interfaces that has the same ID, the first
     * of them is returned that is allowed of access.
     * 
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @return IMemoryContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IMemoryContext getMemoryContext(long pluginID,
            Class<?> contextInterface, String contextID) {
        return getMemoryContext(pluginID, contextInterface, contextID, 0);
    }

    /**
     * Get registered memory context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to memory in the abstract schema.
     *
     * This method should be used when requested memory has more than one
     * context implementing the same interface.
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all memory contexts.
     * 
     * @param pluginID plug-in requesting the memory context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @param index the order of the specific context, if they are more than one
     * with the same ID. Does nothing if the index is out of bounds.
     * @return IMemoryContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IMemoryContext getMemoryContext(long pluginID,
            Class<?> contextInterface, String contextID, int index) {
        return (IMemoryContext)getContext(pluginID, IMemoryContext.class,
                contextInterface, contextID, index);
    }

    /**
     * Get registered device context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to device in
     * the abstract schema.
     *
     * If the device has more than one context implementing required context
     * interface, the first one is returned that allows the access.
     * For specific context, use method
     * getDeviceContext(pluginID,contextInterface,contextID).
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @return IDeviceContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IDeviceContext getDeviceContext(long pluginID,
            Class<?> contextInterface) {
        return getDeviceContext(pluginID, contextInterface, 0);
    }

    /**
     * Get registered device context, if plug-in has the permission to access it.
     * The permission is approved, if the plug-in is connected to device in
     * the abstract schema.
     *
     * If the device has more than one context implementing required context
     * interface, the user can select the correct with the index parameter.
     * For specific context, use method
     * getDeviceContext(pluginID,contextInterface,contextID,index).
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all device contexts.
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @param index index of the context implementation. Does nothing if the
     * index is out of bounds.
     * @return IDeviceContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IDeviceContext getDeviceContext(long pluginID,
            Class<?> contextInterface, int index) {
        return (IDeviceContext)getContext(pluginID, IDeviceContext.class,
                contextInterface, null, index);
    }

    /**
     * Get registered device context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to device in the abstract schema.
     *
     * This method should be used when requested device has more than one
     * context implementing the same interface.
     *
     * If there exist more than one interfaces that has the same ID, the first
     * of them is returned that is allowed of access.
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @return IDeviceContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IDeviceContext getDeviceContext(long pluginID,
            Class<?> contextInterface, String contextID) {
        return getDeviceContext(pluginID, contextInterface, contextID, 0);
    }

    /**
     * Get registered device context with the specific ID, if plug-in has the
     * permission to access it. The permission is approved, if the plug-in
     * is connected to device in the abstract schema.
     *
     * This method should be used when requested device has more than one
     * context implementing the same interface.
     *
     * If there are more contextes and there is some that implement required
     * interface and some not, the index parameter is considering only wanted
     * contexts, ignoring the others. So it is not true index to array of
     * all device contexts.
     *
     * @param pluginID plug-in requesting the device context
     * @param contextInterface Interface of the context
     * @param contextID specific case-sensitive ID of context
     * @param index the order of the specific context if they are more than one
     * with the same ID. Does nothing if the index is out of bounds.
     * @return IDeviceContext object if it is found and the plug-in has the
     *         permission, null otherwise
     */
    public IDeviceContext getDeviceContext(long pluginID,
            Class<?> contextInterface, String contextID, int index) {
        return (IDeviceContext)getContext(pluginID, IDeviceContext.class,
                contextInterface, contextID, index);
    }

    /**
     * This method checks if the hash of the contextInterface equals given
     * hash string. The contextInterface hash is computed.
     *
     * @param contextInterface context interface for checking its hash
     * @param hash comparison hash
     * @return true if the hashes match in case-isensitive manner,
     *         false otherwise
     */
    private boolean checkHash(Class<?> contextInterface, String hash) {
        String computedHash = computeHash(emuStudioPassword, contextInterface);
        if (computedHash == null)
            return false;
        return (computedHash.equalsIgnoreCase(hash)) ? true : false;
    }

    /**
     * This method check the plug-in for the permission to access specified
     * context.
     *
     * The permission is granted if and only if context owner is connected
     * with the requesting plug-in in the abstract schema.
     *
     * @param pluginID ID of requesting plug-in
     * @param context Context wanted
     * @return true if the plug-in is approved to access the context, false
     *         otherwise
     */
    private boolean checkPermission(long pluginID, IContext context) {
        // check if it is possible to check the plug-in for the permission
        if (computer == null)
            return false;

        // first it must be found the owner of the Context.
        Long owner = null;
        Iterator<Long> t = contextOwners.keySet().iterator();

        while (t.hasNext()) {
            long pID = t.next();
            List<IContext> con = contextOwners.get(pID);
            if (con == null)
                continue;
            if (con.contains(context)) {
                owner = pID;
                break;
            }
        }
        // contex was not found in owners? This would be emuLib BUG!!
        if (owner == null)
            return false;

        // THIS is the permission check
        return computer.isConnected(pluginID, owner);
    }

    /**
     * Assigns a hash to the emuStudioHash variable. This hash represents
     * "password" by which the emuStudio is allowed to perform critical operations
     * in the emuLib. The operations must be strictly proteted from plug-ins.
     * They include e.g. providing information about plug-in connections.
     *
     * This method is called only once, by the emuStudio. After each next call,
     * it does nothing and returns false.
     *
     * @param password emuStudio hash string, the "password".
     * @return true if the assignment was successfull (first call), false
     *         otherwise.
     */
    public static boolean assignPassword(String password) {
        if (emuStudioPassword == null) {
            emuStudioPassword = password;
            return true;
        }
        return false;
    }

    /**
     * Compute hash of a plug-in context interface. Uses SHA-1 method.
     *
     * @param password emuStudio hash string, the "password".
     * @param inter  Interface to computer hash of
     * @return SHA-1 hash string
     */
    public String computeHash(String password, Class<?> inter) {
        int i;
        Method[] methods, met;
        String hash = "";

        met = inter.getDeclaredMethods();
        List me = new ArrayList();
        for (i = 0; i < met.length; i++)
            me.add(met[i]);
        Collections.sort(me, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Method m1 = (Method)o1;
                Method m2 = (Method)o2;

                return m1.getName().compareTo(m2.getName());
            }

        });
        methods = (Method[])me.toArray(new Method[0]);
        me.clear();

        for (i = 0; i < methods.length; i++) {
            hash += methods[i].getGenericReturnType().toString() + " ";
            hash += methods[i].getName() + "(";
            Class<?>[] params = methods[i].getParameterTypes();
            for (int j = 0; j < params.length; j++)
                hash += params[j].getName() + ",";
            hash += ");";
        }
        try {
            return SHA1(hash);
        } catch(Exception e) {
            logger.error("Could not compute hash.");
            return null;
        }
    }

    /**
     * Compute SHA-1 hash string. Letters in the hash string are in upper-case.
     *
     * @param text Data to make hash from
     * @return SHA-1 hash Hexadecimal string, null if some exception has been
     * catched
     */
    public static String SHA1(String text) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash;
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            sha1hash = md.digest();
            return RadixUtils.convertToRadix(sha1hash, 16, false);
        } catch (NoSuchAlgorithmException e) {}
        catch (UnsupportedEncodingException r) {}
        return null;
    }

    /**
     * Set object with method of updating debug table in emuStudio. It should
     * be called once by emuStudio.
     * 
     * @param debug The IDebugTable object
     * @param password password that was assigned to the emuLib. It prevents
     * from misuse of this method by other plugins.
     */
    public void setDebugTableInterfaceObject(IDebugTable debug, String password) {
        if ((emuStudioPassword == null) || (!emuStudioPassword.equals(password)))
            return;
        this.debug = debug;
    }
    
    /**
     * Update debug table in emuStudio. If IDebugTableObject was not set,
     * then it does nothing.
     */
    public void updateDebugTable() {
        if (debug == null) {
            return;
        }
        debug.updateDebugTable();
    }
}
