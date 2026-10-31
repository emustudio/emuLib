/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import net.emustudio.emulib.plugins.memory.MemoryContext;

import java.util.List;
import java.util.Map;

/**
 * Optional CPU debugging port. Register access and listener changes require a paused CPU. Listeners
 * run on the emulator thread; never call CPU lifecycle methods from a listener. Access events
 * describe guest memory accesses, including instruction fetches and attempted writes. Debugger
 * inspection uses the uninstrumented memory port and produces no guest events.
 */
public interface DebugCPU {
    String processor();

    List<Register> registers();

    long readRegister(String name);

    void writeRegister(String name, long value);

    MemoryContext<Byte> memory();

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {
        /** Return true to stop before executing this instruction. */
        default boolean beforeInstruction(int address, int bank) {
            return false;
        }

        /** Return true to stop after the completed instruction. */
        default boolean afterInstruction(Instruction event) {
            return false;
        }
    }

    final class Register {
        public final String name;
        public final int bits;

        public Register(String name, int bits) {
            this.name = name;
            this.bits = bits;
        }
    }

    final class Access {
        public final int address, bank, value;
        public final boolean write;

        public Access(int address, int bank, int value, boolean write) {
            this.address = address;
            this.bank = bank;
            this.value = value;
            this.write = write;
        }
    }

    final class Instruction {
        public final int address, bank;
        public final Map<String, Long> before, after;
        public final List<Access> accesses;
        public final boolean completed;

        public Instruction(
                int address,
                int bank,
                Map<String, Long> before,
                Map<String, Long> after,
                List<Access> accesses,
                boolean completed) {
            this.address = address;
            this.bank = bank;
            this.before = Map.copyOf(before);
            this.after = Map.copyOf(after);
            this.accesses = List.copyOf(accesses);
            this.completed = completed;
        }
    }
}
