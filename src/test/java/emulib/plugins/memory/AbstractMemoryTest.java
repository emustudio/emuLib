package emulib.plugins.memory;

import emulib.emustudio.SettingsManager;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

public class AbstractMemoryTest {
    private AbstractMemoryStub memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryStub(0L);
    }

    @Test(expected = NullPointerException.class)
    public void testInitializeWithThrows() throws Exception {
        memory.initialize(null);
    }

    @Test
    public void testInitializeWithCorrectParameterDoesNotThrow() throws Exception {
        memory.initialize(createMock(SettingsManager.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewInstanceWithNullIdThrows() throws Exception {
        new AbstractMemoryStub(null);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", memory.getTitle());
    }

    @Test
    public void testProgramStartIsZeroAfterStart() {
        assertEquals(0, memory.getProgramStart());
    }

    @Test
    public void testSetAndGetProgramStart() {
        memory.setProgramStart(555);
        assertEquals(555, memory.getProgramStart());
    }

    @Test
    public void testResetCallForIncreasingCoverage() throws Exception {
        memory.reset();
    }
}
