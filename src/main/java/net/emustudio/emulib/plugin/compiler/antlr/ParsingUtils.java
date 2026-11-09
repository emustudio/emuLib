/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler.antlr;

import net.emustudio.emulib.Numbers;
import org.antlr.v4.runtime.Token;

import java.util.Locale;

/**
 * Conversions of common ANTLR literal tokens shared by emuStudio compilers.
 */
public final class ParsingUtils {

    private ParsingUtils() {
    }

    /**
     * Parses a number literal token, e.g. {@code -0x1F}, {@code 1Fh}, {@code 101b}, {@code 17o}, {@code 17q},
     * {@code 12d} or {@code 12}.
     *
     * @param token number token
     * @return parsed value
     * @throws NumberFormatException if the token is not a number; see {@link Numbers#parseRadix(String)}
     */
    public static int parseNumber(Token token) {
        return Numbers.parseRadix(token.getText());
    }

    /**
     * Strips the quotes of a string literal token ({@code 'text'} or {@code "text"}).
     *
     * @param token string token
     * @return the string without quotes
     */
    public static String parseLitString(Token token) {
        String text = token.getText();
        return text.substring(1, text.length() - 1);
    }

    /**
     * Strips the trailing colon of a label definition token ({@code label:}).
     *
     * @param token label token
     * @return the label name
     */
    public static String parseLabel(Token token) {
        String text = token.getText();
        return text.substring(0, text.length() - 1);
    }

    /**
     * Normalizes an identifier for case-insensitive lookup.
     *
     * @param id identifier
     * @return lower-case identifier
     */
    public static String normalizeId(String id) {
        return id.toLowerCase(Locale.ENGLISH);
    }
}
