package emulib.plugins.compiler;

import emulib.plugins.compiler.Compiler.CompilerListener;
import emulib.plugins.compiler.Message.MessageType;
import org.easymock.Capture;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class AbstractCompilerTest {
    private final static long ID = System.currentTimeMillis();
    private TestableAbstractCompiler compiler;
    
    @Before
    public void setUp() {
        compiler = new TestableAbstractCompiler(ID);
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
        CompilerListener r = createNiceMock(CompilerListener.class);
        assertTrue(compiler.addCompilerListener(r));
        assertTrue(compiler.removeCompilerListener(r));
    }

    @Test
    public void testAddCompilerListenerTwice() {
        CompilerListener r = createNiceMock(CompilerListener.class);
        compiler.addCompilerListener(r);
        assertFalse(compiler.addCompilerListener(r));
    }
    
    @Test
    public void testNotifyCompilerStart() {
        CompilerListener listener = createNiceMock(CompilerListener.class);
        listener.onStart();
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileStart();
        verify(listener);
    }
    
    @Test
    public void testNotifyCompilerFinish() {
        int errorCode = 5;
        
        CompilerListener listener = createNiceMock(CompilerListener.class);
        listener.onFinish(EasyMock.eq(errorCode));
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyCompileFinish(errorCode);
        verify(listener);
    }
    
    @Test
    public void testNotifyOnMessage() {        
        CompilerListener listener = createNiceMock(CompilerListener.class);
        listener.onMessage(anyObject(Message.class));
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.notifyOnMessage(createNiceMock(Message.class));

        verify(listener);
    }
    
    @Test
    public void testNotifyInfo() {
        CompilerListener listener = createNiceMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testInfo("Some info");

        assertEquals(MessageType.TYPE_INFO, captured.getValue().getMessageType());
        verify(listener);
    }

    @Test
    public void testNotifyError() {
        CompilerListener listener = createNiceMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testError("Some error");

        assertEquals(MessageType.TYPE_ERROR, captured.getValue().getMessageType());
        verify(listener);
    }
    
    @Test
    public void testNotifyWarning() {
        CompilerListener listener = createNiceMock(CompilerListener.class);
        Capture<Message> captured = new Capture<>();
        listener.onMessage(and(
                isA(Message.class),
                capture(captured))
        );
        expectLastCall().once();
        replay(listener);

        compiler.addCompilerListener(listener);
        compiler.testWarning("Some warning");
        
        assertEquals(MessageType.TYPE_WARNING, captured.getValue().getMessageType());
        verify(listener);
    }
    

}
