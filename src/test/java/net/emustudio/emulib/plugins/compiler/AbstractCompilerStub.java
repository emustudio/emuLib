/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

import javax.swing.*;
import java.util.List;

import static org.easymock.EasyMock.createNiceMock;

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
    public LexicalAnalyzer createLexer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FileExtension> getSourceFileExtensions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showSettings(JFrame parent) {
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

    @Override
    public boolean isAutomationSupported() {
        return false;
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
