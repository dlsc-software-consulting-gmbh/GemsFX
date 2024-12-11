package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingListView;
import com.dlsc.gemsfx.SimplePagingListView;
import com.dlsc.gemsfx.TextView;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class TextViewWithPagingListViewApp extends Application {

    @Override
    public void start(Stage stage) {
        SimplePagingListView<String> listView = new SimplePagingListView<>();
        listView.setUsingScrollPane(false);
        listView.setPageSize(3);
        listView.setFillLastPage(false);

        ObservableList<String> data = FXCollections.observableArrayList();
        for (int i = 0; i < 12; i++) {
            data.add("Item " + i + "\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.");
        }

        listView.setItems(data);
        listView.setCellFactory(lv -> new ListCell<>() {

            private final TextView textView = new TextView();

            {
                setGraphic(textView);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                textView.visibleProperty().bind(itemProperty().isNotNull());
                setStyle("-fx-border-color: black; -fx-border-width: 0px 0px 1px 0px;");
            }

            @Override
            public Orientation getContentBias() {
                return Orientation.HORIZONTAL;
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    textView.setText(item);
                    textView.autosize();
                }
            }
        });

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(listView.getScene()));

        VBox vBox = new VBox(10);

        Button resize = new Button("Resize");
        resize.setOnAction(evt -> listView.getScene().getWindow().setHeight(vBox.prefHeight(vBox.getWidth())));

        HBox controls = new HBox(10, scenicView, resize);

        vBox.getChildren().setAll(listView, controls);
        vBox.setPadding(new Insets(20));

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Text View (Paging List View)");
        stage.show();

        Platform.runLater(stage::sizeToScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}