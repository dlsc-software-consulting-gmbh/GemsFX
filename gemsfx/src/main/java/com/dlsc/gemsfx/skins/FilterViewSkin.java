package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.FilterView;
import com.dlsc.gemsfx.FilterView.Filter;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class FilterViewSkin<T> extends SkinBase<FilterView<T>> {

    private SearchTextField searchTextField = new SearchTextField();
    private HBox filterGroupsPane = new HBox();
    private FlowPane filtersPane = new FlowPane();

    public FilterViewSkin(FilterView<T> view) {
        super(view);

        Label titleLabel = new Label();
        titleLabel.textProperty().bind(view.titleProperty());
        titleLabel.getStyleClass().add("title");

        Label titlePostfixLabel = new Label();
        titlePostfixLabel.textProperty().bind(view.titlePostfixProperty());
        titlePostfixLabel.getStyleClass().addAll("title", "title-postfix");

        // the title postfix label can have an additional style
        updateTitlePostfixLabelStyle(titlePostfixLabel);
        view.titlePostfixStyleProperty().addListener(it -> updateTitlePostfixLabelStyle(titlePostfixLabel));

        HBox titleBox = new HBox(titleLabel, titlePostfixLabel);
        titleBox.getStyleClass().add("title-box");

        Label subtitleLabel = new Label();
        subtitleLabel.textProperty().bind(view.subtitleProperty());
        subtitleLabel.getStyleClass().add("subtitle");

        VBox titleAndSubtitleBox = new VBox(titleBox, subtitleLabel);
        HBox.setHgrow(titleAndSubtitleBox, Priority.ALWAYS);

        HBox headerBox;

        if (view.getExtras() != null) {
            headerBox = new HBox(titleAndSubtitleBox, searchTextField, view.getExtras());
        } else {
            headerBox = new HBox(titleAndSubtitleBox, searchTextField);
        }

        headerBox.setFillHeight(true);
        headerBox.getStyleClass().add("header-box");
        headerBox.visibleProperty().bind(view.showHeaderProperty());
        headerBox.managedProperty().bind(view.showHeaderProperty());

        view.extrasProperty().addListener((obs, oldExtras, newExtras) -> {
            if (oldExtras != null) {
                headerBox.getChildren().remove(oldExtras);
            }
            if (newExtras != null) {
                headerBox.getChildren().add(newExtras);
            }
        });

        filterGroupsPane.getStyleClass().add("filter-groups");
        filterGroupsPane.setFillHeight(true);
        filterGroupsPane.visibleProperty().bind(Bindings.isNotEmpty(view.getFilterGroups()));
        filterGroupsPane.managedProperty().bind(Bindings.isNotEmpty(view.getFilterGroups()));

        filtersPane.getStyleClass().add("filters");
        filtersPane.prefWrapLengthProperty().bind(view.widthProperty());
        filtersPane.visibleProperty().bind(Bindings.isNotEmpty(filtersPane.getChildren()));
        filtersPane.managedProperty().bind(Bindings.isNotEmpty(filtersPane.getChildren()));

        searchTextField.visibleProperty().bind(view.textFilterProviderProperty().isNotNull());
        searchTextField.managedProperty().bind(view.textFilterProviderProperty().isNotNull());
        searchTextField.textProperty().bindBidirectional(view.filterTextProperty());

        view.textFilterProviderProperty().addListener(it -> updateGroups());
        view.getFilterGroups().addListener((Observable it) -> updateGroups());

        view.getFilters().addListener((Observable it) -> updateFilters());
        view.filterTextProperty().addListener(it -> updateFilters());
        view.textFilterProviderProperty().addListener(it -> updateFilters());

        VBox vBox = new VBox(headerBox, filterGroupsPane, filtersPane);
        vBox.getStyleClass().add("filter-container");
        vBox.setFillWidth(true);

        getChildren().add(vBox);

        updateGroups();
        updateFilters();

        getSkinnable().getFilters().addListener((Observable it) -> Platform.runLater(() -> {
            for (Filter filter : filterItemMap.keySet()) {
                CheckMenuItem menuItem = filterItemMap.get(filter);
                menuItem.setSelected(getSkinnable().getFilters().contains(filter));
            }
        }));
    }

    private void updateTitlePostfixLabelStyle(Label label) {
        label.getStyleClass().setAll("title", "title-postfix");
        String style = getSkinnable().getTitlePostfixStyle();
        if (StringUtils.isNotBlank(style)) {
            label.getStyleClass().add(style);
        }
    }

    private final Map<Filter, CheckMenuItem> filterItemMap = new HashMap<>();

    private void updateGroups() {
        filterGroupsPane.getChildren().clear();

        filterItemMap.clear();

        getSkinnable().getFilterGroups().forEach(group -> {
            MenuButton menuButton = new MenuButton();
            menuButton.textProperty().bind(group.nameProperty());
            menuButton.setMaxWidth(Double.MAX_VALUE);
            menuButton.setMaxHeight(Double.MAX_VALUE);

            HBox.setHgrow(menuButton, Priority.ALWAYS);

            MenuItem all = new MenuItem("All");
            ObservableList<Filter<T>> activeFilters = getSkinnable().getFilters();

            all.setOnAction(evt -> {
                // first remove all, otherwise we end up with duplicates
                activeFilters.removeAll(group.getFilters());
                activeFilters.addAll(group.getFilters());
            });

            MenuItem none = new MenuItem("None");
            none.setOnAction(evt -> {
                // first remove all, otherwise we end up with duplicates
                activeFilters.removeAll(group.getFilters());
            });

            menuButton.getItems().addAll(all, none, new SeparatorMenuItem());

            group.getFilters().forEach(filter -> {
                CheckMenuItem item = new CheckMenuItem();
                item.textProperty().bind(filter.nameProperty());
                item.selectedProperty().addListener(it -> {
                    if (item.isSelected()) {
                        if (!activeFilters.contains(filter)) {
                            activeFilters.add(filter);
                        }
                    } else {
                        activeFilters.remove(filter);
                    }
                });
                menuButton.getItems().add(item);

                filterItemMap.put(filter, item);
            });

            filterGroupsPane.getChildren().add(menuButton);
        });

        // special style tag for last menu button
        if (!filterGroupsPane.getChildren().isEmpty()) {
            filterGroupsPane.getChildren().get(filterGroupsPane.getChildren().size() - 1).getStyleClass().add("last");
        }
    }

    private void updateFilters() {
        filtersPane.getChildren().clear();

        FilterView<T> filterView = getSkinnable();

        if (!filterView.getFilters().isEmpty() || StringUtils.isNotBlank(filterView.getFilterText())) {
            filterView.getFilters().forEach(f -> {
                ChipView<Filter> chipView = new ChipView<>();
                chipView.setValue(f);
                chipView.textProperty().bind(f.nameProperty());
                chipView.setOnClose(filter -> filterView.getFilters().remove(filter));
                filtersPane.getChildren().add(chipView);
            });

            String filterText = filterView.getFilterText();
            if (StringUtils.isNotBlank(filterText)) {
                ChipView<String> chipView = new ChipView<>();
                chipView.setValue(filterView.getFilterText());
                chipView.setText("\"" + filterView.getFilterText() + "\"");
                chipView.setOnClose(filter -> filterView.setFilterText(null));
                filtersPane.getChildren().add(chipView);
            }

            Label clearFilter = new Label("Clear Filter");
            clearFilter.getStyleClass().add("clear-filter-label");
            clearFilter.setOnMouseClicked(evt -> {
                filterView.getFilters().clear();
                filterView.setFilterText(null);
            });

            filtersPane.getChildren().add(clearFilter);
        }
    }
}
