package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransformedFlattenedNestedListStreamBindingTest {

    private ObservableList<ObservableList<Integer>> source;
    private TransformedFlattenedNestedListStreamBinding<Integer, Integer> sumBinding;

    @Before
    public void setUp() {
        source = FXCollections.observableArrayList();
        sumBinding = new TransformedFlattenedNestedListStreamBinding<>(
                source,
                numbers -> numbers.mapToInt(Integer::intValue).sum()  // Sum all the integers
        );
    }

    @After
    public void tearDown() {
        sumBinding.dispose();
    }

    @Test
    public void testInitialValue() {
        // Initial value should be 0 as no elements are present.
        assertEquals(0, (int) sumBinding.get());
    }

    @Test
    public void testAddingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5, 6);
        source.add(list1);
        // Outers list added nested lists. 1 + 2 + 3 = 6
        assertEquals(6, (int) sumBinding.get());

        // Outers list added nested lists. (1 + 2 + 3) + (4 + 5 + 6) = 21
        source.add(list2);
        assertEquals(21, (int) sumBinding.get());

        // Outers list added null.
        source.add(null);
        // Adding a null list should not affect the sum
        assertEquals(21, (int) sumBinding.get());

        // Inner list2 added elements.
        list2.add(7);
        // (1 + 2 + 3) + (4 + 5 + 6 + 7) = 28
        assertEquals(28, (int) sumBinding.get());
    }

    @Test
    public void testModifyingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        source.add(list1);

        // Inner list1 added elements.
        list1.add(4);
        // 1 + 2 + 3 + 4 = 10
        assertEquals(10, (int) sumBinding.get());

        // Inner list1 added elements.
        list1.remove((Integer) 1);
        // 2 + 3 + 4 = 9
        assertEquals(9, (int) sumBinding.get());

        // Inner list1 set all elements.
        list1.setAll(5, 6);
        // 5 + 6 = 11
        assertEquals(11, (int) sumBinding.get());

        ObservableList<Integer> list2 = FXCollections.observableArrayList(1, 2);
        source.set(0, list2);
        // 1 + 2 = 3
        assertEquals(3, (int) sumBinding.get());
    }

    @Test
    public void testRemovingNestedLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5, 6);
        source.addAll(list1, list2);

        source.remove(list2);
        assertEquals(6, (int) sumBinding.get());

        list1.clear();
        assertEquals(0, (int) sumBinding.get());

        list1.setAll(1, 2);
        source.clear();
        assertEquals(0, (int) sumBinding.get());
    }

}
