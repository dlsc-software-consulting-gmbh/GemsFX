package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PopOver;
import com.dlsc.gemsfx.PopOver.ArrowLocation;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.Objects;

public class PopOverApp extends GemApplication {

    private final DoubleProperty radius = new SimpleDoubleProperty(6);

    private final DoubleProperty arrowSize = new SimpleDoubleProperty(10);

    private final ObjectProperty<ArrowLocation> arrowLocation = new SimpleObjectProperty<>(ArrowLocation.LEFT_TOP);

    @Override
    public void start(Stage stage) {
        super.start(stage);

        PopOver popOver = new PopOver();
        popOver.cornerRadiusProperty().bind(radius);
        popOver.arrowSizeProperty().bind(arrowSize);
        popOver.arrowLocationProperty().bind(arrowLocation);

        Button button = new Button("Show PopOver");
        button.setOnAction(evt -> popOver.show(button));

        Button dateButton = new Button("Pick Date");
        dateButton.setOnAction(evt -> {
            PopOver.CalendarPopOver pop = PopOver.showCalendarPopOver(dateButton);
            pop.cornerRadiusProperty().bind(radius);
            pop.arrowSizeProperty().bind(arrowSize);
            pop.arrowLocationProperty().bind(arrowLocation);
        });

        Button timeButton = new Button("Pick Time");
        timeButton.setOnAction(evt -> {
            PopOver.TimePopOver pop = PopOver.showTimePopOver(timeButton);
            pop.cornerRadiusProperty().bind(radius);
            pop.arrowSizeProperty().bind(arrowSize);
            pop.arrowLocationProperty().bind(arrowLocation);
        });

        VBox pane = new VBox(20, dateButton);
        pane.setAlignment(Pos.CENTER);
        VBox.setVgrow(pane, Priority.ALWAYS);

        ComboBox<ArrowLocation> arrowLocationBox = new ComboBox<>();
        arrowLocationBox.getItems().addAll(ArrowLocation.values());
        arrowLocationBox.valueProperty().bindBidirectional(arrowLocation);

        Slider radiusSlider = new Slider(0, 32, 0);
        radiusSlider.valueProperty().bindBidirectional(radius);

        Label radiusLabel = new Label();
        radiusLabel.textProperty().bind(radiusSlider.valueProperty().asString("%.0f"));

        Slider arrowSlider = new Slider(0, 32, 0);
        arrowSlider.valueProperty().bindBidirectional(arrowSize);

        Label arrowLabel = new Label();
        arrowLabel.textProperty().bind(arrowSlider.valueProperty().asString("%.0f"));

        HBox controls = new HBox(5, arrowLocationBox, new Separator(Orientation.VERTICAL),
                new Label("Radius:"), radiusSlider, radiusLabel,
                new Separator(Orientation.VERTICAL),
                new Label("Arrow Size:"), arrowSlider, arrowLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(10, pane, new Separator(), controls);
        vBox.setPadding(new Insets(20));
        StageManager.install(stage, "popover.test.demo", 800, 500);

        Scene scene = new Scene(vBox);
        scene.getStylesheets().add(Objects.requireNonNull(PopOverApp.class.getResource("popover-app.css")).toExternalForm());

        stage.setTitle("PopOver");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.centerOnScreen();
        stage.show();

        ScenicView.show(scene);
        CSSFX.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
