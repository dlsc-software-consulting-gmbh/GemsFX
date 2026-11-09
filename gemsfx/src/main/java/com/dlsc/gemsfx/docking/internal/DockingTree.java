package com.dlsc.gemsfx.docking.internal;

import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockingLayoutNode;
import com.dlsc.gemsfx.DockingPosition;
import javafx.geometry.Orientation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The pane tree of a docking pane: exactly one root, which is either an (empty) tab group
 * or a split. Dockables are referenced by ID only; an ID occurs at most once per tree.
 * Cross-pane uniqueness is enforced by the docking context.
 */
public final class DockingTree {

    private static final double EDGE_BAND_RATIO = 0.25;

    public static final class DropTarget {
        private final String targetId;
        private final DockingPosition position;

        public DropTarget(String targetId, DockingPosition position) {
            this.targetId = Objects.requireNonNull(targetId, "targetId must not be null");
            this.position = Objects.requireNonNull(position, "position must not be null");
        }

        public String getTargetId() {
            return targetId;
        }

        public DockingPosition getPosition() {
            return position;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DropTarget)) {
                return false;
            }
            DropTarget other = (DropTarget) obj;
            return targetId.equals(other.targetId) && position == other.position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetId, position);
        }

        @Override
        public String toString() {
            return "DropTarget{targetId='" + targetId + "', position=" + position + "}";
        }
    }

    public static final class ResolvedDropTarget {
        private final DropTarget dropTarget;
        private final double leafX;
        private final double leafY;
        private final double leafWidth;
        private final double leafHeight;
        private final double zoneX;
        private final double zoneY;
        private final double zoneWidth;
        private final double zoneHeight;

        private ResolvedDropTarget(DropTarget dropTarget,
                                   double leafX, double leafY, double leafWidth, double leafHeight,
                                   double zoneX, double zoneY, double zoneWidth, double zoneHeight) {
            this.dropTarget = Objects.requireNonNull(dropTarget, "dropTarget must not be null");
            this.leafX = leafX;
            this.leafY = leafY;
            this.leafWidth = leafWidth;
            this.leafHeight = leafHeight;
            this.zoneX = zoneX;
            this.zoneY = zoneY;
            this.zoneWidth = zoneWidth;
            this.zoneHeight = zoneHeight;
        }

        public DropTarget getDropTarget() {
            return dropTarget;
        }

        public String getTargetId() {
            return dropTarget.getTargetId();
        }

        public DockingPosition getPosition() {
            return dropTarget.getPosition();
        }

        public double getLeafX() {
            return leafX;
        }

        public double getLeafY() {
            return leafY;
        }

        public double getLeafWidth() {
            return leafWidth;
        }

        public double getLeafHeight() {
            return leafHeight;
        }

        public double getZoneX() {
            return zoneX;
        }

        public double getZoneY() {
            return zoneY;
        }

        public double getZoneWidth() {
            return zoneWidth;
        }

        public double getZoneHeight() {
            return zoneHeight;
        }
    }

    /**
     * Marker for the node types of the tree: {@link DockingTabGroup} and {@link DockingSplit}.
     */
    public interface Node {
    }

    private Node root = new DockingTabGroup();

    public Node root() {
        return root;
    }

    public List<String> dockableIds() {
        List<String> ids = new ArrayList<>();
        collect(root, ids);
        return ids;
    }

    public boolean contains(String id) {
        return dockableIds().contains(id);
    }

    /**
     * Adds the dockable as selected tab to the first tab group of the tree.
     */
    public void addToRoot(Dockable dockable) {
        String id = requireNew(dockable);
        firstGroup(root).add(id);
    }

    /**
     * Inserts the dockable relative to an existing one: as tab for {@link DockingPosition#CENTER},
     * otherwise as new split on the given edge of the existing dockable's tab group.
     */
    public void splitAt(String existingId, Dockable inserted, DockingPosition edge, double dividerPosition) {
        String id = requireNew(inserted);
        insert(existingId, id, edge, dividerPosition);
    }

    /**
     * Removes a dockable and collapses empty groups and one-child splits.
     *
     * @return true if the dockable was part of the tree
     */
    public boolean remove(String id) {
        if (!contains(id)) {
            return false;
        }
        root = removeFrom(root, id);
        if (root == null) {
            root = new DockingTabGroup();
        }
        return true;
    }

    public void select(String id) {
        groupOf(id).select(id);
    }

    /**
     * Moves a tab to a new index within its tab group.
     */
    public void moveTab(String id, int index) {
        groupOf(id).moveTab(id, index);
    }

    /**
     * Moves a dockable of this tree relative to another one of this tree.
     */
    public void move(String id, String targetId, DockingPosition edge, double dividerPosition) {
        if (id.equals(targetId)) {
            throw new IllegalArgumentException("Cannot move a dockable relative to itself: " + id);
        }
        groupOf(id);
        groupOf(targetId);
        requireDivider(dividerPosition);
        requireEdge(edge);
        remove(id);
        insert(targetId, id, edge, dividerPosition);
    }

    public boolean canMove(String id, DropTarget target) {
        if (target == null || !contains(id) || !contains(target.getTargetId()) || id.equals(target.getTargetId())) {
            return false;
        }
        return canInsertRelativeTo(target.getTargetId(), target.getPosition());
    }

    public boolean tryMove(String id, DropTarget target, double dividerPosition) {
        if (!canMove(id, target)) {
            return false;
        }
        if (target.getPosition() == DockingPosition.CENTER && inSameGroup(id, target.getTargetId())) {
            reorderAround(id, target.getTargetId());
            return true;
        }
        move(id, target.getTargetId(), target.getPosition(), dividerPosition);
        return true;
    }

    public boolean canInsertRelativeTo(String targetId, DockingPosition position) {
        if (!contains(targetId) || position == null) {
            return false;
        }
        if (position == DockingPosition.CENTER) {
            return true;
        }
        return position == DockingPosition.LEFT
                || position == DockingPosition.RIGHT
                || position == DockingPosition.TOP
                || position == DockingPosition.BOTTOM;
    }

    public void insertRelativeTo(String targetId, Dockable inserted, DockingPosition edge, double dividerPosition) {
        String id = requireNew(inserted);
        insert(targetId, id, edge, dividerPosition);
    }

    /**
     * Sets the divider position of the split addressed by a path of child indices (0 = first, 1 = second)
     * from the root; an empty path addresses the root.
     */
    public void setDividerPosition(List<Integer> path, double dividerPosition) {
        requireDivider(dividerPosition);
        Node node = root;
        for (int index : path) {
            if (!(node instanceof DockingSplit) || (index != 0 && index != 1)) {
                throw new IllegalArgumentException("Path does not address a split: " + path);
            }
            node = index == 0 ? ((DockingSplit) node).getFirst() : ((DockingSplit) node).getSecond();
        }
        if (!(node instanceof DockingSplit)) {
            throw new IllegalArgumentException("Path does not address a split: " + path);
        }
        ((DockingSplit) node).setDividerPosition(dividerPosition);
    }

    /**
     * Creates a validated immutable snapshot of the tree.
     */
    public DockingLayoutNode snapshot() {
        return snapshot(root);
    }

    public static DropTarget resolveDropTarget(DockingLayoutNode root, double x, double y, double width, double height) {
        ResolvedDropTarget resolved = resolveDropTarget(root, x, y, width, height, null);
        return resolved == null ? null : resolved.getDropTarget();
    }

    public static ResolvedDropTarget resolveDropTarget(DockingLayoutNode root, double x, double y,
                                                        double width, double height, String hoveredId) {
        return resolveDropTarget(root, x, y, width, height, hoveredId, null);
    }

    public static ResolvedDropTarget resolveDropTarget(DockingLayoutNode root, double x, double y,
                                                        double width, double height, String hoveredId, String draggedId) {
        if (root == null || !(width > 0) || !(height > 0) || !Double.isFinite(x) || !Double.isFinite(y)) {
            return null;
        }
        DropLeaf leaf = resolveLeaf(root, x, y, 0, 0, width, height);
        if (leaf == null || leaf.selectedId == null) {
            return null;
        }
        String targetId = hoveredId != null && leaf.dockableIds.contains(hoveredId) ? hoveredId : leaf.selectedId;
        double localX = x - leaf.minX;
        double localY = y - leaf.minY;
        DockingPosition edge = resolveEdge(localX, localY, leaf.width, leaf.height);
        if (edge != DockingPosition.CENTER && targetId.equals(draggedId)) {
            for (String id : leaf.dockableIds) {
                if (!id.equals(draggedId)) {
                    targetId = id;
                    break;
                }
            }
        }
        Zone zone = resolveZone(edge, leaf.width, leaf.height);
        return new ResolvedDropTarget(new DropTarget(targetId, edge),
                leaf.minX, leaf.minY, leaf.width, leaf.height,
                leaf.minX + zone.x, leaf.minY + zone.y, zone.width, zone.height);
    }

    public static void validateSnapshotNode(DockingLayoutNode node) {
        Objects.requireNonNull(node, "node must not be null");
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            requireDivider(split.getDividerPosition());
            validateSnapshotNode(split.getFirst());
            validateSnapshotNode(split.getSecond());
        }
    }

    private static DockingLayoutNode snapshot(Node node) {
        if (node instanceof DockingSplit) {
            DockingSplit split = (DockingSplit) node;
            return new DockingLayoutNode.Split(split.getOrientation(), split.getDividerPosition(),
                    snapshot(split.getFirst()), snapshot(split.getSecond()));
        }
        DockingTabGroup group = (DockingTabGroup) node;
        if (group.isEmpty()) {
            return new DockingLayoutNode.Empty();
        }
        return new DockingLayoutNode.Tabs(group.getDockableIds(), group.getSelectedId());
    }

    private boolean inSameGroup(String id, String targetId) {
        return groupOf(id) == groupOf(targetId);
    }

    private void reorderAround(String id, String targetId) {
        DockingTabGroup group = groupOf(id);
        List<String> ids = group.ids();
        int from = ids.indexOf(id);
        int to = ids.indexOf(targetId);
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Unknown dockable id");
        }
        int insertionIndex = from < to ? to : to + 1;
        if (insertionIndex >= ids.size()) {
            insertionIndex = ids.size() - 1;
        }
        group.moveTab(id, insertionIndex);
        group.select(id);
    }

    private String requireNew(Dockable dockable) {
        String id = Objects.requireNonNull(dockable, "dockable must not be null").getId();
        if (contains(id)) {
            throw new IllegalArgumentException("Duplicate dockable id: " + id);
        }
        return id;
    }

    private void insert(String existingId, String id, DockingPosition edge, double dividerPosition) {
        Objects.requireNonNull(edge, "edge must not be null");
        requireDivider(dividerPosition);
        DockingTabGroup target = groupOf(existingId);
        if (edge == DockingPosition.CENTER) {
            target.add(id);
            return;
        }
        DockingTabGroup created = new DockingTabGroup();
        created.add(id);
        boolean insertedFirst = edge == DockingPosition.LEFT || edge == DockingPosition.TOP;
        Orientation orientation = edge == DockingPosition.LEFT || edge == DockingPosition.RIGHT
                ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        DockingSplit split = insertedFirst
                ? new DockingSplit(orientation, dividerPosition, created, target)
                : new DockingSplit(orientation, dividerPosition, target, created);
        root = replace(root, target, split);
    }

    private static void requireDivider(double position) {
        if (!(position >= 0 && position <= 1)) {
            throw new IllegalArgumentException("dividerPosition must be within [0, 1]: " + position);
        }
    }

    private static void requireEdge(DockingPosition edge) {
        Objects.requireNonNull(edge, "edge must not be null");
    }

    private DockingTabGroup groupOf(String id) {
        DockingTabGroup group = find(root, id);
        if (group == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        return group;
    }

    private static DockingTabGroup find(Node node, String id) {
        if (node instanceof DockingSplit) {
            DockingSplit split = (DockingSplit) node;
            DockingTabGroup found = find(split.getFirst(), id);
            return found != null ? found : find(split.getSecond(), id);
        }
        DockingTabGroup group = (DockingTabGroup) node;
        return group.ids().contains(id) ? group : null;
    }

    private static DockingTabGroup firstGroup(Node node) {
        return node instanceof DockingSplit ? firstGroup(((DockingSplit) node).getFirst()) : (DockingTabGroup) node;
    }

    private static void collect(Node node, List<String> ids) {
        if (node instanceof DockingSplit) {
            collect(((DockingSplit) node).getFirst(), ids);
            collect(((DockingSplit) node).getSecond(), ids);
        } else {
            ids.addAll(((DockingTabGroup) node).ids());
        }
    }

    private static DropLeaf resolveLeaf(DockingLayoutNode node, double x, double y,
                                        double minX, double minY, double width, double height) {
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            if (split.getOrientation() == Orientation.HORIZONTAL) {
                double firstWidth = width * split.getDividerPosition();
                if (x <= minX + firstWidth) {
                    return resolveLeaf(split.getFirst(), x, y, minX, minY, firstWidth, height);
                }
                return resolveLeaf(split.getSecond(), x, y, minX + firstWidth, minY, width - firstWidth, height);
            }
            double firstHeight = height * split.getDividerPosition();
            if (y <= minY + firstHeight) {
                return resolveLeaf(split.getFirst(), x, y, minX, minY, width, firstHeight);
            }
            return resolveLeaf(split.getSecond(), x, y, minX, minY + firstHeight, width, height - firstHeight);
        }
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) node;
            return new DropLeaf(tabs.getDockableIds(), tabs.getSelectedId(), minX, minY, width, height);
        }
        return null;
    }

    private static DockingPosition resolveEdge(double x, double y, double width, double height) {
        double left = x;
        double right = width - x;
        double top = y;
        double bottom = height - y;
        double minHorizontal = Math.min(left, right);
        double minVertical = Math.min(top, bottom);
        double horizontalBand = width * EDGE_BAND_RATIO;
        double verticalBand = height * EDGE_BAND_RATIO;
        if (minHorizontal > horizontalBand && minVertical > verticalBand) {
            return DockingPosition.CENTER;
        }
        if (minHorizontal <= minVertical) {
            return left <= right ? DockingPosition.LEFT : DockingPosition.RIGHT;
        }
        return top <= bottom ? DockingPosition.TOP : DockingPosition.BOTTOM;
    }

    private static Zone resolveZone(DockingPosition edge, double width, double height) {
        double bandWidth = width * EDGE_BAND_RATIO;
        double bandHeight = height * EDGE_BAND_RATIO;
        switch (edge) {
            case LEFT:
                return new Zone(0, 0, bandWidth, height);
            case RIGHT:
                return new Zone(width - bandWidth, 0, bandWidth, height);
            case TOP:
                return new Zone(0, 0, width, bandHeight);
            case BOTTOM:
                return new Zone(0, height - bandHeight, width, bandHeight);
            default:
                return new Zone(bandWidth, bandHeight, width - 2 * bandWidth, height - 2 * bandHeight);
        }
    }

    private static final class DropLeaf {
        private final List<String> dockableIds;
        private final String selectedId;
        private final double minX;
        private final double minY;
        private final double width;
        private final double height;

        private DropLeaf(List<String> dockableIds, String selectedId, double minX, double minY, double width, double height) {
            this.dockableIds = dockableIds;
            this.selectedId = selectedId;
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
        }
    }

    private static final class Zone {
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        private Zone(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static Node replace(Node node, Node target, Node replacement) {
        if (node == target) {
            return replacement;
        }
        if (node instanceof DockingSplit) {
            DockingSplit s = (DockingSplit) node;
            return new DockingSplit(s.getOrientation(), s.getDividerPosition(),
                    replace(s.getFirst(), target, replacement), replace(s.getSecond(), target, replacement));
        }
        return node;
    }

    private static Node removeFrom(Node node, String id) {
        if (node instanceof DockingSplit) {
            DockingSplit s = (DockingSplit) node;
            Node first = removeFrom(s.getFirst(), id);
            Node second = removeFrom(s.getSecond(), id);
            if (first == null) {
                return second;
            }
            if (second == null) {
                return first;
            }
            return new DockingSplit(s.getOrientation(), s.getDividerPosition(), first, second);
        }
        DockingTabGroup group = (DockingTabGroup) node;
        if (group.ids().contains(id)) {
            group.remove(id);
        }
        return group.isEmpty() ? null : group;
    }
}
