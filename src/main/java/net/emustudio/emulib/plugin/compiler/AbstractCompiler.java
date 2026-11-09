/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler;

import net.emustudio.emulib.plugin.AbstractPlugin;
import net.emustudio.emulib.application.ApplicationApi;
import net.emustudio.emulib.io.FileType;
import net.emustudio.emulib.application.settings.PluginSettings;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Compiler base. Implements listeners and the compilation workflow:
 * <ol>
 *     <li>listeners are notified about the start; title and version are reported</li>
 *     <li>output path is resolved (replacing source file extension by {@link #getOutputExtension()})</li>
 *     <li>{@link #compile(Path, Path)} is called; {@link CompileException} or any other exception is reported
 *     as an error</li>
 *     <li>listeners are notified about the finish</li>
 * </ol>
 */
public abstract class AbstractCompiler extends AbstractPlugin implements Compiler {
    private final Set<CompilerListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Creates new instance.
     *
     * @param pluginID       plugin ID
     * @param applicationApi emuStudio API
     * @param settings       plugin custom settings
     */
    public AbstractCompiler(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        super(pluginID, applicationApi, settings);
    }

    /**
     * Compiles the input file into the output file.
     * <p>
     * Errors can be reported either by {@link #error(SourceCodePosition, String)} or by throwing an exception.
     *
     * @param input  input file path (source code)
     * @param output output file path
     * @throws Exception on compilation error
     */
    protected abstract void compile(Path input, Path output) throws Exception;

    /**
     * Output file extension (without dot), used when output path is not given.
     *
     * @return output file extension, e.g. "hex"
     */
    protected abstract String getOutputExtension();

    @Override
    public final void compile(Path inputPath, Optional<Path> outputPath) {
        listeners.forEach(CompilerListener::onStart);
        try {
            info(getTitle() + ", version " + getVersion());
            compile(inputPath, outputPath.orElseGet(
                    () -> FileType.replaceExtension(inputPath, getSourceFileTypes(), getOutputExtension())));
        } catch (CompileException e) {
            error(e.position, e.getMessage());
        } catch (Exception e) {
            error("Compilation error: " + e);
        } finally {
            listeners.forEach(CompilerListener::onFinish);
        }
    }

    @Override
    public void addCompilerListener(CompilerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCompilerListener(CompilerListener listener) {
        listeners.remove(listener);
    }

    protected void error(String message) {
        error(null, message);
    }

    /**
     * @param position source position, or null
     * @param message  message
     */
    protected void error(SourceCodePosition position, String message) {
        report(new CompilerMessage(CompilerMessage.Type.ERROR, message, position));
    }

    protected void warning(String message) {
        report(new CompilerMessage(CompilerMessage.Type.WARNING, message, null));
    }

    protected void info(String message) {
        report(new CompilerMessage(CompilerMessage.Type.INFO, message, null));
    }

    private void report(CompilerMessage message) {
        listeners.forEach(listener -> listener.onMessage(message));
    }

    /**
     * Compiler plugin constructor.
     */
    @FunctionalInterface
    public interface Factory {
        Compiler create(long pluginID, ApplicationApi applicationApi, PluginSettings settings);
    }

    /**
     * Command-line runner for compilers. Usage: {@code [-o outputFile] inputFile}, {@code --version}, {@code --help}.
     * Messages are printed to standard error output.
     *
     * @param jarName jar file name used in help
     * @param args    command-line arguments
     * @param factory compiler constructor, e.g. {@code MyCompiler::new}
     * @return exit code: 0 if successful, 1 if there were errors
     */
    public static int runCommandLine(String jarName, String[] args, Factory factory) {
        Compiler compiler = factory.create(0L, ApplicationApi.UNAVAILABLE, PluginSettings.UNAVAILABLE);
        String outputFile = null;
        int i = 0;
        for (; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if ((arg.equals("--output") || arg.equals("-o")) && i + 1 < args.length) {
                outputFile = args[++i];
            } else if (arg.equals("--help") || arg.equals("-h")) {
                System.out.println("Syntax: java -jar " + jarName + ".jar [-o outputFile] inputFile\nOptions:\n" +
                        "\t--output, -o\tfile: name of the output file\n" +
                        "\t--version, -v\t: print version\n" +
                        "\t--help, -h\t: this help");
                return 0;
            } else if (arg.equals("--version") || arg.equals("-v")) {
                System.out.println(compiler.getVersion());
                return 0;
            } else {
                break;
            }
        }
        if (i >= args.length) {
            System.err.println("Error: expected input file name");
            return 1;
        }
        boolean[] failed = new boolean[1];
        compiler.addCompilerListener(message -> {
            failed[0] |= message.getType() == CompilerMessage.Type.ERROR;
            System.err.println(message.getFormattedMessage());
        });
        compiler.compile(Path.of(args[i]), Optional.ofNullable(outputFile).map(Path::of));
        return failed[0] ? 1 : 0;
    }
}
