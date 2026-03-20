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

    private DialogBase dialog;

    @Before
    public void setUp() {
        Assume.assumeFalse("Skipping: headless environment", GraphicsEnvironment.isHeadless());
    }

    @After
    public void tearDown() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    private DialogBase createDialog(JComponent content, boolean modal) {
        return new DialogBase((Frame) null, "Test Dialog", modal) {
            @Override
            protected JComponent initializeComponents() {
                return content;
            }
        };
    }

    private DialogBase createDialog(JComponent content) {
        return createDialog(content, false);
    }

    private DialogBase createDialogWithParentDialog(JComponent content) {
        return new DialogBase(new JDialog(), "Test Dialog", false) {
            @Override
            protected JComponent initializeComponents() {
                return content;
            }
        };
    }

    @Test
    public void testDialogIsCreatedWithFrameParent() {
        dialog = createDialog(new JPanel());
        dialog.buildContent();

        assertEquals("Test Dialog", dialog.getTitle());
        assertEquals(JDialog.DISPOSE_ON_CLOSE, dialog.getDefaultCloseOperation());
    }

    @Test
    public void testDialogIsCreatedWithDialogParent() {
        dialog = createDialogWithParentDialog(new JPanel());
        dialog.buildContent();

        assertEquals("Test Dialog", dialog.getTitle());
        assertEquals(JDialog.DISPOSE_ON_CLOSE, dialog.getDefaultCloseOperation());
    }

    @Test
    public void testModalDialog() {
        dialog = createDialog(new JPanel(), true);
        dialog.buildContent();

        assertTrue(dialog.isModal());
    }

    @Test
    public void testNonModalDialog() {
        dialog = createDialog(new JPanel(), false);
        dialog.buildContent();

        assertFalse(dialog.isModal());
    }

    @Test
    public void testBuildContentAddsContentToDialog() {
        JPanel content = new JPanel();
        dialog = createDialog(content);
        dialog.buildContent();

        Container contentPane = dialog.getContentPane();
        assertTrue(contentPane.getLayout() instanceof BorderLayout);
        assertEquals(1, contentPane.getComponentCount());
        assertSame(content, contentPane.getComponent(0));
    }

    @Test
    public void testBuildContentMakesContentFocusable() {
        JPanel content = new JPanel();
        content.setFocusable(false);

        dialog = createDialog(content);
        dialog.buildContent();

        assertTrue(content.isFocusable());
    }

    @Test
    public void testBuildContentWithNullContent() {
        dialog = new DialogBase((Frame) null, "Test", false) {
            @Override
            protected JComponent initializeComponents() {
                return null;
            }
        };

        // Should not throw
        dialog.buildContent();
    }

    @Test
    public void testBuildContentWithNullContentDoesNotAddComponent() {
        dialog = new DialogBase((Frame) null, "Test", false) {
            @Override
            protected JComponent initializeComponents() {
                return null;
            }
        };
        dialog.buildContent();

        assertEquals(0, dialog.getContentPane().getComponentCount());
    }

    @Test
    public void testBuildContentPacksDialog() {
        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(300, 200));
        dialog = createDialog(content);
        dialog.buildContent();

        // After pack(), the dialog size should accommodate the content
        assertTrue(dialog.getWidth() > 0);
        assertTrue(dialog.getHeight() > 0);
    }

    @Test
    public void testEscKeyListenerIsRegisteredOnContent() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        dialog = createDialog(content);
        dialog.buildContent();

        assertTrue(keyListenerCount(button) > 0);
        assertTrue(keyListenerCount(content) > 0);
    }

    @Test
    public void testDisposeRemovesKeyListeners() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        dialog = createDialog(content);
        dialog.buildContent();

        int buttonListenersBefore = keyListenerCount(button);
        assertTrue(buttonListenersBefore > 0);

        dialog.dispose();

        assertTrue(keyListenerCount(button) < buttonListenersBefore);
        dialog = null; // prevent double dispose in tearDown
    }

    @Test
    public void testSetVisibleTrueReRegistersKeyListeners() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        dialog = createDialog(content);
        dialog.buildContent();

        int initialCount = keyListenerCount(button);

        dialog.dispose();
        int afterDisposeCount = keyListenerCount(button);
        assertTrue(afterDisposeCount < initialCount);

        // setVisible(true) re-registers them (duplicate check prevents double-registration)
        dialog.setVisible(true);
        int afterShowCount = keyListenerCount(button);
        assertEquals(initialCount, afterShowCount);

        dialog.setVisible(false);
    }

    @Test
    public void testSetVisibleFalseDoesNotReRegisterKeyListeners() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        dialog = createDialog(content);
        dialog.buildContent();

        dialog.dispose();
        int afterDisposeCount = keyListenerCount(button);

        dialog.setVisible(false);
        assertEquals(afterDisposeCount, keyListenerCount(button));
        dialog = null;
    }

    @Test
    public void testShouldCloseOnEscapeDefaultsToTrue() {
        dialog = createDialog(new JPanel());
        dialog.buildContent();

        assertTrue(dialog.shouldCloseOnEscape());
    }

    @Test
    public void testShouldCloseOnEscapeCanBeOverridden() {
        dialog = new DialogBase((Frame) null, "Test", false) {
            @Override
            protected boolean shouldCloseOnEscape() {
                return false;
            }

            @Override
            protected JComponent initializeComponents() {
                return new JPanel();
            }
        };
        dialog.buildContent();

        assertFalse(dialog.shouldCloseOnEscape());
    }

    @Test
    public void testEscapeKeyViaRootPaneBinding() {
        dialog = createDialog(new JPanel());
        dialog.buildContent();

        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object binding = inputMap.get(escStroke);
        assertNotNull("ESC key should be bound in WHEN_IN_FOCUSED_WINDOW input map", binding);

        ActionMap actionMap = dialog.getRootPane().getActionMap();
        assertNotNull("Action for ESC binding should exist", actionMap.get(binding));
    }

    @Test
    public void testEscapeDoesNotCloseWhenShouldCloseOnEscapeReturnsFalse() {
        boolean[] disposed = {false};
        dialog = new DialogBase((Frame) null, "Test", false) {
            @Override
            protected boolean shouldCloseOnEscape() {
                return false;
            }

            @Override
            protected JComponent initializeComponents() {
                return new JPanel();
            }

            @Override
            public void dispose() {
                disposed[0] = true;
                super.dispose();
            }
        };
        dialog.buildContent();

        // Simulate ESC via root pane action
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        Object binding = inputMap.get(escStroke);
        ActionMap actionMap = dialog.getRootPane().getActionMap();
        Action action = actionMap.get(binding);
        action.actionPerformed(null);

        assertFalse("Dialog should not be disposed when shouldCloseOnEscape returns false", disposed[0]);
    }

    @Test
    public void testEscapeClosesDialogWhenShouldCloseOnEscapeReturnsTrue() {
        boolean[] disposed = {false};
        dialog = new DialogBase((Frame) null, "Test", false) {
            @Override
            protected JComponent initializeComponents() {
                return new JPanel();
            }

            @Override
            public void dispose() {
                disposed[0] = true;
                super.dispose();
            }
        };
        dialog.buildContent();

        // Simulate ESC via root pane action
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        Object binding = inputMap.get(escStroke);
        ActionMap actionMap = dialog.getRootPane().getActionMap();
        Action action = actionMap.get(binding);
        action.actionPerformed(null);

        assertTrue("Dialog should be disposed when shouldCloseOnEscape returns true", disposed[0]);
        dialog = null; // already disposed
    }

    @Test
    public void testDefaultCloseOperationIsDisposeOnClose() {
        dialog = createDialog(new JPanel());

        assertEquals(JDialog.DISPOSE_ON_CLOSE, dialog.getDefaultCloseOperation());
    }

    @Test
    public void testBuildContentWithComplexComponentHierarchy() {
        JPanel content = new JPanel();
        JPanel nested = new JPanel();
        JButton button = new JButton("Click");
        JTextField textField = new JTextField("text");
        nested.add(button);
        nested.add(textField);
        content.add(nested);

        dialog = createDialog(content);
        dialog.buildContent();

        assertTrue(keyListenerCount(content) > 0);
        assertTrue(keyListenerCount(nested) > 0);
        assertTrue(keyListenerCount(button) > 0);
        assertTrue(keyListenerCount(textField) > 0);
    }

    @Test
    public void testDuplicateBuildContentDoesNotDoubleRegisterListeners() {
        JButton button = new JButton("Test");
        JPanel content = new JPanel();
        content.add(button);
        dialog = createDialog(content);
        dialog.buildContent();

        int countAfterFirstBuild = keyListenerCount(button);

        // Call buildContent again (simulating accidental double-call)
        dialog.buildContent();

        // Duplicate check in addKeyListenerRecursively should prevent double-registration
        assertEquals(countAfterFirstBuild, keyListenerCount(button));
    }

    private int keyListenerCount(Component component) {
        return component.getKeyListeners().length;
    }
}
