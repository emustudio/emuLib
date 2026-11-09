/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import java.util.List;
import java.util.OptionalInt;

/** Immutable capture; contains no machine objects and can be rendered without the plugin. */
public final class StepView {
    public final String summary, foldKey;
    public final OptionalInt programLocation;
    public final List<ViewRow> rows;

    public StepView(String summary, String foldKey, OptionalInt programLocation, List<ViewRow> rows) {
        this.summary = summary;
        this.foldKey = foldKey;
        this.programLocation = programLocation;
        this.rows = List.copyOf(rows);
    }
}
