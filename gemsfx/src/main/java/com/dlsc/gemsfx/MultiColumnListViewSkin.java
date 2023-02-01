package com.dlsc.gemsfx;

import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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

        view.columnsProperty().addListener((Observable it) -> updateView());
        updateView();

        gridPane.getStyleClass().add("grid-pane");

        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.NEVER);
        row1.setFillHeight(true);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        row2.setFillHeight(true);

        gridPane.getRowConstraints().setAll(row1, row2);

        getChildren().setAll(gridPane);
    }

    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        MultiColumnListView<T> view = getSkinnable();

        ObservableList<ListViewColumn<T>> columns = view.getColumns();
        int numberOfColumns = columns.size();

        for (int i = 0; i < numberOfColumns; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100d / (double) numberOfColumns);
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setFillWidth(true);
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i < columns.size(); i++) {
            ListViewColumn<T> column = columns.get(i);

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

            gridPane.add(header, i, 0);
            gridPane.add(listView, i, 1);
        }
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
            System.out.println("drag over on placeholder");
            event.consume();
            event.acceptTransferModes(TransferMode.MOVE);
        });

        placeholder.setOnDragDropped(event -> {
            System.out.println("drag dropped");
            listView.getItems().add(getSkinnable().getDraggedItem());
            event.setDropCompleted(true);
            event.consume();
        });

        placeholder.setOnDragEntered(evt -> placeholder.pseudoClassStateChanged(PseudoClass.getPseudoClass("drag-over"), true));
        placeholder.setOnDragExited(evt -> placeholder.pseudoClassStateChanged(PseudoClass.getPseudoClass("drag-over"), false));
    }
}
