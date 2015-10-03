package emulib.plugins.cpu;

import static org.junit.Assert.assertEquals;

import emulib.plugins.cpu.stubs.AbstractDebugColumnStub;
import org.junit.Test;

public class AbstractDebugColumnTest {

    @Test
    public void testGetValues() {
        String title = "title";
        Class<?> classType = Short.class;
        boolean editable = false;

        AbstractDebugColumn instance = new AbstractDebugColumnStub(
                title,
                classType,
                editable
        );

        assertEquals(title, instance.getTitle());
        assertEquals(classType, instance.getClassType());
        assertEquals(editable, instance.isEditable());
    }

}
