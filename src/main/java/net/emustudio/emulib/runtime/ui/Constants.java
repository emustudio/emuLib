/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import java.awt.*;

/**
 * Shared fonts and colors for plugin GUIs.
 */
public final class Constants {
    /**
     * Default monospaced font (plain, 12 pt).
     */
    public static final Font FONT_MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    /**
     * Big bold monospaced font (18 pt), e.g. for CPU run state.
     */
    public static final Font FONT_MONOSPACED_BIG_BOLD = new Font(Font.MONOSPACED, Font.BOLD, 18);

    /**
     * CPU run state color (can be used in CPU status GUIs).
     */
    public static final Color CPU_RUN_STATE_COLOR = new Color(0, 153, 51);

    /**
     * Table row color for even rows.
     */
    public static final Color TABLE_COLOR_ROW_EVEN = new Color(241, 245, 250);

    /**
     * Table row color for odd rows.
     */
    public static final Color TABLE_COLOR_ROW_ODD = Color.WHITE;

    private Constants() {
    }
}
