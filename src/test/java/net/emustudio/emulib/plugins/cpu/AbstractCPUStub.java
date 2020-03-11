/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
