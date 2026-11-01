/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import net.emustudio.emulib.plugins.memory.MemoryContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    MemoryContext<?> memory();

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
        /** Monotonic execution order within this CPU probe, including across resets. */
        public final long sequence;
        /**
         * Executed instruction location (interrupted location for acknowledge-only events).
         * A composite interrupt/ISR event's before snapshot may precede interrupt acknowledgement.
         */
        public final int address, bank;
        /** Captured before execution; opcode bytes are represented as a hexadecimal sequence. */
        public final String mnemonic, opcodes;
        public final Map<String, Long> before, after;
        public final List<Access> accesses;
        public final boolean completed;

        public Instruction(
                long sequence,
                int address,
                int bank,
                String mnemonic,
                String opcodes,
                Map<String, Long> before,
                Map<String, Long> after,
                List<Access> accesses,
                boolean completed) {
            this.sequence = sequence;
            this.address = address;
            this.bank = bank;
            this.mnemonic = Objects.requireNonNull(mnemonic);
            this.opcodes = Objects.requireNonNull(opcodes);
            this.before = Map.copyOf(before);
            this.after = Map.copyOf(after);
            this.accesses = List.copyOf(accesses);
            this.completed = completed;
        }
    }
}
