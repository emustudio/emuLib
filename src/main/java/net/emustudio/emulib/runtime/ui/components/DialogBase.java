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
     * @param parent parent frame, or {@code null}
     * @param title  dialog title
     * @param modal  whether the dialog is modal
     */
    protected DialogBase(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        initDialog();
    }

    /**
     * @param parent parent dialog
     * @param title  dialog title
     * @param modal  whether the dialog is modal
     */
    protected DialogBase(Dialog parent, String title, boolean modal) {
        super(parent, title, modal);
        initDialog();
    }

    private void initDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Fallback: WHEN_IN_FOCUSED_WINDOW handles ESC for dialogs with no focusable components (e.g. AboutDialog).
        // It fires via postProcessKeyEvent even when no component has focus.
        getRootPane().registerKeyboardAction(
                e -> {
                    if (shouldCloseOnEscape()) {
                        dispose();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
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
     * Adds content from {@link #initializeComponents()}, packs and centers the dialog, and installs ESC handling.
     * Must be the last statement of the subclass constructor.
     */
    protected void buildContent() {
        JComponent content = initializeComponents();
        if (content != null) {
            // Ensure there's always a focusable component in the dialog so that key events
            // are delivered. Without this, dialogs with only non-focusable components (e.g.
            // JLabels) would never receive keyboard input.
            content.setFocusable(true);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);
            pack();
        }
        setLocationRelativeTo(getParent());

        // Primary: KeyListener fires before component-level key bindings, catching ESC before
        // JTable's "cancel" or JSpinner's "reset-field-edit" can consume it. If consumed here,
        // the WHEN_IN_FOCUSED_WINDOW fallback above won't double-fire.
        GUI.addKeyListenerRecursively(this, escapeKeyListener);
    }

    /**
     * @return main content component, or {@code null} for an empty dialog
     */
    protected abstract JComponent initializeComponents();
}
