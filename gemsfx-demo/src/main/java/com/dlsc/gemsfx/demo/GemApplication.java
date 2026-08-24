package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.Theme;
import com.dlsc.gemsfx.Skeleton;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.jpro.webapi.WebAPI;
import devtoolsfx.gui.GUI;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class GemApplication extends Application {

    public String getDescription() {
        try (InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + ".md")) {
            if (inputStream == null) {
                return "";
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    static {
        if (Boolean.getBoolean("atlantafx") && !isRunningInShowcase()) {
            setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        }
    }

    /**
     * Determines whether the demos are running inside of the showcase application. The showcase
     * runs the demos in its own JVM and applies the theme that the user has selected in its own
     * user interface. Setting the user agent stylesheet from within a demo would therefore
     * change the appearance of the entire showcase, hence the demos must not apply a theme of
     * their own in that case.
     *
     * @return true if the demos are being started by the showcase application
     */
    public static boolean isRunningInShowcase() {
        return Boolean.getBoolean("showcase");
    }

    public GemApplication() {
    }

    protected final <T extends Node> T hideInBrowser(T node) {
        boolean visible = !WebAPI.isBrowser();
        node.setVisible(visible);
        node.setManaged(visible);
        return node;
    }

    protected final <T extends ButtonBase> T configureDevToolsButton(T button) {
        button.setText("Dev Tools");
        hideInBrowser(button);
        button.setOnAction(evt -> {
            if (button.getScene() != null && button.getScene().getWindow() instanceof Stage stage) {
                GUI.openToolStage(stage, getHostServices());
            }
        });
        return button;
    }

    /**
     * Creates a combo box that allows the user to switch the AtlantaFX theme of the running
     * demo at runtime. The switcher is only useful when the demo is started with AtlantaFX
     * support (system property {@code atlantafx}), hence an empty optional is returned when
     * the application runs with the standard Modena stylesheet. No switcher is created either
     * when the demo runs inside of the showcase application, which comes with a theme switcher
     * of its own.
     *
     * @return the theme switcher or an empty optional if AtlantaFX is not being used
     */
    protected final Optional<Node> createThemeSwitcher() {
        if (!Boolean.getBoolean("atlantafx") || isRunningInShowcase()) {
            return Optional.empty();
        }

        ComboBox<Theme> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(GemsFXDemoLauncher.ALL_THEMES);
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Theme theme) {
                return theme == null ? "" : theme.getName();
            }

            @Override
            public Theme fromString(String string) {
                return null;
            }
        });

        String currentStylesheet = Application.getUserAgentStylesheet();
        GemsFXDemoLauncher.ALL_THEMES.stream()
                .filter(theme -> Objects.equals(theme.getUserAgentStylesheet(), currentStylesheet))
                .findFirst()
                .ifPresent(comboBox::setValue);

        comboBox.valueProperty().addListener((obs, oldTheme, newTheme) -> {
            if (newTheme != null) {
                Application.setUserAgentStylesheet(newTheme.getUserAgentStylesheet());
            }
        });

        HBox box = new HBox(5, new Label("Theme:"), comboBox);
        box.setAlignment(Pos.CENTER_LEFT);
        return Optional.of(box);
    }

    /**
     * Creates a check box that can be used to switch the shimmer animation of all
     * {@link Skeleton} controls found inside the given node on and off. Switching the
     * animation off is done by setting the cycle duration of the skeletons to
     * {@link Duration#ZERO}. Switching it on again restores the duration that the
     * skeleton had when this method was called.
     *
     * @param root the node containing the skeletons, usually a placeholder node
     * @return the check box controlling the shimmer animation
     */
    protected final CheckBox createShimmerToggle(Node root) {
        Map<Skeleton, Duration> skeletons = new LinkedHashMap<>();
        collectSkeletons(root, skeletons);

        CheckBox checkBox = new CheckBox("Shimmer");
        checkBox.setSelected(true);
        checkBox.selectedProperty().addListener(it -> skeletons.forEach((skeleton, duration) ->
                skeleton.setCycleDuration(checkBox.isSelected() ? duration : Duration.ZERO)));

        return checkBox;
    }

    private void collectSkeletons(Node node, Map<Skeleton, Duration> skeletons) {
        if (node instanceof Skeleton skeleton) {
            skeletons.put(skeleton, skeleton.getCycleDuration());
        }

        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collectSkeletons(child, skeletons));
        }
    }

    @Override
    public void start(Stage stage) {
        if (Boolean.getBoolean("atlantafx")) {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    GemsFXAtlantaFX.applyTo(newScene);
                }
            });
        }
    }
}
