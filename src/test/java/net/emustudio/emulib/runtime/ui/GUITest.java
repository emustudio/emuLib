/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.ui.components.BrowseButton;
import net.miginfocom.swing.MigLayout;
import org.junit.Test;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
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
        JLabel label = GUI.labelBold("Bold Text");

        assertNotNull(label);
        assertEquals("Bold Text", label.getText());
        assertEquals(Font.BOLD, label.getFont().getStyle());
    }

    @Test
    public void testLabelTitleCreation() {
        JLabel label = GUI.labelTitle("Title Text");

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
        // Simulate button click to test action - doClick() on JToggleButton triggers both
        // the action listener and the item listener (since toggle state changes)
        button.doClick();
        assertTrue(actionCalled[0]);
        assertTrue(itemActionCalled[0]);
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
        button.doClick();
        assertTrue(actionCalled[0]);
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

        assertTrue(button.isDefaultCapable());
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

    // --- toolbarButton ---

    @Test
    public void testToolbarButtonWithConsumerAction() {
        boolean[] actionCalled = {false};

        var button = GUI.toolbarButton(
            e -> actionCalled[0] = true,
            "/icon.png",
            "Tooltip"
        );

        assertNotNull(button);
        assertEquals("Tooltip", button.getToolTipText());
        assertFalse(button.isFocusable());
        button.doClick();
        assertTrue(actionCalled[0]);
    }

    @Test
    public void testToolbarButtonWithSwingAction() {
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };

        var button = GUI.toolbarButton(action);

        assertNotNull(button);
    }

    @Test
    public void testToolbarButtonWithSwingActionAndIcon() {
        Action action = new AbstractAction("Test") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };

        var button = GUI.toolbarButton(action, "/icon.png", "Tooltip");

        assertNotNull(button);
        assertEquals("Tooltip", button.getToolTipText());
    }

    // --- labelPadded ---

    @Test
    public void testLabelPadded() {
        JLabel label = GUI.labelPadded("Padded", 5, 10, 5, 10);

        assertNotNull(label);
        assertEquals("Padded", label.getText());
        assertNotNull(label.getBorder());
        Insets insets = label.getBorder().getBorderInsets(label);
        assertEquals(5, insets.top);
        assertEquals(10, insets.left);
        assertEquals(5, insets.bottom);
        assertEquals(10, insets.right);
    }

    // --- button with icon ---

    @Test
    public void testButtonWithIconTextAndAction() {
        boolean[] actionCalled = {false};
        JButton button = GUI.button("/nonexistent/icon.png", "Icon Button", () -> actionCalled[0] = true);

        assertNotNull(button);
        assertEquals("Icon Button", button.getText());

        button.doClick();
        assertTrue(actionCalled[0]);
    }

    // --- button with ActionListener ---

    @Test
    public void testButtonWithActionListenerCreation() {
        boolean[] actionCalled = {false};
        JButton button = GUI.button("Listener Button", e -> actionCalled[0] = true);

        assertNotNull(button);
        assertEquals("Listener Button", button.getText());

        button.doClick();
        assertTrue(actionCalled[0]);
    }

    // --- buttonBrowseFiles ---

    @Test
    public void testButtonBrowseFiles() {
        Dialogs dialogs = createMock(Dialogs.class);

        expect(dialogs.chooseFile(
            eq("Open File"),
            eq("Open"),
            anyObject(Path.class),
            eq(false)
        )).andReturn(Optional.empty());

        replay(dialogs);

        BrowseButton button = GUI.buttonBrowseFiles(
            dialogs, "Open File", "Open", false, path -> {
            }
        );

        assertNotNull(button);
        assertEquals("Browse...", button.getText());

        button.doClick();
        verify(dialogs);
    }

    // --- menuItem ---

    @Test
    public void testMenuItemCreation() {
        Action action = new AbstractAction("Test Action") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };

        JMenuItem item = GUI.menuItem(action);

        assertNotNull(item);
        assertEquals("Test Action", item.getText());
    }

    // --- textAreaReadOnly ---

    @Test
    public void testTextAreaReadOnly() {
        JTextArea textArea = GUI.textAreaReadOnly(10, 5);

        assertNotNull(textArea);
        assertFalse(textArea.isEditable());
    }

    // --- toolBarVertical ---

    @Test
    public void testToolBarVertical() {
        JToolBar toolbar = GUI.toolBarVertical();

        assertNotNull(toolbar);
        assertFalse(toolbar.isFloatable());
        assertTrue(toolbar.isRollover());
        assertFalse(toolbar.isBorderPainted());
        assertEquals(JToolBar.VERTICAL, toolbar.getOrientation());
    }

    // --- splitPaneLeftToRight ---

    @Test
    public void testSplitPaneLeftToRight() {
        JPanel left = new JPanel();
        JPanel right = new JPanel();

        JSplitPane splitPane = GUI.splitPaneLeftToRight(left, right, 0.5);

        assertNotNull(splitPane);
        assertNull(splitPane.getBorder());
        assertTrue(splitPane.isOneTouchExpandable());
        assertTrue(splitPane.isContinuousLayout());
        assertEquals(JSplitPane.HORIZONTAL_SPLIT, splitPane.getOrientation());
        assertSame(left, splitPane.getLeftComponent());
        assertSame(right, splitPane.getRightComponent());
        assertEquals(0.5, splitPane.getResizeWeight(), 0.001);
    }

    // --- splitPaneTopToBottom ---

    @Test
    public void testSplitPaneTopToBottom() {
        JPanel top = new JPanel();
        JPanel bottom = new JPanel();

        JSplitPane splitPane = GUI.splitPaneTopToBottom(top, bottom, 0.3);

        assertNotNull(splitPane);
        assertNull(splitPane.getBorder());
        assertTrue(splitPane.isOneTouchExpandable());
        assertTrue(splitPane.isContinuousLayout());
        assertEquals(JSplitPane.VERTICAL_SPLIT, splitPane.getOrientation());
        assertSame(top, splitPane.getLeftComponent());
        assertSame(bottom, splitPane.getRightComponent());
        assertEquals(0.3, splitPane.getResizeWeight(), 0.001);
    }

    // --- panel ---

    @Test
    public void testPanelWithCustomLayout() {
        JPanel panel = GUI.panel("insets dialog", "[grow]", "[][]");

        assertNotNull(panel);
        assertTrue(panel.getLayout() instanceof MigLayout);
    }

    // --- addKeyListenerRecursively / removeKeyListenerRecursively ---

    @Test
    public void testAddKeyListenerRecursively() {
        JPanel parent = new JPanel();
        JPanel child = new JPanel();
        JButton grandChild = new JButton();
        parent.add(child);
        child.add(grandChild);

        KeyAdapter listener = new KeyAdapter() {
        };

        GUI.addKeyListenerRecursively(parent, listener);

        assertTrue(containsKeyListener(parent, listener));
        assertTrue(containsKeyListener(child, listener));
        assertTrue(containsKeyListener(grandChild, listener));
    }

    @Test
    public void testRemoveKeyListenerRecursively() {
        JPanel parent = new JPanel();
        JPanel child = new JPanel();
        JButton grandChild = new JButton();
        parent.add(child);
        child.add(grandChild);

        KeyAdapter listener = new KeyAdapter() {
        };

        GUI.addKeyListenerRecursively(parent, listener);
        GUI.removeKeyListenerRecursively(parent, listener);

        assertFalse(containsKeyListener(parent, listener));
        assertFalse(containsKeyListener(child, listener));
        assertFalse(containsKeyListener(grandChild, listener));
    }

    private boolean containsKeyListener(Component component, java.awt.event.KeyListener listener) {
        for (java.awt.event.KeyListener kl : component.getKeyListeners()) {
            if (kl == listener) {
                return true;
            }
        }
        return false;
    }

    // --- loadFontResource ---

    @Test
    public void testLoadFontResourceFallsBackToMonospaced() {
        Font font = GUI.loadFontResource("/nonexistent/font.ttf", GUITest.class, 12);

        assertNotNull(font);
        assertEquals(Font.MONOSPACED, font.getFamily());
        assertEquals(Font.PLAIN, font.getStyle());
        assertEquals(12, font.getSize());
    }

    // --- section ---

    @Test
    public void testSectionCreation() {
        JPanel panel = GUI.section("Tape content", "insets dialog", "[grow]", "[grow]");

        assertNotNull(panel);
        assertTrue(panel.getLayout() instanceof MigLayout);
        assertTrue(panel.getBorder() instanceof TitledBorder);
        assertEquals("Tape content", ((TitledBorder) panel.getBorder()).getTitle());
    }
}
