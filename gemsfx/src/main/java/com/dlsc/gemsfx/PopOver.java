package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PopOverSkin;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

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
 */
public class PopOver extends PopupControl {

    private static final String DEFAULT_STYLE_CLASS = "popover";

    private static final Duration DEFAULT_FADE_DURATION = Duration.millis(100);

    private static final double DEFAULT_ARROW_SIZE = 10;
    private static final double DEFAULT_ARROW_INDENT = 12;
    private static final double DEFAULT_CORNER_RADIUS = 6;

    private double targetX;

    private double targetY;

    private final StackPane root = new StackPane() {
        @Override
        public String getUserAgentStylesheet() {
            return requireNonNull(PopOver.class.getResource("popover.css")).toExternalForm();
        }
    };

    /**
     * Creates a popover with a label as the content node.
     */
    public PopOver() {
        super();
        // Store reference to this PopOver in bridge's properties for CSS access
        bridge.getProperties().put(PopOver.class, this);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        setOnHiding(evt -> setDetached(false));

        /*
         * Create some initial content.
         */
        Label label = new Label("Content");
        label.setPrefSize(200, 200);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        setContentNode(label);

        /*
         * A detached popover should not automatically hide itself.
         */
        detached.addListener(it -> setAutoHide(!isDetached()));

        setAutoHide(true);
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PopOverSkin(this);
    }

    /**
     * The root pane stores the content node of the popover. It is accessible
     * via this method to support proper styling.
     *
     * <h3>Example:</h3>
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
        @Override
        public void setValue(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("content node can not be null");
            }
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

    /**
     * Returns the value of the content property
     *
     * @return the content node
     * @see #contentNodeProperty()
     */
    public final Node getContentNode() {
        return contentNodeProperty().get();
    }

    /**
     * Sets the value of the content property.
     *
     * @param content the new content node value
     * @see #contentNodeProperty()
     */
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
        show(owner, 4);
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

