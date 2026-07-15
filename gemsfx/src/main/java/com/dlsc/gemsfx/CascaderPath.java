package com.dlsc.gemsfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Immutable root-to-item path snapshot used by {@link CascaderView} and
 * {@link Cascader}.
 *
 * <p>A path captures only its node identity chain ({@link #getItems() items}).
 * That is what a path fundamentally is — which nodes were traversed — and it
 * never goes stale: changing the view's {@code converter} or an item's value
 * does not alter which nodes this path represents. Display text is a derived
 * view, not part of the snapshot: resolve it from the items with whatever scheme
 * you need (the view's {@code converter}, or {@link #toString()} as a
 * value-based fallback).
 *
 * @param <T> application value type
 */
public final class CascaderPath<T> {

    private final List<CascaderItem<T>> items;

    /**
     * Creates a path snapshot from the given items.
     *
     * @param items root-to-item sequence
     */
    public CascaderPath(List<CascaderItem<T>> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Returns the path items.
     *
     * @return immutable path items
     */
    public List<CascaderItem<T>> getItems() {
        return items;
    }

    /**
     * Returns the path values, derived from the items.
     *
     * @return immutable path values
     */
    public List<T> getValues() {
        List<T> values = new ArrayList<>(items.size());
        for (CascaderItem<T> item : items) {
            values.add(item.getValue());
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns the leaf item.
     *
     * @return leaf item, or {@code null} for an empty path
     */
    public CascaderItem<T> getLeaf() {
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /**
     * Returns whether this path contains the given item instance, compared by
     * reference (identity).
     *
     * @param item item to test
     * @return {@code true} if the item instance is in this path
     */
    public boolean contains(CascaderItem<T> item) {
        for (CascaderItem<T> candidate : items) {
            if (candidate == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * Two paths are equal when they traverse the same item instances in the same
     * order. Item instances are compared by reference (identity), matching this
     * snapshot's identity-chain contract: a subclass overriding {@code equals} does
     * not affect path equality, so distinct nodes with equal values stay distinct
     * and a re-selection of the same leaf compares equal (suppressing spurious
     * change events).
     *
     * @param obj object to compare
     * @return {@code true} if the other object is a path over the same item instances
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CascaderPath<?>)) {
            return false;
        }
        CascaderPath<?> other = (CascaderPath<?>) obj;
        if (items.size() != other.items.size()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != other.items.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}, derived from the
     * identity hash of each item in the chain.
     *
     * @return the path hash code
     */
    @Override
    public int hashCode() {
        int hash = 1;
        for (CascaderItem<T> item : items) {
            hash = 31 * hash + System.identityHashCode(item);
        }
        return hash;
    }

    /**
     * Returns a slash-separated value-based fallback representation. This uses
     * {@link CascaderItem#toString()} (the value), not the view's
     * {@code converter}, so it is for debugging and not guaranteed to match the
     * visible cascader text — resolve display text from {@link #getItems()} with
     * the view's {@code converter} for that.
     *
     * @return slash-separated value-based path text
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" / ");
        for (CascaderItem<T> item : items) {
            joiner.add(String.valueOf(item));
        }
        return joiner.toString();
    }
}
