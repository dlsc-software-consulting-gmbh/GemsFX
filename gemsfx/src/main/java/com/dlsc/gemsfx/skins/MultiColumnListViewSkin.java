package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.util.Callback;

public class MultiColumnListViewSkin<T> extends SkinBase<MultiColumnListView<T>> {

    private final GridPane gridPane = new GridPane();

    public MultiColumnListViewSkin(MultiColumnListView<T> view) {
        super(view);

        InvalidationListener updateListener = (Observable it) -> updateView();
        view.columnsProperty().addListener(updateListener);
        view.showHeadersProperty().addListener(updateListener);
        view.separatorFactoryProperty().addListener(updateListener);
        view.listViewFactoryProperty().addListener(updateListener);
        updateView();

        gridPane.getStyleClass().add("grid-pane");

        getChildren().setAll(gridPane);
    }

    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.NEVER);
        row1.setFillHeight(true);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        row2.setFillHeight(true);

        MultiColumnListView<T> view = getSkinnable();

        if (view.isShowHeaders()) {
            gridPane.getRowConstraints().setAll(row1, row2);
        } else {
            gridPane.getRowConstraints().setAll(row2);
        }

        ObservableList<ListViewColumn<T>> columns = view.getColumns();
        int numberOfColumns = columns.size();

        Callback<Integer, Node> separatorFactory = view.getSeparatorFactory();

        for (int i = 0; i < numberOfColumns; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setFillWidth(true);
            columnConstraints.setPrefWidth(1); // make sure all columns have the same width
            gridPane.getColumnConstraints().add(columnConstraints);

            if (separatorFactory != null && i < numberOfColumns - 1) {
                columnConstraints = new ColumnConstraints();
                columnConstraints.setHgrow(Priority.NEVER);
                columnConstraints.setHalignment(HPos.CENTER);
                gridPane.getColumnConstraints().add(columnConstraints);
            }
        }

        int columnIndex = 0;
        int col = 0;

        do {
            ListViewColumn<T> column = columns.get(columnIndex);

            Node header = column.getHeader();
            if (header instanceof Region) {
                ((Region) header).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }

            ListView<T> listView = view.getListViewFactory().call(view);
            if (listView.getPlaceholder() == null) {
                createPlaceholder(listView);
            }

            initPlaceholder(listView, listView.getPlaceholder());
            listView.placeholderProperty().addListener((obs, oldPlaceholder, newPlaceholder) -> {
                if (newPlaceholder == null) {
                    createPlaceholder(listView);
                }
                initPlaceholder(listView, listView.getPlaceholder());
            });

            listView.itemsProperty().bind(column.itemsProperty());

            listView.cellFactoryProperty().bind(Bindings.createObjectBinding(() -> lv -> {
                Callback<MultiColumnListView<T>, ColumnListCell<T>> cellFactory = view.getCellFactory();
                return cellFactory.call(view);
            }, view.cellFactoryProperty()));

            if (view.isShowHeaders()) {
                gridPane.add(header, col, 0);
                gridPane.add(listView, col, 1);
            } else {
                gridPane.add(listView, col, 0);
            }

            if (separatorFactory != null && columnIndex < numberOfColumns - 1) {
                Node separator = separatorFactory.call(columnIndex);
                col++;
                gridPane.add(separator, col, 0);
                GridPane.setFillHeight(separator, true);
                if (view.isShowHeaders()) {
                    GridPane.setRowSpan(separator, 2);
                }
            }

            col++;
            columnIndex++;
        } while (columnIndex < numberOfColumns);
    }

    private void createPlaceholder(ListView<T> listView) {
        Label label = new Label();
        label.getStyleClass().add("placeholder");
        label.setAlignment(Pos.CENTER);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        listView.setPlaceholder(label);
    }

    private void initPlaceholder(ListView listView, Node placeholder) {
        placeholder.setOnDragOver(event -> {
            event.consume();
            event.acceptTransferModes(TransferMode.MOVE);
        });

        placeholder.setOnDragDropped(event -> {
            listView.getItems().add(getSkinnable().getDraggedItem());
            event.setDropCompleted(true);
            event.consume();
        });

        placeholder.setOnDragEntered(evt -> placeholder.pseudoClassStateChanged(PseudoClass.getPseudoClass("drag-over"), true));
        placeholder.setOnDragExited(evt -> placeholder.pseudoClassStateChanged(PseudoClass.getPseudoClass("drag-over"), false));
    }
}
