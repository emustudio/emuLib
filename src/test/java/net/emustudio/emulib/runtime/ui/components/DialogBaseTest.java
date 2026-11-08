/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static org.junit.Assert.*;

public class DialogBaseTest {
    private TestDialog dialog;

    private static class TestDialog extends DialogBase {
        private final JComponent content;
        private final boolean closeOnEscape;
        private int disposeCount;

        TestDialog(Window parent, boolean modal, JComponent content, boolean closeOnEscape) {
            super(parent, "Test Dialog", modal);
            this.content = content;
            this.closeOnEscape = closeOnEscape;
            buildContent();
        }

        @Override
        protected JComponent initializeComponents() {
            return content;
        }

        @Override
        protected boolean shouldCloseOnEscape() {
            return closeOnEscape;
        }

        @Override
        public void dispose() {
            disposeCount++;
            super.dispose();
        }
    }

    @Before
    public void setUp() {
        Assume.assumeFalse("Skipping: headless environment", GraphicsEnvironment.isHeadless());
    }

    @After
    public void tearDown() {
        if (dialog != null) {
            dialog.dispose();
        }
    }

    private TestDialog createDialog(JComponent content) {
        return dialog = new TestDialog(null, false, content, true);
    }

    @Test
    public void testNullParentUsesSharedOwnerFrame() {
        createDialog(new JPanel());

        assertSame(JOptionPane.getRootFrame(), dialog.getOwner());
        assertEquals("Test Dialog", dialog.getTitle());
        assertEquals(JDialog.DISPOSE_ON_CLOSE, dialog.getDefaultCloseOperation());
    }

    @Test
    public void testDialogParentIsOwner() {
        JDialog parent = new JDialog();
        dialog = new TestDialog(parent, false, new JPanel(), true);

        assertSame(parent, dialog.getOwner());
        parent.dispose();
    }

    @Test
    public void testModality() {
        assertTrue(new TestDialog(null, true, null, true).isModal());
        assertFalse(createDialog(null).isModal());
    }

    @Test
    public void testBuildContentAddsFocusableContentAndPacks() {
        JPanel content = new JPanel();
        content.setFocusable(false);
        content.setPreferredSize(new Dimension(300, 200));
        createDialog(content);

        Container contentPane = dialog.getContentPane();
        assertTrue(contentPane.getLayout() instanceof BorderLayout);
        assertEquals(1, contentPane.getComponentCount());
        assertSame(content, contentPane.getComponent(0));
        assertTrue(content.isFocusable());
        assertTrue(dialog.getWidth() >= 300 && dialog.getHeight() >= 200);
    }

    @Test
    public void testNullContentAddsNothing() {
        createDialog(null);

        assertEquals(0, dialog.getContentPane().getComponentCount());
    }

    @Test
    public void testEscKeyListenerIsRegisteredOnWholeHierarchyOnce() {
        JPanel content = new JPanel();
        JPanel nested = new JPanel();
        JButton button = new JButton("Click");
        nested.add(button);
        content.add(nested);
        createDialog(content);
        int count = keyListenerCount(button);

        dialog.buildContent();

        assertTrue(keyListenerCount(content) > 0);
        assertTrue(keyListenerCount(nested) > 0);
        assertTrue(count > 0);
        assertEquals(count, keyListenerCount(button));
    }

    @Test
    public void testDisposeRemovesAndShowingRestoresKeyListeners() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        createDialog(content);
        int initialCount = keyListenerCount(button);

        dialog.dispose();
        int afterDispose = keyListenerCount(button);
        assertTrue(afterDispose < initialCount);

        dialog.setVisible(false);
        assertEquals(afterDispose, keyListenerCount(button));

        dialog.setVisible(true);
        assertEquals(initialCount, keyListenerCount(button));
        dialog.setVisible(false);
    }

    @Test
    public void testEscapeKeyListenerClosesDialog() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        createDialog(content);
        KeyEvent esc = new KeyEvent(button, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);

        for (var listener : button.getKeyListeners()) {
            listener.keyPressed(esc);
        }

        assertTrue(esc.isConsumed());
        assertEquals(1, dialog.disposeCount);
    }

    @Test
    public void testEscapeRootPaneBindingClosesDialog() {
        createDialog(new JPanel());

        escapeAction(dialog).actionPerformed(null);

        assertEquals(1, dialog.disposeCount);
    }

    @Test
    public void testEscapeDoesNotCloseWhenDisabled() {
        dialog = new TestDialog(null, false, new JPanel(), false);

        escapeAction(dialog).actionPerformed(null);

        assertEquals(0, dialog.disposeCount);
    }

    private static Action escapeAction(JDialog dialog) {
        Object binding = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        return dialog.getRootPane().getActionMap().get(binding);
    }

    private static int keyListenerCount(Component component) {
        return component.getKeyListeners().length;
    }
}