        if (isShowing()) {
            return;
        }

        Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());

        switch (getArrowLocation()) {
            case BOTTOM_CENTER:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                show(owner, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + offset);
                break;
            case LEFT_BOTTOM:
            case LEFT_CENTER:
            case LEFT_TOP:
                show(owner, bounds.getMaxX() - offset, bounds.getMinY() + bounds.getHeight() / 2);
                break;
            case RIGHT_BOTTOM:
            case RIGHT_CENTER:
            case RIGHT_TOP:
                show(owner, bounds.getMinX() + offset, bounds.getMinY() + bounds.getHeight() / 2);
                break;
            case TOP_CENTER:
            case TOP_LEFT:
            case TOP_RIGHT:
                show(owner, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() - offset);
                break;
            default:
                break;
        }
    }

    /**
     * Makes the popover visible at the give location and associates it with
     * the given owner node. The x and y coordinate will be the target location
     * of the arrow of the popover and not the location of the window.
     *
     * @param owner the owning node
     * @param x     the x coordinate for the popover arrow tip
     * @param y     the y coordinate for the popover arrow tip
     */
    @Override
    public final void show(Node owner, double x, double y) {
        show(owner, x, y, getFadeInDuration());
    }

    /**
     * Makes the popover visible at the give location and associates it with
     * the given owner node. The x and y coordinate will be the target location
     * of the arrow of the popover and not the location of the window.
     *
     * @param owner          the owning node
     * @param x              the x coordinate for the popover arrow tip
     * @param y              the y coordinate for the popover arrow tip
     * @param fadeInDuration the time it takes for the popover to be fully visible. This duration takes precedence over the fade-in property without setting.
     */
    public final void show(Node owner, double x, double y, Duration fadeInDuration) {
        if (isShowing()) {
            return;
        }

        targetX = x;
        targetY = y;

        if (owner == null) {
            throw new IllegalArgumentException("owner can not be null");
        }

        if (fadeInDuration == null) {
            fadeInDuration = DEFAULT_FADE_DURATION;
        }

        super.show(owner, x, y);

        Bounds bounds = getContentNode().getBoundsInParent();

        Bounds rootBounds = root.getBoundsInParent();
        switch (getArrowLocation()) {
            case TOP_CENTER:
            case TOP_LEFT:
            case TOP_RIGHT:
                x -= computeXOffset();
                y += computeYOffset();
                break;
            case BOTTOM_CENTER:
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                x -= computeXOffset();
                y -= computeYOffset();
                break;
            case LEFT_TOP:
            case LEFT_CENTER:
            case LEFT_BOTTOM:
                x += computeXOffset();
                y -= computeYOffset();
                break;
            case RIGHT_TOP:
            case RIGHT_BOTTOM:
            case RIGHT_CENTER:
                x -= computeXOffset();
                y -= computeYOffset();
                break;
        }

        setX(x);
        setY(y);

        if (isAnimated()) {
            showFadeInAnimation(fadeInDuration);
        }
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
    private double computeXOffset() {
        final Bounds rootBounds = root.getBoundsInParent();
        final Bounds layoutBounds = root.getLayoutBounds();

        final double rootNodeWidth = layoutBounds.getWidth();
        final double rootNodeMinX = rootBounds.getMinX();

        final double cornerRadius = getCornerRadius();
        final double arrowIndent = getArrowIndent();
        final double arrowSize = getArrowSize();

        return switch (getArrowLocation()) {
            case TOP_LEFT, BOTTOM_LEFT -> cornerRadius + arrowIndent + arrowSize - rootNodeMinX;
            case TOP_CENTER, BOTTOM_CENTER -> rootNodeWidth / 2 - rootNodeMinX;
            case TOP_RIGHT, BOTTOM_RIGHT -> rootNodeWidth - arrowIndent - cornerRadius - arrowSize - rootNodeMinX;
            case LEFT_TOP, LEFT_CENTER, LEFT_BOTTOM -> rootNodeMinX + arrowSize;
            case RIGHT_TOP, RIGHT_CENTER, RIGHT_BOTTOM -> -rootNodeMinX + rootNodeWidth + arrowSize;
        };
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
    private double computeYOffset() {
        final Bounds rootBounds = root.getBoundsInParent();
        final Bounds layoutBounds = root.getLayoutBounds();

        final double rootNodeHeight = layoutBounds.getHeight();
        final double rootNodeMinY = rootBounds.getMinY();

        final double arrowIndent = getArrowIndent();
        final double arrowSize = getArrowSize();
        final double cornerRadius = getCornerRadius();

        return switch (getArrowLocation()) {
            case LEFT_TOP, RIGHT_TOP -> cornerRadius + arrowIndent + arrowSize - rootNodeMinY;
            case LEFT_CENTER, RIGHT_CENTER -> rootNodeHeight / 2 - rootNodeMinY;
            case LEFT_BOTTOM, RIGHT_BOTTOM -> rootNodeHeight - cornerRadius - arrowIndent - arrowSize - rootNodeMinY;
            case BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT -> rootNodeHeight - rootNodeMinY + arrowSize;
            case TOP_CENTER, TOP_LEFT, TOP_RIGHT -> rootNodeMinY + arrowSize;
        };
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
     */
    public final BooleanProperty detachableProperty() {
        return detachable;
    }

    /**
     * Sets the value of the detachable property.
     *
     * @param detachable if true, then the user can detach / tear off the popover
     * @see #detachableProperty()
     */
    public final void setDetachable(boolean detachable) {
        detachableProperty().set(detachable);
    }

    /**
     * Returns the value of the detachable property.
     *
     * @return true if the user is allowed to detach / tear off the popover
     * @see #detachableProperty()
     */
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

    /**
     * Sets the value of the detached property.
     *
     * @param detached if true, the popover will change its appearance to "detached" mode
     * @see #detachedProperty()
     */
    public final void setDetached(boolean detached) {
        detachedProperty().set(detached);
    }

    /**
     * Returns the value of the detached property.
     *
     * @return true if the popover is currently detached.
     * @see #detachedProperty()
     */
    public final boolean isDetached() {
        return detachedProperty().get();
    }

    // arrow size support

    private final DoubleProperty arrowSize = new StyleableDoubleProperty(DEFAULT_ARROW_SIZE) {
        @Override
        public Object getBean() {
            return PopOver.this;
        }

        @Override
        public String getName() {
            return "arrowSize";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.ARROW_SIZE;
        }
    };

    /**
     * Controls the size of the arrow. The default value is 10.
     *
     * @return the arrow size property
     */
    public final DoubleProperty arrowSizeProperty() {
        return arrowSize;
    }

    /**
     * Returns the value of the arrow size property.
     *
     * @return the arrow size property value
     * @see #arrowSizeProperty()
     */
    public final double getArrowSize() {
        return arrowSizeProperty().get();
    }

    /**
     * Sets the value of the arrow size property.
     *
     * @param size the new value of the arrow size property
     * @see #arrowSizeProperty()
     */
    public final void setArrowSize(double size) {
        arrowSizeProperty().set(size);
    }

    // arrow indent support

    private final DoubleProperty arrowIndent = new StyleableDoubleProperty(DEFAULT_ARROW_INDENT) {
        @Override
        public Object getBean() {
            return PopOver.this;
        }

        @Override
        public String getName() {
            return "arrowIndent";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.ARROW_INDENT;
        }
    };

    /**
     * Controls the distance between the arrow and the corners of the popover.
     * The default value is 12.
     *
     * @return the arrow indent property
     */
    public final DoubleProperty arrowIndentProperty() {
        return arrowIndent;
    }

    /**
     * Returns the value of the arrow indent property.
     *
     * @return the arrow indent value
     * @see #arrowIndentProperty()
     */
    public final double getArrowIndent() {
        return arrowIndentProperty().get();
    }

    /**
     * Sets the value of the arrow indent property.
     *
     * @param size the arrow indent value
     * @see #arrowIndentProperty()
     */
    public final void setArrowIndent(double size) {
        arrowIndentProperty().set(size);
    }

    // corner radius support

    private final DoubleProperty cornerRadius = new StyleableDoubleProperty(DEFAULT_CORNER_RADIUS) {
        @Override
        public Object getBean() {
            return PopOver.this;
        }

        @Override
        public String getName() {
            return "cornerRadius";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.CORNER_RADIUS;
        }
    };

    /**
     * Returns the corner radius property for the popover.
     *
     * @return the corner radius property (default is 6)
     */
    public final DoubleProperty cornerRadiusProperty() {
        return cornerRadius;
    }

    /**
     * Returns the value of the corner radius property.
     *
     * @return the corner radius
     * @see #cornerRadiusProperty()
     */
    public final double getCornerRadius() {
        return cornerRadiusProperty().get();
    }

    /**
     * Sets the value of the corner radius property.
     *
     * @param radius the corner radius
     * @see #cornerRadiusProperty()
     */
    public final void setCornerRadius(double radius) {
        cornerRadiusProperty().set(radius);
    }


    private final ObjectProperty<ArrowLocation> arrowLocation = new SimpleObjectProperty<>(this, "arrowLocation", ArrowLocation.LEFT_TOP);

    /**
     * Stores the preferred arrow location. This might not be the actual
     * location of the arrow if auto fix is enabled.
     *
     * @return the arrow location property
     * @see #setAutoFix(boolean)
     */
    public final ObjectProperty<ArrowLocation> arrowLocationProperty() {
        return arrowLocation;
    }

    /**
     * Sets the value of the arrow location property.
     *
     * @param location the requested location
     * @see #arrowLocationProperty()
     */
    public final void setArrowLocation(ArrowLocation location) {
        arrowLocationProperty().set(location);
    }

    /**
     * Returns the value of the arrow location property.
     *
     * @return the preferred arrow location
     * @see #arrowLocationProperty()
     */
    public final ArrowLocation getArrowLocation() {
        return arrowLocationProperty().get();
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

    /**
     * Returns the value of the fade-in duration property.
     *
     * @return the fade-in duration
     * @see #fadeInDurationProperty()
     */
    public final Duration getFadeInDuration() {
        return fadeInDurationProperty().get();
    }

    /**
     * Sets the value of the fade-in duration property. This should be set before calling PopOver.show(...).
     *
     * @param duration the requested fade-in duration
     * @see #fadeInDurationProperty()
     */
    public final void setFadeInDuration(Duration duration) {
        fadeInDurationProperty().setValue(duration);
    }

    /**
     * Returns the value of the fade-out duration property.
     *
     * @return the fade-out duration
     * @see #fadeOutDurationProperty()
     */
    public final Duration getFadeOutDuration() {
        return fadeOutDurationProperty().get();
    }

    /**
     * Sets the value of the fade-out duration property.
     *
     * @param duration the requested fade-out duration
     * @see #fadeOutDurationProperty()
     */
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

    /**
     * Returns the value of the "animated" property.
     *
     * @return true if the PopOver will be shown and hidden with a short fade animation
     * @see #animatedProperty()
     */
    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    /**
     * Sets the value of the "animated" property.
     *
     * @param animated if true, the PopOver will be shown and hidden with a short fade animation
     * @see #animatedProperty()
     */
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

    private static class StyleableProperties {

        private static final CssMetaData<CSSBridge, Number> ARROW_SIZE =
                new CssMetaData<>("-fx-arrow-size", SizeConverter.getInstance(), DEFAULT_ARROW_SIZE) {
                    @Override
                    public boolean isSettable(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return popOver != null && !popOver.arrowSize.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return (StyleableProperty<Number>) popOver.arrowSizeProperty();
                    }
                };

        private static final CssMetaData<CSSBridge, Number> ARROW_INDENT =
                new CssMetaData<>("-fx-arrow-indent", SizeConverter.getInstance(), DEFAULT_ARROW_INDENT) {
                    @Override
                    public boolean isSettable(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return popOver != null && !popOver.arrowIndent.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return (StyleableProperty<Number>) popOver.arrowIndentProperty();
                    }
                };

        private static final CssMetaData<CSSBridge, Number> CORNER_RADIUS =
                new CssMetaData<>("-fx-corner-radius", SizeConverter.getInstance(), DEFAULT_CORNER_RADIUS) {
                    @Override
                    public boolean isSettable(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return popOver != null && !popOver.cornerRadius.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(CSSBridge cssBridge) {
                        PopOver popOver = getPopOver(cssBridge);
                        return (StyleableProperty<Number>) popOver.cornerRadiusProperty();
                    }
                };

        private static PopOver getPopOver(CSSBridge cssBridge) {
            return (PopOver) cssBridge.getProperties().get(PopOver.class);
        }

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(PopupControl.getClassCssMetaData());
            styleables.add(ARROW_SIZE);
            styleables.add(ARROW_INDENT);
            styleables.add(CORNER_RADIUS);
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
}