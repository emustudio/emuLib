/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler.antlr;

import net.emustudio.emulib.plugin.compiler.CompileException;
import net.emustudio.emulib.plugin.compiler.SourceCodePosition;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.Objects;

/**
 * ANTLR error listener which throws {@link CompileException} on first syntax error.
 */
public class ParserErrorListener extends BaseErrorListener {
    private final String fileName;

    /**
     * @param fileName source file name used in error positions
     */
    public ParserErrorListener(String fileName) {
        this.fileName = Objects.requireNonNull(fileName);
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        throw new CompileException(new SourceCodePosition(line, charPositionInLine, fileName), msg, e);
    }
}
