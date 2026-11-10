package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingTab;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;
import java.util.Objects;

/**
 * Renders one tab group: a strip of factory-created headers above the content of the selected dockable only.
 * This deliberately does not use the JavaFX {@code TabPane}.
 */
final class DockingTabPane extends VBox {

    static final String DOCKABLE_ID_KEY = "docking.dockable-id";
    private static final PseudoClass LAST_TAB = PseudoClass.getPseudoClass("last-tab");
    private static final Object TITLE_ACCESSIBLE_TEXT_BINDING_KEY = new Object();

    private final DockingPane dockingPane;
    private final List<String> ids;

    DockingTabPane(DockingPane dockingPane, DockingLayoutNode.Tabs tabs) {
        this.dockingPane = dockingPane;
        this.ids = tabs.getDockableIds();
        getStyleClass().add("docking-tab-pane");

        HBox strip = new HBox();
        strip.getStyleClass().add("docking-tab-strip");
        StackPane contentArea = new StackPane();
        contentArea.getStyleClass().add("docking-tab-content");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        for (String id : ids) {
            Dockable dockable = find(dockingPane, id);
            if (dockable == null) {
                continue;
            }
            boolean selected = id.equals(tabs.getSelectedId());
            strip.getChildren().add(createTitle(dockable, selected));
            if (selected) {
                contentArea.getChildren().setAll(dockable.getContent());
            }
        }
        if (!strip.getChildren().isEmpty()) {
            strip.getChildren().get(strip.getChildren().size() - 1).pseudoClassStateChanged(LAST_TAB, true);
        }
        getChildren().addAll(strip, contentArea);
    }

    private static Dockable find(DockingPane pane, String id) {
        for (Dockable dockable : pane.getDockables()) {
            if (dockable.getId().equals(id)) {
                return dockable;
            }
        }
        return null;
    }

    private Node createTitle(Dockable dockable, boolean selected) {
        Callback<Dockable, Node> factory = dockingPane.getDockingContext().getTabFactory();
        Node title = Objects.requireNonNull(factory == null
                ? new DockingTab(dockingPane.getDockingContext(), dockable)
                : factory.call(dockable), "tabFactory must not return null");
        if (title.getParent() != null || title.getProperties().containsKey(DOCKABLE_ID_KEY)) {
            throw new IllegalArgumentException("tabFactory must return a fresh, unparented node");
        }
        if (!title.getStyleClass().contains("docking-tab")) {
            title.getStyleClass().add("docking-tab");
        }
        if (selected) {
            title.getStyleClass().add("selected");
        } else {
            title.getStyleClass().remove("selected");
        }
        if (title instanceof Region) {
            Region region = (Region) title;
            region.setMinWidth(0);
            region.setPrefWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setFocusTraversable(true);
        title.getProperties().put(DOCKABLE_ID_KEY, dockable.getId());
        AccessibilityUtil.setRole(title, AccessibleRole.TAB_ITEM);
        StringBinding accessibleText = Bindings.createStringBinding(dockable::getTitle, dockable.titleProperty());
        AccessibilityUtil.bindAccessibleText(title, accessibleText);
        title.getProperties().put(TITLE_ACCESSIBLE_TEXT_BINDING_KEY, accessibleText);

        // Selecting on press rebuilds the titles and detaches the drag source before drag detection.
        title.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                dockingPane.selectDockable(dockable.getId());
            }
        });
        title.addEventHandler(KeyEvent.KEY_PRESSED, e -> handleKey(dockable, e));
        return title;
    }

    private void handleKey(Dockable dockable, KeyEvent event) {
        KeyCode code = event.getCode();
        int index = ids.indexOf(dockable.getId());
        if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            dockingPane.selectDockable(dockable.getId());
        } else if (code == KeyCode.LEFT && index > 0) {
            dockingPane.selectDockable(ids.get(index - 1));
        } else if (code == KeyCode.RIGHT && index < ids.size() - 1) {
            dockingPane.selectDockable(ids.get(index + 1));
        } else if (code == KeyCode.DELETE || (code == KeyCode.W && event.isShortcutDown())) {
            dockingPane.getDockingContext().requestCloseDockable(dockable);
        } else {
            return;
        }
        event.consume();
    }
}
