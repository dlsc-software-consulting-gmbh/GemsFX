package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.MaskedViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A view that takes a content node and applies advanced clipping so that its
 * left and right side will "fade out". This is especially useful in combination
 * with controls that want to show scrolling controls on both sides for moving
 * things to the left and right as those controls will then be nicely visible.
 * The {@link StripView} control is using the masked view inside its skin.
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-fading-size}</td><td>{@code double}</td><td>Size of the side fading areas in pixels</td></tr>
 *   </tbody>
 * </table>
 */
public class MaskedView extends Control {

    private static final int DEFAULT_FADING_SIZE = 120;

    /**
     * Constructs a new masked view. Content can be specified later by calling
     * {@link #setContent(Node)}.
     */
    public MaskedView() {
        getStyleClass().add("masked-view");
        setFocusTraversable(false);
    }

    /**
     * Constructs a new masked view for the given content.
     *
     * @param content the content to show inside the view
     */
    public MaskedView(Node content) {
        this();
        setContent(content);
    }

    /**
     * Creates the default skin for this control.
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaskedViewSkin(this);
    }

    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    /**
     * The content that will be shown inside the masked view.
     *
     * @return the content node
     */
    public final SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    private DoubleProperty fadingSize;

    public final double getFadingSize() {
        return fadingSize == null ? DEFAULT_FADING_SIZE : fadingSize.get();
    }

    /**
     * The width of the clips on the left and right hand side of the view. This property
     * defines how big the fade in / out areas will be.
     * <p>
     * Can be set via CSS using the {@code -fx-fading-size} property.
     * Valid values are: positive numbers.
     * The default value is {@code 120}.
     * </p>
     *
     * @return the size of the side fading areas / clip areas
     */
    public final DoubleProperty fadingSizeProperty() {
        if (fadingSize == null) {
            fadingSize = new StyleableDoubleProperty(DEFAULT_FADING_SIZE) {
                /**
                 * {@inheritDoc}
                 *
                 * @return the owning bean
                 */
                @Override
                public Object getBean() {
                    return MaskedView.this;
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the property name
                 */
                @Override
                public String getName() {
                    return "fadingSize";
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the CSS metadata for this property
                 */
                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.FADING_SIZE;
                }
            };
        }
        return fadingSize;
    }

    public final void setFadingSize(double fadingSize) {
        fadingSizeProperty().set(fadingSize);
    }

    private static class StyleableProperties {

        private static final CssMetaData<MaskedView, Number> FADING_SIZE = new CssMetaData<>(
                "-fx-fading-size", SizeConverter.getInstance(), DEFAULT_FADING_SIZE) {

            /**
             * {@inheritDoc}
             *
             * @param control the control to inspect
             * @return the styleable property
             */
            @Override
            public StyleableProperty<Number> getStyleableProperty(MaskedView control) {
                return (StyleableProperty<Number>) control.fadingSizeProperty();
            }

            /**
             * {@inheritDoc}
             *
             * @param control the control to inspect
             * @return true if the property can be styled
             */
            @Override
            public boolean isSettable(MaskedView control) {
                return control.fadingSize == null || !control.fadingSize.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(FADING_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the control CSS metadata
     */
    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the class CSS metadata
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return MaskedView.StyleableProperties.STYLEABLES;
    }

}
