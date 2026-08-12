package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link GridTablePropertyValueFactory}.
 */
public class GridTablePropertyValueFactoryTest extends FxTestBase {

    /** A simple test bean with a public field and a private field. */
    static class Person {
        public String name;
        private int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void testExtractsPublicField() {
        GridTablePropertyValueFactory<Person, String> factory =
                new GridTablePropertyValueFactory<>("name");
        String result = factory.call(new Person("Alice", 30));
        assertEquals("Alice", result);
    }

    @Test
    public void testExtractsPrivateField() {
        GridTablePropertyValueFactory<Person, Integer> factory =
                new GridTablePropertyValueFactory<>("age");
        Integer result = factory.call(new Person("Bob", 25));
        assertEquals(25, (int) result);
    }

    @Test
    public void testFieldIsCachedOnSubsequentCalls() {
        GridTablePropertyValueFactory<Person, String> factory =
                new GridTablePropertyValueFactory<>("name");
        factory.call(new Person("First", 1));
        String second = factory.call(new Person("Second", 2));
        assertEquals("Second", second);
    }

    @Test
    public void testUnknownFieldThrows() {
        GridTablePropertyValueFactory<Person, Object> factory =
                new GridTablePropertyValueFactory<>("nonExistent");
        try {
            factory.call(new Person("X", 1));
            fail("Expected RuntimeException for missing field");
        } catch (RuntimeException expected) {
            // ok
        }
    }

    @Test
    public void testNullPropertyNameThrows() {
        try {
            new GridTablePropertyValueFactory<>(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // ok
        }
    }
}
