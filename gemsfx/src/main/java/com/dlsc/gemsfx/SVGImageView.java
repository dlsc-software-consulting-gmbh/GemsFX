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

/*
 * Note for SvgImageView:
 * Currently, due to the limitation that weisj can only render BufferedImage from SVG,
 * SvgImageView does not support usage in native packaging scenarios.
 */
public class SVGImageView extends Control {

    private static final String DEFAULT_STYLE_CLASS = "svg-image-view";
    private static final double DEFAULT_FIT_WIDTH = 0;
    private static final double DEFAULT_FIT_HEIGHT = 0;
    private static final boolean DEFAULT_PRESERVE_RATIO = false;
    private static final boolean DEFAULT_SMOOTH = true;
    private static final boolean DEFAULT_BACKGROUND_LOADING = false;

    public SVGImageView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

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

    public double getFitWidth() {
        return fitWidth.get();
    }

    public DoubleProperty fitWidthProperty() {
        return fitWidth;
    }

    public void setFitWidth(double fitWidth) {
        this.fitWidth.set(fitWidth);
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

    public double getFitHeight() {
        return fitHeight.get();
    }

    public DoubleProperty fitHeightProperty() {
        return fitHeight;
    }

    public void setFitHeight(double fitHeight) {
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

    public boolean isPreserveRatio() {
        return preserveRatio.get();
    }

    public BooleanProperty preserveRatioProperty() {
        return preserveRatio;
    }

    public void setPreserveRatio(boolean preserveRatio) {
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

    public boolean isSmooth() {
        return smooth.get();
    }

    public BooleanProperty smoothProperty() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
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

    public String getSvgUrl() {
        return svgUrl.get();
    }

    public StringProperty svgUrlProperty() {
        return svgUrl;
    }

    public void setSvgUrl(String svgUrl) {
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

    public boolean isBackgroundLoading() {
        return backgroundLoading.get();
    }

    public BooleanProperty backgroundLoadingProperty() {
        return backgroundLoading;
    }

    public void setBackgroundLoading(boolean backgroundLoading) {
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

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return SVGImageView.StyleableProperties.STYLEABLES;
    }

}
