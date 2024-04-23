package com.dlsc.gemsfx.binding;

import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This abstract class provides a framework for tracking changes in an ObservableList of Observable elements.
 * It encapsulates common logic for adding and removing change listeners to the observable elements
 * within the list and provides a mechanism to notify changes through a specified Consumer.
 *
 * @param <T> the type of Observable elements in the list that is being tracked
 */
public abstract class AbstractChangeTracker<T extends Observable> {

    private final ObservableList<T> source;
    private final ListChangeListener<T> outerListChangeListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().forEach(this::safeRemoveListener);
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::safeAddListener);
            }
            notifyChange();
        }
    };
    private final WeakListChangeListener<T> weakOuterListChangeListener = new WeakListChangeListener<>(outerListChangeListener);
    protected Consumer<ObservableList<T>> onChanged;

    /**
     * Constructs an AbstractChangeTracker with the provided source list.
     *
     * @param source the observable list of elements that is being monitored
     */
    public AbstractChangeTracker(ObservableList<T> source) {
        this(source, null);
    }

    /**
     * Constructs an AbstractChangeTracker with the provided source list and change handler.
     *
     * @param source    the observable list of elements that is being monitored
     * @param onChanged the consumer to handle change notifications
     */
    public AbstractChangeTracker(ObservableList<T> source, Consumer<ObservableList<T>> onChanged) {
        this.source = Objects.requireNonNull(source, "Source list cannot be null");
        // Consumer is allowed to be null
        this.onChanged = onChanged;
    }

    /**
     * Initializes the listener for the outer list.
     * This method should be called after subclass constructors have fully initialized any necessary fields to ensure
     * that listeners interact with a fully initialized object.
     */
    protected void initOuterListener() {
        this.source.forEach(this::safeAddListener);
        this.source.addListener(weakOuterListChangeListener);
    }

    /**
     * Sets the consumer that will be notified when changes occur.
     *
     * @param onChanged the consumer to be notified of changes
     */
    public void setOnChanged(Consumer<ObservableList<T>> onChanged) {
        this.onChanged = onChanged;
    }

    protected void notifyChange() {
        if (onChanged != null) {
            onChanged.accept(source);
        }
    }

    protected abstract void safeAddListener(T value);

    protected abstract void safeRemoveListener(T value);

    /**
     * Disposes this tracker by removing all listeners from the source and nested lists.
     * It is recommended to call this method when the tracker is no longer needed.
     * Doing so helps ensure that resources are properly released and helps prevent potential memory leaks.
     * Calling this method is particularly important in environments with limited resources or
     * in applications that create and dispose many trackers over their lifetime.
     */
    public void dispose() {
        source.forEach(this::safeRemoveListener);
        source.removeListener(weakOuterListChangeListener);
    }
}
