package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PopOverSkin;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

import com.dlsc.gemsfx.util.ResourceBundleManager;

/**
 * A control that is intended to provide detailed information about
 * an owning node in a popup window. The popup window has a lightweight
 * appearance (no default window decorations) and an arrow pointing at the owner.
 * Due to the nature of popup windows, the popover will move around with the parent
 * window when the user drags it.
 * <p>
 * The Popover can be detached from the owning node by dragging it away from the
 * owner.
 *
 * <p>Example
 *
 * <pre>{@code
 * var textFlow = new TextFlow(new Text("Some content"));
 * textFlow.setPrefWidth(300);
 *
 * var popover = new Popover(textFlow);
 * var ownerLink = new Hyperlink("Show popover");
 * ownerLink.setOnAction(e -> popover.show(ownerLink));
 * }</pre>
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-arrow-indent}</td><td>{@code Double}</td><td>Distance between the arrow and popup corners.</td></tr>
 *     <tr><td>{@code -fx-arrow-size}</td><td>{@code Double}</td><td>The size of the arrow.</td></tr>
 *     <tr><td>{@code -fx-corner-radius}</td><td>{@code Double}</td><td>The corner radius of the popover.</td></tr>
 *   </tbody>
 * </table>
 */
public class PopOver extends PopupControl {
    private static final PseudoClass DETACHED_PSEUDO_CLASS = PseudoClass.getPseudoClass("detached");

    private static final String DEFAULT_STYLE_CLASS = "popover";

    private static final Duration DEFAULT_FADE_DURATION = Duration.millis(100);

    private static final double DEFAULT_ARROW_SIZE = 10;
    private static final double DEFAULT_ARROW_INDENT = 12;
    private static final double DEFAULT_CORNER_RADIUS = 6;
    public static final int DEFAULT_OFFSET = 4;

    private final PopOverRoot root = new PopOverRoot();

    /*
     * The owner's bounds (in screen coordinates) and the offset that were passed
     * to the last show(...) invocation. They are stored so that the popover can
     * recompute its location whenever the preferred size of the content node
     * changes while the popover is showing.
     */
    private Bounds ownerBounds;

    private double ownerOffset = DEFAULT_OFFSET;

    /*
     * Reacts to changes of the content node's layout bounds by recomputing the
     * popover's location and bounds so that the arrow keeps pointing at the owner
     * node correctly.
     */
    private final InvalidationListener contentBoundsListener = it -> {
        if (isShowing()) {
            updateLocation();
        }
    };

    private final WeakInvalidationListener weakContentBoundsListener = new WeakInvalidationListener(contentBoundsListener);

    /**
     * Creates a popover with a label as the content node.
     */
    public PopOver() {
        super();

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        setOnHiding(evt -> setDetached(false));

        /*
         * Keep a listener on the content node's layout bounds in sync so that the
         * popover can reposition itself when the content changes its size.
         */
        contentNode.addListener((obs, oldNode, newNode) -> {
            if (oldNode != null) {
                oldNode.layoutBoundsProperty().removeListener(weakContentBoundsListener);
            }
            if (newNode != null) {
                newNode.layoutBoundsProperty().addListener(weakContentBoundsListener);
            }
        });

        /*
         * Create some initial content.
         */
        Label label = new Label(ResourceBundleManager.getString(ResourceBundleManager.BundleType.POP_OVER, "content.default-label", "Content"));
        label.setPrefSize(200, 200);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        setContentNode(label);

        /*
         * A detached popover should not automatically hide itself.
         */
        detached.addListener(it -> {
            root.pseudoClassStateChanged(DETACHED_PSEUDO_CLASS, isDetached());
            setAutoHide(!isDetached());
        });

        setAutoHide(true);
        setAutoFix(false);

        /*
         * Keep the computed (effective) arrow location in sync with the preferred
         * one while the popover is not showing. When it is showing, the effective
         * location is managed by show(...) which may flip it to keep the popover
         * on screen.
         */
        arrowLocation.addListener((obs, oldLoc, newLoc) -> {
            if (!isShowing()) {
                setComputedArrowLocation(newLoc);
            }
        });
    }

    /**
     * Creates a popover with the given node as the content node.
     *
     * @param content The content shown by the popover
     */
    public PopOver(Node content) {
        this();

        setContentNode(content);
    }

    /**
     * Creates the default skin for this control.
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new PopOverSkin(this);
    }

    /**
     * The root pane stores the content node of the popover. It is accessible
     * via this method to support proper styling.
     *
     * <h4>Example:</h4>
     *
     * <pre>{@code
     * Popover popOver = new Popover();
     * popOver.getRoot().getStylesheets().add(...);
     * }</pre>
     *
     * @return the root pane
     */
    public final StackPane getRoot() {
        return root;
    }

