/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.helpers.Bits;
import net.jcip.annotations.Immutable;

import java.util.*;

/**
 * A decoded, but not yet disassembled instruction.
 * Supposed to be immutable, although some fields (arrays) are not protected for performance reasons.
 * It is ready to be put in a hashed collections (implements equals() and hashCode()).
 */
@Immutable
public class DecodedInstruction {
    // A rule key can carry raw operand bits returned by a subrule.
    private static final byte HAS_BITS = 4;

    /**
     * Constants
     */
    public final int[] constants;

    /**
     * Strings
     */
    public final String[] strings;

    /**
     * Bits objects
     */
    public final Bits[] bits;

    /**
     * Number of keys. Keys go from 1..keyCount
     */
    public final int keyCount;

    /**
     * Instruction image
     */
    public final byte[] image;

    /**
     * Constructs a new DecodedInstruction.
     */
    public DecodedInstruction(byte[] image, int keyCount, byte[] keyStates, int[] constants, String[] strings,
                              int[] bitValues, int[] bitLengths) {
        this.image = Arrays.copyOf(image, image.length);
        this.constants = Arrays.copyOf(constants, constants.length);
        this.strings = Arrays.copyOf(strings, strings.length);
        this.keyCount = keyCount;

        this.bits = new Bits[keyStates.length];
        for (int key = 0; key < keyStates.length; key++) {
            if ((keyStates[key] & HAS_BITS) != 0) {
                bits[key] = new Bits(bitValues[key], bitLengths[key]);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DecodedInstruction)) return false;
        DecodedInstruction that = (DecodedInstruction) o;
        return Objects.deepEquals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(image);
    }

    @Override
    public String toString() {
        return Arrays.toString(image);
    }
}
