package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import com.dlsc.gemsfx.daterange.DateRangeView;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class DateRangeViewSkin extends SkinBase<DateRangeView> {

    private final CalendarView startCalendarView;
    private final CalendarView endCalendarView;
    private final Label presetsTitleLabel;
    private final VBox presetsBox;
    private final Button applyButton;
    private final Button cancelButton;

    public DateRangeViewSkin(DateRangeView view) {
        super(view);

        SelectionModel selectionModel = view.getSelectionModel();

        startCalendarView = view.getStartCalendarView();
        startCalendarView.setSelectionModel(selectionModel);
        startCalendarView.setShowDaysOfPreviousOrNextMonth(true);
        startCalendarView.setYearMonth(YearMonth.now().minusMonths(1));
        startCalendarView.setShowToday(false);
        startCalendarView.setYearMonth(YearMonth.now());

        endCalendarView = view.getEndCalendarView();
        endCalendarView.setSelectionModel(selectionModel);
        endCalendarView.setShowDaysOfPreviousOrNextMonth(true);
        endCalendarView.setYearMonth(YearMonth.now());
        endCalendarView.setShowToday(false);
        endCalendarView.setYearMonth(YearMonth.now().plusMonths(1));

        startCalendarView.disableNextMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startCalendarView.getYearMonth().equals(endCalendarView.getYearMonth().minusMonths(1)), startCalendarView.yearMonthProperty(), endCalendarView.yearMonthProperty()));
        endCalendarView.disablePreviousMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startCalendarView.getYearMonth().equals(endCalendarView.getYearMonth().minusMonths(1)), startCalendarView.yearMonthProperty(), endCalendarView.yearMonthProperty()));

        presetsTitleLabel = new Label("PRESETS");
        presetsTitleLabel.getStyleClass().add("quick-select-title");
        presetsTitleLabel.setMinWidth(Region.USE_PREF_SIZE);

        applyButton = new Button("APPLY");
        applyButton.setMinWidth(Region.USE_PREF_SIZE);

        applyButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!selectionModel.getSelectedDates().isEmpty() && !endCalendarView.getSelectionModel().getSelectedDates().isEmpty()) {
                LocalDate st = selectionModel.getSelectedDate();
                LocalDate et = selectionModel.getSelectedEndDate();
                return et.isBefore(st);
            }
            return false;
        }, selectionModel.getSelectedDates(), endCalendarView.getSelectionModel().getSelectedDates()));

        applyButton.setOnAction(evt -> {
            DateRange selectedDateRange = new DateRange(
                    selectionModel.getSelectedDate(),
                    selectionModel.getSelectedEndDate());

            boolean foundPreset = false;
            for (DateRangePreset preset : view.getPresets()) {
                if (Objects.equals(preset.getStartDate(), selectedDateRange.getStartDate()) && Objects.equals(preset.getEndDate(), selectedDateRange.getEndDate())) {
                    view.setValue(preset);
                    foundPreset = true;
                }
            }

            if (!foundPreset) {
                view.setValue(selectedDateRange);
            }

            view.getOnClose().run();
        });

        cancelButton = new Button("CANCEL");
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);
        cancelButton.setOnAction(evt -> view.getOnClose().run());

        applyButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        presetsBox = new VBox();
        presetsBox.getStyleClass().add("quick-select-box");
        presetsBox.setFillWidth(true);
        presetsBox.setPrefWidth(270);
        presetsBox.visibleProperty().bind(view.showPresetsProperty());
        presetsBox.managedProperty().bind(view.showPresetsProperty());

        HBox monthsBox = new HBox(startCalendarView, endCalendarView);
        monthsBox.getStyleClass().add("months-box");

        Label centerPiece = new Label("TO");
        centerPiece.getStyleClass().add("center-piece");

        StackPane stackPane = new StackPane(monthsBox, centerPiece);
        stackPane.getStyleClass().add("stack-pane");

        HBox hBox = new HBox();

        view.presetsLocationProperty().addListener(it -> updateLayout(hBox, stackPane, presetsBox));
        updateLayout(hBox, stackPane, presetsBox);

        hBox.getStyleClass().add("range-view-container");
        hBox.setFillHeight(true);

        HBox.setHgrow(startCalendarView, Priority.ALWAYS);
        HBox.setHgrow(endCalendarView, Priority.ALWAYS);
        HBox.setHgrow(presetsBox, Priority.ALWAYS);

        HBox.setMargin(presetsBox, new Insets(0, 0, 0, 10));

        view.valueProperty().addListener((obs, oldRange, newRange) -> {
            if (newRange != null) {
                // the start and end dates can be visible in both month views
                applyRangeToMonthViews(newRange);
            } else {
                view.setValue(oldRange);
            }
        });

        view.getPresets().addListener((Observable it) -> updatePresetsView());

        getChildren().add(hBox);
        updatePresetsView();
        applyRangeToMonthViews(view.getValue());
    }

    private void updateLayout(HBox hBox, StackPane stackPane, VBox quickSelectBox) {
        Side quickSelectPosition = getSkinnable().getPresetsLocation();
        if (quickSelectPosition.equals(Side.LEFT)) {
            hBox.getChildren().setAll(quickSelectBox, stackPane);
        } else {
            hBox.getChildren().setAll(stackPane, quickSelectBox);
        }
    }

    private void applyRangeToMonthViews(DateRange newRange) {
        SelectionModel selectionModel = getSkinnable().getSelectionModel();
        selectionModel.clearSelection();
        if (newRange != null) {
            selectionModel.select(newRange.getStartDate());
            selectionModel.select(newRange.getEndDate());

            YearMonth fromMonth = YearMonth.from(newRange.getStartDate());
            YearMonth toMonth = YearMonth.from(newRange.getEndDate());
            if (fromMonth.isBefore(toMonth)) {
                startCalendarView.setYearMonth(fromMonth);
                endCalendarView.setYearMonth(toMonth);
            } else {
                startCalendarView.setYearMonth(fromMonth);
                endCalendarView.setYearMonth(fromMonth.plusMonths(1));
            }
        }
    }

    private void updatePresetsView() {
        DateRangeView view = getSkinnable();

        presetsBox.getChildren().clear();
        presetsBox.getChildren().add(presetsTitleLabel);

        view.getPresets().forEach(rangePreset -> {
            Label l = new Label(rangePreset.getTitle());
            l.getStyleClass().add("preset-label");
            l.setOnMouseClicked(evt -> applyRangeToMonthViews(rangePreset));
            presetsBox.getChildren().add(l);

            Separator separator = new Separator(Orientation.HORIZONTAL);
            presetsBox.getChildren().add(separator);
        });

        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);

        presetsBox.getChildren().add(filler);

        applyButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(applyButton, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);

        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonBar.setButtonUniformSize(applyButton, true);
        ButtonBar.setButtonUniformSize(cancelButton, true);

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().setAll(applyButton, cancelButton);

        presetsBox.getChildren().add(buttonBar);
    }
}
