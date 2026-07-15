package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.Skeleton;
import com.dlsc.gemsfx.Skeleton.Variant;
import com.dlsc.gemsfx.util.TreeShowing;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Default skin for {@link Skeleton}. Renders the base block driven by the
 * control's {@link Variant}, overlays a configurable shimmer band, and scrolls
 * that band horizontally on an indefinite {@link Timeline}.
 */
public class SkeletonSkin extends GemsSkinBase<Skeleton> {

    private static final double DEFAULT_PREF_WIDTH = 120.0;
    private static final double DEFAULT_PREF_HEIGHT = 16.0;
    private static final double DEFAULT_CIRCULAR_SIZE = 48.0;
    private static final double HALF = 0.5;
    private static final double FULL_PERCENT = 100.0;

    private final Group baseLayer = new Group();
    private final Group shimmerViewport = new Group();
    private final Group clipLayer = new Group();
    private final Rectangle shimmerBand = new Rectangle();
    private final BooleanProperty treeShowing;

    private Timeline shimmerTimeline;
    private double cachedContentWidth;
    private double cachedBandWidth;
    private double timelineBandWidth = Double.NaN;
    private double timelineSpan = Double.NaN;
    private Duration timelineCycle;

    /**
     * Constructs a skin for the given control.
     *
     * @param control the skinnable control
     */
    public SkeletonSkin(Skeleton control) {
        super(control);

        initNodes();
        treeShowing = TreeShowing.treeShowing(control);
        registerListeners(control);
        applyBaseFill();
        applyShimmerFill();
    }

    private void initNodes() {
        baseLayer.getStyleClass().add("base-layer");
        baseLayer.setManaged(false);
        baseLayer.setMouseTransparent(true);

        shimmerViewport.getStyleClass().add("shimmer-viewport");
        shimmerViewport.setManaged(false);
        shimmerViewport.setMouseTransparent(true);
        shimmerViewport.setClip(clipLayer);

        clipLayer.getStyleClass().add("clip-layer");
        clipLayer.setManaged(false);
        clipLayer.setMouseTransparent(true);

        shimmerBand.getStyleClass().add("shimmer-band");
        shimmerBand.setManaged(false);
        shimmerBand.setMouseTransparent(true);

        shimmerViewport.getChildren().setAll(shimmerBand);
        getChildren().setAll(baseLayer, shimmerViewport);
    }

    private void registerListeners(Skeleton control) {
        register(control.variantProperty(), it -> control.requestLayout());
        register(control.cornerRadiusProperty(), it -> control.requestLayout());

        register(control.baseColorProperty(), it -> applyBaseFill());
        register(control.shimmerFillProperty(), it -> applyShimmerFill());

        register(control.cycleDurationProperty(), it -> rebuildShimmerTimeline());
        register(control.shimmerWidthProperty(), it -> control.requestLayout());

        register(control.lineCountProperty(), it -> control.requestLayout());
        register(control.lineHeightProperty(), it -> control.requestLayout());
        register(control.lineSpacingProperty(), it -> control.requestLayout());
        register(control.lastLineFillPercentProperty(), it -> control.requestLayout());

        register(treeShowing, (obs, wasShowing, isShowing) -> onTreeShowingChanged(isShowing));
    }

    private void applyBaseFill() {
        Paint fill = getSkinnable().getBaseColor();
        for (Node node : baseLayer.getChildren()) {
            if (node instanceof Rectangle) {
                ((Rectangle) node).setFill(fill);
            }
        }
    }

    private void applyShimmerFill() {
        shimmerBand.setFill(getSkinnable().getShimmerFill());
    }

