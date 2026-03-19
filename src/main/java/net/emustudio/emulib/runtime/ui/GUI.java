/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.ui.components.BrowseButton;
import net.emustudio.emulib.runtime.ui.components.FileExtensionsFilter;
import net.emustudio.emulib.runtime.ui.components.ToolbarButton;
import net.emustudio.emulib.runtime.ui.components.ToolbarToggleButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

/**
 * GUI utility class providing factory methods for common Swing components.
 */
public class GUI {

    /**
     * Constructs a new GUI instance.
     */
    private GUI() {
    }

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar button
     */
    public static ToolbarButton toolbarButton(Consumer<ActionEvent> action, String iconResource, String tooltipText) {
        return new ToolbarButton(action, iconResource, tooltipText);
    }

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action button action
     * @return the toolbar button
     */
    public static ToolbarButton toolbarButton(Action action) {
        return new ToolbarButton(action);
    }

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar button
     */
    public static ToolbarButton toolbarButton(Action action, String iconResource, String tooltipText) {
        return new ToolbarButton(action, iconResource, tooltipText);
    }

    /**
     * Toolbar toggle button - a JToggleButton ready to add to a toolbar.
     * <p>
     * Properties:
     * - button text is hidden
     * - tooltip is set from Action.getValue(SHORT_DESCRIPTION) by default
     * - is not focusable
     * - icon is set from icon resource path
     * - button action is external
     *
     * @param action       button action
     * @param itemAction   item state change action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar toggle button
     */
    public static ToolbarToggleButton toolbarToggleButton(Consumer<ActionEvent> action, Consumer<ItemEvent> itemAction, String iconResource, String tooltipText) {
        return new ToolbarToggleButton(action, itemAction, iconResource, tooltipText);
    }

    /**
     * Creates a toolbar toggle button with only action (no item state change action).
     * <p>
     * Properties:
     * - button text is hidden
     * - tooltip is set from Action.getValue(SHORT_DESCRIPTION) by default
     * - is not focusable
     * - icon is set from icon resource path
     * - button action is external
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar toggle button
     */
    public static ToolbarToggleButton toolbarToggleButton(Consumer<ActionEvent> action, String iconResource, String tooltipText) {
        return new ToolbarToggleButton(action, iconResource, tooltipText);
    }


    /**
     * Creates a JLabel with given text.
     *
     * @param text label text
     * @return the JLabel
     */
    public static JLabel label(String text) {
        return new JLabel(text);
    }

