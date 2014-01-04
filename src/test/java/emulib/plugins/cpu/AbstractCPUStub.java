package emulib.plugins.cpu;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;

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
    public void step() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
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
    public void destroy() {
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
    public void run() {
        runCalled = true;
        latch.countDown();
    }

    public boolean wasRunCalled() throws InterruptedException {
        latch.await(10, TimeUnit.SECONDS);
        return runCalled;
    }

}
