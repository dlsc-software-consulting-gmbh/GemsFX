package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingControlBase;
import com.dlsc.gemsfx.PagingControls;
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
        pageLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page Index: " + pagingControls.getPage(), pagingControls.pageProperty()));

        Label pageCountLabel = new Label();
        pageCountLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page count: " + pagingControls.getPageCount(), pagingControls.pageCountProperty()));

        ChoiceBox<PagingControls.FirstLastPageDisplayMode> displayModeChoiceBox = new ChoiceBox<>();
        displayModeChoiceBox.getItems().setAll(PagingControls.FirstLastPageDisplayMode.values());
        displayModeChoiceBox.valueProperty().bindBidirectional(pagingControls.firstLastPageDisplayModeProperty());

        CheckBox showPreviousNextButton = new CheckBox("Show prev / next buttons");
        showPreviousNextButton.selectedProperty().bindBidirectional(pagingControls.showPreviousNextPageButtonProperty());

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

        HBox displayModeBox = new HBox(5, new Label("Display mode: "), displayModeChoiceBox);
        displayModeBox.setAlignment(Pos.CENTER_LEFT);

        HBox strategyBox = new HBox(5, new Label("Label strategy: "), strategyChoiceBox);
        strategyBox.setAlignment(Pos.CENTER_LEFT);

        HBox indicatorBox = new HBox(5, new Label("# Indicators: "), maxPageIndicatorsBox);
        indicatorBox.setAlignment(Pos.CENTER_LEFT);

        FlowPane flowPane = new FlowPane(pageLabel, pageCountLabel, new Spacer(), showPreviousNextButton, displayModeBox, strategyBox, indicatorBox);
        flowPane.setVgap(10);
        flowPane.setHgap(20);

        setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().setAll(flowPane);
    }
}
