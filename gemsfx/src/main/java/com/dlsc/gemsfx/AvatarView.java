package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.AvatarViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A custom control representing an avatar view.
 * <p>
 * The AvatarView can display either an image or the user's initials if no image is provided.
 * The appearance of the avatar can be customized with properties such as clip type (circular or rectangular),
 * round size for rectangular avatars, and the size of the avatar.
 * </p>
 *
 * <p>
 * The following CSS properties can be used to style the AvatarView:
 * <ul>
 *   <li>-fx-clip-type: Defines the clipping type of the avatar (circular or rectangular).</li>
 *   <li>-fx-round-size: Defines the corner roundness of rectangular avatars.</li>
 *   <li>-fx-avatar-size: Defines the size of the avatar.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The default style class for this control is "avatar-view".
 * </p>
 **/
public class AvatarView extends Control {

    private static final String DEFAULT_STYLE_CLASS = "avatar-view";
    private static final ClipType DEFAULT_CLIP_TYPE = ClipType.SQUARE;
    private static final double DEFAULT_ROUND_SIZE = 10;
    private static final double DEFAULT_SIZE = 50;

    public AvatarView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setFocusTraversable(false);
    }

    public AvatarView(String initials, Image image) {
        this();
        setInitials(initials);
        setImage(image);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AvatarViewSkin(this);
    }

    // user name initials

    private final StringProperty initials = new SimpleStringProperty(this, "initials");

    public final String getInitials() {
        return initials.get();
    }

    /**
     * The initials of the user that should be displayed in the avatar.
     * <p>
     * The initials are used as a fallback if no image is provided.
     * If an image is provided and has finished loading, the initials are not displayed.
     * </p>
     *
     * @return the initials property
     */
    public final StringProperty initialsProperty() {
        return initials;
    }

    public final void setInitials(String initials) {
        this.initials.set(initials);
    }

    // user image

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");

    /**
     * The image of the user that should be displayed in the avatar.
     * <p>
     * If an image is provided and has finished loading, it will be displayed in the avatar.
     * If no image is provided or the image fails to load, the initials will be displayed instead.
     * </p>
     *
     * @return the image property
     */
    public final ObjectProperty<Image> imageProperty() {
        return image;
    }

    public final Image getImage() {
        return imageProperty().get();
    }

    public final void setImage(Image image) {
        imageProperty().set(image);
    }

    // round size

    private final DoubleProperty roundSize = new StyleableDoubleProperty(DEFAULT_ROUND_SIZE) {
        @Override
        public Object getBean() {
            return AvatarView.this;
        }

        @Override
        public String getName() {
            return "roundSize";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.ROUND_SIZE;
        }
    };

    public final double getRoundSize() {
        return roundSize.get();
    }

    /**
     * The round size property of the avatar.
     * <p>
     * This property represents the degree of rounding applied to the corners of a rectangular avatar.
     * If the avatar is circular, this property is not used.
     * </p>
     *
     * @return the round size property
     */
    public final DoubleProperty roundSizeProperty() {
        return roundSize;
    }

    public final void setRoundSize(double roundSize) {
        this.roundSize.set(roundSize);
    }

    // size

    private final DoubleProperty size = new StyleableDoubleProperty(DEFAULT_SIZE) {
        @Override
        public Object getBean() {
            return AvatarView.this;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.AVATAR_SIZE;
        }
    };

    public final double getSize() {
        return size.get();
    }

    /**
     * The size property of the avatar.
     * <p>
     * This property represents the diameter (size) of the avatar.
     * </p>
     *
     * @return the size property
     */
    public final DoubleProperty sizeProperty() {
        return size;
    }

    public final void setSize(double size) {
        this.size.set(size);
    }

    // clip type

    private final StyleableObjectProperty<ClipType> clipType = new StyleableObjectProperty<>(DEFAULT_CLIP_TYPE) {
        @Override
        public CssMetaData<? extends Styleable, ClipType> getCssMetaData() {
            return StyleableProperties.CLIP_TYPE;
        }

        @Override
        public Object getBean() {
            return AvatarView.this;
        }

        @Override
        public String getName() {
            return "clipType";
        }

    };

    /**
     * The clip type property of the avatar.
     * <p>
     * This property determines the shape of the avatar, whether it is circular or rectangular.
     * </p>
     *
     * @return the clip type property
     */
    public final ObjectProperty<ClipType> clipTypeProperty() {
        return this.clipType;
    }

    public final ClipType getClipType() {
        return this.clipTypeProperty().get();
    }

    public final void setClipType(final ClipType type) {
        this.clipTypeProperty().set(type);
    }

    private static class StyleableProperties {
        private static final CssMetaData<AvatarView, ClipType> CLIP_TYPE = new CssMetaData<>(
                "-fx-clip-type", new EnumConverter<>(ClipType.class), DEFAULT_CLIP_TYPE) {
            @Override
            public boolean isSettable(AvatarView control) {
                return !control.clipType.isBound();
            }

            @Override
            public StyleableProperty<ClipType> getStyleableProperty(AvatarView control) {
                return (StyleableProperty<ClipType>) control.clipTypeProperty();
            }
        };

        private static final CssMetaData<AvatarView, Number> ROUND_SIZE =
                new CssMetaData<>("-fx-round-size", SizeConverter.getInstance(), DEFAULT_ROUND_SIZE) {
                    @Override
                    public boolean isSettable(AvatarView n) {
                        return !n.roundSize.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AvatarView n) {
                        return (StyleableProperty<Number>) n.roundSizeProperty();
                    }
                };

        private static final CssMetaData<AvatarView, Number> AVATAR_SIZE =
                new CssMetaData<>("-fx-avatar-size", SizeConverter.getInstance(), DEFAULT_SIZE) {
                    @Override
                    public boolean isSettable(AvatarView n) {
                        return !n.size.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AvatarView n) {
                        return (StyleableProperty<Number>) n.sizeProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, CLIP_TYPE, ROUND_SIZE, AVATAR_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * Enumeration representing the type of clipping applied to the avatar.
     */
    public enum ClipType {
        /**
         * The avatar is clipped to a circular shape.
         */
        CIRCLE,

        /**
         * The avatar is clipped to a square or rectangular shape.
         */
        SQUARE
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(AvatarView.class.getResource("avatar-view.css")).toExternalForm();
    }

}
