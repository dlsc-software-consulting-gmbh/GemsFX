package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.util.SessionManager;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

public class SessionManagerApp extends Application {

    // Properties for session state
    private final StringProperty userName = new SimpleStringProperty();
    private final BooleanProperty darkModeEnabled = new SimpleBooleanProperty();
    private final DoubleProperty score = new SimpleDoubleProperty(80);
    private final StringProperty birthDateString = new SimpleStringProperty();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String LIGHT_BG = "-fx-background-color: white;";
    private static final String DARK_BG = "-fx-background-color: rgb(225,225,225);";
    private static final String BASIC_STYLE = "-fx-padding: 20; -fx-font-size: 14px; -fx-spacing: 25px;";

    private final VBox root = new VBox();

    @Override
    public void start(Stage primaryStage) {
        // Set up session manager
        SessionManager sessionManager = new SessionManager(Preferences.userNodeForPackage(SessionManagerApp.class));
        sessionManager.register("session.manager.app5.user.name", userName);
        sessionManager.register("session.manager.app5.score", score);
        sessionManager.register("session.manager.app5.dark.mode", darkModeEnabled);
        sessionManager.register("session.manager.app5.birth.date", birthDateString);

        // UI Elements

        Label tipsLabel = new Label("The following values will be restored automatically on next launch.");
        tipsLabel.setWrapText(true);

        // Name input field (stored as a StringProperty)
        Label nameLabel = new Label("Select or enter your name:");
        ComboBox<String> nameComboBox = new ComboBox<>();
        nameComboBox.getItems().addAll("John", "Jane", "Doe");
        nameComboBox.setEditable(true);
        nameComboBox.setPromptText("Enter your name");
        nameComboBox.valueProperty().bindBidirectional(userName);
        VBox nameBox = new VBox(5, nameLabel, nameComboBox);

        // Score slider (stored as a DoubleProperty)
        Label scoreLabel = new Label("Your score:");
        Slider scoreSlider = new Slider(0, 100, score.get());
        scoreSlider.setMaxWidth(200);
        scoreSlider.setShowTickLabels(true);
        scoreSlider.setShowTickMarks(true);
        scoreSlider.setMajorTickUnit(20);
        scoreSlider.valueProperty().bindBidirectional(score);
        scoreLabel.textProperty().bind(score.asString("Your score: %.1f"));
        VBox scoreBox = new VBox(5, scoreLabel, scoreSlider);

        // Calendar date picker (stores the selected LocalDate as a String and restores it on startup)
        Label dateLabel = new Label("Select your birth date:");
        CalendarPicker calendarPicker = new CalendarPicker();

        String storedDate = birthDateString.get();
        if (StringUtils.isNotBlank(storedDate) && !"null".equalsIgnoreCase(storedDate)) {
            try {
                LocalDate parsedDate = LocalDate.parse(storedDate, DATE_FORMATTER);
                calendarPicker.setValue(parsedDate);
            } catch (Exception ignored) {
                // Ignore parsing errors
            }
        }

        calendarPicker.valueProperty().addListener((obs, oldVal, newVal) ->
                birthDateString.set(newVal != null ? newVal.format(DATE_FORMATTER) : "null"));

        VBox dateBox = new VBox(5, dateLabel, calendarPicker);

        // Dark mode setting (stored as a BooleanProperty)
        Label darkModeLabel = new Label("Enable dark mode:");
        CheckBox darkModeCheckBox = new CheckBox("Dark mode");
        darkModeCheckBox.selectedProperty().bindBidirectional(darkModeEnabled);
        darkModeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyDarkMode(newVal));
        applyDarkMode(darkModeEnabled.get());
        VBox darkModeBox = new VBox(5, darkModeLabel, darkModeCheckBox);

        // Combine all sections
        root.getChildren().addAll(tipsLabel, nameBox, scoreBox, dateBox, darkModeBox);

        Scene scene = new Scene(root, 300, 420);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Session State Demo");
        primaryStage.show();
    }

    private void applyDarkMode(boolean enabled) {
        root.setStyle((enabled ? DARK_BG : LIGHT_BG) + BASIC_STYLE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
