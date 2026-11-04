/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.memory.MemoryContext;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Connects a decoder factory to the current logical memory and explicit live banks. */
public final class BankedDisassembler<T> implements Disassembler {
    private final MemoryContext<T> memory;
    private final Function<MemoryContext<T>, Disassembler> factory;
    private final Disassembler logical;

    public BankedDisassembler(MemoryContext<T> memory, Function<MemoryContext<T>, Disassembler> factory) {
        this.memory = Objects.requireNonNull(memory);
        this.factory = Objects.requireNonNull(factory);
        logical = Objects.requireNonNull(factory.apply(memory));
    }

    @Override public DisassembledInstruction disassemble(int address) throws InvalidInstructionException {
        return logical.disassemble(address);
    }
    @Override public int getNextInstructionPosition(int address) { return logical.getNextInstructionPosition(address); }
    @Override public Optional<InstructionFlow> getFlow(int address) { return logical.getFlow(address); }
    @Override public Optional<Disassembler> forBank(int bank) {
        return Optional.of(Objects.requireNonNull(factory.apply(memory.forBank(bank))));
    }
}
