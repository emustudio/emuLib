/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import net.emustudio.emulib.runtime.ui.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Base class for dialogs.
 * Provides common functionality like ESC key to close.
 * <p>
 * Call buildContent() at the end of your constructor to build and display the content.
 */
public abstract class DialogBase extends JDialog {

    private final KeyListener escapeKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && shouldCloseOnEscape()) {
                e.consume();
                dispose();
            }
        }
    };

    /**
     * Constructs a new DialogBase.
     *
     * @param parent parent frame
     * @param title  dialog title
     * @param modal  whether the dialog is modal
     */
    protected DialogBase(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Constructs a new DialogBase.
     *
     * @param parent parent dialog
     * @param title  dialog title
     * @param modal  whether the dialog is modal
     */
    protected DialogBase(Dialog parent, String title, boolean modal) {
        super(parent, title, modal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Determines whether pressing ESC should close this dialog.
     * Subclasses can override this to prevent ESC from closing the dialog
     * (e.g., when ESC is needed for other purposes like emulated keyboard input).
     *
     * @return true if ESC should close the dialog (default), false otherwise
     */
    protected boolean shouldCloseOnEscape() {
        return true;
    }

    @Override
    public void dispose() {
        GUI.removeKeyListenerRecursively(this, escapeKeyListener);
        super.dispose();
    }

    /**
     * Call this method at the end of your constructor to build and display the content.
     * This calls initializeComponents(), adds it to the dialog, packs it, and centers it.
     */
    protected void buildContent() {
        JComponent content = initializeComponents();
        if (content != null) {
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);
            pack();
        }
        setLocationRelativeTo(getParent());
        GUI.addKeyListenerRecursively(this, escapeKeyListener);
    }

    /**
     * Initialize components in the dialog which should be put in a main component. Called by buildContent().
     *
     * @return The main content component of the dialog.
     */
    protected abstract JComponent initializeComponents();
}
