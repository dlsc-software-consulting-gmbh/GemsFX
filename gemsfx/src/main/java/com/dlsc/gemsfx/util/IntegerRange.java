package com.dlsc.gemsfx.util;

/**
 * Represents an inclusive range of integers. This record defines a start and end point for the range, both inclusive.
 */
public class IntegerRange {

    private final int fromInclusive;
    private final int toInclusive;

    public IntegerRange(int fromInclusive, int toInclusive) {
        this.fromInclusive = fromInclusive;
        this.toInclusive = toInclusive;
    }

    /**
     * Constructs an IntegerRange with one endpoint specified by the given number and the other endpoint set to 0.
     */
    public IntegerRange(int toInclusive) {
        this(0, toInclusive);
    }

    /**
     * Retrieves the maximum value in the range.
     *
     * @return The larger of the two numbers defining the range.
     */
    public int getMax() {
        return Math.max(fromInclusive, toInclusive);
    }

    /**
     * Retrieves the minimum value in the range.
     *
     * @return The smaller of the two numbers defining the range.
     */
    public int getMin() {
        return Math.min(fromInclusive, toInclusive);
    }

}

