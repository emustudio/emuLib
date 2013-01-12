/*
 * Logger.java
 * 
 * Copyright (C) 2009-2012 Peter Jakubčo
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
package emulib.runtime.interfaces;

/**
 * Logger interface for plug-ins.
 * 
 * Any plug-in can obtain a logger by calling LoggerFactory.getLogger().
 * 
 * @author Peter Jakubčo
 */
public interface Logger {
    
    /**
     * Log informational message.
     * 
     * @param message message string
     */
    public void info(String message);
    
    /**
     * Log informational message with exception stacktrace.
     * 
     * @param message message string
     * @param throwable exception object
     */
    public void info(String message, Throwable throwable);
    
    /**
     * Log error message.
     * 
     * @param message message string
     */
    public void error(String message);
    
    /**
     * Log error message with exception stacktrace.
     * 
     * @param message message string
     * @param throwable exception object
     */
    public void error(String message, Throwable throwable);
    
    /**
     * Log warning message.
     * 
     * @param message message string
     */
    public void warning(String message);

    /**
     * Log warning message with exception stacktrace.
     * 
     * @param message message string
     * @param throwable exception object
     */
    public void warning(String message, Throwable throwable);

    /**
     * Log debug message.
     * 
     * If debug is not enabled, does nothing.
     * 
     * @param message message string
     */
    public void debug(String message);
    
    /**
     * Log debug message with exception stacktrace.
     * 
     * If debug is not enabled, does nothing.
     * 
     * @param message message string
     * @param throwable exception object
     */
    public void debug(String message, Throwable throwable);
    
}
