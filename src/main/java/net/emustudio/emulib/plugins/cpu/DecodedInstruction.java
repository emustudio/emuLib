/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.helpers.Bits;

import java.util.*;

/**
 * A decoded, but not yet disassembled instruction.
 * Basic unit is a byte. Instruction image is an int stored in big-endian.
 */
public class DecodedInstruction {
    // A rule key can carry the constant decoded from a string-returning variant.
    private static final byte HAS_CONSTANT = 1;
    // A rule key can carry the original string returned by a named variant.
    private static final byte HAS_STRING = 2;
    // A rule key can carry raw operand bits returned by a subrule.
    private static final byte HAS_BITS = 4;
    // Keep the default footprint small; sparse or high rule codes grow on demand.
    private static final int INITIAL_KEY_CAPACITY = 8;

    /**
     * Constructs a new DecodedInstruction.
     */
    public DecodedInstruction() {
    }

    private byte[] keyStates = new byte[INITIAL_KEY_CAPACITY];
    private int[] constants = new int[INITIAL_KEY_CAPACITY];
    private String[] strings = new String[INITIAL_KEY_CAPACITY];
    private int[] bitValues = new int[INITIAL_KEY_CAPACITY];
    private int[] bitLengths = new int[INITIAL_KEY_CAPACITY];
    private Bits[] bitObjects = new Bits[INITIAL_KEY_CAPACITY];
    private int[] keys = new int[INITIAL_KEY_CAPACITY];
    private int keyCount;
    private final KeySetView keySetView = new KeySetView();

    private byte[] image;

    /**
     * Adds the recognized string-returning variant to the instruction.
     *
     * @param key      the key (rule code)
     * @param string   the string which the recognized variant returned
     * @param constant the constant obtained from the string
     */
    public void add(int key, String string, int constant) {
        ensureKeyCapacity(key);
        if (keyStates[key] == 0) {
            rememberKey(key);
        }
        keyStates[key] |= HAS_CONSTANT | HAS_STRING;
        constants[key] = constant;
        strings[key] = string;
    }

    /**
     * Adds the recognized subrule-returning variant to the instruction.
     *
     * @param key    the rule code
     * @param bits   the bit sequence in little-endian, padded to whole bytes
     * @param length bit length (in bits)
     */
    public void add(int key, int bits, int length) {
        ensureKeyCapacity(key);
        if (keyStates[key] == 0) {
            rememberKey(key);
        }
        keyStates[key] |= HAS_BITS;
        bitValues[key] = bits;
        bitLengths[key] = length;
        bitObjects[key] = null;
    }

    /**
     * Returns true if the instruction contains the specified key.
     *
     * @param key the rule code
     * @return true if the instruction contains the key, false otherwise
     */
    public boolean hasKey(int key) {
        return key >= 0 && key < keyStates.length && keyStates[key] != 0;
    }

    /**
     * Returns the constant value to which the given key is mapped.
     *
     * @param key the key
     * @return the constant; or -1 if the key is not mapped to a constant
     */
    public int get(int key) {
        return hasState(key, HAS_CONSTANT) ? constants[key] : -1;
    }

    /**
     * Returns the string value to which the given key is mapped.
     *
     * @param key the key
     * @return the string; or null if the key is not mapped to a string
     */
    public String getString(int key) {
        return hasState(key, HAS_STRING) ? strings[key] : null;
    }

    /**
     * Returns the bit sequence to which the given key is mapped.
     *
     * @param key the key
     * @return the bit sequence; or null if the key is not mapped to bits
     */
    public Bits getBits(int key) {
        if (!hasState(key, HAS_BITS)) {
            return null;
        }

        Bits bits = bitObjects[key];
        if (bits == null) {
            bits = new Bits(bitValues[key], bitLengths[key]);
            bitObjects[key] = bits;
        }
        return bits;
    }

    /**
     * Returns the binary image of the whole instruction.
     *
     * @return the binary image
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the binary image of the whole instruction.
     *
     * @param image the binary image
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Returns the length of the instruction in the memory.
     *
     * @return the length, in bytes
     */
    public int getLength() {
        return image.length;
    }

    /**
     * Returns the set of all keys contained in this instruction.
     *
     * @return the set of keys
     */
    public Set<Integer> getKeys() {
        return keySetView;
    }

    private boolean hasState(int key, int state) {
        return key >= 0 && key < keyStates.length && (keyStates[key] & state) != 0;
    }

    private void rememberKey(int key) {
        if (keyCount == keys.length) {
            keys = Arrays.copyOf(keys, keyCount << 1);
        }
        keys[keyCount++] = key;
        keySetView.invalidateHash();
    }

    private void ensureKeyCapacity(int key) {
        if (key < keyStates.length) {
            return;
        }

        int newLength = keyStates.length;
        while (newLength <= key) {
            newLength <<= 1;
        }

        keyStates = Arrays.copyOf(keyStates, newLength);
        constants = Arrays.copyOf(constants, newLength);
        strings = Arrays.copyOf(strings, newLength);
        bitValues = Arrays.copyOf(bitValues, newLength);
        bitLengths = Arrays.copyOf(bitLengths, newLength);
        bitObjects = Arrays.copyOf(bitObjects, newLength);
    }

    private final class KeySetView extends AbstractSet<Integer> {
        private boolean hashComputed;
        private int hash;

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return index < keyCount;
                }

                @Override
                public Integer next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return keys[index++];
                }
            };
        }

        @Override
        public int size() {
            return keyCount;
        }

        @Override
        public boolean contains(Object value) {
            if (!(value instanceof Integer)) {
                return false;
            }
            return hasKey((Integer) value);
        }

        @Override
        public int hashCode() {
            if (!hashComputed) {
                int computedHash = 0;
                for (int i = 0; i < keyCount; i++) {
                    computedHash += keys[i];
                }
                hash = computedHash;
                hashComputed = true;
            }
            return hash;
        }

        private void invalidateHash() {
            hashComputed = false;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(image);
    }
}