    // Content support.

    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<Node>(this, "contentNode") {
        /**
         * Updates the content node shown by the popover.
         *
         * @param node the new content node
         */
        @Override
        public void setValue(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("content node can not be null");
            }
            super.setValue(node);
        }
    };

    /**
     * Returns the content shown by the popover.
     *
     * @return the content node property
     */
    public final ObjectProperty<Node> contentNodeProperty() {
        return contentNode;
    }

    public final Node getContentNode() {
        return contentNodeProperty().get();
    }

    public final void setContentNode(Node content) {
        contentNodeProperty().set(content);
    }

    /**
     * Shows the popover in a position relative to the edges of the given owner
     * node. The position is dependent on the arrow location. If the arrow is
     * pointing to the right, then the popover will be placed to the left of the
     * given owner. If the arrow points up, then the popover will be placed
     * below the given owner node. The arrow will slightly overlap with the
     * owner node.
     *
     * @param owner the owner of the popover
     */
    public final void show(Node owner) {
        show(owner, DEFAULT_OFFSET);
    }

    /**
     * Shows the popover in a position relative to the edges of the given owner
     * node. The position is dependent on the arrow location. If the arrow is
     * pointing to the right, then the popover will be placed to the left of the
     * given owner. If the arrow points up, then the popover will be placed
     * below the given owner node.
     *
     * @param owner  the owner of the popover
     * @param offset if negative specifies the distance to the owner node, or when
     *               positive specifies the number of pixels that the arrow will
     *               overlap with the owner node (positive values are recommended)
     */
    public final void show(Node owner, double offset) {
        requireNonNull(owner);
        show(owner, owner.localToScreen(owner.getBoundsInLocal()), offset);
    }

    public final void show(Node owner, Bounds bounds) {
        requireNonNull(owner);
        show(owner, bounds, DEFAULT_OFFSET);
    }

    /**
     * Shows the popover in a position relative to the given bounds (in screen
     * coordinates) of the owner node. The concrete side on which the popover
     * appears is derived from the {@link #arrowLocationProperty() arrow location},
     * which is treated as the <em>preferred</em> location: if the popover would
     * not fit on the preferred side of the owner within the visual bounds of the
     * screen, it is automatically flipped to the opposite side (Apple
     * {@code NSPopover}-style behavior). The public {@link #arrowLocationProperty()
     * arrow location} property is never modified by this method; the effective
     * location is exposed via {@link #computedArrowLocationProperty()}.
     *
     * @param owner  the owner of the popover
     * @param bounds the owner's bounds in screen coordinates that the popover
     *               points at
     * @param offset if positive specifies the number of pixels that the arrow will
     *               overlap with the owner node (positive values are recommended)
     */
    public final void show(Node owner, Bounds bounds, double offset) {
        requireNonNull(owner);
        requireNonNull(bounds);

        /*
         * If the popover is already showing, do not show it again: re-showing it
         * would re-run the fade-in animation (which resets the opacity to 0) and
         * cause a visible "blink".
         */
        if (isShowing()) {
            return;
        }

        /*
         * Realize the popup so that its scene graph is created and laid out. Only
         * then are the size and the "bounds in parent" of the root node (which
         * account for the drop shadow) available, both of which are required to
         * compute the final anchor position and to decide whether the popover has
         * to be flipped to stay on screen. The temporary location is corrected
         * right afterwards.
         */
        super.show(owner, bounds.getMinX(), bounds.getMinY());

        /*
         * Remember the parameters of this show(...) invocation so that the popover
         * can recompute its location when the content node's preferred size changes.
         */
        ownerBounds = bounds;
        ownerOffset = offset;

        updateLocation();

        if (isAnimated()) {
            showFadeInAnimation(getFadeInDuration());
        }
    }

    /*
     * Computes the effective arrow location and the anchor position of the popover
     * based on the last known owner bounds and offset. This is called by show(...)
     * and whenever the content node changes its preferred size while the popover is
     * showing, so that the arrow keeps pointing at the owner node correctly.
     */
    private void updateLocation() {
        if (ownerBounds == null) {
            return;
        }

        final Bounds bounds = ownerBounds;
        final double offset = ownerOffset;

        root.applyCss();
        root.layout();

        /*
         * Determine the effective arrow location (preferred location, flipped when
         * necessary to keep the popover on screen).
         */
        ArrowLocation location = determineArrowLocation(bounds);
        setComputedArrowLocation(location);

        /*
         * Re-run layout in case flipping the arrow location changed the required
         * size (e.g. because the arrow moved to another edge).
         */
        root.applyCss();
        root.layout();

        final double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2;
        final double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2;

        double targetX;
        double targetY;

        switch (location) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                // popover below the owner, arrow points up at the owner's bottom edge
                targetX = centerX;
                targetY = bounds.getMaxY() - offset;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                // popover above the owner, arrow points down at the owner's top edge
                targetX = centerX;
                targetY = bounds.getMinY() + offset;
                break;
            case LEFT_TOP:
            case LEFT_CENTER:
            case LEFT_BOTTOM:
                // popover to the right of the owner, arrow points at the owner's right edge
                targetX = bounds.getMaxX() - offset;
                targetY = centerY;
                break;
            case RIGHT_TOP:
            case RIGHT_CENTER:
            case RIGHT_BOTTOM:
                // popover to the left of the owner, arrow points at the owner's left edge
                targetX = bounds.getMinX() + offset;
                targetY = centerY;
                break;
            default:
                throw new IllegalStateException("Unexpected arrow location: " + location);
        }

        setAnchorX(targetX - computeXOffset(location));
        setAnchorY(targetY - computeYOffset(location));
    }

    private void showFadeInAnimation(Duration fadeInDuration) {
        // Fade In
        Node skinNode = getSkin().getNode();
        skinNode.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(fadeInDuration, skinNode);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /**
     * Hides the popover by quickly changing its opacity to 0.
     *
     * @see #hide(Duration)
     */
    @Override
    public final void hide() {
        hide(getFadeOutDuration());
    }

    /**
     * Hides the popover by quickly changing its opacity to 0.
     *
     * @param fadeOutDuration the duration of the fade transition that is being used to
     *                        change the opacity of the popover
     * @since 1.0
     */
    public final void hide(Duration fadeOutDuration) {
        if (fadeOutDuration == null) {
            fadeOutDuration = DEFAULT_FADE_DURATION;
        }

        if (isShowing()) {
            if (isAnimated()) {
                // Fade Out
                Node skinNode = getSkin().getNode();

                FadeTransition fadeOut = new FadeTransition(fadeOutDuration, skinNode);
                fadeOut.setFromValue(skinNode.getOpacity());
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(evt -> super.hide());
                fadeOut.play();
            } else {
                super.hide();
            }
        }
    }

    /*
     * Calculates how much the popover has to be moved left / right depending on the arrow location. There is some
     * complexity involved as the "layout bounds" and the "bounds in parent" are different because of the drop shadow
     * effect that is normally applied to the popover. The other variables are the corner radius, the arrow size, and
     * the arrow indentation.
     *
     * The offset is always relative to the main four handle positions of the owning node. The positons are located in
     * the center of the top edge, bottom edge, left edge, and right edge.
     */
    private double computeXOffset(ArrowLocation arrowLocation) {
        final Bounds rootBounds = root.getBoundsInParent();
        final Bounds layoutBounds = root.getLayoutBounds();

        final double rootNodeWidth = layoutBounds.getWidth();
        final double rootNodeMinX = rootBounds.getMinX();

        final double cornerRadius = getCornerRadius();
        final double arrowIndent = getArrowIndent();
        final double arrowSize = getArrowSize();

        switch (arrowLocation) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
                return cornerRadius + arrowIndent + arrowSize - rootNodeMinX;
            case TOP_CENTER:
            case BOTTOM_CENTER:
                return rootNodeWidth / 2 - rootNodeMinX;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return rootNodeWidth - arrowIndent - cornerRadius - arrowSize - rootNodeMinX;
            case LEFT_TOP:
            case LEFT_CENTER:
            case LEFT_BOTTOM:
                return -rootNodeMinX - arrowSize;
            case RIGHT_TOP:
            case RIGHT_CENTER:
            case RIGHT_BOTTOM:
                return -rootNodeMinX + rootNodeWidth + arrowSize;
            default:
                throw new IllegalStateException("Unexpected arrow location: " + getArrowLocation());
        }
    }

    /*
     * Calculates how much the popover has to be moved up / down depending on the arrow location. There is some
     * complexity involved as the "layout bounds" and the "bounds in parent" are different because of the drop shadow
     * effect that is normally applied to the popover. The other variables are the corner radius, the arrow size, and
     * the arrow indentation.
     *
     * The offset is always relative to the main four handle positions of the owning node. The positons are located in
     * the center of the top edge, bottom edge, left edge, and right edge.
     */
    private double computeYOffset(ArrowLocation arrowLocation) {
        final Bounds rootBounds = root.getBoundsInParent();
        final Bounds layoutBounds = root.getLayoutBounds();

        final double rootNodeHeight = layoutBounds.getHeight();
        final double rootNodeMinY = rootBounds.getMinY();

        final double arrowIndent = getArrowIndent();
        final double arrowSize = getArrowSize();
        final double cornerRadius = getCornerRadius();

        switch (arrowLocation) {
            case LEFT_TOP:
            case RIGHT_TOP:
                return cornerRadius + arrowIndent + arrowSize - rootNodeMinY;
            case LEFT_CENTER:
            case RIGHT_CENTER:
                return rootNodeHeight / 2 - rootNodeMinY;
            case LEFT_BOTTOM:
            case RIGHT_BOTTOM:
                return rootNodeHeight - cornerRadius - arrowIndent - arrowSize - rootNodeMinY;
            case BOTTOM_CENTER:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                return rootNodeHeight - rootNodeMinY + arrowSize;
            case TOP_CENTER:
            case TOP_LEFT:
            case TOP_RIGHT:
                return -rootNodeMinY - arrowSize;
            default:
                throw new IllegalStateException("Unexpected arrow location: " + getArrowLocation());
        }
    }

    /**
     * Detaches the popover from the owning node. The popover will no longer
     * display an arrow pointing at the owner node.
     */
    public final void detach() {
        if (isDetachable()) {
            setDetached(true);
        }
    }

    // detach support

    private final BooleanProperty detachable = new SimpleBooleanProperty(this, "detachable");

    /**
     * Determines if the popover is detachable at all.
     *
     * @return the detachable property
     */
    public final BooleanProperty detachableProperty() {
        return detachable;
    }

    public final void setDetachable(boolean detachable) {
        detachableProperty().set(detachable);
    }

    public final boolean isDetachable() {
        return detachableProperty().get();
    }

    private final BooleanProperty detached = new SimpleBooleanProperty(this, "detached", false);

    /**
     * Determines whether the popover is detached from the owning node or not.
     * A detached popover no longer shows an arrow pointing at the owner and
     * features its own title bar.
     *
     * @return the detached property
     */
    public final BooleanProperty detachedProperty() {
        return detached;
    }

    public final void setDetached(boolean detached) {
        detachedProperty().set(detached);
    }

    public final boolean isDetached() {
        return detachedProperty().get();
    }

    // arrow size support

    /**
     * Controls the size of the arrow. The default value is 10.
     *
     * @return the arrow size property
     */
    public final DoubleProperty arrowSizeProperty() {
        return root.arrowSizeProperty();
    }

    public final double getArrowSize() {
        return arrowSizeProperty().get();
    }

    public final void setArrowSize(double size) {
        arrowSizeProperty().set(size);
    }

    // arrow indent support

    /**
     * Controls the distance between the arrow and the corners of the popover.
     * The default value is 12.
     *
     * @return the arrow indent property
     */
    public final DoubleProperty arrowIndentProperty() {
        return root.arrowIndentProperty();
    }

    public final double getArrowIndent() {
        return arrowIndentProperty().get();
    }

    public final void setArrowIndent(double size) {
        arrowIndentProperty().set(size);
    }

    // radius support

    /**
     * Returns the corner radius property for the popover.
     *
     * @return the corner radius property (default is 6)
     */
    public final DoubleProperty cornerRadiusProperty() {
        return root.cornerRadiusProperty();
    }

    public final double getCornerRadius() {
        return cornerRadiusProperty().get();
    }

    public final void setCornerRadius(double radius) {
        cornerRadiusProperty().set(radius);
    }

    private final ObjectProperty<ArrowLocation> arrowLocation = new SimpleObjectProperty<>(this, "arrowLocation", ArrowLocation.TOP_CENTER);

    public final ObjectProperty<ArrowLocation> arrowLocationProperty() {
        return arrowLocation;
    }

    public final void setArrowLocation(ArrowLocation location) {
        arrowLocationProperty().set(location);
    }

    public final ArrowLocation getArrowLocation() {
        return arrowLocationProperty().get();
    }

    private ArrowLocation determineArrowLocation(Bounds ownerBounds) {
        return findPopOverArrowLocation(getArrowLocation(), ownerBounds);
    }

    /**
     * Determines the effective arrow location for the given preferred location and
     * owner bounds. The preferred location is honored whenever the popover fits on
     * the corresponding side of the owner within the visual bounds of the screen.
     * Otherwise the location is flipped to the opposite side if that side offers
     * enough room. A {@code null} preferred location triggers a fully automatic
     * choice of the side with the most available space.
     * <p>
     * After the side (top / bottom / left / right) has been decided, the alignment
     * along that edge (i.e. the {@code *_LEFT} / {@code *_CENTER} / {@code *_RIGHT}
     * or {@code *_TOP} / {@code *_CENTER} / {@code *_BOTTOM} suffix) is adjusted so
     * that the popover does not stick out of the screen along the edge either.
     */
    private ArrowLocation findPopOverArrowLocation(ArrowLocation preferred, Bounds ownerBounds) {
        final Bounds popBounds = root.getBoundsInParent();
        final double popWidth = popBounds.getWidth();
        final double popHeight = popBounds.getHeight();

        final Rectangle2D screen = getScreenBounds(ownerBounds);

        final double spaceAbove = ownerBounds.getMinY() - screen.getMinY();
        final double spaceBelow = screen.getMaxY() - ownerBounds.getMaxY();
        final double spaceLeft = ownerBounds.getMinX() - screen.getMinX();
        final double spaceRight = screen.getMaxX() - ownerBounds.getMaxX();

        // 1. Decide on which side of the owner the popover is placed (and flip it
        //    to the opposite side if the preferred side lacks room).
        ArrowLocation side;
        if (preferred == null) {
            if (spaceBelow >= popHeight) {
                side = ArrowLocation.TOP_CENTER;
            } else if (spaceAbove >= popHeight) {
                side = ArrowLocation.BOTTOM_CENTER;
            } else if (spaceRight >= popWidth) {
                side = ArrowLocation.LEFT_CENTER;
            } else if (spaceLeft >= popWidth) {
                side = ArrowLocation.RIGHT_CENTER;
            } else {
                side = ArrowLocation.TOP_CENTER;
            }
        } else {
            switch (preferred) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    // popover below the owner
                    side = (spaceBelow < popHeight && spaceAbove >= popHeight) ? flipVertically(preferred) : preferred;
                    break;
                case BOTTOM_LEFT:
                case BOTTOM_CENTER:
                case BOTTOM_RIGHT:
                    // popover above the owner
                    side = (spaceAbove < popHeight && spaceBelow >= popHeight) ? flipVertically(preferred) : preferred;
                    break;
                case LEFT_TOP:
                case LEFT_CENTER:
                case LEFT_BOTTOM:
                    // popover to the right of the owner
                    side = (spaceRight < popWidth && spaceLeft >= popWidth) ? flipHorizontally(preferred) : preferred;
                    break;
                case RIGHT_TOP:
                case RIGHT_CENTER:
                case RIGHT_BOTTOM:
                    // popover to the left of the owner
                    side = (spaceLeft < popWidth && spaceRight >= popWidth) ? flipHorizontally(preferred) : preferred;
                    break;
                default:
                    side = preferred;
                    break;
            }
        }

        // 2. Adjust the alignment along the chosen edge so the popover stays on
        //    screen in the direction parallel to the edge as well.
        return fitAlongEdge(side, ownerBounds, screen);
    }

    /**
     * Picks the alignment suffix (along the edge) that keeps the popover within the
     * screen's visual bounds parallel to the edge, while staying as close as
     * possible to the requested alignment. The exact tip position of each candidate
     * is obtained from {@link #computeXOffset(ArrowLocation)} /
     * {@link #computeYOffset(ArrowLocation)}, so it matches how {@code show(...)}
     * ultimately anchors the popover.
     */
    private ArrowLocation fitAlongEdge(ArrowLocation location, Bounds ownerBounds, Rectangle2D screen) {
        final ArrowLocation[] candidates;
        final boolean horizontal;

        switch (location) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                candidates = new ArrowLocation[]{ArrowLocation.TOP_LEFT, ArrowLocation.TOP_CENTER, ArrowLocation.TOP_RIGHT};
                horizontal = true;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                candidates = new ArrowLocation[]{ArrowLocation.BOTTOM_LEFT, ArrowLocation.BOTTOM_CENTER, ArrowLocation.BOTTOM_RIGHT};
                horizontal = true;
                break;
            case LEFT_TOP:
            case LEFT_CENTER:
            case LEFT_BOTTOM:
                candidates = new ArrowLocation[]{ArrowLocation.LEFT_TOP, ArrowLocation.LEFT_CENTER, ArrowLocation.LEFT_BOTTOM};
                horizontal = false;
                break;
            case RIGHT_TOP:
            case RIGHT_CENTER:
            case RIGHT_BOTTOM:
                candidates = new ArrowLocation[]{ArrowLocation.RIGHT_TOP, ArrowLocation.RIGHT_CENTER, ArrowLocation.RIGHT_BOTTOM};
                horizontal = false;
                break;
            default:
                return location;
        }

        final Bounds popBounds = root.getBoundsInParent();
        final double size = horizontal ? popBounds.getWidth() : popBounds.getHeight();
        final double center = horizontal
                ? (ownerBounds.getMinX() + ownerBounds.getMaxX()) / 2
                : (ownerBounds.getMinY() + ownerBounds.getMaxY()) / 2;
        final double screenMin = horizontal ? screen.getMinX() : screen.getMinY();
        final double screenMax = horizontal ? screen.getMaxX() : screen.getMaxY();

        ArrowLocation best = location;
        double bestOverflow = Double.MAX_VALUE;
        double preferredOverflow = Double.MAX_VALUE;

        for (ArrowLocation candidate : candidates) {
            // Tip offset from the popover's leading (left / top) edge, i.e. where
            // the arrow tip sits within the popover window for this candidate.
            final double tip = horizontal ? computeXOffset(candidate) : computeYOffset(candidate);
            final double start = center - tip;
            final double end = start + size;
            final double overflow = Math.max(0, screenMin - start) + Math.max(0, end - screenMax);

            if (candidate == location) {
                preferredOverflow = overflow;
            }
            if (overflow < bestOverflow) {
                bestOverflow = overflow;
                best = candidate;
            }
        }

        // Keep the requested alignment when it is already among the best fits.
        return preferredOverflow <= bestOverflow ? location : best;
    }

    private Rectangle2D getScreenBounds(Bounds ownerBounds) {
        List<Screen> screens = Screen.getScreensForRectangle(
                ownerBounds.getMinX(), ownerBounds.getMinY(),
                Math.max(1, ownerBounds.getWidth()), Math.max(1, ownerBounds.getHeight()));
        Screen screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
        return screen.getVisualBounds();
    }

    private static ArrowLocation flipVertically(ArrowLocation location) {
        switch (location) {
            case TOP_LEFT:
                return ArrowLocation.BOTTOM_LEFT;
            case TOP_CENTER:
                return ArrowLocation.BOTTOM_CENTER;
            case TOP_RIGHT:
                return ArrowLocation.BOTTOM_RIGHT;
            case BOTTOM_LEFT:
                return ArrowLocation.TOP_LEFT;
            case BOTTOM_CENTER:
                return ArrowLocation.TOP_CENTER;
            case BOTTOM_RIGHT:
                return ArrowLocation.TOP_RIGHT;
            default:
                return location;
        }
    }

    private static ArrowLocation flipHorizontally(ArrowLocation location) {
        switch (location) {
            case LEFT_TOP:
                return ArrowLocation.RIGHT_TOP;
            case LEFT_CENTER:
                return ArrowLocation.RIGHT_CENTER;
            case LEFT_BOTTOM:
                return ArrowLocation.RIGHT_BOTTOM;
            case RIGHT_TOP:
                return ArrowLocation.LEFT_TOP;
            case RIGHT_CENTER:
                return ArrowLocation.LEFT_CENTER;
            case RIGHT_BOTTOM:
                return ArrowLocation.LEFT_BOTTOM;
            default:
                return location;
        }
    }

    // computed (effective) arrow location support

    private final ReadOnlyObjectWrapper<ArrowLocation> computedArrowLocation = new ReadOnlyObjectWrapper<>(this, "computedArrowLocation", ArrowLocation.TOP_LEFT);

    /**
     * The effective arrow location that is actually used to draw and position the
     * popover. It equals the preferred {@link #arrowLocationProperty() arrow
     * location} unless the popover had to be flipped to the opposite side in order
     * to stay on screen. Skins should render the arrow based on this property
     * rather than on {@link #arrowLocationProperty()}.
     *
     * @return the read-only computed arrow location property
     */
    public final ReadOnlyObjectProperty<ArrowLocation> computedArrowLocationProperty() {
        return computedArrowLocation.getReadOnlyProperty();
    }

    public final ArrowLocation getComputedArrowLocation() {
        return computedArrowLocation.get();
    }

    private void setComputedArrowLocation(ArrowLocation location) {
        computedArrowLocation.set(location);
    }

    /**
     * Locations where the arrow of the popover can be placed.
     */
    public enum ArrowLocation {
        LEFT_TOP,
        LEFT_CENTER,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_CENTER,
        RIGHT_BOTTOM,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT;
    }

    private final ObjectProperty<Duration> fadeInDuration = new SimpleObjectProperty<>(this, "fadeInDuration", DEFAULT_FADE_DURATION);

    /**
     * Stores the fade-in duration. This should be set before calling PopOver.show(…).
     *
     * @return the fade-in duration property
     */
    public final ObjectProperty<Duration> fadeInDurationProperty() {
        return fadeInDuration;
    }

    private final ObjectProperty<Duration> fadeOutDuration = new SimpleObjectProperty<>(this, "fadeOutDuration", DEFAULT_FADE_DURATION);

    /**
     * Stores the fade-out duration.
     *
     * @return the fade-out duration property
     */
    public final ObjectProperty<Duration> fadeOutDurationProperty() {
        return fadeOutDuration;
    }

    public final Duration getFadeInDuration() {
        return fadeInDurationProperty().get();
    }

    public final void setFadeInDuration(Duration duration) {
        fadeInDurationProperty().setValue(duration);
    }

    public final Duration getFadeOutDuration() {
        return fadeOutDurationProperty().get();
    }

    public final void setFadeOutDuration(Duration duration) {
        fadeOutDurationProperty().setValue(duration);
    }

    private final SimpleBooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);

    /**
     * Stores the "animated" flag. If true, then the PopOver will be shown / hidden with a short fade in / out animation.
     *
     * @return the "animated" property
     */
    public final BooleanProperty animatedProperty() {
        return animated;
    }

    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    public final void setAnimated(boolean animated) {
        animatedProperty().set(animated);
    }

    /**
     * A specialized popover to let the user select a date or a date range.
     */
    public static class CalendarPopOver extends PopOver {

        private final CalendarView calendarView = new CalendarView();

        /**
         * Constructs a new calendar popover for selecting a single date.
         */
        public CalendarPopOver() {
            this(false);
        }

        /**
         * Constructs a new calendar popover for selecting a date or a date range.
         *
         * @param selectRange determines the selection mode
         */
        public CalendarPopOver(boolean selectRange) {
            getStyleClass().add("calendar-popover");

            calendarView.getStyleClass().add("popover");
            calendarView.getYearView().getStyleClass().add("popover");
            calendarView.getYearMonthView().getStyleClass().add("popover");
            if (selectRange) {
                calendarView.getSelectionModel().setSelectionMode(CalendarView.SelectionModel.SelectionMode.DATE_RANGE);
                calendarView.getSelectionModel().selectedEndDateProperty().addListener(it -> hide());
            } else {
                calendarView.getSelectionModel().setSelectionMode(CalendarView.SelectionModel.SelectionMode.SINGLE_DATE);
                calendarView.getSelectionModel().selectedDateProperty().addListener(it -> hide());
            }

            setContentNode(calendarView);
        }

        /**
         * Returns the internally used calendar view.
         *
         * @return the actual calendar view
         */
        public final CalendarView getCalendarView() {
            return calendarView;
        }
    }

    private static final class PopOverRoot extends StackPane {

        /**
         * Controls the size of the arrow. The default value is 10.
         * <p>
         * Can be set via CSS using the {@code -fx-arrow-size} property.
         * Valid values are: positive numbers.
         * The default value is {@code 10}.
         * </p>
         *
         * @return the arrow size property
         */
        public final DoubleProperty arrowSizeProperty() {
            return arrowSize;
        }

        private final DoubleProperty arrowSize = new StyleableDoubleProperty(DEFAULT_ARROW_SIZE) {
            /**
             * {@inheritDoc}
             *
             * @return the owning bean
             */
            @Override
            public Object getBean() {
                return PopOverRoot.this;
            }

            /**
             * {@inheritDoc}
             *
             * @return the property name
             */
            @Override
            public String getName() {
                return "arrowSize";
            }

            /**
             * {@inheritDoc}
             *
             * @return the CSS metadata for this property
             */
            @Override
            public CssMetaData<PopOverRoot, Number> getCssMetaData() {
                return StyleableProperties.ARROW_SIZE;
            }
        };

        /**
         * Controls the distance between the arrow and the corners of the popover.
         * <p>
         * Can be set via CSS using the {@code -fx-arrow-indent} property.
         * Valid values are: positive numbers.
         * The default value is {@code 12}.
         * </p>
         *
         * @return the arrow indent property
         */
        public final DoubleProperty arrowIndentProperty() {
            return arrowIndent;
        }

        private final DoubleProperty arrowIndent = new StyleableDoubleProperty(DEFAULT_ARROW_INDENT) {
            /**
             * {@inheritDoc}
             *
             * @return the owning bean
             */
            @Override
            public Object getBean() {
                return PopOverRoot.this;
            }

            /**
             * {@inheritDoc}
             *
             * @return the property name
             */
            @Override
            public String getName() {
                return "arrowIndent";
            }

            /**
             * {@inheritDoc}
             *
             * @return the CSS metadata for this property
             */
            @Override
            public CssMetaData<PopOverRoot, Number> getCssMetaData() {
                return StyleableProperties.ARROW_INDENT;
            }
        };

        /**
         * Returns the corner radius property for the popover.
         * <p>
         * Can be set via CSS using the {@code -fx-corner-radius} property.
         * Valid values are: positive numbers.
         * The default value is {@code 6}.
         * </p>
         *
         * @return the corner radius property
         */
        public final DoubleProperty cornerRadiusProperty() {
            return cornerRadius;
        }

        private final DoubleProperty cornerRadius = new StyleableDoubleProperty(DEFAULT_CORNER_RADIUS) {
            /**
             * {@inheritDoc}
             *
             * @return the owning bean
             */
            @Override
            public Object getBean() {
                return PopOverRoot.this;
            }

            /**
             * {@inheritDoc}
             *
             * @return the property name
             */
            @Override
            public String getName() {
                return "cornerRadius";
            }

            /**
             * {@inheritDoc}
             *
             * @return the CSS metadata for this property
             */
            @Override
            public CssMetaData<PopOverRoot, Number> getCssMetaData() {
                return StyleableProperties.CORNER_RADIUS;
            }
        };

        /**
         * Returns the stylesheet used by this control.
         *
         * @return the user agent stylesheet
         */
        @Override
        public String getUserAgentStylesheet() {
            return requireNonNull(PopOver.class.getResource("popover.css")).toExternalForm();
        }

        /**
         * Returns the CSS metadata supported by this control.
         *
         * @return the class CSS metadata
         */
        public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
            return StyleableProperties.STYLEABLES;
        }

        /**
         * Returns the CSS metadata supported by this control.
         *
         * @return the control CSS metadata
         */
        @Override
        public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
            return getClassCssMetaData();
        }

        private static class StyleableProperties {

            private static final CssMetaData<PopOverRoot, Number> ARROW_SIZE =
                    new CssMetaData<>("-fx-arrow-size", SizeConverter.getInstance(), DEFAULT_ARROW_SIZE) {
                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return true if the property can be styled
                         */
                        @Override
                        public boolean isSettable(PopOverRoot node) {
                            return !node.arrowSize.isBound();
                        }

                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return the styleable property
                         */
                        @Override
                        public StyleableProperty<Number> getStyleableProperty(PopOverRoot node) {
                            return (StyleableProperty<Number>) node.arrowSizeProperty();
                        }
                    };

            private static final CssMetaData<PopOverRoot, Number> ARROW_INDENT =
                    new CssMetaData<>("-fx-arrow-indent", SizeConverter.getInstance(), DEFAULT_ARROW_INDENT) {
                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return true if the property can be styled
                         */
                        @Override
                        public boolean isSettable(PopOverRoot node) {
                            return !node.arrowIndent.isBound();
                        }

                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return the styleable property
                         */
                        @Override
                        public StyleableProperty<Number> getStyleableProperty(PopOverRoot node) {
                            return (StyleableProperty<Number>) node.arrowIndentProperty();
                        }
                    };

            private static final CssMetaData<PopOverRoot, Number> CORNER_RADIUS =
                    new CssMetaData<>("-fx-corner-radius", SizeConverter.getInstance(), DEFAULT_CORNER_RADIUS) {
                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return true if the property can be styled
                         */
                        @Override
                        public boolean isSettable(PopOverRoot node) {
                            return !node.cornerRadius.isBound();
                        }

                        /**
                         * {@inheritDoc}
                         *
                         * @param node the control to inspect
                         * @return the styleable property
                         */
                        @Override
                        public StyleableProperty<Number> getStyleableProperty(PopOverRoot node) {
                            return (StyleableProperty<Number>) node.cornerRadiusProperty();
                        }
                    };

            private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

            static {
                final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(StackPane.getClassCssMetaData());
                styleables.add(ARROW_SIZE);
                styleables.add(ARROW_INDENT);
                styleables.add(CORNER_RADIUS);
                STYLEABLES = Collections.unmodifiableList(styleables);
            }
        }
    }
}
