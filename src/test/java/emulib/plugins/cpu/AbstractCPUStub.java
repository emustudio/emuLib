package emulib.plugins.cpu;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.plugins.PluginInitializationException;

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

    public AbstractCPUStub(Long id) {
        super(id);
    }

    @Override
    protected void destroyInternal() {
    }

    @Override
    protected RunState stepInternal() {
        throw new UnsupportedOperationException();
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
    protected void requestStop() {

    }

    @Override
    public RunState call() {
        runCalled = true;
        latch.countDown();
        return RunState.STATE_STOPPED_NORMAL;
    }

    public boolean wasRunCalled() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return runCalled;
    }

}
