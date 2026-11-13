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
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextInputControl;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Renders one tab group: a strip of factory-created headers above the content of the selected dockable only.
 * This deliberately does not use the JavaFX {@code TabPane}.
 */
final class DockingTabPane extends VBox {

    static final String DOCKABLE_ID_KEY = "docking.dockable-id";
    private static final PseudoClass LAST_TAB = PseudoClass.getPseudoClass("last-tab");
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final Object TITLE_ACCESSIBLE_TEXT_BINDING_KEY = new Object();

    private final DockingPane dockingPane;
    private final List<String> ids;
    private final HBox strip = new HBox();
    private final StackPane contentArea = new StackPane();
    private StackPane headerAccessory;
    private String selectedId;

    DockingTabPane(DockingPane dockingPane, DockingLayoutNode.Tabs tabs) {
        this.dockingPane = dockingPane;
        this.ids = tabs.getDockableIds();
        this.selectedId = tabs.getSelectedId();
        getStyleClass().add("docking-tab-pane");

        strip.getStyleClass().add("docking-tab-strip");
        strip.addEventFilter(MouseEvent.MOUSE_CLICKED, this::toggleMaximized);
        contentArea.getStyleClass().add("docking-tab-content");
        contentArea.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> activateSelectedDockable());
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::activateFromHeaderOrBackground);
        addEventFilter(MouseEvent.MOUSE_CLICKED, this::activateFromHeaderOrBackground);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        List<Dockable> dockables = new ArrayList<>();
        for (String id : ids) {
            Dockable dockable = find(dockingPane, id);
            if (dockable == null) {
                continue;
            }
            dockables.add(dockable);
            boolean selected = id.equals(selectedId);
            strip.getChildren().add(createTitle(dockable, selected));
            if (selected) {
                contentArea.getChildren().setAll(dockable.getContent());
            }
        }
        if (!strip.getChildren().isEmpty()) {
            strip.getChildren().get(strip.getChildren().size() - 1).pseudoClassStateChanged(LAST_TAB, true);
        }
        BiFunction<DockingPane, List<Dockable>, Node> factory = dockingPane.getDockingContext().getTabPaneHeaderFactory();
        Node accessory = factory == null ? null : factory.apply(dockingPane, List.copyOf(dockables));
        if (accessory != null) {
            if (accessory.getParent() != null) {
                throw new IllegalArgumentException("tabPaneHeaderFactory must return a fresh, unparented node");
            }
            headerAccessory = new StackPane(accessory);
            headerAccessory.getStyleClass().addAll("docking-tab", "docking-tab-pane-actions");
            strip.getChildren().add(headerAccessory);
        }
        getChildren().addAll(strip, contentArea);
    }

    private void toggleMaximized(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 2
                || !event.isShiftDown() || !event.isStillSincePress() || !(event.getTarget() instanceof Node)) {
            return;
        }
        if (dockingPane.getFloatingGroups().stream()
                .anyMatch(group -> group.getTabs().getDockableIds().contains(selectedId))) {
            return;
        }
        String id = selectedId;
        for (Node node = (Node) event.getTarget(); node != null && node != strip; node = node.getParent()) {
            if (node == headerAccessory) {
                return;
            }
            if (node instanceof ButtonBase || node instanceof TextInputControl) {
                return;
            }
            if (node.getParent() == strip) {
                id = (String) node.getProperties().get(DOCKABLE_ID_KEY);
            }
        }
        Dockable maximized = dockingPane.getMaximizedDockable();
        dockingPane.setMaximizedDockable(maximized != null && containsDockable(maximized.getId())
                ? null : find(dockingPane, id));
        event.consume();
    }

    /**
     * Activates on a completed click before header controls handle the release. Unselected titles
     * retain their own selection and drag behavior, and content clicks are handled on press.
     */
    private void activateFromHeaderOrBackground(MouseEvent event) {
        if (!event.isStillSincePress() || !(event.getTarget() instanceof Node)) {
            return;
        }
        for (Node node = (Node) event.getTarget(); node != null && node != this; node = node.getParent()) {
            if (node == contentArea || (node.getParent() == strip && node != titleOf(selectedId))) {
                return;
            }
        }
        activateSelectedDockable();
    }

    private void activateSelectedDockable() {
        Dockable active = dockingPane.getActiveDockable();
        if (active == null || !selectedId.equals(active.getId())) {
            dockingPane.selectDockable(selectedId);
        }
    }

    void updateSelection(String id) {
        if (selectedId.equals(id)) {
            return;
        }
        selectedId = id;
        for (Node title : strip.getChildren()) {
            title.pseudoClassStateChanged(SELECTED, id.equals(title.getProperties().get(DOCKABLE_ID_KEY)));
        }
        Dockable dockable = find(dockingPane, id);
        if (dockable == null) {
            contentArea.getChildren().clear();
        } else {
            contentArea.getChildren().setAll(dockable.getContent());
        }
    }

    boolean containsDockable(String id) {
        return ids.contains(id);
    }

    Node titleOf(String id) {
        for (Node title : strip.getChildren()) {
            if (id.equals(title.getProperties().get(DOCKABLE_ID_KEY))) {
                return title;
            }
        }
        return null;
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
        title.pseudoClassStateChanged(SELECTED, selected);
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

        // Select on click so pressing an unselected tab can still initiate a drag.
        title.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (!e.isConsumed() && e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                dockingPane.selectDockable(dockable.getId());
            }
        });
        title.addEventHandler(KeyEvent.KEY_PRESSED, e -> handleKey(dockable, e));
        return title;
    }

    private void handleKey(Dockable dockable, KeyEvent event) {
        if (event.isConsumed()) {
            return;
        }
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
