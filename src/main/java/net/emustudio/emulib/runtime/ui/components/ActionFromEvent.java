/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static net.emustudio.emulib.runtime.ui.GUI.loadIcon;

/**
 * An action created from an ActionEvent consumer.
 */
public class ActionFromEvent extends AbstractAction {
    /**
     * The action consumer.
     */
    private final Consumer<ActionEvent> action;

    /**
     * Constructs a new ActionFromEvent.
     *
     * @param action       action consumer
     * @param name         action name
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     */
    public ActionFromEvent(Consumer<ActionEvent> action, String name, String iconResource, String tooltipText) {
        super(name, loadIcon(iconResource));
        putValue(SHORT_DESCRIPTION, tooltipText);
        this.action = action;
    }

    /**
     * Constructs a new ActionFromEvent without a name.
     *
     * @param action       action consumer
     * @param iconResource icon resource path
     * @param tooltipText  tooltip text
     */
    public ActionFromEvent(Consumer<ActionEvent> action, String iconResource, String tooltipText) {
        this(action, null, iconResource, tooltipText);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        action.accept(actionEvent);
    }
}
