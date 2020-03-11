// SPDX-License-Identifier: GPL-3.0-or-later
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
     * @param extension   Case insensitive file extension of the file, without the starting dot
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

    /**
     * This method returns the formatted file-extension description.
     * <p>
     * The formatting represents combining the raw description with the
     * file extension, enclosed in brackets.
     * <p>
     * For example, if the raw description was "Assembler source" and the
     * extension "asm", then the formatted description will be:
     * "Assembler source (*.asm)"
     *
     * @return formatted file-extension description
     */
    public String getFormattedDescription() {
        return description + " (*." + extension + ")";
    }
}
