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
 * Base class for emuStudio dialogs.
 * <p>
 * Subclasses implement {@link #initializeComponents()} and call {@link #buildContent()} as the last statement
 * of their constructor. The dialog is packed, centered on its parent, disposed on close, and closed by ESC
 * (unless {@link #shouldCloseOnEscape()} returns {@code false}). A disposed dialog can be shown again.
 */
public abstract class DialogBase extends JDialog {

    // Primary ESC handler: key listeners run before component key bindings, so ESC is caught before
    // e.g. JTable's "cancel" or JSpinner's "reset-field-edit" consumes it.
    private final KeyListener escapeKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && closeOnEscape()) {
                e.consume();
            }
        }
    };

    /**
     * @param parent parent window; {@code null} uses the shared Swing owner frame
     * @param title  dialog title
     * @param modal  whether the dialog is application-modal
     */
    protected DialogBase(Window parent, String title, boolean modal) {
        super(parent == null ? JOptionPane.getRootFrame() : parent, title,
                modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GUI.relayoutOnShow(this);

        // Fallback ESC handler for dialogs without focusable components; fires even when nothing has focus.
        getRootPane().registerKeyboardAction(e -> closeOnEscape(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Disposes the dialog and removes the ESC key listeners.
     */
    @Override
    public void dispose() {
        GUI.removeKeyListenerRecursively(this, escapeKeyListener);
        super.dispose();
    }

    /**
     * Shows or hides the dialog; showing re-registers ESC key listeners removed by {@link #dispose()}.
     *
     * @param visible whether to show the dialog
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            GUI.addKeyListenerRecursively(this, escapeKeyListener);
        }
        super.setVisible(visible);
    }

    /**
     * Checked on every ESC press; return {@code false} to let ESC propagate (e.g. to an emulated keyboard).
     *
     * @return whether ESC closes the dialog (default {@code true})
     */
    protected boolean shouldCloseOnEscape() {
        return true;
    }

    /**
     * Adds content from {@link #initializeComponents()}, packs and centers the dialog, and installs ESC handling.
     * Must be the last statement of the subclass constructor.
     */
    protected void buildContent() {
        JComponent content = initializeComponents();
        if (content != null) {
            // A focusable component is needed for key events to be delivered at all.
            content.setFocusable(true);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);
            pack();
        }
        setLocationRelativeTo(getParent());
        GUI.addKeyListenerRecursively(this, escapeKeyListener);
    }

    /**
     * @return main content component, or {@code null} for an empty dialog
     */
    protected abstract JComponent initializeComponents();

    private boolean closeOnEscape() {
        boolean close = shouldCloseOnEscape();
        if (close) {
            dispose();
        }
        return close;
    }
}
