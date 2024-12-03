package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EnhancedLabel;
import com.dlsc.gemsfx.PagingListView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.ArrayList;
import java.util.List;

public class PagingListViewApp extends Application {

    @Override
    public void start(Stage stage) {
        PagingListView<String> pagingListView = new PagingListView<>();
        pagingListView.setPrefWidth(400);
        pagingListView.setTotalItemCount(30);
        pagingListView.setPageSize(10);
        pagingListView.setLoader(lv -> {
            List<String> data = new ArrayList<>();
            int offset = lv.getPage() * lv.getPageSize();
            for (int i = 0; i < lv.getPageSize(); i++) {
                data.add("Item " + (offset + i));
            }
            return data;
        });

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        VBox box = new VBox(20, pagingListView, scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);
        stage.setTitle("Paging List View");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
