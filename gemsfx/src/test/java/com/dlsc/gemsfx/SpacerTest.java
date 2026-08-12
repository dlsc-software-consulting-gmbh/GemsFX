package com.dlsc.gemsfx;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Spacer}.
 */
public class SpacerTest extends FxTestBase {

    @Test
    public void styleClass() {
        Spacer spacer = invoke(Spacer::new);
        assertTrue(spacer.getStyleClass().contains("spacer"));
    }

    @Test
    public void defaultActiveTrue() {
        Spacer spacer = invoke(Spacer::new);
        assertTrue(spacer.isActive());
    }

    @Test
    public void activeBindsToVisible() {
        Spacer spacer = invoke(Spacer::new);
        assertTrue(spacer.isVisible());
        runFx(() -> spacer.setActive(false));
        assertFalse(spacer.isVisible());
    }

    @Test
    public void activeManagedBinding() {
        Spacer spacer = invoke(Spacer::new);
        // When active, spacer is managed; when inactive it is not
        assertTrue(spacer.isManaged());
        runFx(() -> spacer.setActive(false));
        assertFalse(spacer.isManaged());
    }

    @Test
    public void hboxGrowPriority() {
        Spacer spacer = invoke(Spacer::new);
        assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer));
    }

    @Test
    public void vboxGrowPriority() {
        Spacer spacer = invoke(Spacer::new);
        assertEquals(Priority.ALWAYS, VBox.getVgrow(spacer));
    }

    @Test
    public void setActiveFalse() {
        Spacer spacer = invoke(Spacer::new);
        runFx(() -> spacer.setActive(false));
        assertFalse(spacer.isActive());
    }

    @Test
    public void setActiveTrue() {
        Spacer spacer = invoke(Spacer::new);
        runFx(() -> {
            spacer.setActive(false);
            spacer.setActive(true);
        });
        assertTrue(spacer.isActive());
    }

    @Test
    public void activePropertyListener() {
        Spacer spacer = invoke(Spacer::new);
        boolean[] fired = {false};
        runFx(() -> spacer.activeProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> spacer.setActive(false));
        assertTrue(fired[0]);
    }
}
