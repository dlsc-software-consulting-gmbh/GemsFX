package com.dlsc.gemsfx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class DockableTest extends FxTestBase {

    @Test
    public void dockableRetainsIdentityAndContent() {
        Dockable dockable = invoke(() -> {
            Label content = new Label("Editor");
            return new Dockable("editor:main", "Editor", content);
        });

        assertEquals("editor:main", dockable.getId());
        assertEquals("Editor", dockable.getTitle());
        assertNotNull(dockable.getContent());
    }

    @Test
    public void dockableTitleCanBeUpdatedAndObserved() {
        Dockable dockable = invoke(() -> new Dockable("editor:main", "Editor", new Label("Editor")));
        AtomicReference<String> observedTitle = new AtomicReference<>();
        dockable.titleProperty().addListener((observable, oldTitle, newTitle) -> observedTitle.set(newTitle));

        invoke(() -> {
            dockable.setTitle("Renamed Editor");
            return null;
        });

        assertEquals("Renamed Editor", dockable.getTitle());
        assertEquals("Renamed Editor", dockable.titleProperty().get());
        assertEquals("Renamed Editor", observedTitle.get());
    }

    @Test
    public void dockableRejectsBlankUpdatedTitle() {
        Dockable dockable = invoke(() -> new Dockable("editor:main", "Editor", new Label("Editor")));

        assertThrows(IllegalArgumentException.class, () -> invoke(() -> {
            dockable.setTitle("   ");
            return null;
        }));
        assertEquals("Editor", dockable.getTitle());
    }

    @Test
    public void dockableWithOptionalGraphic() {
        Dockable dockable = invoke(() -> {
            Label content = new Label("Panel");
            ImageView graphic = new ImageView();
            return new Dockable("panel:1", "Panel", graphic, content);
        });

        assertEquals("panel:1", dockable.getId());
        assertEquals("Panel", dockable.getTitle());
        assertNotNull(dockable.getGraphic());
        assertNotNull(dockable.getContent());
    }

    @Test
    public void dockableGraphicCanBeReplacedRemovedAndObserved() {
        invoke(() -> {
            ImageView initial = new ImageView();
            ImageView replacement = new ImageView();
            Dockable dockable = new Dockable("panel:1", "Panel", initial, new Label("Content"));
            List<Node> changes = new ArrayList<>();
            dockable.graphicProperty().addListener((obs, oldGraphic, newGraphic) -> changes.add(newGraphic));

            dockable.setGraphic(replacement);
            assertSame(replacement, dockable.getGraphic());
            dockable.setGraphic(null);
            assertNull(dockable.getGraphic());
            assertEquals(Arrays.asList(replacement, null), changes);
            return null;
        });
    }

    @Test
    public void dockableWithoutGraphicReturnsNull() {
        Dockable dockable = invoke(() -> {
            Label content = new Label("View");
            return new Dockable("view:1", "View", content);
        });

        assertNull(dockable.getGraphic());
    }

    @Test
    public void dockableRejectsNullId() {
        assertThrows(NullPointerException.class, () -> {
            invoke(() -> new Dockable(null, "Title", new Label()));
        });
    }

    @Test
    public void dockableRejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> {
            invoke(() -> new Dockable("   ", "Title", new Label()));
        });
    }

    @Test
    public void dockableRejectsNullTitle() {
        assertThrows(NullPointerException.class, () -> {
            invoke(() -> new Dockable("id", null, new Label()));
        });
    }

    @Test
    public void dockableRejectsNullContent() {
        assertThrows(NullPointerException.class, () -> {
            invoke(() -> new Dockable("id", "Title", null));
        });
    }

    @Test
    public void dockableRejectsNullContentWithGraphic() {
        assertThrows(NullPointerException.class, () -> {
            invoke(() -> new Dockable("id", "Title", new Label(), null));
        });
    }
}
