package com.dlsc.gemsfx.util;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class ListUtils {

    /**
     * Update matching elements in the list with the provided new value.
     *
     * @param list                 the list to operate on
     * @param matchPredicate       the predicate to match elements
     * @param newValue             the new value to replace matching elements
     * @param breakAfterFirstMatch whether to break after the first match
     * @param <T>                  the type of elements in the list
     * @return the number of elements updated
     */
    public static <T> int updateMatching(List<T> list, Predicate<T> matchPredicate, T newValue, boolean breakAfterFirstMatch) {
        int updateCount = 0;
        ListIterator<T> iterator = list.listIterator();
        while (iterator.hasNext()) {
            T currentElement = iterator.next();
            if (matchPredicate.test(currentElement)) {
                iterator.set(newValue);
                updateCount++;
                if (breakAfterFirstMatch) {
                    break;
                }
            }
        }
        return updateCount;
    }

    /**
     * Update the first matching element in the list with the provided new value.
     *
     * @param list           the list to operate on
     * @param matchPredicate the predicate to match elements
     * @param newValue       the new value to replace matching elements
     * @param <T>            the type of elements in the list
     * @return the number of elements updated
     */
    public static <T> int updateFirstMatching(List<T> list, Predicate<T> matchPredicate, T newValue) {
        return updateMatching(list, matchPredicate, newValue, true);
    }

    /**
     * Update all matching elements in the list with the provided new value.
     *
     * @param list           the list to operate on
     * @param matchPredicate the predicate to match elements
     * @param newValue       the new value to replace matching elements
     * @param <T>            the type of elements in the list
     * @return the number of elements updated
     */
    public static <T> int updateAllMatching(List<T> list, Predicate<T> matchPredicate, T newValue) {
        return updateMatching(list, matchPredicate, newValue, false);
    }
}

