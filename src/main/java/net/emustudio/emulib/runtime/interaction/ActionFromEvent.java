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

import static net.emustudio.emulib.runtime.interaction.GuiUtils.loadIcon;

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
