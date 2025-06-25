package com.dlsc.gemsfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ResponsivePane is a container that allows for responsive behavior of a sidebar and a main content panel.
 * Depending on the specified position of the sidebar (LEFT or RIGHT), it will automatically adjust its visibility
 * based on the width of the pane. When the window width is narrow, the sidebar is hidden, and only the content panel is shown.
 * When the window width is moderate, both a small sidebar and a large sidebar, along with the content panel, are displayed.
 * When the window width is wide, both the large sidebar and the main content pane are shown.
 * Similarly, if the sidebar is positioned at the TOP or BOTTOM, its visibility will be adjusted based on the height of the pane.
 * However, it is also possible to force the sidebar to be displayed regardless of the window size.
 */
public class ResponsivePane extends Pane {

    private static final Side DEFAULT_SIDE = Side.LEFT;

    // Pseudo classes for the sidebar showing state
    private static final PseudoClass SHOWING_SMALL_PSEUDOCLASS = PseudoClass.getPseudoClass("showing-small");
    private static final PseudoClass SHOWING_LARGE_PSEUDOCLASS = PseudoClass.getPseudoClass("showing-large");
    private static final PseudoClass SHOWING_NONE_PSEUDOCLASS = PseudoClass.getPseudoClass("showing-none");

    /**
     * This pseudo-class is applied only  when both smallSideBar is visible and largeSideBar is forced to display.
     */
    private static final PseudoClass FORCED_PSEUDOCLASS = PseudoClass.getPseudoClass("forced");

    /**
     * This pseudo-class is applied only when both smallSideBar is visible and largeSideBar is also visible and overlapping smallSideBar.
     */
    private static final PseudoClass COVERING_PSEUDOCLASS = PseudoClass.getPseudoClass("covering");

    // Pseudo classes for the side
    private static final PseudoClass LEFT_PSEUDOCLASS = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_PSEUDOCLASS = PseudoClass.getPseudoClass("right");
    private static final PseudoClass TOP_PSEUDOCLASS = PseudoClass.getPseudoClass("top");
    private static final PseudoClass BOTTOM_PSEUDOCLASS = PseudoClass.getPseudoClass("bottom");

    private final BooleanProperty glassPaneNeedHide = new SimpleBooleanProperty(true);

    private final GlassPane glassPane = new GlassPane();

    public ResponsivePane() {
        super();

        getStyleClass().add("responsive-pane");

        // handle the glassPane
        glassPane.hideProperty().bind(glassPaneNeedHide);
        glassPane.setOnMouseClicked(event -> setForceLargeSidebarDisplay(false));
        getChildren().add(glassPane);

        // pseudo-class state
        updatePseudoClasses(getSide());

    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(ResponsivePane.class.getResource("responsive-pane.css")).toExternalForm();
    }

    private ObjectProperty<Node> content;

    public final ObjectProperty<Node> contentProperty() {
        if (content == null) {
            content = new NodeProperty("content");
        }
        return content;
    }

    public final void setContent(Node value) {
        contentProperty().set(value);
    }

    public final Node getContent() {
        return content == null ? null : content.get();
    }

    public ObjectProperty<Node> smallSidebar;

    public final ObjectProperty<Node> smallSidebarProperty() {
        if (smallSidebar == null) {
            smallSidebar = new NodeProperty("smallSidebar");
        }
        return smallSidebar;
    }

    public final void setSmallSidebar(Node value) {
        smallSidebarProperty().set(value);
    }

    public final Node getSmallSidebar() {
        return smallSidebar == null ? null : smallSidebar.get();
    }

    public ObjectProperty<Node> largeSidebar;

    public final ObjectProperty<Node> largeSidebarProperty() {
        if (largeSidebar == null) {
            largeSidebar = new NodeProperty("largeSidebar");
        }
        return largeSidebar;
    }

    public final void setLargeSidebar(Node value) {
        largeSidebarProperty().set(value);
    }

    public final Node getLargeSidebar() {
        return largeSidebar == null ? null : largeSidebar.get();
    }

    private BooleanProperty largeSidebarCoversSmall;

    public final boolean isLargeSidebarCoversSmall() {
        return largeSidebarCoversSmall != null && largeSidebarCoversSmall.get();
    }

