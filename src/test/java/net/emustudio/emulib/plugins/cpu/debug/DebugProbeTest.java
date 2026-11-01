/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import static org.junit.Assert.*;

import net.emustudio.emulib.plugins.memory.AbstractMemoryContext;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;

import org.junit.Test;

import java.util.*;

public class DebugProbeTest {
    @Test
    public void widthAndRegisterBindingsValidateEdits() {
        DebugProbe probe = new DebugProbe("8080", new Memory(), disassembler());
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
        DebugProbe probe = new DebugProbe("Z80", memory, disassembler());
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
        assertEquals(1, events.get(0).sequence);
        assertEquals("nop", events.get(0).mnemonic);
        assertEquals("00", events.get(0).opcodes);
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
        assertEquals(2, events.get(1).sequence);
        assertEquals(1, events.get(0).accesses.size());
        probe.removeListener(listener);
        assertEquals(List.of(true, false), activity);
        assertFalse(probe.isActive());
    }

    @Test
    public void refusingAnInstructionDoesNotReadRegistersOrMemory() {
        DebugProbe probe = new DebugProbe("8080", new Memory(), disassembler());
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

    @Test
    public void capturedCodeAndBankSurviveMemoryChanges() {
        Memory memory = new Memory();
        memory.write(0, (byte) 0x3c);
        memory.bytes[0][0] = 0;
        Disassembler decoder = new Disassembler() {
            @Override public DisassembledInstruction disassemble(int at) {
                return new DisassembledInstruction(at, "opcode " + memory.read(at),
                        String.format("%02X", memory.read(at)));
            }
            @Override public int getNextInstructionPosition(int at) { return at + 1; }
        };
        DebugProbe probe = new DebugProbe("8080", memory, decoder);
        List<DebugCPU.Instruction> events = collect(probe);
        assertTrue(probe.begin(0));
        probe.instrumentedMemory().write(0, (byte) 0);
        probe.end(true);
        assertEquals(1, events.get(0).bank);
        assertEquals("3C", events.get(0).opcodes);
        assertEquals("opcode 60", events.get(0).mnemonic);
        assertEquals(1, events.get(0).accesses.size());
        assertTrue(events.get(0).accesses.get(0).write);
        assertTrue(probe.begin(0));
        probe.end(true);
        assertEquals("00", events.get(1).opcodes);
        assertEquals("3C", events.get(0).opcodes);
    }

    @Test
    public void decodeFailureDoesNotPreventExecutionOrHideFailedInstructions() {
        Disassembler decoder = new Disassembler() {
            @Override public DisassembledInstruction disassemble(int at) {
                throw new IndexOutOfBoundsException("Incomplete instruction");
            }
            @Override public int getNextInstructionPosition(int at) { return at + 1; }
        };
        DebugProbe probe = new DebugProbe("8080", new Memory(), decoder);
        List<DebugCPU.Instruction> events = collect(probe);
        assertTrue(probe.begin(0));
        probe.instrumentedMemory().write(0, (byte) 42);
        probe.end(false);
        assertEquals("<invalid instruction>", events.get(0).mnemonic);
        assertEquals("<unavailable>", events.get(0).opcodes);
        assertFalse(events.get(0).completed);
        assertEquals(Byte.valueOf((byte) 42), probe.memory().read(0));
    }

    @Test
    public void wordAccessesAndFullWidthRegistersRetainAllBits() {
        DebugProbe probe = new DebugProbe("word-cpu", new Memory(), disassembler());
        long[] wide = {Long.MIN_VALUE};
        probe.register("WIDE", 64, () -> wide[0], value -> wide[0] = value);
        probe.writeRegister("WIDE", -1L);
        assertEquals(-1L, wide[0]);
        probe.writeRegister("WIDE", Long.MIN_VALUE);
        List<DebugCPU.Instruction> events = collect(probe);
        assertTrue(probe.begin(0));
        probe.access(2, 1, Integer.MIN_VALUE, true);
        probe.end(true);
        assertEquals(Integer.MIN_VALUE, events.get(0).accesses.get(0).value);
        assertEquals(Long.valueOf(Long.MIN_VALUE), events.get(0).before.get("WIDE"));
        assertThrows(IllegalArgumentException.class,
                () -> probe.register("TOO_WIDE", 65, () -> 0, value -> {}));
    }

    @Test
    public void inspectionReadsPhysicalBanksWithoutGuestReadEffects() {
        Memory memory = new Memory() {
            @Override public Byte read(int at) { throw new AssertionError("Guest read"); }
            @Override public Byte[] read(int at, int count) { throw new AssertionError("Guest read"); }
        };
        memory.bytes[1][0] = 42;
        memory.bytes[0][0] = 7;
        net.emustudio.emulib.plugins.memory.MemoryContext<Byte> view = DebugProbe.inspectionMemory(memory);
        assertEquals(Byte.valueOf((byte) 42), view.read(0));
        assertArrayEquals(new Byte[] {42, 0}, view.read(0, 2));
        assertEquals(Byte.valueOf((byte) 7), view.readBank(0, 0));
        assertThrows(UnsupportedOperationException.class, () -> view.write(0, (byte) 3));
    }

    @Test
    public void explicitExecutionBankAndIsrRecaptureUseTruthfulLocations() {
        DebugProbe probe = new DebugProbe("banked", new Memory(), disassembler());
        probe.register("PC", 16, () -> 0, value -> {});
        int[] observedBank = {-1};
        probe.addListener(new DebugCPU.Listener() {
            @Override public boolean beforeInstruction(int pc, int bank) {
                observedBank[0] = bank;
                return false;
            }
        });
        List<DebugCPU.Instruction> events = collect(probe);
        assertTrue(probe.begin(0, 0));
        probe.end(true);
        assertEquals(0, observedBank[0]);
        assertEquals(0, events.get(0).bank);
        assertTrue(probe.begin(0));
        probe.captureInstruction(5, "INT acknowledge; ");
        probe.end(true);
        assertEquals(1, observedBank[0]);
        assertEquals(5, events.get(1).address);
        assertEquals(0, events.get(1).bank);
        assertEquals(Long.valueOf(0), events.get(1).before.get("PC"));
    }

    private static List<DebugCPU.Instruction> collect(DebugProbe probe) {
        List<DebugCPU.Instruction> events = new ArrayList<>();
        probe.addListener(new DebugCPU.Listener() {
            @Override public boolean afterInstruction(DebugCPU.Instruction event) {
                events.add(event);
                return false;
            }
        });
        return events;
    }

    private static Disassembler disassembler() {
        return new Disassembler() {
            @Override public DisassembledInstruction disassemble(int at) {
                return new DisassembledInstruction(at, "nop", "00");
            }
            @Override public int getNextInstructionPosition(int at) { return at + 1; }
        };
    }

    private static class Memory extends AbstractMemoryContext<Byte> {
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
