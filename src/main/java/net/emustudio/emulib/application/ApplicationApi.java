/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application;

import net.emustudio.emulib.application.ui.Dialogs;
import net.emustudio.emulib.application.annotations.MemoryAnnotations;
import net.emustudio.emulib.application.ui.GUI;
import net.jcip.annotations.ThreadSafe;

/**
 * emuStudio application API.
 * <p>
 * Plugins can use it for the communication with emuStudio.
 */
@SuppressWarnings("unused")
@ThreadSafe
public interface ApplicationApi {

    /** Returns the active computer's live annotations, or null when unavailable. */
    MemoryAnnotations getAnnotations();

    /**
     * Get context pool.
     * <p>
     * Context pool is used for registration and obtaining plugin contexts, which are used in plugin communication.
     *
     * @return context pool
     */
    ContextPool getContextPool();

    /**
     * Get dialogs object.
     * <p>
     * Dialogs object can be used by plugins to display common dialogs like information/error messages, or ask user
     * for input, like read integer/double value or ask for confirmation.
     *
     * @return dialogs object
     */
    Dialogs getDialogs();

    /**
     * Get GUI object.
     * <p>
     * GUI object can be used by plugins to create common Swing components like labels, buttons, panels, etc.
     * The implementation is provided by the host application.
     *
     * @return GUI object, or null if GUI is not available
     */
    GUI getGUI();

    /**
     * Sets program start location (usually a memory address).
     * <p>
     * This method is usually called by a compiler when it finishes compilation process, if it's clear at
     * which memory location the compiled program starts. The start location is then used by CPU, in a reset
     * operation - PC (program counter, or something similar) is usually set to this location.
     *
     * @param location program start location (usually a memory address)
     */
    void setProgramLocation(int location);

    /**
     * Gets program start location (usually a memory address).
     * <p>
     * This method is usually called by a CPU in a reset operation - PC (program counter, or something similar) is
     * usually set to this location.
     *
     * @return program's start address in memory
     */
    int getProgramLocation();

    /**
     * Unavailable API; all services are null. Useful for creating plugins outside emuStudio (e.g. tests, listing).
     */
    ApplicationApi UNAVAILABLE = new ApplicationApi() {
        // @formatter:off
        @Override public MemoryAnnotations getAnnotations() { return null; }
        @Override public ContextPool getContextPool() { return null; }
        @Override public Dialogs getDialogs() { return null; }
        @Override public GUI getGUI() { return null; }
        @Override public void setProgramLocation(int location) { }
        @Override public int getProgramLocation() { return 0; }
        // @formatter:on
    };
}
