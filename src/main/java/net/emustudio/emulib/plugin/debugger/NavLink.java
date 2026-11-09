/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

/** Finds an earlier retained event carrying the opaque tag; no live machine reference. */
public final class NavLink {
    public final String label, tag;

    public NavLink(String label, String tag) {
        this.label = label;
        this.tag = tag;
    }
}
