package com.dlsc.gemsfx.util;

/**
 * A simple history manager that does not persist its items anywhere. The history will
 * always be clear after an application restart.
 *
 * @param <T> the type of objects to store in the history list
 */
public class InMemoryHistoryManager<T> extends HistoryManager<T> {

    @Override
    protected void loadHistory() {
    }

    @Override
    protected void storeHistory() {
    }
}
