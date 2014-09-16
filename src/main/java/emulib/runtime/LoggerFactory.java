/*
 * Copyright (C) 2009-2014 Peter Jakubčo
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
 * Logger factory which provides loggers for given classes.
 *
 * It is preferred way for plug-ins to obtain a logger.
 */
public class LoggerFactory {
    private static final Map<Class, Logger> loggers = new HashMap<>();

    private LoggerFactory() {}

    /**
     * Get a logger for a class.
     *
     * Only one instance of logger exists for one identification class.
     *
     * @param identificationClass Class acting as an identification who logged such an information.
     * @return Logger object for specified class.
     */
    public static Logger getLogger(Class identificationClass) {
        if (!loggers.containsKey(identificationClass)) {
            loggers.put(identificationClass, new LoggerImpl(org.slf4j.LoggerFactory.getLogger(identificationClass)));
        }
        return loggers.get(identificationClass);
    }

}
