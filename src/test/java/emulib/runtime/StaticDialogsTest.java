package emulib.runtime;

import emulib.emustudio.APITest;
import java.net.MalformedURLException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class StaticDialogsTest {

    @Before
    public void setUp() throws MalformedURLException {
        APITest.assignEmuStudioPassword();
    }

    @Test
    public void testSetGetGuiSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(true, APITest.getEmuStudioPassword());
        assertTrue(StaticDialogs.isGUISupported());
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());
        assertFalse(StaticDialogs.isGUISupported());
    }

    @Test
    public void testInputReturnsNullWhenGUIisNotSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());

        assertNull(StaticDialogs.inputDoubleValue("A"));
        assertNull(StaticDialogs.inputDoubleValue("A", "B", 0.4));
        assertNull(StaticDialogs.inputIntValue("A"));
        assertNull(StaticDialogs.inputIntValue("A", "B", 3));
        assertNull(StaticDialogs.inputStringValue("A"));
        assertNull(StaticDialogs.inputStringValue("A", "B", "C"));
    }

    @Test
    public void testShowDialogsDontShowNothingWhenGUIisNotSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());

        StaticDialogs.confirmMessage("A");
        StaticDialogs.confirmMessage("A", "B");
        StaticDialogs.showErrorMessage("A");
        StaticDialogs.showErrorMessage("A", "B");
        StaticDialogs.showMessage("A");
        StaticDialogs.showMessage("A", "B");
    }


}
