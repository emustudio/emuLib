/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import static org.junit.Assert.*;

import net.emustudio.emulib.plugins.memory.AbstractMemoryContext;

import org.junit.Test;

import java.util.*;

public class DebugProbeTest {
    @Test
    public void widthAndRegisterBindingsValidateEdits() {
        DebugProbe probe = new DebugProbe("8080", new Memory());
        long[] value = {1};
        probe.register("A", 8, () -> value[0], v -> value[0] = v);
        int[] edits = {0};
        probe.onRegisterEdited(() -> edits[0]++);
        probe.writeRegister("a", 255);
        assertEquals(255, probe.readRegister("A"));
        assertThrows(IllegalArgumentException.class, () -> probe.writeRegister("A", 256));
        assertThrows(IllegalArgumentException.class, () -> probe.writeRegister("A", -1));
        assertThrows(IllegalArgumentException.class, () -> probe.readRegister("unknown"));
        assertThrows(
                IllegalArgumentException.class, () -> probe.register("A", 8, () -> 0, v -> {}));
        assertEquals(255, value[0]);
        assertEquals(1, edits[0]);
    }

    @Test
    public void listenersNestedInstructionsAndSnapshotsRemainIndependent() {
        Memory memory = new Memory();
        DebugProbe probe = new DebugProbe("Z80", memory);
        long[] pc = {0};
        probe.register("PC", 16, () -> pc[0], v -> pc[0] = v);
        List<DebugCPU.Instruction> events = new ArrayList<>();
        List<Boolean> activity = new ArrayList<>();
        probe.onActivityChanged(activity::add);
        DebugCPU.Listener listener =
                new DebugCPU.Listener() {
                    @Override
                    public boolean afterInstruction(DebugCPU.Instruction event) {
                        events.add(event);
                        return false;
                    }
                };
        probe.addListener(listener);
        probe.addListener(listener);
        assertEquals(List.of(true), activity);
        assertTrue(probe.begin(0));
        assertTrue(probe.begin(2));
        probe.instrumentedMemory().write(1, (byte) 42);
        assertFalse(probe.end(true));
        assertTrue(events.isEmpty());
        pc[0] = 3;
        assertFalse(probe.end(true));
        assertEquals(1, events.size());
        assertEquals(Long.valueOf(0), events.get(0).before.get("PC"));
        assertEquals(Long.valueOf(3), events.get(0).after.get("PC"));
        assertEquals(1, events.get(0).accesses.size());
        assertEquals(1, events.get(0).accesses.get(0).bank);
        assertThrows(UnsupportedOperationException.class, () -> events.get(0).before.put("PC", 5L));
        memory.readBank(1, 0);
        probe.memory().read(1);
        assertEquals(1, events.size());
        assertTrue(probe.begin(3));
        probe.instrumentedMemory().read(5);
        probe.end(false);
        assertEquals(0, events.get(1).accesses.get(0).bank);
        assertFalse(events.get(1).completed);
        assertEquals(1, events.get(0).accesses.size());
        probe.removeListener(listener);
        assertEquals(List.of(true, false), activity);
        assertFalse(probe.isActive());
    }

    @Test
    public void refusingAnInstructionDoesNotReadRegistersOrMemory() {
        DebugProbe probe = new DebugProbe("8080", new Memory());
        probe.register(
                "PC",
                16,
                () -> {
                    throw new AssertionError("snapshot read");
                },
                v -> {});
        probe.addListener(
                new DebugCPU.Listener() {
                    @Override
                    public boolean beforeInstruction(int pc, int bank) {
                        return true;
                    }
                });
        assertFalse(probe.begin(0));
    }

    private static final class Memory extends AbstractMemoryContext<Byte> {
        final byte[][] bytes = new byte[2][8];

        @Override
        public Byte read(int at) {
            return readBank(at, 1);
        }

        @Override
        public Byte readBank(int at, int bank) {
            return bytes[at < 4 ? bank : 0][at];
        }

        @Override
        public Byte[] read(int at, int count) {
            Byte[] values = new Byte[count];
            for (int i = 0; i < count; i++) values[i] = read(at + i);
            return values;
        }

        @Override
        public void write(int at, Byte value) {
            bytes[getBank(at)][at] = value;
        }

        @Override
        public void write(int at, Byte[] values, int count) {
            for (int i = 0; i < count; i++) write(at + i, values[i]);
        }

        @Override
        public int getSelectedBank() {
            return 1;
        }

        @Override
        public int getBanksCount() {
            return 2;
        }

        @Override
        public int getCommonBoundary() {
            return 4;
        }

        @Override
        public int getSize() {
            return 8;
        }

        @Override
        public Class<Byte> getCellTypeClass() {
            return Byte.class;
        }

        @Override
        public void clear() {}

        @Override
        public net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations
                annotations() {
            return null;
        }
    }
}
