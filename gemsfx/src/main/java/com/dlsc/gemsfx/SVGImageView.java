package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SVGImageViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.URLConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A control which can display SVG images.
 * <p>
 * SVGImageView can display svg icons in high definition, and they won't become blurry even when zoomed in.
 * <p/>
 * Note for SvgImageView:
 * Currently, due to the limitation that weisj can only render BufferedImage from SVG,
 * SvgImageView does not support usage in native packaging scenarios.
 */
public class SVGImageView extends Control {

    private static final String DEFAULT_STYLE_CLASS = "svg-image-view";
    private static final double DEFAULT_FIT_WIDTH = 0;
    private static final double DEFAULT_FIT_HEIGHT = 0;
    private static final boolean DEFAULT_PRESERVE_RATIO = true;
    private static final boolean DEFAULT_SMOOTH = true;
    private static final boolean DEFAULT_BACKGROUND_LOADING = false;

    /**
     * Constructs a new SVGImageView.
     */
    public SVGImageView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setFocusTraversable(false);
    }

    /**
     * Constructs a new SVGImageView with the given SVG url.
     *
     * @param url the url of the SVG image to be rendered
     */
    public SVGImageView(String url) {
        this();
        setSvgUrl(url);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SVGImageViewSkin(this);
    }

    private final DoubleProperty fitWidth = new StyleableDoubleProperty(DEFAULT_FIT_WIDTH) {

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return SVGImageView.StyleableProperties.FIT_WIDTH;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "fitWidth";
        }
    };

    /**
     * Gets the value of {@link #fitWidthProperty()}.
     *
     * @return The fit width value.
     */
    public final double getFitWidth() {
        return fitWidth.get();
    }

    /**
     * Defines the width of the box that the source svg image should fit into. If the
     * value is <= 0, the svg image's intrinsic width will be used.
     * <p>
     * When {@link #preserveRatioProperty()} is set to true, then the actual displayed
     * width of the image is constrained not only by this property, but
     * also by {@link #fitHeightProperty()}, and it may not be the same as fitWidth.
     * <p/>
     * The default value is 0.
     */
    public final DoubleProperty fitWidthProperty() {
        return fitWidth;
    }

    /**
     * Sets the value of the {@link #fitWidthProperty()}.
     *
     * @param width the "fit width" value.
     */
    public final void setFitWidth(double width) {
        this.fitWidth.set(width);
    }

    private final DoubleProperty fitHeight = new StyleableDoubleProperty(DEFAULT_FIT_HEIGHT) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return SVGImageView.StyleableProperties.FIT_HEIGHT;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "fitHeight";
        }
    };

    /**
     * Gets the value of {@link #fitHeightProperty()}.
     *
     * @return The fit height value
     */
    public final double getFitHeight() {
        return fitHeight.get();
    }

    /**
     * Defines the height of the box that the source svg image should fit into. If the
     * value is <= 0, the svg image's intrinsic height will be used.
     * <p>
     * When {@link #preserveRatioProperty()} is set to true, then the actual displayed
     * height of the image is constrained not only by this property, but
     * also by {@link #fitWidthProperty()}, and it may not be the same as fitHeight.
     * <p/>
     * The default value is 0.
     */
    public final DoubleProperty fitHeightProperty() {
        return fitHeight;
    }

    /**
     * Sets the value of the {@link #fitHeightProperty()} property.
     *
     * @param fitHeight the "fit height" value
     */
    public final void setFitHeight(double fitHeight) {
        this.fitHeight.set(fitHeight);
    }

    private final BooleanProperty preserveRatio = new StyleableBooleanProperty(DEFAULT_PRESERVE_RATIO) {
        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return SVGImageView.StyleableProperties.PRESERVE_RATIO;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "preserveRatio";
        }
    };

    /**
     * Gets the value of the {@link #preserveRatioProperty()} property.
     *
     * @return The preserve ratio value.
     */
    public final boolean isPreserveRatio() {
        return preserveRatio.get();
    }

    /**
     * A property that determines whether the image should maintain its aspect ratio or not.
     * When set to true, the image will preserve its aspect ratio.
     * When set to false, the image may be stretched or compressed to fit the specified dimensions,
     * without preserving its aspect ratio.
     * <p>
     * The default value is false
     */
    public final BooleanProperty preserveRatioProperty() {
        return preserveRatio;
    }

    /**
     * Sets the value of the preserveRatio property.
     *
     * @param preserveRatio the preserve ratio value.
     */
    public final void setPreserveRatio(boolean preserveRatio) {
        this.preserveRatio.set(preserveRatio);
    }

    private final BooleanProperty smooth = new StyleableBooleanProperty(DEFAULT_SMOOTH) {

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return SVGImageView.StyleableProperties.SMOOTH;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "smooth";
        }
    };

    /**
     * Gets the value of the smooth property.
     *
     * @return The smooth value.
     */
    public final boolean isSmooth() {
        return smooth.get();
    }

    /**
     * A property that determines whether the SVG image should be rendered using a
     * smoothing algorithm. If true, the image will be rendered with smoothing
     * applied, which can improve the visual quality but may reduce performance.
     * <p>
     * defaultValue true
     */
    public final BooleanProperty smoothProperty() {
        return smooth;
    }

    /**
     * Sets the value of the smooth property.
     *
     * @param smooth The smooth value.
     */
    public final void setSmooth(boolean smooth) {
        this.smooth.set(smooth);
    }

    private final StringProperty svgUrl = new StyleableStringProperty() {
        @Override
        public CssMetaData<SVGImageView, String> getCssMetaData() {
            return SVGImageView.StyleableProperties.SVG_URL;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "svgUrl";
        }
    };

    /**
     * Gets the value of the svgUrl property.
     *
     * @return The svg url value.
     */
    public final String getSvgUrl() {
        return svgUrl.get();
    }

    /**
     * A property that holds the URL of the SVG image to be rendered.
     * Changing the URL will result in loading and rendering the new SVG image.
     * <p>
     * defaultValue null
     */
    public final StringProperty svgUrlProperty() {
        return svgUrl;
    }

    /**
     * Sets the value of the svgUrl property.
     *
     * @param svgUrl The svg url value.
     */
    public final void setSvgUrl(String svgUrl) {
        this.svgUrl.set(svgUrl);
    }

    private final BooleanProperty backgroundLoading = new StyleableBooleanProperty(DEFAULT_BACKGROUND_LOADING) {

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return SVGImageView.StyleableProperties.BACKGROUND_LOADING;
        }

        @Override
        public Object getBean() {
            return SVGImageView.this;
        }

        @Override
        public String getName() {
            return "backgroundLoading";
        }
    };

    /**
     * Gets the value of the backgroundLoading property.
     *
     * @return The background loading value.
     */
    public final boolean isBackgroundLoading() {
        return backgroundLoading.get();
    }

    /**
     * A property that indicates whether the SVG image should be loaded in the background.
     * When set to true, the image is loaded in a background thread, allowing for
     * asynchronous loading of images.
     * <p>
     * defaultValue false
     */
    public final BooleanProperty backgroundLoadingProperty() {
        return backgroundLoading;
    }

    /**
     * Sets the value of the backgroundLoading property.
     *
     * @param backgroundLoading The background loading value.
     */
    public final void setBackgroundLoading(boolean backgroundLoading) {
        this.backgroundLoading.set(backgroundLoading);
    }

    private static class StyleableProperties {

        public static final CssMetaData<SVGImageView, Number> FIT_WIDTH = new CssMetaData<>("-fx-fit-width", SizeConverter.getInstance(), DEFAULT_FIT_WIDTH) {
            @Override
            public boolean isSettable(SVGImageView styleable) {
                return !styleable.fitWidth.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(SVGImageView styleable) {
                return (StyleableProperty<Number>) styleable.fitWidthProperty();
            }
        };

        public static final CssMetaData<SVGImageView, Number> FIT_HEIGHT = new CssMetaData<>("-fx-fit-height", SizeConverter.getInstance(), DEFAULT_FIT_HEIGHT) {
            @Override
            public boolean isSettable(SVGImageView styleable) {
                return !styleable.fitHeight.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Number> getStyleableProperty(SVGImageView styleable) {
                return (StyleableProperty<Number>) styleable.fitHeightProperty();
            }
        };

        private static final CssMetaData<SVGImageView, String> SVG_URL = new CssMetaData<>("-fx-svg-url", URLConverter.getInstance()) {

            @Override
            public boolean isSettable(SVGImageView styleable) {
                return !styleable.svgUrl.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<String> getStyleableProperty(SVGImageView styleable) {
                return (StyleableProperty<String>) styleable.svgUrlProperty();
            }
        };

        private static final CssMetaData<SVGImageView, Boolean> PRESERVE_RATIO = new CssMetaData<>("-fx-preserve-ratio", BooleanConverter.getInstance(), DEFAULT_PRESERVE_RATIO) {
            public boolean isSettable(SVGImageView styleable) {
                return !styleable.preserveRatio.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Boolean> getStyleableProperty(SVGImageView styleable) {
                return (StyleableProperty<Boolean>) styleable.preserveRatio;
            }
        };

        private static final CssMetaData<SVGImageView, Boolean> SMOOTH = new CssMetaData<>("-fx-smooth", BooleanConverter.getInstance(), DEFAULT_SMOOTH) {
            public boolean isSettable(SVGImageView styleable) {
                return !styleable.smooth.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Boolean> getStyleableProperty(SVGImageView styleable) {
                return (StyleableProperty<Boolean>) styleable.smooth;
            }
        };

        public static final CssMetaData<? extends Styleable, Boolean> BACKGROUND_LOADING = new CssMetaData<>("-fx-background-loading", BooleanConverter.getInstance(), DEFAULT_BACKGROUND_LOADING) {
            @Override
            public boolean isSettable(Styleable styleable) {
                return !((SVGImageView) styleable).backgroundLoading.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Boolean> getStyleableProperty(Styleable styleable) {
                return (StyleableProperty<Boolean>) ((SVGImageView) styleable).backgroundLoading;
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, FIT_WIDTH, FIT_HEIGHT, SVG_URL, PRESERVE_RATIO, SMOOTH, BACKGROUND_LOADING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the CSS metadata associated with this class.
     *
     * @return A list of {@code CssMetaData} objects containing the CSS metadata.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return SVGImageView.StyleableProperties.STYLEABLES;
    }
}
