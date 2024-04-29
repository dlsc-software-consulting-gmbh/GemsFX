package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TransformedNestedListBindingTest {

    private ObservableList<ObservableList<Integer>> source;
    private TransformedNestedListBinding<Integer, Integer> sumBinding;

    @Before
    public void setUp() {
        source = FXCollections.observableArrayList();
        sumBinding = new TransformedNestedListBinding<>(
                source,
                lists -> lists.stream().filter(Objects::nonNull).flatMapToInt(list -> list.stream().mapToInt(Integer::intValue)).sum());
    }

    @After
    public void tearDown() {
        sumBinding.dispose();
    }

    @Test
    public void testInitialValue() {
        assertEquals(0, sumBinding.get().intValue());
    }

    @Test
    public void testAddingLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        source.add(list1);
        // Outers list added nested lists. 1 + 2 + 3 = 6
        assertEquals(6, sumBinding.get().intValue());

        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5);
        source.add(list2);
        // Outers list added nested lists. 1 + 2 + 3 + 4 + 5 = 15
        assertEquals(15, sumBinding.get().intValue());

        // Outers list added null.
        source.add(null);
        // Adding a null list should not affect the sum
        assertEquals(15, sumBinding.get().intValue());

        // Inner list2 added elements. 1 + 2 + 3 + 4 + 5 + 6 + 7 = 28
        list2.addAll(6, 7);
        assertEquals(28, sumBinding.get().intValue());
    }

    @Test
    public void testRemovingLists() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5);
        source.addAll(list1, list2);
        assertEquals(15, sumBinding.get().intValue());

        // Outers list removed nested lists. 15 - (1 + 2 + 3) = 9
        source.remove(list1);
        assertEquals(9, sumBinding.get().intValue());

        // Inner list2 removed elements. 4 + 5 - 4 = 5
        list2.remove(0);
        assertEquals(5, sumBinding.get().intValue());
    }

    @Test
    public void testModifyingListContents() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(3, 4);
        source.addAll(list1, list2);
        assertEquals(10, sumBinding.get().intValue());

        // Modifying inner list1.
        list1.set(0, 3);
        // (3 + 2) + (3 + 4) = 12
        assertEquals(12, sumBinding.get().intValue());

        // Modifying outer list
        source.set(0, FXCollections.observableArrayList(0, 1));
        // (0 + 1) + (3 + 4) = 8
        assertEquals(8, sumBinding.get().intValue());

    }

    @Test
    public void testReactingToElementChanges() {
        ObservableList<Integer> list1 = FXCollections.observableArrayList();
        list1.add(10);

        ObservableList<Integer> list2 = FXCollections.observableArrayList();
        list2.add(2);

        // Outers list added nested lists.
        source.add(list1);
        assertEquals(10, sumBinding.get().intValue());

        // Outers list added nested lists.
        source.add(list2);
        // 10 + 2 = 12
        assertEquals(12, sumBinding.get().intValue());

        // Inner list1 added elements.
        list1.set(0, 1);
        // 1 + 2 = 3
        assertEquals(3, sumBinding.get().intValue());

        // Inner list2 added elements.
        list2.set(0, 3);
        // 1 + 3 = 4
        assertEquals(4, sumBinding.get().intValue());
    }

    @Test
    public void testNestedListCharacterReduction() {
        ObservableList<ObservableList<Character>> source = FXCollections.observableArrayList();

        TransformedNestedListBinding<Character, String> connectBinding = new TransformedNestedListBinding<>(
                source,
                lists -> lists.stream().flatMap(list -> list.stream().map(Object::toString)).reduce((s1, s2) -> s1 + s2).orElse(""));

        ObservableList<Character> list1 = FXCollections.observableArrayList('a', 'b', 'c');
        ObservableList<Character> list2 = FXCollections.observableArrayList('c', 'd');
        source.addAll(list1, list2);
        assertEquals("abccd", connectBinding.get());
        connectBinding.dispose();


        TransformedNestedListBinding<Character, HashMap<Character, Integer>> charCountBinding = new TransformedNestedListBinding<>(
                source,
                lists -> lists.stream().flatMap(Collection::stream).collect(HashMap::new, (map, c) -> map.merge(c, 1, Integer::sum), (m1, m2) -> {
                    m2.forEach((k, v) -> m1.merge(k, v, Integer::sum));
                }));
        assertEquals(1, charCountBinding.get().get('a').intValue());
        assertEquals(1, charCountBinding.get().get('b').intValue());
        assertEquals(2, charCountBinding.get().get('c').intValue());
        assertEquals(1, charCountBinding.get().get('d').intValue());

        source.add(FXCollections.observableArrayList('a', 'a'));
        assertEquals(3, charCountBinding.get().get('a').intValue());

        list2.remove((Character) 'd');
        assertNull(charCountBinding.get().get('d'));
        charCountBinding.dispose();
    }


    @Test
    public void testNestedListMaxSumBinding() {
        ObservableList<ObservableList<Integer>> source = FXCollections.observableArrayList();

        // Find the max in each list and sum all max values
        TransformedNestedListBinding<Integer, Integer> maxSumBinding = new TransformedNestedListBinding<>(
                source,
                lists -> lists.stream().filter(Objects::nonNull).mapToInt(list -> list.stream().mapToInt(Integer::intValue).max().orElse(0)).sum());

        ObservableList<Integer> list1 = FXCollections.observableArrayList(1, 2, 3);
        ObservableList<Integer> list2 = FXCollections.observableArrayList(4, 5, 6);

        source.add(list1);
        assertEquals(3, maxSumBinding.get().intValue());

        source.add(list2);
        // list1 max = 3, list2 max = 6; 3 + 6 = 9
        assertEquals(9, maxSumBinding.get().intValue());

        list2.addAll(7, 8);
        // list1 max = 3, list2 max = 8; 3 + 8 = 11
        assertEquals(11, maxSumBinding.get().intValue());

        list1.remove((Integer) 3);
        // list1 max = 2, list2 max = 8; 2 + 8 = 10
        assertEquals(10, maxSumBinding.get().intValue());

        list2.set(0, 9);
        // list1 max = 2, list2 max = 9; 2 + 9 = 11
        assertEquals(11, maxSumBinding.get().intValue());
    }

}
