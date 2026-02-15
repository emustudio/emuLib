/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import net.emustudio.emulib.runtime.ui.Dialogs;
import org.easymock.EasyMock;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class BrowseButtonTest {

    @Test
    public void testConstructorForDirectory() {
        Dialogs dialogs = createMock(Dialogs.class);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose Directory", "Select", path -> {});
        
        assertNotNull(button);
        assertEquals("Browse...", button.getText());
    }

    @Test
    public void testConstructorForFile() {
        Dialogs dialogs = createMock(Dialogs.class);
        FileExtensionsFilter filter = new FileExtensionsFilter("Text files", "txt");
        
        BrowseButton button = new BrowseButton(dialogs, "Choose File", "Open", false, path -> {}, filter);
        
        assertNotNull(button);
        assertEquals("Browse...", button.getText());
    }

    @Test
    public void testDirectorySelectionInvokesConsumer() {
        Dialogs dialogs = createMock(Dialogs.class);
        Path selectedPath = Path.of("/test/directory");
        boolean[] consumerCalled = {false};
        
        expect(dialogs.chooseDirectory(
            eq("Choose Directory"), 
            eq("Select"), 
            anyObject(Path.class)
        )).andReturn(Optional.of(selectedPath));
        
        replay(dialogs);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose Directory", "Select", path -> {
            consumerCalled[0] = true;
            assertEquals(selectedPath, path);
        });
        
        button.doClick();
        
        verify(dialogs);
        assertTrue(consumerCalled[0]);
    }

    @Test
    public void testFileSelectionInvokesConsumer() {
        Dialogs dialogs = createMock(Dialogs.class);
        Path selectedPath = Path.of("/test/file.txt");
        boolean[] consumerCalled = {false};
        FileExtensionsFilter filter = new FileExtensionsFilter("Text files", "txt");
        
        expect(dialogs.chooseFile(
            eq("Choose File"), 
            eq("Open"), 
            anyObject(Path.class),
            eq(false),
            eq(filter)
        )).andReturn(Optional.of(selectedPath));
        
        replay(dialogs);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose File", "Open", false, path -> {
            consumerCalled[0] = true;
            assertEquals(selectedPath, path);
        }, filter);
        
        button.doClick();
        
        verify(dialogs);
        assertTrue(consumerCalled[0]);
    }

    @Test
    public void testCancelledDialogDoesNotInvokeConsumer() {
        Dialogs dialogs = createMock(Dialogs.class);
        boolean[] consumerCalled = {false};
        
        expect(dialogs.chooseDirectory(
            eq("Choose Directory"), 
            eq("Select"), 
            anyObject(Path.class)
        )).andReturn(Optional.empty());
        
        replay(dialogs);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose Directory", "Select", path -> {
            consumerCalled[0] = true;
        });
        
        button.doClick();
        
        verify(dialogs);
        assertFalse(consumerCalled[0]);
    }

    @Test
    public void testPathCacheRemembersLastSelection() {
        Dialogs dialogs = createMock(Dialogs.class);
        Path firstPath = Path.of("/first");
        Path secondPath = Path.of("/second");
        
        // First selection
        expect(dialogs.chooseDirectory(
            anyString(), 
            anyString(), 
            anyObject(Path.class)
        )).andReturn(Optional.of(firstPath));
        
        // Second selection should receive first path as current directory
        expect(dialogs.chooseDirectory(
            anyString(), 
            anyString(), 
            eq(firstPath)
        )).andReturn(Optional.of(secondPath));
        
        replay(dialogs);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose", "Select", path -> {});
        
        button.doClick();
        button.doClick();
        
        verify(dialogs);
    }

    @Test
    public void testMultipleFileFilters() {
        Dialogs dialogs = createMock(Dialogs.class);
        FileExtensionsFilter filter1 = new FileExtensionsFilter("Text files", "txt");
        FileExtensionsFilter filter2 = new FileExtensionsFilter("Java files", "java");
        
        expect(dialogs.chooseFile(
            anyString(), 
            anyString(), 
            anyObject(Path.class),
            eq(true),
            eq(filter1),
            eq(filter2)
        )).andReturn(Optional.empty());
        
        replay(dialogs);
        
        BrowseButton button = new BrowseButton(dialogs, "Choose", "Open", true, path -> {}, filter1, filter2);
        button.doClick();
        
        verify(dialogs);
    }
}
