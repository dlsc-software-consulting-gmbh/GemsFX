package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PopOver;
import com.dlsc.gemsfx.PopOver.ArrowLocation;
import com.dlsc.gemsfx.PopOver.CalendarPopOver;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.scenicview.ScenicView;

import java.time.LocalDate;
import java.util.Objects;

public class PopOverApp extends GemApplication {

    private final ObjectProperty<Double> radius = new SimpleObjectProperty<>(6d);

    private final ObjectProperty<Double> arrowSize = new SimpleObjectProperty<>(10d);

    private final ObjectProperty<Double> arrowIndent = new SimpleObjectProperty<>(12d);

    private final ObjectProperty<ArrowLocation> arrowLocation = new SimpleObjectProperty<>(ArrowLocation.LEFT_TOP);

    private CalendarPopOver calendarPopOver;

    @Override
    public void start(Stage stage) {
        super.start(stage);

        PopOver popOver = new PopOver();
        popOver.cornerRadiusProperty().bind(radius);
        popOver.arrowSizeProperty().bind(arrowSize);
        popOver.arrowIndentProperty().bind(arrowIndent);
        popOver.arrowLocationProperty().bind(arrowLocation);

        Button button = new Button("Show PopOver");
        button.setOnAction(evt -> {
            System.out.println(evt);
            popOver.show(button);
        });

        Button dateButton = new Button("Pick Date");
        dateButton.setOnAction(evt -> {
            if (calendarPopOver == null) {
                calendarPopOver = new CalendarPopOver();
                calendarPopOver.cornerRadiusProperty().bind(radius);
                calendarPopOver.arrowSizeProperty().bind(arrowSize);
                calendarPopOver.arrowIndentProperty().bind(arrowIndent);
                calendarPopOver.arrowLocationProperty().bind(arrowLocation);
                calendarPopOver.addEventHandler(WindowEvent.WINDOW_HIDING, evt1 -> {
                    LocalDate selectedDate = calendarPopOver.getCalendarView().getSelectionModel().getSelectedDate();
                    if (selectedDate != null) {
                        dateButton.setText(selectedDate.toString());
                    }
                });
            }
            calendarPopOver.show(dateButton);
        });

        VBox pane = new VBox(20, button, dateButton);
        pane.setAlignment(Pos.CENTER);
        VBox.setVgrow(pane, Priority.ALWAYS);

        ComboBox<ArrowLocation> arrowLocationBox = new ComboBox<>();
        arrowLocationBox.getItems().addAll(ArrowLocation.values());
        arrowLocationBox.valueProperty().bindBidirectional(arrowLocation);

        ComboBox<Double> radiusBox = new ComboBox<>();
        radiusBox.getItems().addAll(0.0, 6.0, 10.0, 15.0, 20.0, 32.0);
        radiusBox.valueProperty().bindBidirectional(radius);

        ComboBox<Double> arrowSizeBox = new ComboBox<>();
        arrowSizeBox.getItems().addAll(0.0, 5.0, 10.0, 15.0, 20.0, 32.0);
        arrowSizeBox.valueProperty().bindBidirectional(arrowSize);

        ComboBox<Double> arrowIndentBox = new ComboBox<>();
        arrowIndentBox.getItems().addAll( 0.0, 5.0, 12.0, 15.0);
        arrowIndentBox.valueProperty().bindBidirectional(arrowIndent);

        CheckBox autoHideBox = new CheckBox("Auto Hide");
        autoHideBox.selectedProperty().bindBidirectional(popOver.autoHideProperty());

        CheckBox detachableBox = new CheckBox("Detachable");
        detachableBox.selectedProperty().bindBidirectional(popOver.detachableProperty());

        Button sceneViewButton = new Button("ScenicView");
        sceneViewButton.setOnAction(evt -> ScenicView.show(pane.getScene()));

        HBox controls = new HBox(5, arrowLocationBox, new Separator(Orientation.VERTICAL),
                new Label("Radius:"), radiusBox,
                new Separator(Orientation.VERTICAL),
                new Label("Arrow Size:"), arrowSizeBox,
                new Separator(Orientation.VERTICAL),
                new Label("Arrow Indent:"), arrowIndentBox,
                new Spacer(),
                sceneViewButton,
                new Spacer(),
                detachableBox,
                autoHideBox);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(10, pane, new Separator(), controls);
        vBox.setPadding(new Insets(20));

        Scene scene = new Scene(vBox);
        scene.getStylesheets().add(Objects.requireNonNull(PopOverApp.class.getResource("popover-app.css")).toExternalForm());

        stage.setTitle("PopOver");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        CSSFX.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
