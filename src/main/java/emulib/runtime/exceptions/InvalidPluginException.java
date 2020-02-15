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
package emulib.runtime.exceptions;

/**
 * This class represents an exception that can be raised during PluginLoader.loadPlugin method if a main plug-in class
 * does not meet requirements for Plug-in classes.
 *
 */
public class InvalidPluginException extends Exception {

    public InvalidPluginException(String cause) {
        super(cause);
    }

    public InvalidPluginException(String cause, Throwable e) {
        super(cause, e);
    }

}
