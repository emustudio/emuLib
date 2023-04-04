/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.runtime.interaction;

import javax.swing.*;
import java.util.Iterator;

/**
 * Cached ComboBox model. Saves up to 10 items
 */
@SuppressWarnings("unused")
public class CachedComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
    private final LimitedCache<T> cache = new LimitedCache<>(10);
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
