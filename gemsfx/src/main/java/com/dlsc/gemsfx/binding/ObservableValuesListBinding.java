package com.dlsc.gemsfx.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class binds to an ObservableList of ObservableValue objects and updates its value based on
 * the current values of these ObservableValues. It reevaluates its value whenever any of the
 * ObservableValues change. The computed value is determined by applying a transformation function
 * to the list of current values.
 *
 * @param <T> the type held by the ObservableValues in the source list
 * @param <U> the type of the output value after applying the transformation function
 */
public class ObservableValuesListBinding<T, U> extends ObjectBinding<U> {

    private final ObservableList<ObservableValue<T>> source;
    private final Function<ObservableList<T>, U> transformer;
    private final InvalidationListener elementInvalidationListener = obs -> invalidate();
    private final WeakInvalidationListener weakElementInvalidationListener = new WeakInvalidationListener(elementInvalidationListener);
    private final ListChangeListener<ObservableValue<T>> listChangeListener = change -> {
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
    private final WeakListChangeListener<ObservableValue<T>> weakListChangeListener = new WeakListChangeListener<>(listChangeListener);

    /**
     * Constructs a new ObservableValuesListBinding.
     *
     * @param source      the observable list of ObservableValue objects that is the source of the binding
     * @param transformer a function that transforms the list of current values into a computed value of type U
     */
    public ObservableValuesListBinding(ObservableList<ObservableValue<T>> source, Function<ObservableList<T>, U> transformer) {
        this.source = source;
        this.transformer = transformer;

        source.forEach(this::safeAddListener);

        source.addListener(weakListChangeListener);
    }

    private void safeAddListener(ObservableValue<T> item) {
        if (item != null) {
            item.addListener(weakElementInvalidationListener);
        }
    }

    private void safeRemoveListener(ObservableValue<T> item) {
        if (item != null) {
            item.removeListener(weakElementInvalidationListener);
        }
    }

    @Override
    protected U computeValue() {
        return transformer.apply(source.stream().map(ObservableValue::getValue).collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    @Override
    public void dispose() {
        source.forEach(this::safeRemoveListener);
        source.removeListener(weakListChangeListener);
        super.dispose();
    }

}
