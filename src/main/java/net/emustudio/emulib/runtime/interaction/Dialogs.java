// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime.interaction;

import java.util.Optional;

/**
 * This class provides dialogs used by emuStudio and plugins.
 *
 * If GUI is enabled, the dialogs are shown on the screen. Otherwise, they are logged.
 *
 * Example of usage:
 * <code>
 *     dialogs.showMessage("Hello, world!");
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
     * @param title title of the message
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
     * @param title title of the message
     */
    void showInfo(String message, String title);

    /**
     * Ask user for integer input.
     *
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @return integer number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Integer> readInteger(String message);

    /**
     * Ask user for integer input.
     *
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @param title title of the input message
     * @return integer number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Integer> readInteger(String message, String title);

    /**
     * Ask user for integer input.
     *
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @param title title of the input message
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
     * @param title title of the input message
     * @return a String entered by user, or Optional.empty() if user cancelled the input dialog
     */
    Optional<String> readString(String message, String title);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return a String entered by user, or the initial value by default
     */
    Optional<String> readString(String message, String title, String initial);

    /**
     * Ask user for Double input.
     *
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @return double number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message);

    /**
     * Ask user for Double input.
     *
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @param title title of the input message
     * @return double number entered by user, or Optional.empty() if user cancelled the input dialog
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message, String title);

    /**
     * Ask user for Double input.
     *
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return double number entered by user, or the initial value by default
     * @throws NumberFormatException if the number format is wrong
     */
    Optional<Double> readDouble(String message, String title, double initial);

    /**
     * Ask user for a confirmation.
     *
     * User can choose between of YES/NO/CANCEL option.
     *
     * @param message message to show
     * @return {@link DialogAnswer#ANSWER_YES} or {@link DialogAnswer#ANSWER_NO} or {@link DialogAnswer#ANSWER_CANCEL}
     * */
    DialogAnswer ask(String message);

    /**
     * Ask user for a confirmation.
     *
     * User can choose between of YES/NO/CANCEL option.
     *
     * @param message message to show
     * @param title title of the message
     * @return {@link DialogAnswer#ANSWER_YES} or {@link DialogAnswer#ANSWER_NO} or {@link DialogAnswer#ANSWER_CANCEL}
     */
    DialogAnswer ask(String message, String title);
}
