package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.Dockable;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DockingPaneAppTest {

    @Test
    public void successfulContentDropMovesRocketAndInstructionsInBothDirections() {
        Group rocketAndInstructions = new Group();
        Group dropInstructions = new Group();
        StackPane source = new StackPane(rocketAndInstructions);
        StackPane target = new StackPane(dropInstructions);
        Dockable drag = new Dockable("drag", "Rocket Source", source);
        Dockable drop = new Dockable("drop", "Rocket Target", target);

        DockingPaneApp.swapContentsAndTitles(drag, drop);

        assertEquals("Rocket Target", drag.getTitle());
        assertEquals("Rocket Source", drop.getTitle());
        assertSame(dropInstructions, source.getChildren().getFirst());
        assertSame(rocketAndInstructions, target.getChildren().getFirst());

        DockingPaneApp.swapContentsAndTitles(drop, drag);

        assertEquals("Rocket Source", drag.getTitle());
        assertEquals("Rocket Target", drop.getTitle());
        assertSame(rocketAndInstructions, source.getChildren().getFirst());
        assertSame(dropInstructions, target.getChildren().getFirst());
    }
}
