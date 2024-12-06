package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingListView;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.ArrayList;
import java.util.List;

public class PagingListViewApp extends Application {

    private final BooleanProperty simulateDelayProperty = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage stage) {
        PagingListView<String> pagingListView = new PagingListView<>();
        pagingListView.setPrefWidth(400);
        pagingListView.setTotalItemCount(205);
        pagingListView.setPageSize(10);
        pagingListView.setLoader(loadRequest -> {
            if (simulateDelayProperty.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            List<String> data = new ArrayList<>();
            int offset = loadRequest.getPage() * loadRequest.getPageSize();
            for (int i = 0; i < loadRequest.getPageSize(); i++) {
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

        CheckBox simulateDelay = new CheckBox("Simulate delay");
        simulateDelay.selectedProperty().bindBidirectional(simulateDelayProperty);

        HBox settingsBox = new HBox(10, fillBox, simulateDelay);

        VBox box = new VBox(20, pagingListView, settingsBox, new PagingControlsSettingsView(pagingListView), scenicView);
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
