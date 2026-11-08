/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.awt.image.BaseMultiResolutionImage;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory of host-styled Swing components for plugins, plus static Swing helpers.
 * <p>
 * The host application (e.g., emuStudio) implements the factory methods and provides the instance via
 * {@link net.emustudio.emulib.runtime.ApplicationApi#getGUI()}. Panel layout constraints are MigLayout strings.
 */
public interface GUI {

    /**
     * Loads an icon from a resource. If a matching resource with {@code @2x} before the file extension exists
     * (e.g. {@code icon@2x.png} for {@code icon.png}), it is used for high-resolution rendering.
     *
     * @param resource      resource path; relative paths are resolved against {@code resourceClass}
     * @param resourceClass class whose classloader is used for resource lookup
     * @return loaded icon, or null if the base resource could not be found
     */
    static ImageIcon loadIcon(String resource, Class<?> resourceClass) {
        URL url = resourceClass.getResource(resource);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        int extension = resource.lastIndexOf('.');
        if (extension <= resource.lastIndexOf('/')) {
            extension = resource.length();
        }
        URL url2x = resourceClass.getResource(resource.substring(0, extension) + "@2x" + resource.substring(extension));
        return url2x == null ? icon : new ImageIcon(new BaseMultiResolutionImage(
                icon.getImage(), new ImageIcon(url2x).getImage()), icon.getDescription());
    }

    /**
     * Loads and registers a plain true-type font with kerning enabled.
     *
     * @param path          resource path; relative paths are resolved against {@code resourceClass}
     * @param resourceClass class whose classloader is used for resource lookup
     * @param size          font size
     * @return loaded font, or a plain {@code Font.MONOSPACED} font if the font could not be loaded
     */
    static Font loadFontResource(String path, Class<?> resourceClass, int size) {
        try (InputStream fin = resourceClass.getResourceAsStream(path)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(fin))
                    .deriveFont(Font.PLAIN, size).deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            return new Font(Font.MONOSPACED, Font.PLAIN, size);
        }
    }

    /**
     * Shortens text to fit the given width when rendered with the component's font.
     * <p>
     * The text is cut in the middle, keeping a prefix and suffix around "..."; Unicode characters (grapheme
     * clusters) are kept intact. If even "..." does not fit, only the dots that fit are returned.
     *
     * @param text      text to shorten
     * @param component component whose font is used for measuring
     * @param width     available width in pixels; non-positive widths produce an empty string
     * @return text, or its shortened form that fits the width
     */
    static String shortenToFit(String text, Component component, int width) {
        FontMetrics metrics = component.getFontMetrics(component.getFont());
        if (metrics.stringWidth(text) <= width) {
            return text;
        }
        String best = "";
        int low = 0;
        int high = text.length() - 1;
        while (low <= high) {
            int length = low + (high - low) / 2;
            String candidate = shorten(text, length);
            if (metrics.stringWidth(candidate) <= width) {
                best = candidate;
                low = length + 1;
            } else {
                high = length - 1;
            }
        }
        return best;
    }

    private static String shorten(String text, int maxLength) {
        if (maxLength < 3) {
            return "...".substring(0, maxLength);
        }
        int visible = maxLength - 3;
        int prefixBudget = visible / 2;
        int suffixBoundary = text.length() - (visible - prefixBudget);
        int prefixEnd = 0;
        int suffixStart = text.length();
        Matcher characters = Pattern.compile("\\X").matcher(text);
        while (characters.find()) {
            if (characters.end() <= prefixBudget) {
                prefixEnd = characters.end();
            }
            if (characters.start() >= suffixBoundary) {
                suffixStart = characters.start();
                break;
            }
        }
        return text.substring(0, prefixEnd) + "..." + text.substring(suffixStart);
    }

    /**
     * Adds a KeyListener to the component and all its descendants, including children added later.
     * Adding the same listener twice has no effect.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    static void addKeyListenerRecursively(Component component, KeyListener listener) {
        if (!Arrays.asList(component.getKeyListeners()).contains(listener)) {
            component.addKeyListener(listener);
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                addKeyListenerRecursively(child, listener);
            }
            if (findAdapter(container, listener) == null) {
                container.addContainerListener(new KeyListenerContainerAdapter(listener));
            }
        }
    }

    /**
     * Removes a KeyListener added by {@link #addKeyListenerRecursively} from the component and its descendants.
     *
     * @param component GUI component
     * @param listener  KeyListener object
     */
    static void removeKeyListenerRecursively(Component component, KeyListener listener) {
        component.removeKeyListener(listener);
        if (component instanceof Container) {
            Container container = (Container) component;
            container.removeContainerListener(findAdapter(container, listener));
            for (Component child : container.getComponents()) {
                removeKeyListenerRecursively(child, listener);
            }
        }
    }

    private static ContainerListener findAdapter(Container container, KeyListener listener) {
        for (ContainerListener cl : container.getContainerListeners()) {
            if (cl instanceof KeyListenerContainerAdapter && ((KeyListenerContainerAdapter) cl).keyListener == listener) {
                return cl;
            }
        }
        return null;
    }

    /**
     * Creates a toolbar button from an action. The action provides the icon ({@link Action#SMALL_ICON}),
     * tooltip and accessible name ({@link Action#SHORT_DESCRIPTION}) and enabled state; the text is hidden.
     *
     * @param action button action
     * @return the toolbar button
     */
    JButton toolbarButton(Action action);

    /**
     * Creates a plain label.
     *
     * @param text label text
     * @return the label
     */
    JLabel label(String text);

    /**
     * Creates a bold label.
     *
     * @param text label text
     * @return the bold label
     */
    JLabel labelBold(String text);

    /**
     * Creates a button; add listeners with {@link JButton#addActionListener}.
     *
     * @param text button text
     * @return the button
     */
    JButton button(String text);

    /**
     * Styles a button as the primary (default) button of a dialog.
     *
     * @param button the button
     * @return the same button
     */
    JButton buttonMakePrimary(JButton button);

    /**
     * Creates a text field.
     *
     * @param text    initial text
     * @param columns number of columns
     * @return the text field
     */
    JTextField textField(String text, int columns);

    /**
     * Creates a non-editable text area.
     *
     * @param rows    number of rows
     * @param columns number of columns
     * @return the text area
     */
    JTextArea textAreaReadOnly(int rows, int columns);

    /**
     * Creates a horizontal, non-floatable toolbar. Use {@link JToolBar#setOrientation} for a vertical one.
     *
     * @return the toolbar
     */
    JToolBar toolBar();

    /**
     * Creates a split pane.
     *
     * @param orientation  {@link JSplitPane#HORIZONTAL_SPLIT} (left to right) or
     *                     {@link JSplitPane#VERTICAL_SPLIT} (top to bottom)
     * @param first        left or top component
     * @param second       right or bottom component
     * @param resizeWeight how extra space is distributed (0 = all to second, 1 = all to first)
     * @return the split pane
     */
    JSplitPane splitPane(int orientation, Component first, Component second, double resizeWeight);

    /**
     * Creates a borderless scroll pane.
     *
     * @param view the scrolled component
     * @return the scroll pane
     */
    JScrollPane scrollPane(Component view);

    /**
     * Creates a panel with MigLayout.
     *
     * @param layoutConstraints layout constraints
     * @param colConstraints    column constraints
     * @param rowConstraints    row constraints
     * @return the panel
     */
    JPanel panel(String layoutConstraints, String colConstraints, String rowConstraints);

    /**
     * Creates a panel with MigLayout and a titled border.
     *
     * @param title             section title
     * @param layoutConstraints layout constraints
     * @param colConstraints    column constraints
     * @param rowConstraints    row constraints
     * @return the titled panel
     */
    JPanel section(String title, String layoutConstraints, String colConstraints, String rowConstraints);

    /**
     * Applies the host's table styling. Reapplying the style must not keep increasing row heights.
     *
     * @param table the table
     */
    void styleTable(JTable table);

    /**
     * Container listener propagating a key listener to added children and removing it from removed ones.
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
}
