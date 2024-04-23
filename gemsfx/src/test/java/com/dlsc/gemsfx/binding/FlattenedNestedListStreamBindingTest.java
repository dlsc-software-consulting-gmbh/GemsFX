package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FlattenedNestedListStreamBindingTest {

    private ObservableList<ObservableList<Integer>> source;
    private FlattenedNestedListStreamBinding<Integer> binding;

    @Before
    public void setUp() {
        source = FXCollections.observableArrayList();
        binding = new FlattenedNestedListStreamBinding<>(source);
    }

    @After
    public void tearDown() {
        binding.dispose();
    }

    @Test
    public void testInitialValue() {
        assertEquals(0, binding.get().count());
    }

    @Test
    public void testAddingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5);
        // Outers list added nested lists.
        source.addAll(list1, list2);

        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, binding.get().toArray());

        // Outers list added null.
        source.add(null);
        // Adding a null list should not affect the stream
        assertEquals(5, binding.get().count());

        // Outers list added nested lists.
        source.add(FXCollections.observableArrayList(6, 7));
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5, 6, 7}, binding.get().toArray());

        // Inner list added elements.
        list1.addAll(8, 9);
        assertArrayEquals(new Integer[]{1, 2, 3, 8, 9, 4, 5, 6, 7}, binding.get().toArray());
    }

    @Test
    public void testModifyingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        source.add(list1);

        // Inner list added elements.
        list1.addAll(4, 5);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, binding.get().toArray());

        // Inner list set elements.
        list1.setAll(6, 7);
        assertArrayEquals(new Integer[]{6, 7}, binding.get().toArray());

        // Outers list set nested list.
        ObservableList<Integer> list2 = FXCollections.observableArrayList(8, 9);
        source.set(0, list2);
        assertArrayEquals(new Integer[]{8, 9}, binding.get().toArray());
    }

    @Test
    public void testRemovingElementsFromNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5);

        source.addAll(list1, list2);
        // Inner list removed elements.
        list1.remove(1);
        list2.remove(0);
        assertArrayEquals(new Integer[]{1, 3, 5}, binding.get().toArray());

        // Outers list removed nested list.
        source.remove(list1);
        assertArrayEquals(new Integer[]{5}, binding.get().toArray());
    }

    @Test
    public void testClearingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5);
        source.addAll(list1, list2);

        list1.clear();
        assertEquals(2, binding.get().count());

        source.clear();
        assertEquals(0, binding.get().count());
    }
}
