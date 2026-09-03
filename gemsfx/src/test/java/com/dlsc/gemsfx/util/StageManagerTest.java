package com.dlsc.gemsfx.util;

import com.dlsc.gemsfx.FxTestBase;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class StageManagerTest extends FxTestBase {

    private static final String NODE = "gemsfx.tests.stagemanager";

    private Preferences preferences;

    @Before
    public void setup() throws BackingStoreException {
        preferences = Preferences.userRoot().node(NODE);
        preferences.clear();
    }

    @After
    public void cleanup() throws BackingStoreException {
        preferences.clear();
    }

    @Test
    public void shouldUseMinimumSizeWhenNothingHasBeenStored() {
        // given / when
        Stage stage = installNewStage(400, 300);

        // then: the stage has never been shown, its size is "NaN" and must not become "NaN"
        assertEquals(400, stage.getWidth(), 0);
        assertEquals(300, stage.getHeight(), 0);
    }

    @Test
    public void shouldKeepApplicationSizeWhenNothingHasBeenStored() {
        // given
        Stage stage = invoke(Stage::new);
        runFx(() -> {
            stage.setWidth(1000);
            stage.setHeight(800);
        });

        // when
        install(stage, 400, 300);

        // then
        assertEquals(1000, stage.getWidth(), 0);
        assertEquals(800, stage.getHeight(), 0);
    }

    @Test
    public void shouldNotStoreUndefinedBounds() {
        // given / when: the stage is never shown, so x, y are undefined
        installNewStage(400, 300);

        // then
        assertNull(preferences.get("x", null));
        assertNull(preferences.get("y", null));
        assertNull(preferences.get("width", null));
        assertNull(preferences.get("height", null));
    }

    @Test
    public void shouldRestoreStoredBounds() {
        // given
        preferences.putDouble("x", 100);
        preferences.putDouble("y", 150);
        preferences.putDouble("width", 900);
        preferences.putDouble("height", 700);

        // when
        Stage stage = installNewStage(400, 300);

        // then
        assertEquals(100, stage.getX(), 0);
        assertEquals(150, stage.getY(), 0);
        assertEquals(900, stage.getWidth(), 0);
        assertEquals(700, stage.getHeight(), 0);
    }

    @Test
    public void shouldRestoreNegativeCoordinates() {
        // given: a stage that slightly hangs over the left edge of the primary screen
        preferences.putDouble("x", -1);
        preferences.putDouble("y", 50);
        preferences.putDouble("width", 900);
        preferences.putDouble("height", 700);

        // when
        Stage stage = installNewStage(400, 300);

        // then: -1 must not be interpreted as "nothing stored"
        assertEquals(-1, stage.getX(), 0);
        assertEquals(50, stage.getY(), 0);
    }

    @Test
    public void shouldEnforceMinimumSize() {
        // given
        preferences.putDouble("x", 100);
        preferences.putDouble("y", 150);
        preferences.putDouble("width", 100);
        preferences.putDouble("height", 50);

        // when
        Stage stage = installNewStage(400, 300);

        // then
        assertEquals(400, stage.getWidth(), 0);
        assertEquals(300, stage.getHeight(), 0);
    }

    @Test
    public void shouldIgnoreCorruptedBounds() {
        // given: values written by an earlier version while the stage was not showing
        preferences.putDouble("x", Double.NaN);
        preferences.putDouble("y", Double.NaN);
        preferences.putDouble("width", Double.NaN);
        preferences.putDouble("height", Double.NaN);

        // when
        Stage stage = installNewStage(400, 300);

        // then
        assertFalse(Double.isNaN(stage.getWidth()));
        assertFalse(Double.isNaN(stage.getHeight()));
        assertEquals(400, stage.getWidth(), 0);
        assertEquals(300, stage.getHeight(), 0);
    }

    @Test
    public void shouldSupportFullScreenAndMaximizedRightFromTheStart() {
        // given
        preferences.putBoolean("maximized", true);

        // when: the flag has to be known while the stage gets restored
        StageManager manager = invoke(() -> StageManager.install(new Stage(), preferences, 400, 300, true));
        waitForFxEvents();

        // then
        assertTrue(manager.isSupportFullScreenAndMaximized());
    }

    @Test
    public void shouldRejectInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> invoke(() -> StageManager.install(new Stage(), preferences, 0, 300)));
        assertThrows(IllegalArgumentException.class, () -> invoke(() -> StageManager.install(new Stage(), preferences, 400, 0)));
        assertThrows(NullPointerException.class, () -> invoke(() -> StageManager.install(null, preferences, 400, 300)));
        assertThrows(NullPointerException.class, () -> invoke(() -> StageManager.install(new Stage(), (Preferences) null, 400, 300)));
        assertThrows(NullPointerException.class, () -> invoke(() -> StageManager.install(new Stage(), (String) null, 400, 300)));
    }

    private Stage installNewStage(double minWidth, double minHeight) {
        return install(invoke(Stage::new), minWidth, minHeight);
    }

    private Stage install(Stage stage, double minWidth, double minHeight) {
        runFx(() -> StageManager.install(stage, preferences, minWidth, minHeight));
        waitForFxEvents();
        return stage;
    }
}
