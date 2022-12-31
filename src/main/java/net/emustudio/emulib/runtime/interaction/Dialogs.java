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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * This class provides dialogs used by emuStudio and plugins.
 * <p>
 * If GUI is enabled, the dialogs are shown on the screen. Otherwise, they are logged.
 * <p>
 * Example of usage:
 * <code>
 * dialogs.showMessage("Hello, world!");
 * </code>
 */
@SuppressWarnings("unused")
public interface Dialogs {

    enum DialogAnswer {
        ANSWER_YES, ANSWER_NO, ANSWER_CANCEL
    }

    /**
     * Show error message.
     *
     * @param message error message to show
     */
    void showError(String message);

    /**
     * Show error message.
     *
     * @param message error message to show
     * @param title   title of the message
     */
    void showError(String message, String title);

    /**
     * Show information message.
     *
     * @param message information message to show
     */
    void showInfo(String message);

    /**
     * Show information message.
     *
     * @param message information message to show
     * @param title   title of the message
     */
    void showInfo(String message, String title);

    /**
     * Ask user for integer input.
     * <p>
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @return integer number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Integer> readInteger(String message);

    /**
     * Ask user for integer input.
     * <p>
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @param title   title of the input message
     * @return integer number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Integer> readInteger(String message, String title);

    /**
     * Ask user for integer input.
     * <p>
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @param title   title of the input message
     * @param initial initial value
     * @return integer number entered by user, or the initial value by default
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Integer> readInteger(String message, String title, int initial);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @return a String entered by user, or Optional.empty() if user cancelled the input dialog
     */
    Optional<String> readString(String message);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @param title   title of the input message
     * @return a String entered by user, or Optional.empty() if user cancelled the input dialog
     */
    Optional<String> readString(String message, String title);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @param title   title of the input message
     * @param initial initial value
     * @return a String entered by user, or the initial value by default
     */
    Optional<String> readString(String message, String title, String initial);

    /**
     * Ask user for Double input.
     * <p>
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @return double number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message);

    /**
     * Ask user for Double input.
     * <p>
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @param title   title of the input message
     * @return double number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message, String title);

    /**
     * Ask user for Double input.
     * <p>
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @param title   title of the input message
     * @param initial initial value
     * @return double number entered by user, or the initial value by default
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message, String title, double initial);

    /**
     * Ask user for a confirmation.
     * <p>
     * User can choose between of YES/NO/CANCEL option.
     *
     * @param message message to show
     * @return {@link DialogAnswer#ANSWER_YES} or {@link DialogAnswer#ANSWER_NO} or {@link DialogAnswer#ANSWER_CANCEL}
     */
    DialogAnswer ask(String message);

    /**
     * Ask user for a confirmation.
     * <p>
     * User can choose between of YES/NO/CANCEL option.
     *
     * @param message message to show
     * @param title   title of the message
     * @return {@link DialogAnswer#ANSWER_YES} or {@link DialogAnswer#ANSWER_NO} or {@link DialogAnswer#ANSWER_CANCEL}
     */
    DialogAnswer ask(String message, String title);

    /**
     * Ask user to choose a file.
     * <p>
     * Base directory of the dialog will be set to {@code System.getProperty("user.dir")}.
     *
     * @param title                  dialog title
     * @param approveButtonText      approve button text (e.g. "Open", "Save", ...)
     * @param appendMissingExtension Whether extension from selected filter should be appended to selected file if it
     *                               has no extension
     * @param filters                supported file filters
     * @return Selected file if provided, or Optional.empty() if user cancelled the dialog
     */
    Optional<Path> chooseFile(String title, String approveButtonText, boolean appendMissingExtension,
                              FileExtensionsFilter... filters);

    /**
     * Ask user to choose a file.
     * <p>
     * Base directory of the dialog will be set to {@code System.getProperty("user.dir")}.
     *
     * @param title                  dialog title
     * @param approveButtonText      approve button text (e.g. "Open", "Save", ...)
     * @param appendMissingExtension Whether extension from selected filter should be appended to selected file if it
     *                               has no extension
     * @param filters                supported file filters
     * @return Selected file if provided, or Optional.empty() if user cancelled the dialog
     */
    Optional<Path> chooseFile(String title, String approveButtonText, boolean appendMissingExtension,
                              List<FileExtensionsFilter> filters);

    /**
     * Ask user to choose a file.
     *
     * @param title                  dialog title
     * @param approveButtonText      approve button text (e.g. "Open", "Save", ...)
     * @param baseDirectory          Base directory of the dialog (where will the dialog point to)
     * @param appendMissingExtension Whether extension from selected filter should be appended to selected file if it
     *                               has no extension
     * @param filters                supported file filters
     * @return Selected file if provided, or Optional.empty() if user cancelled the dialog
     */
    Optional<Path> chooseFile(String title, String approveButtonText, Path baseDirectory, boolean appendMissingExtension,
                              FileExtensionsFilter... filters);

    /**
     * Ask user to choose a file.
     *
     * @param title                  dialog title
     * @param approveButtonText      approve button text (e.g. "Open", "Save", ...)
     * @param baseDirectory          Base directory of the dialog (where will the dialog point to)
     * @param appendMissingExtension Whether extension from selected filter should be appended to selected file if it
     *                               has no extension
     * @param filters                supported file filters
     * @return Selected file if provided, or Optional.empty() if user cancelled the dialog
     */
    Optional<Path> chooseFile(String title, String approveButtonText, Path baseDirectory, boolean appendMissingExtension,
                              List<FileExtensionsFilter> filters);
}
