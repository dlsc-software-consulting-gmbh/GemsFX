package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockingLayoutNode;
import com.dlsc.gemsfx.DockingPane;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import com.dlsc.gemsfx.util.ResourceBundleManager.BundleType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.List;

/**
 * Renders one tab group: a strip with one title control per dockable (each with its own close button) above the
 * content of the selected dockable only. This deliberately does not use the JavaFX {@code TabPane}.
 */
final class DockingTabPane extends VBox {

    static final String DOCKABLE_ID_KEY = "docking.dockable-id";
    private static final Object TITLE_ACCESSIBLE_TEXT_BINDING_KEY = new Object();

    private final DockingPane dockingPane;
    private final List<String> ids;
    private final String closePattern;
    private final String closeTooltip;

    DockingTabPane(DockingPane dockingPane, DockingLayoutNode.Tabs tabs) {
        this.dockingPane = dockingPane;
        this.ids = tabs.getDockableIds();
        this.closePattern = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "accessible.text.close", "Close {0}");
        this.closeTooltip = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "tooltip.close", "Close");
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
        HBox title = new HBox();
        title.getStyleClass().add("docking-tab");
        if (selected) {
            title.getStyleClass().add("selected");
        }
        title.setAlignment(Pos.CENTER_LEFT);
        title.setMinWidth(0);
        title.setPrefWidth(0);
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setFocusTraversable(true);
        title.getProperties().put(DOCKABLE_ID_KEY, dockable.getId());
        AccessibilityUtil.setRole(title, AccessibleRole.TAB_ITEM);
        StringBinding accessibleText = Bindings.createStringBinding(dockable::getTitle, dockable.titleProperty());
        AccessibilityUtil.bindAccessibleText(title, accessibleText);
        title.getProperties().put(TITLE_ACCESSIBLE_TEXT_BINDING_KEY, accessibleText);

        Label label = new Label();
        label.textProperty().bind(dockable.titleProperty());
        label.graphicProperty().bind(dockable.graphicProperty());
        label.getStyleClass().add("docking-tab-title");
        label.setMnemonicParsing(false);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);

        Button close = new Button();
        close.getStyleClass().add("docking-tab-close-button");
        close.setFocusTraversable(false);
        Region icon = new Region();
        icon.getStyleClass().add("icon");
        close.setGraphic(icon);
        close.setTooltip(new Tooltip(closeTooltip));
        close.accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> MessageFormat.format(closePattern, dockable.getTitle()), dockable.titleProperty()));
        close.setOnAction(e -> dockingPane.getDockingContext().requestCloseDockable(dockable));
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        title.getChildren().addAll(label, close);
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
