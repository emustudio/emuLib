package emulib.plugins.cpu;

import emulib.plugins.cpu.CPU.CPUListener;
import emulib.plugins.cpu.CPU.RunState;
import emulib.plugins.cpu.stubs.AbstractCPUStub;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractCPUTest {
    private AbstractCPUStub cpu;

    @Before
    public void setUp() {
        cpu = new AbstractCPUStub(0L);
    }

    @After
    public void tearDown() {
        cpu.destroy();
    }

    private CPUListener createCPUListenerMock(RunState runState) {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(runState));
        expectLastCall().once();
        replay(listener);
        return listener;
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullPluginIDThrows() throws Exception {
        new AbstractCPUStub(null);
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

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testNotifyInternalStateChangeDoesNotThrow() throws Exception {
        CPUListener listener = EasyMock.createNiceMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().andThrow(new RuntimeException()).once();
        listener.runStateChanged(RunState.STATE_RUNNING);
        expectLastCall().anyTimes();
        replay(listener);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testNotifyRunStateChangeDoesNotThrow() throws Exception {
        CPUListener listener = EasyMock.createNiceMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().anyTimes();
        listener.runStateChanged(RunState.STATE_RUNNING);
        expectLastCall().andThrow(new RuntimeException()).once();
        replay(listener);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testNotifyChangeDoesNotCallObserverAfterItsRemoval() {
        CPUListener listener = EasyMock.createNiceMock(CPUListener.class);
        replay(listener);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.removeCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testRunCalledWhenCPUIsExecuted() throws InterruptedException {
        cpu.reset();
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

    @Test
    public void testCPUisDestroyedAfterCrazyRequestsAndThenDestroyCalled() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    cpu.execute();
                }
            });
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    cpu.stop();
                }
            });
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    cpu.step();
                }
            });
        }
        cpu.destroy();

        try {
            cpu.execute();
            fail("Expected RuntimeException");
        } catch (Exception e) {
            // expected, because CPU is destroyed
        }
    }

    @Test
    public void testPauseAfterExecuteSetsBreakpointState() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_RUNNING));
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_STOPPED_BREAK));
        expectLastCall().once();
        replay(listener);

        cpu.setLoopUntilThreadIsInterrupted(true);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();
        cpu.pause();

        verify(listener);
    }

    @Test
    public void testPauseOnBreakpointStateHasNoEffect() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        replay(listener);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.pause();

        verify(listener);
    }

    @Test
    public void testPauseOnNormalStopHasNoEffect() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        replay(listener);

        cpu.addCPUListener(listener);
        cpu.pause();

        verify(listener);
    }

    @Test
    public void testCallingStepWhileRunningHasNoEffect() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_RUNNING);

        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithRunningStateSetBreakpointState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_BREAK);

        cpu.setRunStateToReturn(RunState.STATE_RUNNING);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithNormalStopStateKeepsNormalStopState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_NORMAL);

        cpu.setRunStateToReturn(RunState.STATE_STOPPED_NORMAL);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithBreakpointStateKeepsBreakpointState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_BREAK);

        cpu.setRunStateToReturn(RunState.STATE_STOPPED_BREAK);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_ADDR_FALLOUT);

        cpu.setExceptionToThrow(new IndexOutOfBoundsException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithArrayIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_ADDR_FALLOUT);

        cpu.setExceptionToThrow(new ArrayIndexOutOfBoundsException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithRuntimeExceptionWithCauseIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_ADDR_FALLOUT);

        cpu.setExceptionToThrow(new RuntimeException(new ArrayIndexOutOfBoundsException()));
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testStepWithOtherExceptionSetsInstructionFalloutState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_STOPPED_BAD_INSTR);

        cpu.setExceptionToThrow(new RuntimeException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.step();

        verify(listener);
    }

    @Test
    public void testExecuteWithRunningStateHasNoEffect() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        replay(listener);

        cpu.setRunStateToReturn(RunState.STATE_RUNNING);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testExecuteWithNormalStopHasNoEffect() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        replay(listener);

        cpu.setRunStateToReturn(RunState.STATE_STOPPED_NORMAL);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testExecuteWithBreakpointStateChangeToRunningState() throws Exception {
        CPUListener listener = createCPUListenerMock(RunState.STATE_RUNNING);

        cpu.setRunStateToReturn(RunState.STATE_STOPPED_BREAK);
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        verify(listener);
    }

    @Test
    public void testExecuteWithIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_RUNNING));
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_STOPPED_ADDR_FALLOUT));
        expectLastCall().once();
        replay(listener);

        cpu.setExceptionToThrow(new IndexOutOfBoundsException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        LockSupport.parkNanos(100000000);

        verify(listener);
    }

    @Test
    public void testExecuteWithRuntimeExceptionWithCauseIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_RUNNING));
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_STOPPED_ADDR_FALLOUT));
        expectLastCall().once();
        replay(listener);

        cpu.setExceptionToThrow(new RuntimeException(new IndexOutOfBoundsException()));
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        LockSupport.parkNanos(100000000);

        verify(listener);
    }

    @Test
    public void testExecuteWithArrayIndexOutOfBoundsExceptionSetsAddressFalloutState() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_RUNNING));
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_STOPPED_ADDR_FALLOUT));
        expectLastCall().once();
        replay(listener);

        cpu.setExceptionToThrow(new ArrayIndexOutOfBoundsException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        LockSupport.parkNanos(100000000);

        verify(listener);
    }

    @Test
    public void testExecuteWithOtherExceptionSetsInstructionFalloutState() throws Exception {
        CPUListener listener = createMock(CPUListener.class);
        listener.internalStateChanged();
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_RUNNING));
        expectLastCall().once();
        listener.runStateChanged(eq(RunState.STATE_STOPPED_BAD_INSTR));
        expectLastCall().once();
        replay(listener);

        cpu.setExceptionToThrow(new RuntimeException());
        cpu.reset();
        cpu.addCPUListener(listener);
        cpu.execute();

        LockSupport.parkNanos(100000000);

        verify(listener);
    }


}
