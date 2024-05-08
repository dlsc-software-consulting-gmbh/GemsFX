package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.LimitedTextAreaSkin;
import com.dlsc.gemsfx.util.IntegerRange;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A specialized text area view that imposes restrictions on the input text length, allowing
 * specification of minimum and maximum character limits. It provides the functionality to define
 * a range of allowed text lengths and supports the removal of specified strings by replacing them
 * with an empty string. This component is designed for scenarios where input text needs to adhere
 * to specific length requirements and certain substrings are not permitted.
 *
 * <p>
 * If the text length range is not specified, the value of the text length indicator label will reflect the number of characters in the text area.
 * If a range is specified, the label will show the difference between the current text length and the maximum allowed character count.
 * Should the text exceed the allowed maximum, the label will display a negative number.
 *
 * <p>
 * Use cases include form fields where input character count is restricted within a certain range
 * for validation purposes, or text areas that need to filter out specific unwanted characters or
 * phrases.
 */

public class LimitedTextArea extends ResizableTextArea {

    private static final String DEFAULT_STYLE_CLASS = "limited-text-area";
    private static final boolean DEFAULT_SHOW_BOTTOM = true;

    public enum LengthDisplayMode {

        /**
         * When in a warning or error state, the text length indicator label will be displayed.
         */
        AUTO,

        /**
         * Always show the length indicator.
         */
        ALWAYS_SHOW,

        /**
         * Always hide the length indicator.
         */
        ALWAYS_HIDE

    }

    public LimitedTextArea() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        getStylesheets().add(Objects.requireNonNull(LimitedTextArea.class.getResource("limited-text-area.css")).toExternalForm());
    }

    public LimitedTextArea(String text) {
        this();
        setText(text);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LimitedTextAreaSkin(this, outOfRange);
    }

    private BooleanProperty showBottom;

    public final BooleanProperty showBottomProperty() {
        if (showBottom == null) {
            showBottom = new StyleableBooleanProperty(DEFAULT_SHOW_BOTTOM) {
                @Override
                public Object getBean() {
                    return LimitedTextArea.this;
                }

                @Override
                public String getName() {
                    return "showBottom";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_BOTTOM;
                }
            };
        }
        return showBottom;
    }

    public final boolean isShowBottom() {
        return showBottom == null ? DEFAULT_SHOW_BOTTOM : showBottom.get();
    }

    public final void setShowBottom(boolean showBottom) {
        showBottomProperty().set(showBottom);
    }

    private final ObjectProperty<IntegerRange> characterRangeLimit = new SimpleObjectProperty<>(this, "characterRangeLimit");

    public final IntegerRange getCharacterRangeLimit() {
        return characterRangeLimit.get();
    }

    /**
     * The character range limit property defines the minimum and maximum number of characters allowed in the text area.
     *
     * @return the character range limit property
     */
    public final ObjectProperty<IntegerRange> characterRangeLimitProperty() {
        return characterRangeLimit;
    }

    public final void setCharacterRangeLimit(IntegerRange characterRangeLimit) {
        this.characterRangeLimit.set(characterRangeLimit);
    }

    /**
     * A list of items that should be excluded from the text content.
     * e.g. "\r", "\n", "\t" ,"." etc.
     */
    private final ObservableList<String> excludedItems = FXCollections.observableArrayList();

    public final ObservableList<String> getExcludedItems() {
        return excludedItems;
    }

    private final StringProperty tips = new SimpleStringProperty(this, "tips");

    public final String getTips() {
        return tips.get();
    }

    /**
     * The tips property used to display a hint or description of the text area.
     */
    public final StringProperty tipsProperty() {
        return tips;
    }

    public final void setTips(String tips) {
        this.tips.set(tips);
    }

    private final ReadOnlyBooleanWrapper outOfRange = new ReadOnlyBooleanWrapper(this, "isOverLimit", false);

    public final boolean isOutOfRange() {
        return outOfRange.get();
    }

    /**
     * A read-only property indicating whether the text content is over the maximum length or under the minimum length.
     */
    public final ReadOnlyBooleanProperty outOfRangeProperty() {
        return outOfRange.getReadOnlyProperty();
    }

    private final void setOutOfRange(boolean outOfRange) {
        this.outOfRange.set(outOfRange);
    }

    private final ObjectProperty<LengthDisplayMode> lengthDisplayMode = new SimpleObjectProperty<>(this, "lengthDisplayMode", LengthDisplayMode.AUTO);

    public final LengthDisplayMode getLengthDisplayMode() {
        return lengthDisplayMode.get();
    }

    /**
     * The length display mode property defines when the text length indicator label should be displayed.
     * {@link LengthDisplayMode#AUTO}, {@link LengthDisplayMode#ALWAYS_SHOW}, {@link LengthDisplayMode#ALWAYS_HIDE}
     *
     * @return the length display mode property
     */
    public final ObjectProperty<LengthDisplayMode> lengthDisplayModeProperty() {
        return lengthDisplayMode;
    }

    public final void setLengthDisplayMode(LengthDisplayMode lengthDisplayMode) {
        this.lengthDisplayMode.set(lengthDisplayMode);
    }

    private final DoubleProperty warningThreshold = new SimpleDoubleProperty(this, "warningThreshold", 0.9);

    public final double getWarningThreshold() {
        return warningThreshold.get();
    }

    /**
     * The warning threshold is a value between 0 and 1.
     * When the text length is greater than or equal to the maximum length times the warning threshold, the warning style will be applied.
     */
    public final DoubleProperty warningThresholdProperty() {
        return warningThreshold;
    }

    public final void setWarningThreshold(double warningThreshold) {
        this.warningThreshold.set(warningThreshold);
    }

    public final double getValidWarningThreshold() {
        return Math.min(Math.max(getWarningThreshold(), 0), 0.999999);
    }

    private static class StyleableProperties {

        private static final CssMetaData<LimitedTextArea, Boolean> SHOW_BOTTOM = new CssMetaData<>(
                "-fx-show-bottom", BooleanConverter.getInstance(), DEFAULT_SHOW_BOTTOM) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(LimitedTextArea control) {
                return (StyleableProperty<Boolean>) control.showBottomProperty();
            }

            @Override
            public boolean isSettable(LimitedTextArea control) {
                return control.showBottom == null || !control.showBottom.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ResizableTextArea.getClassCssMetaData());
            styleables.add(SHOW_BOTTOM);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return LimitedTextArea.StyleableProperties.STYLEABLES;
    }

    // Issue: some css styles will fail. All css files merged into one maybe a solution
    //@Override
    //public String getUserAgentStylesheet() {
    //    return LimitedTextArea.class.getResource("limited-text-area.css").toExternalForm();
    //}

}
