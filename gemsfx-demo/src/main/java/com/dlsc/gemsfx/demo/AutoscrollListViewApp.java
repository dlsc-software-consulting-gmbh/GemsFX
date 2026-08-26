package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AutoscrollListView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AutoscrollListViewApp extends GemApplication {

    private static final DataFormat ITEM_FORMAT = new DataFormat("gemsfx/autoscroll-list-view-item");

    // the item that is currently being dragged, together with the list it was dragged out of
    private String draggedItem;
    private ListView<String> dragSource;

    @Override
    public void start(Stage stage) {
        super.start(stage);

        AutoscrollListView<String> leftList = createListView("Item", 100);
        AutoscrollListView<String> rightList = createListView("Task", 100);

        HBox listBox = new HBox(20, createColumn("Drag items out of this list ...", leftList), createColumn("... and drop them into this one", rightList));
        listBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(listBox, Priority.ALWAYS);

        Label descriptionLabel = new Label("""
                The autoscroll list view starts scrolling all by itself as soon as the mouse cursor \
                comes close to the top or the bottom edge of the list while a drag and drop operation \
                is in progress. Grab an item and move it towards one of the edges to see it happen.""");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        VBox root = new VBox(20, descriptionLabel, listBox);
        root.setPadding(new Insets(20));

        stage.setTitle("Autoscroll List View");
        stage.setScene(new Scene(root, 700, 500));
        stage.show();
    }

    private VBox createColumn(String title, ListView<String> listView) {
        Label titleLabel = new Label(title);
        VBox.setVgrow(listView, Priority.ALWAYS);

        VBox box = new VBox(10, titleLabel, listView);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private AutoscrollListView<String> createListView(String prefix, int count) {
        AutoscrollListView<String> listView = new AutoscrollListView<>();

        for (int i = 1; i <= count; i++) {
            listView.getItems().add(prefix + " " + i);
        }

        listView.setCellFactory(view -> new DragCell());

        // dropping onto the empty area below the last cell appends the item
        listView.setOnDragOver(evt -> {
            if (evt.getDragboard().hasContent(ITEM_FORMAT)) {
                evt.acceptTransferModes(TransferMode.MOVE);
            }
            evt.consume();
        });

        listView.setOnDragDropped(evt -> {
            if (draggedItem != null) {
                moveItem(listView, listView.getItems().size());
                evt.setDropCompleted(true);
            }
            evt.consume();
        });

        return listView;
    }

    /**
     * Moves the item that is currently being dragged from its source list to the given target
     * list, inserting it at the given index.
     */
    private void moveItem(ListView<String> target, int index) {
        String item = draggedItem;

        if (dragSource != null) {
            dragSource.getItems().remove(item);
        }

        int insertIndex = Math.min(index, target.getItems().size());
        target.getItems().add(insertIndex, item);
        target.getSelectionModel().select(item);

        draggedItem = null;
        dragSource = null;
    }

    /**
     * A list cell that can be dragged to another position or to the other list view.
     */
    private class DragCell extends ListCell<String> {

        public DragCell() {
            setOnDragDetected(evt -> {
                if (isEmpty() || getItem() == null) {
                    return;
                }

                draggedItem = getItem();
                dragSource = getListView();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(snapshot(null, null));

                ClipboardContent content = new ClipboardContent();
                content.put(ITEM_FORMAT, getItem());
                content.putString(getItem());
                dragboard.setContent(content);

                evt.consume();
            });

            setOnDragOver(evt -> {
                if (evt.getDragboard().hasContent(ITEM_FORMAT) && !isEmpty()) {
                    evt.acceptTransferModes(TransferMode.MOVE);
                }
                evt.consume();
            });

            setOnDragDropped(evt -> {
                if (draggedItem != null && !isEmpty()) {
                    moveItem(getListView(), getIndex());
                    evt.setDropCompleted(true);
                }
                evt.consume();
            });

            setOnDragDone(evt -> {
                draggedItem = null;
                dragSource = null;
                evt.consume();
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
