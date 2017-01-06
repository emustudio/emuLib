/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2017, Peter Jakubčo
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
package emulib.plugins.compiler;

import emulib.plugins.compiler.Compiler.CompilerListener;
import emulib.plugins.compiler.Message.MessageType;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractCompilerTest {
    private AbstractCompilerStub compiler;

    @Before
    public void setUp() {
        compiler = new AbstractCompilerStub();
    }

    @Test
    public void testInitializeDoesNotThrow() throws Exception {
        compiler.initialize(null);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", compiler.getTitle());
    }

    @Test
    public void testGetProgramStartOnUnusedCompiler() {
        assertEquals(0, compiler.getProgramStartAddress());
    }

    @Test
    public void testAddAndRemoveCompilerListener() {
        CompilerListener r = createMock(CompilerListener.class);
        assertTrue(compiler.addCompilerListener(r));
        assertTrue(compiler.removeCompilerListener(r));
    }

    @Test
    public void testAddCompilerListenerTwice() {
        CompilerListener r = createMock(CompilerListener.class);
        compiler.addCompilerListener(r);
        assertFalse(compiler.addCompilerListener(r));
    }

    @Test
    public void testNotifyCompilerStart() {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onStart();
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileStart();
        verify(listener);
    }

    @Test
    public void testNotifyCompilerStartDoesNotThrow() throws Exception {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onStart();
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileStart();
        verify(listener);
    }


    @Test
    public void testNotifyCompilerFinish() {
        int errorCode = 5;

        CompilerListener listener = createMock(CompilerListener.class);
        listener.onFinish(EasyMock.eq(errorCode));
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileFinish(errorCode);
        verify(listener);
    }

    @Test
    public void testNotifyCompilerFinishDoesNotThrow() throws Exception {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onFinish(anyInt());
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileFinish(5);
        verify(listener);
    }


    @Test
    public void testNotifyOnMessage() {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onMessage(anyObject(Message.class));
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyOnMessage(createMock(Message.class));

        verify(listener);
    }

    @Test
    public void testNotifyOnMessageDoesNotThrow() throws Exception {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onMessage(anyObject(Message.class));
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyOnMessage(createMock(Message.class));

        verify(listener);
    }

    @Test
    public void testNotifyInfo() {
        CompilerListener listener = createMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testInfo();

        assertEquals(MessageType.TYPE_INFO, captured.getValue().getMessageType());
        verify(listener);
    }

    @Test
    public void testNotifyError() {
        CompilerListener listener = createMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testError();

        assertEquals(MessageType.TYPE_ERROR, captured.getValue().getMessageType());
        verify(listener);
    }

    @Test
    public void testNotifyWarning() {
        CompilerListener listener = createMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testWarning();

        assertEquals(MessageType.TYPE_WARNING, captured.getValue().getMessageType());
        verify(listener);
    }

}
