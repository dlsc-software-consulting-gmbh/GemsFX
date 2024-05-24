package com.dlsc.gemsfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.shape.ArcType;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ArcProgressIndicator is a visual control used to indicate the progress of a task.
 * It represents progress in an arc form, with options to show determinate or indeterminate states.
 * <p>
 * In a determinate state, the arc fills up based on the progress value, which ranges from 0.0 to 1.0,
 * where 0.0 indicates no progress and 1.0 indicates completion.
 * <p>
 * In an indeterminate state, the arc shows a cyclic animation, indicating that progress is ongoing
 * but the exact status is unknown. This state is useful for tasks where the progress cannot be determined.
 * <p>
 * The control also supports displaying a text or graphic inside the arc to provide additional
 * information or visual feedback to the user.
 * <p>
 * Usage examples include file downloads, file transfers, or any long-running tasks where
 * visual feedback on progress is beneficial.
 *
 * <p>
 * <b>Pseudo class:</b> Beyond the <b>inherited</b>, <b>indeterminate</b>, and <b>determinate</b> pseudo-classes
 * from ProgressIndicator, ArcProgressIndicator introduces a <b>completed</b> pseudo-class.
 * This pseudo-class can be used in CSS to apply custom styles when the progress reaches 1.0 (100%).
 *
 * <p>
 * <b>Tips:</b> If you prefer not to instantiate the animation object during initialization,
 * pass a 0.0 as the initial progress. This setup indicates no progress but avoids entering
 * the indeterminate state, which would otherwise instantiate and start the animation.
 *
 * <p>Usage examples:
 * <pre>
 *     // Initializes with no progress and no animation.
 *     ArcProgressIndicator progressIndicator = new ArcProgressIndicator(0.0);
 * </pre>
 */
public class ArcProgressIndicator extends ProgressIndicator {

    private static final String DEFAULT_STYLE_CLASS = "arc-progress-indicator";
    private static final ArcType DEFAULT_PROGRESS_ARC_TYPE = ArcType.OPEN;
    private static final ArcType DEFAULT_TRACK_ARC_TYPE = ArcType.CHORD;

    private static final StringConverter<Double> DEFAULT_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Double progress) {
            // indeterminate
            if (progress == null || progress < 0.0) {
                return "";
            }
            // completed
            if (progress == 1.0) {
                return "Completed";
            }
            return String.format("%.0f%%", progress * 100);
        }

        @Override
        public Double fromString(String string) {
            return null;
        }
    };

    public ArcProgressIndicator() {
        this(INDETERMINATE_PROGRESS);
    }

    public ArcProgressIndicator(double progress) {
        super(progress);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(ArcProgressIndicator.class.getResource("arc-progress-indicator.css")).toExternalForm();
    }

    private final ObjectProperty<StringConverter<Double>> converter = new SimpleObjectProperty<>(this, "converter", DEFAULT_CONVERTER);

    public final StringConverter<Double> getConverter() {
        return converter.get();
    }

    /**
     * The converter is used to convert the progress value to a string that is displayed
     *
     * @return the converter property
     */
    public final ObjectProperty<StringConverter<Double>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<Double> converter) {
        converterProperty().set(converter);
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    /**
     * The graphic property is used to display a custom node within the progress indicator.
     * progress label's graphic property is bound to this property.
     *
     * @return the graphic property
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final Node getGraphic() {
        return graphic.get();
    }

    public final void setGraphic(Node graphic) {
        graphicProperty().set(graphic);
    }

    private ObjectProperty<ArcType> progressArcType;

    /**
     * The arc type property defines the type of the arc that is used to display the progress.
     *
     * @return the arc type property for the progress
     */
    public final ObjectProperty<ArcType> progressArcTypeProperty() {
        if (progressArcType == null) {
            progressArcType = new StyleableObjectProperty<>(DEFAULT_PROGRESS_ARC_TYPE) {
                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "progressArcType";
                }

                @Override
                public CssMetaData<? extends Styleable, ArcType> getCssMetaData() {
                    return StyleableProperties.PROGRESS_ARC_TYPE;
                }
            };
        }
        return progressArcType;
    }

    public final ArcType getProgressArcType() {
        return progressArcType == null ? DEFAULT_PROGRESS_ARC_TYPE : progressArcType.get();
    }

    public final void setProgressArcType(ArcType progressArcType) {
        progressArcTypeProperty().set(progressArcType);
    }

    private ObjectProperty<ArcType> trackArcType;

    /**
     * The arc type property defines the type of the arc that is used to display the track.
     *
     * @return the arc type property for the track
     */
    public final ObjectProperty<ArcType> trackArcTypeProperty() {
        if (trackArcType == null) {
            trackArcType = new StyleableObjectProperty<>(DEFAULT_TRACK_ARC_TYPE) {
                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "trackArcType";
                }

                @Override
                public CssMetaData<? extends Styleable, ArcType> getCssMetaData() {
                    return StyleableProperties.TRACK_ARC_TYPE;
                }
            };
        }
        return trackArcType;
    }

    public final ArcType getTrackArcType() {
        return trackArcType == null ? DEFAULT_TRACK_ARC_TYPE : trackArcType.get();
    }

    public final void setTrackArcType(ArcType trackArcType) {
        trackArcTypeProperty().set(trackArcType);
    }

    private static class StyleableProperties {

        private static final CssMetaData<ArcProgressIndicator, ArcType> PROGRESS_ARC_TYPE = new CssMetaData<>(
                "-fx-progress-arc-type", new EnumConverter<>(ArcType.class), DEFAULT_PROGRESS_ARC_TYPE) {

            @Override
            public StyleableProperty<ArcType> getStyleableProperty(ArcProgressIndicator control) {
                return (StyleableProperty<ArcType>) control.progressArcTypeProperty();
            }

            @Override
            public boolean isSettable(ArcProgressIndicator control) {
                return control.progressArcType == null || !control.progressArcType.isBound();
            }
        };

        private static final CssMetaData<ArcProgressIndicator, ArcType> TRACK_ARC_TYPE = new CssMetaData<>(
                "-fx-track-arc-type", new EnumConverter<>(ArcType.class), DEFAULT_TRACK_ARC_TYPE) {

            @Override
            public StyleableProperty<ArcType> getStyleableProperty(ArcProgressIndicator control) {
                return (StyleableProperty<ArcType>) control.trackArcTypeProperty();
            }

            @Override
            public boolean isSettable(ArcProgressIndicator control) {
                return control.trackArcType == null || !control.trackArcType.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ProgressIndicator.getClassCssMetaData());
            Collections.addAll(styleables, PROGRESS_ARC_TYPE, TRACK_ARC_TYPE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return ArcProgressIndicator.StyleableProperties.STYLEABLES;
    }

}
