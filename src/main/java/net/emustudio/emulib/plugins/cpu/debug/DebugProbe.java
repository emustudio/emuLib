/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.AbstractMemoryContext;
import java.lang.reflect.Array;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/** Reusable instruction probe; no register snapshots or events when no listeners are attached. */
public final class DebugProbe implements DebugCPU {
    private final String processor;
    private final Disassembler disassembler;
    private String mnemonic, opcodes;
    private long sequence;
    private final MemoryContext<?> memory;
    private final Map<String, Binding> bindings = new LinkedHashMap<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final List<Access> accesses = new ArrayList<>();
    private Map<String, Long> before;
    private Runnable registerEdited = () -> {};
    private int address, depth;
    private java.util.function.Consumer<Boolean> activityChanged = active -> {};

    public DebugProbe(String processor, MemoryContext<?> memory, Disassembler disassembler) {
        this.processor = Objects.requireNonNull(processor);
        this.disassembler = Objects.requireNonNull(disassembler);
        this.memory = Objects.requireNonNull(memory);
    }

    public void register(String name, int bits, LongSupplier read, LongConsumer write) {
        if (bits < 1 || bits > 64 || bindings.containsKey(name))
            throw new IllegalArgumentException("Invalid register");
        bindings.put(name, new Binding(new Register(name, bits), read, write));
    }

    @Override
    public String processor() {
        return processor;
    }

    @Override
    public MemoryContext<?> memory() {
        return memory;
    }

    @Override
    public List<Register> registers() {
        List<Register> result = new ArrayList<>();
        bindings.values().forEach(b -> result.add(b.register));
        return Collections.unmodifiableList(result);
    }

    private Binding binding(String name) {
        Binding b = bindings.get(name.toUpperCase(Locale.ROOT));
        if (b == null) throw new IllegalArgumentException("Unknown register: " + name);
        return b;
    }

    @Override
    public long readRegister(String name) {
        return binding(name).read.getAsLong();
    }

    @Override
    public void writeRegister(String name, long value) {
        Binding b = binding(name);
        long mask = -1L >>> (64 - b.register.bits);
        if (b.register.bits < 64 && (value < 0 || value > mask))
            throw new IllegalArgumentException("Value exceeds register width");
        b.write.accept(value);
        registerEdited.run();
    }

    /** Notify the owning CPU after a successful paused register edit. */
    public void onRegisterEdited(Runnable callback) {
        registerEdited = Objects.requireNonNull(callback);
    }

    /**
     * Engine adapter callback; configure before exposing the probe, change listeners only while
     * paused.
     */
    public void onActivityChanged(java.util.function.Consumer<Boolean> callback) {
        activityChanged = Objects.requireNonNull(callback);
    }

    @Override
    public void addListener(Listener l) {
        if (listeners.addIfAbsent(Objects.requireNonNull(l))) activityChanged.accept(isActive());
    }

    @Override
    public void removeListener(Listener l) {
        if (listeners.remove(l)) activityChanged.accept(isActive());
    }

    public boolean isActive() {
        return !listeners.isEmpty();
    }

    public boolean begin(int pc) {
        if (depth > 0) {
            depth++;
            return true;
        }
        for (Listener l : listeners) if (l.beforeInstruction(pc)) return false;
        accesses.clear();
        before = snapshot();
        depth = 1;
        captureInstruction(pc, "");
        return true;
    }

    public boolean end(boolean completed) {
        if (--depth > 0) return false;
        Instruction event = new Instruction(++sequence, address, mnemonic, opcodes, before, snapshot(), accesses, completed);
        boolean stop = false;
        for (Listener l : listeners) stop |= l.afterInstruction(event);
        before = null;
        accesses.clear();
        return stop;
    }

