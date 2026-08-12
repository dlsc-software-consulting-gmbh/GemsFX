package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Tests for {@link FilterView}, including its inner {@link FilterView.FilterGroup}
 * and {@link FilterView.Filter} classes.
 */
public class FilterViewTest extends FxTestBase {

    // ---- simple concrete Filter subclass for tests ----

    private static class StartsWithFilter extends FilterView.Filter<String> {
        private final String prefix;

        StartsWithFilter(String name, String prefix) {
            super(name);
            this.prefix = prefix;
        }

        @Override
        public boolean test(String s) {
            return s != null && s.startsWith(prefix);
        }
    }

    // ---- construction ----

    @Test
    public void testDefaultConstruction() {
        FilterView<String> view = invoke(FilterView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        FilterView<String> view = invoke(FilterView::new);
        assertTrue(view.getStyleClass().contains("filter-view"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        FilterView<String> view = invoke(FilterView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testSkinCreation() {
        FilterView<String> view = layout(invoke(FilterView::new));
        assertNotNull(view.getSkin());
    }

    // ---- items / filteredItems ----

    @Test
    public void testDefaultItemsEmpty() {
        FilterView<String> view = invoke(FilterView::new);
        assertTrue(view.getItems().isEmpty());
    }

    @Test
    public void testItemsCanBeAdded() {
        FilterView<String> view = invoke(FilterView::new);
        runFx(() -> view.getItems().addAll("Alpha", "Beta", "Gamma"));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testFilteredItemsShowsAllByDefault() {
        FilterView<String> view = invoke(FilterView::new);
        runFx(() -> view.getItems().addAll("Alice", "Bob", "Carol"));
        waitForFxEvents();
        assertEquals(3, view.getFilteredItems().size());
    }

    // ---- text filter ----

    @Test
    public void testTextFilterReducesFilteredItems() {
        FilterView<String> view = invoke(FilterView::new);
        runFx(() -> {
            view.getItems().addAll("Alice", "Bob", "Carol");
            view.setTextFilterProvider(text -> s -> s.toLowerCase().startsWith(text));
            view.setFilterText("a");
        });
        waitForFxEvents();
        assertEquals(1, view.getFilteredItems().size());
        assertEquals("Alice", view.getFilteredItems().get(0));
    }

    // ---- filter groups ----

    @Test
    public void testFilterGroupNameRoundTrip() {
        FilterView.FilterGroup<String> group = new FilterView.FilterGroup<>("MyGroup");
        assertEquals("MyGroup", group.getName());
        group.setName("NewName");
        assertEquals("NewName", group.getName());
    }

    @Test
    public void testFilterGroupFiltersCanBeAdded() {
        FilterView.FilterGroup<String> group = new FilterView.FilterGroup<>("G");
        group.getFilters().add(new StartsWithFilter("A-filter", "A"));
        assertEquals(1, group.getFilters().size());
    }

    // ---- active filters ----

    @Test
    public void testActiveFiltersReduceFilteredItems() {
        FilterView<String> view = invoke(FilterView::new);
        runFx(() -> {
            view.getItems().addAll("Alice", "Bob", "Carol");

            FilterView.FilterGroup<String> group = new FilterView.FilterGroup<>("Letters");
            StartsWithFilter f = new StartsWithFilter("B-names", "B");
            group.getFilters().add(f);
            view.getFilterGroups().add(group);
            view.getFilters().add(f);
        });
        waitForFxEvents();
        assertEquals(1, view.getFilteredItems().size());
        assertEquals("Bob", view.getFilteredItems().get(0));
    }

    // ---- additional predicate ----

    @Test
    public void testAdditionalFilterPredicateApplied() {
        FilterView<String> view = invoke(FilterView::new);
        runFx(() -> {
            view.getItems().addAll("Alice", "Bob", "Carol");
            view.setAdditionalFilterPredicate(s -> s.length() > 3);
        });
        waitForFxEvents();
        // "Bob" has 3 chars, not > 3
        assertEquals(2, view.getFilteredItems().size());
    }

    // ---- Filter inner class ----

    @Test
    public void testFilterNameRoundTrip() {
        StartsWithFilter f = new StartsWithFilter("MyFilter", "x");
        assertEquals("MyFilter", f.getName());
    }

    @Test
    public void testFilterDefaultNotSelected() {
        StartsWithFilter f = new StartsWithFilter("F", "f");
        assertFalse(f.isSelected());
    }

    @Test
    public void testFilterConstructorWithSelected() {
        // Test via the name+boolean constructor variant
        FilterView.Filter<String> selected = new FilterView.Filter<String>("label", true) {
            @Override
            public boolean test(String s) {
                return true;
            }
        };
        assertTrue(selected.isSelected());
    }
}
