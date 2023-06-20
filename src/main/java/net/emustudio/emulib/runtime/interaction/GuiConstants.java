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

import java.awt.*;


/**
 * GUI constants
 * <p>
 * Default constants for emuStudio GUI
 */
@SuppressWarnings("unused")
public class GuiConstants {

    /**
     * Default font size
     */
    public static final int FONT_DEFAULT_SIZE = 12;

    /**
     * Big font size
     */
    public static final int FONT_BIG_SIZE = 18;

    /**
     * Default monospaced font
     */
    public static final Font FONT_MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, FONT_DEFAULT_SIZE);

    /**
     * Default monospaced BOLD font
     */
    public static final Font FONT_MONOSPACED_BOLD = new Font(Font.MONOSPACED, Font.BOLD, FONT_DEFAULT_SIZE);

    /**
     * Default bigger monospaced BOLD font
     */
    public static final Font FONT_MONOSPACED_BIG_BOLD = new Font(Font.MONOSPACED, Font.BOLD, FONT_BIG_SIZE);


    /**
     * Default dialog font
     */
    public static final Font FONT_COMMON = new Font(Font.DIALOG, Font.PLAIN, FONT_DEFAULT_SIZE);

    /**
     * Default TitleBorder font
     */
    public static final Font FONT_TITLE_BORDER = new Font(Font.SANS_SERIF, Font.BOLD, FONT_DEFAULT_SIZE);

    /**
     * CPU Run state color (can be used in CPU status GUIs)
     */
    public static final Color CPU_RUN_STATE_COLOR = new java.awt.Color(0, 153, 51);

    /**
     * Default table row color for even rows
     */
    public static final Color TABLE_COLOR_ROW_EVEN = new Color(241, 245, 250);

    /**
     * Default table row color for odd rows
     */
    public static final Color TABLE_COLOR_ROW_ODD = Color.WHITE;

    /**
     * Default table grid color
     */
    public static final Color TABLE_COLOR_TABLE_GRID = new Color(0xd9d9d9);
}
