package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.*;

public class GeneralAggregatedListBindingTest {

    private ObservableList<Group> groups;
    private GeneralAggregatedListBinding<Group, Integer, Integer, Integer> maxSumBinding;

    @Before
    public void setUp() {
        groups = FXCollections.observableArrayList();
        maxSumBinding = new GeneralAggregatedListBinding<>(
                groups,
                Group::getValues,
                // Find the max in each list
                values -> values.stream().max(Integer::compare).orElse(0),
                // Sum of all max values
                maxValues -> maxValues.mapToInt(Integer::intValue).sum()
        );
    }

    @After
    public void tearDown() {
        maxSumBinding.dispose();
    }

    @Test
    public void testInitialValue() {
        // Initial value should be 0 when no groups are present.
        assertEquals(0, (int) maxSumBinding.get());
    }

    @Test
    public void testAddingGroups() {
        Group group1 = new Group(1, 2, 3);
        groups.add(group1);
        Group group2 = new Group(4, 5, 6);
        groups.add(group2);

        // group1 max = 3, group2 max = 6; 3 + 6 = 9
        assertEquals(9, (int) maxSumBinding.get());

        Group group3 = new Group(7, 8, 9);
        groups.add(group3);
        // group1 max = 3, group2 max = 6, group3 max = 9; 3 + 6 + 9 = 18
        assertEquals(18, (int) maxSumBinding.get());

        group2.getValues().addAll(7, 8);
        // group1 max = 3, group2 max = 8, group3 max = 9; 3 + 8 + 9 = 20
        assertEquals(20, (int) maxSumBinding.get());
    }

    @Test
    public void testModifyingGroupValues() {
        Group group1 = new Group(1, 2, 3);
        groups.add(group1);
        // Adding a new max value
        group1.getValues().addAll(7);

        // group1 max = 7;
        assertEquals(7, (int) maxSumBinding.get());

        group1.getValues().setAll(6, 7, 8);
        // group1 max = 8;
        assertEquals(8, (int) maxSumBinding.get());

        Group group2 = new Group(4, 5, 6);
        groups.set(0, group2);
        // group2 max = 6;
        assertEquals(6, (int) maxSumBinding.get());
    }

    @Test
    public void testRemovingGroups() {
        Group group1 = new Group(1, 2, 3);
        Group group2 = new Group(4, 5, 6);
        groups.addAll(group1, group2);

        groups.remove(group2);
        // group1 max = 3;
        assertEquals(3, (int) maxSumBinding.get());

        group1.getValues().remove((Integer) 3);
        // group1 max = 2;
        assertEquals(2, (int) maxSumBinding.get());

        groups.remove(group1);
        // No groups present
        assertEquals(0, (int) maxSumBinding.get());
    }


    private static class Group {
        private final ObservableList<Integer> values;

        public Group(Integer... numbers) {
            this.values = FXCollections.observableArrayList(numbers);
        }

        public ObservableList<Integer> getValues() {
            return values;
        }
    }
}