    /**
     * Creates a bold JLabel with given text.
     *
     * @param text label text
     * @return the bold JLabel
     */
    public static JLabel labelBold(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    /**
     * Creates a title JLabel with given text. It means bold font with size increased by 2.
     *
     * @param text label text
     * @return the title JLabel
     */
    public static JLabel labelTitle(String text) {
        JLabel label = new JLabel(text);
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD, font.getSize() + 2));
        return label;
    }

    /**
     * Creates a JLabel with given text and padding (empty border).
     *
     * @param text   label text
     * @param top    top padding in pixels
     * @param left   left padding in pixels
     * @param bottom bottom padding in pixels
     * @param right  right padding in pixels
     * @return the padded JLabel
     */
    public static JLabel labelPadded(String text, int top, int left, int bottom, int right) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return label;
    }

    /**
     * Makes a JButton look like a primary button.
     *
     * @param button the JButton
     * @return the same JButton with modified properties
     */
    public static JButton buttonMakePrimary(JButton button) {
        button.putClientProperty("JButton.buttonType", "borderless");
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        return button;
    }

    /**
     * Creates a JButton with given text.
     *
     * @param text button text
     * @return the JButton
     */
    public static JButton button(String text) {
        return new JButton(text);
    }

    /**
     * Creates a JButton with given text and action.
     *
     * @param text   button text
     * @param action button action
     * @return the JButton
     */
    public static JButton button(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    /**
     * Creates a JButton with given icon, text and action.
     *
     * @param iconResource icon resource path
     * @param text         button text
     * @param action       button action
     * @return the JButton
     */
    public static JButton button(String iconResource, String text, Runnable action) {
        JButton btn = new JButton(text, loadIcon(iconResource));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    /**
     * Creates a JButton with given text and action.
     *
     * @param text   button text
     * @param action button action
     * @return the JButton
     */
    public static JButton button(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.addActionListener(action);
        return btn;
    }

    /**
     * Creates a new BrowseButton with directory-choosing action. It's a button with "Browse..." text, opening a dialog
     * for selecting directories.
     * <p>
     * It can remember up to 10 last selected paths for convenience.
     *
     * @param dialogs           emuStudio dialogs
     * @param dialogTitle       Open/Save dialog title
     * @param approveButtonText Approve button text (in the Open/Save dialog)
     * @param onApprove         Approved path consumer
     * @return the BrowseButton
     */
    public static BrowseButton buttonBrowseDirectories(Dialogs dialogs, String dialogTitle, String approveButtonText, Consumer<Path> onApprove) {
        return new BrowseButton(dialogs, dialogTitle, approveButtonText, onApprove);
    }

    /**
     * Creates a new BrowseButton with file-choosing action. It's a button with "Browse..." text, opening a dialog
     * for selecting files.
     * <p>
     * It can remember up to 10 last selected paths for convenience.
     *
     * @param dialogs                 emuStudio dialogs
     * @param dialogTitle             Open/Save dialog title
     * @param approveButtonText       Approve button text (in the Open/Save dialog)
     * @param onApprove               Approved path consumer
     * @param appendMissingExtensions Append extension to selected file if it doesn't have it (useful for Save dialog)
     * @param filters                 list of file filters
     * @return the BrowseButton
     */
    public static BrowseButton buttonBrowseFiles(Dialogs dialogs, String dialogTitle, String approveButtonText, boolean appendMissingExtensions, Consumer<Path> onApprove, FileExtensionsFilter... filters) {
        return new BrowseButton(dialogs, dialogTitle, approveButtonText, appendMissingExtensions, onApprove, filters);
    }

    /**
     * Creates a JMenuItem from a Swing Action.
     *
     * @param action the action for the menu item
     * @return the JMenuItem
     */
    public static JMenuItem menuItem(Action action) {
        return new JMenuItem(action);
    }

    /**
     * Creates a JTextField with given text. The columns count is set to 20.
     *
     * @param text initial text
     * @return the JTextField
     */
    public static JTextField textField(String text) {
        return textField(text, 20);
    }

    /**
     * Creates a JTextField with given text.
     *
     * @param text    initial text
     * @param columns number of columns
     * @return the JTextField
     */
    public static JTextField textField(String text, int columns) {
        return new JTextField(text, columns);
    }

    /**
     * Creates a non-editable JTextArea with given columns and rows.
     *
     * @param columns number of columns
     * @param rows    number of rows
     * @return the non-editable JTextArea
     */
    public static JTextArea textAreaReadOnly(int columns, int rows) {
        JTextArea textArea = new JTextArea(columns, rows);
        textArea.setEditable(false);
        return textArea;
    }


    /**
     * Creates a non-floatable, rollover JToolBar with no border painted.
     *
     * @return the JToolBar
     */
    public static JToolBar toolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        return toolbar;
    }

    /**
     * Creates a vertical JToolBar with the same properties as {@link #toolBar()}.
     *
     * @return the vertical JToolBar
     */
    public static JToolBar toolBarVertical() {
        JToolBar toolbar = toolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        return toolbar;
    }

    /**
     * Creates a JSplitPane with no border, one-touch expandable and continuous layout.
     *
     * @return the JSplitPane
     */
    public static JSplitPane splitPane() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    /**
     * Creates a JSplitPane with left and right components, no border, one-touch expandable and continuous layout.
     * The split pane is oriented horizontally (left to right).
     *
     * @param left         left component
     * @param right        right component
     * @param resizeWeight resize weight (0.0 - left component does not resize, 1.0 - right component does not resize)
     * @return the JSplitPane
     */
    public static JSplitPane splitPaneLeftToRight(Component left, Component right, Double resizeWeight) {
        JSplitPane splitPane = splitPane();
        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(right);
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        return splitPane;
    }

    /**
     * Creates a JSplitPane with top and bottom components, no border, one-touch expandable and continuous layout.
     * The split pane is oriented vertically (top to bottom).
     *
     * @param top          top component
     * @param bottom       bottom component
     * @param resizeWeight resize weight (0.0 - top component does not resize, 1.0 - bottom component does not resize)
     * @return the JSplitPane
     */
    public static JSplitPane splitPaneTopToBottom(Component top, Component bottom, Double resizeWeight) {
        JSplitPane splitPane = splitPane();
        splitPane.setLeftComponent(top);
        splitPane.setRightComponent(bottom);
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        return splitPane;
    }

    /**
     * Creates a JScrollPane with no border around the viewport. The returned scroll pane has empty border.
     *
     * @param view the component to be displayed in the scroll pane
     * @return the JScrollPane
     */
    public static JScrollPane scrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    /**
     * Creates a JPanel in a vertical arrangement. Vertical panel has one column that grows and
     * fills the available horizontal space.
     *
     * @return the JPanel in a vertical arrangement
     */
    public static JPanel panelVertical() {
        return new JPanel(new MigLayout("insets dialog, fillx", "[grow]", "[]"));
    }

    /**
     * Creates a JPanel with custom MigLayout constraints.
     *
     * @param layoutConstraints layout constraints
     * @param colConstraints    column constraints
     * @param rowConstraints    row constraints
     * @return the JPanel
     */
    public static JPanel panel(String layoutConstraints, String colConstraints, String rowConstraints) {
        return new JPanel(new MigLayout(layoutConstraints, colConstraints, rowConstraints));
    }

    /**
     * Creates a JPanel in a horizontal arrangement. Horizontal panel has one row that grows and
     * fills the available vertical space.
     *
     * @return the JPanel in a horizontal arrangement
     */
    public static JPanel panelHorizontal() {
        return new JPanel(new MigLayout("insets dialog, filly", "[]", "[grow]"));
    }

    /**
     * Creates a JPanel for buttons at the bottom of dialogs. The panel has one row and one column,
     * aligned to the right and growing.
     *
     * @return the JPanel for buttons
     */
    public static JPanel panelButtons() {
        return new JPanel(new MigLayout("insets dialog", "[grow, right]", "[]"));
    }

    /**
     * Makes a JTable look modern. It means no grid lines, no intercell spacing and increased row height.
     *
     * @param table the JTable
     */
    public static void styleTable(JTable table) {
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(table.getRowHeight() + 4);
    }

    /**
     * Makes a JList look modern. It means adding an empty border around the list, with thickness set to 2.
     *
     * @param list the JList
     */
    public static void styleList(JList<?> list) {
        list.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    /**
     * ContainerListener that automatically adds/removes a KeyListener to/from dynamically added/removed children.
     */
    private static class KeyListenerContainerAdapter implements ContainerListener {
        final KeyListener keyListener;

        KeyListenerContainerAdapter(KeyListener keyListener) {
            this.keyListener = keyListener;
        }

        @Override
        public void componentAdded(ContainerEvent e) {
            addKeyListenerRecursively(e.getChild(), keyListener);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            removeKeyListenerRecursively(e.getChild(), keyListener);
        }
    }

    private static boolean hasKeyListener(Component component, KeyListener listener) {
        for (KeyListener kl : component.getKeyListeners()) {
            if (kl == listener) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a KeyListener to given component recursively.
     * Also installs a ContainerListener so that dynamically added children automatically get the KeyListener.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    public static void addKeyListenerRecursively(Component component, KeyListener listener) {
        if (!hasKeyListener(component, listener)) {
            component.addKeyListener(listener);
        }
        if (component instanceof Container) {
            Container cont = (Container) component;
            for (Component child : cont.getComponents()) {
                addKeyListenerRecursively(child, listener);
            }
            boolean hasAdapter = false;
            for (ContainerListener cl : cont.getContainerListeners()) {
                if (cl instanceof KeyListenerContainerAdapter && ((KeyListenerContainerAdapter) cl).keyListener == listener) {
                    hasAdapter = true;
                    break;
                }
            }
            if (!hasAdapter) {
                cont.addContainerListener(new KeyListenerContainerAdapter(listener));
            }
        }
    }

    /**
     * Removes given KeyListener from a component recursively.
     * Also removes the ContainerListener that was installed by {@link #addKeyListenerRecursively}.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    public static void removeKeyListenerRecursively(Component component, KeyListener listener) {
        component.removeKeyListener(listener);
        if (component instanceof Container) {
            Container cont = (Container) component;
            for (ContainerListener cl : cont.getContainerListeners()) {
                if (cl instanceof KeyListenerContainerAdapter && ((KeyListenerContainerAdapter) cl).keyListener == listener) {
                    cont.removeContainerListener(cl);
                    break;
                }
            }
            for (Component child : cont.getComponents()) {
                removeKeyListenerRecursively(child, listener);
            }
        }
    }

    /**
     * Loads a true-type font from a resource. The font is derived true-type, plain with given size and kerning enabled.
     *
     * @param path          Resource path
     * @param size          Default font size
     * @param resourceClass class where to look for resources
     * @return loaded font, or <code>Font.MONOSPACED</code> if the font could not be loaded
     */
    public static Font loadFontResource(String path, Class<?> resourceClass, int size) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);

        try (InputStream fin = resourceClass.getResourceAsStream(path)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fin)).deriveFont(Font.PLAIN, size).deriveFont(attrs);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, size);
        }
    }

    /**
     * Creates a titled section panel with MigLayout.
     *
     * @param title             section title (used as titled border)
     * @param layoutConstraints MigLayout layout constraints
     * @param colConstraints    MigLayout column constraints
     * @param rowConstraints    MigLayout row constraints
     * @return JPanel with a titled border and MigLayout
     */
    public static JPanel section(String title, String layoutConstraints, String colConstraints, String rowConstraints) {
        JPanel panel = new JPanel(new MigLayout(layoutConstraints, colConstraints, rowConstraints));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    /**
     * Loads an icon from a resource
     *
     * @param resource resource path
     * @return loaded icon, or null if the icon could not be loaded
     */
    public static ImageIcon loadIcon(String resource) {
        // emuLib is loaded at the system level, so it does not see resources of a plugin
        Class<?> klass = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass();
        URL url = (klass == null ? GUI.class : klass).getResource(resource);
        return url == null ? null : new ImageIcon(url);
    }
}
