/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.debugger;

import java.util.List;

/** Passive detail row; tags are opaque identities and links belong to this row's event. */
public final class ViewRow {
    public final String group, label, before, after;
    public final boolean changed;
    public final List<String> tags;
    public final List<NavLink> links;

    public ViewRow(String group, String label, String before, String after, boolean changed,
                   List<String> tags, List<NavLink> links) {
        this.group = group;
        this.label = label;
        this.before = before;
        this.after = after;
        this.changed = changed;
        this.tags = List.copyOf(tags);
        this.links = List.copyOf(links);
    }
}
