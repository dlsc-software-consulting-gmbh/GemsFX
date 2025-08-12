package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.paging.PagingControlBase;
import com.dlsc.gemsfx.paging.PagingListView;
import com.dlsc.gemsfx.paging.PagingLoadResponse;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.ArrayList;
import java.util.List;

public class PagingListViewApp extends GemApplication {

    private final BooleanProperty simulateDelayProperty = new SimpleBooleanProperty(false);

    private final BooleanProperty simulateNoData = new SimpleBooleanProperty(false);

    private final IntegerProperty count = new SimpleIntegerProperty(5);

    @Override
    public void start(Stage stage) { super.start(stage);
        PagingListView<String> pagingListView = new PagingListView<>();
        count.addListener(it -> pagingListView.reload());

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
                if (index <= count.get()) {
                    data.add("Item " + (offset + i + 1));
                } else {
                    break;
                }
            }
            return new PagingLoadResponse<>(data, simulateNoData.get() ? 0 : count.get());
        });
        pagingListView.setPrefWidth(600);
        pagingListView.setPageSize(5);
        pagingListView.setMessageLabelStrategy(PagingControlBase.MessageLabelStrategy.SHOW_WHEN_NEEDED);
        pagingListView.totalItemCountProperty().subscribe(count -> System.out.println("total item count: " + count));
        simulateNoData.addListener(it -> pagingListView.reload());

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        CheckBox fillBox = new CheckBox("Fill last page");
        fillBox.selectedProperty().bindBidirectional(pagingListView.fillLastPageProperty());

        CheckBox simulateDelay = new CheckBox("Simulate delay");
        simulateDelay.selectedProperty().bindBidirectional(simulateDelayProperty);

        CheckBox showPagingControls = new CheckBox("Show paging controls");
        showPagingControls.selectedProperty().bindBidirectional(pagingListView.showPagingControlsProperty());

        ComboBox<Side> location = new ComboBox<>();
        location.getItems().addAll(Side.TOP, Side.BOTTOM);
        location.valueProperty().bindBidirectional(pagingListView.pagingControlsLocationProperty());

        Button clearSetData = new Button("Clear Set Data");
        clearSetData.setOnAction(evt -> simulateNoData.set(!simulateNoData.get()));

        Button reduceItemCount = new Button("Reduce Count");
        reduceItemCount.setOnAction(evt -> count.set(count.get() - 1));

        Button increaseItemCount = new Button("Increase Count");
        increaseItemCount.setOnAction(evt -> count.set(count.get() + 1));

        CheckBox useScrollPane = new CheckBox("Use Scroll Pane");
        useScrollPane.selectedProperty().bindBidirectional(pagingListView.usingScrollPaneProperty());

        HBox settingsBox = new HBox(10, fillBox, simulateDelay, showPagingControls, useScrollPane, location, clearSetData, reduceItemCount, increaseItemCount);
        settingsBox.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(20, pagingListView, settingsBox, new PagingControlsSettingsView(pagingListView), scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);

        scene.focusOwnerProperty().addListener(it -> System.out.println("focus owner: " + scene.getFocusOwner()));

        stage.setTitle("Paging List View");
        stage.setScene(scene);
        stage.centerOnScreen();

        StageManager.install(stage, "product.list.view");
        stage.show();
        stage.sizeToScene();
    }

    public static void main(String[] args) {
        launch();
    }
}
