package com.dlsc.gemsfx.treeview;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TreeNodeViewSkin<T> extends SkinBase<TreeNodeView<T>> {
    private final Map<TreeNode<T>, InvalidationListener> expandListenerMap = new HashMap<>();
    private final Map<TreeNode<T>, InvalidationListener> invailidateListenerMap = new HashMap<>();
    private final Map<TreeNode<T>, ListChangeListener<TreeNode<T>>> childrenListListenerMap = new HashMap<>();

    private final Map<TreeNode<T>, List<Node>> nodeToComponentsMap = new HashMap<>();
    /**
     * The position of the nodes
     */
    private final Map<TreeNode<T>, Point2D> nodeToPositionMap = new HashMap<>();
    /**
     * A map storing the total dimension (either width or height based on the layout direction) of each node.
     * - For LEFT_TO_RIGHT or RIGHT_TO_LEFT layouts, it represents the total height of a node, which includes the node itself and its maximum subtree height.
     * - For TOP_TO_BOTTOM or BOTTOM_TO_TOP layouts, it represents the total width of a node, including the node and its maximum subtree width.
     * The purpose of this map is to dynamically adapt to different layout directions and provide a consistent way to handle node dimensions.
     */
    private final Map<TreeNode<T>, Double> nodeTotalDimensionMap = new HashMap<>();

    /**
     * A map storing the maximum dimension (either width or height based on the layout direction) for each level (or depth) in the tree.
     * - For LEFT_TO_RIGHT or RIGHT_TO_LEFT layouts, it represents the maximum height for a particular depth or level.
     * - For TOP_TO_BOTTOM or BOTTOM_TO_TOP layouts, it represents the maximum width for a particular depth or level.
     * This map ensures that nodes on the same level do not overlap and provides consistent spacing across different layout directions.
     */
    private final Map<Integer, Double> levelToMaxDimensionMap = new HashMap<>();

    private final List<TreeNode<T>> currentLevelNodesCache = new ArrayList<>();

//    private List<Node> additionalLinkedNodeList = new ArrayList<>();

    private final Group contentGroup = new Group();

    public TreeNodeViewSkin(TreeNodeView<T> view) {
        super(view);
        contentGroup.getStyleClass().add("tree-content");
        getChildren().add(contentGroup);

        initTree();
        view.rootProperty().addListener((ob, ov, newRoot) -> initTree());

        ChangeListener<Object> buildTreeListener = (ob, ov, nv) -> buildTree();

        view.layoutTypeProperty().addListener(buildTreeListener);
        view.cellFactoryProperty().addListener(buildTreeListener);
        view.cellWidthProperty().addListener(buildTreeListener);
        view.cellHeightProperty().addListener(buildTreeListener);
        view.vgapProperty().addListener(buildTreeListener);
        view.hgapProperty().addListener(buildTreeListener);
        view.nodeLineGapProperty().addListener(buildTreeListener);
        view.rowAlignmentProperty().addListener(buildTreeListener);
        view.columnAlignmentProperty().addListener(buildTreeListener);
        view.linkStrategyProperty().addListener(buildTreeListener);
        view.layoutDirectionProperty().addListener(buildTreeListener);
        view.placeholderProperty().addListener((ob, ov, nv) -> {
            if (view.getRoot() == null) {
                contentGroup.getChildren().setAll(nv);
            }
        });
    }

    private void initTree() {
        buildTree();
        initNodeListeners();
    }

    private void addListenersToNode(TreeNode<T> node) {
        // Create and store the expansion listener
        InvalidationListener expandListener = createExpandListener(node);
        expandListenerMap.put(node, expandListener);
        node.expandedProperty().addListener(expandListener);

        // Create and store the invalidation listener
        InvalidationListener invalidationListener = it -> buildTree();
        invailidateListenerMap.put(node, invalidationListener);

        node.getChildren().addListener(invalidationListener);
        node.widthProperty().addListener(invalidationListener);
        node.heightProperty().addListener(invalidationListener);
        node.getLinkedNodes().addListener(invalidationListener);

        // Add listeners to the new nodes (if any) and remove listeners from removed nodes (if any) when they are added/removed to/from the children list.
        ListChangeListener<TreeNode<T>> nodeListChangeListener = createNodeListChangeListener();
        childrenListListenerMap.put(node, nodeListChangeListener);
        node.getChildren().addListener(nodeListChangeListener);
        node.getLinkedNodes().addListener(nodeListChangeListener);

    }

    private void removeListenersFromNode(TreeNode<T> removedNode) {
        InvalidationListener expandListener = expandListenerMap.remove(removedNode);
        if (expandListener != null) {
            removedNode.expandedProperty().removeListener(expandListener);
        }

        InvalidationListener invalidationListener = invailidateListenerMap.remove(removedNode);
        if (invalidationListener != null) {
            removedNode.getChildren().removeListener(invalidationListener);
            removedNode.widthProperty().removeListener(invalidationListener);
            removedNode.heightProperty().removeListener(invalidationListener);
            removedNode.getLinkedNodes().removeListener(invalidationListener);
        }

        ListChangeListener<TreeNode<T>> nodeListChangeListener = childrenListListenerMap.remove(removedNode);
        if (nodeListChangeListener != null) {
            removedNode.getChildren().removeListener(nodeListChangeListener);
            removedNode.getLinkedNodes().removeListener(nodeListChangeListener);
        }

        nodeToComponentsMap.remove(removedNode);
        nodeToPositionMap.remove(removedNode);
        nodeTotalDimensionMap.remove(removedNode);
    }

    private ListChangeListener<TreeNode<T>> createNodeListChangeListener() {
        return (ListChangeListener.Change<? extends TreeNode<T>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::addListenersToNode);
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(this::removeListenersFromNode);
                }
            }
        };
    }

    private void initNodeListeners() {
        TreeNode<T> rootNode = getSkinnable().getRoot();
        if (rootNode == null) {
            return;
        }
        rootNode.stream().forEach(this::addListenersToNode);
    }

    private InvalidationListener createExpandListener(TreeNode<T> node) {
        return it -> {
            toggleChildrenVisibility(node, node.isExpanded(), true);
            if (node.isExpanded()) {
                drawNode(node);
            }
        };
    }

    private void buildTree() {
        TreeNode<T> root = getSkinnable().getRoot();
        contentGroup.getChildren().clear();
        clearMapsForBuild();

        if (root != null) {
            calculatePositions(root);
            drawNode(root);
            drawAdditionalLinkedNodes();
        } else {
            contentGroup.getChildren().setAll(getSkinnable().getPlaceholder());
        }
    }

    private void calculatePositions(TreeNode<T> root) {
        if (getSkinnable().getLayoutType() == TreeNodeView.LayoutType.REGULAR) {
            positionNodesRegular(root);
        } else {
            positionNodesCompact(root);
        }
    }

    private void positionNodesCompact(TreeNode<T> root) {
        computeAndCacheNodeDimension(root);

        TreeNodeView.LayoutDirection layoutDirection = getSkinnable().getLayoutDirection();
        if (layoutDirection == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT || layoutDirection == TreeNodeView.LayoutDirection.RIGHT_TO_LEFT) {
            positionNodesCompactHorizontally(root, findMaxColumnHeight(root), layoutDirection);
        } else {
            positionNodesCompactVertically(root, findMaxRowWidth(root), layoutDirection);
        }
    }

    /**
     * Calculate the width of the widest row/level in the tree.
     */
    private double findMaxRowWidth(TreeNode<T> root) {
        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);
        double maxRowWidth = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            double currentRowWidth = 0;
            for (int i = 0; i < size; i++) {
                TreeNode<T> node = queue.poll();
                if (node == null) {
                    continue;
                }
                currentRowWidth += computeNodeWidth(node);
                if (node.isExpanded()) {
                    for (TreeNode<T> child : node.getChildren()) {
                        queue.offer(child);
                    }
                }
            }
            // Add the horizontal gaps
            currentRowWidth += (size - 1) * getSkinnable().getHgap();
            maxRowWidth = Math.max(maxRowWidth, currentRowWidth);
        }
        return maxRowWidth;
    }

    private double findMaxColumnHeight(TreeNode<T> root) {
        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);
        double maxColumnHeight = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            double currentColumnHeight = 0;
            for (int i = 0; i < size; i++) {
                TreeNode<T> node = queue.poll();
                if (node == null) {
                    continue;
                }
                currentColumnHeight += computeNodeHeight(node);
                if (node.isExpanded()) {
                    for (TreeNode<T> child : node.getChildren()) {
                        queue.offer(child);
                    }
                }
            }
            // Add the vertical gaps
            currentColumnHeight += (size - 1) * getSkinnable().getVgap();
            maxColumnHeight = Math.max(maxColumnHeight, currentColumnHeight);
        }
        return maxColumnHeight;
    }

    private void drawNode(TreeNode<T> node) {
        TreeNodeView<T> view = getSkinnable();
        if (!nodeToPositionMap.containsKey(node)) {
            calculatePositions(view.getRoot());
        }

        Point2D point = nodeToPositionMap.get(node);
        if (point == null) {
            return;
        }

        TreeNodeCell<T> cell = view.getCellFactory().call(node.getValue());
        cell.setTreeNode(node);
        cell.setPrefSize(computeNodeWidth(node), computeNodeHeight(node));
        cell.setLayoutX(point.getX());
        cell.setLayoutY(point.getY());
        contentGroup.getChildren().add(cell);

        TreeNode<T> parent = node.getParent();
        if (parent != null) {
            Point2D parentPoint = nodeToPositionMap.get(parent);
            if (parentPoint == null) {
                return;
            }
            List<Node> nodes = view.getLinkStrategy().drawNodeLink(getSkinnable().getLayoutDirection(), levelToMaxDimensionMap.get(node.getLevel()), parent, parentPoint, computeNodeWidth(parent), computeNodeHeight(parent), node, point, computeNodeWidth(node), computeNodeHeight(node), view.getNodeLineGap(), view.getVgap(), view.getHgap());
            if (parent.getName() != null && node.getName() != null) {
                nodes.forEach(n -> n.getStyleClass().add("link-" + parent.getName() + "-" + node.getName()));
            }

            contentGroup.getChildren().addAll(nodes);
            nodes.add(cell);
            nodeToComponentsMap.put(node, nodes);
        } else {
            nodeToComponentsMap.put(node, List.of(cell));
        }

        for (TreeNode<T> child : node.getChildren()) {
            if (node.isExpanded()) {
                drawNode(child);
            }
        }
    }

    private void positionNodesRegular(TreeNode<T> root) {
        nodeToPositionMap.clear();
        levelToMaxDimensionMap.clear();
        computeAndCacheNodeDimension(root);
        TreeNodeView.LayoutDirection layoutDirection = getSkinnable().getLayoutDirection();
        if (layoutDirection == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT || layoutDirection == TreeNodeView.LayoutDirection.RIGHT_TO_LEFT) {
            positionNodesRegularHorizontally(root, 0, 0, layoutDirection);
        } else {
            positionNodesRegularVertically(root, 0, 0, layoutDirection);
        }
    }

    private void positionNodesCompactVertically(TreeNode<T> root, double maxRowWidth, TreeNodeView.LayoutDirection direction) {
        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);
        double currentPositionY = 0;

        double hgap = getSkinnable().getHgap();
        double vgap = getSkinnable().getVgap();
        double nodeLineGaps = getSkinnable().getNodeLineGap() * 2;
        VPos alignment = getSkinnable().getRowAlignment();

        while (!queue.isEmpty()) {
            int size = queue.size();
            double currentRowWidth = 0;
            double currentLevelMaxHeight = 0;
            double currentLevelTotalHeight;

            currentLevelNodesCache.clear();

            for (int i = 0; i < size; i++) {
                TreeNode<T> node = queue.poll();
                if (node == null) {
                    continue;
                }
                currentRowWidth += computeNodeWidth(node);
                if (i < size - 1) {
                    currentRowWidth += hgap;
                }
                currentLevelMaxHeight = Math.max(currentLevelMaxHeight, computeNodeHeight(node));
                currentLevelNodesCache.add(node);
            }

            currentLevelTotalHeight = currentLevelMaxHeight + vgap + nodeLineGaps;

            if (direction == TreeNodeView.LayoutDirection.BOTTOM_TO_TOP) {
                currentPositionY -= currentLevelTotalHeight;
            }

            double currentPositionX = (maxRowWidth - currentRowWidth) / 2;

            for (TreeNode<T> node : currentLevelNodesCache) {
                if (node == null) {
                    continue;
                }

                double adjustedYPosition = computeCompactAdjustedYPosition(currentPositionY, currentLevelMaxHeight, computeNodeHeight(node), alignment, direction);
                nodeToPositionMap.put(node, new Point2D(currentPositionX, adjustedYPosition - (direction == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM ? nodeLineGaps : 0)));

                currentPositionX += computeNodeWidth(node);
                if (!node.equals(currentLevelNodesCache.get(currentLevelNodesCache.size() - 1))) {
                    currentPositionX += hgap;
                }
            }

            if (direction == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM) {
                currentPositionY += currentLevelTotalHeight;
            }

            for (TreeNode<T> node : currentLevelNodesCache) {
                if (node.isExpanded()) {
                    for (TreeNode<T> child : node.getChildren()) {
                        queue.offer(child);
                    }
                }
            }
        }
    }

    private void positionNodesCompactHorizontally(TreeNode<T> root, double maxColumnHeight, TreeNodeView.LayoutDirection direction) {
        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);
        double currentPositionX = 0;
        double hgap = getSkinnable().getHgap();
        double vgap = getSkinnable().getVgap();
        double nodeColumnGaps = getSkinnable().getNodeLineGap() * 2;
        HPos alignment = getSkinnable().getColumnAlignment();

        while (!queue.isEmpty()) {
            int size = queue.size();
            double currentColumnHeight = 0;
            double currentLevelMaxWidth = 0;
            double currentLevelTotalWidth;

            currentLevelNodesCache.clear();

            for (int i = 0; i < size; i++) {
                TreeNode<T> node = queue.poll();
                if (node == null) {
                    continue;
                }
                currentColumnHeight += computeNodeHeight(node);
                if (i < size - 1) {
                    currentColumnHeight += vgap;
                }
                currentLevelMaxWidth = Math.max(currentLevelMaxWidth, computeNodeWidth(node));
                currentLevelNodesCache.add(node);
            }

            currentLevelTotalWidth = currentLevelMaxWidth + hgap + nodeColumnGaps;

            // Adjusting the X position for RIGHT_TO_LEFT direction
            if (direction == TreeNodeView.LayoutDirection.RIGHT_TO_LEFT) {
                currentPositionX -= currentLevelTotalWidth;
            }

            double currentPositionY = (maxColumnHeight - currentColumnHeight) / 2;

            for (TreeNode<T> node : currentLevelNodesCache) {
                if (node == null) {
                    continue;
                }

                double adjustedXPosition = computeCompactAdjustedXPosition(currentPositionX, currentLevelMaxWidth, computeNodeWidth(node), alignment, direction);
                nodeToPositionMap.put(node, new Point2D(adjustedXPosition - (direction == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT ? nodeColumnGaps : 0), currentPositionY));

                currentPositionY += computeNodeHeight(node);
                if (!node.equals(currentLevelNodesCache.get(currentLevelNodesCache.size() - 1))) {
                    currentPositionY += vgap;
                }
            }

            // Adjusting the X position for LEFT_TO_RIGHT direction
            if (direction == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT) {
                currentPositionX += currentLevelTotalWidth;
            }

            for (TreeNode<T> node : currentLevelNodesCache) {
                if (node.isExpanded()) {
                    for (TreeNode<T> child : node.getChildren()) {
                        queue.offer(child);
                    }
                }
            }
        }
    }

    private double computeAndCacheNodeDimension(TreeNode<T> node) {
        TreeNodeView.LayoutDirection direction = getSkinnable().getLayoutDirection();
        if (direction == TreeNodeView.LayoutDirection.BOTTOM_TO_TOP || direction == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM) {
            double nodeHeight = computeNodeHeight(node);
            int level = node.getLevel();
            levelToMaxDimensionMap.put(level, Math.max(nodeHeight, levelToMaxDimensionMap.getOrDefault(level, 0.0)));

            double nodeWidth = computeNodeWidth(node);
            if (!node.getChildren().isEmpty() && node.isExpanded()) {
                double childrenTotalWidth = node.getChildren().stream().mapToDouble(this::computeAndCacheNodeDimension).sum()
                        + getSkinnable().getHgap() * (node.getChildren().size() - 1);
                nodeWidth = Math.max(nodeWidth, childrenTotalWidth);
            }
            nodeTotalDimensionMap.put(node, nodeWidth);
            return nodeWidth;
        } else { // LEFT_TO_RIGHT or RIGHT_TO_LEFT
            double nodeWidth = computeNodeWidth(node);
            int level = node.getLevel();
            levelToMaxDimensionMap.put(level, Math.max(nodeWidth, levelToMaxDimensionMap.getOrDefault(level, 0.0)));

            double nodeHeight = computeNodeHeight(node);
            if (!node.getChildren().isEmpty() && node.isExpanded()) {
                double childrenTotalHeight = node.getChildren().stream().mapToDouble(this::computeAndCacheNodeDimension).sum()
                        + getSkinnable().getVgap() * (node.getChildren().size() - 1);
                nodeHeight = Math.max(nodeHeight, childrenTotalHeight);
            }
            nodeTotalDimensionMap.put(node, nodeHeight);
            return nodeHeight;
        }
    }

    private void positionNodesRegularVertically(TreeNode<T> node, double x, double y, TreeNodeView.LayoutDirection layoutDirection) {
        double currentTotalWidth = nodeTotalDimensionMap.get(node);
        double currentRealWidth = computeNodeWidth(node);

        // Calculate the startX based on the total width of this node
        double startX = x + (currentTotalWidth - currentRealWidth) / 2;

        int level = node.getLevel();
        double maxLevelHeight = levelToMaxDimensionMap.get(level);
        VPos alignment = getSkinnable().getRowAlignment();
        double adjustedY = computeRegularAdjustedYPosition(y, maxLevelHeight, computeNodeHeight(node), alignment, layoutDirection);

        nodeToPositionMap.put(node, new Point2D(startX, adjustedY));

        // If the node is not expanded, don't position its children
        if (!node.isExpanded()) {
            return;
        }

        double childrenTotalWidth = 0;
        for (TreeNode<T> child : node.getChildren()) {
            childrenTotalWidth += nodeTotalDimensionMap.get(child);
        }
        // add gaps
        childrenTotalWidth += getSkinnable().getHgap() * (node.getChildren().size() - 1);

        double childrenStartX;
        // If the parent node is wider than its children combined
        if (currentRealWidth > childrenTotalWidth) {
            childrenStartX = startX + (currentRealWidth - childrenTotalWidth) / 2;
        } else {
            childrenStartX = x;
        }
        double nextY;
        if (layoutDirection == TreeNodeView.LayoutDirection.BOTTOM_TO_TOP) {
            nextY = y - maxLevelHeight - getSkinnable().getVgap() - getSkinnable().getNodeLineGap() * 2;
        } else {
            nextY = y + maxLevelHeight + getSkinnable().getVgap() + getSkinnable().getNodeLineGap() * 2;
        }
        for (TreeNode<T> child : node.getChildren()) {
            positionNodesRegularVertically(child, childrenStartX, nextY, layoutDirection);
            childrenStartX += nodeTotalDimensionMap.get(child) + getSkinnable().getHgap();
        }
    }

    private void positionNodesRegularHorizontally(TreeNode<T> node, double x, double y, TreeNodeView.LayoutDirection layoutDirection) {
        double currentTotalHeight = nodeTotalDimensionMap.get(node);
        double currentRealHeight = computeNodeHeight(node);

        // Calculate the startY based on the total height of this node
        double startY = y + (currentTotalHeight - currentRealHeight) / 2;

        int level = node.getLevel();
        Double maxLevelWidth = levelToMaxDimensionMap.get(level);
        HPos alignment = getSkinnable().getColumnAlignment();
        double adjustedX = computeRegularAdjustedXPosition(x, maxLevelWidth, computeNodeWidth(node), alignment, layoutDirection);

        nodeToPositionMap.put(node, new Point2D(adjustedX, startY));

        // If the node is not expanded, don't position its children
        if (!node.isExpanded()) {
            return;
        }

        double childrenTotalHeight = 0;
        for (TreeNode<T> child : node.getChildren()) {
            childrenTotalHeight += nodeTotalDimensionMap.get(child);
        }
        childrenTotalHeight += getSkinnable().getVgap() * (node.getChildren().size() - 1);

        // Adjust the start position of children so the parent is vertically centered with respect to its children.
        double childrenStartY = startY + (currentRealHeight / 2) - (childrenTotalHeight / 2);

        double nextX;
        double nodeLineGaps = getSkinnable().getNodeLineGap() * 2;
        if (layoutDirection == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT) {
            if (alignment == HPos.LEFT) {
                nextX = adjustedX + maxLevelWidth + getSkinnable().getHgap() + nodeLineGaps;
            } else if (alignment == HPos.CENTER) {
                double offset = (maxLevelWidth - computeNodeWidth(node)) / 2;
                nextX = adjustedX + offset + computeNodeWidth(node) + getSkinnable().getHgap() + nodeLineGaps;
            } else { // HPos.RIGHT
                nextX = adjustedX + computeNodeWidth(node) + getSkinnable().getHgap() + nodeLineGaps;
            }
        } else {  // RIGHT_TO_LEFT
            if (alignment == HPos.LEFT) {
                nextX = adjustedX - getSkinnable().getHgap() - nodeLineGaps;
            } else if (alignment == HPos.CENTER) {
                double offset = (maxLevelWidth - computeNodeWidth(node)) / 2;
                nextX = adjustedX - offset - getSkinnable().getHgap() - nodeLineGaps;
            } else { // HPos.RIGHT
                nextX = adjustedX - maxLevelWidth + computeNodeWidth(node) - getSkinnable().getHgap() - nodeLineGaps;
            }
        }
        for (TreeNode<T> child : node.getChildren()) {
            positionNodesRegularHorizontally(child, nextX, childrenStartY, layoutDirection);
            childrenStartY += nodeTotalDimensionMap.get(child) + getSkinnable().getVgap();
        }
    }

    private double computeRegularAdjustedXPosition(double x, double maxLevelWidth, double nodeWidth, HPos alignment, TreeNodeView.LayoutDirection direction) {
        if (direction == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT) {
            return switch (alignment) {
                case LEFT -> x;
                case CENTER -> x + (maxLevelWidth - nodeWidth) / 2;
                case RIGHT -> x + maxLevelWidth - nodeWidth;
            };
        } else { // RIGHT_TO_LEFT
            return switch (alignment) {
                case LEFT -> x - maxLevelWidth;
                case CENTER -> x - (maxLevelWidth + nodeWidth) / 2;
                case RIGHT -> x - nodeWidth;
            };
        }
    }

    private double computeRegularAdjustedYPosition(double y, double maxLevelHeight, double nodeHeight, VPos alignment, TreeNodeView.LayoutDirection layoutDirection) {
        if (layoutDirection == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM) {
            return switch (alignment) {
                case TOP -> y;
                case CENTER -> y + (maxLevelHeight - nodeHeight) / 2;
                case BASELINE, BOTTOM -> y + maxLevelHeight - nodeHeight;
            };
        } else {
            return switch (alignment) {
                case TOP -> y - maxLevelHeight;
                case CENTER -> y - (maxLevelHeight + nodeHeight) / 2;
                case BASELINE, BOTTOM -> y - nodeHeight;
            };
        }
    }

    private double computeCompactAdjustedYPosition(double y, double maxLevelHeight, double nodeHeight, VPos alignment, TreeNodeView.LayoutDirection layoutDirection) {
        if (layoutDirection == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM) {
            return switch (alignment) {
                case TOP -> y;
                case CENTER -> y + (maxLevelHeight - nodeHeight) / 2;
                case BASELINE, BOTTOM -> y + maxLevelHeight - nodeHeight;
            };
        } else {
            return switch (alignment) {
                case TOP -> y;
                case CENTER -> y + (maxLevelHeight / 2) - (nodeHeight / 2);
                case BASELINE, BOTTOM -> y + maxLevelHeight - nodeHeight;
            };
        }
    }

    private double computeCompactAdjustedXPosition(double x, double maxLevelWidth, double nodeWidth, HPos alignment, TreeNodeView.LayoutDirection direction) {
        if (direction == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT) {
            return switch (alignment) {
                case LEFT -> x;
                case CENTER -> x + (maxLevelWidth - nodeWidth) / 2;
                case RIGHT -> x + maxLevelWidth - nodeWidth;
            };
        } else {
            return switch (alignment) {
                case LEFT -> x + maxLevelWidth - nodeWidth;
                case CENTER -> x + (maxLevelWidth / 2) - (nodeWidth / 2);
                case RIGHT -> x;
            };
        }
    }

    /**
     * Toggle the visibility of the node's children
     */
    private void toggleChildrenVisibility(TreeNode<T> node, boolean isVisible, boolean updateTreeAfterToggle) {
        for (TreeNode<T> child : node.getChildren()) {
            if (!nodeToComponentsMap.containsKey(child)) {
                drawNode(child);
            }
            List<Node> components = nodeToComponentsMap.get(child);
            if (components != null) {
                for (Node component : components) {
                    component.setVisible(isVisible);
                    component.setManaged(isVisible);
                }
            }
            if (child.isExpanded()) {
                // Avoid calling updateTree repeatedly, set updateTreeAfterToggle false here
                toggleChildrenVisibility(child, isVisible && child.isExpanded(), false);
            }
        }
        if (updateTreeAfterToggle) {
            updateTree();
        }
    }

    /**
     * When expanding or collapsing, the display of the tree needs to be updated
     */
    private void updateTree() {
        contentGroup.getChildren().clear();
        clearMapsForUpdate();
        TreeNode<T> root = getSkinnable().getRoot();
        if (root != null) {
            calculatePositions(root);
            drawNode(root);
            drawAdditionalLinkedNodes();
        }
    }

    private void drawAdditionalLinkedNodes() {
        TreeNode<T> root = getSkinnable().getRoot();
        if (root != null) {
            root.stream().forEach(this::drawLinksForNode);
        }
    }

    private void drawLinksForNode(TreeNode<T> node) {
        Point2D sourcePosition = nodeToPositionMap.get(node);
        for (TreeNode<T> linkedNode : node.getLinkedNodes()) {
            Point2D targetPosition = nodeToPositionMap.get(linkedNode);
            if (sourcePosition != null && targetPosition != null) {
                List<Node> nodes = getSkinnable().getLinkStrategy().drawNodeLink(getSkinnable().getLayoutDirection(), levelToMaxDimensionMap.get(node.getLevel()), node, sourcePosition, computeNodeWidth(node), computeNodeHeight(node), linkedNode, targetPosition, computeNodeWidth(linkedNode), computeNodeHeight(linkedNode), getSkinnable().getNodeLineGap(), getSkinnable().getVgap(), getSkinnable().getHgap());
                if (node.getName() != null && linkedNode.getName() != null) {
                    nodes.forEach(n -> n.getStyleClass().add("link-extra-" + node.getName() + "-" + linkedNode.getName()));
                }
                contentGroup.getChildren().addAll(nodes);
//                additionalLinkedNodeList.addAll(nodes);
            }
        }
    }

    /**
     * Clear the position information of the nodes; do not clear the listeners
     */
    private void clearMapsForUpdate() {
        nodeToComponentsMap.clear();
//        additionalLinkedNodeList.clear();
        nodeToPositionMap.clear();
        nodeTotalDimensionMap.clear();
        levelToMaxDimensionMap.clear();
        currentLevelNodesCache.clear();
    }

    /**
     * Clear all maps; (including listeners)
     */
    private void clearMapsForBuild() {
        clearMapsForUpdate();
        expandListenerMap.clear();
        invailidateListenerMap.clear();
        childrenListListenerMap.clear();
    }

    public double computeNodeWidth(TreeNode<T> node) {
        return node.getWidth() == TreeNode.USE_TREE_CELL_SIZE ? getSkinnable().getCellWidth() : node.getWidth();
    }

    public double computeNodeHeight(TreeNode<T> node) {
        return node.getHeight() == TreeNode.USE_TREE_CELL_SIZE ? getSkinnable().getCellHeight() : node.getHeight();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return contentGroup.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return contentGroup.prefHeight(width) + leftInset + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    public void refresh() {
        buildTree();
    }
}