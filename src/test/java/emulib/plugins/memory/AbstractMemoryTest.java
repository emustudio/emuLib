package emulib.plugins.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class AbstractMemoryTest {
    private AbstractMemoryStub memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryStub(0L);
    }

    @Test
    public void testInitializeIsAlwaysSuccessful() {
        assertTrue(memory.initialize(null));
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

}
