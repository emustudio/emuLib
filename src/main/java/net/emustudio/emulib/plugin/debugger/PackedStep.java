/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.Numbers;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.RandomAccess;

/** Raw state and ordered accesses. Rows are formatted only when read. */
public final class PackedStep extends AbstractList<ViewRow> implements RandomAccess {
    private static final int MAX_ROWS = 4096;
    private static final int MAX_SCHEMA_BYTES = 65_536;
    private static final int MAX_DATA_BYTES = 8 * 1024 * 1024;
    private static final int SYMBOLIC = 16;
    private static final int INTEGER_VALUE = 1, LONG_VALUE = 2, TEXT_VALUE = 3;

    public enum AccessFormat {
        BYTE(0, 4), WIDE_BYTE(1, 6), WORD(2, 9), TAPE(3, 13);

        private final int code, width;

        AccessFormat(int code, int width) {
            this.code = code;
            this.width = width;
        }
    }

    private static final AccessFormat[] ACCESS_FORMATS = AccessFormat.values();

    private final Instruction instruction;
    private final byte[] data;
    private final int offset, length, accessCount;

    private PackedStep(Instruction instruction, byte[] data, int offset, int length, int accessCount) {
        this.instruction = instruction;
        this.data = data;
        this.offset = offset;
        this.length = length;
        this.accessCount = accessCount;
    }

    static final class Field {
        private final String name;
        private final int width, high, low;

        public Field(String name, int width) {
            this(name, width, -1, -1);
            if (width != 1 && width != 2 && width != 4 && width != 8) throw new IllegalArgumentException("Invalid register width");
        }

        private Field(String name, int width, int high, int low) {
            this.name = Objects.requireNonNull(name);
            this.width = width;
            this.high = high;
            this.low = low;
        }

        public static Field bytePair(String name, int high, int low) {
            return new Field(name, 0, high, low);
        }

        public static Field symbolic(String name) {
            return new Field(name, SYMBOLIC, -1, -1);
        }

        public boolean isStored() {
            return width != 0;
        }

        boolean isSymbolic() {
            return width == SYMBOLIC;
        }

        private int size() {
            return isSymbolic() ? Integer.BYTES : width;
        }
    }

    /** Shared field layout. Byte pairs are derived from earlier byte fields. */
    static final class Layout {
        private final String machine, writePrefix;
        private final List<Field> fields;
        private final int[] offsets;
        private final int width;
        private final AccessFormat accessFormat;
        private final List<String> tapes;
        private final List<String> tapeWritePrefixes;
        private final boolean hasValues;
        private final byte[] encoded;

        Layout(String machine, List<Field> fields, AccessFormat accessFormat, List<String> tapes) {
            this(machine, fields, accessFormat, tapes, null);
        }

        private Layout(String machine, List<Field> fields, AccessFormat accessFormat, List<String> tapes, byte[] schema) {
            this.machine = Objects.requireNonNull(machine);
            this.writePrefix = machine + "/write/";
            this.fields = List.copyOf(fields);
            this.accessFormat = accessFormat;
            this.tapes = List.copyOf(tapes);
            if (this.tapes.size() > 64 || (accessFormat == AccessFormat.TAPE) != !this.tapes.isEmpty())
                throw new IllegalArgumentException("Invalid recording tape layout");
            List<String> prefixes = new ArrayList<>(tapes.size());
            for (String tape : this.tapes) prefixes.add(machine + "/" + tape + "/write/");
            tapeWritePrefixes = List.copyOf(prefixes);
            hasValues = accessFormat == AccessFormat.TAPE || this.fields.stream().anyMatch(Field::isSymbolic);
            if (this.fields.size() >= MAX_ROWS) throw new IllegalArgumentException("Too many register fields");
            offsets = new int[this.fields.size()];
            int position = 0;
            for (int i = 0; i < this.fields.size(); i++) {
                Field field = this.fields.get(i);
                if (field.width == 0 && (!isByteField(field.high, i) || !isByteField(field.low, i)))
                    throw new IllegalArgumentException("Register pair must refer to two earlier bytes");
                offsets[i] = position;
                position += field.size();
            }
            width = position;
            encoded = schema == null ? encode() : schema;
        }

