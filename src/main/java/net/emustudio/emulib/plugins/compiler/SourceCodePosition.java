/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.jcip.annotations.Immutable;

import java.util.Objects;

/**
 * Position in the source code.
 */
@Immutable
public class SourceCodePosition {

    /**
     * Line
     */
    public final int line;
    /**
     * Column
     */
    public final int column;

    /**
     * File name
     */
    public final String fileName;

    /**
     * Gets line
     *
     * @return line
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets column
     *
     * @return column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Creates new SourceCodePosition object
     *
     * @param line     line
     * @param column   column
     * @param fileName file name (non-null)
     */
    public SourceCodePosition(int line, int column, String fileName) {
        this.line = line;
        this.column = column;
        this.fileName = Objects.requireNonNull(fileName);
    }

    /**
     * Creates new SourceCodePosition object
     *
     * @param line     line
     * @param column   column
     * @param fileName file name (non-null)
     * @return SourceCodePosition object
     */
    public static SourceCodePosition of(int line, int column, String fileName) {
        return new SourceCodePosition(line, column, fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceCodePosition that = (SourceCodePosition) o;
        return line == that.line && column == that.column && Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column, fileName);
    }

    @Override
    public String toString() {
        return "[line=" + line + ", column=" + column + ", file=" + fileName + "]";
    }
}
