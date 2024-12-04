package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SimplePagingListView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class SimplePagingListViewApp extends Application {

    @Override
    public void start(Stage stage) {
        SimplePagingListView<String> pagingListView = new SimplePagingListView<>();
        pagingListView.setPrefWidth(400);
        for (int i = 0; i < 200; i++) {
            pagingListView.getItems().add("Item " + (i + 1));
        }

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
