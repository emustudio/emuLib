// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;
import static org.easymock.EasyMock.createNiceMock;

import java.io.Reader;
import java.util.List;

@PluginRoot(
        type = PLUGIN_TYPE.COMPILER,
        title = "title"
)
class AbstractCompilerStub extends AbstractCompiler {

    AbstractCompilerStub() {
        super(0L, createNiceMock(ApplicationApi.class), createNiceMock(PluginSettings.class));
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
    public int getProgramLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SourceFileExtension> getSourceFileExtensions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShowSettingsSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCopyright() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    public void testError() {
        notifyError("Some error");
    }
    
    public void testInfo() {
        notifyInfo("Some info");
    }
    
    public void testWarning() {
        notifyWarning("Some warning");
    }
    
}
