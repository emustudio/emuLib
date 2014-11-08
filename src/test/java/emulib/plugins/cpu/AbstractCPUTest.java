package emulib.plugins.cpu;

import emulib.plugins.cpu.CPU.CPUListener;
import emulib.plugins.cpu.CPU.RunState;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractCPUTest {
    private AbstractCPUStub cpu;

    @Before
    public void setUp() {
        cpu = new AbstractCPUStub(0L);
    }

    private CPUListener createCPUListenerMock(RunState runState) {
        CPUListener listener = EasyMock.createNiceMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(runState));
        expectLastCall().once();
        replay(listener);
        return listener;
    }

    @Test
    public void testInitializeDoesNotThrow() throws Exception {
        cpu.initialize(null);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", cpu.getTitle());
    }

    @Test
    public void testBreakpointIsSupported() {
        assertTrue(cpu.isBreakpointSupported());
    }

    @Test
    public void testAddGetBreakpoint() {
        cpu.setBreakpoint(3);
        assertTrue(cpu.isBreakpointSet(3));
        cpu.unsetBreakpoint(3);
        assertFalse(cpu.isBreakpointSet(3));
    }

    @Test
    public void testNotifyChange() {
        CPUListener listener = createCPUListenerMock(RunState.STATE_RUNNING);

        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testNotifyChangeDoesNotCallObserverAfterItsRemoval() {
        CPUListener listener = EasyMock.createNiceMock(CPUListener.class);
        replay(listener);

        cpu.addCPUListener(listener);
        cpu.removeCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testRunCalledWhenCPUIsExecuted() throws InterruptedException {
        cpu.execute();
        assertTrue(cpu.wasRunCalled());
    }

    @Test
    public void testStateIsNotifiedWhenResetIsCalled() {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_BREAK);

        cpu.addCPUListener(listener);
        cpu.reset();

        verify(listener);
    }
}
