package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import com.dlsc.gemsfx.daterange.DateRangeView;
import com.dlsc.gemsfx.util.Utils;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.layout.*;
import javafx.stage.WindowEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DateRangePickerSkin extends CustomComboBoxSkinBase<DateRangePicker> {

    private Label titleLabel;
    private Label rangeLabel;

    private DateRangeView view;
    private HBox hBox;

    public DateRangePickerSkin(DateRangePicker picker) {
        super(picker);

        view = picker.getDateRangeView();
        view.setFocusTraversable(false); // keep the picker focused / blue border
        view.valueProperty().bindBidirectional(getSkinnable().valueProperty());
        view.setOnClose(this::hide);

        picker.setOnMouseClicked(evt -> picker.show());
        picker.setOnTouchPressed(evt -> picker.show());

        InvalidationListener updateLabelsListener = it -> updateLabels();
        picker.valueProperty().addListener(updateLabelsListener);
        picker.formatterProperty().addListener(updateLabelsListener);

        picker.valueProperty().addListener(it -> view.setValue(picker.getValue()));
        picker.smallProperty().addListener(it -> updateView());

        updateView();
        updateLabels();
    }

    @Override
    protected double computePrefHeight(double width, double v1, double v2, double v3, double v4) {
        return hBox.prefHeight(width);
    }

    protected double computeMaxHeight(double width, double v1, double v2, double v3, double v4) {
        return hBox.prefHeight(width);
    }

    protected Node getPopupContent() {
        return view;
    }

    private void updateView() {
        DateRangePicker picker = getSkinnable();

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");

        titleLabel.visibleProperty().bind(picker.showPresetTitleProperty());
        titleLabel.managedProperty().bind(picker.showPresetTitleProperty());

        rangeLabel = new Label();
        rangeLabel.getStyleClass().add("range-label");

        Region icon = new Region();
        icon.getStyleClass().add("icon");

        StackPane iconButton = new StackPane(icon);
        iconButton.getStyleClass().add("icon-button");
        iconButton.visibleProperty().bind(picker.showIconProperty());
        iconButton.managedProperty().bind(picker.showIconProperty());

        rangeLabel.setGraphic(iconButton);

        Pane pane;

        if (!picker.getSmall()) {
            pane = new VBox(titleLabel, rangeLabel);
            pane.getStyleClass().remove("small");
        } else {
            Region divider = new Region();
            divider.getStyleClass().add("divider");
            divider.visibleProperty().bind(picker.showPresetTitleProperty());
            divider.managedProperty().bind(picker.showPresetTitleProperty());
            pane = new HBox(titleLabel, divider, rangeLabel);
            pane.getStyleClass().add("small");
        }

        pane.getStyleClass().add("inner-range-container");
        pane.setMinWidth(Region.USE_PREF_SIZE);

        Region arrow = new Region();
        arrow.getStyleClass().add("arrow");

        StackPane arrowButton = new StackPane(arrow);
        arrowButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        arrowButton.getStyleClass().add("arrow-button");

        HBox.setHgrow(pane, Priority.ALWAYS);
        hBox = new HBox(pane, arrowButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("outer-range-container");

        updateLabels();

        getChildren().setAll(hBox);
    }

    private void updateLabels() {
        DateRange dateRange = getSkinnable().getValue();
        if (dateRange != null) {
            if (dateRange instanceof DateRangePreset) {
                DateRangePreset preset = (DateRangePreset) dateRange;
                titleLabel.setText(preset.getTitle());
            } else {
                titleLabel.setText("Custom range");
            }
            rangeLabel.setText(toString(dateRange));
        } else {
            titleLabel.setText("");
            rangeLabel.setText("");
        }
    }

    public String toString(DateRange range) {
        DateTimeFormatter formatter = getSkinnable().getFormatter();

        if (range.getStartDate().equals(range.getEndDate())) {
            return formatter.format(range.getStartDate());
        }

        return formatter.format(range.getStartDate()) + " - " + formatter.format(range.getEndDate());
    }
}
