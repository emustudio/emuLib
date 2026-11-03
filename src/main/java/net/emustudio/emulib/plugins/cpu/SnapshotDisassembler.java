/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.MemorySnapshot;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Connects a decoder factory to live inspection and immutable explicit-bank analysis. */
public final class SnapshotDisassembler<T> implements Disassembler {
    private final MemoryContext<T> memory;
    private final UnaryOperator<T> immutableCopy;
    private final Function<MemoryContext<T>, Disassembler> factory;
    private final Disassembler live;

    public SnapshotDisassembler(MemoryContext<T> memory, UnaryOperator<T> immutableCopy,
                                Function<MemoryContext<T>, Disassembler> factory) {
        this.memory = Objects.requireNonNull(memory);
        this.immutableCopy = Objects.requireNonNull(immutableCopy);
        this.factory = Objects.requireNonNull(factory);
        live = Objects.requireNonNull(factory.apply(memory));
    }

    @Override public DisassembledInstruction disassemble(int address) throws InvalidInstructionException {
        return live.disassemble(address);
    }
    @Override public int getNextInstructionPosition(int address) { return live.getNextInstructionPosition(address); }
    @Override public Optional<InstructionFlow> getFlow(int address) { return live.getFlow(address); }
    @Override public Optional<Disassembler> capture(int bank) {
        return Optional.of(Objects.requireNonNull(factory.apply(MemorySnapshot.capture(memory, bank, immutableCopy))));
    }
}
