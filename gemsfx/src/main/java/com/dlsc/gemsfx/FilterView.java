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
        return getClass().getResource("filter-view.css").toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterViewSkin<>(this);
    }

    private final BooleanProperty showHeader = new SimpleBooleanProperty(this, "showHeader", true);

    public boolean isShowHeader() {
        return showHeader.get();
    }

    public BooleanProperty showHeaderProperty() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader.set(showHeader);
    }

    private final ObjectProperty<Node> extras = new SimpleObjectProperty<>(this, "extras");

    public Node getExtras() {
        return extras.get();
    }

    public ObjectProperty<Node> extrasProperty() {
        return extras;
    }

    public void setExtras(Node extras) {
        this.extras.set(extras);
    }

    private final StringProperty title = new SimpleStringProperty(this, "title", "Untitled");

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    private final StringProperty titlePostfix = new SimpleStringProperty(this, "titlePostfix", "");

    public String getTitlePostfix() {
        return titlePostfix.get();
    }

    public StringProperty titlePostfixProperty() {
        return titlePostfix;
    }

    public void setTitlePostfix(String titlePostfix) {
        this.titlePostfix.set(titlePostfix);
    }

    private final StringProperty titlePostfixStyle = new SimpleStringProperty(this, "titlePostfix", "");

    public String getTitlePostfixStyle() {
        return titlePostfixStyle.get();
    }

    public StringProperty titlePostfixStyleProperty() {
        return titlePostfixStyle;
    }

    public void setTitlePostfixStyle(String titlePostfixStyle) {
        this.titlePostfixStyle.set(titlePostfixStyle);
    }

    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle", "");

    public String getSubtitle() {
        return subtitle.get();
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public ObservableList getItems() {
        return items.get();
    }

    public ListProperty<T> itemsProperty() {
        return items;
    }

    public void setItems(ObservableList items) {
        this.items.set(items);
    }

    private final ReadOnlyListWrapper<T> filteredItems = new ReadOnlyListWrapper<>();

    public ReadOnlyListProperty<T> filteredItemsProperty() {
        return filteredItems.getReadOnlyProperty();
    }

    public ObservableList getFilteredItems() {
        return filteredItems.getReadOnlyProperty();
    }

    // text filter

    private final StringProperty filterText = new SimpleStringProperty(this, "filterText");

    public String getFilterText() {
        return filterText.get();
    }

    public StringProperty filterTextProperty() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText.set(filterText);
    }

    // text filter predicate provider

    private final ObjectProperty<Callback<String, Predicate<T>>> textFilterProvider = new SimpleObjectProperty<>(this, "textFilterProvider");

    public Callback<String, Predicate<T>> getTextFilterProvider() {
        return textFilterProvider.get();
    }

    public ObjectProperty<Callback<String, Predicate<T>>> textFilterProviderProperty() {
        return textFilterProvider;
    }

    public void setTextFilterProvider(Callback<String, Predicate<T>> textFilterProvider) {
        this.textFilterProvider.set(textFilterProvider);
    }

    // filter groups

    private final ObservableList<FilterGroup<T>> filterGroups = FXCollections.observableArrayList();

    public ObservableList<FilterGroup<T>> getFilterGroups() {
        return filterGroups;
    }

    // filters

    private final ObservableList<Filter> filters = FXCollections.observableArrayList();

    public ObservableList<Filter> getFilters() {
        return filters;
    }

    // additional filter predicate

    private final ObjectProperty<Predicate<T>> additionalFilterPredicate = new SimpleObjectProperty<>(this, "additionalFilterPredicate", item -> true);

    public Predicate<T> getAdditionalFilterPredicate() {
        return additionalFilterPredicate.get();
    }

    public ObjectProperty<Predicate<T>> additionalFilterPredicateProperty() {
        return additionalFilterPredicate;
    }

    public void setAdditionalFilterPredicate(Predicate<T> additionalFilterPredicate) {
        this.additionalFilterPredicate.set(additionalFilterPredicate);
    }

    // filter predicate

    private final ReadOnlyObjectWrapper<Predicate<T>> filterPredicate = new ReadOnlyObjectWrapper<>(this, "filterPredicate", item -> true);

    public Predicate getFilterPredicate() {
        return filterPredicate.get();
    }

    public ReadOnlyObjectProperty<Predicate<T>> filterPredicateProperty() {
        return filterPredicate.getReadOnlyProperty();
    }


    public static class FilterGroup<T> {

        private final String id;

        public FilterGroup(String name) {
            setName(name);
            id = name.toLowerCase().replace(" ", "-");

            filters.addListener((Change<? extends Filter<T>> change) -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(filter -> filter.setGroup(this));
                    }
                }
            });
        }

        public String getId() {
            return id;
        }

        // filters

        private final ObservableList<Filter<T>> filters = FXCollections.observableArrayList();

        public ObservableList<Filter<T>> getFilters() {
            return filters;
        }

        // filter groups

        private StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

        public StringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }
    }

    public static class Filter<T> implements Predicate<T> {

        private final String id;
        private FilterGroup<T> group;

        public Filter(String name) {
            Objects.requireNonNull(name, "filter name can not be null");
            setName(name);
            this.id = name.toLowerCase().replace(" ", "-");
        }

        public String getId() {
            return id;
        }

        FilterGroup<T> getGroup() {
            return group;
        }

        void setGroup(FilterGroup<T> group) {
            this.group = group;
        }

        private StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

        public StringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        @Override
        public boolean test(T t) {
            return false;
        }
    }
}
