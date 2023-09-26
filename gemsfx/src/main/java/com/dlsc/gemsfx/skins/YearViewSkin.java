package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.YearView;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

public class YearViewSkin extends SkinBase<YearView> {

    private final Label yearRangeLabel;
    private final HBox header;
    private final GridPane gridPane;

    private int offset = 0;

    public YearViewSkin(YearView yearView) {
        super(yearView);

        yearRangeLabel = new Label();
        yearRangeLabel.getStyleClass().add("year-range-label");
        yearRangeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(yearRangeLabel, Priority.ALWAYS);

        Region leftArrow = new Region();
        leftArrow.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        leftArrow.getStyleClass().addAll("arrow", "left-arrow");

        Region rightArrow = new Region();
        rightArrow.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        rightArrow.getStyleClass().addAll("arrow", "right-arrow");

        StackPane leftArrowButton = new StackPane(leftArrow);
        leftArrowButton.setMaxHeight(Double.MAX_VALUE);
        leftArrowButton.getStyleClass().addAll("arrow-button", "left-button");
        leftArrowButton.setOnMouseClicked(evt -> {
            offset--;
            buildGrid();
            evt.consume();
        });

        StackPane rightArrowButton = new StackPane(rightArrow);
        rightArrowButton.getStyleClass().addAll("arrow-button", "right-button");
        rightArrowButton.setOnMouseClicked(evt -> {
            offset++;
            buildGrid();
            evt.consume();
        });

        header = new HBox(leftArrowButton, yearRangeLabel, rightArrowButton);
        header.addEventHandler(MouseEvent.MOUSE_CLICKED, Event::consume);
        header.getStyleClass().add("header");
        header.setViewOrder(Double.NEGATIVE_INFINITY);
        header.setFillHeight(true);

        gridPane = new GridPane();
        gridPane.getStyleClass().add("grid-pane");

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(25);
            gridPane.getColumnConstraints().add(col1);
        }

        int numberOfRows = 5;
        for (int i = 0; i < numberOfRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100d / numberOfRows);
            gridPane.getRowConstraints().add(row);
        }

        getChildren().addAll(header, gridPane);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(yearView.widthProperty());
        clip.heightProperty().bind(yearView.heightProperty());
        yearView.setClip(clip);

        InvalidationListener buildGridListener = obs -> buildGrid();
        yearView.valueProperty().addListener(buildGridListener);
        yearView.rowsProperty().addListener(buildGridListener);
        yearView.colsProperty().addListener(buildGridListener);

        buildGrid();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double headerHeight = snapSizeY(header.prefHeight(-1));
        header.resizeRelocate(contentX, contentY, contentWidth, headerHeight);
        gridPane.resizeRelocate(contentX, contentY + headerHeight, contentWidth, contentHeight - headerHeight);
    }

    private void buildGrid() {
        YearView yearView = getSkinnable();

        int rows = yearView.getRows();
        int cols = yearView.getCols();

        final int visibleYears = rows * cols;

        Year selectedYear = yearView.getValue();
        int currentYear = LocalDate.now().getYear();
        int firstYear = ((Optional.ofNullable(selectedYear).map(Year::getValue).orElse(currentYear) / visibleYears) * visibleYears) + (offset * visibleYears);

        gridPane.getChildren().clear();
        yearRangeLabel.setText(firstYear + "-" + (firstYear + visibleYears - 1));

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < cols; column++) {
                final int finalYear = firstYear;

                Node yearNode = createYearNode(finalYear);

                yearNode.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    Year earliestYear = yearView.getEarliestYear();
                    if (earliestYear != null && Year.of(finalYear).isBefore(earliestYear)) {
                        return true;
                    }
                    Year latestYear = yearView.getLatestYear();
                    if (latestYear != null && Year.of(finalYear).isAfter(latestYear)) {
                        return true;
                    }
                    return false;
                }, yearView.earliestYearProperty(), yearView.latestYearProperty()));

                yearNode.setOnMouseClicked(evt -> {
                    offset = 0;
                    yearView.setValue(Year.of(finalYear));
                });

                if (selectedYear != null && firstYear == selectedYear.getValue()) {
                    yearNode.getStyleClass().add("selected");
                }

                if (firstYear == currentYear) {
                    yearNode.getStyleClass().add("current");
                }

                gridPane.add(yearNode, column, row);
                firstYear++;
            }
        }
    }

    private Node createYearNode(int year) {
        Label yearLabel = new Label(Integer.toString(year));
        yearLabel.setMinWidth(Region.USE_PREF_SIZE);
        yearLabel.getStyleClass().add("year-label");

        YearView view = getSkinnable();

        Region selectionIndicator = new Region();
        selectionIndicator.visibleProperty().bind(Bindings.createBooleanBinding(() -> view.getYear() == year, view.valueProperty()));
        selectionIndicator.getStyleClass().add("selection-indicator");

        VBox box = new VBox(yearLabel, selectionIndicator);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("year-box");

        return box;
    }

}
