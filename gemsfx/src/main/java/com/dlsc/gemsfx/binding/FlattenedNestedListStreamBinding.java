package com.dlsc.gemsfx.binding;

import javafx.collections.ObservableList;

import java.util.stream.Stream;

/**
 * Binds a nested structure of {@code ObservableList<ObservableList<T>>} to a flattened {@code Stream<T>}.
 * This binding class listens for changes in the nested lists and provides a continuously updated stream
 * that represents the flattened view of these lists. This stream can be used for further processing such
 * as collecting into lists, sets, or applying transformations.
 *
 * <p>
 * Each call to {@code getValue()} will provide a new stream based on the current state of the nested lists.</p>
 *
 * @param <T> the type of the elements contained within the nested lists
 */
public class FlattenedNestedListStreamBinding<T> extends AbstractNestedListBinding<T, Stream<T>> {

    public FlattenedNestedListStreamBinding(ObservableList<ObservableList<T>> source) {
        super(source);

        initListeners();
    }

    /**
     * Computes the value of the binding by flattening all elements contained within the source nested lists into a single stream.
     * This method provides a stream of all elements, which can be collected or further processed by the consumer as needed.
     *
     * @return The flattened stream of all elements.
     */
    @Override
    protected Stream<T> computeValue() {
        return flattenSource();
    }

}
