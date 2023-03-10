package com.dlsc.gemsfx.demo.fake;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class WeatherSummaryPane extends Label {

    private final Label dateLabel;
    private final Label windLabel;
    private final Label accurateLabel;

    private final ImageView imageView;

    public WeatherSummaryPane() {
        getStyleClass().add("weather-summary-pane");

        StackPane pane = new StackPane();
        dateLabel = new Label();

        imageView = new ImageView();
        imageView.setFitWidth(52);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        VBox.setMargin(imageView, new Insets(16, 0, 12, 0));

        windLabel = new Label();
        windLabel.getStyleClass().add("wind-label");

        accurateLabel = new Label();

        VBox box = new VBox(8);
        box.setPrefWidth(88);
        box.setAlignment(Pos.TOP_LEFT);
        box.getChildren().addAll(dateLabel, imageView, windLabel, accurateLabel);

        pane.getChildren().add(box);
        pane.setPadding(new Insets(10, 20, 10, 20));
        pane.getStyleClass().add("ws-content-pane");
        setGraphic(pane);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        weatherSummaryProperty().addListener(it -> updatePane());
    }

    private final ObjectProperty<WeatherData> weatherSummary = new SimpleObjectProperty<>(this, "weatherSummary");

    public WeatherData getWeatherSummary() {
        return weatherSummary.get();
    }

    public ObjectProperty<WeatherData> weatherSummaryProperty() {
        return weatherSummary;
    }

    public void setWeatherSummary(WeatherData summary) {
        this.weatherSummary.set(summary);
    }

    private void updatePane() {
        WeatherData summary = getWeatherSummary();

        if (summary == null) {
            dateLabel.setText(null);
            imageView.setImage(null);
            windLabel.setText(null);
            accurateLabel.setText(null);
        } else {
            LocalDate localDate = summary.getDate();
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            String shortName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            dateLabel.setText(shortName + " " + localDate.getDayOfMonth());
            imageView.setImage(summary.getWeatherCondition().getImage());
            windLabel.setText(String.format("%.2f", summary.getWindSpeed()) + " m/s");
            accurateLabel.setText(String.format("%.0f", summary.getAccurate() * 100) + "% accurate");
        }
    }

}
