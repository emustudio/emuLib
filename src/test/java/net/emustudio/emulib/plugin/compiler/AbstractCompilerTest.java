/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler;

import net.emustudio.emulib.plugin.annotations.PluginRoot;
import net.emustudio.emulib.plugin.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.application.ApplicationApi;
import net.emustudio.emulib.io.FileType;
import net.emustudio.emulib.application.settings.PluginSettings;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class AbstractCompilerTest {

    @PluginRoot(type = PLUGIN_TYPE.COMPILER, title = "title")
    static class TestCompiler extends AbstractCompiler {
        Exception toThrow;
        Path output;

        TestCompiler(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
            super(pluginID, applicationApi, settings);
        }

        @Override
        protected void compile(Path input, Path output) throws Exception {
            this.output = output;
            warning("warn");
            if (toThrow != null) throw toThrow;
        }

        @Override
        protected String getOutputExtension() {
            return "hex";
        }

        @Override
        public LexicalAnalyzer createLexer() {
            return null;
        }

        @Override
        public List<FileType> getSourceFileTypes() {
            return List.of(new FileType("Assembler", "asm"));
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String getCopyright() {
            return "";
        }
    }

    private final TestCompiler compiler = new TestCompiler(0, ApplicationApi.UNAVAILABLE, PluginSettings.UNAVAILABLE);
    private final List<String> events = new ArrayList<>();

    {
        compiler.addCompilerListener(new CompilerListener() {
            @Override
            public void onStart() {
                events.add("start");
            }

            @Override
            public void onMessage(CompilerMessage message) {
                events.add(message.getType() + ":" + message.getFormattedMessage());
            }

            @Override
            public void onFinish() {
                events.add("finish");
            }
        });
    }

    @Test
    public void testSuccessfulWorkflowDerivesOutputPath() {
        compiler.compile(Path.of("dir/program.asm"), Optional.empty());

        assertEquals(Path.of("dir/program.hex"), compiler.output);
        assertEquals(List.of("start", "INFO:title, version 1.0", "WARNING:warn", "finish"), events);
    }

    @Test
    public void testGivenOutputPathIsUsed() {
        compiler.compile(Path.of("program.asm"), Optional.of(Path.of("out.bin")));
        assertEquals(Path.of("out.bin"), compiler.output);
    }

    @Test
    public void testCompileExceptionIsReportedWithPosition() {
        compiler.toThrow = new CompileException(new SourceCodePosition(1, 2, "f.asm"), "bad");
        compiler.compile(Path.of("program.asm"), Optional.empty());

        assertEquals("ERROR:" + new SourceCodePosition(1, 2, "f.asm") + " bad", events.get(3));
        assertEquals("finish", events.get(4));
    }

    @Test
    public void testOtherExceptionIsReportedAsError() {
        compiler.toThrow = new IOException("io");
        compiler.compile(Path.of("program.asm"), Optional.empty());

        assertEquals("ERROR:Compilation error: java.io.IOException: io", events.get(3));
    }

    @Test
    public void testRemovedListenerIsNotNotified() {
        TestCompiler other = new TestCompiler(0, ApplicationApi.UNAVAILABLE, PluginSettings.UNAVAILABLE);
        CompilerListener listener = m -> events.add("message");
        other.addCompilerListener(listener);
        other.removeCompilerListener(listener);
        other.compile(Path.of("program.asm"), Optional.empty());
        assertEquals(List.of(), events);
    }

    @Test
    public void testCommandLineReturnsErrorExitCodeWithoutInput() {
        assertEquals(1, AbstractCompiler.runCommandLine("test", new String[0], TestCompiler::new));
    }

    @Test
    public void testCommandLineReturnsErrorExitCodeOnCompileError() {
        assertEquals(1, AbstractCompiler.runCommandLine("test", new String[]{"nonexistent.asm"},
                (id, api, settings) -> {
                    TestCompiler c = new TestCompiler(id, api, settings);
                    c.toThrow = new IOException("io");
                    return c;
                }));
    }

    @Test
    public void testCommandLineSucceeds() {
        assertEquals(0, AbstractCompiler.runCommandLine("test", new String[]{"-o", "x.bin", "a.asm"}, TestCompiler::new));
    }
}
