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
    // the following tables are of type  Long x String
    private Hashtable<Long,Hashtable<String,ICompilerContext>> compilerContexts;
    private Hashtable<Long,Hashtable<String,ICPUContext>> cpuContexts;
    private Hashtable<Long,Hashtable<String,IMemoryContext>> memContexts;
    private Hashtable<Long,Hashtable<String,IDeviceContext>> deviceContexts;

    private static Context instance = null;

    private Context() {
        compilerContexts = new Hashtable<Long, Hashtable<String,ICompilerContext>>();
        cpuContexts = new Hashtable<Long, Hashtable<String,ICPUContext>>();
        memContexts = new Hashtable<Long, Hashtable<String,IMemoryContext>>();
        deviceContexts = new Hashtable<Long, Hashtable<String,IDeviceContext>>();
    }

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
     * Requirements for the context:
     *   It is allowed (and required) to implement one and the only one
     *   interface of a plug-in context type (ie. one of the ICPUContext,
     *   IMemoryContext, ICompilerContext, or IDeviceContext)
     *
     * @param pluginID
     *        The plugin identification number
     * @param context
     *        The context that the plug-in want to register. It HAS TO
     *        be a class, not an interface.
     * @param contextInterface
     *        The interface that the context has to implement. This is the
     *        extended context. It HAS TO be an interface, not a class.
     * @return true if the registration is successful, false if it fails.
     */
    public boolean registerContext(long pluginID, IContext context,
            Class<?> contextInterface) {
        // check if the context is class
        if (context.getClass().isInterface())
            return false;
        // check if the contextInterface is interface
        if (!contextInterface.getClass().isInterface())
            return false;
        // if the context is already registered, return false
        Hashtable<String,?> t = cpuContexts.get(pluginID);
        if ((t == null) || (t.contains(context)))
            return false;
        t = memContexts.get(pluginID);
        if ((t == null) || (t.contains(context)))
            return false;
        t = deviceContexts.get(pluginID);
        if ((t == null) || (t.contains(context)))
            return false;
        t = compilerContexts.get(pluginID);
        if ((t == null) || (t.contains(context)))
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
        if (context instanceof ICPUContext) {
            Hashtable<String,ICPUContext> ctab = cpuContexts.get(pluginID);
            if (ctab == null) {
                ctab = new Hashtable<String,ICPUContext>();
                cpuContexts.put(pluginID, ctab);
            }
            ctab.put(hash, (ICPUContext)context);
        } else if (context instanceof ICompilerContext) {
            Hashtable<String,ICompilerContext> ctab = compilerContexts.get(pluginID);
            if (ctab == null) {
                ctab = new Hashtable<String,ICompilerContext>();
                compilerContexts.put(pluginID, ctab);
            }
            ctab.put(hash, (ICompilerContext)context);
        } else if (context instanceof IDeviceContext) {
            Hashtable<String,IDeviceContext> ctab = deviceContexts.get(pluginID);
            if (ctab == null) {
                ctab = new Hashtable<String,IDeviceContext>();
                deviceContexts.put(pluginID, ctab);
            }
            ctab.put(hash, (IDeviceContext)context);
        } else if (context instanceof IMemoryContext) {
            Hashtable<String,IMemoryContext> ctab = memContexts.get(pluginID);
            if (ctab == null) {
                ctab = new Hashtable<String,IMemoryContext>();
                memContexts.put(pluginID, ctab);
            }
            ctab.put(hash, (IMemoryContext)context);
        } else {
            // This check IS needed
            return false;
        }
        return true;
    }

    /**
     * Method removes value from a hashtable by value, not key.
     *
     * @param t hashtable
     * @param context value that shall be removed
     * @return true if the value has been removed (and was found in the hashtable)
     */
    private boolean removeValue(Hashtable<String,?> t, IContext context) {
        if (t == null)
            return false;
        Enumeration<String> e = t.keys();
        while (e.hasMoreElements()) {
            String hash = e.nextElement();
            if (t.get(hash) == context)
                return (t.remove(hash) == null) ? false: true;
        }
        return false;
    }

    /**
     * Method unregisters given context of given plug-in.
     *
     * @param pluginID ID if plugin
     * @param context Context to unregister
     * @return true if the unregistration was successful, false otherwise
     */
    public boolean unregisterContext(long pluginID, IContext context) {
        // check if the context is class
        if (context.getClass().isInterface())
            return false;

        boolean result = false;

        Hashtable<String,?> t = cpuContexts.get(pluginID);
        result = removeValue(t,context);

        t = memContexts.get(pluginID);
        result |= removeValue(t,context);

        t = deviceContexts.get(pluginID);
        result |= removeValue(t,context);

        t = compilerContexts.get(pluginID);
        result |= removeValue(t,context);

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
     * Compute hash of a plug-in context. Use SHA-1 method.
     *
     * @param inter  Interface to computer hash of
     * @return SHA-1 hash
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
