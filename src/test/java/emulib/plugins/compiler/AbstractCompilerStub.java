/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
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
class AbstractCompilerStub extends AbstractCompiler {

    AbstractCompilerStub() {
        super(0L);
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
