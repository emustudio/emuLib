/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.io;

import net.jcip.annotations.Immutable;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * File type: a description and file extensions (without dot). Used for source files of compilers and in file dialogs.
 */
@Immutable
public final class FileType {
    private final String description;
    private final List<String> extensions;

    /**
     * @param description human-readable description, e.g. "Assembler source"
     * @param extensions  one or more extensions without dot, e.g. "asm"
     */
    public FileType(String description, String... extensions) {
        this.description = Objects.requireNonNull(description);
        this.extensions = List.of(extensions);
        if (this.extensions.isEmpty()) {
            throw new IllegalArgumentException("At least one extension is required");
        }
    }

    /**
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return extensions without dot
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * Determines whether the file name ends with one of the extensions (case-insensitive).
     *
     * @param path file path
     * @return true if the file is of this type
     */
    public boolean matches(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return extensions.stream().anyMatch(e -> fileName.endsWith("." + e.toLowerCase(Locale.ROOT)));
    }

    /**
     * Replaces file extension of given types (or appends new extension if the path has none of them).
     *
     * @param path         file path
     * @param types        known file types
     * @param newExtension new extension without dot, e.g. "hex"
     * @return path with replaced extension
     */
    public static Path replaceExtension(Path path, List<FileType> types, String newExtension) {
        String fileName = path.toString();
        for (FileType type : types) {
            for (String extension : type.extensions) {
                if (fileName.endsWith("." + extension)) {
                    return Path.of(fileName.substring(0, fileName.length() - extension.length()) + newExtension);
                }
            }
        }
        return Path.of(fileName + "." + newExtension);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileType)) return false;
        FileType that = (FileType) o;
        return description.equals(that.description) && extensions.equals(that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, extensions);
    }

    @Override
    public String toString() {
        return description + " " + extensions;
    }
}
