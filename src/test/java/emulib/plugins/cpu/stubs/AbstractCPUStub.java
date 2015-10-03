package emulib.plugins.cpu.stubs;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.plugins.PluginInitializationException;
import emulib.plugins.cpu.AbstractCPU;
import emulib.plugins.cpu.Disassembler;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@PluginType(
        type = PLUGIN_TYPE.CPU,
        title = "title",
        copyright = "copyright",
        description = "description"
)
public class AbstractCPUStub extends AbstractCPU {
    private volatile boolean runCalled = false;
    private final CountDownLatch latch = new CountDownLatch(1);

    private RunState runStateToReturn = RunState.STATE_STOPPED_NORMAL;
    private RuntimeException exceptionToThrow;
    private boolean loopUntilThreadIsInterrupted;

    public AbstractCPUStub(Long id) {
        super(id);
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

    public void setLoopUntilThreadIsInterrupted(boolean loopUntilThreadIsInterrupted) {
        this.loopUntilThreadIsInterrupted = loopUntilThreadIsInterrupted;
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
    public int getInstructionPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setInstructionPosition(int pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Disassembler getDisassembler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize(SettingsManager settingsManager) throws PluginInitializationException {
    }

    @Override
    public void showSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShowSettingsSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RunState call() {
        runCalled = true;
        latch.countDown();
        throwIfSet();

        while (loopUntilThreadIsInterrupted && !Thread.currentThread().isInterrupted()) {

        }

        return runStateToReturn;
    }

    public boolean wasRunCalled() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return runCalled;
    }

}
