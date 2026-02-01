/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import net.emustudio.emulib.runtime.ui.Dialogs;
import net.emustudio.emulib.runtime.ui.MaxItemsCache;

import javax.swing.*;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * BrowseButton
 * <p>
 * A button with "Browse..." text, opening a dialog for selecting files or directories.
 * It can remember up to 10 last selected paths for convenience.
 */
public class BrowseButton extends JButton {
    private final MaxItemsCache<Path> pathCache = new MaxItemsCache<>(10);

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
