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
 * Binds an {@link ObservableList} of items to a computed value based on the elements of their associated nested {@link ObservableList}s.
 * This binding listens for changes not only in the top-level list but also in the nested lists of each item.
 * It is useful for aggregating or computing values dynamically as the lists change.
 *
 * @param <T> the type of the elements in the source list
 * @param <S> the type of the elements in the nested lists
 * @param <R> the type of the result computed from the nested lists
 */
public class AggregatedListBinding<T, S, R> extends ObjectBinding<R> {

    private final Function<T, ObservableList<S>> itemToListFunction;
    private final ObservableList<T> sourceList;
    private final Function<List<S>, R> aggregationFunction;

    private final ListChangeListener<Object> nestedListChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded() || change.wasRemoved()) {
                invalidate();
                break;
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
     * Constructs a new AggregatedListBinding.
     *
     * @param source              the observable list of source items
     * @param itemToListFunction  a function to retrieve the observable list from each source item
     * @param aggregationFunction a function to compute a result from all elements in the nested lists
     */
    public AggregatedListBinding(ObservableList<T> source, final Function<T, ObservableList<S>> itemToListFunction, Function<List<S>, R> aggregationFunction) {
        this.sourceList = source;
        this.itemToListFunction = itemToListFunction;
        this.aggregationFunction = aggregationFunction;

        sourceList.stream()
                .map(itemToListFunction)
                .filter(Objects::nonNull)
                .forEach(list -> list.addListener(weakNestedListChangeListener));

        source.addListener(weakSourceListChangeListener);
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
        return aggregationFunction.apply(
                sourceList.stream()
                        .map(itemToListFunction)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void dispose() {
        sourceList.stream()
                .map(itemToListFunction)
                .filter(Objects::nonNull)
                .forEach(list -> list.removeListener(weakNestedListChangeListener));
        sourceList.removeListener(weakSourceListChangeListener);
    }

}
