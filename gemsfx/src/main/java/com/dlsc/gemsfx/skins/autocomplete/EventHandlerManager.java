/*
 * Copyright (c) 2010, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.dlsc.gemsfx.skins.autocomplete;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@code EventDispatcher} which allows user event handler / filter
 * registration and when used in an event dispatch chain it forwards received
 * events to the appropriate registered handlers / filters.
 */
public class EventHandlerManager extends BasicEventDispatcher {

    private final Map<EventType<? extends Event>, CompositeEventHandler<? extends Event>> eventHandlerMap;

    private final Object eventSource;

    public EventHandlerManager(Object eventSource) {
        this.eventSource = eventSource;
        eventHandlerMap = new HashMap<>();
    }

    /**
     * Registers an event handler in {@code EventHandlerManager}.
     *
     * @param <T>          the specific event class of the handler
     * @param eventType    the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public final <T extends Event> void addEventHandler(
            EventType<T> eventType,
            EventHandler<? super T> eventHandler) {
        validateEventType(eventType);
        validateEventHandler(eventHandler);

        CompositeEventHandler<T> compositeEventHandler =
                createGetCompositeEventHandler(eventType);

        compositeEventHandler.addEventHandler(eventHandler);
    }

    /**
     * Unregisters a previously registered event handler.
     *
     * @param <T>          the specific event class of the handler
     * @param eventType    the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public final <T extends Event> void removeEventHandler(
            EventType<T> eventType,
            EventHandler<? super T> eventHandler) {
        validateEventType(eventType);
        validateEventHandler(eventHandler);

        CompositeEventHandler<T> compositeEventHandler =
                (CompositeEventHandler<T>) eventHandlerMap.get(eventType);

        if (compositeEventHandler != null) {
            compositeEventHandler.removeEventHandler(eventHandler);
        }
    }

    /**
     * Registers an event filter in {@code EventHandlerManager}.
     *
     * @param <T>         the specific event class of the filter
     * @param eventType   the type of the events to receive by the filter
     * @param eventFilter the filter to register
     * @throws NullPointerException if the event type or filter is null
     */
    public final <T extends Event> void addEventFilter(
            EventType<T> eventType,
            EventHandler<? super T> eventFilter) {
        validateEventType(eventType);
        validateEventFilter(eventFilter);

        CompositeEventHandler<T> compositeEventHandler =
                createGetCompositeEventHandler(eventType);

        compositeEventHandler.addEventFilter(eventFilter);
    }

    /**
     * Unregisters a previously registered event filter.
     *
     * @param <T>         the specific event class of the filter
     * @param eventType   the event type from which to unregister
     * @param eventFilter the filter to unregister
     * @throws NullPointerException if the event type or filter is null
     */
    public final <T extends Event> void removeEventFilter(
            EventType<T> eventType,
            EventHandler<? super T> eventFilter) {
        validateEventType(eventType);
        validateEventFilter(eventFilter);

        CompositeEventHandler<T> compositeEventHandler =
                (CompositeEventHandler<T>) eventHandlerMap.get(eventType);

        if (compositeEventHandler != null) {
            compositeEventHandler.removeEventFilter(eventFilter);
        }
    }

    /**
     * Sets the specified singleton handler. There can only be one such handler
     * specified at a time.
     *
     * @param <T>          the specific event class of the handler
     * @param eventType    the event type to associate with the given eventHandler
     * @param eventHandler the handler to register, or null to unregister
     * @throws NullPointerException if the event type is null
     */
    public final <T extends Event> void setEventHandler(
            EventType<T> eventType,
            EventHandler<? super T> eventHandler) {
        validateEventType(eventType);

        CompositeEventHandler<T> compositeEventHandler =
                (CompositeEventHandler<T>) eventHandlerMap.get(eventType);

        if (compositeEventHandler == null) {
            if (eventHandler == null) {
                return;
            }
            compositeEventHandler = new CompositeEventHandler<T>();
            eventHandlerMap.put(eventType, compositeEventHandler);
        }

        compositeEventHandler.setEventHandler(eventHandler);
    }

    public final <T extends Event> EventHandler<? super T> getEventHandler(
            EventType<T> eventType) {
        CompositeEventHandler<T> compositeEventHandler =
                (CompositeEventHandler<T>) eventHandlerMap.get(eventType);

        return (compositeEventHandler != null)
                ? compositeEventHandler.getEventHandler()
                : null;
    }

    @Override
    public final Event dispatchCapturingEvent(Event event) {
        EventType<? extends Event> eventType = event.getEventType();
        do {
            event = dispatchCapturingEvent(eventType, event);
            eventType = eventType.getSuperType();
        } while (eventType != null);

        return event;
    }

    @Override
    public final Event dispatchBubblingEvent(Event event) {
        EventType<? extends Event> eventType = event.getEventType();
        do {
            event = dispatchBubblingEvent(eventType, event);
            eventType = eventType.getSuperType();
        } while (eventType != null);

        return event;
    }

    private <T extends Event> CompositeEventHandler<T>
    createGetCompositeEventHandler(EventType<T> eventType) {
        CompositeEventHandler<T> compositeEventHandler =
                (CompositeEventHandler<T>) eventHandlerMap.get(eventType);
        if (compositeEventHandler == null) {
            compositeEventHandler = new CompositeEventHandler<T>();
            eventHandlerMap.put(eventType, compositeEventHandler);
        }

        return compositeEventHandler;
    }

    protected Object getEventSource() {
        return eventSource;
    }

    private Event dispatchCapturingEvent(
            EventType<? extends Event> handlerType, Event event) {
        CompositeEventHandler<? extends Event> compositeEventHandler =
                eventHandlerMap.get(handlerType);

        if (compositeEventHandler != null && compositeEventHandler.hasFilter()) {
            event = fixEventSource(event, eventSource);
            compositeEventHandler.dispatchCapturingEvent(event);
        }

        return event;
    }

    private Event dispatchBubblingEvent(
            EventType<? extends Event> handlerType, Event event) {
        CompositeEventHandler<? extends Event> compositeEventHandler =
                eventHandlerMap.get(handlerType);

        if (compositeEventHandler != null && compositeEventHandler.hasHandler()) {
            event = fixEventSource(event, eventSource);
            compositeEventHandler.dispatchBubblingEvent(event);
        }

        return event;
    }

    private static Event fixEventSource(Event event,
                                        Object eventSource) {
        return (event.getSource() != eventSource)
                ? event.copyFor(eventSource, event.getTarget())
                : event;
    }

    private static void validateEventType(EventType<?> eventType) {
        if (eventType == null) {
            throw new NullPointerException("Event type must not be null");
        }
    }

    private static void validateEventHandler(
            EventHandler<?> eventHandler) {
        if (eventHandler == null) {
            throw new NullPointerException("Event handler must not be null");
        }
    }

    private static void validateEventFilter(
            EventHandler<?> eventFilter) {
        if (eventFilter == null) {
            throw new NullPointerException("Event filter must not be null");
        }
    }
}
