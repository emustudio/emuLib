/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.meta;

import net.emustudio.emulib.plugins.Plugin;

import javax.swing.*;

/**
 * Test fixture: a plugin inheriting metadata defaults whose package carries a {@code version.properties} bundle.
 */
public class SamplePlugin implements Plugin {
    @Override
    public void reset() {
    }

    @Override
    public void initialize() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void showSettings(JFrame parent) {
    }

    @Override
    public boolean isShowSettingsSupported() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Sample plugin";
    }

    @Override
    public String getDescription() {
        return "Metadata test fixture";
    }

    @Override
    public boolean isAutomationSupported() {
        return false;
    }
}
