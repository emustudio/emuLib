/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import net.emustudio.emulib.application.ApplicationApi;
import net.emustudio.emulib.application.settings.PluginSettings;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class AbstractCPUTest {
    @Test
    public void resetUsesConfiguredProgramLocation() {
        ApplicationApi application = createMock(ApplicationApi.class);
        expect(application.getProgramLocation()).andReturn(123);
        replay(application);
        AbstractCPUStub cpu = new AbstractCPUStub(application, createNiceMock(PluginSettings.class));

        cpu.reset();

        assertEquals(123, cpu.location);
        verify(application);
    }

    @Test
    public void synchronousPrimitivesReturnResultsOnCallerThread() {
        AbstractCPUStub cpu = new AbstractCPUStub(createNiceMock(ApplicationApi.class), createNiceMock(PluginSettings.class));

        assertEquals(CPU.RunState.STATE_STOPPED_NORMAL, cpu.execute());
        assertSame(Thread.currentThread(), cpu.executionThread);
        assertEquals(CPU.RunState.STATE_STOPPED_BREAK, cpu.step());
        assertSame(Thread.currentThread(), cpu.executionThread);
    }

    @Test
    public void destructionRunsOnceWithoutExecutionInfrastructure() {
        ApplicationApi application = createMock(ApplicationApi.class);
        replay(application);
        AbstractCPUStub cpu = new AbstractCPUStub(application, createNiceMock(PluginSettings.class));

        cpu.destroy();
        cpu.destroy();

        assertEquals(1, cpu.destroyed);
        verify(application);
    }

    @Test
    public void metadataDoesNotNeedExecutionInfrastructure() {
        AbstractCPUStub cpu = new AbstractCPUStub(createNiceMock(ApplicationApi.class), createNiceMock(PluginSettings.class));

        assertEquals("title", cpu.getTitle());
        assertFalse(cpu.isShowSettingsSupported());
        assertTrue(cpu.isAutomationSupported());
    }
}
