package com.dlsc.gemsfx.util;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A manager for storing the location and dimension of a stage across user sessions.
 * Installing this manager on a stage will ensure that a stage will present itself at
 * the same location and in the same size that it had when the user closed it the last
 * time. This manager also works with multiple screens and will ensure that the window
 * becomes visible if the last used screen is no longer available. In that case the stage
 * will be shown centered on the primary screen with the specified min width and min
 * height.
 */
public class StageManager {

    private final Logger LOG = Logger.getLogger(StageManager.class.getSimpleName());

    private final Stage stage;
    private final Preferences preferences;

    private double minWidth;
    private double minHeight;

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path. The default values for the minimum
     * width is 850 and for the minimum height is 600.
     */
    public static void install(Stage stage, String preferencesPath) {
        install(stage, preferencesPath, 850, 600);
    }

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path.
     *
     * @param stage the stage to persist and restore
     * @param preferencesPath the java.util preferences path used for storing the information
     * @param minWidth the minimum width that will be used for the stage
     * @param minHeight the minimum height that will be used for the stage
     */
    public static void install(Stage stage, String preferencesPath, double minWidth, double minHeight) {
        new StageManager(stage, preferencesPath, minWidth, minHeight);
    }

    /*
     * Constructs a new stage manager.
     */
    private StageManager(Stage stage, String preferencesPath, double minWidth, double minHeight) {
        if (minWidth <= 0) {
            throw new IllegalArgumentException("min width must be larger than 0");
        }
        if (minHeight <= 0) {
            throw new IllegalArgumentException("min height must be larger than 0");
        }

        this.stage = stage;
        this.minWidth = minWidth;
        this.minHeight = minHeight;

        preferences = Preferences.userRoot().node(preferencesPath);

        restoreStage();

        InvalidationListener stageListener = it -> {
            try {
                saveStage();
            } catch (SecurityException ex) {
                LOG.throwing(StageManager.class.getName(), "init", ex);
            }
        };

        stage.xProperty().addListener(stageListener);
        stage.yProperty().addListener(stageListener);
        stage.widthProperty().addListener(stageListener);
        stage.heightProperty().addListener(stageListener);
        stage.iconifiedProperty().addListener(stageListener);
    }

    private void saveStage() throws SecurityException {
        LOG.fine(MessageFormat.format("saving stage, x = {0}, y = {1}, width = {2}, height = {3}, iconified = {4}", stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), stage.isIconified()));

        preferences.putDouble("x", stage.getX());
        preferences.putDouble("y", stage.getY());
        preferences.putDouble("width", stage.getWidth());
        preferences.putDouble("height", stage.getHeight());
        preferences.putBoolean("iconified", stage.isIconified());
    }

    private void restoreStage() throws SecurityException {
        double x = preferences.getDouble("x", -1);
        double y = preferences.getDouble("y", -1);
        double w = preferences.getDouble("width", stage.getWidth());
        double h = preferences.getDouble("height", stage.getHeight());
        boolean iconified = preferences.getBoolean("iconified", false);

        LOG.fine(MessageFormat.format("loading stage, x = {0}, y = {1}, width = {2}, height = {3}, iconified = {4}", x, y, w, h, iconified));

        if (x == -1 && y == -1) {
            stage.centerOnScreen();
        }

        stage.setX(x);
        stage.setY(y);
        stage.setWidth(Math.max(minWidth, w));
        stage.setHeight(Math.max(minHeight, h));

        Platform.runLater(() -> {
            stage.setIconified(iconified);

            if (isWindowIsOutOfBounds()) {
                LOG.fine("stage is out of bounds, moving it to primary screen");
                moveToPrimaryScreen();
            }
        });
    }

    /*
     * Performs a check on the stage to see if its bounds are fully visible on one of the
     * currently used screens.
     */
    private boolean isWindowIsOutOfBounds() {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (stage.getX() + stage.getWidth() - minWidth >= bounds.getMinX() &&
                    stage.getX() + minWidth <= bounds.getMaxX() &&
                    bounds.getMinY() <= stage.getY() && // We want the title bar to always be visible.
                    stage.getY() + minHeight <= bounds.getMaxY()) {
                return false;
            }
        }
        return true;
    }

    /*
     * Moves the stage to the primary screen to ensure visibility.
     */
    private void moveToPrimaryScreen() {
        /*
         * We can not use Stage.centerOnScreen() as it uses the current screen of the window
         * that we are trying to adjust.
         */
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double centerX = bounds.getMinX() + (bounds.getWidth() - minWidth) / 2;
        double centerY = bounds.getMinY() + (bounds.getHeight() - minHeight) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
        stage.setWidth(minWidth);
        stage.setHeight(minHeight);
    }
}
