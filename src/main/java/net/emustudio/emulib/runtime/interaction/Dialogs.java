/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.runtime.interaction;


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
     * @return integer number entered by user, or 0 by default
     * @throws NumberFormatException if the number format is wrong
     */
    int readInteger(String message);

    /**
     * Asks user for integer input.
     *
     * The supported format is the same as when calling {@link Integer#decode(String)}.
     *
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return integer number entered by user, or the initial value by default
     * @throws NumberFormatException if the number format is wrong
     */
    int readInteger(String message, String title, int initial);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @return a String entered by user, or empty string by default
     */
    String readString(String message);

    /**
     * Ask user for String input.
     *
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return a String entered by user, or the initial value by default
     */
    String readString(String message, String title, String initial);

    /**
     * Ask user for Double input.
     *
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @return double number entered by user, or 0.0 by default
     * @throws NumberFormatException if the number format is wrong
     */
    double readDouble(String message);

    /**
     * Asks user for Double input.
     *
     * The supported format is the same as when calling {@link Double#parseDouble(String)}.
     *
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return double number entered by user, or the initial value by default
     * @throws NumberFormatException if the number format is wrong
     */
    double readDouble(String message, String title, double initial);

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
