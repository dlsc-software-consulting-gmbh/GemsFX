package com.dlsc.gemsfx;

import javafx.collections.ModifiableObservableListBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An observable list that rejects {@code null} elements at insertion with a clear
 * {@link NullPointerException}, so a stray {@code null} fails fast at the call site
 * instead of surfacing later as an obscure NPE inside a change listener or a
 * derived-state traversal.
 *
 * <p>Every mutation funnels through {@link #doAdd} / {@link #doSet}, which reject
 * {@code null}; the bulk entry points additionally pre-validate the whole argument
 * so an invalid bulk call ({@code addAll} / {@code setAll} containing a
 * {@code null}) leaves the list unchanged rather than partially mutated.
 *
 * @param <E> element type
 */
final class NonNullObservableList<E> extends ModifiableObservableListBase<E> {

    private final List<E> backing = new ArrayList<>();
    private final String elementName;

    /**
     * Creates an empty null-rejecting list.
     *
     * @param elementName name of an element, used in the rejection message
     */
    NonNullObservableList(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public E get(int index) {
        return backing.get(index);
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    protected void doAdd(int index, E element) {
        backing.add(index, requireNonNull(element));
    }

    @Override
    protected E doSet(int index, E element) {
        return backing.set(index, requireNonNull(element));
    }

    @Override
    protected E doRemove(int index) {
        return backing.remove(index);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        requireNoNulls(elements);
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> elements) {
        requireNoNulls(elements);
        return super.addAll(index, elements);
    }

    @Override
    public boolean setAll(Collection<? extends E> elements) {
        requireNoNulls(elements);
        return super.setAll(elements);
    }

    private E requireNonNull(E element) {
        return Objects.requireNonNull(element, elementName + " must not be null");
    }

    private void requireNoNulls(Collection<? extends E> elements) {
        for (E element : elements) {
            requireNonNull(element);
        }
    }
}
