package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.gridtable.GridTableCell;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.gridtable.GridTableView;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.spreadsheet.Grid;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @param <S> grid table item type
 */
public class GridTableViewSkin<S> extends SkinBase<GridTableView<S>> {

    private final GridPane gridPane = new GridPane();

    // dummy region
    private final LoadingPane loadingPane = new LoadingPane(new Region()) {
        @Override
        public String getUserAgentStylesheet() {
            return Objects.requireNonNull(GridTableView.class.getResource("grid-table-view.css")).toExternalForm();
        }
    };

    public GridTableViewSkin(GridTableView<S> tableView) {
        super(tableView);

        ListChangeListener<S> listChangeListener = c -> updateView();
        tableView.itemsProperty().addListener(listChangeListener);

        Bindings.bindContent(gridPane.getColumnConstraints(), tableView.columnsProperty());
        tableView.columnsProperty().addListener((Observable it) -> updateView());

        gridPane.getStyleClass().add("grid-pane");

        getChildren().add(gridPane);

        tableView.getProperties().addListener((MapChangeListener<? super Object, ? super Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("refresh-items")) {
                    updateView();
                }
            }
        });

        loadingPane.statusProperty().bindBidirectional(tableView.loadingStatusProperty());
        loadingPane.commitDelayProperty().bindBidirectional(tableView.commitLoadStatusDelayProperty());
        loadingPane.visibleProperty().bind(loadingPane.committedStatusProperty().isNotEqualTo(LoadingPane.Status.OK));
        loadingPane.managedProperty().bind(loadingPane.committedStatusProperty().isNotEqualTo(LoadingPane.Status.OK));

        GridPane.setHgrow(loadingPane, Priority.ALWAYS);
        GridPane.setVgrow(loadingPane, Priority.ALWAYS);
        GridPane.setFillHeight(loadingPane, true);
        GridPane.setFillWidth(loadingPane, true);

        updateView();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.minHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.prefHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.maxHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.minWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return gridPane.maxWidth(height) + leftInset + rightInset;
    }

    private void updateView() {
        gridPane.getChildren().clear();
        createHeader();
        createBody();
        gridPane.add(loadingPane, 0, 1); // row 1, still show the headers
        loadingPane.toFront();
    }

    private void createHeader() {
        GridTableView<S> tableView = getSkinnable();
        ObservableList<GridTableColumn<S, ?>> columns = tableView.getColumns();

        int numberOfColumns = columns.size();

        Region headerBackground = new Region();
        headerBackground.getStyleClass().addAll("column-header-background");
        GridPane.setColumnSpan(headerBackground, numberOfColumns);
        gridPane.add(headerBackground, 0, 0);

        GridPane.setColumnSpan(loadingPane, numberOfColumns);

        for (int i = 0; i < numberOfColumns; i++) {

            GridTableColumn<S, ?> column = columns.get(i);
            Node headerNode = column.getHeader();

            headerNode.getStyleClass().addAll("index-" + i, i % 2 == 0 ? "even" : "odd");

            if (numberOfColumns == 1) {
                headerNode.getStyleClass().add("only");
            } else {
                if (i == 0) {
                    headerNode.getStyleClass().add("first");
                } else if (i == numberOfColumns - 1) {
                    headerNode.getStyleClass().add("last");
                } else {
                    headerNode.getStyleClass().add("middle");
                }
            }

            gridPane.add(headerNode, i, 0);
        }
    }

    private void createBody() {
        GridTableView<S> tableView = getSkinnable();

        ObservableList<GridTableColumn<S, ?>> columns = tableView.getColumns();
        int numberOfColumns = columns.size();

        ObservableList<S> items = tableView.getItems();

        if (items.isEmpty()) {
            Node placeholder = tableView.getPlaceholder();
            if (placeholder != null) {
                gridPane.add(placeholder, 0, 1);
                GridPane.setColumnSpan(placeholder, numberOfColumns);
                GridPane.setHalignment(placeholder, HPos.CENTER);
                GridPane.setValignment(placeholder, VPos.CENTER);
                GridPane.setHgrow(placeholder, Priority.ALWAYS);
                GridPane.setVgrow(placeholder, Priority.ALWAYS);
                GridPane.setMargin(placeholder, new Insets(20));
            }
            return;
        }

        int numberOfRows = Math.max(items.size(), tableView.getMinNumberOfRows());
        GridPane.setRowSpan(loadingPane, numberOfRows);

        for (int row = 0; row < numberOfRows; row++) {

            Region rowBackground = new Region();
            rowBackground.getStyleClass().addAll("row-background", "index-" + row, row % 2 == 0 ? "even" : "odd");
            GridPane.setColumnSpan(rowBackground, numberOfColumns);
            gridPane.add(rowBackground, 0, row + 1);

            if (numberOfRows == 1) {
                rowBackground.getStyleClass().add("only");
            } else {
                if (row == 0) {
                    rowBackground.getStyleClass().add("first");
                } else if (row == numberOfRows - 1) {
                    rowBackground.getStyleClass().add("last");
                } else {
                    rowBackground.getStyleClass().add("middle");
                }
            }

            for (int col = 0; col < numberOfColumns; col++) {

                GridTableColumn<S, ?> column = columns.get(col);
                GridTableCell<S, ?> cellNode = column.createCell(tableView, row);
                cellNode.hoverProperty().addListener((obs, oldHover, newHover) -> {
                    rowBackground.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), newHover);
                });

                cellNode.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (event.getClickCount() == 2 &&
                            event.getButton() == javafx.scene.input.MouseButton.PRIMARY &&
                            !event.isConsumed() &&
                            event.isStillSincePress()) {
                        Consumer<S> onOpenItem = tableView.getOnOpenItem();
                        if (onOpenItem != null) {
                            onOpenItem.accept(cellNode.getRowItem());
                        }
                    }
                });

                if (numberOfColumns == 1) {
                    cellNode.getStyleClass().add("only");
                } else {
                    if (col == 0) {
                        cellNode.getStyleClass().add("first");
                    } else if (col == numberOfColumns - 1) {
                        cellNode.getStyleClass().add("last");
                    } else {
                        cellNode.getStyleClass().add("middle");
                    }
                    cellNode.getStyleClass().add("col-index-" + col);
                    if (row == 0) {
                        cellNode.getStyleClass().add("row-first");
                    } else if (row == numberOfRows - 1) {
                        cellNode.getStyleClass().add("row-last");
                    } else {
                        cellNode.getStyleClass().add("row-middle");
                    }
                    cellNode.getStyleClass().add("row-index-" + row);
                }

                gridPane.add(cellNode, col, row + 1);
            }
        }
    }
}
