// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.device;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractDeviceTest {
    private AbstractDeviceStub device;

    @Before
    public void setUp() {
        device = new AbstractDeviceStub(0L);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", device.getTitle());
    }
}
