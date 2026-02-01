/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Base class for dialogs.
 * Provides common functionality like ESC key to close.
 * <p>
 * Call buildContent() at the end of your constructor to build and display the content.
 */
public abstract class DialogBase extends JDialog {

    protected DialogBase(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        initDialog();
    }

    protected DialogBase(Dialog parent, String title, boolean modal) {
        super(parent, title, modal);
        initDialog();
    }

    private void initDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ESC key closes dialog
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
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
    }

    /**
     * Initialize components in the dialog which should be put in a main component. Called by buildContent().
     *
     * @return The main content component of the dialog.
     */
    protected abstract JComponent initializeComponents();
}
