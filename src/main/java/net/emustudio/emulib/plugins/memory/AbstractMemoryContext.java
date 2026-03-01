/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class implements some fundamental functionality of MemoryContext
 * interface, that can be useful in the programming of the own memory context.
 *
 * @param <CellType> the memory cell type
 */
@ThreadSafe
public abstract class AbstractMemoryContext<CellType> implements MemoryContext<CellType> {

    /**
     * Constructs a new AbstractMemoryContext.
     */
    protected AbstractMemoryContext() {
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractMemoryContext.class);

    private volatile boolean notificationsEnabled = true;

    @Override
    public boolean areMemoryNotificationsEnabled() {
        return notificationsEnabled;
    }

    @Override
    public void setMemoryNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    /**
     * List of all memory listeners. The listeners are objects implementing
     * the IMemoryListener interface. Methods within the listeners are called
     * on some events that happen inside memory (e.g. value change).
     */
    protected final Set<MemoryListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Adds a listener onto listeners list
     *
     * @param listener listener object
     */
    @Override
    public void addMemoryListener(MemoryListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the listener from listeners list
     *
     * @param listener listener object
     */
    @Override
    public void removeMemoryListener(MemoryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that memory content has changed at given positions.
     * <p>
     * This method should be called by a memory context, whenever a <code>write()</code> is invoked.
     *
     * @param fromLocation from location
     * @param toLocation   to location
     */
    public void notifyMemoryContentChanged(int fromLocation, int toLocation) {
        if (notificationsEnabled) {
            listeners.forEach(listener -> {
                try {
                    listener.memoryContentChanged(fromLocation, toLocation);
                } catch (Exception e) {
                    LOGGER.error("Memory listener error", e);
                }
            });
        }
    }

    /**
     * Notify all listeners that memory content has changed at given positions.
     * <p>
     * This method should be called by a memory context, whenever a <code>write()</code> is invoked.
     *
     * @param location memory location
     */
    public void notifyMemoryContentChanged(int location) {
        notifyMemoryContentChanged(location, location);
    }

    /**
     * Notify listeners that memory size has changed.
     */
    public void notifyMemorySizeChanged() {
        if (notificationsEnabled) {
            listeners.forEach(listener -> {
                try {
                    listener.memorySizeChanged();
                } catch (Exception e) {
                    LOGGER.error("Memory listener error", e);
                }
            });
        }
    }
}
