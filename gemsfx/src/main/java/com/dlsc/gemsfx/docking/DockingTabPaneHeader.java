package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.docking.model.DockingTabGroup;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Objects;

/**
 * Default tab group header menu, offering close, float, and maximize actions.
 * Applications can create or subclass it in a {@link DockingContext#tabPaneHeaderFactoryProperty() header factory}.
 */
public class DockingTabPaneHeader extends MenuButton {

    /**
     * Creates a menu for the given tab group.
     *
     * @param pane the non-null owning pane
     * @param dockables the non-null, non-empty group of non-null dockables, in tab order; copied at construction
     * @throws IllegalArgumentException if the group is empty
     * @throws NullPointerException if the pane, list, or any dockable is null
     */
    public DockingTabPaneHeader(DockingPane pane, List<Dockable> dockables) {
        Objects.requireNonNull(pane, "pane must not be null");
        List<Dockable> groupDockables = List.copyOf(dockables);
        if (groupDockables.isEmpty()) {
            throw new IllegalArgumentException("dockables must not be empty");
        }
        DockingContext context = pane.getDockingContext();
        ResourceBundleManager.BundleType bundle = ResourceBundleManager.BundleType.DOCKING_PANE;
        getStyleClass().add("docking-tab-pane-menu-button");
        setGraphic(new FontIcon("bi-chevron-down"));
        String accessibleText = ResourceBundleManager.getString(bundle, "accessible.text.group-actions", "Tab group actions");
        setAccessibleText(accessibleText);
        setTooltip(new Tooltip(accessibleText));

        MenuItem closeAll = new MenuItem(ResourceBundleManager.getString(bundle, "menu.close-all", "Close All"));
        closeAll.getStyleClass().add("docking-group-close-all");
        closeAll.setOnAction(event -> {
            DockingTabGroup group = pane.groupOf(groupDockables.get(0).getId());
            if (group != null) {
                for (Dockable dockable : groupDockables) {
                    if (pane.groupOf(dockable.getId()) == group) {
                        context.requestCloseDockable(dockable);
                    }
                }
            }
        });

        MenuItem floatGroup = new MenuItem(ResourceBundleManager.getString(bundle, "menu.float-group", "Float Group"));
        floatGroup.getStyleClass().add("docking-group-float");
        floatGroup.visibleProperty().bind(context.floatingEnabledProperty());
        floatGroup.setOnAction(event -> {
            String id = groupDockables.get(0).getId();
            if (context.isFloatingEnabled() && pane.groupOf(id) != null) {
                Bounds bounds = localToScreen(getBoundsInLocal());
                context.floatTabGroup(id, bounds == null ? 0 : bounds.getMinX(), bounds == null ? 0 : bounds.getMaxY());
            }
        });

        String maximizeText = ResourceBundleManager.getString(bundle, "menu.maximize-group", "Maximize Group");
        String restoreText = ResourceBundleManager.getString(bundle, "menu.restore-group", "Restore Group");
        MenuItem maximize = new MenuItem();
        maximize.getStyleClass().add("docking-group-maximize");
        maximize.textProperty().bind(Bindings.createStringBinding(
                () -> pane.getMaximizedDockable() != null && groupDockables.contains(pane.getMaximizedDockable())
                        ? restoreText : maximizeText,
                pane.maximizedDockableProperty()));
        maximize.setOnAction(event -> {
            DockingTabGroup group = pane.groupOf(groupDockables.get(0).getId());
            if (group != null) {
                pane.setMaximizedDockable(pane.getMaximizedDockable() != null
                        && groupDockables.contains(pane.getMaximizedDockable())
                        ? null : pane.selectedGroups().get(group));
            }
        });
        getItems().addAll(closeAll, floatGroup, maximize);
    }
}
