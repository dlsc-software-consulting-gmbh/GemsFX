package com.dlsc.gemsfx.binding;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Provides a base for creating bindings based on a nested structure of observable lists.
 * This abstract class handles the addition and removal of listeners to these nested observable lists
 * to facilitate easy updates and maintenance of bindings that depend on their content.
 *
 * <p>The class uses weak listeners to prevent memory leaks and ensure that lists can be garbage collected
 * when no longer in use. Changes in any of the nested lists will trigger an invalidation in the binding,
 * prompting a re-computation of its value based on the specific implementation of {@link #computeValue()} in the subclass.</p>
 *
 * @param <T> the type of the elements in the nested observable lists
 * @param <U> the type of the value computed by the binding, based on the nested list structure
 */
public abstract class AbstractNestedListBinding<T, U> extends ObjectBinding<U> {

    protected final ObservableList<ObservableList<T>> source;
    private final ListChangeListener<T> innerListChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded() || change.wasRemoved()) {
                invalidate();
                break;
            }
        }
    };
    private final WeakListChangeListener<T> weakInnerListChangeListener = new WeakListChangeListener<>(innerListChangeListener);
    private final ListChangeListener<ObservableList<T>> outerListChangeListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().forEach(this::safeRemoveListener);
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::safeAddListener);
            }
            invalidate();
        }
    };
    private final WeakListChangeListener<ObservableList<T>> weakOuterListChangeListener = new WeakListChangeListener<>(outerListChangeListener);

    /**
     * Constructs an AbstractNestedListBinding with the specified source of nested observable lists.
     *
     * @param source The observable list of observable lists that serves as the source for this binding.
     */
    public AbstractNestedListBinding(ObservableList<ObservableList<T>> source) {
        this.source = Objects.requireNonNull(source, "Source list cannot be null");
    }

    /**
     * Initializes listeners on the source list and any existing nested lists. This method should be
     * called after subclass constructors have fully initialized any necessary fields to ensure
     * that listeners interact with a fully initialized object.
     */
    protected void initListeners() {
        source.forEach(this::safeAddListener);
        source.addListener(weakOuterListChangeListener);
    }

    private void safeAddListener(ObservableList<T> list) {
        if (list != null) {
            list.addListener(weakInnerListChangeListener);
        }
    }

    private void safeRemoveListener(ObservableList<T> list) {
        if (list != null) {
            list.removeListener(weakInnerListChangeListener);
        }
    }

    protected Stream<T> flattenSource() {
        return source.stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream);
    }

    @Override
    public void dispose() {
        source.forEach(this::safeRemoveListener);
        source.removeListener(weakOuterListChangeListener);
    }

}
