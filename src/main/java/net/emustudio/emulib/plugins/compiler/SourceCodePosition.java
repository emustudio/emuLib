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
