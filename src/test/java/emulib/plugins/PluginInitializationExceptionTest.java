package emulib.plugins;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertSame;

public class PluginInitializationExceptionTest {

    @Test(expected = NullPointerException.class)
    public void testNullPluginThrows() throws Exception {
        new PluginInitializationException(null);
    }

    @Test(expected = NullPointerException.class)
    public void testMessageNullPluginThrows() throws Exception {
        new PluginInitializationException(null, "message");
    }

    @Test
    public void testNullMessageValidPluginDoesNotThrow() throws Exception {
        new PluginInitializationException(createMock(Plugin.class), (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void testMessageThrowableNullPluginThrows() throws Exception {
        new PluginInitializationException(null, "message", new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testThrowableNullPluginThrows() throws Exception {
        new PluginInitializationException(null, new Exception());
    }

    @Test
    public void testGetPluginWorks() throws Exception {
        Plugin plugin = createMock(Plugin.class);
        PluginInitializationException exception = new PluginInitializationException(plugin);

        assertSame(plugin, exception.getPlugin());
    }
}