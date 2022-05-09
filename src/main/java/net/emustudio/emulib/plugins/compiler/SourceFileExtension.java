/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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

/**
 * This class is a container that holds a single file extension and its
 * description for source file that is supported for the compiler.
 * <p>
 * The file extension should have the form without the starting
 * dot ("."), i.e. "asm" instead of ".asm". The extension will be taken in
 * case insensitive manner.
 * <p>
 * The description of the extension should be in any form.
 * <p>
 * Immutable object.
 */
public class SourceFileExtension {
    private final String extension;
    private final String description;

    /**
     * Creates an instance of the class.
     *
     * @param extension   Case-insensitive file extension of the file, without the starting dot
     *                    (example: "asm", but not ".asm").
     * @param description The description of the file extension.
     */
    public SourceFileExtension(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    /**
     * Return the file extension.
     *
     * @return File extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Return raw file-extension description as it was passed.
     *
     * @return file-extension description
     */
    public String getDescription() {
        return description;
    }
}
