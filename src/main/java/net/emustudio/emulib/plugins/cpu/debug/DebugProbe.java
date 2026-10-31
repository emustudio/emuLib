/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu.debug;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/** Reusable instruction probe; no register snapshots or events when no listeners are attached. */
public final class DebugProbe implements DebugCPU {
    private final String processor;
    private final MemoryContext<Byte> memory;
    private final Map<String, Binding> bindings = new LinkedHashMap<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final List<Access> accesses = new ArrayList<>();
    private Map<String, Long> before;
    private Runnable registerEdited = () -> {};
    private int address, bank, depth;
    private java.util.function.Consumer<Boolean> activityChanged = active -> {};

    public DebugProbe(String processor, MemoryContext<Byte> memory) {
        this.processor = Objects.requireNonNull(processor);
        this.memory = Objects.requireNonNull(memory);
    }

    public void register(String name, int bits, LongSupplier read, LongConsumer write) {
        if (bits < 1 || bits > 32 || bindings.containsKey(name))
            throw new IllegalArgumentException("Invalid register");
        bindings.put(name, new Binding(new Register(name, bits), read, write));
    }

    @Override
    public String processor() {
        return processor;
    }

    @Override
    public MemoryContext<Byte> memory() {
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
        long mask = (1L << b.register.bits) - 1;
        if (value < 0 || value > mask)
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
        int currentBank = memory.getBank(pc);
        for (Listener l : listeners) if (l.beforeInstruction(pc, currentBank)) return false;
        address = pc;
        bank = currentBank;
        accesses.clear();
        before = snapshot();
        depth = 1;
        return true;
    }

    public boolean end(boolean completed) {
        if (--depth > 0) return false;
        Instruction event = new Instruction(address, bank, before, snapshot(), accesses, completed);
        boolean stop = false;
        for (Listener l : listeners) stop |= l.afterInstruction(event);
        before = null;
        accesses.clear();
        return stop;
    }

    private Map<String, Long> snapshot() {
        Map<String, Long> values = new LinkedHashMap<>();
        bindings.forEach((name, b) -> values.put(name, b.read.getAsLong()));
        return values;
    }

    private void access(int at, int selectedBank, Byte value, boolean write) {
        if (depth > 0) accesses.add(new Access(at, selectedBank, value & 255, write));
    }

    /** Wrap only the CPU's memory reference; disassemblers and inspectors keep the original. */
    public MemoryContext<Byte> instrumentedMemory() {
        return new MemoryContext<Byte>() {
            @Override
            public Byte read(int at) {
                int bank = depth > 0 ? memory.getBank(at) : 0;
                Byte value = memory.read(at);
                access(at, bank, value, false);
                return value;
            }

            @Override
            public Byte[] read(int at, int count) {
                Byte[] values = memory.read(at, count);
                if (depth > 0)
                    for (int i = 0; i < values.length; i++)
                        access(at + i, memory.getBank(at + i), values[i], false);
                return values;
            }

            @Override
            public void write(int at, Byte value) {
                int bank = depth > 0 ? memory.getBank(at) : 0;
                memory.write(at, value);
                access(at, bank, value, true);
            }

            @Override
            public void write(int at, Byte[] values, int count) {
                int bank = depth > 0 ? memory.getSelectedBank() : 0;
                memory.write(at, values, count);
                if (depth > 0)
                    for (int i = 0; i < count; i++)
                        access(
                                at + i,
                                at + i < memory.getCommonBoundary() ? bank : 0,
                                values[i],
                                true);
            }

            @Override
            public Class<Byte> getCellTypeClass() {
                return memory.getCellTypeClass();
            }

            @Override
            public void clear() {
                memory.clear();
            }

            @Override
            public int getSize() {
                return memory.getSize();
            }

            @Override
            public MemoryContextAnnotations annotations() {
                return memory.annotations();
            }

            @Override
            public void addMemoryListener(MemoryListener l) {
                memory.addMemoryListener(l);
            }

            @Override
            public void removeMemoryListener(MemoryListener l) {
                memory.removeMemoryListener(l);
            }

            @Override
            public void setMemoryNotificationsEnabled(boolean on) {
                memory.setMemoryNotificationsEnabled(on);
            }

            @Override
            public boolean areMemoryNotificationsEnabled() {
                return memory.areMemoryNotificationsEnabled();
            }

            @Override
            public int getBanksCount() {
                return memory.getBanksCount();
            }

            @Override
            public int getSelectedBank() {
                return memory.getSelectedBank();
            }

            @Override
            public int getCommonBoundary() {
                return memory.getCommonBoundary();
            }

            @Override
            public Byte readBank(int at, int bank) {
                return memory.readBank(at, bank);
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
