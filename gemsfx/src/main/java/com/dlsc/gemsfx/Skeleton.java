package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SkeletonSkin;
import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.DurationConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single-unit skeleton placeholder with a horizontal shimmer animation,
 * intended for content-is-loading UI. The skeleton draws one of three
 * variants selected via {@link #variantProperty()} and overlays a shimmer band
 * that scrolls left-to-right to suggest activity.
 *
 * <p>The skeleton is stretchable: its skin reports {@code maxWidth} and
 * {@code maxHeight} as {@link Double#MAX_VALUE}, so the placeholder can grow
 * to mirror real content as its container resizes.
 *
 * <p>Variants:
 * <ul>
 *   <li>{@link Variant#ROUNDED_RECTANGLE}: a rectangle whose corners use
 *       {@link #cornerRadiusProperty() cornerRadius}</li>
 *   <li>{@link Variant#CIRCULAR}: a circle inscribed in
 *       {@code min(width, height)}</li>
 *   <li>{@link Variant#TEXT}: stacked lines that simulate a paragraph</li>
 * </ul>
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-variant}</td><td>{@code Variant}</td><td>Geometric form of the placeholder</td></tr>
 *     <tr><td>{@code -fx-corner-radius}</td><td>{@code Size}</td><td>Corner radius for rounded rectangles</td></tr>
 *     <tr><td>{@code -fx-base-color}</td><td>{@code Paint}</td><td>Base block fill</td></tr>
 *     <tr><td>{@code -fx-shimmer-fill}</td><td>{@code Paint}</td><td>Moving shimmer band fill</td></tr>
 *     <tr><td>{@code -fx-cycle-duration}</td><td>{@code Duration}</td><td>Duration of one shimmer sweep</td></tr>
 *     <tr><td>{@code -fx-shimmer-width}</td><td>{@code Size}</td><td>Width of the shimmer band</td></tr>
 *     <tr><td>{@code -fx-line-count}</td><td>{@code Size}</td><td>Number of text lines</td></tr>
 *     <tr><td>{@code -fx-line-height}</td><td>{@code Size}</td><td>Height of each text line</td></tr>
 *     <tr><td>{@code -fx-line-spacing}</td><td>{@code Size}</td><td>Spacing between text lines</td></tr>
 *     <tr><td>{@code -fx-last-line-fill-percent}</td><td>{@code Size}</td><td>Width percentage of the final text line</td></tr>
 *   </tbody>
 * </table>
 *
 * @see SkeletonPane
 */
public class Skeleton extends Control {

    private static final String DEFAULT_STYLE_CLASS = "skeleton";

    public static final Variant DEFAULT_VARIANT = Variant.ROUNDED_RECTANGLE;
    private static final double DEFAULT_CORNER_RADIUS = 4.0;
    private static final Duration DEFAULT_CYCLE_DURATION = Duration.millis(1500.0);
    private static final double DEFAULT_SHIMMER_WIDTH = 56.0;
    private static final int DEFAULT_LINE_COUNT = 1;
    private static final double DEFAULT_LINE_HEIGHT = 14.0;
    private static final double DEFAULT_LINE_SPACING = 8.0;
    private static final double DEFAULT_LAST_LINE_FILL_PERCENT = 70.0;
    private static final Paint DEFAULT_BASE_COLOR = Color.web("#e0e0e0");
    private static final Paint DEFAULT_SHIMMER_FILL = createShimmerGradient(Color.web("#ffffff", 0.6));

    /**
     * Creates a standard shimmer gradient from a single highlight color.
     *
     * @param highlightColor the center highlight color
     * @return a linear gradient suitable for {@link #shimmerFillProperty()}
     * @throws NullPointerException if {@code highlightColor} is {@code null}
     */
    public static LinearGradient createShimmerGradient(Color highlightColor) {
        if (highlightColor == null) {
            throw new NullPointerException("highlightColor cannot be null");
        }

        Color edge = new Color(highlightColor.getRed(), highlightColor.getGreen(), highlightColor.getBlue(), 0.0);
        return new LinearGradient(0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, edge),
                new Stop(0.5, highlightColor),
                new Stop(1.0, edge));
    }

    /**
     * Creates a skeleton with the {@linkplain #DEFAULT_VARIANT default variant}.
     */
    public Skeleton() {
        this(null);
    }

    /**
     * Creates a skeleton with the given variant.
     *
     * @param variant the initial variant; {@code null} keeps the default value
     *                and leaves the property settable from CSS
     */
    public Skeleton(@NamedArg("variant") Variant variant) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        AccessibilityUtil.setRole(this, AccessibleRole.NODE, ResourceBundleManager.getString(ResourceBundleManager.BundleType.SKELETON, "accessible.role-description", "loading placeholder"));
        if (variant != null) {
            setVariant(variant);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkeletonSkin(this);
    }

    private final ObjectProperty<Variant> variant = new StyleableObjectProperty<>(DEFAULT_VARIANT) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "variant";
        }

        @Override
        public CssMetaData<Skeleton, Variant> getCssMetaData() {
            return StyleableProperties.VARIANT;
        }
    };

    /**
     * Geometric form of the placeholder. A {@code null} value resolves to the
     * default {@link #DEFAULT_VARIANT} at the use site.
     *
     * @return the variant property
     */
    public final ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    /**
     * Gets the geometric variant.
     *
     * @return the current variant
     */
    public final Variant getVariant() {
        return variant.get();
    }

    /**
     * Sets the geometric variant.
     *
     * @param value the variant, or {@code null} to fall back to the default
     */
    public final void setVariant(Variant value) {
        variant.set(value);
    }

    private final DoubleProperty cornerRadius = new StyleableDoubleProperty(DEFAULT_CORNER_RADIUS) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "cornerRadius";
        }

        @Override
        public CssMetaData<Skeleton, Number> getCssMetaData() {
            return StyleableProperties.CORNER_RADIUS;
        }
    };

    /**
     * Corner radius applied when {@link #variantProperty()} is
     * {@link Variant#ROUNDED_RECTANGLE}. Negative values and {@code NaN} are
     * treated as {@code 0} at render time.
     *
     * @return the corner-radius property
     */
    public final DoubleProperty cornerRadiusProperty() {
        return cornerRadius;
    }

    /**
     * Gets the corner radius.
     *
     * @return the corner radius in pixels
     */
    public final double getCornerRadius() {
        return cornerRadius.get();
    }

    /**
     * Sets the corner radius.
     *
     * @param value the corner radius in pixels
     */
    public final void setCornerRadius(double value) {
        cornerRadius.set(value);
    }

    private final ObjectProperty<Paint> baseColor = new StyleableObjectProperty<>(DEFAULT_BASE_COLOR) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "baseColor";
        }

        @Override
        public CssMetaData<Skeleton, Paint> getCssMetaData() {
            return StyleableProperties.BASE_COLOR;
        }
    };

    /**
     * Paint used for the base block under the shimmer band. Setting
     * {@code null} renders no base fill.
     *
     * @return the base-color property
     */
    public final ObjectProperty<Paint> baseColorProperty() {
        return baseColor;
    }

    /**
     * Gets the base fill paint.
     *
     * @return the base fill paint, or {@code null} for no base fill
     */
    public final Paint getBaseColor() {
        return baseColor.get();
    }

    /**
     * Sets the base fill paint.
     *
     * @param value the base fill paint, or {@code null} for no base fill
     */
    public final void setBaseColor(Paint value) {
        baseColor.set(value);
    }

    private final ObjectProperty<Paint> shimmerFill = new StyleableObjectProperty<>(DEFAULT_SHIMMER_FILL) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "shimmerFill";
        }

        @Override
        public CssMetaData<Skeleton, Paint> getCssMetaData() {
            return StyleableProperties.SHIMMER_FILL;
        }
    };

    /**
     * Fill paint used for the moving shimmer band. Setting {@code null}
     * renders no shimmer fill.
     *
     * @return the shimmer-fill property
     */
    public final ObjectProperty<Paint> shimmerFillProperty() {
        return shimmerFill;
    }

    /**
     * Gets the shimmer band fill paint.
     *
     * @return the shimmer band fill paint, or {@code null} for no shimmer fill
     */
    public final Paint getShimmerFill() {
        return shimmerFill.get();
    }

    /**
     * Sets the shimmer band fill paint.
     *
     * @param value the shimmer band fill paint, or {@code null} for no shimmer fill
     */
    public final void setShimmerFill(Paint value) {
        shimmerFill.set(value);
    }

    private final ObjectProperty<Duration> cycleDuration =
            new StyleableObjectProperty<>(DEFAULT_CYCLE_DURATION) {
                @Override
                public Object getBean() {
                    return Skeleton.this;
                }

                @Override
                public String getName() {
                    return "cycleDuration";
                }

                @Override
                public CssMetaData<Skeleton, Duration> getCssMetaData() {
                    return StyleableProperties.CYCLE_DURATION;
                }
            };

    /**
     * Duration of one full left-to-right shimmer sweep. A value of {@code null},
     * an unknown or indefinite duration, or any duration less than or equal to
     * {@link Duration#ZERO} suppresses the animation.
     *
     * @return the cycle-duration property
     */
    public final ObjectProperty<Duration> cycleDurationProperty() {
        return cycleDuration;
    }

    /**
     * Gets the shimmer cycle duration.
     *
     * @return the cycle duration, or {@code null} to disable animation
     */
    public final Duration getCycleDuration() {
        return cycleDuration.get();
    }

    /**
     * Sets the shimmer cycle duration.
     *
     * @param value the cycle duration, or {@code null} to disable animation
     */
    public final void setCycleDuration(Duration value) {
        cycleDuration.set(value);
    }

    private final DoubleProperty shimmerWidth =
            new StyleableDoubleProperty(DEFAULT_SHIMMER_WIDTH) {
                @Override
                public Object getBean() {
                    return Skeleton.this;
                }

                @Override
                public String getName() {
                    return "shimmerWidth";
                }

                @Override
                public CssMetaData<Skeleton, Number> getCssMetaData() {
                    return StyleableProperties.SHIMMER_WIDTH;
                }
            };

    /**
     * Width of the shimmer band in pixels. Values that are negative,
     * {@code NaN}, or infinite are treated as {@code 0} at render time.
     *
     * @return the shimmer-width property
     */
    public final DoubleProperty shimmerWidthProperty() {
        return shimmerWidth;
    }

    /**
     * Gets the shimmer band width.
     *
     * @return the shimmer band width in pixels
     */
    public final double getShimmerWidth() {
        return shimmerWidth.get();
    }

    /**
     * Sets the shimmer band width.
     *
     * @param value the shimmer band width in pixels
     */
    public final void setShimmerWidth(double value) {
        shimmerWidth.set(value);
    }

    private final IntegerProperty lineCount = new StyleableIntegerProperty(DEFAULT_LINE_COUNT) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "lineCount";
        }

        @Override
        public CssMetaData<Skeleton, Number> getCssMetaData() {
            return StyleableProperties.LINE_COUNT;
        }
    };

    /**
     * Number of stacked lines drawn for {@link Variant#TEXT}. Values less than
     * {@code 1} are treated as {@code 1}.
     *
     * @return the line-count property
     */
    public final IntegerProperty lineCountProperty() {
        return lineCount;
    }

    /**
     * Gets the number of text lines.
     *
     * @return the configured line count
     */
    public final int getLineCount() {
        return lineCount.get();
    }

    /**
     * Sets the number of text lines.
     *
     * @param value the configured line count
     */
    public final void setLineCount(int value) {
        lineCount.set(value);
    }

    private final DoubleProperty lineHeight = new StyleableDoubleProperty(DEFAULT_LINE_HEIGHT) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "lineHeight";
        }

        @Override
        public CssMetaData<Skeleton, Number> getCssMetaData() {
            return StyleableProperties.LINE_HEIGHT;
        }
    };

    /**
     * Per-line height for {@link Variant#TEXT}, in pixels. Values that are
     * negative, {@code NaN}, or infinite are treated as {@code 0} at render time.
     *
     * @return the line-height property
     */
    public final DoubleProperty lineHeightProperty() {
        return lineHeight;
    }

    /**
     * Gets the text line height.
     *
     * @return the line height in pixels
     */
    public final double getLineHeight() {
        return lineHeight.get();
    }

    /**
     * Sets the text line height.
     *
     * @param value the line height in pixels
     */
    public final void setLineHeight(double value) {
        lineHeight.set(value);
    }

    private final DoubleProperty lineSpacing = new StyleableDoubleProperty(DEFAULT_LINE_SPACING) {
        @Override
        public Object getBean() {
            return Skeleton.this;
        }

        @Override
        public String getName() {
            return "lineSpacing";
        }

        @Override
        public CssMetaData<Skeleton, Number> getCssMetaData() {
            return StyleableProperties.LINE_SPACING;
        }
    };

    /**
     * Vertical gap between adjacent lines for {@link Variant#TEXT}, in pixels.
     * Values that are negative, {@code NaN}, or infinite are treated as
     * {@code 0} at render time.
     *
     * @return the line-spacing property
     */
    public final DoubleProperty lineSpacingProperty() {
        return lineSpacing;
    }

    /**
     * Gets the spacing between text lines.
     *
     * @return the line spacing in pixels
     */
    public final double getLineSpacing() {
        return lineSpacing.get();
    }

    /**
     * Sets the spacing between text lines.
     *
     * @param value the line spacing in pixels
     */
    public final void setLineSpacing(double value) {
        lineSpacing.set(value);
    }

    private final DoubleProperty lastLineFillPercent =
            new StyleableDoubleProperty(DEFAULT_LAST_LINE_FILL_PERCENT) {
                @Override
                public Object getBean() {
                    return Skeleton.this;
                }

                @Override
                public String getName() {
                    return "lastLineFillPercent";
                }

                @Override
                public CssMetaData<Skeleton, Number> getCssMetaData() {
                    return StyleableProperties.LAST_LINE_FILL_PERCENT;
                }
            };

    /**
     * Width of the last line for {@link Variant#TEXT}, expressed as a percent
     * of the placeholder width. Values are clamped to {@code [0, 100]} at render time.
     *
     * @return the last-line-fill-percent property
     */
    public final DoubleProperty lastLineFillPercentProperty() {
        return lastLineFillPercent;
    }

    /**
     * Gets the fill percent used for the final text line.
     *
     * @return the final line fill percent
     */
    public final double getLastLineFillPercent() {
        return lastLineFillPercent.get();
    }

    /**
     * Sets the fill percent used for the final text line.
     *
     * @param value the final line fill percent
     */
    public final void setLastLineFillPercent(double value) {
        lastLineFillPercent.set(value);
    }

    private static final class StyleableProperties {

        private static final CssMetaData<Skeleton, Variant> VARIANT =
                new CssMetaData<>("-fx-variant", new EnumConverter<>(Variant.class), DEFAULT_VARIANT) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.variant.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Variant> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Variant>) n.variantProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> CORNER_RADIUS =
                new CssMetaData<>("-fx-corner-radius", SizeConverter.getInstance(), DEFAULT_CORNER_RADIUS) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.cornerRadius.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.cornerRadiusProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Paint> BASE_COLOR =
                new CssMetaData<>("-fx-base-color", PaintConverter.getInstance(), DEFAULT_BASE_COLOR) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.baseColor.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Paint> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Paint>) n.baseColorProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Paint> SHIMMER_FILL =
                new CssMetaData<>("-fx-shimmer-fill", PaintConverter.getInstance(), DEFAULT_SHIMMER_FILL) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.shimmerFill.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Paint> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Paint>) n.shimmerFillProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Duration> CYCLE_DURATION =
                new CssMetaData<>("-fx-cycle-duration", DurationConverter.getInstance(), DEFAULT_CYCLE_DURATION) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.cycleDuration.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Duration> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Duration>) n.cycleDurationProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> SHIMMER_WIDTH =
                new CssMetaData<>("-fx-shimmer-width", SizeConverter.getInstance(), DEFAULT_SHIMMER_WIDTH) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.shimmerWidth.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.shimmerWidthProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> LINE_COUNT =
                new CssMetaData<>("-fx-line-count", SizeConverter.getInstance(), DEFAULT_LINE_COUNT) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.lineCount.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.lineCountProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> LINE_HEIGHT =
                new CssMetaData<>("-fx-line-height", SizeConverter.getInstance(), DEFAULT_LINE_HEIGHT) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.lineHeight.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.lineHeightProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> LINE_SPACING =
                new CssMetaData<>("-fx-line-spacing", SizeConverter.getInstance(), DEFAULT_LINE_SPACING) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.lineSpacing.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.lineSpacingProperty();
                    }
                };

        private static final CssMetaData<Skeleton, Number> LAST_LINE_FILL_PERCENT =
                new CssMetaData<>("-fx-last-line-fill-percent", SizeConverter.getInstance(), DEFAULT_LAST_LINE_FILL_PERCENT) {
                    @Override
                    public boolean isSettable(Skeleton n) {
                        return !n.lastLineFillPercent.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(Skeleton n) {
                        return (StyleableProperty<Number>) n.lastLineFillPercentProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables,
                    VARIANT,
                    CORNER_RADIUS,
                    BASE_COLOR,
                    SHIMMER_FILL,
                    CYCLE_DURATION,
                    SHIMMER_WIDTH,
                    LINE_COUNT,
                    LINE_HEIGHT,
                    LINE_SPACING,
                    LAST_LINE_FILL_PERCENT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns the CSS metadata associated with this class.
     *
     * @return the CSS metadata
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * Returns the CSS metadata associated with this control instance.
     *
     * @return the CSS metadata
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }


    /**
     * Geometric form of a {@link Skeleton}.
     */
    public enum Variant {
        /**
         * A rounded rectangle whose corner radius is driven by
         * {@link Skeleton#cornerRadiusProperty() cornerRadius}.
         */
        ROUNDED_RECTANGLE,

        /**
         * A circle inscribed in {@code min(width, height)}.
         */
        CIRCULAR,

        /**
         * A vertical stack of {@link Skeleton#lineCountProperty() lineCount}
         * horizontal lines simulating a paragraph.
         */
        TEXT
    }
}
