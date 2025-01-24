package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.paging.SimplePagingListView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class SimplePagingListViewApp extends Application {

    @Override
    public void start(Stage stage) {
        SimplePagingListView<String> pagingListView = new SimplePagingListView<>();
        pagingListView.setPrefWidth(400);
        for (int i = 0; i < 100; i++) {
            pagingListView.getItems().add("Item " + (i + 1));
        }

        pagingListView.setCellFactory(lv -> new ListCell<>() {
            {
                setOnContextMenuRequested(request -> {
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(evt -> pagingListView.getItems().remove(getItem()));

                    MenuItem duplicateItem = new MenuItem("Duplicate");
                    duplicateItem.setOnAction(evt -> {
                        int index = pagingListView.getItems().indexOf(getItem());
                        pagingListView.getItems().add(index + 1, getItem() + " (copy)");
                    });

                    MenuItem replaceItem = new MenuItem("Replace");
                    replaceItem.setOnAction(evt -> {
                        int index = pagingListView.getItems().indexOf(getItem());
                        pagingListView.getItems().set(index, getItem() + " (replacement)");
                    });

                    MenuItem showItem = new MenuItem("Show item at current index + 10");
                    showItem.setOnAction(evt -> {
                        int index = pagingListView.getItems().indexOf(getItem());
                        pagingListView.show(pagingListView.getItems().get(index + 10));
                        pagingListView.getSelectionModel().select(pagingListView.getItems().get(index + 10));
                    });

                    ContextMenu contextMenu = new ContextMenu(deleteItem, duplicateItem, replaceItem, showItem);
                    contextMenu.show(lv, request.getScreenX(), request.getScreenY());
                });

            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null) {
                    setText(item);
                } else {
                    setText("");
                }
            }
        });

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        VBox box = new VBox(20, pagingListView, new PagingControlsSettingsView(pagingListView), scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

        stage.setTitle("Simple Paging List View");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
