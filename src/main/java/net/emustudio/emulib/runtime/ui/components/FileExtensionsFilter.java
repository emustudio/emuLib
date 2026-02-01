/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import net.emustudio.emulib.plugins.compiler.FileExtension;
import net.jcip.annotations.Immutable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Definition of supported file extensions of one "filter" when choosing files. A filter might be understood
 * as container of file extensions for one file format.
 */
@Immutable
@SuppressWarnings("unused")
public class FileExtensionsFilter {
    private final String description;
    private final List<String> extensions;

    public FileExtensionsFilter(String description, String... extensions) {
        this.description = Objects.requireNonNull(description);
        this.extensions = List.of(extensions);
    }

    public FileExtensionsFilter(String description, Collection<String> extensions) {
        this.description = Objects.requireNonNull(description);
        this.extensions = List.copyOf(extensions);
    }

    public FileExtensionsFilter(FileExtension extension) {
        this(extension.getDescription(), extension.getExtension());
    }

    /**
     * Get description of the filter.
     * <p>
     * The description should not include extensions. For example, the following description is a good one:
     * <p>
     * {@code "Image files"}
     * <p>
     * While the following one is a bad one:
     * <p>
     * {@code "Image files (*.jpg, *.png)}
     *
     * @return description of the filter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get list of supported file extensions of the filter.
     * <p>
     * The extensions are case-insensitive. In addition, an extension should not start with {@code "*."} prefix.
     * For example, the following extension is a good one:
     * <p>
     * {@code "png"}
     * <p>
     * While the following one is a bad one:
     * <p>
     * {@code "*.png"}
     *
     * @return list of supported file extensions
     */
    public List<String> getExtensions() {
        return extensions;
    }
}
