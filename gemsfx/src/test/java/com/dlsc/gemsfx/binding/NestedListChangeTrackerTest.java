package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class NestedListChangeTrackerTest {

    private ObservableList<ObservableList<Integer>> source;
    private NestedListChangeTracker<Integer> tracker;
    private AtomicInteger changeCount;

    @Before
    public void setUp() {
        source = FXCollections.observableArrayList();
        changeCount = new AtomicInteger(0);
        tracker = new NestedListChangeTracker<>(source, s -> changeCount.incrementAndGet());
    }

    @After
    public void tearDown() {
        tracker.dispose();
    }

    @Test
    public void testInitialSetup() {
        // No changes should be notified initially.
        assertEquals(0, changeCount.get());
    }

    @Test
    public void testAddingOuterList() {
        ObservableList<Integer> innerList1 = FXCollections.observableArrayList();
        source.add(innerList1);
        // Adding an inner list should trigger a change.
        assertEquals(1, changeCount.get());

        ObservableList<Integer> innerList2 = FXCollections.observableArrayList();
        source.add(innerList2);
        // Adding another inner list should trigger another change.
        assertEquals(2, changeCount.get());

        // Adding a null list should trigger another change.
        source.add(null);
        assertEquals(3, changeCount.get());

        innerList1.add(1);
        // Adding an element to an inner list should trigger another change.
        assertEquals(4, changeCount.get());
    }

    @Test
    public void testModifyingInnerList() {
        ObservableList<Integer> innerList = FXCollections.observableArrayList();
        source.add(innerList);
        // Reset after initial add
        changeCount.set(0);

        // Modifying inner list should trigger a change.
        innerList.add(1);
        assertEquals(1, changeCount.get());

        // Setting an element should trigger another change.
        innerList.set(0, 3);
        assertEquals(2, changeCount.get());

        // Removing an element should trigger another change.
        innerList.remove(0);
        assertEquals(3, changeCount.get());

        // Setting a new inner list should trigger another change.
        source.set(0, FXCollections.observableArrayList());
        assertEquals(4, changeCount.get());
    }

    @Test
    public void testRemovingInnerList() {
        ObservableList<Integer> innerList = FXCollections.observableArrayList();
        source.add(innerList);
        // Reset after additions
        changeCount.set(0);

        // Removing an inner list should trigger a change.
        source.remove(innerList);
        assertEquals(1, changeCount.get());

        // source is now empty, so removing an inner list should not trigger a change.
        source.clear();
        assertEquals(1, changeCount.get());
    }

    @Test
    public void testSetOnChanged() {
        tracker.setOnChanged(s -> System.out.println("Changed"));
        ObservableList<Integer> innerList = FXCollections.observableArrayList();
        source.add(innerList);

        ObservableList<ObservableList<String>> sourceList = FXCollections.observableArrayList();
        NestedListChangeTracker<String> changeTracker = new NestedListChangeTracker<>(sourceList, s -> System.out.println("Source list Changed"));
        sourceList.add(FXCollections.observableArrayList("Hello", "World"));
        sourceList.add(FXCollections.observableArrayList("Foo", "Bar"));

        changeTracker.dispose();
    }
}