    /**
     * This property determines the display behavior of the large sidebar in relation to the small sidebar when the large sidebar is forced to display.
     * When set to true, the large sidebar will cover the small sidebar.
     * When set to false, the large sidebar will position itself next to the small sidebar without overlaying, ensuring both sidebars are visible
     * and accessible to the user.
     */
    public final BooleanProperty largeSidebarCoversSmallProperty() {
        if (largeSidebarCoversSmall == null) {
            largeSidebarCoversSmall = new SimpleBooleanProperty(this, "largeSidebarCoversSmall", false) {
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };
        }
        return largeSidebarCoversSmall;
    }

    public final void setLargeSidebarCoversSmall(boolean largeSidebarCoversSmall) {
        largeSidebarCoversSmallProperty().set(largeSidebarCoversSmall);
    }

    private BooleanProperty forceLargeSidebarDisplay;

    public final boolean isForceLargeSidebarDisplay() {
        return forceLargeSidebarDisplay != null && forceLargeSidebarDisplay.get();
    }

    /**
     * This property, when value is true, forces the display of the large sidebar even when the small sidebar is visible.
     */
    public final BooleanProperty forceLargeSidebarDisplayProperty() {
        if (forceLargeSidebarDisplay == null) {
            forceLargeSidebarDisplay = new SimpleBooleanProperty(this, "forceLargeSidebarDisplay", false) {
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };
        }
        return forceLargeSidebarDisplay;
    }

    public final void setForceLargeSidebarDisplay(boolean forceLargeSidebarDisplay) {
        forceLargeSidebarDisplayProperty().set(forceLargeSidebarDisplay);
    }

    private ObjectProperty<Side> side;

    public final ObjectProperty<Side> sideProperty() {
        if (side == null) {
            side = new StyleableObjectProperty<>(DEFAULT_SIDE) {

                @Override
                protected void invalidated() {
                    requestLayout();
                    updatePseudoClasses(get());
                }

                @Override
                public Object getBean() {
                    return ResponsivePane.this;
                }

                @Override
                public String getName() {
                    return "side";
                }


                @Override
                public CssMetaData<? extends Styleable, Side> getCssMetaData() {
                    return StyleableProperties.SIDE;
                }

            };
        }
        return side;
    }

    private void updatePseudoClasses(final Side currentSide) {
        pseudoClassStateChanged(LEFT_PSEUDOCLASS, currentSide == Side.LEFT);
        pseudoClassStateChanged(RIGHT_PSEUDOCLASS, currentSide == Side.RIGHT);
        pseudoClassStateChanged(TOP_PSEUDOCLASS, currentSide == Side.TOP);
        pseudoClassStateChanged(BOTTOM_PSEUDOCLASS, currentSide == Side.BOTTOM);
    }

    public Side getSide() {
        return side == null ? DEFAULT_SIDE : side.get();
    }

    public void setSide(Side side) {
        sideProperty().set(side);
    }

    private DoubleProperty gap;

    public final DoubleProperty gapProperty() {
        if (gap == null) {
            gap = new StyleableDoubleProperty(0d) {

                @Override
                protected void invalidated() {
                    requestLayout();
                }

                @Override
                public Object getBean() {
                    return ResponsivePane.this;
                }

                @Override
                public String getName() {
                    return "gap";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.GAP;
                }
            };
        }
        return gap;
    }

    public double getGap() {
        return gap == null ? 0d : gap.get();
    }

    public void setGap(double gap) {
        gapProperty().set(gap);
    }

    private static class StyleableProperties {
        private static final CssMetaData<ResponsivePane, Side> SIDE =
                new CssMetaData<>("-fx-side",
                        new EnumConverter<>(Side.class), DEFAULT_SIDE) {

                    @Override
                    public boolean isSettable(ResponsivePane node) {
                        return node.side == null || !node.side.isBound();
                    }

                    @Override
                    public StyleableProperty<Side> getStyleableProperty(ResponsivePane node) {
                        return (StyleableProperty<Side>) node.sideProperty();
                    }
                };


