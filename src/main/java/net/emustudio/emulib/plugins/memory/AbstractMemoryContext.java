// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.memory.Memory.MemoryListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements some fundamental functionality of MemoryContext
 * interface, that can be useful in the programming of the own memory context.
 *
 * @param <Type> the memory cell type
 */
@ThreadSafe
public abstract class AbstractMemoryContext<Type> implements MemoryContext<Type> {
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
     * Notify all listeners that memory has changed.
     *
     * This method should be called whenever a some plugin writes to the
     * memory.
     *
     * @param position memory position (address) on which the value has changed
     */
    public void notifyMemoryChanged(int position) {
        if (notificationsEnabled) {
            listeners.forEach(listener -> {
                try {
                    listener.memoryChanged(position);
                } catch (Exception e) {
                    LOGGER.error("Memory listener error", e);
                }
            });
        }
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
