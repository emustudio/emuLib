/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Ordered pipeline of {@link Bits} operations.
 */
public final class BitOperations implements UnaryOperator<Bits> {
    private final List<UnaryOperator<Bits>> operations = new ArrayList<>();

    /**
     * Creates a new ordered pipeline for composing {@link Bits} transformations.
     *
     * @return operations pipeline
     */
    public static BitOperations builder() {
        return new BitOperations();
    }

    /**
     * Applies the configured operation pipeline in declaration order.
     *
     * @param bits source bits
     * @return transformed bits
     */
    @Override
    public Bits apply(Bits bits) {
        Bits result = Objects.requireNonNull(bits, "bits must not be null");
        for (UnaryOperator<Bits> operation : operations) {
            result = Objects.requireNonNull(operation.apply(result), "operation must not return null");
        }
        return result;
    }

    /**
     * Appends a custom operation to the pipeline.
     *
     * @param operation operation to append
     * @return this pipeline
     */
    public BitOperations then(UnaryOperator<Bits> operation) {
        operations.add(Objects.requireNonNull(operation, "operation must not be null"));
        return this;
    }

    /**
     * Appends {@link Bits#reverseBytes()} to the pipeline.
     *
     * @return this pipeline
     */
    public BitOperations reverseBytes() {
        return then(Bits::reverseBytes);
    }

    /**
     * Appends {@link Bits#reverseBits()} to the pipeline.
     *
     * @return this pipeline
     */
    public BitOperations reverseBits() {
        return then(Bits::reverseBits);
    }

    /**
     * Appends {@link Bits#absolute()} to the pipeline.
     *
     * @return this pipeline
     */
    public BitOperations absolute() {
        return then(Bits::absolute);
    }

    /**
     * Appends {@link Bits#shiftLeft()} to the pipeline.
     *
     * @return this pipeline
     */
    public BitOperations shiftLeft() {
        return then(Bits::shiftLeft);
    }

    /**
     * Appends {@link Bits#shiftRight()} to the pipeline.
     *
     * @return this pipeline
     */
    public BitOperations shiftRight() {
        return then(Bits::shiftRight);
    }
}
