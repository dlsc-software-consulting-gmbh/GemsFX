package com.dlsc.gemsfx.binding;

import javafx.collections.ObservableList;

import java.util.Objects;
import java.util.function.Function;

/**
 * A concrete implementation of {@link AbstractNestedListBinding} that computes its value using
 * a function that directly applies to the source list of lists. This class maintains the nested list
 * structure in its computation.
 *
 * @param <T> the type of the elements in the nested observable lists
 * @param <U> the type of the value computed by the binding
 */
public class TransformedNestedListBinding<T, U> extends AbstractNestedListBinding<T, U> {

    private final Function<ObservableList<ObservableList<T>>, U> transformer;

    /**
     * Constructs a NestedListBinding with a source and a transformer function.
     * The transformer function is applied directly to the nested list structure to compute the value.
     *
     * @param source      The observable list of observable lists that serves as the source for this binding.
     * @param transformer A function that transforms the source into a computed value of type U.
     */
    public TransformedNestedListBinding(ObservableList<ObservableList<T>> source, Function<ObservableList<ObservableList<T>>, U> transformer) {
        super(source);
        this.transformer = Objects.requireNonNull(transformer, "Transformer function cannot be null");

        initListeners();
    }

    /**
     * Computes the value of the binding by applying the transformer function to the source.
     * This method directly reflects changes in the nested list structure in the computed value.
     *
     * @return The computed value, as transformed from the nested list structure.
     */
    @Override
    protected U computeValue() {
        return transformer.apply(source);
    }

}
