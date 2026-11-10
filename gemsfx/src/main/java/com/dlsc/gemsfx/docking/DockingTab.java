package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import com.dlsc.gemsfx.util.ResourceBundleManager.BundleType;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Default docking tab header, displaying a dockable's title, graphic, and close button.
 * Selection, keyboard navigation, and docking drag-and-drop are installed by the workspace.
 * Applications can create or subclass this header in a {@link DockingContext#tabFactoryProperty() tab factory}.
 */
public class DockingTab extends HBox {

    /**
     * Creates a header whose close button requests closure through the given context.
     *
     * @param context the non-null context managing the dockable
     * @param dockable the non-null dockable represented by this header
     */
    public DockingTab(DockingContext context, Dockable dockable) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(dockable, "dockable must not be null");
        getStyleClass().add("docking-tab");
        setAlignment(Pos.CENTER_LEFT);
        AccessibilityUtil.setRole(this, AccessibleRole.TAB_ITEM);

        Tooltip titleTooltip = new Tooltip();
        titleTooltip.textProperty().bind(dockable.titleProperty());
        Label label = new Label() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                // Text measurements are current only after the skin has laid out the label.
                setTooltip(getWidth() < prefWidth(-1) ? titleTooltip : null);
            }
        };
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
        close.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), close);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        hoverProperty().addListener((obs, wasHovered, hovered) -> {
            if (hovered) {
                fadeIn.playFromStart();
            } else {
                fadeIn.stop();
                close.setOpacity(0);
            }
        });
        Region icon = new Region();
        icon.getStyleClass().add("icon");
        close.setGraphic(icon);
        close.setTooltip(new Tooltip(ResourceBundleManager.getString(BundleType.DOCKING_PANE, "tooltip.close", "Close")));
        String closePattern = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "accessible.text.close", "Close {0}");
        close.accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> MessageFormat.format(closePattern, dockable.getTitle()), dockable.titleProperty()));
        close.setOnAction(event -> context.requestCloseDockable(dockable));
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        getChildren().addAll(label, close);
    }
}
