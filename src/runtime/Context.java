/*
 * Context.java
 *
 * (c) Copyright 2010, P. Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
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

package runtime;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import plugins.IContext;
import plugins.compiler.ICompilerContext;
import plugins.cpu.ICPUContext;
import plugins.device.IDeviceContext;
import plugins.memory.IMemoryContext;

/**
 * This class manages all contexts. Plug-ins should register their contexts
 * manually. This class also checks the correct hash of the context interfaces
 *
 * @author vbmacher
 */
public class Context {
    // the following tables store all registered contexts.
    // Contexts implementing the same context interfaces are stored
    // to the end of the arraylist under the same hashtable key
    private Hashtable<Class<IContext>,ArrayList<ICompilerContext>> compilerContexts;
    private Hashtable<Class<IContext>,ArrayList<ICPUContext>> cpuContexts;
    private Hashtable<Class<IContext>,ArrayList<IMemoryContext>> memContexts;
    private Hashtable<Class<IContext>,ArrayList<IDeviceContext>> deviceContexts;

    // This hashtable represents owners of registered contexts (keys).
    // It is used for checking the plug-in permissions to access them
    private Hashtable<Long,ArrayList<IContext>> contextOwners;

    // instance of this class
    private static Context instance = null;

    private static String emuStudioHash = null;

