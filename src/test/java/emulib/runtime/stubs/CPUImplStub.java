/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.runtime.stubs;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.plugins.PluginInitializationException;
import emulib.plugins.cpu.Disassembler;

import javax.swing.JPanel;

@PluginType(title = "CPU", description = "", type = PLUGIN_TYPE.CPU, copyright = "(c)")
public class CPUImplStub extends AbstractCPUStub {

    @Override
    public boolean addCPUListener(CPUListener listener) {
        return true;
    }

    @Override
    public boolean removeCPUListener(CPUListener listener) {
        return true;
    }

    @Override
    public void step() {
    }

    @Override
    public void execute() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public JPanel getStatusPanel() {
        return null;
    }

    @Override
    public boolean isBreakpointSupported() {
        return false;
    }

    @Override
    public void setBreakpoint(int pos) {
    }

    @Override
    public void unsetBreakpoint(int pos) {
    }

    @Override
    public boolean isBreakpointSet(int pos) {
        return false;
    }

    @Override
    public void reset(int startAddress) {
    }

    @Override
    public int getInstructionPosition() {
        return 0;
    }

    @Override
    public boolean setInstructionPosition(int pos) {
        return false;
    }

    @Override
    public Disassembler getDisassembler() {
        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public void initialize(SettingsManager sHandler) throws PluginInitializationException {
        throw new PluginInitializationException(null);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void showSettings() {
    }

    @Override
    public boolean isShowSettingsSupported() {
        return false;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getTitle() {
        return CPUImplStub.class.getAnnotation(PluginType.class).title();
    }
}
