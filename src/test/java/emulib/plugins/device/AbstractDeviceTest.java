package emulib.plugins.device;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class AbstractDeviceTest {
    private AbstractDeviceStub device;

    @Before
    public void setUp() {
        device = new AbstractDeviceStub(0L);
    }

    @Test
    public void testInitializeDoesNotThrow() throws Exception {
        device.initialize(null);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", device.getTitle());
    }

}
