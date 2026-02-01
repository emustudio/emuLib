/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
    public void compile(Path inputPath, Optional<Path> outputPath) {
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
