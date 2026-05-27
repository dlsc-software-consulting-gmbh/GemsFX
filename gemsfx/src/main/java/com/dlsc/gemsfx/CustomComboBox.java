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

/**
 * A base combo-box control that supports configurable placement of the popup trigger button
 * relative to the text field.
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-button-display}</td><td>{@code ButtonDisplay}</td><td>Display mode of the picker button</td></tr>
 *   </tbody>
 * </table>
 */
public class CustomComboBox<T> extends ComboBoxBase<T> {

    private static final ButtonDisplay DEFAULT_BUTTON_DISPLAY = ButtonDisplay.RIGHT;
    private static final PseudoClass PSEUDO_CLASS_LEFT = PseudoClass.getPseudoClass("left");
    private static final PseudoClass PSEUDO_CLASS_RIGHT = PseudoClass.getPseudoClass("right");
    private static final PseudoClass PSEUDO_CLASS_BUTTON_ONLY = PseudoClass.getPseudoClass("button-only");
    private static final PseudoClass PSEUDO_CLASS_FIELD_ONLY = PseudoClass.getPseudoClass("field-only");

    /**
     * The supported display modes for the popup button.
     */
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

    /**
     * Constructs a new custom combo box.
     */
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
     * Can be set via CSS using the {@code -fx-button-display} property.
     * Valid values are: {@code left}, {@code right}, {@code button-only}, {@code field-only}.
     * The default value is {@code right}.
     *
     * @return  the button display property of the picker
     */
    public final ObjectProperty<ButtonDisplay> buttonDisplayProperty() {
        if (buttonDisplay == null) {
            buttonDisplay = new StyleableObjectProperty<>(DEFAULT_BUTTON_DISPLAY) {

                /**
                 * {@inheritDoc}
                 */
                @Override
                protected void invalidated() {
                    final ButtonDisplay value = get();
                    pseudoClassStateChanged(PSEUDO_CLASS_LEFT, value == ButtonDisplay.LEFT);
                    pseudoClassStateChanged(PSEUDO_CLASS_RIGHT, value == ButtonDisplay.RIGHT);
                    pseudoClassStateChanged(PSEUDO_CLASS_BUTTON_ONLY, value == ButtonDisplay.BUTTON_ONLY);
                    pseudoClassStateChanged(PSEUDO_CLASS_FIELD_ONLY, value == ButtonDisplay.FIELD_ONLY);
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the CSS metadata for this property
                 */
                @Override
                public CssMetaData<CustomComboBox, ButtonDisplay> getCssMetaData() {
                    return CustomComboBox.StyleableProperties.BUTTON_DISPLAY;
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the owning bean
                 */
                @Override
                public Object getBean() {
                    return CustomComboBox.this;
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the property name
                 */
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

            /**
             * {@inheritDoc}
             *
             * @param styleable the control to inspect
             * @return true if the property can be styled
             */
            @Override
            public boolean isSettable(CustomComboBox styleable) {
                return styleable.buttonDisplay == null || !styleable.buttonDisplay.isBound();
            }

            /**
             * {@inheritDoc}
             *
             * @param styleable the control to inspect
             * @return the styleable property
             */
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
        return CustomComboBox.StyleableProperties.STYLEABLES;
    }

}
