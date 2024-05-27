package com.dlsc.gemsfx.util;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility methods for working with lists.
 */
public class ListUtils {

    /**
     * Replaces matching elements in the list with the provided new value.
     *
     * @param list                 the list to operate on
     * @param matchPredicate       the predicate to match elements
     * @param newValue             the new value to replace matching elements
     * @param <T>                  the type of elements in the list
     */
    public static <T> boolean replaceIf(List<T> list, Predicate<T> matchPredicate, T newValue) {
        Objects.requireNonNull(list, "list can not be null");
        Objects.requireNonNull(matchPredicate, "matchPredicate can not be null");
        Objects.requireNonNull(newValue, "newValue can not be null");

        boolean success = false;
        ListIterator<T> iterator = list.listIterator();
        while (iterator.hasNext()) {
            T currentElement = iterator.next();
            if (matchPredicate.test(currentElement)) {
                iterator.set(newValue);
                success = true;
            }
        }
        return success;
    }
}

