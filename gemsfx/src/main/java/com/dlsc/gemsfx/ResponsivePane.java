package com.dlsc.gemsfx;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.css.*;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
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
 *
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
            content = new ResponsivePane.NodeProperty("content");
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
                    return this;
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
                    return this;
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
        Insets insets = getInsets();

        final double insideWidth = getWidth() - (insets.getLeft() + insets.getRight());
        final double insideHeight = getHeight() - (insets.getTop() + insets.getBottom());

        Node contentNode = getContent();
        Node smallSidebarNode = getSmallSidebar();
        Node largeSidebarNode = getLargeSidebar();

        double gap = Math.max(0d, getGap());
        Side side = getSide() == null ? DEFAULT_SIDE : getSide();
        boolean isSideBarInHorizontal = (side == Side.LEFT || side == Side.RIGHT);

        double prefContentWidth = computeNodeWidth(contentNode, isSideBarInHorizontal);
        double prefContentHeight = computeNodeHeight(contentNode, isSideBarInHorizontal);

        double largeSidebarWidth = computeNodeWidth(largeSidebarNode, isSideBarInHorizontal);
        double largeSidebarHeight = computeNodeHeight(largeSidebarNode, isSideBarInHorizontal);

        double smallSidebarWidth = computeNodeWidth(smallSidebarNode, isSideBarInHorizontal);
        double smallSidebarHeight = computeNodeHeight(smallSidebarNode, isSideBarInHorizontal);

        double sidebarWidth = 0;
        double sidebarHeight = 0;

        Node activeSidebar = null;
        // Check if we can display the large sidebar
        if (largeSidebarNode != null && isSideBarInHorizontal && insideWidth >= prefContentWidth + largeSidebarWidth + gap) {
            activeSidebar = largeSidebarNode;
            sidebarWidth = largeSidebarWidth;
        } else if (largeSidebarNode != null && !isSideBarInHorizontal && insideHeight >= prefContentHeight + largeSidebarHeight + gap) {
            activeSidebar = largeSidebarNode;
            sidebarHeight = largeSidebarHeight;
        }
        // Check if we can display the small sidebar
        else if (smallSidebarNode != null && isSideBarInHorizontal && insideWidth > prefContentWidth + smallSidebarWidth + gap) {
            activeSidebar = smallSidebarNode;
            sidebarWidth = smallSidebarWidth;
        } else if (smallSidebarNode != null && !isSideBarInHorizontal && insideHeight > prefContentHeight + smallSidebarHeight + gap) {
            activeSidebar = smallSidebarNode;
            sidebarHeight = smallSidebarHeight;
        }

        // Layout active sidebar and content
        double contentStartX = insets.getLeft();
        double contentStartY = insets.getTop();
        double contentWidth = insideWidth;
        double contentHeight = insideHeight;

        boolean largeSidebarNeedDisplay = false;
        if (activeSidebar != null) {
            switch (side) {
                case LEFT:
                    activeSidebar.relocate(contentStartX, contentStartY);
                    activeSidebar.resize(sidebarWidth, insideHeight);

                    // Show large sidebar if it is forced to be displayed
                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        // Cover the small sidebar
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.relocate(contentStartX, contentStartY);
                            largeSidebarNode.resize(largeSidebarWidth, insideHeight);
                        } else {
                            // Next to the small sidebar
                            largeSidebarNode.relocate(contentStartX + sidebarWidth, contentStartY);
                            largeSidebarNode.resize(largeSidebarWidth, insideHeight);
                        }
                    }

                    contentStartX += sidebarWidth + gap;
                    contentWidth -= sidebarWidth + gap;
                    break;
                case RIGHT:
                    activeSidebar.relocate(contentStartX + insideWidth - sidebarWidth, contentStartY);
                    activeSidebar.resize(sidebarWidth, insideHeight);

                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.relocate(contentStartX + insideWidth - largeSidebarWidth, contentStartY);
                            largeSidebarNode.resize(largeSidebarWidth, insideHeight);
                        } else {
                            largeSidebarNode.relocate(contentStartX + insideWidth - largeSidebarWidth - sidebarWidth, contentStartY);
                            largeSidebarNode.resize(largeSidebarWidth, insideHeight);
                        }
                    }

                    contentWidth -= sidebarWidth + gap;
                    break;
                case TOP:
                    activeSidebar.relocate(contentStartX, contentStartY);
                    activeSidebar.resize(insideWidth, sidebarHeight);

                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.relocate(contentStartX, contentStartY);
                            largeSidebarNode.resize(insideWidth, largeSidebarHeight);
                        } else {
                            largeSidebarNode.relocate(contentStartX, contentStartY + sidebarHeight);
                            largeSidebarNode.resize(insideWidth, largeSidebarHeight);
                        }
                    }

                    contentStartY += sidebarHeight + gap;
                    contentHeight -= sidebarHeight + gap;
                    break;
                case BOTTOM:
                    activeSidebar.relocate(contentStartX, contentStartY + insideHeight - sidebarHeight);
                    activeSidebar.resize(insideWidth, sidebarHeight);

                    if (activeSidebar == smallSidebarNode && largeSidebarNode != null && isForceLargeSidebarDisplay()) {
                        largeSidebarNeedDisplay = true;
                        if (isLargeSidebarCoversSmall()) {
                            largeSidebarNode.relocate(contentStartX, contentStartY + insideHeight - largeSidebarHeight);
                            largeSidebarNode.resize(insideWidth, largeSidebarHeight);
                        } else {
                            largeSidebarNode.relocate(contentStartX, contentStartY + insideHeight - largeSidebarHeight - sidebarHeight);
                            largeSidebarNode.resize(insideWidth, largeSidebarHeight);
                        }
                    }

                    contentHeight -= sidebarHeight + gap;
                    break;
            }
            activeSidebar.setVisible(true);
            activeSidebar.setManaged(true);
        }

        pseudoClassStateChanged(SHOWING_NONE_PSEUDOCLASS, activeSidebar == null);
        pseudoClassStateChanged(SHOWING_SMALL_PSEUDOCLASS, activeSidebar == smallSidebarNode);
        pseudoClassStateChanged(SHOWING_LARGE_PSEUDOCLASS, activeSidebar == largeSidebarNode);
        pseudoClassStateChanged(FORCED_PSEUDOCLASS, largeSidebarNeedDisplay);
        pseudoClassStateChanged(COVERING_PSEUDOCLASS, largeSidebarNeedDisplay && isLargeSidebarCoversSmall());

        glassPaneNeedHide.set(!largeSidebarNeedDisplay);

        if (contentNode != null) {
            contentNode.relocate(contentStartX, contentStartY);
            contentNode.resize(contentWidth, contentHeight);
            if (getChildren().get(0) != contentNode) {
                ((NodeProperty) contentProperty()).setSuppressLayoutChanges(true);
                contentNode.toBack();
                ((NodeProperty) contentProperty()).setSuppressLayoutChanges(false);
            }
        }
        glassPane.relocate(contentStartX, contentStartY);
        glassPane.resize(contentWidth, contentHeight);

        // Ensure only the active sidebar is visible
        if (smallSidebarNode != null && activeSidebar != smallSidebarNode) {
            smallSidebarNode.setVisible(false);
            smallSidebarNode.setManaged(false);
        }

        if (largeSidebarNode != null && activeSidebar != largeSidebarNode) {
            if (isForceLargeSidebarDisplay()) {
                largeSidebarNode.setVisible(largeSidebarNeedDisplay);
                largeSidebarNode.setManaged(largeSidebarNeedDisplay);
                if (getChildren().get(getChildren().size() - 1) != largeSidebarNode) {
                    ((NodeProperty) largeSidebarProperty()).setSuppressLayoutChanges(true);
                    largeSidebarNode.toFront();
                    ((NodeProperty) largeSidebarProperty()).setSuppressLayoutChanges(false);
                }
            } else {
                largeSidebarNode.setVisible(false);
                largeSidebarNode.setManaged(false);
            }
        }
    }

    private double computeNodeHeight(Node node, boolean isSideBarInHorizontal) {
        if (node == null || isSideBarInHorizontal) {
            return 0d;
        }
        return boundSize(node.prefHeight(-1), node.minHeight(-1), node.maxHeight(-1));
    }

    private double computeNodeWidth(Node node, boolean isSideBarInHorizontal) {
        if (node == null || !isSideBarInHorizontal) {
            return 0d;
        }
        return boundSize(node.prefWidth(-1), node.minWidth(-1), node.maxWidth(-1));
    }

    private double boundSize(double pref, double min, double max) {
        double a = Math.max(pref, min);
        double b = Math.max(min, max);
        return Math.min(a, b);
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
            return this;
        }

        @Override
        public String getName() {
            return propertyName;
        }
    }

}
