/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application.ui;

import net.emustudio.emulib.io.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Dialogs used by emuStudio and plugins.
 * <p>
 * If GUI is enabled, the dialogs are shown on the screen. Otherwise, they are logged and read* methods return
 * empty values. Each method requires a title; initial values and base directories are nullable.
 * <p>
 * Example: {@code dialogs.showError("Cannot open file", "Open file")}
 */
public interface Dialogs {

    enum Answer {
        YES, NO, CANCEL
    }

    void showError(String message, String title);

    void showInfo(String message, String title);

    /**
     * Asks a yes/no/cancel question.
     *
     * @param message question
     * @param title   dialog title
     * @return answer
     */
    Answer ask(String message, String title);

    /**
     * @param message message
     * @param title   dialog title
     * @param initial initial value, or null
     * @return entered value; empty if cancelled or invalid
     */
    Optional<String> readString(String message, String title, String initial);

    /**
     * @param message message
     * @param title   dialog title
     * @param initial initial value, or null
     * @return entered value; empty if cancelled or invalid
     */
    Optional<Integer> readInteger(String message, String title, Integer initial);

    /**
     * @param message message
     * @param title   dialog title
     * @param initial initial value, or null
     * @return entered value; empty if cancelled or invalid
     */
    Optional<Double> readDouble(String message, String title, Double initial);

    /**
     * Shows file chooser.
     *
     * @param title                  dialog title
     * @param approveButtonText      approve button text
     * @param baseDirectory          initial directory, or null for current directory
     * @param appendMissingExtension whether to append the first extension of selected file type if missing
     * @param fileTypes              file types to filter
     * @return selected file; empty if cancelled
     */
    Optional<Path> chooseFile(String title, String approveButtonText, Path baseDirectory,
                              boolean appendMissingExtension, List<FileType> fileTypes);

    /**
     * Shows directory chooser.
     *
     * @param title             dialog title
     * @param approveButtonText approve button text
     * @param baseDirectory     initial directory, or null for current directory
     * @return selected directory; empty if cancelled
     */
    Optional<Path> chooseDirectory(String title, String approveButtonText, Path baseDirectory);
}
