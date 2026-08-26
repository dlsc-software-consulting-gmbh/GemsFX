package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.GlassPane;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Demonstrates the {@link GlassPane} control. The glass pane covers the user interface of a
 * small form with a semi-transparent color, which is the standard way of showing that the
 * application is busy and that the user interface does not accept any input at the moment.
 */
public class GlassPaneApp extends GemApplication {

    private final GlassPane glassPane = new GlassPane();

    @Override
    public void start(Stage stage) {
        super.start(stage);

        // no padding, the glass pane is supposed to cover the entire content area
        StackPane contentPane = new StackPane(createForm(), glassPane, createBusyIndicator());
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        HBox box = new HBox(20, contentPane, createOptions());
        box.setFillHeight(true);

        Scene scene = new Scene(box);

        stage.setTitle("Glass Pane Demo");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(500);
        stage.show();
    }

    /**
     * Creates the user interface that will be covered by the glass pane.
     */
    private VBox createForm() {
        Label titleLabel = new Label("Sign In");
        titleLabel.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("User name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> serverBox = new ComboBox<>();
        serverBox.getItems().setAll("Production", "Staging", "Development");
        serverBox.setValue("Production");
        serverBox.setMaxWidth(Double.MAX_VALUE);

        CheckBox rememberBox = new CheckBox("Remember me");

        Button signInButton = new Button("Sign In");
        signInButton.setDefaultButton(true);
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setOnAction(evt -> showBusy());

        VBox form = new VBox(10, titleLabel, userField, passwordField, serverBox, rememberBox, signInButton);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setMaxSize(300, VBox.USE_PREF_SIZE);
        return form;
    }

    /**
     * Creates the "busy" indicator that is shown on top of the glass pane while the glass pane
     * is blocking the user interface.
     */
    private VBox createBusyIndicator() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);

        Label label = new Label("Signing in ...");
        label.setStyle("-fx-font-weight: bold;");

        VBox box = new VBox(10, progressIndicator, label);
        box.setAlignment(Pos.CENTER);
        box.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        box.setMouseTransparent(true);
        box.setStyle("-fx-background-color: -fx-background; -fx-background-radius: 8px; -fx-padding: 20px;");

        // the indicator is only interesting while the glass pane is being shown
        box.visibleProperty().bind(glassPane.hideProperty().not());
        box.managedProperty().bind(box.visibleProperty());

        return box;
    }

    /**
     * Shows the glass pane for two seconds, simulating a long-running background operation.
     */
    private void showBusy() {
        glassPane.setHide(false);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(evt -> glassPane.setHide(true));
        pause.play();
    }

    /**
     * Creates the panel with the settings that can be changed by the user.
     */
    private VBox createOptions() {
        ToggleButton blockButton = new ToggleButton("Block user interface");
        blockButton.setMaxWidth(Double.MAX_VALUE);
        blockButton.setSelected(!glassPane.isHide());
        blockButton.selectedProperty().addListener(it -> glassPane.setHide(!blockButton.isSelected()));
        glassPane.hideProperty().addListener(it -> blockButton.setSelected(!glassPane.isHide()));

        Button busyButton = new Button("Simulate long-running task");
        busyButton.setMaxWidth(Double.MAX_VALUE);
        busyButton.setOnAction(evt -> showBusy());

        CheckBox fadeBox = new CheckBox("Fade in / out");
        fadeBox.selectedProperty().bindBidirectional(glassPane.fadeInOutProperty());

        Slider durationSlider = new Slider(0, 2000, glassPane.getFadeInOutDuration().toMillis());
        durationSlider.setShowTickLabels(true);
        durationSlider.setShowTickMarks(true);
        durationSlider.setMajorTickUnit(500);
        durationSlider.disableProperty().bind(fadeBox.selectedProperty().not());
        durationSlider.valueProperty().addListener(it -> glassPane.setFadeInOutDuration(Duration.millis(durationSlider.getValue())));

        Slider opacitySlider = new Slider(0, 1, glassPane.getBlockingOpacity());
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(.25);
        opacitySlider.valueProperty().addListener(it -> glassPane.setBlockingOpacity(opacitySlider.getValue()));

        Button devTools = configureDevToolsButton(new Button());
        devTools.setMaxWidth(Double.MAX_VALUE);

        VBox optionsBox = new VBox(10,
                blockButton,
                busyButton,
                new Separator(),
                fadeBox,
                new Label("Fade duration (ms)"), durationSlider,
                new Label("Blocking opacity"), opacitySlider,
                devTools);

        createThemeSwitcher().ifPresent(switcher -> optionsBox.getChildren().add(0, switcher));

        optionsBox.setPrefWidth(260);
        optionsBox.setMinWidth(260);
        optionsBox.setPadding(new Insets(20));
        return optionsBox;
    }

    public static void main(String[] args) {
        launch();
    }
}
