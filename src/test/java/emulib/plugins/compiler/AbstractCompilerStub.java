package emulib.plugins.compiler;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import java.io.Reader;

@PluginType(
        type = PLUGIN_TYPE.COMPILER,
        title = "title",
        copyright = "copyright",
        description = "description"
)
public class AbstractCompilerStub extends AbstractCompiler {

    public AbstractCompilerStub(Long aa) {
        super(aa);
    }

    @Override
    public boolean compile(String inputFileName, String outputFileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compile(String inputFileName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public LexicalAnalyzer getLexer(Reader in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SourceFileExtension[] getSourceSuffixList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void showSettings() {
    }

    @Override
    public boolean isShowSettingsSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }
        
    public void testError(String msg) {
        notifyError(msg);
    }
    
    public void testInfo(String msg) {
        notifyInfo(msg);
    }
    
    public void testWarning(String msg) {
        notifyWarning(msg);
    }
    
}
