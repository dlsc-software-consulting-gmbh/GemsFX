package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.StripView;
import com.dlsc.gemsfx.StripView.StripCell;
import com.dlsc.gemsfx.demo.fake.WeatherCondition;
import com.dlsc.gemsfx.demo.fake.WeatherData;
import com.dlsc.gemsfx.demo.fake.WeatherSummaryPane;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Objects;

public class StripViewApp extends Application {

    @Override
    public void start(Stage stage) {
        StripView<String> textView = new StripView<>();
        textView.setMinHeight(Region.USE_PREF_SIZE);
        textView.setFadingSize(200);
        textView.getStyleClass().add("string-demo");

        for (int i = 1; i < 30; i++) {
            textView.getItems().add("Item " + i);
        }

        StripView<WeatherData> weatherView = new StripView<>();
        weatherView.setMinHeight(Region.USE_PREF_SIZE);
        weatherView.getStyleClass().add("weather-demo");
        weatherView.setFadingSize(200);
        weatherView.setCellFactory(strip -> new StripCell<>() {
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                WeatherSummaryPane summaryPane = new WeatherSummaryPane();
                summaryPane.setOnMouseClicked(evt -> weatherView.setSelectedItem(getItem()));
                summaryPane.weatherSummaryProperty().bind(itemProperty());
                setGraphic(summaryPane);
            }
        });

        weatherView.getItems().add(createWeatherData(1, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(2, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(3, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(4, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(5, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(6, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(7, WeatherCondition.RAINY));
        weatherView.getItems().add(createWeatherData(8, WeatherCondition.STORMY));
        weatherView.getItems().add(createWeatherData(9, WeatherCondition.STORMY));
        weatherView.getItems().add(createWeatherData(10, WeatherCondition.RAINY));
        weatherView.getItems().add(createWeatherData(11, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(12, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(13, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(14, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(15, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(16, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(17, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(18, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(19, WeatherCondition.SUNNY));
        weatherView.getItems().add(createWeatherData(20, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(21, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(22, WeatherCondition.RAINY));
        weatherView.getItems().add(createWeatherData(23, WeatherCondition.RAINY));
        weatherView.getItems().add(createWeatherData(24, WeatherCondition.RAINY));
        weatherView.getItems().add(createWeatherData(25, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(26, WeatherCondition.PARTIALLY_CLOUDY));
        weatherView.getItems().add(createWeatherData(27, WeatherCondition.CLOUDY));
        weatherView.getItems().add(createWeatherData(28, WeatherCondition.SNOWY));
        weatherView.getItems().add(createWeatherData(29, WeatherCondition.SNOWY));
        weatherView.getItems().add(createWeatherData(30, WeatherCondition.SNOWY));
        weatherView.getItems().add(createWeatherData(31, WeatherCondition.SUNNY));

        Label title1 = new Label("String Items");
        Label title2 = new Label("Weather Info");

        title1.getStyleClass().add("title");
        title2.getStyleClass().add("title");

        Slider slider = new Slider();
        slider.setValue(weatherView.getFadingSize());
        slider.maxProperty().bind(weatherView.widthProperty().divide(2));
        slider.valueProperty().bindBidirectional(weatherView.fadingSizeProperty());
        slider.valueProperty().bindBidirectional(textView.fadingSizeProperty());
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1);

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(() -> Double.toString(slider.getValue()), slider.valueProperty()));

        CheckBox centerCheckBox = new CheckBox("Always center");
        centerCheckBox.setTooltip(new Tooltip("Always scroll newly selected items into the center"));
        centerCheckBox.selectedProperty().bindBidirectional(textView.alwaysCenterProperty());
        centerCheckBox.selectedProperty().bindBidirectional(weatherView.alwaysCenterProperty());

        CheckBox autoScrollingCheckBox = new CheckBox("Auto scrolling");
        autoScrollingCheckBox.setTooltip(new Tooltip("Automatically scroll newly selected items into the un-faded area (if possible)"));
        autoScrollingCheckBox.selectedProperty().bindBidirectional(textView.autoScrollingProperty());
        autoScrollingCheckBox.selectedProperty().bindBidirectional(weatherView.autoScrollingProperty());

        CheckBox animateScrollingCheckBox = new CheckBox("Animate scrolling");
        animateScrollingCheckBox.setTooltip(new Tooltip("Use animation when scrolling to the newly selected item"));
        animateScrollingCheckBox.selectedProperty().bindBidirectional(textView.animateScrollingProperty());
        animateScrollingCheckBox.selectedProperty().bindBidirectional(weatherView.animateScrollingProperty());

        HBox box = new HBox(10, new Label("Fading size"), slider, valueLabel, centerCheckBox, autoScrollingCheckBox, animateScrollingCheckBox);
        box.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(box, new Insets(50, 0, 0, 0));

        VBox vBox = new VBox(10, title1, textView, title2, weatherView, box);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.TOP_LEFT);

        Scene scene = new Scene(vBox);
        scene.setFill(Color.WHITE);
        scene.getStylesheets().add(Objects.requireNonNull(StripViewApp.class.getResource("fonts.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(StripViewApp.class.getResource("strip-view-app.css")).toExternalForm());

        CSSFX.start();

        stage.setTitle("Strip View");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(500);
        stage.centerOnScreen();
        stage.show();
    }


    private WeatherData createWeatherData(int i, WeatherCondition condition) {
        LocalDate date = LocalDate.now();
        WeatherData data = new WeatherData();
        data.setDate(date.plusDays(i));
        data.setAccurate(1 - 0.1 * i);
        data.setWindSpeed(10 + Math.random() * 20);
        data.setWeatherCondition(condition);
        return data;
    }

    public static void main(String[] args) {
        launch();
    }
}
