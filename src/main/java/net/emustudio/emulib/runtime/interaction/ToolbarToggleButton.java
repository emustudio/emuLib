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
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static javax.swing.Action.SHORT_DESCRIPTION;

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
