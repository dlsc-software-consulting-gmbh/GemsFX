package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.FilterViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A view for presenting a set of predefined filter groups, each one with a list of filters.
 * The user can select one or more filters from each group. Elements found in the resulting
 * filtered list have to match ALL filters. The selected filters will be shown as "chips"
 * (see @link {@link ChipView}).
 * <p>
 * Items can be added via the {@link #getItems()} list. Table or list views have to use
 * the {@link #getFilteredItems()} list. This filtered list can also be wrapped via a
 * {@link SortedList} and then added to a table or list view.
 * </p>
 * <p>
 * An input field for filtering based on text input will appear as soon as a text filter provider
 * has been defined. See {@link #setTextFilterProvider(Callback)}.
 * </p>
 * <p>
 * Applications with additional filtering needs can utilize the {@link #additionalFilterPredicateProperty()}.
 * </p>
 *
 * @param <T> the type of the model objects filtered by the view
 */
public class FilterView<T> extends Control {

    public FilterView() {
        getStyleClass().add("filter-view");

        final InvalidationListener updatePredicateListener = (Observable it) -> {

            // first group the filters together according to the group to which they belong

            Map<FilterGroup<T>, List<Filter<T>>> map = new HashMap<>();
            for (Filter<T> filter : getFilters()) {
                for (FilterGroup<T> group : getFilterGroups()) {
                    if (group.getFilters().contains(filter)) {
                        map.computeIfAbsent(group, key -> new ArrayList<>()).add(filter);
                    }
                }
            }

            // now create the predicate. filters of same group use OR, else ANDs

            Predicate<T> predicate = item -> true; // "and" chain starts with a true

            for (FilterGroup<T> group : map.keySet()) {
                Predicate<T> groupPredicate = item -> false; // or chain starts with a false
                for (Filter<T> filter : map.get(group)) {
                    groupPredicate = groupPredicate.or(filter);
                }

                predicate = predicate.and(groupPredicate);
            }

            final Callback<String, Predicate<T>> textFilterProvider = getTextFilterProvider();
            if (textFilterProvider != null && StringUtils.isNotBlank(getFilterText())) {
                predicate = predicate.and(textFilterProvider.call(getFilterText().toLowerCase()));
            }

            filterPredicate.set(predicate.and(getAdditionalFilterPredicate()));
        };

        filters.addListener(updatePredicateListener);
        textFilterProviderProperty().addListener(updatePredicateListener);
        filterTextProperty().addListener(updatePredicateListener);
        additionalFilterPredicateProperty().addListener(updatePredicateListener);

        items.addListener((Observable it) -> {
            FilteredList<T> filteredList = new FilteredList<>(getItems());
            filteredList.predicateProperty().bind(filterPredicateProperty());
            filteredItems.set(filteredList);
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return FilterView.class.getResource("filter-view.css").toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterViewSkin<>(this);
    }

    private final BooleanProperty showHeader = new SimpleBooleanProperty(this, "showHeader", true);

    public final boolean isShowHeader() {
        return showHeader.get();
    }

    /**
     * A flag to control whether the title, subtitle, and the search field
     * will be shown or not.
     *
     * @return true if the header will be shown (default is "true")
     */
    public final BooleanProperty showHeaderProperty() {
        return showHeader;
    }

    public final void setShowHeader(boolean showHeader) {
        this.showHeader.set(showHeader);
    }

    private final ObjectProperty<Node> extras = new SimpleObjectProperty<>(this, "extras");

    public final Node getExtras() {
        return extras.get();
    }

    /**
     * An extra node that can be added to the header of the filter view. The node will appear
     * to the right of the (optional) text filter field.
     *
     * @return
     */
    public final ObjectProperty<Node> extrasProperty() {
        return extras;
    }

    public final void setExtras(Node extras) {
        this.extras.set(extras);
    }

    private final StringProperty title = new SimpleStringProperty(this, "title", "Untitled");

    public final String getTitle() {
        return title.get();
    }

    /**
     * The title for the filter view
     *
     * @return the title text
     */
    public final StringProperty titleProperty() {
        return title;
    }

    public final void setTitle(String title) {
        this.title.set(title);
    }

    private final StringProperty titlePostfix = new SimpleStringProperty(this, "titlePostfix", "");

    public final String getTitlePostfix() {
        return titlePostfix.get();
    }

    /**
     * A text that can be added to the title text. Via the {@link #titlePostfixStyleProperty()} it
     * can be styled differently than the title.
     *
     * @return the title postfix text
     */
    public final StringProperty titlePostfixProperty() {
        return titlePostfix;
    }

    public final void setTitlePostfix(String titlePostfix) {
        this.titlePostfix.set(titlePostfix);
    }

    private final StringProperty titlePostfixStyle = new SimpleStringProperty(this, "titlePostfix", "");

    public final String getTitlePostfixStyle() {
        return titlePostfixStyle.get();
    }

    /**
     * A CSS styleclass that will be applied to the title postfix label.
     *
     * @return the title postfix style
     */
    public final StringProperty titlePostfixStyleProperty() {
        return titlePostfixStyle;
    }

    public final void setTitlePostfixStyle(String titlePostfixStyle) {
        this.titlePostfixStyle.set(titlePostfixStyle);
    }

    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle", "");

    public final String getSubtitle() {
        return subtitle.get();
    }

    /**
     * The subtitle text for the filter view.
     *
     * @return the subtitle text
     */
    public final StringProperty subtitleProperty() {
        return subtitle;
    }

    public final void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public final ObservableList getItems() {
        return items.get();
    }

    /**
     * The list of items that will be managed by the view.
     *
     * @return the model
     */
    public final ListProperty<T> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList items) {
        this.items.set(items);
    }

    private final ReadOnlyListWrapper<T> filteredItems = new ReadOnlyListWrapper<>();

    public final ReadOnlyListProperty<T> filteredItemsProperty() {
        return filteredItems.getReadOnlyProperty();
    }

    public final ObservableList getFilteredItems() {
        return filteredItems.getReadOnlyProperty();
    }

    // text filter

    private final StringProperty filterText = new SimpleStringProperty(this, "filterText");

    public final String getFilterText() {
        return filterText.get();
    }

    /**
     * Stores the filter text entered by the user inside the filter text field.
     *
     * @see #setTextFilterProvider(Callback)
     * @return the filter text
     */
    public final StringProperty filterTextProperty() {
        return filterText;
    }

    public final void setFilterText(String filterText) {
        this.filterText.set(filterText);
    }

    // text filter predicate provider

    private final ObjectProperty<Callback<String, Predicate<T>>> textFilterProvider = new SimpleObjectProperty<>(this, "textFilterProvider");

    public final Callback<String, Predicate<T>> getTextFilterProvider() {
        return textFilterProvider.get();
    }

    /**
     * Returns a filter predicate for a given text. This predicate will be added to the list of
     * internally managed predicates. The input field for text will only appear if this predicate
     * has been specified.
     *
     * @return the text filter predicate provider
     */
    public final ObjectProperty<Callback<String, Predicate<T>>> textFilterProviderProperty() {
        return textFilterProvider;
    }

    public final void setTextFilterProvider(Callback<String, Predicate<T>> textFilterProvider) {
        this.textFilterProvider.set(textFilterProvider);
    }

    // filter groups

    private final ListProperty<FilterGroup<T>> filterGroups = new SimpleListProperty<>(FXCollections.observableArrayList());

    public final ObservableList<FilterGroup<T>> getFilterGroups() {
        return filterGroups.get();
    }

    public final ListProperty<FilterGroup<T>> filterGroupsProperty() {
        return filterGroups;
    }

    public final void setFilterGroups(ObservableList<FilterGroup<T>> filterGroups) {
        this.filterGroups.set(filterGroups);
    }

// filters

    private final ObservableList<Filter> filters = FXCollections.observableArrayList();

    public final ObservableList<Filter> getFilters() {
        return filters;
    }

    // additional filter predicate

    private final ObjectProperty<Predicate<T>> additionalFilterPredicate = new SimpleObjectProperty<>(this, "additionalFilterPredicate", item -> true);

    public final Predicate<T> getAdditionalFilterPredicate() {
        return additionalFilterPredicate.get();
    }

    public final ObjectProperty<Predicate<T>> additionalFilterPredicateProperty() {
        return additionalFilterPredicate;
    }

    public final void setAdditionalFilterPredicate(Predicate<T> additionalFilterPredicate) {
        this.additionalFilterPredicate.set(additionalFilterPredicate);
    }

    // filter predicate

    private final ReadOnlyObjectWrapper<Predicate<T>> filterPredicate = new ReadOnlyObjectWrapper<>(this, "filterPredicate", item -> true);

    public final Predicate getFilterPredicate() {
        return filterPredicate.get();
    }

    public final ReadOnlyObjectProperty<Predicate<T>> filterPredicateProperty() {
        return filterPredicate.getReadOnlyProperty();
    }


    /**
     * A filter group consists of a group of filters and has a name. The name
     * is displayed in the UI as part of the dropdown list that expostes the
     * available filters.
     *
     * @param <T> the type of the model objects managed by the filter view
     */
    public static class FilterGroup<T> {

        /**
         * Constructs a new group.
         *
         * @param name the name that will be shown in the UI, e.g. "Gender"
         * @param id the id of the group
         */
        public FilterGroup(String name, String id) {
            setName(name);

            filters.addListener((Change<? extends Filter<T>> change) -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(filter -> filter.setGroup(this));
                    }
                }
            });
        }

        public FilterGroup(String name) {
            this(name, name.toLowerCase().replace("_", "-").replace("&", "and").replace(" ", "-"));
        }

        // filters

        private final ListProperty<Filter<T>> filters = new SimpleListProperty<>(FXCollections.observableArrayList());

        public final ObservableList<Filter<T>> getFilters() {
            return filters.get();
        }

        public final ListProperty<Filter<T>> filtersProperty() {
            return filters;
        }

        public final void setFilters(ObservableList<Filter<T>> filters) {
            this.filters.set(filters);
        }

        // group name

        private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

        /**
         * The name of the filter as shown in the filter group's dropdown list.
         *
         * @return the filter name
         */
        public final StringProperty nameProperty() {
            return name;
        }

        public final String getName() {
            return name.get();
        }

        public final void setName(String name) {
            this.name.set(name);
        }

        private final StringProperty id = new SimpleStringProperty(this, "id");

        public final String getId() {
            return id.get();
        }

        /**
         * An identifier, useful for persisting session state of the filter view.
         * @return the ID of the filter group
         */
        public final StringProperty idProperty() {
            return id;
        }

        public final void setId(String id) {
            this.id.set(id);
        }
    }

    /**
     * A filter is a predicate that will be used for filtering the elements of an
     * observable list. A filter may only be added to one {@link FilterGroup}.
     *
     * @see FilterGroup#getFilters()
     * @see FilteredList#setPredicate(Predicate)
     *
     * @param <T> the type of the model objects managed by the filter view
     */
    public abstract static class Filter<T> implements Predicate<T> {

        private FilterGroup<T> group;

        /**
         * Constructs a new filter with the given name.
         *
         * @param name the name of the filter (e.g. "Male")
         * @param id the id of the filter
         */
        public Filter(String name, String id) {
            Objects.requireNonNull(name, "filter name can not be null");
            setName(name);
            setId(id);
        }

        /**
         * Constructs a new filter with the given name.
         *
         * @param name the name of the filter (e.g. "Male")
         */
        public Filter(String name) {
            this(name, name.toLowerCase().replace("_", "-").replace("&", "and").replace(" ", "-"));
        }

        /**
         * Returns the group to which the filter belongs.
         *
         * @see FilterGroup#getFilters()
         * @return the filter's "parent" group
         */
        public FilterGroup<T> getGroup() {
            return group;
        }

        void setGroup(FilterGroup<T> group) {
            this.group = group;
        }

        // name

        private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

        /**
         * The name of the filter as shown inside the UI.
         *
         * @return the filter's name
         */
        public final StringProperty nameProperty() {
            return name;
        }

        public final String getName() {
            return name.get();
        }

        public final void setName(String name) {
            this.name.set(name);
        }

        private final StringProperty id = new SimpleStringProperty(this, "id");

        public final String getId() {
            return id.get();
        }

        /**
         * An identifier, useful for persisting session state of the filter view.
         * @return the ID of the filter group
         */
        public final StringProperty idProperty() {
            return id;
        }

        public final void setId(String id) {
            this.id.set(id);
        }
    }
}
