package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ArcProgressIndicator;
import com.dlsc.gemsfx.ArcProgressIndicator.StyleType;
import com.dlsc.gemsfx.CircleProgressIndicator;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.scenicview.ScenicView;

import java.util.Objects;

public class CircleProgressIndicatorApp extends GemApplication {

    private StringConverter<Double> customConverter;

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        CircleProgressIndicator progressIndicator = new CircleProgressIndicator();
        delayAutoUpdateProgress(progressIndicator);

        // styles
        ComboBox<StyleType> styleComboBox = new ComboBox<>();
        styleComboBox.getItems().addAll(StyleType.values());
        styleComboBox.valueProperty().bindBidirectional(progressIndicator.styleTypeProperty());
        styleComboBox.setMaxWidth(Double.MAX_VALUE);

        // start Angle
        Label startAngleLabel = new Label("Start Angle");
        Slider startAngleSlider = new Slider(0, 360, 90);
        progressIndicator.startAngleProperty().bind(startAngleSlider.valueProperty());
        Label startAngleValue = new Label();
        startAngleValue.setPrefWidth(30);
        startAngleValue.textProperty().bind(Bindings.format("%.0f", startAngleSlider.valueProperty()));
        HBox startAngleBox = new HBox(5, startAngleLabel, startAngleSlider, startAngleValue);
        startAngleBox.setAlignment(Pos.CENTER_LEFT);

        // string converter
        StringConverter<Double> defaultConvert = progressIndicator.getConverter();
        CheckBox customConverterBox = new CheckBox("Custom Converter");
        customConverterBox.selectedProperty().addListener((observable, oldValue, newValue) -> progressIndicator.setConverter(newValue ? getCustomConverter() : defaultConvert));
        customConverterBox.setSelected(true);

        // layout
        StackPane indicatorWrapper = new StackPane(progressIndicator);
        indicatorWrapper.getStyleClass().add("indicator-wrapper");
        VBox.setVgrow(indicatorWrapper, Priority.ALWAYS);

        Button scenicView = new Button("Scenic View");
        scenicView.setMaxWidth(Double.MAX_VALUE);
        scenicView.setOnAction(e -> ScenicView.show(scenicView.getScene()));

        VBox bottom = new VBox(10, customConverterBox, startAngleBox, styleComboBox, scenicView);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setMaxWidth(Region.USE_PREF_SIZE);

        VBox containerBox = new VBox(20);
        containerBox.getStyleClass().add("container-box");
        if (Boolean.getBoolean("atlantafx")) {
            containerBox.setStyle("-fx-background-color: -color-bg-default;");
        }
        containerBox.setPadding(new Insets(20));
        containerBox.setAlignment(Pos.CENTER);
        containerBox.getChildren().addAll(indicatorWrapper, new Separator(), bottom);

        Scene scene = new Scene(containerBox, 330, 390);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Circle Progress Indicator Demo");
        StageManager.install(primaryStage, "circle.progress.indicator.app");

        primaryStage.show();
    }

    private void delayAutoUpdateProgress(CircleProgressIndicator graphicIndicator) {
        Service<Void> service = new Service<>() {
            @Override
            protected javafx.concurrent.Task<Void> createTask() {
                return new javafx.concurrent.Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        for (int i = 0; i < 3000; i++) {
                            Thread.sleep(4500);
                            for (int j = 0; j <= 100; j++) {
                                updateProgress(j, 100);
                                Thread.sleep(50);
                            }
                            Thread.sleep(2000);
                            Platform.runLater(() -> updateProgress(-1, 100));
                        }
                        return null;
                    }
                };
            }
        };
        graphicIndicator.progressProperty().bind(service.progressProperty());
        service.start();
    }

    private StringConverter<Double> getCustomConverter() {
        if (customConverter == null) {
            customConverter = new StringConverter<>() {
                @Override
                public String toString(Double progress) {
                    if (progress == null || progress < 0.0) {
                        return "Connecting";
                    }
                    double percentage = progress * 100;
                    if (progress.intValue() == 1) {
                        return "Download Complete";
                    }
                    return String.format("Downloading %.0f%%", percentage);
                }

                @Override
                public Double fromString(String string) {
                    return null;
                }
            };
        }
        return customConverter;
    }

        @Override
    public String getDescription() {
        return """
                ### CircleProgressIndicator
                
                CircleProgressIndicator is a visual control used to indicate the progress of a task.
                It represents progress in a circular form, with options to show determinate or indeterminate states.
                
                In a determinate state, the circle fills up based on the progress value, which ranges from 0.0 to 1.0,
                where 0.0 indicates no progress and 1.0 indicates completion.
                
                In an indeterminate state, the circle shows a cyclic animation, indicating that progress is ongoing
                but the exact status is unknown. This state is useful for tasks where the progress cannot be determined.
                
                The control also supports displaying a text or graphic inside the circle to provide additional
                information or visual feedback to the user.
                
                Usage examples include file downloads, file transfers, or any long-running tasks where
                visual feedback on progress is beneficial.
                
                The CircleProgressIndicator extends ArcProgressIndicator and adds a start angle property
                that defines the starting angle of the arc used to display the progress. The default start angle is 90 degrees,
                which corresponds to the top of the circle.
                
                **Pseudo class:** Beyond the **inherited** , **indeterminate** and determinate pseudo-classes
                from ProgressIndicator, CircleProgressIndicator introduces a **completed** pseudo-class.
                This pseudo-class can be used in CSS to apply custom styles when the progress reaches 1.0 (100%).
                
                **Tips:** If you prefer not to instantiate the animation object during initialization,
                pass a 0.0 as the initial progress. This setup indicates no progress but avoids entering
                the indeterminate state, which would otherwise instantiate and start the animation.
                
                Usage examples:
                ```
                
                    // Initializes with no progress and no animation.
                    CircleProgressIndicator progressIndicator = new CircleProgressIndicator(0.0);
                
                ```
                """;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
