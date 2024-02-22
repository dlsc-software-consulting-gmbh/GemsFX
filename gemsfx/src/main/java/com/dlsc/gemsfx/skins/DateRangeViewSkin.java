package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import com.dlsc.gemsfx.daterange.DateRangeView;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
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
    private final StackPane stackPane;
    private final HBox container;
    private final Label toLabel;
    private boolean updatingMonths;

    public DateRangeViewSkin(DateRangeView view) {
        super(view);

        SelectionModel selectionModel = view.getSelectionModel();

        toLabel = new Label();
        toLabel.textProperty().bind(view.toTextProperty());
        toLabel.getStyleClass().add("to-label");
        toLabel.setMouseTransparent(true);

        startCalendarView = view.getStartCalendarView();
        endCalendarView = view.getEndCalendarView();

        startCalendarView.latestDateProperty().bind(Bindings.createObjectBinding(() -> {
            YearMonth month = endCalendarView.getYearMonth().minusMonths(1);
            return month.atDay(month.lengthOfMonth());
        }, endCalendarView.yearMonthProperty()));

        endCalendarView.earliestDateProperty().bind(Bindings.createObjectBinding(() -> {
            YearMonth month = startCalendarView.getYearMonth().plusMonths(1);
            return month.atDay(1);
        }, startCalendarView.yearMonthProperty()));

        startCalendarView.disableNextMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startCalendarView.getYearMonth().equals(endCalendarView.getYearMonth().minusMonths(1)), startCalendarView.yearMonthProperty(), endCalendarView.yearMonthProperty()));
        endCalendarView.disablePreviousMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> startCalendarView.getYearMonth().equals(endCalendarView.getYearMonth().minusMonths(1)), startCalendarView.yearMonthProperty(), endCalendarView.yearMonthProperty()));

        startCalendarView.yearMonthProperty().addListener((obs, oldStartMonth, newStartMonth) -> {
            YearMonth endMonth = endCalendarView.getYearMonth();
            if (!newStartMonth.isBefore(endMonth) && !updatingMonths) {
                startCalendarView.setYearMonth(oldStartMonth);
            }
        });

        endCalendarView.yearMonthProperty().addListener((obs, oldEndMonth, newEndMonth) -> {
            YearMonth startMonth = startCalendarView.getYearMonth();
            if (!newEndMonth.isAfter(startMonth) && !updatingMonths) {
                endCalendarView.setYearMonth(oldEndMonth);
            }
        });

        presetsTitleLabel = new Label();
        presetsTitleLabel.textProperty().bind(view.presetTitleProperty());
        presetsTitleLabel.getStyleClass().add("presets-title");
        presetsTitleLabel.setMinWidth(Region.USE_PREF_SIZE);
        presetsTitleLabel.visibleProperty().bind(presetsTitleLabel.textProperty().isNotEmpty());
        presetsTitleLabel.managedProperty().bind(presetsTitleLabel.textProperty().isNotEmpty());

        applyButton = new Button();
        applyButton.textProperty().bind(view.applyTextProperty());
        applyButton.getStyleClass().add("apply-button");
        applyButton.setMinWidth(Region.USE_PREF_SIZE);
        applyButton.setMaxWidth(Double.MAX_VALUE);

        applyButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!selectionModel.getSelectedDates().isEmpty() && !endCalendarView.getSelectionModel().getSelectedDates().isEmpty()) {
                LocalDate st = selectionModel.getSelectedDate();
                LocalDate et = selectionModel.getSelectedEndDate();
                return et.isBefore(st);
            }
            return false;
        }, selectionModel.getSelectedDates(), endCalendarView.getSelectionModel().getSelectedDates()));

        applyButton.setOnAction(evt -> {
            LocalDate endDate = selectionModel.getSelectedEndDate();
            DateRange selectedDateRange;
            if (endDate == null) {
                selectedDateRange = new DateRange(selectionModel.getSelectedDate());
            } else {
                selectedDateRange = new DateRange(selectionModel.getSelectedDate(), endDate);
            }

            boolean foundPreset = false;
            for (DateRangePreset preset : view.getPresets()) {
                DateRange dateRange = preset.getDateRangeSupplier().get();
                if (Objects.equals(dateRange.getStartDate(), selectedDateRange.getStartDate()) && Objects.equals(dateRange.getEndDate(), selectedDateRange.getEndDate())) {
                    view.setValue(dateRange);
                    foundPreset = true;
                }
            }

            if (!foundPreset) {
                view.setValue(selectedDateRange);
            }

            view.getOnClose().run();
        });

        cancelButton = new Button();
        cancelButton.textProperty().bind(view.cancelTextProperty());
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setOnAction(evt -> view.getOnClose().run());

        presetsBox = new VBox();
        presetsBox.getStyleClass().add("presets-box");
        presetsBox.setFillWidth(true);
        presetsBox.visibleProperty().bind(view.showPresetsProperty());
        presetsBox.managedProperty().bind(view.showPresetsProperty());
        presetsBox.setMinWidth(Region.USE_PREF_SIZE);

        stackPane = new StackPane();
        stackPane.getStyleClass().add("stack-pane");

        container = new HBox();
        container.getStyleClass().add("range-view-container");
        container.setFillHeight(true);

        HBox.setHgrow(startCalendarView, Priority.ALWAYS);
        HBox.setHgrow(endCalendarView, Priority.ALWAYS);
        HBox.setHgrow(presetsBox, Priority.ALWAYS);

        view.valueProperty().addListener((obs, oldRange, newRange) -> {
            if (newRange != null) {
                // the start and end dates can be visible in both month views
                applyRangeToMonthViews(newRange);
            } else {
                view.setValue(oldRange);
            }
        });

        view.getPresets().addListener((Observable it) -> updatePresetsView());
        view.orientationProperty().addListener(it -> updateCalendarLayout());
        view.presetsLocationProperty().addListener(it -> updateLayout());

        getChildren().add(container);

        updatePresetsView();
        updateCalendarLayout();
        updateLayout();

        applyRangeToMonthViews(view.getValue());
    }

    private void updateLayout() {
        Side quickSelectPosition = getSkinnable().getPresetsLocation();
        if (quickSelectPosition.equals(Side.LEFT)) {
            container.getChildren().setAll(presetsBox, stackPane);
        } else {
            container.getChildren().setAll(stackPane, presetsBox);
        }
    }

    private void updateCalendarLayout() {
        DateRangeView view  = getSkinnable();

        if (view.getOrientation().equals(Orientation.HORIZONTAL)) {
            HBox monthsBox = new HBox(startCalendarView, endCalendarView);
            monthsBox.getStyleClass().add("months-box");
            stackPane.getChildren().setAll(monthsBox, toLabel);
        } else {
            VBox monthsBox = new VBox(startCalendarView, endCalendarView);
            monthsBox.getStyleClass().add("months-box");
            stackPane.getChildren().setAll(monthsBox, toLabel);
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

            try {
                updatingMonths = true;
                if (fromMonth.isBefore(toMonth)) {
                    startCalendarView.setYearMonth(fromMonth);
                    endCalendarView.setYearMonth(toMonth);
                } else {
                    startCalendarView.setYearMonth(fromMonth);
                    endCalendarView.setYearMonth(fromMonth.plusMonths(1));
                }
            } finally {
                updatingMonths = false;
            }
        }
    }

    private void updatePresetsView() {
        DateRangeView view = getSkinnable();

        presetsBox.getChildren().clear();
        presetsBox.getChildren().add(presetsTitleLabel);

        ObservableList<DateRangePreset> presets = view.getPresets();
        for (int i = 0; i < presets.size(); i++) {
            DateRangePreset rangePreset = presets.get(i);
            Label l = new Label(rangePreset.getTitle());
            l.getStyleClass().add("preset-name-label");
            l.setOnMouseClicked(evt -> applyRangeToMonthViews(rangePreset.getDateRangeSupplier().get()));
            presetsBox.getChildren().add(l);

            if (i < presets.size() - 1) {
                Separator separator = new Separator(Orientation.HORIZONTAL);
                presetsBox.getChildren().add(separator);
            }
        }

        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);

        presetsBox.getChildren().add(filler);

        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setMinWidth(Region.USE_PREF_SIZE);

        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMinWidth(Region.USE_PREF_SIZE);

        HBox.setHgrow(applyButton, Priority.ALWAYS);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);

        HBox buttonBar = new HBox(applyButton, cancelButton);
        buttonBar.visibleProperty().bind(view.showCancelAndApplyButtonProperty());
        buttonBar.managedProperty().bind(view.showCancelAndApplyButtonProperty());
        buttonBar.getStyleClass().add("buttons-box");

        presetsBox.getChildren().add(buttonBar);
    }
}
