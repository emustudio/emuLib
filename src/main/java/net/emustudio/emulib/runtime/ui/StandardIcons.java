/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

/**
 * Resource paths of icons shared by emuStudio and its plugins.
 * {@code ICON_*} icons are 22x22 with {@code @2x} variants; load them with {@link GUI#loadIcon(String)}
 * or pass them to {@link GUI} factory methods. File names follow constant names.
 */
@SuppressWarnings("unused")
public final class StandardIcons {
    private static final String PATH = "/net/emustudio/emulib/runtime/ui/icons/";

    public static final String LOGO_FILE = PATH + "logo.png";

    public static final String ICON_ADD = PATH + "add.png";
    public static final String ICON_ASCII = PATH + "ascii.png";
    public static final String ICON_BIDIRECTION = PATH + "bidirection.png";
    public static final String ICON_BREAKPOINT = PATH + "breakpoint.png";
    public static final String ICON_BREAKPOINTS = PATH + "breakpoints.png";
    public static final String ICON_COMPILER = PATH + "compiler.png";
    public static final String ICON_COMPUTER = PATH + "computer.png";
    public static final String ICON_CONNECTION = PATH + "connection.png";
    public static final String ICON_COPY = PATH + "copy.png";
    public static final String ICON_CPU = PATH + "cpu.png";
    public static final String ICON_CUT = PATH + "cut.png";
    public static final String ICON_DELETE = PATH + "delete.png";
    public static final String ICON_DEVICE = PATH + "device.png";
    public static final String ICON_DISK_EMPTY = PATH + "disk-empty.png";
    public static final String ICON_DISK_MOUNTED = PATH + "disk-mounted.png";
    public static final String ICON_EJECT = PATH + "eject.png";
    public static final String ICON_ERASE_MEMORY = PATH + "erase-memory.png";
    public static final String ICON_FAVICON = PATH + "favicon.png";
    public static final String ICON_FIND = PATH + "find.png";
    public static final String ICON_GRID = PATH + "grid.png";
    public static final String ICON_INDICATOR_BLUE = PATH + "indicator-blue.png";
    public static final String ICON_INDICATOR_GREEN = PATH + "indicator-green.png";
    public static final String ICON_INDICATOR_RED = PATH + "indicator-red.png";
    public static final String ICON_JUMP = PATH + "jump.png";
    public static final String ICON_JUMP_TO_BEGINNING = PATH + "jump-to-beginning.png";
    public static final String ICON_LOAD = PATH + "load.png";
    public static final String ICON_MEMORY = PATH + "memory.png";
    public static final String ICON_MOTHERBOARD = PATH + "motherboard.png";
    public static final String ICON_MOVE_DOWN = PATH + "move-down.png";
    public static final String ICON_MOVE_UP = PATH + "move-up.png";
    public static final String ICON_NEW_FILE = PATH + "new-file.png";
    public static final String ICON_OPEN_FILE = PATH + "open-file.png";
    public static final String ICON_PAGE_BACK = PATH + "page-back.png";
    public static final String ICON_PAGE_CURRENT = PATH + "page-current.png";
    public static final String ICON_PAGE_FIRST = PATH + "page-first.png";
    public static final String ICON_PAGE_SEEK_BACKWARD = PATH + "page-seek-backward.png";
    public static final String ICON_PAGE_SEEK_FORWARD = PATH + "page-seek-forward.png";
    public static final String ICON_PASTE = PATH + "paste.png";
    public static final String ICON_PAUSE = PATH + "pause.png";
    public static final String ICON_RECORD = PATH + "record.png";
    public static final String ICON_REDO = PATH + "redo.png";
    public static final String ICON_REMOVE = PATH + "remove.png";
    public static final String ICON_RENAME = PATH + "rename.png";
    public static final String ICON_REPLACE = PATH + "replace.png";
    public static final String ICON_RESET = PATH + "reset.png";
    public static final String ICON_RUN = PATH + "run.png";
    public static final String ICON_RUN_TIMED = PATH + "run-timed.png";
    public static final String ICON_SAVE = PATH + "save.png";
    public static final String ICON_STEP = PATH + "step.png";
    public static final String ICON_STEP_BACK = PATH + "step-back.png";
    public static final String ICON_STOP = PATH + "stop.png";
    public static final String ICON_UNDO = PATH + "undo.png";
    public static final String ICON_VOLUME = PATH + "volume.png";

    // Same artwork, different meaning
    public static final String ICON_PAGE_FORWARD = ICON_RUN;
    public static final String ICON_PLAY = ICON_RUN;
    public static final String ICON_REFRESH = ICON_RESET;
    public static final String ICON_SETTINGS = ICON_COMPILER;

    private StandardIcons() {
    }
}
