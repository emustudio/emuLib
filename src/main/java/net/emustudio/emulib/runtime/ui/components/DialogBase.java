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
 * Base class for emuStudio plugin dialogs.
 * <p>
 * Provides a standard dialog workflow with automatic ESC-to-close behavior, recursive key listener management,
 * proper dispose/re-show lifecycle, and content centering relative to the parent window.
 *
 * <h2>Workflow</h2>
 * <ol>
 *   <li>Subclass {@code DialogBase}, choosing a {@link Frame} or {@link Dialog} parent constructor.</li>
 *   <li>Implement {@link #initializeComponents()} to build and return the dialog's main content panel.</li>
 *   <li>Call {@link #buildContent()} as the <strong>last statement</strong> of your constructor. This:
 *       <ul>
 *         <li>Invokes {@link #initializeComponents()} to obtain the content.</li>
 *         <li>Makes the content focusable (so key events are delivered even if it only contains non-focusable
 *             components like {@link JLabel}s).</li>
 *         <li>Adds the content to the dialog's content pane with {@link BorderLayout#CENTER}.</li>
 *         <li>Calls {@link #pack()} to size the dialog to its preferred size.</li>
 *         <li>Centers the dialog relative to its parent.</li>
 *         <li>Installs the ESC key listener recursively on all child components.</li>
 *       </ul>
 *   </li>
 *   <li>Show the dialog with {@link #setVisible(boolean) setVisible(true)}.</li>
 * </ol>
 *
 * <h2>Capabilities</h2>
 * <ul>
 *   <li><strong>ESC-to-close:</strong> Pressing ESC disposes the dialog. This is handled via two complementary
 *       mechanisms:
 *       <ul>
 *         <li><em>Primary:</em> A {@link KeyListener} installed recursively on the dialog and all its descendants
 *             (via {@link GUI#addKeyListenerRecursively}). It fires before component-level key bindings, so ESC is
 *             caught before Swing components like {@link JTable} or {@link JSpinner} can consume it.</li>
 *         <li><em>Fallback:</em> A root-pane keyboard action registered with {@link JComponent#WHEN_IN_FOCUSED_WINDOW}.
 *             This handles the case where no component has focus (e.g. a dialog containing only JLabels). If the
 *             primary listener already consumed the event, this fallback does not double-fire.</li>
 *       </ul>
 *   </li>
 *   <li><strong>Recursive key listener management:</strong> The ESC key listener is automatically added to all
 *       existing child components and to any components added dynamically later (via a {@link java.awt.event.ContainerListener}).
 *       On {@link #dispose()}, the listeners are recursively removed to prevent leaks.</li>
 *   <li><strong>Dispose/re-show lifecycle:</strong> Calling {@link #dispose()} removes all ESC key listeners.
 *       Calling {@link #setVisible(boolean) setVisible(true)} re-registers them, allowing a dialog instance to be
 *       safely re-shown after disposal. Duplicate registration is prevented internally.</li>
 *   <li><strong>Default close operation:</strong> Set to {@link JDialog#DISPOSE_ON_CLOSE}, so closing the dialog
 *       via the window manager's close button also releases its resources.</li>
 *   <li><strong>Null content support:</strong> If {@link #initializeComponents()} returns {@code null}, the dialog
 *       is still created and centered, but no content is added and {@link #pack()} is not called.</li>
 * </ul>
 *
 * <h2>Controlling ESC behavior with {@link #shouldCloseOnEscape()}</h2>
 * <p>
 * The {@link #shouldCloseOnEscape()} method is consulted <em>every time</em> the ESC key is pressed, not just once
 * at construction time. This means a subclass can change the return value dynamically based on runtime state
 * (e.g. whether an emulated keyboard is active, or whether a text field is being edited).
 * <p>
 * When the method returns {@code false}, the ESC key event is neither consumed nor acted upon — it propagates
 * normally and can be handled by other components.
 *
 * <h2>Usage examples</h2>
 *
 * <h3>Simple dialog</h3>
 * <pre>{@code
 * public class AboutDialog extends DialogBase {
 *
 *     public AboutDialog(Frame parent) {
 *         super(parent, "About", true);
 *         buildContent();
 *     }
 *
 *     @Override
 *     protected JComponent initializeComponents() {
 *         JPanel panel = new JPanel();
 *         panel.add(new JLabel("My Plugin v1.0"));
 *         return panel;
 *     }
 * }
 * }</pre>
 *
 * <h3>Conditionally preventing ESC from closing</h3>
 * <pre>{@code
 * public class KeyboardDialog extends DialogBase {
 *     private boolean keyboardActive = false;
 *
 *     public KeyboardDialog(Frame parent) {
 *         super(parent, "Emulated Keyboard", false);
 *         buildContent();
 *     }
 *
 *     @Override
 *     protected boolean shouldCloseOnEscape() {
 *         // When the emulated keyboard is active, ESC should be forwarded
 *         // to the emulator instead of closing the dialog.
 *         return !keyboardActive;
 *     }
 *
 *     public void setKeyboardActive(boolean active) {
 *         this.keyboardActive = active;
 *     }
 *
 *     @Override
 *     protected JComponent initializeComponents() {
 *         return new JPanel(); // keyboard UI
 *     }
 * }
 * }</pre>
 *
 * <h3>Re-showing a dialog after dispose</h3>
 * <pre>{@code
 * // Create once, show multiple times:
 * AboutDialog dialog = new AboutDialog(mainFrame);
 * dialog.setVisible(true);  // first show — ESC listeners are registered
 * // ... user presses ESC or closes the dialog — dispose() removes listeners
 * dialog.setVisible(true);  // re-show — ESC listeners are re-registered automatically
 * }</pre>
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
     * Constructs a new DialogBase with a {@link Frame} parent.
     * <p>
     * Sets the default close operation to {@link JDialog#DISPOSE_ON_CLOSE} and registers the root-pane fallback
     * ESC binding. Call {@link #buildContent()} as the last statement of the subclass constructor.
     *
     * @param parent the parent frame (may be {@code null} for an unowned dialog)
     * @param title  the dialog title shown in the title bar
     * @param modal  {@code true} to block input to other windows while this dialog is visible
     */
    protected DialogBase(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        initDialog();
    }

    /**
     * Constructs a new DialogBase with a {@link Dialog} parent.
     * <p>
     * Sets the default close operation to {@link JDialog#DISPOSE_ON_CLOSE} and registers the root-pane fallback
     * ESC binding. Call {@link #buildContent()} as the last statement of the subclass constructor.
     *
     * @param parent the parent dialog
     * @param title  the dialog title shown in the title bar
     * @param modal  {@code true} to block input to other windows while this dialog is visible
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
     * Determines whether pressing ESC should close (dispose) this dialog.
     * <p>
     * This method is called <em>on every ESC key press</em>, not cached at construction time. This allows subclasses
     * to return different values depending on runtime state. For example, an emulated-keyboard dialog can return
     * {@code false} while the keyboard is capturing input, and {@code true} otherwise.
     * <p>
     * When this method returns {@code false}:
     * <ul>
     *   <li>The ESC key event is <strong>not</strong> consumed — it propagates normally to other listeners and
     *       key bindings.</li>
     *   <li>The dialog is <strong>not</strong> disposed.</li>
     * </ul>
     * When this method returns {@code true} (the default):
     * <ul>
     *   <li>The ESC key event is consumed (preventing further processing by Swing components).</li>
     *   <li>{@link #dispose()} is called, which also removes the recursively-installed key listeners.</li>
     * </ul>
     *
     * @return {@code true} if ESC should close the dialog (default), {@code false} to let ESC propagate
     * @see #dispose()
     */
    protected boolean shouldCloseOnEscape() {
        return true;
    }

    /**
     * Disposes of the dialog and releases its resources.
     * <p>
     * Before calling {@code super.dispose()}, this method recursively removes the ESC key listener from the dialog
     * and all its descendants to prevent memory leaks. If the dialog is later re-shown via
     * {@link #setVisible(boolean) setVisible(true)}, the listeners are automatically re-registered.
     */
    @Override
    public void dispose() {
        GUI.removeKeyListenerRecursively(this, escapeKeyListener);
        super.dispose();
    }

    /**
     * Shows or hides this dialog.
     * <p>
     * When {@code visible} is {@code true}, the ESC key listener is re-registered recursively on all child
     * components. This supports the pattern of creating a dialog once and re-showing it after a previous
     * {@link #dispose()} call. Duplicate listener registration is prevented internally by
     * {@link GUI#addKeyListenerRecursively}.
     *
     * @param visible {@code true} to show the dialog, {@code false} to hide it
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            GUI.addKeyListenerRecursively(this, escapeKeyListener);
        }
        super.setVisible(visible);
    }

    /**
     * Builds and lays out the dialog content. <strong>Must be called as the last statement of the subclass
     * constructor.</strong>
     * <p>
     * This method performs the following steps in order:
     * <ol>
     *   <li>Calls {@link #initializeComponents()} to obtain the dialog's main content component.</li>
     *   <li>If the content is non-{@code null}:
     *       <ul>
     *         <li>Makes the content focusable so that key events are delivered even when the content contains only
     *             non-focusable components (e.g. {@link JLabel}s).</li>
     *         <li>Adds the content to the dialog's content pane using {@link BorderLayout#CENTER}.</li>
     *         <li>Calls {@link #pack()} to size the dialog to its preferred size.</li>
     *       </ul>
     *   </li>
     *   <li>Centers the dialog relative to its parent window.</li>
     *   <li>Installs the ESC key listener recursively on the dialog and all its descendants (including any
     *       components added dynamically in the future via a {@link java.awt.event.ContainerListener}).</li>
     * </ol>
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
     * Creates and returns the main content component of this dialog.
     * <p>
     * Called by {@link #buildContent()}. Subclasses should build their UI component tree and return the root
     * component. Return {@code null} if the dialog has no content (the dialog will still be created and centered).
     *
     * @return the main content component, or {@code null} for an empty dialog
     */
    protected abstract JComponent initializeComponents();
}
