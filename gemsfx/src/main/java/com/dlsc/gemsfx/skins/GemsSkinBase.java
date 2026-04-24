/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dlsc.gemsfx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for GemsFX skins that automatically tracks listener registrations
 * and removes them all in {@link #dispose()}, preventing memory leaks caused by
 * skins holding strong references on the skinnable control's observable properties.
 *
 * <p>Usage: instead of manually calling {@code obs.addListener(l)} in the constructor
 * and {@code obs.removeListener(l)} in {@code dispose()}, call {@code register(obs, l)}.
 * The listener will be added immediately and removed automatically on disposal.</p>
 */
public abstract class GemsSkinBase<C extends Control> extends SkinBase<C> {

    private final List<Runnable> disposers = new ArrayList<>();

    protected GemsSkinBase(C control) {
        super(control);
    }

    /**
     * Adds an {@link InvalidationListener} to the given {@link Observable} and tracks
     * it for automatic removal when this skin is disposed.
     *
     * @return the listener, for use as a field initializer
     */
    protected InvalidationListener register(Observable obs, InvalidationListener listener) {
        obs.addListener(listener);
        disposers.add(() -> obs.removeListener(listener));
        return listener;
    }

    /**
     * Adds a {@link ChangeListener} to the given {@link ObservableValue} and tracks
     * it for automatic removal when this skin is disposed.
     *
     * @return the listener, for use as a field initializer
     */
    protected <T> ChangeListener<T> register(ObservableValue<T> obs, ChangeListener<T> listener) {
        obs.addListener(listener);
        disposers.add(() -> obs.removeListener(listener));
        return listener;
    }

    /**
     * Adds a {@link ListChangeListener} to the given {@link ObservableList} and tracks
     * it for automatic removal when this skin is disposed.
     *
     * @return the listener, for use as a field initializer
     */
    protected <T> ListChangeListener<T> register(ObservableList<T> list, ListChangeListener<T> listener) {
        list.addListener(listener);
        disposers.add(() -> list.removeListener(listener));
        return listener;
    }

    /**
     * Adds a {@link MapChangeListener} to the given {@link ObservableMap} and tracks
     * it for automatic removal when this skin is disposed.
     *
     * @return the listener, for use as a field initializer
     */
    protected <K, V> MapChangeListener<K, V> register(ObservableMap<K, V> map, MapChangeListener<K, V> listener) {
        map.addListener(listener);
        disposers.add(() -> map.removeListener(listener));
        return listener;
    }

    /**
     * Adds an event handler to the given {@link Node} and tracks it for
     * automatic removal when this skin is disposed.
     *
     * @return the handler, for use as a field initializer
     */
    protected <E extends Event> EventHandler<E> registerHandler(Node target, EventType<E> type, EventHandler<E> handler) {
        target.addEventHandler(type, handler);
        disposers.add(() -> target.removeEventHandler(type, handler));
        return handler;
    }

    /**
     * Adds an event filter to the given {@link Node} and tracks it for
     * automatic removal when this skin is disposed.
     *
     * @return the filter, for use as a field initializer
     */
    protected <E extends Event> EventHandler<E> registerFilter(Node target, EventType<E> type, EventHandler<E> filter) {
        target.addEventFilter(type, filter);
        disposers.add(() -> target.removeEventFilter(type, filter));
        return filter;
    }

    /**
     * Removes all registered listeners, event handlers, and event filters,
     * then delegates to {@link SkinBase#dispose()}.
     */
    @Override
    public void dispose() {
        disposers.forEach(Runnable::run);
        disposers.clear();
        super.dispose();
    }
}
