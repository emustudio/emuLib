package emulib.plugins.device;

import emulib.emustudio.SettingsManager;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

public class AbstractDeviceTest {
    private AbstractDeviceStub device;

    @Before
    public void setUp() {
        device = new AbstractDeviceStub(0L);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullPluginIDThrows() throws Exception {
        new AbstractDeviceStub(null);
    }

    @Test(expected = NullPointerException.class)
    public void testInitializeWithNullParameterThrows() throws Exception {
        device.initialize(null);
    }

    @Test
    public void testInitializeWithCorrectParameterDoesNotThrow() throws Exception {
        device.initialize(createMock(SettingsManager.class));
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", device.getTitle());
    }

    @Test
    public void testResetForIncreasingCoverage() throws Exception {
        device.reset();
    }
}