        private boolean isByteField(int index, int before) {
            return index >= 0 && index < before && fields.get(index).width == 1;
        }

        private int stateStart() {
            return hasValues ? Integer.BYTES : 0;
        }

        private int accessesStart() {
            return stateStart() + 2 * width;
        }

        private byte[] encode() {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeUTF(machine);
                out.writeShort(fields.size());
                for (Field field : fields) {
                    out.writeUTF(field.name);
                    out.writeByte(field.width);
                    if (field.width == 0) {
                        out.writeShort(field.high);
                        out.writeShort(field.low);
                    }
                }
                if (accessFormat != AccessFormat.BYTE) {
                    out.writeByte(accessFormat.code);
                    out.writeByte(tapes.size());
                    for (String tape : tapes) out.writeUTF(tape);
                }
            } catch (IOException invalid) {
                throw new IllegalArgumentException("Invalid recording layout", invalid);
            }
            if (bytes.size() > MAX_SCHEMA_BYTES) throw new IllegalArgumentException("Recording layout is too large");
            return bytes.toByteArray();
        }

        private static Layout read(DataInputStream in) throws IOException {
            byte[] encoded = readBytes(in, MAX_SCHEMA_BYTES);
            try (DataInputStream schema = new DataInputStream(new ByteArrayInputStream(encoded))) {
                String machine = schema.readUTF();
                int count = schema.readUnsignedShort();
                if (count >= MAX_ROWS) throw new IOException("Too many register fields");
                List<Field> fields = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    String name = schema.readUTF();
                    int width = schema.readUnsignedByte();
                    if (width == 0) fields.add(Field.bytePair(name, schema.readUnsignedShort(), schema.readUnsignedShort()));
                    else if (width == SYMBOLIC) fields.add(Field.symbolic(name));
                    else fields.add(new Field(name, width));
                }
                int format = schema.read();
                if (format < 0) return new Layout(machine, fields, AccessFormat.BYTE, List.of(), encoded);
                AccessFormat accessFormat = null;
                for (AccessFormat candidate : ACCESS_FORMATS)
                    if (candidate.code == format) accessFormat = candidate;
                if (accessFormat == null) throw new IOException("Invalid packed access format");
                int tapeCount = schema.readUnsignedByte();
                List<String> tapes = new ArrayList<>(tapeCount);
                for (int i = 0; i < tapeCount; i++) tapes.add(schema.readUTF());
                if (schema.read() != -1) throw new IOException("Unexpected recording layout data");
                return new Layout(machine, fields, accessFormat, tapes, encoded);
            }
        }
    }

    /** Description shared by all executions of an identical decoded instruction. */
    static final class Instruction {
        private final Layout layout;
        private final String summary, foldKey;
        private final OptionalInt location;
        private final ViewRow encoding;
        private final byte[] encodedInstruction;

        public Instruction(Layout layout, String summary, String encoding, OptionalInt location) {
            this.layout = Objects.requireNonNull(layout);
            this.summary = Objects.requireNonNull(summary);
            this.location = Objects.requireNonNull(location);
            this.foldKey = layout.machine + ":" + summary + ":" + encoding;
            this.encoding = new ViewRow("Instruction", "Encoding", "", encoding, false, List.of(), List.of());
            this.encodedInstruction = Objects.requireNonNull(encoding).getBytes(StandardCharsets.UTF_8);
            if (encodedInstruction.length > MAX_SCHEMA_BYTES) throw new IllegalArgumentException("Instruction encoding is too large");
        }

        boolean matches(int address, String text, String bytes) {
            return location.orElse(-1) == address && summary.equals(text) && encoding.after.equals(bytes);
        }
    }

    /** Appends to primitive chunks. Published slices are immutable while the buffer writes later slices. */
    static final class Buffer {
        private static final int CHUNK_BYTES = 16_384;
        private byte[] chunk = new byte[0];
        private int position, start;
        private final Map<String, byte[]> strings = new HashMap<>();

        StepView capture(Instruction instruction, long[] before, long[] after, Object[] beforeSymbols, Object[] afterSymbols,
                         long[] accesses, byte[] flags, Object[] oldValues, Object[] newValues, int count) {
            Layout layout = instruction.layout;
            begin(layout, count);
            putRegisters(layout, before, after, beforeSymbols, afterSymbols);
            for (int i = 0; i < count; i++) {
                long access = accesses[i];
                int field = layout.accessesStart() + i * layout.accessFormat.width;
                int address = (int) (access >>> 32);
                int value = (int) access;
                switch (layout.accessFormat) {
                    case BYTE:
                        put(chunk, start + field, access(address, value, (flags[i] & 1) != 0), Integer.BYTES);
                        break;
                    case WIDE_BYTE:
                        if ((value & ~0xFF) != 0) throw new IllegalArgumentException("Invalid byte-memory value");
                        put(chunk, start + field, address, Integer.BYTES);
                        put(chunk, start + field + 4, (value << 1) | flags[i], Short.BYTES);
                        break;
                    case WORD:
                        put(chunk, start + field, address, Integer.BYTES);
                        put(chunk, start + field + 4, value, Integer.BYTES);
                        chunk[start + field + 8] = flags[i];
                        break;
                    case TAPE:
                        int old = putValue(oldValues[i]);
                        int latest = Objects.equals(oldValues[i], newValues[i]) ? old : putValue(newValues[i]);
                        put(chunk, start + field, address, Integer.BYTES);
                        chunk[start + field + 4] = flags[i];
                        put(chunk, start + field + 5, old, Integer.BYTES);
                        put(chunk, start + field + 9, latest, Integer.BYTES);
                        break;
                }
            }
            return finish(instruction, count);
        }

        private void begin(Layout layout, int count) {
            if (count < 0 || count >= MAX_ROWS - layout.fields.size()) throw new IllegalArgumentException("Too many memory accesses");
            start = position;
            int length = layout.accessesStart() + layout.accessFormat.width * count;
            reserve(length);
            if (layout.hasValues) put(chunk, start, count, Integer.BYTES);
            position += length;
        }

        private void putRegisters(Layout layout, long[] before, long[] after, Object[] beforeSymbols, Object[] afterSymbols) {
            for (int i = 0; i < layout.fields.size(); i++) {
                Field field = layout.fields.get(i);
                if (!field.isStored()) continue;
                int at = layout.stateStart() + layout.offsets[i];
                if (field.isSymbolic()) {
                    boolean changed = !Objects.equals(beforeSymbols[i], afterSymbols[i]);
                    int old = putValue(beforeSymbols[i]);
                    int latest = changed ? putValue(afterSymbols[i]) | Integer.MIN_VALUE : old;
                    put(chunk, start + at, old, Integer.BYTES);
                    put(chunk, start + at + layout.width, latest, Integer.BYTES);
                } else {
                    put(chunk, start + at, before[i], field.width);
                    put(chunk, start + at + layout.width, after[i], field.width);
                }
            }
        }

        private int putValue(Object value) {
            int relative = position - start;
            if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
                reserve(5);
                chunk[position++] = INTEGER_VALUE;
                put(chunk, position, ((Number) value).longValue(), Integer.BYTES);
                position += Integer.BYTES;
            } else if (value instanceof Long) {
                reserve(9);
                chunk[position++] = LONG_VALUE;
                put(chunk, position, (Long) value, Long.BYTES);
                position += Long.BYTES;
            } else {
                String text = String.valueOf(value);
                byte[] bytes = strings.get(text);
                if (bytes == null) {
                    if (text.length() > MAX_SCHEMA_BYTES) throw new IllegalArgumentException("Recorded value is too large");
                    bytes = text.getBytes(StandardCharsets.UTF_8);
                    if (strings.size() >= 256) strings.clear();
                    if (bytes.length <= 1024) strings.put(text, bytes);
                }
                reserve(5 + bytes.length);
                chunk[position++] = TEXT_VALUE;
                put(chunk, position, bytes.length, Integer.BYTES);
                position += Integer.BYTES;
                System.arraycopy(bytes, 0, chunk, position, bytes.length);
                position += bytes.length;
            }
            return relative;
        }

        private void reserve(int bytes) {
            int used = position - start;
            if (bytes > MAX_DATA_BYTES - used) throw new IllegalArgumentException("Packed instruction is too large");
            if (bytes <= chunk.length - position) return;
            int capacity = Math.max(CHUNK_BYTES, used + bytes);
            if (capacity > CHUNK_BYTES) capacity = Math.max(capacity, Math.min(MAX_DATA_BYTES, chunk.length * 2));
            byte[] next = new byte[capacity];
            System.arraycopy(chunk, start, next, 0, used);
            chunk = next;
            start = 0;
            position = used;
        }

        private StepView finish(Instruction instruction, int count) {
            return new PackedStep(instruction, chunk, start, position - start, count).view();
        }
    }

    /** Packs one 16-bit address, unsigned byte and read/write flag. */
    private static int access(int address, int value, boolean write) {
        if ((address & ~0xFFFF) != 0 || (value & ~0xFF) != 0) throw new IllegalArgumentException("Invalid byte-memory access");
        return address << 9 | value << 1 | (write ? 1 : 0);
    }

    private static int address(int access) {
        return access >>> 9;
    }

    private static boolean isWrite(int access) {
        return (access & 1) != 0;
    }

    private StepView view() {
        return new StepView(instruction.summary, instruction.foldKey, instruction.location, this);
    }

    @Override public int size() {
        Layout layout = instruction.layout;
        return 1 + layout.fields.size() + accessCount;
    }

    @Override public ViewRow get(int index) {
        Objects.checkIndex(index, size());
        if (index == 0) return instruction.encoding;
        Layout layout = instruction.layout;
        if (index <= layout.fields.size()) {
            int i = index - 1;
            Field field = layout.fields.get(i);
            int base = offset + layout.stateStart();
            if (field.isSymbolic()) {
                int at = base + layout.offsets[i];
                int old = (int) get(at, Integer.BYTES);
                int next = (int) get(at + layout.width, Integer.BYTES);
                return stateRow(field.name, displayValue(old), displayValue(next & Integer.MAX_VALUE), next < 0);
            }
            long before = value(i, base);
            long after = value(i, base + layout.width);
            return stateRow(field.name, Long.toString(before), Long.toString(after), before != after);
        }
        return accessRow(index - 1 - layout.fields.size());
    }

    private static ViewRow stateRow(String field, String before, String after, boolean changed) {
        return new ViewRow("State", field, before, after, changed, List.of(), List.of());
    }

    private ViewRow accessRow(int index) {
        Layout layout = instruction.layout;
        int position = accessPosition(index);
        int address = accessAddress(position);
        int flags = accessFlags(position);
        boolean write = isWrite(flags);
        String tag = writePrefix(flags) + address;
        if (layout.accessFormat == AccessFormat.TAPE) {
            String label = (write ? "Write " : "Read ") + layout.tapes.get(flags >>> 2) + "[" + address + "]";
            return accessRow("Tape", label, displayValue((int) get(position + 5, Integer.BYTES)),
                    displayValue((int) get(position + 9, Integer.BYTES)), (flags & 2) != 0, write, tag);
        }
        String location = layout.accessFormat == AccessFormat.WORD
                ? String.format(Locale.ROOT, "%04d", address) : Numbers.formatHex(address, 4);
        int value;
        switch (layout.accessFormat) {
            case BYTE:
                value = ((int) get(position, Integer.BYTES) >>> 1) & 0xFF;
                break;
            case WIDE_BYTE:
                value = (int) get(position + 4, Short.BYTES) >>> 1;
                break;
            default:
                value = (int) get(position + 4, Integer.BYTES);
        }
        return accessRow("Memory", (write ? "Write @" : "Read @") + location, "", Integer.toString(value), write, write, tag);
    }

    private static ViewRow accessRow(String group, String label, String before, String after, boolean changed, boolean write, String tag) {
        return new ViewRow(group, label, before, after, changed, write ? List.of(tag) : List.of(),
                write ? List.of() : List.of(new NavLink("Find earlier write", tag)));
    }

    public boolean hasWrite(String tag) {
        int afterPrefix = tag.lastIndexOf('/') + 1;
        if (afterPrefix == 0 || afterPrefix == tag.length()) return false;
        char first = tag.charAt(afterPrefix);
        if (first == '+' || (first == '0' && tag.length() - afterPrefix > 1)
                || (first == '-' && afterPrefix + 1 < tag.length() && tag.charAt(afterPrefix + 1) == '0')) return false;
        for (int i = first == '-' ? afterPrefix + 1 : afterPrefix; i < tag.length(); i++) {
            char c = tag.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        int address;
        try {
            address = Integer.parseInt(tag, afterPrefix, tag.length(), 10);
        } catch (NumberFormatException invalid) {
            return false;
        }
        for (int i = 0; i < accessCount; i++) {
            int position = accessPosition(i);
            int flags = accessFlags(position);
            if (isWrite(flags) && accessAddress(position) == address) {
                String prefix = writePrefix(flags);
                if (prefix.length() == afterPrefix && tag.startsWith(prefix)) return true;
            }
        }
        return false;
    }

    private int accessPosition(int index) {
        Layout layout = instruction.layout;
        return offset + layout.accessesStart() + index * layout.accessFormat.width;
    }

    private int accessAddress(int position) {
        int data = (int) get(position, Integer.BYTES);
        return instruction.layout.accessFormat == AccessFormat.BYTE ? address(data) : data;
    }

    private int accessFlags(int position) {
        switch (instruction.layout.accessFormat) {
            case BYTE: return data[position + 3] & 1;
            case WIDE_BYTE: return data[position + 5] & 1;
            case WORD: return data[position + 8] & 0xFF;
            case TAPE: return data[position + 4] & 0xFF;
            default: throw new AssertionError(instruction.layout.accessFormat);
        }
    }

    private String writePrefix(int flags) {
        Layout layout = instruction.layout;
        return layout.accessFormat == AccessFormat.TAPE ? layout.tapeWritePrefixes.get(flags >>> 2) : layout.writePrefix;
    }

    private String displayValue(int relative) {
        int at = offset + relative;
        switch (data[at]) {
            case INTEGER_VALUE: return Integer.toString((int) get(at + 1, Integer.BYTES));
            case LONG_VALUE: return Long.toString(get(at + 1, Long.BYTES));
            case TEXT_VALUE: return new String(data, at + 5, (int) get(at + 1, Integer.BYTES), StandardCharsets.UTF_8);
            default: throw new AssertionError("Invalid packed value");
        }
    }

    private long value(int index, int base) {
        Layout layout = instruction.layout;
        Field field = layout.fields.get(index);
        if (field.width == 0) {
            return get(base + layout.offsets[field.high], 1) << 8 | get(base + layout.offsets[field.low], 1);
        }
        return get(base + layout.offsets[index], field.width);
    }

    private long get(int position, int width) {
        long value = 0;
        for (int i = 0; i < width; i++) value = (value << 8) | (data[position + i] & 0xFFL);
        return value;
    }

    private static void put(byte[] data, int position, long value, int width) {
        for (int i = position + width - 1; i >= position; i--) {
            data[i] = (byte) value;
            value >>>= 8;
        }
    }

    public int encodedSize() {
        return 3 * Integer.BYTES + instruction.layout.encoded.length + instruction.encodedInstruction.length + length;
    }

    public void write(ByteBuffer out) {
        byte[] schema = instruction.layout.encoded;
        byte[] encoding = instruction.encodedInstruction;
        out.putInt(schema.length).put(schema);
        out.putInt(encoding.length).put(encoding);
        out.putInt(length).put(data, offset, length);
    }

    public static StepView read(String summary, OptionalInt location, DataInputStream in) throws IOException {
        try {
            Layout layout = Layout.read(in);
            String encoding = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(readBytes(in, MAX_SCHEMA_BYTES))).toString();
            int maximum = layout.hasValues ? MAX_DATA_BYTES
                    : layout.accessesStart() + layout.accessFormat.width * (MAX_ROWS - 1 - layout.fields.size());
            byte[] data = readBytes(in, maximum);
            int accessBytes = data.length - layout.accessesStart();
            if (accessBytes < 0 || (!layout.hasValues && accessBytes % layout.accessFormat.width != 0))
                throw new IOException("Invalid packed instruction data");
            int count = layout.hasValues ? ByteBuffer.wrap(data).getInt() : accessBytes / layout.accessFormat.width;
            if (count < 0 || count >= MAX_ROWS - layout.fields.size() || count > accessBytes / layout.accessFormat.width)
                throw new IOException("Invalid packed access count");
            PackedStep result = new PackedStep(new Instruction(layout, summary, encoding, location), data, 0, data.length, count);
            result.validate();
            return result.view();
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid packed instruction data", invalid);
        }
    }

    private void validate() throws IOException {
        Layout layout = instruction.layout;
        int valuesStart = accessPosition(accessCount);
        for (int i = 0; i < layout.fields.size(); i++) {
            if (layout.fields.get(i).isSymbolic()) {
                int at = layout.stateStart() + layout.offsets[i];
                validateValue((int) get(at, Integer.BYTES), valuesStart);
                validateValue((int) get(at + layout.width, Integer.BYTES) & Integer.MAX_VALUE, valuesStart);
            }
        }
        for (int i = 0; i < accessCount; i++) {
            int at = accessPosition(i);
            switch (layout.accessFormat) {
                case BYTE:
                    if ((data[at] & 0xFE) != 0) throw new IOException("Invalid packed byte-memory access");
                    break;
                case WIDE_BYTE:
                    if ((data[at + 4] & 0xFE) != 0) throw new IOException("Invalid packed byte-memory value");
                    break;
                case WORD:
                    if (accessFlags(at) > 1) throw new IOException("Invalid packed word-memory access");
                    break;
                case TAPE:
                    if ((accessFlags(at) >>> 2) >= layout.tapes.size()) throw new IOException("Invalid packed tape access");
                    validateValue((int) get(at + 5, Integer.BYTES), valuesStart);
                    validateValue((int) get(at + 9, Integer.BYTES), valuesStart);
                    break;
            }
        }
    }

    private void validateValue(int at, int valuesStart) throws IOException {
        if (at < valuesStart || at > length - 5) throw new IOException("Invalid packed value location");
        int bytes;
        switch (data[at]) {
            case INTEGER_VALUE: bytes = 5; break;
            case LONG_VALUE: bytes = 9; break;
            case TEXT_VALUE:
                int size = (int) get(at + 1, Integer.BYTES);
                if (size < 0 || size > MAX_SCHEMA_BYTES * 4) throw new IOException("Invalid packed text size");
                bytes = 5 + size;
                break;
            default: throw new IOException("Invalid packed value type");
        }
        if (bytes > length - at) throw new IOException("Truncated packed value");
    }

    private static byte[] readBytes(DataInputStream in, int maximum) throws IOException {
        int size = in.readInt();
        if (size < 0 || size > maximum) throw new IOException("Invalid packed instruction size");
        byte[] bytes = new byte[size];
        in.readFully(bytes);
        return bytes;
    }
}
