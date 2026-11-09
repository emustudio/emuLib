/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.ui.components;

import net.jcip.annotations.NotThreadSafe;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Combo box model keeping the 10 most recently added items, most recent first.
 * <p>
 * The first added item becomes selected; later additions do not change the selection.
 *
 * @param <T> item type
 */
@NotThreadSafe
public class CachedComboBoxModel<T> extends AbstractListModel<T> implements ComboBoxModel<T> {
    private static final int LIMIT = 10;
    private final List<T> items = new ArrayList<>();
    private Object selected;

    /**
     * Adds an item to the front. An already present item is moved to the front; the oldest item is dropped
     * when the limit is exceeded.
     *
     * @param item item to add
     * @throws NullPointerException if the item is null
     */
    public void add(T item) {
        boolean known = items.remove(Objects.requireNonNull(item));
        items.add(0, item);
        if (items.size() > LIMIT) {
            items.remove(LIMIT);
        } else if (!known) {
            fireIntervalAdded(this, items.size() - 1, items.size() - 1);
        }
        if (selected == null) {
            setSelectedItem(item);
        }
        fireContentsChanged(this, 0, items.size() - 1);
    }

    @Override
    public void setSelectedItem(Object item) {
        if (!Objects.equals(selected, item)) {
            selected = item;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return index >= 0 && index < items.size() ? items.get(index) : null;
    }
}
