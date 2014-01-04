package emulib.runtime;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class LoggerImplTest {
    private Logger slf4jLogger;
    private LoggerImpl logger;

    @Before
    public void setUp() {
        slf4jLogger = createNiceMock(Logger.class);
        logger = new LoggerImpl(slf4jLogger);
    }

    @After
    public void tearDown() {
        verify(slf4jLogger);
    }

    @Test
    public void testInfo_String() {
        String message = "message";

        slf4jLogger.info(message);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.info(message);
    }

    @Test
    public void testDebug_String() {
        String message = "message";

        slf4jLogger.debug(message);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.debug(message);
    }

    @Test
    public void testError_String() {
        String message = "message";

        slf4jLogger.error(message);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.error(message);
    }

    @Test
    public void testWarning_String() {
        String message = "message";

        slf4jLogger.warn(message);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.warning(message);
    }

    @Test
    public void testInfo_String_Throwable() {
        String message = "message";
        Throwable exception = new Throwable();

        slf4jLogger.info(message, exception);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.info(message, exception);
    }

    @Test
    public void testDebug_String_Throwable() {
        String message = "message";
        Throwable exception = new Throwable();

        slf4jLogger.debug(message, exception);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.debug(message, exception);
    }

    @Test
    public void testError_String_Throwable() {
        String message = "message";
        Throwable exception = new Throwable();

        slf4jLogger.error(message, exception);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.error(message, exception);
    }

    @Test
    public void testWarning_String_Throwable() {
        String message = "message";
        Throwable exception = new Throwable();

        slf4jLogger.warn(message, exception);
        expectLastCall().once();
        replay(slf4jLogger);

        logger.warning(message, exception);
    }

}
