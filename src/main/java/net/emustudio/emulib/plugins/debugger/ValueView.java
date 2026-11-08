/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.debugger;

import java.util.List;

/** Already formatted watch value. Alternatives contain text, never executable formatters. */
public final class ValueView {
    public final String text;
    public final List<Format> formats;
    public final boolean changed;

    public ValueView(String text, List<Format> formats, boolean changed) {
        this.text = text;
        this.formats = List.copyOf(formats);
        this.changed = changed;
    }

    public static final class Format {
        public final String label, text;

        public Format(String label, String text) {
            this.label = label;
            this.text = text;
        }
    }
}
