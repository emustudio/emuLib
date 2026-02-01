/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static net.emustudio.emulib.runtime.ui.GUI.loadIcon;

public class ActionFromEvent extends AbstractAction {
    private final Consumer<ActionEvent> action;

    public ActionFromEvent(Consumer<ActionEvent> action, String name, String iconResource, String tooltipText) {
        super(name, loadIcon(iconResource));
        putValue(SHORT_DESCRIPTION, tooltipText);
        this.action = action;
    }

    public ActionFromEvent(Consumer<ActionEvent> action, String iconResource, String tooltipText) {
        this(action, null, iconResource, tooltipText);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        action.accept(actionEvent);
    }
}
