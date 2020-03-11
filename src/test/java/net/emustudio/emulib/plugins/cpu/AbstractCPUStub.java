// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@PluginRoot(
        type = PLUGIN_TYPE.CPU,
        title = "title"
)
public class AbstractCPUStub extends AbstractCPU {
    private volatile boolean runCalled = false;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean shouldStop = false;

    private RunState runStateToReturn = RunState.STATE_STOPPED_NORMAL;
    private RuntimeException exceptionToThrow;

    public AbstractCPUStub(long id, ApplicationApi emustudio, PluginSettings settngs) {
        super(id, emustudio, settngs);
    }

    @Override
    protected void destroyInternal() {
    }

    public void setRunStateToReturn(RunState runStateToReturn) {
        this.runStateToReturn = runStateToReturn;
    }

    public void setExceptionToThrow(RuntimeException exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

    @Override
    protected RunState stepInternal() {
        throwIfSet();
        return runStateToReturn;
    }

    private void throwIfSet() {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
    }

    @Override
    public JPanel getStatusPanel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInstructionLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setInstructionLocation(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Disassembler getDisassembler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void resetInternal(int startPos) { }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCopyright() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RunState call() {
        runCalled = true;
        shouldStop = false;
        latch.countDown();
        throwIfSet();

        while (!Thread.currentThread().isInterrupted() && !shouldStop) {
            LockSupport.parkNanos(10000);
        }

        return runStateToReturn;
    }

    public boolean wasRunCalled() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return runCalled;
    }

    public void stopSpontaneously() {
        this.shouldStop = true;
    }
}
