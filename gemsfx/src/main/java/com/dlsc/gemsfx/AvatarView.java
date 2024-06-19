/*
 * Magic number algorithm inspired by Dani Guardiola's blog: https://dio.la/article/colorful-avatars
 */
package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.AvatarViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.apache.commons.lang3.StringUtils;

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
    private static final AvatarShape DEFAULT_AVATAR_SHAPE = AvatarShape.SQUARE;
    private static final double DEFAULT_ARC_SIZE = 10;
    private static final double DEFAULT_SIZE = 50;

    /**
     * Constructs a new avatar view with no initials and no image. The
     * view will fall back to showing the default icon.
     */
    public AvatarView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setFocusTraversable(false);

        initials.subscribe(this::updateMagicNumber);
        magicNumber.subscribe(number -> {
            getStyleClass().setAll(DEFAULT_STYLE_CLASS);
            if (number.intValue() >= 0) {
                int index = number.intValue() % getNumberOfStyles();
                getStyleClass().add("style" + index);
            }
        });

        prefWidthProperty().bind(sizeProperty());
        prefHeightProperty().bind(sizeProperty());
        minWidthProperty().bind(sizeProperty());
        minHeightProperty().bind(sizeProperty());
        maxWidthProperty().bind(sizeProperty());
        maxHeightProperty().bind(sizeProperty());
    }

    /**
     * Constructs a new avatar view with the given initials and image. The
     * view will display the image unless it is null. Should the image be
     * removed at a later point in time then the initials will be shown.
     *
     * @param initials the initials
     * @param image the image
     */
    public AvatarView(String initials, Image image) {
        this();
        setInitials(initials);
        setImage(image);
    }

    /**
     * Constructs a new avatar view with the given initials. If an image gets
     * added later on then the image will be shown.
     *
     * @param initials the initials
     */
    public AvatarView(String initials) {
        this();
        setInitials(initials);
    }

    public AvatarView(Image image) {
        this();
        setImage(image);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AvatarViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(AvatarView.class.getResource("avatar-view.css")).toExternalForm();
    }

    // number of styles

    private final IntegerProperty numberOfStyles = new SimpleIntegerProperty(this, "numberOfStyles", 5);

    public final int getNumberOfStyles() {
        return numberOfStyles.get();
    }

    /**
     * A property used to specify how many different styles the application wants to support
     * for styling the view based on the initials. By default, this value is "5", which means that
     * the view will have styles like this: style0, style1, style2, style3, style4 (but always only
     * one of them). These five styles will use five different background colors. Please be aware that
     * changing the number of styles requires you to define additional CSS rules as the default agent
     * stylesheet that ships with this control only supports five different styles out of the box.
     *
     * @return the number of supported styles
     */
    public final IntegerProperty numberOfStylesProperty() {
        return numberOfStyles;
    }

    public final void setNumberOfStyles(int numberOfStyles) {
        this.numberOfStyles.set(numberOfStyles);
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

    // arc size

    private final DoubleProperty arcSize = new StyleableDoubleProperty(DEFAULT_ARC_SIZE) {
        @Override
        public Object getBean() {
            return AvatarView.this;
        }

        @Override
        public String getName() {
            return "arcSize";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.AVATAR_ARC_SIZE;
        }
    };

    public final double getArcSize() {
        return arcSize.get();
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
    public final DoubleProperty arcSizeProperty() {
        return arcSize;
    }

    public final void setArcSize(double arcSize) {
        this.arcSize.set(arcSize);
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

    // shape

    /**
     * Enumeration representing the shape of the avatar.
     */
    public enum AvatarShape {

        /**
         * The avatar will have a circular shape.
         */
        ROUND,

        /**
         * The avatar will have a rectangular shape.
         */
        SQUARE
    }

    private final StyleableObjectProperty<AvatarShape> avatarShape = new StyleableObjectProperty<>(DEFAULT_AVATAR_SHAPE) {
        @Override
        public CssMetaData<? extends Styleable, AvatarShape> getCssMetaData() {
            return StyleableProperties.AVATAR_SHAPE;
        }

        @Override
        public Object getBean() {
            return AvatarView.this;
        }

        @Override
        public String getName() {
            return "avatarShape";
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
    public final ObjectProperty<AvatarShape> avatarShapeProperty() {
        return this.avatarShape;
    }

    public final AvatarShape getAvatarShape() {
        return this.avatarShapeProperty().get();
    }

    public final void setAvatarShape(final AvatarShape type) {
        this.avatarShapeProperty().set(type);
    }

    private final IntegerProperty magicNumber = new SimpleIntegerProperty(this, "magicNumber", -1);

    private void updateMagicNumber() {
        String initials = getInitials();
        if (StringUtils.isNotBlank(initials)) {
            char[] numbers;
            int spice;
            if (initials.toLowerCase().length() >= 2) {
                numbers = initials.toLowerCase().substring(0, 2).toCharArray();
                spice = numbers[0] < numbers[1] ? 0 : 1;
            } else {
                numbers = initials.toLowerCase().substring(0, 1).toCharArray();
                spice = 1;
            }

            int sum = 0;
            for (char c : numbers) {
                sum += c;
            }

            magicNumber.set(sum + spice);
        } else {
            magicNumber.set(-1);
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<AvatarView, AvatarShape> AVATAR_SHAPE = new CssMetaData<>(
                "-fx-avatar-shape", new EnumConverter<>(AvatarShape.class), DEFAULT_AVATAR_SHAPE) {
            @Override
            public boolean isSettable(AvatarView control) {
                return !control.avatarShape.isBound();
            }

            @Override
            public StyleableProperty<AvatarShape> getStyleableProperty(AvatarView control) {
                return (StyleableProperty<AvatarShape>) control.avatarShapeProperty();
            }
        };

        private static final CssMetaData<AvatarView, Number> AVATAR_ARC_SIZE =
                new CssMetaData<>("-fx-avatar-arc-size", SizeConverter.getInstance(), DEFAULT_ARC_SIZE) {
                    @Override
                    public boolean isSettable(AvatarView n) {
                        return !n.arcSize.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AvatarView n) {
                        return (StyleableProperty<Number>) n.arcSizeProperty();
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
            Collections.addAll(styleables, AVATAR_SHAPE, AVATAR_ARC_SIZE, AVATAR_SIZE);
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
}
