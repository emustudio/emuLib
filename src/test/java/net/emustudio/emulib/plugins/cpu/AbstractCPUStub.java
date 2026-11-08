/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;
import javax.swing.JPanel;

@PluginRoot(type = PLUGIN_TYPE.CPU, title = "title")
public class AbstractCPUStub extends AbstractCPU {
    int destroyed;
    int location;
    Thread executionThread;

    public AbstractCPUStub(ApplicationApi applicationApi, PluginSettings settings) {
        super(44, applicationApi, settings);
    }

    @Override protected void destroyInternal() { destroyed++; }
    @Override public RunState execute() { executionThread = Thread.currentThread(); return RunState.STATE_STOPPED_NORMAL; }
    @Override public RunState step() { executionThread = Thread.currentThread(); return RunState.STATE_STOPPED_BREAK; }
    @Override public void reset(int location) { this.location = location; }
    @Override public JPanel getStatusPanel() { return null; }
    @Override public int getInstructionLocation() { return location; }
    @Override public boolean setInstructionLocation(int location) { this.location = location; return true; }
    @Override public int getAddressSpaceSize() { return 256; }
    @Override public Disassembler getDisassembler() { return null; }
    @Override public void initialize() { }
    @Override public String getDescription() { return "CPU stub"; }
}
