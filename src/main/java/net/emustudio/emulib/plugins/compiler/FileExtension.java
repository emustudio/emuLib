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
 * A file extension with description.
 * A compiler provides list of supported source file extensions with a description, which is then used in
 * emuStudio application when opening a file.
 * <p>
 * The file extension should be provided without the starting dot ("."), i.e. "asm" instead of ".asm".
 * The extension is case-sensitive.
 * <p>
 * Instances of this class can be compared using .equals() method and also put to hashed collections like
 * HashMap or HashSet. The comparison is performed by extension only.
 */
@Immutable
public class FileExtension {
    private final String extension;
    private final String description;

    /**
     * Creates new instance of FileExtension.
     *
     * @param extension   Case-sensitive non-null file extension of the file, without the starting dot
     *                    (example: "asm", but not ".asm").
     * @param description Non-null description of the file extension.
     * @throws NullPointerException if extension or description is null
     */
    public FileExtension(String extension, String description) {
        this.extension = Objects.requireNonNull(extension);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * Returns the file extension.
     *
     * @return File extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the file-extension description.
     *
     * @return file-extension description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileExtension that = (FileExtension) o;
        return extension.equals(that.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension);
    }

    /**
     * Strip file name extension if the extension belongs to known extensions.
     * <p>
     * The idea is that compilers should generate file with some "output" extension, so the source extension is replaced
     * with it. However, if the source extension is unknown, it should be preserved.
     *
     * @param fileName        a file name
     * @param knownExtensions known extensions
     * @return given file name stripped from extension if it belongs to known ones
     */
    public static String stripKnownExtension(String fileName, Iterable<FileExtension> knownExtensions) {
        for (FileExtension extension : knownExtensions) {
            int i = fileName.lastIndexOf("." + extension.getExtension());
            if (i >= 0) {
                return fileName.substring(0, i);
            }
        }
        return fileName;
    }
}