    private void rebuildShimmerTimeline() {
        Duration cycle = getSkinnable().getCycleDuration();
        boolean disabled = cycle == null || cycle.isUnknown() || cycle.isIndefinite()
                || cycle.lessThanOrEqualTo(Duration.ZERO);
        double bandWidth = cachedBandWidth;
        double span = cachedContentWidth + bandWidth;

        if (disabled || bandWidth <= 0.0 || cachedContentWidth <= 0.0) {
            stopAndClearTimeline();
            shimmerBand.setTranslateX(-bandWidth);
            timelineBandWidth = Double.NaN;
            timelineSpan = Double.NaN;
            timelineCycle = null;
            return;
        }

        boolean unchanged = bandWidth == timelineBandWidth
                && span == timelineSpan
                && durationEquals(cycle, timelineCycle)
                && shimmerTimeline != null;
        if (unchanged) {
            return;
        }

        stopAndClearTimeline();
        shimmerTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(shimmerBand.translateXProperty(), -bandWidth, Interpolator.LINEAR)),
                new KeyFrame(cycle,
                        new KeyValue(shimmerBand.translateXProperty(), cachedContentWidth, Interpolator.LINEAR))
        );
        shimmerTimeline.setCycleCount(Animation.INDEFINITE);
        if (treeShowing.get()) {
            shimmerTimeline.play();
        }

        timelineBandWidth = bandWidth;
        timelineSpan = span;
        timelineCycle = cycle;
    }

    private void stopAndClearTimeline() {
        if (shimmerTimeline != null) {
            shimmerTimeline.stop();
            shimmerTimeline = null;
        }
    }

    private static boolean durationEquals(Duration a, Duration b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.toMillis() == b.toMillis();
    }

    private void onTreeShowingChanged(boolean showing) {
        if (shimmerTimeline == null) {
            return;
        }

        if (showing) {
            shimmerTimeline.play();
        } else {
            shimmerTimeline.pause();
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY,
                                  double contentWidth, double contentHeight) {
        cachedContentWidth = contentWidth;

        if (contentWidth <= 0.0 || contentHeight <= 0.0) {
            collapseAll();
            return;
        }

        List<Block> blocks = computeBlocks(variantOrDefault(), contentWidth, contentHeight);
        if (blocks.isEmpty()) {
            collapseAll();
            return;
        }

        syncLayer(baseLayer, blocks, contentX, contentY, getSkinnable().getBaseColor(), "base-block");
        syncLayer(clipLayer, blocks, 0.0, 0.0, Color.BLACK, "clip-block");

        layoutShimmer(contentX, contentY, contentWidth, contentHeight);
        rebuildShimmerTimeline();
    }

    private void collapseAll() {
        baseLayer.getChildren().clear();
        clipLayer.getChildren().clear();
        positionShimmerViewport(0.0, 0.0);
        shimmerBand.setWidth(0.0);
        shimmerBand.setHeight(0.0);
        cachedBandWidth = 0.0;
        rebuildShimmerTimeline();
    }

    private Variant variantOrDefault() {
        Variant variant = getSkinnable().getVariant();
        return variant == null ? Skeleton.DEFAULT_VARIANT : variant;
    }

    private List<Block> computeBlocks(Variant variant, double contentWidth, double contentHeight) {
        List<Block> blocks = new ArrayList<>();

        switch (variant) {
            case CIRCULAR:
                double diameter = Math.min(contentWidth, contentHeight);
                double offsetX = (contentWidth - diameter) * HALF;
                double offsetY = (contentHeight - diameter) * HALF;
                blocks.add(new Block(offsetX, offsetY, diameter, diameter, diameter, diameter));
                break;
            case TEXT:
                addTextBlocks(blocks, contentWidth, contentHeight);
                break;
            case ROUNDED_RECTANGLE:
            default:
                double radius = sanitizeNonNegative(getSkinnable().getCornerRadius());
                blocks.add(new Block(0.0, 0.0, contentWidth, contentHeight, radius * 2.0, radius * 2.0));
                break;
        }

        return blocks;
    }

    private void addTextBlocks(List<Block> blocks, double contentWidth, double contentHeight) {
        double lineHeight = sanitizeFiniteNonNegative(getSkinnable().getLineHeight());
        double lineSpacing = sanitizeFiniteNonNegative(getSkinnable().getLineSpacing());
        double lastPercentSource = sanitizeNonNegative(getSkinnable().getLastLineFillPercent());
        double lastPercent = clamp(lastPercentSource, 0.0, FULL_PERCENT);
        int lineCount = Math.max(1, getSkinnable().getLineCount());
        double radius = lineHeight * HALF;

        if (lineHeight <= 0.0) {
            return;
        }

        for (int i = 0; i < lineCount; i++) {
            double y = i * (lineHeight + lineSpacing);
            if (y + lineHeight > contentHeight) {
                break;
            }

            double width = contentWidth;
            if (i == lineCount - 1 && lineCount > 1) {
                width = contentWidth * lastPercent / FULL_PERCENT;
            }
            blocks.add(new Block(0.0, y, width, lineHeight, radius * 2.0, radius * 2.0));
        }
    }

    private void syncLayer(Group layer, List<Block> blocks, double offsetX,
                           double offsetY, Paint fill, String blockStyleClass) {
        while (layer.getChildren().size() < blocks.size()) {
            Rectangle rectangle = new Rectangle();
            rectangle.getStyleClass().add(blockStyleClass);
            rectangle.setManaged(false);
            rectangle.setMouseTransparent(true);
            layer.getChildren().add(rectangle);
        }
        while (layer.getChildren().size() > blocks.size()) {
            layer.getChildren().remove(layer.getChildren().size() - 1);
        }

        for (int i = 0; i < blocks.size(); i++) {
            Rectangle rectangle = (Rectangle) layer.getChildren().get(i);
            Block block = blocks.get(i);
            rectangle.setX(offsetX + block.x);
            rectangle.setY(offsetY + block.y);
            rectangle.setWidth(block.width);
            rectangle.setHeight(block.height);
            rectangle.setArcWidth(block.arcWidth);
            rectangle.setArcHeight(block.arcHeight);
            rectangle.setFill(fill);
        }
    }

    private void layoutShimmer(double contentX, double contentY, double contentWidth, double contentHeight) {
        double bandWidth = sanitizeFiniteNonNegative(getSkinnable().getShimmerWidth());
        positionShimmerViewport(contentX, contentY);
        if (bandWidth <= 0.0) {
            shimmerBand.setWidth(0.0);
            shimmerBand.setHeight(contentHeight);
            cachedBandWidth = 0.0;
            return;
        }

        shimmerBand.setX(0.0);
        shimmerBand.setY(0.0);
        shimmerBand.setWidth(bandWidth);
        shimmerBand.setHeight(contentHeight);
        cachedBandWidth = bandWidth;
    }

    private void positionShimmerViewport(double x, double y) {
        shimmerViewport.setLayoutX(x);
        shimmerViewport.setLayoutY(y);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return leftInset + rightInset;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return topInset + bottomInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        double inner;
        switch (variantOrDefault()) {
            case CIRCULAR:
                inner = DEFAULT_CIRCULAR_SIZE;
                break;
            case ROUNDED_RECTANGLE:
            case TEXT:
            default:
                inner = DEFAULT_PREF_WIDTH;
                break;
        }

        return leftInset + inner + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        double inner;
        switch (variantOrDefault()) {
            case TEXT:
                int lineCount = Math.max(1, getSkinnable().getLineCount());
                double lineHeight = sanitizeFiniteNonNegative(getSkinnable().getLineHeight());
                double lineSpacing = sanitizeFiniteNonNegative(getSkinnable().getLineSpacing());
                inner = lineHeight <= 0.0 ? 0.0 : lineCount * lineHeight + Math.max(0, lineCount - 1) * lineSpacing;
                break;
            case CIRCULAR:
                inner = DEFAULT_CIRCULAR_SIZE;
                break;
            case ROUNDED_RECTANGLE:
            default:
                inner = DEFAULT_PREF_HEIGHT;
                break;
        }

        return topInset + inner + bottomInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    @Override
    public void dispose() {
        stopAndClearTimeline();
        shimmerViewport.setClip(null);
        super.dispose();
    }

    private static double sanitizeNonNegative(double value) {
        if (Double.isNaN(value) || value < 0.0) {
            return 0.0;
        }
        return value;
    }

    private static double sanitizeFiniteNonNegative(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            return 0.0;
        }
        return value;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Block {

        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final double arcWidth;
        private final double arcHeight;

        private Block(double x, double y, double width, double height, double arcWidth, double arcHeight) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.arcWidth = arcWidth;
            this.arcHeight = arcHeight;
        }
    }
}
