package com.dlsc.gemsfx.binding;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.function.Function;

/**
 * This class represents a binding on an observable list of observable lists. It listens to changes
 * within both the outer list and any of the inner lists, and recalculates its value when any change is detected.
 * The value is computed based on a transformation function applied to the entire structure of the list.
 *
 * @param <T> the type of elements within the inner observable lists
 * @param <U> the type of the computed value based on the entire nested list structure
 */
public class NestedListBinding<T, U> extends ObjectBinding<U> {

    private final ObservableList<ObservableList<T>> source;
    private final Function<ObservableList<ObservableList<T>>, U> transformer;
    private final ListChangeListener<T> innerListChangeListener = change -> invalidate();
    private final WeakListChangeListener<T> weakInnerListChangeListener = new WeakListChangeListener<>(innerListChangeListener);
    private final ListChangeListener<ObservableList<T>> outerListChangeListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().forEach(this::safeRemoveListener);
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::safeAddListener);
            }
        }
        invalidate();
    };
    private final WeakListChangeListener<ObservableList<T>> weakOuterListChangeListener = new WeakListChangeListener<>(outerListChangeListener);

    /**
     * Constructs a new NestedListBinding.
     *
     * @param source      the observable list of observable lists that is the source of the binding
     * @param transformer a function that transforms the nested list structure into a computed value of type U
     */
    public NestedListBinding(ObservableList<ObservableList<T>> source, Function<ObservableList<ObservableList<T>>, U> transformer) {
        this.source = source;
        this.transformer = transformer;

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

    @Override
    protected U computeValue() {
        return transformer.apply(source);
    }

    @Override
    public void dispose() {
        source.forEach(this::safeRemoveListener);
        source.removeListener(weakOuterListChangeListener);
    }

}