    /** Engine hook for an interrupt acknowledge that executes no instruction from memory. */
    public void describeInterrupt(String mnemonic, String opcodes) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
        this.opcodes = Objects.requireNonNull(opcodes);
    }

    /**
     * Capture the actual CPU-visible instruction address, including an ISR instruction after an
     * acknowledge. The before-register snapshot remains the state at begin(), before the acknowledge.
     */
    public void captureInstruction(int pc, String prefix) {
        address = pc;
        try {
            DisassembledInstruction instruction = disassembler.disassemble(pc);
            mnemonic = prefix + instruction.mnemo;
            opcodes = instruction.opCode;
        } catch (InvalidInstructionException | RuntimeException e) {
            mnemonic = prefix + "<invalid instruction>";
            opcodes = "<unavailable>";
        }
    }

    private Map<String, Long> snapshot() {
        Map<String, Long> values = new LinkedHashMap<>();
        bindings.forEach((name, b) -> values.put(name, b.read.getAsLong()));
        return values;
    }

    /** Report a guest access for CPUs whose memory cells are not bytes or use a raw execution view. */
    public void access(int at, int value, boolean write) {
        if (depth > 0) accesses.add(new Access(at, value, write));
    }

    /**
     * Read-only disassembler view of the CPU's uninstrumented memory wiring.
     */
    public static <T> MemoryContext<T> inspectionMemory(MemoryContext<T> memory) {
        Objects.requireNonNull(memory);
        return new AbstractMemoryContext<T>() {
            @Override public T read(int at) { return memory.peek(at); }
            @Override @SuppressWarnings("unchecked") public T[] read(int at, int count) {
                Objects.checkFromIndexSize(at, 0, memory.getSize());
                if (count < 0) throw new IndexOutOfBoundsException("Negative count");
                int length = Math.min(count, memory.getSize() - at);
                T[] values = (T[]) Array.newInstance(memory.getCellTypeClass(), length);
                for (int i = 0; i < length; i++) values[i] = read(at + i);
                return values;
            }
            @Override public int getSize() { return memory.getSize(); }
            @Override public Class<T> getCellTypeClass() { return memory.getCellTypeClass(); }
            @Override public void write(int at, T value) { throw new UnsupportedOperationException("Read-only inspection"); }
            @Override public void write(int at, T[] values, int count) { throw new UnsupportedOperationException("Read-only inspection"); }
            @Override public void clear() { throw new UnsupportedOperationException("Read-only inspection"); }
        };
    }

    /** Wrap only the CPU's memory reference; disassemblers and inspectors keep the original. */
    @SuppressWarnings("unchecked")
    public MemoryContext<Byte> instrumentedMemory() {
        if (memory.getCellTypeClass() != Byte.class)
            throw new IllegalStateException("Byte instrumentation requires byte memory; report accesses through access()");
        MemoryContext<Byte> byteMemory = (MemoryContext<Byte>) memory;
        return new MemoryContext<Byte>() {
            @Override
            public Byte read(int at) {
                Byte value = byteMemory.read(at);
                access(at, value & 255, false);
                return value;
            }

            @Override
            public Byte peek(int at) {
                return byteMemory.peek(at);
            }

            @Override
            public Byte[] read(int at, int count) {
                Byte[] values = byteMemory.read(at, count);
                if (depth > 0)
                    for (int i = 0; i < values.length; i++)
                        access(at + i, values[i] & 255, false);
                return values;
            }

            @Override
            public void write(int at, Byte value) {
                byteMemory.write(at, value);
                access(at, value & 255, true);
            }

            @Override
            public void write(int at, Byte[] values, int count) {
                byteMemory.write(at, values, count);
                if (depth > 0)
                    for (int i = 0; i < count; i++)
                        access(
                                at + i,
                                values[i] & 255,
                                true);
            }

            @Override
            public Class<Byte> getCellTypeClass() {
                return byteMemory.getCellTypeClass();
            }

            @Override
            public void clear() {
                byteMemory.clear();
            }

            @Override
            public int getSize() {
                return byteMemory.getSize();
            }

            @Override
            public void addMemoryListener(MemoryListener l) {
                byteMemory.addMemoryListener(l);
            }

            @Override
            public void removeMemoryListener(MemoryListener l) {
                byteMemory.removeMemoryListener(l);
            }

            @Override
            public void setMemoryNotificationsEnabled(boolean on) {
                byteMemory.setMemoryNotificationsEnabled(on);
            }

            @Override
            public boolean areMemoryNotificationsEnabled() {
                return byteMemory.areMemoryNotificationsEnabled();
            }

        };
    }

    private static final class Binding {
        final Register register;
        final LongSupplier read;
        final LongConsumer write;

        Binding(Register register, LongSupplier read, LongConsumer write) {
            this.register = register;
            this.read = read;
            this.write = write;
        }
    }
}
