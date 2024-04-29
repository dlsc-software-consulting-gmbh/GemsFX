package com.dlsc.gemsfx.binding;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Binds an {@link ObservableList} of items to a computed value based on the elements of their associated nested {@link ObservableList}s.
 * This binding listens for changes not only in the top-level list but also in the nested lists of each item.
 * It aggregates or computes values dynamically as the lists change, making it ideal for applications where the data model is highly dynamic.
 *
 * <p>This class is designed to be used with a consistent source of {@link ObservableList}s provided by the {@code itemToListFunction}.
 * The function must return the same {@link ObservableList} instance for any given item throughout its lifecycle to ensure
 * correct behavior and prevent memory leaks.</p>
 *
 * <p><strong>Important Considerations:</strong></p>
 * <ul>
 *   <li><strong>Stable List References:</strong> The {@code itemToListFunction} should not return different instances of {@link ObservableList}
 *       over time for the same item. If the lists are stored in properties such as {@link ObjectProperty} or {@link SimpleListProperty},
 *       ensure that these properties are not reassigned new lists during the lifecycle of an item, as this can lead to erratic behavior and potential
 *       memory leaks if listeners are not removed from old lists.</li>
 *   <li><strong>Managing Listeners:</strong> If using properties to store the lists, manage listeners carefully. Ensure that any list set on a property
 *       has listeners appropriately added or removed to prevent memory leaks. This may require overriding property setters or using property
 *       change listeners to add/remove list listeners when the property's value changes.</li>
 * </ul>
 *
 * @param <T> the type of the elements in the source list
 * @param <S> the type of the elements in the nested lists
 * @param <R> the type of the result computed from the stream of elements in the nested lists
 */
public class AggregatedListBinding<T, S, R> extends ObjectBinding<R> {

    private final Function<T, ObservableList<S>> itemToListFunction;
    private final ObservableList<T> source;
    private final Function<Stream<S>, R> aggregationFunction;

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
     * Constructs a new AggregatedListBinding.
     *
     * @param source              the observable list of source items that should be stable (not dynamically replaced).
     * @param itemToListFunction  a function to retrieve an observable list from each source item, which must consistently
     *                            return the same observable list instance for any given item throughout its lifecycle.
     * @param aggregationFunction a function to compute a result from all elements in the nested lists, based on current
     *                            data in those lists.
     */
    public AggregatedListBinding(ObservableList<T> source, final Function<T, ObservableList<S>> itemToListFunction, Function<Stream<S>, R> aggregationFunction) {
        this.source = Objects.requireNonNull(source, "Source list cannot be null");
        this.itemToListFunction = Objects.requireNonNull(itemToListFunction, "Item-to-list function cannot be null");
        this.aggregationFunction = Objects.requireNonNull(aggregationFunction, "Aggregation function cannot be null");

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
        return aggregationFunction.apply(
                source.stream()
                        .map(itemToListFunction)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream));
    }

    @Override
    public void dispose() {
        source.stream()
                .map(itemToListFunction)
                .forEach(this::safeRemoveListener);
        source.removeListener(weakSourceListChangeListener);
    }

}
