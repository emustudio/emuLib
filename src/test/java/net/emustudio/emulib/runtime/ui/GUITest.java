/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.ui.components.BrowseButton;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class GUITest {

    @Test
    public void testLabelCreation() {
        JLabel label = GUI.label("Test Text");

        assertNotNull(label);
        assertEquals("Test Text", label.getText());
    }

    @Test
    public void testBoldLabelCreation() {
        JLabel label = GUI.boldLabel("Bold Text");

        assertNotNull(label);
        assertEquals("Bold Text", label.getText());
        assertEquals(Font.BOLD, label.getFont().getStyle());
    }

    @Test
    public void testTitleLabelCreation() {
        JLabel label = GUI.titleLabel("Title Text");

        assertNotNull(label);
        assertEquals("Title Text", label.getText());
        assertEquals(Font.BOLD, label.getFont().getStyle());
        // Title should have larger font size
        assertTrue(label.getFont().getSize() > new JLabel().getFont().getSize());
    }

    @Test
    public void testToolbarToggleButtonWithItemAction() {
        boolean[] actionCalled = {false};
        boolean[] itemActionCalled = {false};

        var button = GUI.toolbarToggleButton(
            e -> actionCalled[0] = true,
            e -> itemActionCalled[0] = true,
            "/icon.png",
            "Test Tooltip"
        );

        assertNotNull(button);
        assertEquals("Test Tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
    }

    @Test
    public void testToolbarToggleButtonWithoutItemAction() {
        boolean[] actionCalled = {false};

        var button = GUI.toolbarToggleButton(
            e -> actionCalled[0] = true,
            "/icon.png",
            "Test Tooltip"
        );

        assertNotNull(button);
        assertEquals("Test Tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
    }

    @Test
    public void testTextFieldWithTextCreation() {
        JTextField field = GUI.textField("Initial Text", 15);

        assertNotNull(field);
        assertEquals("Initial Text", field.getText());
        assertEquals(15, field.getColumns());
    }

    @Test
    public void testTextFieldDefaultColumns() {
        JTextField field = GUI.textField("Text");

        assertNotNull(field);
        assertEquals("Text", field.getText());
        assertEquals(20, field.getColumns());
    }

    @Test
    public void testButtonCreation() {
        JButton button = GUI.button("Click Me");

        assertNotNull(button);
        assertEquals("Click Me", button.getText());
    }

    @Test
    public void testButtonWithActionCreation() {
        boolean[] actionCalled = {false};
        JButton button = GUI.button("Action Button", () -> actionCalled[0] = true);

        assertNotNull(button);
        assertEquals("Action Button", button.getText());

        button.doClick();
        assertTrue(actionCalled[0]);
    }

    @Test
    public void testButtonMakePrimary() {
        JButton button = new JButton("Test");

        GUI.buttonMakePrimary(button);

        assertEquals("borderless", button.getClientProperty("JButton.buttonType"));
        assertEquals(Font.BOLD, button.getFont().getStyle());
    }

    @Test
    public void testButtonBrowseDirectories() {
        Dialogs dialogs = createMock(Dialogs.class);

        expect(dialogs.chooseDirectory(
            eq("Choose Directory"),
            eq("Select"),
            anyObject(Path.class)
        )).andReturn(Optional.empty());

        replay(dialogs);

        BrowseButton button = GUI.buttonBrowseDirectories(dialogs, "Choose Directory", "Select", path -> {});

        assertNotNull(button);
        assertEquals("Browse...", button.getText());

        button.doClick();
        verify(dialogs);
    }

    @Test
    public void testScrollPaneCreation() {
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = GUI.scrollPane(textArea);

        assertNotNull(scrollPane);
        assertEquals(textArea, scrollPane.getViewport().getView());
    }

    @Test
    public void testPanelVerticalCreation() {
        JPanel panel = GUI.panelVertical();

        assertNotNull(panel);
        assertNotNull(panel.getLayout());
    }

    @Test
    public void testPanelHorizontalCreation() {
        JPanel panel = GUI.panelHorizontal();

        assertNotNull(panel);
        assertNotNull(panel.getLayout());
    }

    @Test
    public void testPanelButtonsCreation() {
        JPanel panel = GUI.panelButtons();

        assertNotNull(panel);
        assertNotNull(panel.getLayout());
    }

    @Test
    public void testToolBarCreation() {
        JToolBar toolbar = GUI.toolBar();

        assertNotNull(toolbar);
        assertFalse(toolbar.isFloatable());
        assertTrue(toolbar.isRollover());
        assertFalse(toolbar.isBorderPainted());
    }

    @Test
    public void testSplitPaneCreation() {
        JSplitPane splitPane = GUI.splitPane();

        assertNotNull(splitPane);
        assertNull(splitPane.getBorder());
        assertTrue(splitPane.isOneTouchExpandable());
        assertTrue(splitPane.isContinuousLayout());
    }

    @Test
    public void testStyleTable() {
        JTable table = new JTable();

        GUI.styleTable(table);

        // Just verify it doesn't throw an exception
        assertNotNull(table);
    }

    @Test
    public void testStyleList() {
        JList<String> list = new JList<>();

        GUI.styleList(list);

        // Just verify it doesn't throw an exception
        assertNotNull(list);
    }

    @Test
    public void testLoadIconReturnsNullForNonExistentResource() {
        Icon icon = GUI.loadIcon("/nonexistent/icon.png");

        assertNull(icon);
    }
}
