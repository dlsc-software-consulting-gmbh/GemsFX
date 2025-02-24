package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ArcProgressIndicator;
import com.dlsc.gemsfx.SemiCircleProgressIndicator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class SemiCircleProgressIndicatorApp extends GemApplication {

    private StringConverter<Double> customConverter;

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        SemiCircleProgressIndicator progressIndicator = new SemiCircleProgressIndicator();
        delayAutoUpdateProgress(progressIndicator);

        // styles
        ComboBox<ArcProgressIndicator.StyleType> styleComboBox = new ComboBox<>();
        styleComboBox.getItems().addAll(ArcProgressIndicator.StyleType.values());
        styleComboBox.valueProperty().bindBidirectional(progressIndicator.styleTypeProperty());
        styleComboBox.setMaxWidth(Double.MAX_VALUE);

        // string converter
        StringConverter<Double> defaultConvert = progressIndicator.getConverter();
        CheckBox customConverterBox = new CheckBox("Custom Converter");
        customConverterBox.selectedProperty().addListener((observable, oldValue, newValue) -> progressIndicator.setConverter(newValue ? getCustomConverter() : defaultConvert));
        customConverterBox.setSelected(true);

        // layout
        StackPane indicatorWrapper = new StackPane(progressIndicator);
        indicatorWrapper.getStyleClass().add("indicator-wrapper");
        VBox.setVgrow(indicatorWrapper, Priority.ALWAYS);

        VBox bottom = new VBox(10, styleComboBox, customConverterBox);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setMaxWidth(Region.USE_PREF_SIZE);

        VBox containerBox = new VBox(20);
        containerBox.getStyleClass().add("container-box");
        containerBox.setPadding(new Insets(20));
        containerBox.setAlignment(Pos.CENTER);
        containerBox.getChildren().addAll(indicatorWrapper, new Separator(), bottom);

        Scene scene = new Scene(containerBox, 300, 390);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Semi Circle Progress Indicator");
        primaryStage.show();
    }

    private void delayAutoUpdateProgress(SemiCircleProgressIndicator graphicIndicator) {
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

    public static void main(String[] args) {
        launch(args);
    }

}
