/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Execution-thread scratch state for CPU adapters that capture packed steps. */
public final class PackedCapture {
    private static final int MAX_DESCRIPTIONS = 4096;
    private final String machine;
    private final PackedStep.AccessFormat format;
    private final List<String> tapes;
    private final PackedStep.Buffer buffer = new PackedStep.Buffer();
    private final Map<String, Binding> bindings = new LinkedHashMap<>();
    private final PackedStep.Instruction[] descriptions = new PackedStep.Instruction[MAX_DESCRIPTIONS];
    private PackedStep.Layout layout;
    private Binding[] fields = new Binding[0];
    private long[] before, after;
    private Object[] beforeSymbols, afterSymbols;
    private long[] accesses = new long[8];
    private byte[] flags = new byte[8];
    private Object[] oldValues = new Object[8], newValues = new Object[8];
    private int accessCount;

    private static final class Binding {
        final PackedStep.Field field;
        final LongSupplier number;
        final Supplier<Object> inspect;

        Binding(PackedStep.Field field, LongSupplier number, Supplier<Object> inspect) {
            this.field = field;
            this.number = number;
            this.inspect = inspect;
        }
    }

    public PackedCapture(String machine, PackedStep.AccessFormat format) {
        this(machine, format, List.of());
    }

    public PackedCapture(String machine, List<String> tapes) {
        this(machine, PackedStep.AccessFormat.TAPE, tapes);
    }

    private PackedCapture(String machine, PackedStep.AccessFormat format, List<String> tapes) {
        this.machine = machine;
        this.format = format;
        this.tapes = List.copyOf(tapes);
    }

    /** Registers an unsigned 1-, 2-, or 4-byte field or a signed 8-byte field. */
    public void register(String name, int width, LongSupplier read) {
        register(name, new PackedStep.Field(name, width), read);
    }

    public void registerPair(String name, String high, String low) {
        List<String> names = new ArrayList<>(bindings.keySet());
        LongSupplier upper = bindings.get(high).number;
        LongSupplier lower = bindings.get(low).number;
        register(name, PackedStep.Field.bytePair(name, names.indexOf(high), names.indexOf(low)),
                () -> (upper.getAsLong() << 8) | lower.getAsLong());
    }

    private void register(String name, PackedStep.Field field, LongSupplier read) {
        bind(name, new Binding(field, read, read::getAsLong));
    }

    public void inspect(String name, Supplier<?> read) {
        bind(name, new Binding(PackedStep.Field.symbolic(name), null, read::get));
    }

    private void bind(String name, Binding binding) {
        bindings.put(name, binding);
        layout = null;
    }

    public Supplier<Object> binding(String name) {
        Binding binding = bindings.get(name);
        return binding == null ? null : binding.inspect;
    }

    public void clearAccesses() {
        if (format == PackedStep.AccessFormat.TAPE) {
            Arrays.fill(oldValues, 0, accessCount, null);
            Arrays.fill(newValues, 0, accessCount, null);
        }
        accessCount = 0;
    }

    public void access(int address, int value, boolean write) {
        addAccess(address, value, write ? 1 : 0);
    }

    public void tapeAccess(String tape, int address, Object before, Object after, boolean write, boolean changed) {
        int index = tapes.indexOf(tape);
        if (index < 0) throw new IllegalArgumentException("Unknown recording tape " + tape);
        int entry = accessCount;
        addAccess(address, 0, (index << 2) | (changed ? 2 : 0) | (write ? 1 : 0));
        oldValues[entry] = before;
        newValues[entry] = after;
    }

    private void addAccess(int address, int value, int flag) {
        if (accessCount == accesses.length) {
            int capacity = 2 * accesses.length;
            accesses = Arrays.copyOf(accesses, capacity);
            flags = Arrays.copyOf(flags, capacity);
            if (format == PackedStep.AccessFormat.TAPE) {
                oldValues = Arrays.copyOf(oldValues, capacity);
                newValues = Arrays.copyOf(newValues, capacity);
            }
        }
        accesses[accessCount] = ((long) address << 32) | Integer.toUnsignedLong(value);
        flags[accessCount++] = (byte) flag;
    }

    public boolean accessed(int address, boolean write) {
        return accessed(0, address, write);
    }

    public boolean accessed(String tape, int address, boolean write) {
        int index = tapes.indexOf(tape);
        return index >= 0 && accessed(index, address, write);
    }

    private boolean accessed(int tape, int address, boolean write) {
        for (int i = 0; i < accessCount; i++) {
            if ((int) (accesses[i] >>> 32) == address && ((flags[i] & 1) != 0) == write && ((flags[i] & 0xFF) >>> 2) == tape)
                return true;
        }
        return false;
    }

    public void before() {
        if (layout == null) {
            Arrays.fill(descriptions, null);
            fields = bindings.values().toArray(new Binding[0]);
            List<PackedStep.Field> metadata = new ArrayList<>(fields.length);
            for (Binding binding : fields) metadata.add(binding.field);
            layout = new PackedStep.Layout(machine, metadata, format, tapes);
            before = new long[fields.length];
            after = new long[fields.length];
            beforeSymbols = new Object[fields.length];
            afterSymbols = new Object[fields.length];
        }
        captureFields(before, beforeSymbols);
    }

    public StepView after(DecodedInstruction decoded, int address) {
        return after(address, true, decoded == null ? "<invalid instruction>" : decoded.mnemonic(),
                decoded == null ? "<unavailable>" : decoded.encoding());
    }

    public StepView after(int address, boolean located, String summary, String encoding) {
        captureFields(after, afterSymbols);
        int index = address & (MAX_DESCRIPTIONS - 1);
        PackedStep.Instruction description = located ? descriptions[index] : null;
        if (description == null || !description.matches(address, summary, encoding)) {
            description = new PackedStep.Instruction(layout, summary, encoding, located ? OptionalInt.of(address) : OptionalInt.empty());
            if (located) descriptions[index] = description;
        }
        return buffer.capture(description, before, after, beforeSymbols, afterSymbols, accesses, flags, oldValues, newValues, accessCount);
    }

    private void captureFields(long[] numbers, Object[] symbols) {
        for (int i = 0; i < fields.length; i++) {
            Binding binding = fields[i];
            if (binding.field.isSymbolic()) symbols[i] = binding.inspect.get();
            else if (binding.field.isStored()) numbers[i] = binding.number.getAsLong();
        }
    }
}
