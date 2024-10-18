package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.TimeRangePicker;
import com.dlsc.gemsfx.demo.fake.SimpleControlPane;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TimeRangePickerApp extends Application {

    private TimeRangePicker timeRangePicker;

    @Override
    public void start(Stage primaryStage) throws Exception {
        timeRangePicker = new TimeRangePicker();
        timeRangePicker.setPrefWidth(200);
        timeRangePicker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        timeRangePicker.getSelectionModel().selectIndices(1, 2, 3);

        StackPane wrapper = new StackPane(timeRangePicker);
        wrapper.setStyle("-fx-background-color: white; -fx-padding: 10;");

        SplitPane splitPane = new SplitPane(wrapper, createControlPane());
        splitPane.setDividerPositions(0.7);

        primaryStage.setScene(new Scene(splitPane, 800, 600));
        primaryStage.setTitle("Hello TimeRangePicker");
        primaryStage.show();
    }

    private Node createControlPane() {
        ComboBox<SelectionMode> controlPane = new ComboBox<>();
        controlPane.getItems().addAll(SelectionMode.values());
        controlPane.valueProperty().bindBidirectional(timeRangePicker.getSelectionModel().selectionModeProperty());

        return new SimpleControlPane(
                "Time Range Picker",
                new SimpleControlPane.ControlItem("Selection Mode", controlPane)
        );
    }


    public static void main(String[] args) {
        launch(args);
    }
}
