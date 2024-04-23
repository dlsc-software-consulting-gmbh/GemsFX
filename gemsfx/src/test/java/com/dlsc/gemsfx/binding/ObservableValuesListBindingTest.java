package com.dlsc.gemsfx.binding;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ObservableValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.*;

public class ObservableValuesListBindingTest {

    private ObservableList<ObservableValue<Number>> source;
    private ObservableValuesListBinding<Number, Integer> sumBinding;

    @Before
    public void setUp() {
        source = FXCollections.observableArrayList();
        sumBinding = new ObservableValuesListBinding<>(
                source,
                values -> values.mapToInt(Number::intValue).sum()  // Sum all the values
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
    public void testAddingValues() {
        IntegerProperty value1 = new SimpleIntegerProperty(10);
        IntegerProperty value2 = new SimpleIntegerProperty(20);
        source.addAll(value1, value2);

        // 10 + 20 = 30
        assertEquals(30, (int) sumBinding.get());

        IntegerProperty value3 = new SimpleIntegerProperty(30);
        source.add(value3);
        // 10 + 20 + 30 = 60
        assertEquals(60, (int) sumBinding.get());
    }

    @Test
    public void testModifyingValues() {
        IntegerProperty ip1 = new SimpleIntegerProperty(10);
        source.add(ip1);

        // Modify the value of ip1
        ip1.set(20);
        assertEquals(20, (int) sumBinding.get());

        source.set(0, new SimpleIntegerProperty(30));
        assertEquals(30, (int) sumBinding.get());
    }

    @Test
    public void testRemovingValues() {
        IntegerProperty value1 = new SimpleIntegerProperty(10);
        IntegerProperty value2 = new SimpleIntegerProperty(20);

        source.addAll(value1, value2);
        assertEquals(30, (int) sumBinding.get());

        source.remove(value1);
        assertEquals(20, (int) sumBinding.get());

        source.clear();
        assertEquals(0, (int) sumBinding.get());
    }
}
