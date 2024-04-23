package com.dlsc.gemsfx.binding;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a generic binding that aggregates results from a nested collection structure.
 * This class listens to changes not only in the top-level observable list of type T but also in the nested observable lists of type S associated with each T.
 * The binding performs aggregation operations over these lists to produce a final result of type R, making it ideal for applications with dynamic data models.
 *
 * <p>This class is designed to work with a stable source of {@link ObservableList}s provided by the {@code itemToListFunction}.
 * It is crucial for the correct operation of this binding that the same instance of {@link ObservableList} is returned for any given item in the lifecycle of that item.
 * Changing the underlying {@link ObservableList} instances during the lifecycle can lead to unpredictable behavior and potential memory leaks.</p>
 *
 * <p><strong>Important Considerations:</strong></p>
 * <ul>
 *   <li><strong>Stable List References:</strong> The {@code itemToListFunction} must consistently return the same {@link ObservableList} instance for any given item
 *       throughout its lifecycle. If the lists are stored in properties such as {@link javafx.beans.property.ObjectProperty} or {@link javafx.beans.property.SimpleListProperty},
 *       ensure that these properties do not get reassigned to new lists as this can disrupt the behavior of the binding and lead to memory leaks.</li>
 *   <li><strong>Managing Listeners:</strong> Proper management of listeners is critical. Ensure that any list obtained from the {@code itemToListFunction}
 *       has listeners appropriately added or removed. This may require overriding property setters or employing property change listeners to manage list listeners when the property's value changes.</li>
 * </ul>
 *
 * @param <T> the type of elements in the source list
 * @param <S> the type of elements in the nested lists
 * @param <U> the intermediate aggregation type
 * @param <R> the final result type
 */
public class GeneralAggregatedListBinding<T, S, U, R> extends ObjectBinding<R> {

    private final Function<T, ObservableList<S>> itemToListFunction;
    private final ObservableList<T> source;
    private final Function<List<S>, U> aggregationFunction;
    private final Function<Stream<U>, R> finalAggregationFunction;

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
                change.getRemoved().forEach(this::convertToListAndSafeRemoveListener);
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::convertToListAndSafeAddListener);
            }
            invalidate();
        }
    };

    private final WeakListChangeListener<T> weakSourceListChangeListener = new WeakListChangeListener<>(sourceListChangeListener);

    /**
     * Constructs a new GeneralAggregatedListBinding.
     * This constructor initializes the binding based on a source list, a function to map each element of the source to an observable list,
     * and two aggregation functions. It sets up listeners on the source list and all nested lists, allowing the binding to react dynamically
     * to changes within these lists.
     *
     * @param source                   The observable list of source items. This list should be stable during the lifecycle of the binding,
     *                                 meaning that elements should not be replaced with different instances to ensure reliable behavior.
     * @param itemToListFunction       A function that maps each element of the source list, T, to an observable list of S.
     *                                 This function must consistently return the same instance of ObservableList for any given item
     *                                 throughout its lifecycle to avoid erratic behavior and potential memory leaks.
     * @param aggregationFunction      A function that aggregates a list of S into an intermediate result U, based on the current data
     *                                 in those lists.
     * @param finalAggregationFunction A function that aggregates a list of intermediate results U into the final result R, which represents
     *                                 the aggregate of the entire structure.
     */
    public GeneralAggregatedListBinding(ObservableList<T> source, Function<T, ObservableList<S>> itemToListFunction, Function<List<S>, U> aggregationFunction, Function<Stream<U>, R> finalAggregationFunction) {
        this.source = Objects.requireNonNull(source, "Source list cannot be null");
        this.itemToListFunction = Objects.requireNonNull(itemToListFunction, "Item-to-list function cannot be null");
        this.aggregationFunction = Objects.requireNonNull(aggregationFunction, "Aggregation function cannot be null");
        this.finalAggregationFunction = Objects.requireNonNull(finalAggregationFunction, "Final aggregation function cannot be null");

        this.source.stream()
                .map(itemToListFunction)
                .forEach(this::safeAddListener);

        this.source.addListener(weakSourceListChangeListener);
    }

    private void convertToListAndSafeAddListener(T item) {
        ObservableList<S> list = itemToListFunction.apply(item);
        safeAddListener(list);
    }

    private void safeAddListener(ObservableList<S> list) {
        if (list != null) {
            list.addListener(weakNestedListChangeListener);
        }
    }

    private void convertToListAndSafeRemoveListener(T item) {
        ObservableList<S> list = itemToListFunction.apply(item);
        safeRemoveListener(list);
    }

    private void safeRemoveListener(ObservableList<S> list) {
        if (list != null) {
            list.removeListener(weakNestedListChangeListener);
        }
    }

    @Override
    protected R computeValue() {
        return finalAggregationFunction.apply(source.stream()
                .map(itemToListFunction)
                .map(aggregationFunction));
    }

    @Override
    public void dispose() {
        source.stream()
                .map(itemToListFunction)
                .forEach(this::safeRemoveListener);
        source.removeListener(weakSourceListChangeListener);
    }

}
