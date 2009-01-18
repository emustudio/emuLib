/*
 * StaticDialogs.java
 *
 * (c) Copyright 2008-2009, P.Jakubčo
 * 
 * Created on 17.6.2008, 12:36:08
 * 
 * KISS, YAGNI
 *
 */
package runtime;

/**
 * This class offers static methods that show some messages on the screen.
 * Plugins should use these methods for displaying messages. Example of use:
 * <code>
 *     StaticDialogs.showMessage("Hello, world!");
 * </code>
 */
public class StaticDialogs {
    /**
     * Show error message as <code>JOptionPane</code> dialog. Title will be "Error".
     * @param message error message to show
     */
    public static void showErrorMessage(String message) {
        javax.swing.JOptionPane.showMessageDialog(null,
                message,"Error",javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show information message as <code>JOptionPane</code> dialog. Title will
     * be "Message".
     * @param message information message to show
     */
    public static void showMessage(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Message",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Get communication model version number. It's a part of its identification
     * for plugins. Plugins can identify the model and if they are not compatible
     * with it, they can raise an error.
     * 
     * @return major version number
     */
    public int getModelVersion () { return 3; }
    
    /**
     * Get communication model minor version number. It's a part of its identification
     * for plugins. Plugins can identify the model and if they are not compatible
     * with it, they can raise an error.
     * 
     * @return minor version number
     */
    public int getModelMinor () { return 5; }
    

}
