package com.dlsc.gemsfx.util;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A manager for storing the location and dimension of a stage across user sessions.
 * Installing this manager on a stage will ensure that a stage will present itself at
 * the same location and in the same size that it had when the user closed it the last
 * time. This manager also works with multiple screens and will ensure that the window
 * becomes visible if the last used screen is no longer available. In that case the stage
 * will be shown centered on the primary screen with the specified min width and min
 * height (the stage will also be resized to that minimum size in this case).
 */
public class StageManager {

    private static final Logger LOG = Logger.getLogger(StageManager.class.getSimpleName());

    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_ICONIFIED = "iconified";
    private static final String KEY_MAXIMIZED = "maximized";
    private static final String KEY_FULLSCREEN = "fullscreen";

    private final Stage stage;
    private final Preferences preferences;

    private final double minWidth;
    private final double minHeight;

    private boolean supportFullScreenAndMaximized;

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path. The default values for the minimum
     * width is 850 and for the minimum height is 600.
     *
     * @param stage the stage to persist and restore
     * @param preferencesPath the java.util preferences path used for storing the information
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, String preferencesPath) {
        return install(stage, Preferences.userRoot().node(Objects.requireNonNull(preferencesPath, "preferences path can not be null")));
    }
    
    /**
     * Installs a new manager for the given stage. The location and dimension
     * information will be stored in the given preferences. The
     * default values for the minimum width is 850 and for the minimum height is
     * 600.
     *
     * @param stage the stage to persist and restore
     * @param preferences the preferences used for storing the information
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, Preferences preferences) {
        return install(stage, preferences, 850, 600);
    }

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path.
     *
     * @param stage the stage to persist and restore
     * @param preferencesPath the java.util preferences path used for storing the information
     * @param minWidth the minimum width that will be used for the stage
     * @param minHeight the minimum height that will be used for the stage
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, String preferencesPath, double minWidth, double minHeight) {
        return install(stage, Preferences.userRoot().node(Objects.requireNonNull(preferencesPath, "preferences path can not be null")), minWidth, minHeight);
    }

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path.
     *
     * @param stage the stage to persist and restore
     * @param preferences the java.util preferences used for storing the information
     * @param minWidth the minimum width that will be used for the stage
     * @param minHeight the minimum height that will be used for the stage
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, Preferences preferences, double minWidth, double minHeight) {
        return install(stage, preferences, minWidth, minHeight, false);
    }

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the user preferences at the given path.
     *
     * @param stage the stage to persist and restore
     * @param preferencesPath the java.util preferences path used for storing the information
     * @param minWidth the minimum width that will be used for the stage
     * @param minHeight the minimum height that will be used for the stage
     * @param supportFullScreenAndMaximized whether the maximized and the full screen state shall be persisted and restored
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, String preferencesPath, double minWidth, double minHeight, boolean supportFullScreenAndMaximized) {
        return install(stage, Preferences.userRoot().node(Objects.requireNonNull(preferencesPath, "preferences path can not be null")), minWidth, minHeight, supportFullScreenAndMaximized);
    }

    /**
     * Installs a new manager for the given stage. The location and dimension information will
     * be stored in the given preferences.
     *
     * @param stage the stage to persist and restore
     * @param preferences the java.util preferences used for storing the information
     * @param minWidth the minimum width that will be used for the stage
     * @param minHeight the minimum height that will be used for the stage
     * @param supportFullScreenAndMaximized whether the maximized and the full screen state shall be persisted and restored
     * @return the installed stage manager
     */
    public static StageManager install(Stage stage, Preferences preferences, double minWidth, double minHeight, boolean supportFullScreenAndMaximized) {
        return new StageManager(stage, preferences, minWidth, minHeight, supportFullScreenAndMaximized);
    }

    /*
     * Constructs a new stage manager.
     */
    private StageManager(Stage stage, Preferences preferences, double minWidth, double minHeight, boolean supportFullScreenAndMaximized) {
        if (minWidth <= 0) {
            throw new IllegalArgumentException("min width must be larger than 0");
        }
        if (minHeight <= 0) {
            throw new IllegalArgumentException("min height must be larger than 0");
        }

        this.stage = Objects.requireNonNull(stage, "stage can not be null");
        this.preferences = Objects.requireNonNull(preferences, "preferences can not be null");
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.supportFullScreenAndMaximized = supportFullScreenAndMaximized;

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
        stage.maximizedProperty().addListener(stageListener);

        stage.showingProperty().addListener(it -> {
            if (!stage.isShowing()) {
                flush();
            }
        });
    }

    /**
     * Sets whether full-screen and maximized states should be persisted.
     *
     * <p>Please note that the state can only be <em>restored</em> when the flag is passed to one of
     * the {@code install} methods, as the restoration happens while the manager is being created.
     *
     * @param supportFullScreenAndMaximized {@code true} to persist full-screen and maximized states
     */
    public final void setSupportFullScreenAndMaximized(boolean supportFullScreenAndMaximized) {
        this.supportFullScreenAndMaximized = supportFullScreenAndMaximized;
    }

    /**
     * Returns whether full-screen and maximized states should be persisted.
     *
     * @return {@code true} if full-screen and maximized states are persisted
     */
    public final boolean isSupportFullScreenAndMaximized() {
        return supportFullScreenAndMaximized;
    }

    private void saveStage() throws SecurityException {
        boolean maximizedOrFullScreen = supportFullScreenAndMaximized && (stage.isMaximized() || stage.isFullScreen());

        if (maximizedOrFullScreen) {
            LOG.fine(MessageFormat.format("saving stage, iconified = {0}, maximized = {1}, fullscreen = {2}", stage.isIconified(), stage.isMaximized(), stage.isFullScreen()));
        } else {
            LOG.fine(MessageFormat.format("saving stage, x = {0}, y = {1}, width = {2}, height = {3}, iconified = {4}, maximized = {5}, fullscreen = {6}", stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), stage.isIconified(), stage.isMaximized(), stage.isFullScreen()));
        }

        /*
         * The bounds of a stage are "NaN" as long as the stage has not been shown, and they can
         * not be restored in a meaningful way. Storing them would corrupt the values of a previous
         * session, hence we only store bounds that are completely defined.
         */
        if (!maximizedOrFullScreen && hasValidBounds()) {
            preferences.putDouble(KEY_X, stage.getX());
            preferences.putDouble(KEY_Y, stage.getY());
            preferences.putDouble(KEY_WIDTH, stage.getWidth());
            preferences.putDouble(KEY_HEIGHT, stage.getHeight());
        }

        preferences.putBoolean(KEY_ICONIFIED, stage.isIconified());
        preferences.putBoolean(KEY_MAXIMIZED, stage.isMaximized());
        preferences.putBoolean(KEY_FULLSCREEN, stage.isFullScreen());
    }

    /*
     * Determines whether the current bounds of the stage are fully defined, which is not the
     * case as long as the stage has never been shown.
     */
    private boolean hasValidBounds() {
        return isDefined(stage.getX()) && isDefined(stage.getY()) && isDefined(stage.getWidth()) && isDefined(stage.getHeight());
    }

    private static boolean isDefined(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /*
     * Writes the preferences to the backing store. Called when the stage gets hidden so that
     * the values also survive a JVM that gets shut down without running any shutdown hooks.
     */
    private void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException | SecurityException ex) {
            LOG.log(Level.FINE, "unable to flush the preferences", ex);
        }
    }

    private void restoreStage() throws SecurityException {
        double x = preferences.getDouble(KEY_X, Double.NaN);
        double y = preferences.getDouble(KEY_Y, Double.NaN);
        double w = preferences.getDouble(KEY_WIDTH, Double.NaN);
        double h = preferences.getDouble(KEY_HEIGHT, Double.NaN);

        boolean iconified = preferences.getBoolean(KEY_ICONIFIED, false);
        boolean maximized = preferences.getBoolean(KEY_MAXIMIZED, false);
        boolean fullscreen = preferences.getBoolean(KEY_FULLSCREEN, false);

        if (supportFullScreenAndMaximized) {
            LOG.fine(MessageFormat.format("loading stage, x = {0}, y = {1}, width = {2}, height = {3}, iconified = {4}, maximized = {5}, fullscreen = {6}", x, y, w, h, iconified, maximized, fullscreen));
        } else {
            LOG.fine(MessageFormat.format("loading stage, x = {0}, y = {1}, width = {2}, height = {3}, iconified = {4}", x, y, w, h, iconified));
        }

        if (!isDefined(w)) {
            // nothing stored, yet: fall back to the size that the application might have set
            w = stage.getWidth();
        }

        if (!isDefined(h)) {
            h = stage.getHeight();
        }

        /*
         * Math.max() returns NaN if one of its arguments is NaN, hence the explicit checks.
         */
        stage.setWidth(isDefined(w) ? Math.max(minWidth, w) : minWidth);
        stage.setHeight(isDefined(h) ? Math.max(minHeight, h) : minHeight);

        if (isDefined(x) && isDefined(y)) {
            stage.setX(x);
            stage.setY(y);
        } else {
            stage.centerOnScreen();
        }

        Platform.runLater(() -> {
            stage.setIconified(iconified);

            if (supportFullScreenAndMaximized) {
                stage.setMaximized(maximized);
                stage.setFullScreen(fullscreen);
            }

            if (isWindowOutOfBounds()) {
                LOG.fine("stage is out of bounds, moving it to primary screen");
                moveToPrimaryScreen();
            }
        });
    }

    /*
     * Performs a check on the stage to see if its bounds are fully visible on one of the
     * currently used screens.
     */
    private boolean isWindowOutOfBounds() {
        if (!hasValidBounds()) {
            /*
             * The stage has not been shown, yet, so its location is unknown. Any check would
             * be meaningless and moving the stage would discard the size and location that
             * were just restored.
             */
            return false;
        }

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
