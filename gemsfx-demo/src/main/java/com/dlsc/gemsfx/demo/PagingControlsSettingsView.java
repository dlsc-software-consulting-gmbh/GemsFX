package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.paging.PagingControlBase;
import com.dlsc.gemsfx.paging.PagingControls;
import com.dlsc.gemsfx.Spacer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PagingControlsSettingsView extends VBox {

    public PagingControlsSettingsView(PagingControlBase pagingControls) {
        setSpacing(10);

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page index: " + pagingControls.getPage(), pagingControls.pageProperty()));

        Label pageCountLabel = new Label();
        pageCountLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page count: " + pagingControls.getPageCount(), pagingControls.pageCountProperty()));

        Label pageSizeLabel = new Label();
        pageSizeLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page size: " + pagingControls.getPageSize(), pagingControls.pageSizeProperty()));

        ChoiceBox<PagingControls.FirstLastPageDisplayMode> firstLastPageDisplayModeBox = new ChoiceBox<>();
        firstLastPageDisplayModeBox.getItems().setAll(PagingControls.FirstLastPageDisplayMode.values());
        firstLastPageDisplayModeBox.valueProperty().bindBidirectional(pagingControls.firstLastPageDisplayModeProperty());

        CheckBox showPreviousNextButton = new CheckBox("Show prev / next buttons");
        showPreviousNextButton.selectedProperty().bindBidirectional(pagingControls.showPreviousNextPageButtonProperty());

        CheckBox showPageSizeSelector = new CheckBox("Show page size selector");
        showPageSizeSelector.selectedProperty().bindBidirectional(pagingControls.showPageSizeSelectorProperty());

        ChoiceBox<PagingControlBase.MessageLabelStrategy> strategyChoiceBox = new ChoiceBox<>();
        strategyChoiceBox.getItems().addAll(PagingControlBase.MessageLabelStrategy.values());
        strategyChoiceBox.valueProperty().bindBidirectional(pagingControls.messageLabelStrategyProperty());

        ChoiceBox<Integer> maxPageIndicatorsBox = new ChoiceBox<>();
        List<Integer> counts = new ArrayList<>(List.of(0, 1, 2, 5, 10));
        if (!counts.contains(pagingControls.getMaxPageIndicatorsCount())) {
            counts.add(pagingControls.getMaxPageIndicatorsCount());
        }

        maxPageIndicatorsBox.getItems().setAll(counts);
        maxPageIndicatorsBox.setValue(pagingControls.getMaxPageIndicatorsCount());
        maxPageIndicatorsBox.valueProperty().addListener(it -> pagingControls.setMaxPageIndicatorsCount(maxPageIndicatorsBox.getValue()));

        CheckBox sameWidthButtonsBox = new CheckBox("Same width page buttons");
        sameWidthButtonsBox.selectedProperty().bindBidirectional(pagingControls.sameWidthPageButtonsProperty());

        HBox displayModeBox = new HBox(5, new Label("First / last buttons: "), firstLastPageDisplayModeBox);
        displayModeBox.setAlignment(Pos.CENTER_LEFT);

        HBox strategyBox = new HBox(5, new Label("Label strategy: "), strategyChoiceBox);
        strategyBox.setAlignment(Pos.CENTER_LEFT);

        HBox indicatorBox = new HBox(5, new Label("# Indicators: "), maxPageIndicatorsBox);
        indicatorBox.setAlignment(Pos.CENTER_LEFT);

        FlowPane flowPane = new FlowPane(pageLabel, pageSizeLabel, pageCountLabel, new Spacer(), showPreviousNextButton, showPageSizeSelector, sameWidthButtonsBox,
                displayModeBox, strategyBox, indicatorBox);
        flowPane.setVgap(10);
        flowPane.setHgap(20);

        setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().setAll(flowPane);
    }
}
