/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2012  Matúš Sulír
 * Copyright (C) 2012-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.helpers.Bits;

import java.util.*;

/**
 * A decoded, but not yet disassembled instruction.
 *
 * Basic unit is a byte. Instruction image is an int stored in big-endian.
 */
public class DecodedInstruction {
    private final Map<Integer, Integer> constants = new HashMap<>();
    private final Map<Integer, String> strings = new HashMap<>();
    private final Map<Integer, Bits> bits = new HashMap<>();

    private byte[] image;

    /**
     * Adds the recognized string-returning variant to the instruction.
     * @param key the key (rule code)
     * @param string the string which the recognized variant returned
     * @param constant the constant obtained from the string
     */
    public void add(int key, String string, int constant) {
        constants.put(key, constant);
        strings.put(key, string);
    }

    /**
     * Adds the recognized subrule-returning variant to the instruction.
     * @param key the rule code
     * @param bits the bit sequence in little-endian, padded to whole bytes
     * @param length bit length (in bits)
     */
    public void add(int key, int bits, int length) {
        this.bits.put(key, new Bits(bits, length));
    }

    /**
     * Returns true if the instruction contains the specified key.
     * @param key the rule code
     * @return true if the instruction contains the key, false otherwise
     */
    public boolean hasKey(int key) {
        return bits.containsKey(key) || constants.containsKey(key);
    }

    /**
     * Returns the constant value to which the given key is mapped.
     * @param key the key
     * @return the constant; or -1 if the key is not mapped to a constant
     */
    public int get(int key) {
        Integer value = constants.get(key);
        return (value != null) ? value : -1;
    }

    /**
     * Returns the string value to which the given key is mapped.
     * @param key the key
     * @return the string; or null if the key is not mapped to a string
     */
    public String getString(int key) {
        return strings.get(key);
    }

    /**
     * Returns the bit sequence to which the given key is mapped.
     * @param key the key
     * @return the bit sequence; or null if the key is not mapped to bits
     */
    public Bits getBits(int key) {
        return bits.get(key);
    }

    /**
     * Returns the binary image of the whole instruction.
     * @return the binary image
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the binary image of the whole instruction.
     * @param image the binary image
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Returns the length of the instruction in the memory.
     * @return the length, in bytes
     */
    public int getLength() {
        return image.length;
    }

    /**
     * Returns the set of all keys contained in this instruction.
     * @return the set of keys
     */
    public Set<Integer> getKeys() {
        Set<Integer> keys = new HashSet<>(constants.keySet());
        keys.addAll(bits.keySet());
        return keys;
    }

    @Override
    public String toString() {
        return Arrays.toString(image);
    }
}
