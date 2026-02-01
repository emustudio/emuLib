/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import net.emustudio.emulib.runtime.ui.MaxItemsCache;

import javax.swing.*;
import java.util.Iterator;

/**
 * Cached ComboBox model. Saves up to 10 items
 */
@SuppressWarnings("unused")
public class CachedComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
    private final MaxItemsCache<T> cache = new MaxItemsCache<>(10);
    private Object selected;

    public void add(T item) {
        int origSize = cache.getSize();
        cache.put(item);
        if (origSize < cache.getSize()) {
            fireIntervalAdded(this, cache.getSize() - 1, cache.getSize() -1);
        }
        fireContentsChanged(this, 0, cache.getSize() - 1);
        if (selected == null && cache.getSize() > 0) {
            cache.first().ifPresent(f -> selected = f);
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if ((selected != null && !selected.equals(item)) || selected == null && item != null) {
            this.selected = item;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return cache.getSize();
    }

    @Override
    public T getElementAt(int index) {
        int i = 0;
        Iterator<T> it = cache.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (i++ == index) {
                return item;
            }
        }
        return null;
    }
}
