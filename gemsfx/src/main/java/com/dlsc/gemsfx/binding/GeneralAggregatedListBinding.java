package com.dlsc.gemsfx.binding;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a generic binding that aggregates results from a nested collection structure.
 * It listens to changes in an observable list of type T, and each T element is associated with an observable list of type S.
 * The class performs aggregation operations over the lists and produces a final result of type R.
 *
 * @param <T> the type of elements in the source list
 * @param <S> the type of elements in the nested lists
 * @param <U> the intermediate aggregation type
 * @param <R> the final result type
 */
public class GeneralAggregatedListBinding<T, S, U, R> extends ObjectBinding<R> {

    private final Function<T, ObservableList<S>> itemToListFunction;
    private final ObservableList<T> sourceList;
    private final Function<List<S>, U> aggregationFunction;
    private final Function<List<U>, R> finalAggregationFunction;

    private final ListChangeListener<Object> nestedListChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded() || change.wasRemoved()) {
                invalidate();
            }
        }
    };

    private final WeakListChangeListener<Object> weakNestedListChangeListener = new WeakListChangeListener<>(nestedListChangeListener);

    private final ListChangeListener<T> sourceListChangeListener = change -> {
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

    private final WeakListChangeListener<T> weakSourceListChangeListener = new WeakListChangeListener<>(sourceListChangeListener);

    /**
     * Constructs a new GeneralAggregatedListBinding.
     *
     * @param source                   the source observable list of T
     * @param itemToListFunction       a function mapping T to an observable list of S
     * @param aggregationFunction      a function to aggregate a list of S into U
     * @param finalAggregationFunction a function to aggregate a list of U into R
     */
    public GeneralAggregatedListBinding(ObservableList<T> source, Function<T, ObservableList<S>> itemToListFunction, Function<List<S>, U> aggregationFunction, Function<List<U>, R> finalAggregationFunction) {
        this.sourceList = source;
        this.itemToListFunction = itemToListFunction;
        this.aggregationFunction = aggregationFunction;
        this.finalAggregationFunction = finalAggregationFunction;

        sourceList.stream()
                .map(itemToListFunction)
                .filter(Objects::nonNull)
                .forEach(list -> list.addListener(weakNestedListChangeListener));

        source.addListener(weakSourceListChangeListener);
        bind(source);
    }

    private void safeAddListener(T item) {
        ObservableList<S> list = itemToListFunction.apply(item);
        if (list != null) {
            list.addListener(weakNestedListChangeListener);
        }
    }

    private void safeRemoveListener(T item) {
        ObservableList<S> list = itemToListFunction.apply(item);
        if (list != null) {
            list.removeListener(weakNestedListChangeListener);
        }
    }

    @Override
    protected R computeValue() {
        List<U> individualResults = sourceList.stream()
                .map(itemToListFunction)
                .map(aggregationFunction)
                .collect(Collectors.toList());

        return finalAggregationFunction.apply(individualResults);
    }

    @Override
    public void dispose() {
        sourceList.stream()
                .map(itemToListFunction)
                .filter(Objects::nonNull)
                .forEach(list -> list.removeListener(weakNestedListChangeListener));
        sourceList.removeListener(weakSourceListChangeListener);
        unbind(sourceList);
        super.dispose();
    }

}
