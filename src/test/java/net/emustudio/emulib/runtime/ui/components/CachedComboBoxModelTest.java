/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CachedComboBoxModelTest {
    private CachedComboBoxModel<String> model;

    @Before
    public void setUp() {
        model = new CachedComboBoxModel<>();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, model.getSize());
        assertNull(model.getSelectedItem());
    }

    @Test
    public void testAddSingleItem() {
        model.add("item1");
        assertEquals(1, model.getSize());
        assertEquals("item1", model.getElementAt(0));
        assertEquals("item1", model.getSelectedItem());
    }

    @Test
    public void testAddMultipleItems() {
        model.add("item1");
        model.add("item2");
        model.add("item3");

        assertEquals(3, model.getSize());
        assertEquals("item3", model.getElementAt(0)); // Most recent first
        assertEquals("item2", model.getElementAt(1));
        assertEquals("item1", model.getElementAt(2));
    }

    @Test
    public void testAddDuplicateItem() {
        model.add("item1");
        model.add("item2");
        model.add("item1"); // Re-adding item1

        assertEquals(2, model.getSize()); // Size should not increase
        assertEquals("item1", model.getElementAt(0)); // item1 should be first now
        assertEquals("item2", model.getElementAt(1));
    }

    @Test
    public void testCacheLimit() {
        // Add more than 10 items (the cache limit)
        for (int i = 0; i < 15; i++) {
            model.add("item" + i);
        }

        assertEquals(10, model.getSize()); // Should be limited to 10
        assertEquals("item14", model.getElementAt(0)); // Most recent
        assertEquals("item5", model.getElementAt(9)); // Oldest kept
    }

    @Test
    public void testSetSelectedItem() {
        model.add("item1");
        model.add("item2");

        model.setSelectedItem("item1");
        assertEquals("item1", model.getSelectedItem());

        model.setSelectedItem("item2");
        assertEquals("item2", model.getSelectedItem());
    }

    @Test
    public void testSetSelectedItemToNull() {
        model.add("item1");
        assertEquals("item1", model.getSelectedItem());

        model.setSelectedItem(null);
        assertNull(model.getSelectedItem());
    }

    @Test
    public void testSetSelectedItemNotInModel() {
        model.add("item1");
        model.setSelectedItem("item2"); // Not in model
        assertEquals("item2", model.getSelectedItem()); // Should still accept it
    }

    @Test
    public void testGetElementAtInvalidIndex() {
        model.add("item1");
        assertNull(model.getElementAt(10)); // Out of bounds
    }

    @Test
    public void testGetElementAtNegativeIndex() {
        model.add("item1");
        assertNull(model.getElementAt(-1));
    }

    @Test
    public void testFirstItemBecomesSelectedWhenAddedToEmptyModel() {
        assertNull(model.getSelectedItem());
        model.add("item1");
        assertEquals("item1", model.getSelectedItem());
    }

    @Test
    public void testSelectedItemDoesNotChangeWhenAddingToNonEmptyModel() {
        model.add("item1");
        model.setSelectedItem("item1");

        model.add("item2");
        assertEquals("item1", model.getSelectedItem()); // Should remain item1
    }

    @Test
    public void testOrderAfterReAddingItems() {
        model.add("A");
        model.add("B");
        model.add("C");

        // Re-add B, it should move to the front
        model.add("B");

        assertEquals(3, model.getSize());
        assertEquals("B", model.getElementAt(0));
        assertEquals("C", model.getElementAt(1));
        assertEquals("A", model.getElementAt(2));
    }

    @Test
    public void testIntegerModel() {
        CachedComboBoxModel<Integer> intModel = new CachedComboBoxModel<>();
        intModel.add(1);
        intModel.add(2);
        intModel.add(3);

        assertEquals(3, intModel.getSize());
        assertEquals(Integer.valueOf(3), intModel.getElementAt(0));
        assertEquals(Integer.valueOf(2), intModel.getElementAt(1));
        assertEquals(Integer.valueOf(1), intModel.getElementAt(2));
    }

    @Test
    public void testReAddingOldestItemWhenAtLimit() {
        // Fill cache to limit
        for (int i = 0; i < 10; i++) {
            model.add("item" + i);
        }

        // The oldest item should be "item0"
        // Re-add it to move it to front
        model.add("item0");

        assertEquals(10, model.getSize());
        assertEquals("item0", model.getElementAt(0)); // Should be at front now
    }

    @Test
    public void testSetSelectedItemSameAsCurrentDoesNotTrigger() {
        model.add("item1");
        model.setSelectedItem("item1");

        // Setting same item again - internally should not trigger change
        model.setSelectedItem("item1");
        assertEquals("item1", model.getSelectedItem());
    }
}
