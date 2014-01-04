package emulib.plugins.memory;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Before;
import org.junit.Test;

public class AbstractMemoryContextTest {
    private AbstractMemoryContext memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryContextStub();
    }

    @Test
    public void testListenerIsCalledAfterNotifyChange() {
        int memoryPosition = 199;

        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        listener.memoryChanged(eq(memoryPosition));
        expectLastCall().once();
        replay(listener);

        memory.addMemoryListener(listener);
        memory.notifyMemoryChanged(memoryPosition);

        verify(listener);
    }

    @Test
    public void testListenerIsNotCalledAfterItsRemoval() {
        Memory.MemoryListener listener = EasyMock.createNiceMock(Memory.MemoryListener.class);
        replay(listener);

        memory.addMemoryListener(listener);
        memory.removeMemoryListener(listener);
        memory.notifyMemoryChanged(234);

        verify(listener);
    }

}
