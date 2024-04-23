package com.dlsc.gemsfx.binding;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.function.Consumer;

/**
 * This class extends AbstractChangeTracker to specifically handle ObservableList of ObservableList.
 * It tracks changes to both the outer list and inner lists and notifies the consumer upon any modifications.
 *
 * @param <T> the type of the elements within the nested ObservableLists
 */
public class NestedListChangeTracker<T> extends AbstractChangeTracker<ObservableList<T>> {

    private final ListChangeListener<T> innerListListener = change -> {
        while (change.next()) {
            if (change.wasAdded() || change.wasRemoved()) {
                notifyChange();
                break;
            }
        }
    };
    private final WeakListChangeListener<T> weakInnerListListener = new WeakListChangeListener<>(innerListListener);

    /**
     * Constructs a NestedListChangeTracker with the specified source list.
     *
     * @param source the observable list of observable lists that is being monitored
     */
    public NestedListChangeTracker(ObservableList<ObservableList<T>> source) {
        this(source, null);
    }

    /**
     * Constructs a NestedListChangeTracker with the specified source list and change handler.
     *
     * @param source    the observable list of observable lists that is being monitored
     * @param onChanged the consumer to handle change notifications
     */
    public NestedListChangeTracker(ObservableList<ObservableList<T>> source, Consumer<ObservableList<ObservableList<T>>> onChanged) {
        super(source, onChanged);

        initOuterListener();
    }

    protected void safeAddListener(ObservableList<T> value) {
        if (value != null) {
            value.addListener(weakInnerListListener);
        }
    }

    protected void safeRemoveListener(ObservableList<T> value) {
        if (value != null) {
            value.removeListener(weakInnerListListener);
        }
    }

}
