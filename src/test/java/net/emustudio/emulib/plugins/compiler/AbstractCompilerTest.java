/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.compiler.CompilerMessage.MessageType;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class AbstractCompilerTest {
    private AbstractCompilerStub compiler;

    @Before
    public void setUp() {
        compiler = new AbstractCompilerStub();
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", compiler.getTitle());
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
    public void testNotifyCompilerFinish() {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onFinish();
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileFinish();
        verify(listener);
    }

    @Test
    public void testNotifyOnMessage() {
        CompilerListener listener = createMock(CompilerListener.class);
        listener.onMessage(anyObject(CompilerMessage.class));
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyOnMessage(createMock(CompilerMessage.class));

        verify(listener);
    }

    @Test
    public void testNotifyInfo() {
        CompilerListener listener = createMock(CompilerListener.class);
        Capture<CompilerMessage> captured = Capture.newInstance();
        listener.onMessage(and(
                isA(CompilerMessage.class),
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
        Capture<CompilerMessage> captured = Capture.newInstance();
        listener.onMessage(and(
                isA(CompilerMessage.class),
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
        Capture<CompilerMessage> captured = Capture.newInstance();
        listener.onMessage(and(
                isA(CompilerMessage.class),
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
