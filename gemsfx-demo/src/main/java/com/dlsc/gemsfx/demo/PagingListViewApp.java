package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EnhancedLabel;
import com.dlsc.gemsfx.PagingListView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
        pagingListView.setTotalItemCount(305);
        pagingListView.setPageSize(15);
        pagingListView.setLoader(lv -> {
            if (Math.random() > .75) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            List<String> data = new ArrayList<>();
            int offset = lv.getPage() * lv.getPageSize();
            for (int i = 0; i < lv.getPageSize(); i++) {
                int index = offset + i + 1;
                if (index <= pagingListView.getTotalItemCount()) {
                    data.add("Item " + (offset + i + 1));
                } else {
                    break;
                }
            }
            return data;
        });

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        CheckBox fillBox = new CheckBox("Fill last page");
        fillBox.selectedProperty().bindBidirectional(pagingListView.fillLastPageProperty());

        VBox box = new VBox(20, pagingListView, fillBox, new PagingControlsSettingsView(pagingListView), scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

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