    /**
     * Private constructor.
     */
    private Context() {
        compilerContexts = new Hashtable<Class<IContext>,
                ArrayList<ICompilerContext>>();
        cpuContexts = new Hashtable<Class<IContext>, ArrayList<ICPUContext>>();
        memContexts = new Hashtable<Class<IContext>,
                ArrayList<IMemoryContext>>();
        deviceContexts = new Hashtable<Class<IContext>,
                ArrayList<IDeviceContext>>();

        contextOwners = new Hashtable<Long,ArrayList<IContext>>();
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
     */
    public boolean register(long pluginID, IContext context,
            Class<IContext> contextInterface) {
        // check if the context is class
        if (context.getClass().isInterface())
            return false;
        // check if the contextInterface is interface
        if (!contextInterface.getClass().isInterface())
            return false;

        // if the context is already registered, return false
        ArrayList tt = cpuContexts.get(contextInterface);
        if ((tt == null) || tt.contains(context))
            return false;
        tt = memContexts.get(contextInterface);
        if ((tt == null) || tt.contains(context))
            return false;
        tt = deviceContexts.get(contextInterface);
        if ((tt == null) || tt.contains(context))
            return false;
        tt = compilerContexts.get(contextInterface);
        if ((tt == null) || tt.contains(context))
            return false;

        // check if the contextInterface is implemented by context
        Class<?>[] intf = context.getClass().getInterfaces();
        boolean resultCheck = false;
        for (int j = 0; j < intf.length; j++) {
            if (intf[j] == contextInterface) {
                resultCheck = true;
                break;
            }
        }
        if (resultCheck == false)
            return false;

        // check hash of the interface
        String hash = null;
        String contextIName = contextInterface.getSimpleName();
        if (contextIName.equals(ICPUContext.class.getSimpleName())
                && (context instanceof ICPUContext))
            hash = computeHash(ICPUContext.class);
        else if (contextIName.equals(ICompilerContext.class.getSimpleName())
                && (context instanceof ICompilerContext))
            hash = computeHash(ICompilerContext.class);
        else if (contextIName.equals(IDeviceContext.class.getSimpleName())
                && (context instanceof IDeviceContext))
            hash = computeHash(IDeviceContext.class);
        else if (contextIName.equals(IMemoryContext.class.getSimpleName())
                && (context instanceof IMemoryContext))
            hash = computeHash(IMemoryContext.class);
        else {
            // extract hash from interface name
            hash = contextIName.substring(1);
        }
        if (checkHash(contextInterface, hash) == false)
            return false;

        // finally register context
        ArrayList<IContext> ar = contextOwners.get(pluginID);
        if (ar == null) {
            ar = new ArrayList<IContext>();
            contextOwners.put(pluginID, ar);
        }
        ar.add(context);

        if (context instanceof ICPUContext) {
            ArrayList<ICPUContext> arc = cpuContexts.get(contextInterface);
            if (arc == null) {
                arc = new ArrayList<ICPUContext>();
                cpuContexts.put(contextInterface, arc);
            }
            arc.add((ICPUContext)context);
        } else if (context instanceof ICompilerContext) {
            ArrayList<ICompilerContext> arc = compilerContexts.get(contextInterface);
            if (arc == null) {
                arc = new ArrayList<ICompilerContext>();
                compilerContexts.put(contextInterface, arc);
            }
            arc.add((ICompilerContext)context);
        } else if (context instanceof IDeviceContext) {
            ArrayList<IDeviceContext> arc = deviceContexts.get(contextInterface);
            if (arc == null) {
                arc = new ArrayList<IDeviceContext>();
                deviceContexts.put(contextInterface, arc);
            }
            arc.add((IDeviceContext)context);
        } else if (context instanceof IMemoryContext) {
            ArrayList<IMemoryContext> arc = memContexts.get(contextInterface);
            if (arc == null) {
                arc = new ArrayList<IMemoryContext>();
                memContexts.put(contextInterface, arc);
            }
            arc.add((IMemoryContext)context);
        } else {
            // This if branch IS needed
            return false;
        }
        return true;
    }

    /**
     * Method removes context from a context hashtable by value, not key.
     * It searches through the ArrayList for the value. If the ArrayList
     * is empty, removes also the key from the hashtable.
     *
     * @param t hashtable
     * @param context value that shall be removed
     * @return true if the value has been removed (and was found in the hashtable)
     */
    private boolean removeContext(Hashtable<Class<IContext>,?> t,
            IContext context) {
        if (t == null)
            return false;
        Enumeration<Class<IContext>> e = t.keys();
        while (e.hasMoreElements()) {
            Class<IContext> intf = e.nextElement();
            ArrayList<?> ar = (ArrayList<?>)t.get(intf);
            if (ar == null)
                continue;
            if (ar.contains(context)) {
                boolean b = ar.remove(context);
                if (ar.isEmpty())
                    t.remove(intf);
                return b;
            }
        }
        return false;
    }

    /**
     * Method removes all contexts from a context hashtable.
     * It removes also the key from the hashtable.
     *
     * @param t hashtable
     * @return true if all contexts were removed (and was found in the hashtable)
     */
    private boolean removeAllContexts(Hashtable<Class<IContext>,?> t,
            Class<IContext> contextInterface, ArrayList<IContext> owner) {
        if ((t == null) || (contextInterface == null))
            return false;

        ArrayList<?> ar = (ArrayList<?>)t.get(contextInterface);
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
            t.remove(contextInterface);
        return result;
    }

    /**
     * Method unregisters given context of given plug-in if the plug-in has
     * a permission to do it. The permission is approved if and only if
     * the context is implemented inside the requesting plug-in.
     *
     * @param pluginID ID of plug-in
     * @param context Context to unregister
     * @return true if the unregistration was successful, false otherwise
     */
    public boolean unregister(long pluginID, IContext context) {
        // check if the context is class
        if (context.getClass().isInterface())
            return false;

        // check for permission
        ArrayList<IContext> owner = contextOwners.get(pluginID);
        if (owner == null)
            return false;
        if (!owner.contains(context))
            return false;

        boolean result = false;

        result = removeContext(cpuContexts,context);
        result |= removeContext(memContexts,context);
        result |= removeContext(deviceContexts,context);
        result |= removeContext(compilerContexts,context);

        if (result == true)
            owner.remove(context);
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
    public boolean unregisterAll(long pluginID, Class<IContext> contextInterface) {
        // check if the context is class
        if (!contextInterface.getClass().isInterface())
            return false;

        // check for permission
        ArrayList<IContext> owner = contextOwners.get(pluginID);
        if (owner == null)
            return false;

        boolean result = false;

        result = removeAllContexts(cpuContexts, contextInterface, owner);
        result |= removeAllContexts(memContexts, contextInterface, owner);
        result |= removeAllContexts(compilerContexts, contextInterface, owner);
        result |= removeAllContexts(deviceContexts, contextInterface, owner);

        return result;
    }

    /**
     * Get registered CPU context of given plugin ID and context hash
     *
     * @param pluginID Plug-in identification number
     * @param hash Hash string of the plug-in
     * @return ICPUContext object if it is found, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID, String hash) {
        Hashtable<String, ICPUContext> t = cpuContexts.get(pluginID);
        if (t != null)
            return t.get(hash);
        return null;
    }

    /**
     * Get registered CPU context of given plugin ID and context hash
     *
     * @param pluginID Plug-in identification number
     * @param contextInterface Extended context interface
     * @return ICPUContext object if it is found, null otherwise
     */
    public ICPUContext getCPUContext(long pluginID, Class<?> contextInterface) {
        Hashtable<String, ICPUContext> t = cpuContexts.get(pluginID);
        if (t != null)
            return t.get(hash);
        return null;
    }

    /**
     * Get registered compiler context of given plugin ID and context hash
     *
     * @param pluginID Plug-in identification number
     * @param hash Hash string of the plug-in
     * @return ICompilerContext object if it is found, null otherwise
     */
    public ICompilerContext getCompilerContext(long pluginID, String hash) {
        Hashtable<String, ICompilerContext> t = compilerContexts.get(pluginID);
        if (t != null)
            return t.get(hash);
        return null;
    }

    /**
     * Get registered memory context of given plugin ID and context hash
     *
     * @param pluginID Plug-in identification number
     * @param hash Hash string of the plug-in
     * @return IMemoryContext object if it is found, null otherwise
     */
    public IMemoryContext getMemoryContext(long pluginID, String hash) {
        Hashtable<String, IMemoryContext> t = memContexts.get(pluginID);
        if (t != null)
            return t.get(hash);
        return null;
    }

    /**
     * Get registered device context of given plugin ID and context hash
     *
     * @param pluginID Plug-in identification number
     * @param hash Hash string of the plug-in
     * @return IDeviceContext object if it is found, null otherwise
     */
    public IDeviceContext getDeviceContext(long pluginID, String hash) {
        Hashtable<String, IDeviceContext> t = deviceContexts.get(pluginID);
        if (t != null)
            return t.get(hash);
        return null;
    }

    private static boolean checkHash(Class<?> contextInterface, String hash) {
        String computedHash = computeHash(contextInterface);
        if (computedHash == null)
            return false;
        return (computedHash.equals(hash.toUpperCase())) ? true : false;
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
     * @param hash emuStudio hash string, the "password".
     * @return true if the assignment was successfull (first call), false
     *         otherwise.
     */
    public static boolean assignEmuStudioHash(String hash) {
        if (emuStudioHash == null) {
            emuStudioHash = hash;
            return true;
        }
        return false;
    }

    /**
     * Compute hash of a plug-in context interface. Uses SHA-1 method.
     *
     * @param inter  Interface to computer hash of
     * @return SHA-1 hash string
     */
    private static String computeHash(Class<?> inter) {
        int i;
        Method[] methods;
        String hash = "";

        methods = inter.getMethods();
        for (i = 0; i < methods.length; i++) {
            hash += methods[i].toGenericString() + ";";
        }
        try {
            return SHA1(hash);
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Convert data to HEXadecimal string. Letters are in upper case.
     *
     * @param data data to convert
     * @return hexadecimal string.
     */
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('A' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String MD5(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5hash = md.digest();
        return convertToHex(md5hash);
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException,
            UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

}