        private static final CssMetaData<ResponsivePane, Number> GAP =
                new CssMetaData<>("-fx-gap",
                        SizeConverter.getInstance(), 0d) {

                    @Override
                    public boolean isSettable(ResponsivePane node) {
                        return node.gap == null || !node.gap.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(ResponsivePane node) {
                        return (StyleableProperty<Number>) node.gapProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Region.getClassCssMetaData());
            Collections.addAll(styleables, SIDE, GAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    protected void layoutChildren() {
        final Insets insets = getInsets();
        final double insideX = snapSpaceX(insets.getLeft());
        final double insideY = snapSpaceY(insets.getTop());
        final double insideWidth = snapSizeX(getInsideWidth(getWidth()));
        final double insideHeight = snapSizeY(getHeight() - insideY - snapSpaceY(insets.getBottom()));

        final Node contentNode = getContent();
        final Node smallSidebarNode = getSmallSidebar();
        final Node largeSidebarNode = getLargeSidebar();

        final Side side = getSide() == null ? DEFAULT_SIDE : getSide();
        final boolean isHorizontal = side == Side.LEFT || side == Side.RIGHT;

        final double largeSidebarWidth = computeNodeWidth(largeSidebarNode, -1, false);
        final double largeSidebarHeight = computeNodeHeight(largeSidebarNode, insideWidth, false);
        final double smallSidebarWidth = computeNodeWidth(smallSidebarNode, -1, false);
        final double smallSidebarHeight = computeNodeHeight(smallSidebarNode, insideWidth, false);
        final double contentMinWidth = computeNodeWidth(contentNode, -1, true);
        final double contentMinHeight = computeNodeHeight(contentNode, -1, true);

        final double snappedGap = Math.max(0d, isHorizontal ? snapSpaceX(getGap()) : snapSpaceY(getGap()));
        final double gapForCheck = (contentNode != null && (smallSidebarNode != null || largeSidebarNode != null)) ? snappedGap : 0;

        Node activeSidebar = null;
        double sidebarWidth = 0;
        double sidebarHeight = 0;

        if (isHorizontal) {
            if (shouldShowSidebar(insideWidth, largeSidebarWidth, contentMinWidth, gapForCheck)) {
                activeSidebar = largeSidebarNode;
                sidebarWidth = largeSidebarWidth;
            } else if (shouldShowSidebar(insideWidth, smallSidebarWidth, contentMinWidth, gapForCheck)) {
                activeSidebar = smallSidebarNode;
                sidebarWidth = smallSidebarWidth;
            }
        } else { // Vertical
            if (shouldShowSidebar(insideHeight, largeSidebarHeight, contentMinHeight, gapForCheck)) {
                activeSidebar = largeSidebarNode;
                sidebarHeight = largeSidebarHeight;
            } else if (shouldShowSidebar(insideHeight, smallSidebarHeight, contentMinHeight, gapForCheck)) {
                activeSidebar = smallSidebarNode;
                sidebarHeight = smallSidebarHeight;
            }
        }

        double contentStartX = insideX;
        double contentStartY = insideY;
        double contentWidth = insideWidth;
        double contentHeight = insideHeight;
        boolean largeSidebarNeedDisplay = false;

        if (activeSidebar != null) {
            switch (side) {
                case LEFT:
                    double snappedSidebarWidth = snapSizeX(sidebarWidth);
                    activeSidebar.resizeRelocate(insideX, insideY, snappedSidebarWidth, insideHeight);
                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        double snappedLargeSidebarWidth = snapSizeX(largeSidebarWidth);
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.resizeRelocate(insideX, insideY, snappedLargeSidebarWidth, insideHeight);
                        } else {
                            largeSidebarNode.resizeRelocate(insideX + snappedSidebarWidth, insideY, snappedLargeSidebarWidth, insideHeight);
                        }
                    }
                    contentStartX += snappedSidebarWidth + gapForCheck;
                    contentWidth -= snappedSidebarWidth + gapForCheck;
                    break;
                case RIGHT:
                    snappedSidebarWidth = snapSizeX(sidebarWidth);
                    activeSidebar.resizeRelocate(insideX + insideWidth - snappedSidebarWidth, insideY, snappedSidebarWidth, insideHeight);
                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        double snappedLargeSidebarWidth = snapSizeX(largeSidebarWidth);
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.resizeRelocate(insideX + insideWidth - snappedLargeSidebarWidth, insideY, snappedLargeSidebarWidth, insideHeight);
                        } else {
                            largeSidebarNode.resizeRelocate(insideX + insideWidth - snappedLargeSidebarWidth - snappedSidebarWidth, insideY, snappedLargeSidebarWidth, insideHeight);
                        }
                    }
                    contentWidth -= snappedSidebarWidth + gapForCheck;
                    break;
                case TOP:
                    double snappedSidebarHeight = snapSizeY(sidebarHeight);
                    activeSidebar.resizeRelocate(insideX, insideY, insideWidth, snappedSidebarHeight);
                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        double snappedLargeSidebarHeight = snapSizeY(largeSidebarHeight);
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.resizeRelocate(insideX, insideY, insideWidth, snappedLargeSidebarHeight);
                        } else {
                            largeSidebarNode.resizeRelocate(insideX, insideY + snappedSidebarHeight, insideWidth, snappedLargeSidebarHeight);
                        }
                    }
                    contentStartY += snappedSidebarHeight + gapForCheck;
                    contentHeight -= snappedSidebarHeight + gapForCheck;
                    break;
                case BOTTOM:
                    snappedSidebarHeight = snapSizeY(sidebarHeight);
                    activeSidebar.resizeRelocate(insideX, insideY + insideHeight - snappedSidebarHeight, insideWidth, snappedSidebarHeight);
                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        double snappedLargeSidebarHeight = snapSizeY(largeSidebarHeight);
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.resizeRelocate(insideX, insideY + insideHeight - snappedLargeSidebarHeight, insideWidth, snappedLargeSidebarHeight);
                        } else {
                            largeSidebarNode.resizeRelocate(insideX, insideY + insideHeight - snappedLargeSidebarHeight - snappedSidebarHeight, insideWidth, snappedLargeSidebarHeight);
                        }
                    }
                    contentHeight -= snappedSidebarHeight + gapForCheck;
                    break;
            }
        }

        pseudoClassStateChanged(SHOWING_NONE_PSEUDOCLASS, activeSidebar == null);
        pseudoClassStateChanged(SHOWING_SMALL_PSEUDOCLASS, activeSidebar == smallSidebarNode);
        pseudoClassStateChanged(SHOWING_LARGE_PSEUDOCLASS, activeSidebar == largeSidebarNode);
        pseudoClassStateChanged(FORCED_PSEUDOCLASS, largeSidebarNeedDisplay);
        pseudoClassStateChanged(COVERING_PSEUDOCLASS, largeSidebarNeedDisplay && isLargeSidebarCoversSmall());

        glassPaneNeedHide.set(!largeSidebarNeedDisplay);

        if (contentNode != null) {
            contentNode.resizeRelocate(contentStartX, contentStartY, contentWidth, contentHeight);
            if (getChildren().get(0) != contentNode) {
                ((NodeProperty) contentProperty()).setSuppressLayoutChanges(true);
                contentNode.toBack();
                ((NodeProperty) contentProperty()).setSuppressLayoutChanges(false);
            }
        }
        glassPane.relocate(contentStartX, contentStartY);
        glassPane.resize(contentWidth, contentHeight);

        if (smallSidebarNode != null) {
            smallSidebarNode.setVisible(activeSidebar == smallSidebarNode);
            smallSidebarNode.setManaged(activeSidebar == smallSidebarNode);
        }
        if (largeSidebarNode != null) {
            boolean isVisible = (activeSidebar == largeSidebarNode) || largeSidebarNeedDisplay;
            largeSidebarNode.setVisible(isVisible);
            largeSidebarNode.setManaged(isVisible);
            if (isVisible && getChildren().get(getChildren().size() - 1) != largeSidebarNode) {
                ((NodeProperty) largeSidebarProperty()).setSuppressLayoutChanges(true);
                largeSidebarNode.toFront();
                ((NodeProperty) largeSidebarProperty()).setSuppressLayoutChanges(false);
            }
        }
    }

    private final class NodeProperty extends ObjectPropertyBase<Node> {
        private Node oldValue = null;
        private final String propertyName;
        private boolean isBeingInvalidated;
        private boolean suppressLayoutChanges;

        NodeProperty(String propertyName) {
            this.propertyName = propertyName;
            getChildren().addListener((ListChangeListener<Node>) c -> {
                if (oldValue == null || isBeingInvalidated || suppressLayoutChanges) {
                    return;
                }
                while (c.next()) {
                    if (c.wasRemoved()) {
                        List<? extends Node> removed = c.getRemoved();
                        for (Node node : removed) {
                            if (node == oldValue) {
                                // Do not remove again in invalidated
                                oldValue = null;
                                set(null);
                            }
                        }
                    }
                }
            });
        }

        @Override
        protected void invalidated() {
            if (suppressLayoutChanges) {
                return;
            }

            final List<Node> children = getChildren();

            isBeingInvalidated = true;
            try {
                if (oldValue != null) {
                    children.remove(oldValue);
                }

                final Node _value = get();
                this.oldValue = _value;

                if (_value != null) {
                    children.add(_value);
                }
            } finally {
                isBeingInvalidated = false;
            }
        }

        public void setSuppressLayoutChanges(boolean suppress) {
            this.suppressLayoutChanges = suppress;
        }

        @Override
        public Object getBean() {
            return ResponsivePane.this;
        }

        @Override
        public String getName() {
            return propertyName;
        }
    }

    private double getInsideWidth(double totalWidth) {
        if (totalWidth == -1) {
            return -1;
        }
        Insets insets = getInsets();
        return totalWidth - snapSpaceX(insets.getLeft()) - snapSpaceX(insets.getRight());
    }

    private boolean shouldShowSidebar(double containerSize, double sidebarSize, double contentMinSize, double gap) {
        if (sidebarSize <= 0 || containerSize < 0) {
            return false;
        }
        return containerSize >= sidebarSize + contentMinSize + gap;
    }

    private double computeNodeWidth(Node node, double height, boolean isMin) {
        if (node == null) {
            return 0;
        }
        return isMin ? node.minWidth(height) : node.prefWidth(height);
    }

    private double computeNodeHeight(Node node, double width, boolean isMin) {
        if (node == null) {
            return 0;
        }
        return isMin ? node.minHeight(width) : node.prefHeight(width);
    }

    @Override
    public Orientation getContentBias() {
        Node contentNode = getContent();
        if (contentNode != null) {
            return contentNode.getContentBias();
        }
        return null;
    }

    @Override
    protected double computePrefWidth(double height) {
        final Insets insets = getInsets();
        final Side side = getSide();
        final boolean isHorizontal = side == Side.LEFT || side == Side.RIGHT;

        double contentPrefWidth = computeNodeWidth(getContent(), height, false);
        double smallSidebarPrefWidth = computeNodeWidth(getSmallSidebar(), isHorizontal ? -1 : height, false);
        double largeSidebarPrefWidth = computeNodeWidth(getLargeSidebar(), isHorizontal ? -1 : height, false);

        if (isHorizontal) {
            double sidebarPrefWidth = Math.max(smallSidebarPrefWidth, largeSidebarPrefWidth);
            double gap = (sidebarPrefWidth > 0 && contentPrefWidth > 0) ? getGap() : 0;
            return snapSpaceX(insets.getLeft()) + contentPrefWidth + gap + sidebarPrefWidth + snapSpaceX(insets.getRight());
        } else {
            double maxChildWidth = Math.max(contentPrefWidth, Math.max(smallSidebarPrefWidth, largeSidebarPrefWidth));
            return snapSpaceX(insets.getLeft()) + maxChildWidth + snapSpaceX(insets.getRight());
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        final Insets insets = getInsets();
        final Side side = getSide();
        final double insideWidth = getInsideWidth(width);
        double prefHeight;

        if (side == Side.LEFT || side == Side.RIGHT) {
            final double largeSidebarWidth = computeNodeWidth(getLargeSidebar(), -1, false);
            final double smallSidebarWidth = computeNodeWidth(getSmallSidebar(), -1, false);
            final double contentMinWidth = computeNodeWidth(getContent(), -1, true);
            final double gap = (getContent() != null && (getSmallSidebar() != null || getLargeSidebar() != null)) ? getGap() : 0;

            double sidebarWidthToConsider = 0;
            if (shouldShowSidebar(insideWidth, largeSidebarWidth, contentMinWidth, gap)) {
                sidebarWidthToConsider = largeSidebarWidth;
            } else if (shouldShowSidebar(insideWidth, smallSidebarWidth, contentMinWidth, gap)) {
                sidebarWidthToConsider = smallSidebarWidth;
            }

            double contentWidthForSizing = (insideWidth == -1) ? -1 : Math.max(0, insideWidth - sidebarWidthToConsider - gap);
            double contentPrefHeight = computeNodeHeight(getContent(), contentWidthForSizing, false);
            double sidebarPrefHeight = Math.max(computeNodeHeight(getSmallSidebar(), -1, false), computeNodeHeight(getLargeSidebar(), -1, false));

            prefHeight = Math.max(contentPrefHeight, sidebarPrefHeight);
        } else { // TOP or BOTTOM
            double contentPrefHeight = computeNodeHeight(getContent(), insideWidth, false);
            double largeSidebarPrefHeight = computeNodeHeight(getLargeSidebar(), insideWidth, false);
            double smallSidebarPrefHeight = computeNodeHeight(getSmallSidebar(), insideWidth, false);
            double maxSidebarHeight = Math.max(largeSidebarPrefHeight, smallSidebarPrefHeight);

            prefHeight = contentPrefHeight;
            if (maxSidebarHeight > 0 && getContent() != null) {
                prefHeight += getGap();
            }
            prefHeight += maxSidebarHeight;
        }

        return snapSpaceY(insets.getTop()) + prefHeight + snapSpaceY(insets.getBottom());
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }
}