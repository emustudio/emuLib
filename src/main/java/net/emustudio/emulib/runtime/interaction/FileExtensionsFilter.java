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
package net.emustudio.emulib.runtime.interaction;

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
