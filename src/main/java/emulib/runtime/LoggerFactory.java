/*
 * LoggerFactory.java
 * 
 * Copyright (C) 2009-2013 Peter Jakubčo
 * KISS, YAGNI, DRY
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

import emulib.runtime.interfaces.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 * Logger factory which provides loggers for plug-ins.
 * 
 * It is preferred way for plug-ins to obtain a logger.
 * 
 * @author Peter Jakubčo
 */
public class LoggerFactory {
    private static Map<Long, Logger> loggers = new HashMap<Long, Logger>();
    
    /**
     * Get a logger for a plug-in.
     * 
     * Only one instance of logger exists for one plug-in ID. If the method is called more than once with different
     * identification class parameter, the class parameter is ignored.
     * 
     * @param pluginID ID of the plug-in
     * @param identificationClass Class acting as an identification who logged such an information.
     * @return Logger object for specified plug-in.
     */
    public static Logger getLogger(long pluginID, Class identificationClass) {
        if (!loggers.containsKey(pluginID)) {
            loggers.put(pluginID, new LoggerImpl(org.slf4j.LoggerFactory.getLogger(identificationClass)));
        }
        return loggers.get(pluginID);
    }

}
