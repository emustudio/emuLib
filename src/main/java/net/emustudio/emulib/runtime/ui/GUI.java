/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.ui.components.FileExtensionsFilter;

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
 * GUI interface providing factory methods for common Swing components.
 * <p>
 * Implementations are provided by the host application (e.g., emuStudio) and made available to plugins via
 * {@link net.emustudio.emulib.runtime.ApplicationApi#getGUI()}.
 * <p>
 * Static utility methods ({@link #loadIcon(String)}, {@link #loadFontResource}, {@link #addKeyListenerRecursively},
 * {@link #removeKeyListenerRecursively}) are provided directly on this interface and do not depend on any
 * external layout or look-and-feel libraries.
 */
@SuppressWarnings("unused")
public interface GUI {

    // ========================= Static utility methods =========================

    /**
     * Loads an icon from a resource, using the caller's class for resource lookup.
     * <p>
     * Uses {@link StackWalker} to determine the caller's class so that the correct classloader is used
     * for plugin resources.
     *
     * @param resource resource path
     * @return loaded icon, or null if the icon could not be loaded
     */
    static ImageIcon loadIcon(String resource) {
        Class<?> klass = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass();
        return loadIcon(resource, klass);
    }

    /**
     * Loads an icon from a resource using the given class for resource lookup.
     *
     * @param resource    resource path
     * @param callerClass the class whose classloader should be used for resource lookup
     * @return loaded icon, or null if fthe icon could not be loaded
     */
    static ImageIcon loadIcon(String resource, Class<?> callerClass) {
        URL url = (callerClass == null ? GUI.class : callerClass).getResource(resource);
        return url == null ? null : new ImageIcon(url);
    }

    /**
     * Loads a true-type font from a resource. The font is derived true-type, plain with given size and kerning enabled.
     *
     * @param path          Resource path
     * @param resourceClass class where to look for resources
     * @param size          Default font size
     * @return loaded font, or {@code Font.MONOSPACED} if the font could not be loaded
     */
    static Font loadFontResource(String path, Class<?> resourceClass, int size) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);

        try (InputStream fin = resourceClass.getResourceAsStream(path)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fin))
                    .deriveFont(Font.PLAIN, size).deriveFont(attrs);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, size);
        }
    }

    /**
     * Adds a KeyListener to given component recursively.
     * Also installs a ContainerListener so that dynamically added children automatically get the KeyListener.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    static void addKeyListenerRecursively(Component component, KeyListener listener) {
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
                if (cl instanceof KeyListenerContainerAdapter
                        && ((KeyListenerContainerAdapter) cl).keyListener == listener) {
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
    static void removeKeyListenerRecursively(Component component, KeyListener listener) {
        component.removeKeyListener(listener);
        if (component instanceof Container) {
            Container cont = (Container) component;
            for (ContainerListener cl : cont.getContainerListeners()) {
                if (cl instanceof KeyListenerContainerAdapter
                        && ((KeyListenerContainerAdapter) cl).keyListener == listener) {
                    cont.removeContainerListener(cl);
                    break;
                }
            }
            for (Component child : cont.getComponents()) {
                removeKeyListenerRecursively(child, listener);
            }
        }
    }

    // ========================= Instance (abstract) factory methods =========================

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar button
     */
    JButton toolbarButton(Consumer<ActionEvent> action, String iconResource, String tooltipText);

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action button action
     * @return the toolbar button
     */
    JButton toolbarButton(Action action);

    /**
     * Toolbar button - a JButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar button
     */
    JButton toolbarButton(Action action, String iconResource, String tooltipText);

    /**
     * Toolbar toggle button - a JToggleButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param itemAction   item state change action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar toggle button
     */
    JToggleButton toolbarToggleButton(Consumer<ActionEvent> action, Consumer<ItemEvent> itemAction,
                                      String iconResource, String tooltipText);

    /**
     * Toolbar toggle button - a JToggleButton ready to add to a toolbar.
     *
     * @param action       button action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     * @return the toolbar toggle button
     */
    JToggleButton toolbarToggleButton(Consumer<ActionEvent> action, String iconResource, String tooltipText);

    /**
     * Creates a JLabel with given text.
     *
     * @param text label text
     * @return the JLabel
     */
    JLabel label(String text);

    /**
     * Creates a bold JLabel with given text.
     *
     * @param text label text
     * @return the bold JLabel
     */
    JLabel labelBold(String text);

    /**
     * Creates a title JLabel with given text (bold, larger font).
     *
     * @param text label text
     * @return the title JLabel
     */
    JLabel labelTitle(String text);

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
    JLabel labelPadded(String text, int top, int left, int bottom, int right);

    /**
     * Makes a JButton look like a primary button.
     *
     * @param button the JButton
     * @return the same JButton with modified properties
     */
    JButton buttonMakePrimary(JButton button);

    /**
     * Creates a JButton with given text.
     *
     * @param text button text
     * @return the JButton
     */
    JButton button(String text);

    /**
     * Creates a JButton with given text and action.
     *
     * @param text   button text
     * @param action button action
     * @return the JButton
     */
    JButton button(String text, Runnable action);

    /**
     * Creates a JButton with given icon, text and action.
     *
     * @param iconResource icon resource path
     * @param text         button text
     * @param action       button action
     * @return the JButton
     */
    JButton button(String iconResource, String text, Runnable action);

    /**
     * Creates a JButton with given text and action.
     *
     * @param text   button text
     * @param action button action
     * @return the JButton
     */
    JButton button(String text, ActionListener action);

    /**
     * Creates a browse button for directory selection.
     *
     * @param dialogs           emuStudio dialogs
     * @param dialogTitle       Open/Save dialog title
     * @param approveButtonText Approve button text
     * @param onApprove         Approved path consumer
     * @return the browse button
     */
    JButton buttonBrowseDirectories(Dialogs dialogs, String dialogTitle, String approveButtonText,
                                    Consumer<Path> onApprove);

    /**
     * Creates a browse button for file selection.
     *
     * @param dialogs                 emuStudio dialogs
     * @param dialogTitle             Open/Save dialog title
     * @param approveButtonText       Approve button text
     * @param appendMissingExtensions Append extension to selected file if it doesn't have it
     * @param onApprove               Approved path consumer
     * @param filters                 list of file filters
     * @return the browse button
     */
    JButton buttonBrowseFiles(Dialogs dialogs, String dialogTitle, String approveButtonText,
                              boolean appendMissingExtensions, Consumer<Path> onApprove,
                              FileExtensionsFilter... filters);

    /**
     * Creates a JMenuItem from a Swing Action.
     *
     * @param action the action for the menu item
     * @return the JMenuItem
     */
    JMenuItem menuItem(Action action);

    /**
     * Creates a JTextField with given text. The columns count is set to 20.
     *
     * @param text initial text
     * @return the JTextField
     */
    JTextField textField(String text);

    /**
     * Creates a JTextField with given text.
     *
     * @param text    initial text
     * @param columns number of columns
     * @return the JTextField
     */
    JTextField textField(String text, int columns);

    /**
     * Creates a non-editable JTextArea with given columns and rows.
     *
     * @param columns number of columns
     * @param rows    number of rows
     * @return the non-editable JTextArea
     */
    JTextArea textAreaReadOnly(int columns, int rows);

    /**
     * Creates a non-floatable, rollover JToolBar with no border painted.
     *
     * @return the JToolBar
     */
    JToolBar toolBar();

    /**
     * Creates a vertical JToolBar.
     *
     * @return the vertical JToolBar
     */
    JToolBar toolBarVertical();

    /**
     * Creates a JSplitPane with no border, one-touch expandable and continuous layout.
     *
     * @return the JSplitPane
     */
    JSplitPane splitPane();

    /**
     * Creates a horizontal JSplitPane with left and right components.
     *
     * @param left         left component
     * @param right        right component
     * @param resizeWeight resize weight
     * @return the JSplitPane
     */
    JSplitPane splitPaneLeftToRight(Component left, Component right, Double resizeWeight);

    /**
     * Creates a vertical JSplitPane with top and bottom components.
     *
     * @param top          top component
     * @param bottom       bottom component
     * @param resizeWeight resize weight
     * @return the JSplitPane
     */
    JSplitPane splitPaneTopToBottom(Component top, Component bottom, Double resizeWeight);

    /**
     * Creates a JScrollPane with no border around the viewport.
     *
     * @param view the component to be displayed in the scroll pane
     * @return the JScrollPane
     */
    JScrollPane scrollPane(Component view);

    /**
     * Creates a JPanel in a vertical arrangement.
     *
     * @return the JPanel in a vertical arrangement
     */
    JPanel panelVertical();

    /**
     * Creates a JPanel with custom layout constraints.
     *
     * @param layoutConstraints layout constraints
     * @param colConstraints    column constraints
     * @param rowConstraints    row constraints
     * @return the JPanel
     */
    JPanel panel(String layoutConstraints, String colConstraints, String rowConstraints);

    /**
     * Creates a JPanel in a horizontal arrangement.
     *
     * @return the JPanel in a horizontal arrangement
     */
    JPanel panelHorizontal();

    /**
     * Creates a JPanel for buttons at the bottom of dialogs.
     *
     * @return the JPanel for buttons
     */
    JPanel panelButtons();

    /**
     * Makes a JTable look modern.
     *
     * @param table the JTable
     */
    void styleTable(JTable table);

    /**
     * Makes a JList look modern.
     *
     * @param list the JList
     */
    void styleList(JList<?> list);

    /**
     * Creates a titled section panel.
     *
     * @param title             section title
     * @param layoutConstraints layout constraints
     * @param colConstraints    column constraints
     * @param rowConstraints    row constraints
     * @return JPanel with a titled border
     */
    JPanel section(String title, String layoutConstraints, String colConstraints, String rowConstraints);


    // ========================= Internal helper classes =========================

    /**
     * ContainerListener that automatically adds/removes a KeyListener to/from dynamically added/removed children.
     */
    class KeyListenerContainerAdapter implements ContainerListener {
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

    /**
     * Checks if a component already has a given key listener.
     */
    private static boolean hasKeyListener(Component component, KeyListener listener) {
        for (KeyListener kl : component.getKeyListeners()) {
            if (kl == listener) {
                return true;
            }
        }
        return false;
    }
}
