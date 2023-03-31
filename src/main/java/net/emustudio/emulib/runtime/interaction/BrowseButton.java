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
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * BrowseButton
 * <p>
 * A button with "Browse..." text, opening a dialog for selecting files or directories.
 */
@SuppressWarnings("unused")
public class BrowseButton extends JButton {
    private final LimitedCache<Path> pathCache = new LimitedCache<>(10);

    /**
     * Constructs new BrowseButton.
     *
     * @param dialogs           emuStudio dialogs
     * @param dialogTitle       Open dialog title
     * @param approveButtonText Approve button text (in the Open dialog)
     * @param onApprove         Approved path consumer
     * @param filters           list of file filters
     */
    public BrowseButton(Dialogs dialogs, String dialogTitle, String approveButtonText,
                        Consumer<Path> onApprove, FileExtensionsFilter... filters) {
        super("Browse...");

        setAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Path currentDirectory = pathCache
                        .first()
                        .orElse(Path.of(System.getProperty("user.dir")));

                dialogs.chooseFile(
                        dialogTitle, approveButtonText, currentDirectory, false,
                        filters
                ).ifPresent(path -> {
                    pathCache.put(path);
                    onApprove.accept(path);
                });
            }
        });
    }
}
