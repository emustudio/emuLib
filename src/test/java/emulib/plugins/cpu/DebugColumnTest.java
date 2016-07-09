package emulib.plugins.cpu;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DebugColumnTest {

    @Test
    public void testGetDefaultWidthReturnsMinusOne() throws Exception {
        assertEquals(-1, new DebugColumnStub().getDefaultWidth());
    }

    private final class DebugColumnStub implements DebugColumn {

        @Override
        public Class getClassType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTitle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEditable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDebugValue(int location, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getDebugValue(int location) {
            throw new UnsupportedOperationException();
        }
    }
}
