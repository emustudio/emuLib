package emulib.plugins.cpu;

public class AbstractDebugColumnStub extends AbstractDebugColumn {

    public AbstractDebugColumnStub(String title, Class<?> classType, boolean editable) {
        super(title, classType, editable);
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
