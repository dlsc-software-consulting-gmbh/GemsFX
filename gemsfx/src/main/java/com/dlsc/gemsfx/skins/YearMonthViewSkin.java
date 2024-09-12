package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.YearMonthView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class YearMonthViewSkin extends SkinBase<YearMonthView> {

    private static final PseudoClass SELECTED_MONTH_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass CURRENT_MONTH_PSEUDO_CLASS = PseudoClass.getPseudoClass("current");

    private final ObjectProperty<Integer> year = new SimpleObjectProperty<>(this, "year");
    private boolean updatingMonthBox;

    public YearMonthViewSkin(YearMonthView control) {
        super(control);

        year.set(control.getValue().getYear());

        Label yearLabel = new Label();
        yearLabel.getStyleClass().add("year-label");
        yearLabel.textProperty().bind(Bindings.createStringBinding(() -> Integer.toString(year.get()), year));
        yearLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(yearLabel, Priority.ALWAYS);

        Region leftArrow = new Region();
        leftArrow.getStyleClass().addAll("arrow", "left-arrow");

        Region rightArrow = new Region();
        rightArrow.getStyleClass().addAll("arrow", "right-arrow");

        StackPane leftArrowButton = new StackPane(leftArrow);
        leftArrowButton.getStyleClass().addAll("arrow-button", "left-button");
        leftArrowButton.setOnMouseClicked(evt -> year.set(year.get() - 1));

        StackPane rightArrowButton = new StackPane(rightArrow);
        rightArrowButton.getStyleClass().addAll("arrow-button", "right-button");
        rightArrowButton.setOnMouseClicked(evt -> year.set(year.get() + 1));

        HBox header = new HBox(leftArrowButton, yearLabel, rightArrowButton);
        header.getStyleClass().add("header");
        header.visibleProperty().bind(control.showYearProperty());
        header.managedProperty().bind(control.showYearProperty());

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");
        addMonthBoxToGridPane(gridPane, control);

        Region divider = new Region();
        divider.getStyleClass().add("divider");
        gridPane.add(divider, 1, 0);
        GridPane.setRowSpan(divider, 6);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();

        col1.setHalignment(HPos.CENTER);
        col1.setHgrow(Priority.ALWAYS);

        col2.setPrefWidth(1);
        col2.setMinWidth(1);
        col2.setMaxWidth(1);

        col3.setHgrow(Priority.ALWAYS);
        col3.setHalignment(HPos.CENTER);

        gridPane.getColumnConstraints().setAll(col1, col2, col3);

        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(row);
        }

        VBox.setVgrow(gridPane, Priority.ALWAYS);
        VBox container = new VBox(header, gridPane);
        container.getStyleClass().add("container");

        header.setViewOrder(Double.NEGATIVE_INFINITY);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(container.widthProperty());
        clip.heightProperty().bind(container.heightProperty());
        container.setClip(clip);

        getChildren().add(container);

        control.valueProperty().subscribe(value -> {
            updatingMonthBox = true;
            year.set(value.getYear());
            updateMonthBoxes(value, gridPane);
            updatingMonthBox = false;
        });

        year.addListener(it -> {
            if (!updatingMonthBox) {
                updateMonthBoxes(control.getValue(), gridPane);
            }
        });
    }

    private void addMonthBoxToGridPane(GridPane gridPane, YearMonthView control) {
        for (Month month : Month.values()) {
            int columnIndex = (month.getValue() % 2 == 0) ? 2 : 0;
            int rowIndex = (month.getValue() - 1) / 2;
            gridPane.add(new MonthBox(month, control), columnIndex, rowIndex);
        }
    }

    /**
     * Updates the pseudo-class state of the MonthBoxes.
     */
    private void updateMonthBoxes(YearMonth value, GridPane gridPane) {
        Month selectedMonth = value.getMonth();
        int currentYear = LocalDate.now().getYear();
        Month currentMonth = LocalDate.now().getMonth();

        gridPane.getChildren().stream()
                .filter(node -> node instanceof MonthBox)
                .map(node -> (MonthBox) node)
                .forEach(box -> {
                    box.pseudoClassStateChanged(SELECTED_MONTH_PSEUDO_CLASS, box.getMonth() == selectedMonth);
                    box.pseudoClassStateChanged(CURRENT_MONTH_PSEUDO_CLASS, box.getMonth() == currentMonth && year.get() == currentYear);
                });
    }

    private class MonthBox extends VBox {

        private final Month month;

        public MonthBox(Month month, YearMonthView view) {
            getStyleClass().add("month-box");

            this.month = month;

            Label monthLabel = new Label(view.getConverter().toString(month));
            monthLabel.getStyleClass().add("month-label");
            monthLabel.setMinWidth(Region.USE_PREF_SIZE);
            monthLabel.setMaxWidth(Region.USE_PREF_SIZE);

            Region indicator = new Region();
            indicator.getStyleClass().add("indicator");
            indicator.setMinHeight(Region.USE_PREF_SIZE);
            VBox.setVgrow(indicator, Priority.NEVER);

            getChildren().setAll(monthLabel, indicator);
            GridPane.setMargin(this, new Insets(10, 30, 10, 30));

            setMaxWidth(Region.USE_PREF_SIZE);
            setAlignment(Pos.CENTER);
            setOnMouseClicked(evt -> view.setValue(YearMonth.of(year.get(), month.getValue())));
            disableProperty().bind(Bindings.createObjectBinding(() -> {
                YearMonth earliestMonth = view.getEarliestMonth();
                if (earliestMonth != null && YearMonth.of(view.getValue().getYear(), month.getValue()).isBefore(earliestMonth)) {
                    return true;
                }
                YearMonth latestMonth = view.getLatestMonth();
                return latestMonth != null && YearMonth.of(view.getValue().getYear(), month.getValue()).isAfter(latestMonth);
            }, view.earliestMonthProperty(), view.latestMonthProperty(), view.valueProperty()));
        }

        public final Month getMonth() {
            return month;
        }
    }
}
