package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangeControlBase;
import com.dlsc.gemsfx.daterange.DateRangeView;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.YearMonth;

public class DateRangeViewSkin extends SkinBase<DateRangeView> {

    private final CalendarView startMonth;
    private final CalendarView endMonth;
    private final Label quickSelectLabel;
    private final VBox quickSelectBox;
    private final Button applyButton;
    private final Button cancelButton;

    public DateRangeViewSkin(DateRangeView view) {
        super(view);

        startMonth = createCalendarView();
        startMonth.setShowDaysOfPreviousOrNextMonth(true);
        startMonth.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        startMonth.getSelectionModel().setSelectionMode(CalendarView.SelectionModel.SelectionMode.DATE_RANGE);
        startMonth.setYearMonth(YearMonth.now().minusMonths(1));
        startMonth.setShowToday(false);
        startMonth.getSelectionModel().setSelectionMode(CalendarView.SelectionModel.SelectionMode.DATE_RANGE);

        endMonth = createCalendarView();
        endMonth.setShowDaysOfPreviousOrNextMonth(true);
        endMonth.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        endMonth.setSelectionModel(startMonth.getSelectionModel());
        endMonth.setYearMonth(YearMonth.now());
        endMonth.setShowToday(false);
        endMonth.setYearMonth(YearMonth.now().plusMonths(1));

        startMonth.disableNextMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startMonth.getYearMonth().equals(endMonth.getYearMonth().minusMonths(1)), startMonth.yearMonthProperty(), endMonth.yearMonthProperty()));
        endMonth.disablePreviousMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startMonth.getYearMonth().equals(endMonth.getYearMonth().minusMonths(1)), startMonth.yearMonthProperty(), endMonth.yearMonthProperty()));

//        startMonth.yearMonthProperty().addListener((obs, oldDate, newDate) -> {
//            if (newDate.isAfter(endMonth.getYearMonth())) {
//                startMonth.setDate(LocalDate.of(oldDate.getYear(), oldDate.getMonth(), 1));
//            }
//        });
//
//        endMonth.yearMonthProperty().addListener((obs, oldDate, newDate) -> {
//            if (newDate.isBefore(startMonth.getYearMonth())) {
//                endMonth.setDate(LocalDate.of(oldDate.getYear(), oldDate.getMonth(), 1));
//            }
//        });

        quickSelectLabel = new Label("QUICK SELECT");
        quickSelectLabel.getStyleClass().add("quick-select-title");
        quickSelectLabel.setMinWidth(Region.USE_PREF_SIZE);

        applyButton = new Button("APPLY");
        applyButton.setMinWidth(Region.USE_PREF_SIZE);

//        applyButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
//            if (!startMonth.getSelectedDates().isEmpty() && !endMonth.getSelectedDates().isEmpty()) {
//                LocalDate st = startMonth.getSelectedDates().iterator().next();
//                LocalDate et = endMonth.getSelectedDates().iterator().next();
//                return et.isBefore(st);
//            }
//            return false;
//        }, startMonth.getSelectedDates(), endMonth.getSelectedDates()));

//        applyButton.setOnAction(evt -> {
//            DateRange selectedDateRange = new DateRange(
//                    startMonth.getSelectedDates().iterator().next(),
//                    endMonth.getSelectedDates().iterator().next());
//
//            boolean foundPreset = false;
//            for (DateRangePreset preset : view.getPresets()) {
//                if (Objects.equals(preset.getStartDate(), selectedDateRange.getStartDate()) && Objects.equals(preset.getEndDate(), selectedDateRange.getEndDate())) {
//                    view.setSelectedDateRange(preset);
//                    foundPreset = true;
//                }
//            }
//
//            if (!foundPreset) {
//                view.setSelectedDateRange(selectedDateRange);
//            }
//
//            view.getOnClose().run();
//        });

        cancelButton = new Button("CANCEL");
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);
        cancelButton.setOnAction(evt -> view.getOnClose().run());

        applyButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        quickSelectBox = new VBox();
        quickSelectBox.getStyleClass().add("quick-select-box");
        quickSelectBox.setFillWidth(true);
        quickSelectBox.setPrefWidth(270);
        quickSelectBox.visibleProperty().bind(view.modeProperty().isEqualTo(DateRangeControlBase.Mode.ADVANCED));
        quickSelectBox.managedProperty().bind(view.modeProperty().isEqualTo(DateRangeControlBase.Mode.ADVANCED));

        HBox monthsBox = new HBox(startMonth, endMonth);
        monthsBox.getStyleClass().add("months-box");

        Label centerPiece = new Label("TO");
        centerPiece.getStyleClass().add("center-piece");

        StackPane stackPane = new StackPane(monthsBox, centerPiece);

        HBox hBox = new HBox(stackPane, quickSelectBox);
        hBox.getStyleClass().add("range-view-container");
        hBox.setFillHeight(true);

        HBox.setHgrow(startMonth, Priority.ALWAYS);
        HBox.setHgrow(endMonth, Priority.ALWAYS);
        HBox.setHgrow(quickSelectBox, Priority.ALWAYS);

        HBox.setMargin(quickSelectBox, new Insets(0, 0, 0, 10));

        view.selectedDateRangeProperty().addListener((obs, oldRange, newRange) -> {
            if (newRange != null) {
                // the start and end dates can be visible in both month views
                applyRangeToMonthViews(newRange);
            } else {
                view.setSelectedDateRange(oldRange);
            }
        });

        view.getPresets().addListener((Observable it) -> updateView());

        getChildren().add(hBox);
        updateView();
    }

    protected CalendarView createCalendarView() {
        return new CalendarView();
    }

    private void applyRangeToMonthViews(DateRange newRange) {
//        startMonth.getSelectedDates().clear();
//        startMonth.getSelectedDates().add(newRange.getStartDate());
//        startMonth.setDate(newRange.getStartDate());
//
//        endMonth.getSelectedDates().clear();
//        endMonth.getSelectedDates().add(newRange.getEndDate());
//        endMonth.setDate(newRange.getEndDate());
    }

    private void updateView() {
        DateRangeView view = getSkinnable();
        DateRange range = view.getSelectedDateRange();

        if (range != null) {
//            startMonth.getSelectedDates().add(range.getStartDate());
//            endMonth.getSelectedDates().add(range.getEndDate());
        }

        quickSelectBox.getChildren().clear();
        quickSelectBox.getChildren().add(quickSelectLabel);

        view.getPresets().forEach(rangePreset -> {
            Label l = new Label(rangePreset.getTitle());
            l.getStyleClass().add("preset-label");
            l.setOnMouseClicked(evt -> applyRangeToMonthViews(rangePreset));
            quickSelectBox.getChildren().add(l);

            Separator separator = new Separator(Orientation.HORIZONTAL);
            quickSelectBox.getChildren().add(separator);
        });

        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);

        quickSelectBox.getChildren().add(filler);
        quickSelectBox.getChildren().add(applyButton);
        quickSelectBox.getChildren().add(cancelButton);
    }
}
