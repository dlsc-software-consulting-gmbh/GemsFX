package com.dlsc.gemsfx;

import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.scene.control.ComboBoxBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomComboBox<T> extends ComboBoxBase<T> {

    private static final ButtonDisplay DEFAULT_BUTTON_DISPLAY = ButtonDisplay.RIGHT;
    private static final PseudoClass PSEUDO_CLASS_LEFT = PseudoClass.getPseudoClass("left");
    private static final PseudoClass PSEUDO_CLASS_RIGHT = PseudoClass.getPseudoClass("right");
    private static final PseudoClass PSEUDO_CLASS_BUTTON_ONLY = PseudoClass.getPseudoClass("button-only");
    private static final PseudoClass PSEUDO_CLASS_FIELD_ONLY = PseudoClass.getPseudoClass("field-only");

    public enum ButtonDisplay {

        /**
         * Button positioned before the text field
         */
        LEFT,

        /**
         * Button positioned after the text field
         */
        RIGHT,

        /**
         * Only the button is displayed
         */
        BUTTON_ONLY,

        /**
         * Only the text field is displayed
         */
        FIELD_ONLY
    }

    public CustomComboBox() {
        pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, true);
    }

    /**
     * This property allows customization of the button's display mode within the picker component.
     * Four display modes are supported:
     * <P>
     * {@link ButtonDisplay#LEFT}: Places the button to the left side of the text field. <br/>
     * {@link ButtonDisplay#RIGHT}: Places the button to the right side of the text field, which is the default behavior.<br/>
     * {@link ButtonDisplay#BUTTON_ONLY}: Only the button is visible, and the text field is hidden.<br/>
     * {@link ButtonDisplay#FIELD_ONLY}: Only the text field is visible, and the button is hidden.
     * <P>
     *
     * @return  the button display property of the picker
     */
    public final ObjectProperty<ButtonDisplay> buttonDisplayProperty() {
        if (buttonDisplay == null) {
            buttonDisplay = new StyleableObjectProperty<>(DEFAULT_BUTTON_DISPLAY) {

                @Override
                protected void invalidated() {
                    final ButtonDisplay value = get();
                    pseudoClassStateChanged(PSEUDO_CLASS_LEFT, value == ButtonDisplay.LEFT);
                    pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, value == ButtonDisplay.RIGHT);
                    pseudoClassStateChanged(PSEUDO_CLASS_BUTTON_ONLY, value == ButtonDisplay.BUTTON_ONLY);
                    pseudoClassStateChanged(PSEUDO_CLASS_FIELD_ONLY, value == ButtonDisplay.FIELD_ONLY);
                }

                @Override
                public CssMetaData<CustomComboBox, ButtonDisplay> getCssMetaData() {
                    return CustomComboBox.StyleableProperties.BUTTON_DISPLAY;
                }

                @Override
                public Object getBean() {
                    return CustomComboBox.this;
                }

                @Override
                public String getName() {
                    return "buttonDisplay";
                }
            };
        }
        return buttonDisplay;
    }

    private ObjectProperty<ButtonDisplay> buttonDisplay;

    public final void setButtonDisplay(ButtonDisplay value) {
        buttonDisplayProperty().setValue(value);
    }

    public final ButtonDisplay getButtonDisplay() {
        return buttonDisplay == null ? DEFAULT_BUTTON_DISPLAY : buttonDisplay.getValue();
    }

    private static class StyleableProperties {

        private static final CssMetaData<CustomComboBox, ButtonDisplay> BUTTON_DISPLAY = new CssMetaData<>("-fx-button-display", new EnumConverter<>(ButtonDisplay.class), DEFAULT_BUTTON_DISPLAY) {

            @Override
            public boolean isSettable(CustomComboBox styleable) {
                return styleable.buttonDisplay == null || !styleable.buttonDisplay.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<ButtonDisplay> getStyleableProperty(CustomComboBox styleable) {
                return (StyleableProperty<ButtonDisplay>) styleable.buttonDisplayProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ComboBoxBase.getClassCssMetaData());
            Collections.addAll(styleables, BUTTON_DISPLAY);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CustomComboBox.StyleableProperties.STYLEABLES;
    }

}
