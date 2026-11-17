package com.dlsc.gemsfx.docking.model;

import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPosition;
import com.dlsc.gemsfx.docking.DockingSize;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared preferred-size allocation for docking operations and their previews.
 */
public final class DockingSizing {

    private DockingSizing() {
    }

    /**
     * Sizes a newly inserted side group. Returns false only when pixel sizing must await measurement.
     * Relative splits exchange space with their target only; pane-edge splits share the remainder
     * proportionally among all other children.
     */
    public static boolean apply(DockingTree tree, String id, String targetId, DockingPosition position,
                                DockingSize size, double width, double height) {
        if (size == null || position == DockingPosition.CENTER || !(tree.root() instanceof DockingSplit)) {
            return true;
        }
        boolean horizontal = position == DockingPosition.LEFT || position == DockingPosition.RIGHT;
        double extent = horizontal ? width : height;
        if (size.getUnit() == DockingSize.Unit.PIXELS && extent <= 0) {
            return false;
        }
        double weight = size.getUnit() == DockingSize.Unit.WEIGHT ? size.getValue() : size.getValue() / extent;
        resize(tree.root(), id, targetId, horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL, weight, 1);
        return true;
    }

    private static void resize(DockingTree.Node node, String id, String targetId,
                               Orientation orientation, double weight, double span) {
        if (!(node instanceof DockingSplit)) {
            return;
        }
        DockingSplit split = (DockingSplit) node;
        List<DockingTree.Node> children = split.getChildren();
        List<Double> shares = new ArrayList<>();
        double start = 0;
        int inserted = -1;
        int target = -1;
        for (int i = 0; i < children.size(); i++) {
            double end = i < split.getDividerPositions().size() ? split.getDividerPositions().get(i) : 1;
            shares.add(end - start);
            start = end;
            DockingTree.Node child = children.get(i);
            if (child instanceof DockingTabGroup && ((DockingTabGroup) child).getDockableIds().contains(id)) {
                inserted = i;
            }
            if (contains(child, targetId)) {
                target = i;
            }
        }
        if (inserted >= 0 && split.getOrientation() == orientation) {
            double oldShare = shares.get(inserted);
            double available = target >= 0 && target != inserted ? oldShare + shares.get(target) : 1;
            double requested = Math.max(0, Math.min(available, span > 0 ? weight / span : available));
            if (target >= 0 && target != inserted) {
                shares.set(target, available - requested);
            } else {
                for (int i = 0; i < children.size(); i++) {
                    if (i != inserted) {
                        shares.set(i, oldShare < 1 ? shares.get(i) * (1 - requested) / (1 - oldShare)
                                : (1 - requested) / (children.size() - 1));
                    }
                }
            }
            shares.set(inserted, requested);
            List<Double> positions = new ArrayList<>();
            double sum = 0;
            for (int i = 0; i < shares.size() - 1; i++) {
                sum = Math.min(1, sum + shares.get(i));
                positions.add(sum);
            }
            split.setDividerPositions(positions);
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            if (contains(children.get(i), id)) {
                resize(children.get(i), id, targetId, orientation, weight,
                        split.getOrientation() == orientation ? span * shares.get(i) : span);
                return;
            }
        }
    }

    private static boolean contains(DockingTree.Node node, String id) {
        if (id == null) {
            return false;
        }
        if (node instanceof DockingTabGroup) {
            return ((DockingTabGroup) node).getDockableIds().contains(id);
        }
        return ((DockingSplit) node).getChildren().stream().anyMatch(child -> contains(child, id));
    }

    /**
     * Simulates the side move without touching the live tree, including source-group removal.
     */
    public static Rectangle2D preview(DockingLayoutNode root, List<String> sourceIds, Dockable dockable,
                                      String targetId, DockingPosition position, double width, double height) {
        DockingTree tree = new DockingTree();
        tree.restore(root);
        sourceIds.forEach(tree::remove);
        if (targetId == null) {
            tree.insertAtPaneEdge(dockable, position, 0.5);
        } else {
            tree.insertRelativeTo(targetId, dockable, position, 0.5);
        }
        apply(tree, dockable.getId(), targetId, position, dockable.getPreferredDockingSize(), width, height);
        return bounds(tree.root(), dockable.getId(), new Rectangle2D(0, 0, width, height));
    }

    private static Rectangle2D bounds(DockingTree.Node node, String id, Rectangle2D area) {
        if (node instanceof DockingTabGroup) {
            return area;
        }
        DockingSplit split = (DockingSplit) node;
        double start = 0;
        for (int i = 0; i < split.getChildren().size(); i++) {
            double end = i < split.getDividerPositions().size() ? split.getDividerPositions().get(i) : 1;
            DockingTree.Node child = split.getChildren().get(i);
            if (contains(child, id)) {
                return bounds(child, id, split.getOrientation() == Orientation.HORIZONTAL
                        ? new Rectangle2D(area.getMinX() + start * area.getWidth(), area.getMinY(),
                        (end - start) * area.getWidth(), area.getHeight())
                        : new Rectangle2D(area.getMinX(), area.getMinY() + start * area.getHeight(),
                        area.getWidth(), (end - start) * area.getHeight()));
            }
            start = end;
        }
        throw new IllegalArgumentException("Unknown dockable: " + id);
    }
}
