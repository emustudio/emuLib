/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;
import static net.emustudio.emulib.runtime.ui.GUI.loadIcon;

/**
 * Toolbar toggle button - a JToggleButton ready to add to a toolbar.
 * Properties:
 * - button text is hidden
 * - tooltip is set from Action.getValue(SHORT_DESCRIPTION) by default
 * - is not focusable
 * - icon is set from icon resource path
 * - button action is external
 */
@SuppressWarnings("unused")
public class ToolbarToggleButton extends JToggleButton {

    /**
     * Creates a new toolbar toggle button.
     * <p>
     * Tooltip text is set from <code>action.getValue(SHORT_DESCRIPTION)</code>.
     *
     * @param action action to be performed when the button is pressed
     */
    public ToolbarToggleButton(Action action) {
        super(action);
        setHideActionText(true);
        setToolTipText(String.valueOf(action.getValue(SHORT_DESCRIPTION)));
        setFocusable(false);
        putClientProperty("JButton.buttonType", "toolBarButton");
    }

    /**
     * Creates a new toolbar toggle button.
     * <p>
     * Tooltip text is set to <code>Action.SHORT_DESCRIPTION</code>.
     * Icon is set to <code>Action.SMALL_ICON</code>.
     *
     * @param action       action to be performed when the button is pressed
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     */
    public ToolbarToggleButton(Action action, String iconResource, String tooltipText) {
        super(action);
        action.putValue(SHORT_DESCRIPTION, tooltipText);
        action.putValue(SMALL_ICON, loadIcon(iconResource));
        setHideActionText(true);
        setToolTipText(tooltipText);
        setFocusable(false);
        putClientProperty("JButton.buttonType", "toolBarButton");
    }

    /**
     * Creates a new toolbar toggle button.
     *
     * @param action       action to be performed when the button is pressed
     * @param itemAction   item state change action
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     */
    public ToolbarToggleButton(Consumer<ActionEvent> action, Consumer<ItemEvent> itemAction, String iconResource, String tooltipText) {
        this(action, iconResource, tooltipText);
        addItemListener(itemAction::accept);
    }

    /**
     * Creates a new toolbar toggle button.
     *
     * @param action       action to be performed when the button is pressed
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     */
    public ToolbarToggleButton(Consumer<ActionEvent> action, String iconResource, String tooltipText) {
        this(new ActionFromEvent(action, iconResource, tooltipText));
    }
}
