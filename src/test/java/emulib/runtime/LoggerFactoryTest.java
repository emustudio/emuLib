package emulib.runtime;

import emulib.runtime.interfaces.Logger;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class LoggerFactoryTest {

    @Test
    public void testGetLoggerNotNull() {
        Class identificationClass = LoggerFactoryTest.class;
        assertNotNull(LoggerFactory.getLogger(identificationClass));
    }

    @Test
    public void testGetLoggerTwoTimesSameInstance() {
        Class identificationClass = LoggerFactoryTest.class;
        Logger logger = LoggerFactory.getLogger(identificationClass);
        assertSame(logger, LoggerFactory.getLogger(identificationClass));
    }
}
