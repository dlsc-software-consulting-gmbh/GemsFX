package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ArcProgressIndicator;
import com.dlsc.gemsfx.CircleProgressIndicator;
import com.dlsc.gemsfx.SemiCircleProgressIndicator;
import com.dlsc.gemsfx.util.EnumStringConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

import java.util.List;

/**
 * A demo for the abstract {@link ArcProgressIndicator} base class. The demo shows both
 * implementations that ship with GemsFX side by side and allows the user to play with the
 * properties that they inherit from the base class.
 */
public class ArcProgressIndicatorApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);

        CircleProgressIndicator circleIndicator = new CircleProgressIndicator(.35);
        SemiCircleProgressIndicator semiCircleIndicator = new SemiCircleProgressIndicator(.35);

        List<ArcProgressIndicator> indicators = List.of(circleIndicator, semiCircleIndicator);

        HBox indicatorBox = new HBox(40, createIndicatorBox("Circle", circleIndicator), createIndicatorBox("Semi-Circle", semiCircleIndicator));
        indicatorBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(indicatorBox, Priority.ALWAYS);

        Slider progressSlider = new Slider(0, 1, circleIndicator.getProgress());
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        progressSlider.valueProperty().addListener(it -> indicators.forEach(indicator -> indicator.setProgress(progressSlider.getValue())));

        CheckBox indeterminateBox = new CheckBox("Indeterminate");
        indeterminateBox.selectedProperty().addListener(it -> {
            boolean indeterminate = indeterminateBox.isSelected();
            progressSlider.setDisable(indeterminate);
            indicators.forEach(indicator -> indicator.setProgress(indeterminate ? ProgressIndicator.INDETERMINATE_PROGRESS : progressSlider.getValue()));
        });

        ComboBox<ArcProgressIndicator.StyleType> styleTypeBox = createComboBox(ArcProgressIndicator.StyleType.values(), circleIndicator.getStyleType());
        styleTypeBox.valueProperty().addListener(it -> indicators.forEach(indicator -> indicator.setStyleType(styleTypeBox.getValue())));

        ComboBox<ArcType> progressArcTypeBox = createComboBox(ArcType.values(), circleIndicator.getProgressArcType());
        progressArcTypeBox.valueProperty().addListener(it -> indicators.forEach(indicator -> indicator.setProgressArcType(progressArcTypeBox.getValue())));

        ComboBox<ArcType> trackArcTypeBox = createComboBox(ArcType.values(), circleIndicator.getTrackArcType());
        trackArcTypeBox.valueProperty().addListener(it -> indicators.forEach(indicator -> indicator.setTrackArcType(trackArcTypeBox.getValue())));

        VBox settingsBox = new VBox(10,
                new Label("Progress"), progressSlider, indeterminateBox,
                new Label("Style type"), styleTypeBox,
                new Label("Progress arc type"), progressArcTypeBox,
                new Label("Track arc type"), trackArcTypeBox);
        settingsBox.setMinWidth(Region.USE_PREF_SIZE);

        VBox root = new VBox(20, indicatorBox, new Separator(), settingsBox);
        root.setPadding(new Insets(20));

        stage.setTitle("Arc Progress Indicator");
        stage.setScene(new Scene(root, 520, 520));
        stage.show();
    }

    private VBox createIndicatorBox(String title, ArcProgressIndicator indicator) {
        indicator.setPrefSize(160, 160);

        VBox box = new VBox(10, indicator, new Label(title));
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private <T extends Enum<T>> ComboBox<T> createComboBox(T[] values, T initialValue) {
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(values);
        comboBox.setConverter(new EnumStringConverter<>());
        comboBox.setValue(initialValue);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        return comboBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
