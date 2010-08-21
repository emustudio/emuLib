/*
 * StaticDialogs.java
 *
 * (c) Copyright 2008-2010, P.Jakubčo <pjakubco@gmail.com>
 * 
 * Created on 17.6.2008, 12:36:08
 * 
 * KISS, YAGNI
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package runtime;

import javax.swing.JOptionPane;

/**
 * This class offers static methods that show some messages on the screen.
 * Plugins should use these methods for displaying messages. Example of use:
 * <code>
 *     StaticDialogs.showMessage("Hello, world!");
 * </code>
 */
public class StaticDialogs {
    private final static String InputDialogMSG = "Please insert a value";

    /** YES option for the confirm message dialogs */
    public final static int YES_OPTION = JOptionPane.YES_OPTION;
    /** NO option for the confirm message dialogs */
    public final static int NO_OPTION = JOptionPane.NO_OPTION;
    /** CANCEL option for the confirm message dialogs */
    public final static int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

    /**
     * Show error message as <code>JOptionPane</code> dialog. Title will be "Error".
     * @param message error message to show
     */
    public static void showErrorMessage(String message) {
        showErrorMessage(message, "Error");
    }

    /**
     * Show error message as <code>JOptionPane</code> dialog.
     * @param message error message to show
     * @param title title of the dialog
     */
    public static void showErrorMessage(String message, String title) {
        try {
            JOptionPane.showMessageDialog(null,
                    message,title,javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            System.out.println(title + ": " + message);
        }
    }

    /**
     * Show information message as <code>JOptionPane</code> dialog.
     * Title will be "Information message".
     * @param message information message to show
     */
    public static void showMessage(String message) {
        showMessage(message, "Information message");
    }

    /**
     * Show information message as <code>JOptionPane</code> dialog.
     * @param message information message to show
     * @param title title of the message
     */
    public static void showMessage(String message, String title) {
        try {
            JOptionPane.showMessageDialog(null, message,
                title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception e) {
            System.out.println(title + ": " + message);
        }
    }

    /**
     * Show dialog and asks user for integer input.
     * @param message message to show
     * @return null if user inserts bad value
     *         integral input otherwise
     */
    public static Integer inputIntValue(String message) {
        return inputIntValue(message,InputDialogMSG,0);
    }

    /**
     * Show dialog and asks user for integer input.
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return null if user inserts bad value
     *         integral input otherwise
     */
    public static Integer inputIntValue(String message, String title, int initial) {
        try {
            String s = (String)JOptionPane.showInputDialog(null, message, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initial);
            return Integer.decode(s);
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Show dialog and asks user for String input.
     * @param message message to show
     * @return null if method gets HeadlessException
     *         string input otherwise
     */
    public static String inputStringValue(String message) {
        return inputStringValue(message, InputDialogMSG, "");
    }

    /**
     * Show dialog and asks user for String input.
     * @param message message to show
     * @param title title of the input message
     * @param initial initial value
     * @return null if method gets HeadlessException
     *         string input otherwise
     */
    public static String inputStringValue(String message, String title, String initial) {
        try {
            String s = (String)JOptionPane.showInputDialog(null, message, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initial);
            return s;
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Show dialog and asks user for Double input.
     * @param message message to show
     * @return null if user inserts bad value
     *         double input otherwise
     */
    public static Double inputDoubleValue(String message) {
        return inputDoubleValue(message, InputDialogMSG, 0);
    }

    /**
     * Show dialog and asks user for Double input.
     * @param message message to show
     * @param title title of the message
     * @param initial initial value
     * @return null if user inserts bad value
     *         double input otherwise
     */
    public static Double inputDoubleValue(String message, String title,
            double initial) {
        try {
            String s = (String)JOptionPane.showInputDialog(null, message, title,
                JOptionPane.QUESTION_MESSAGE, null, null, initial);
            return Double.parseDouble(s);
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Show confirmation dialog. User can choose one of YES/NO or CANCEL option.
     * @param message message to show
     * @return CANCEL_OPTION if something goes wrong
     *         YES_OPTION or NO_OPTION or CANCEL_OPTION otherwise
     */
    public static int confirmMessage(String message) {
        return confirmMessage(message, "Confirmation");
    }

    /**
     * Show confirmation dialog. User can choose one of YES/NO or CANCEL option.
     * @param message message to show
     * @param title title of the message
     * @return CANCEL_OPTION if something goes wrong
     *         YES_OPTION or NO_OPTION or CANCEL_OPTION otherwise
     */
    public static int confirmMessage(String message, String title) {
        try {
            return JOptionPane.showConfirmDialog(null,
                message, title, JOptionPane.YES_NO_CANCEL_OPTION);
        } catch (Exception e) {
            return CANCEL_OPTION;
        }
    }

}
