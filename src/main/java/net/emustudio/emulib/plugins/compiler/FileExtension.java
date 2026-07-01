/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
            String suffix = "." + extension.getExtension();
            if (fileName.endsWith(suffix)) {
                return fileName.substring(0, fileName.length() - suffix.length());
            }
        }
        return fileName;
    }
}
