package com.dlsc.gemsfx.binding;

import javafx.collections.ObservableList;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Binds a nested structure of {@code ObservableList<ObservableList<T>>} to a transformed output {@code U}.
 * This class takes a nested list and applies a transformation function to a flattened version of that list,
 * producing a result of type {@code U}. This is useful for performing complex transformations on nested data
 * structures in a declarative way.
 *
 * @param <T> the type of elements within the nested lists
 * @param <U> the type of the output produced by the transformation function
 */
public class TransformedFlattenedNestedListStreamBinding<T, U> extends AbstractNestedListBinding<T, U> {

    private final Function<Stream<T>, U> transformer;

    /**
     * Constructs a new TransformedNestedListBinding with the specified source of nested observable lists
     * and a transformation function. The function will be applied to a flattened stream of the nested lists,
     * allowing for the transformation of nested list elements into a single output value of type {@code U}.
     *
     * @param source      the observable list of observable lists that serves as the source for this binding
     * @param transformer a function that takes a flattened stream of elements of type {@code T} and returns
     *                    a transformed value of type {@code U}
     */
    public TransformedFlattenedNestedListStreamBinding(ObservableList<ObservableList<T>> source, Function<Stream<T>, U> transformer) {
        super(source);
        this.transformer = Objects.requireNonNull(transformer, "Transformer function cannot be null");

        initListeners();
    }

    /**
     * Computes the transformed value by applying the transformation function to the flattened source stream.
     *
     * @return the transformed value of type {@code U}, as derived from the flattened nested lists
     */
    @Override
    protected U computeValue() {
        return transformer.apply(flattenSource());
    }

}
