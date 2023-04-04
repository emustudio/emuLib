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
     * Constructs new BrowseButton with directory-choosing action
     *
     * @param dialogs           emuStudio dialogs
     * @param dialogTitle       Open/Save dialog title
     * @param approveButtonText Approve button text (in the Open/Save dialog)
     * @param onApprove         Approved path consumer
     */
    public BrowseButton(Dialogs dialogs, String dialogTitle, String approveButtonText, Consumer<Path> onApprove) {
        super("Browse...");
        this.addActionListener(e -> dialogs
                .chooseDirectory(dialogTitle, approveButtonText, getCurrentDirectory())
                .ifPresent(path -> {
                    pathCache.put(path);
                    onApprove.accept(path);
                }));
    }

    /**
     * Constructs new BrowseButton with file-choosing action.
     *
     * @param dialogs                 emuStudio dialogs
     * @param dialogTitle             Open/Save dialog title
     * @param approveButtonText       Approve button text (in the Open/Save dialog)
     * @param onApprove               Approved path consumer
     * @param appendMissingExtensions Append extension to selected file if it doesn't have it (useful for Save dialog)
     * @param filters                 list of file filters
     */
    public BrowseButton(Dialogs dialogs, String dialogTitle, String approveButtonText,
                        boolean appendMissingExtensions,
                        Consumer<Path> onApprove, FileExtensionsFilter... filters) {
        super("Browse...");
        addActionListener(e -> dialogs
                .chooseFile(dialogTitle, approveButtonText, getCurrentDirectory(), appendMissingExtensions, filters)
                .ifPresent(path -> {
                    pathCache.put(path);
                    onApprove.accept(path);
                }));
    }

    private Path getCurrentDirectory() {
        return pathCache.first().orElse(Path.of(System.getProperty("user.dir")));
    }
}
