package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingListView;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.ArrayList;
import java.util.List;

public class PagingListViewApp extends Application {

    private final BooleanProperty simulateDelayProperty = new SimpleBooleanProperty(false);

    private final BooleanProperty simulateNoData = new SimpleBooleanProperty(false);

    private final IntegerProperty count = new SimpleIntegerProperty(12);

    @Override
    public void start(Stage stage) {
        PagingListView<String> pagingListView = new PagingListView<>();
        pagingListView.setPrefWidth(600);
        pagingListView.totalItemCountProperty().bind(Bindings.createIntegerBinding(() -> simulateNoData.get() ? 0 : count.get(), simulateNoData, count));
        pagingListView.setPageSize(5);
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

        simulateNoData.addListener(it -> pagingListView.reload());

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        CheckBox fillBox = new CheckBox("Fill last page");
        fillBox.selectedProperty().bindBidirectional(pagingListView.fillLastPageProperty());

        CheckBox simulateDelay = new CheckBox("Simulate delay");
        simulateDelay.selectedProperty().bindBidirectional(simulateDelayProperty);

        ComboBox<Side> location = new ComboBox<>();
        location.getItems().addAll(Side.TOP, Side.BOTTOM);
        location.valueProperty().bindBidirectional(pagingListView.pagingControlsLocationProperty());

        Button clearSetData = new Button("Clear Set Data");
        clearSetData.setOnAction(evt -> simulateNoData.set(!simulateNoData.get()));

        Button reduceItemCount = new Button("Reduce Count");
        reduceItemCount.setOnAction(evt -> count.set(count.get() - 1));

        Button increaseItemCount = new Button("Increase Count");
        increaseItemCount.setOnAction(evt -> count.set(count.get() + 1));

        HBox settingsBox = new HBox(10, fillBox, simulateDelay, new Label("Location"), location, clearSetData, reduceItemCount, increaseItemCount);
        settingsBox.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(20, pagingListView, settingsBox, new PagingControlsSettingsView(pagingListView), scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

        stage.setTitle("Paging List View");
        stage.setScene(scene);
        stage.centerOnScreen();

        StageManager.install(stage, "product.list.view");
        stage.show();

        Platform.runLater(stage::sizeToScene);
    }

    public static void main(String[] args) {
        launch();
    }
}
