package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearPickerSkin;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.css.PseudoClass;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.Year;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A control for selecting a year. This class utilizes the {@link YearView} control inside of its
 * popup.
 */
public class YearPicker extends CustomComboBox<Year> {

    private final TextField editor = new TextField();
    private final NumberStringFilteredConverter converter = new NumberStringFilteredConverter();

    private YearView yearView;

    /**
     * Constructs a new year picker.
     */
    public YearPicker() {
        getStyleClass().setAll("year-picker", "text-input");

        setFocusTraversable(false);
        setEditable(true);

        setOnTouchPressed(evt -> commitValueAndShow());

        valueProperty().addListener((obs, oldV, newV) -> {
            updateTextAndHidePopup(newV);
            year.set(newV == null ? null : newV.getValue());
        });

        editor.setTextFormatter(new TextFormatter<>(converter, null, converter.getFilter()));
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commit());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commit();
            }
            pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), editor.isFocused());
        });

        editor.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.UP)) {
                setValue(getValue().minusYears(1));
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.DOWN)) {
                setValue(getValue().plusYears(1));
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.LEFT) && !isEditable()) {
                setValue(getValue().minusYears(1));
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.RIGHT) && !isEditable()) {
                setValue(getValue().plusYears(1));
                placeCaretAtEnd();
            }
        });

        setMaxWidth(Region.USE_PREF_SIZE);

        // call last
        setValue(Year.now());
    }

    /**
     * Returns the text field control used for manual input.
     *
     * @return the editor / text field
     */
    public final TextField getEditor() {
        return editor;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearPickerSkin(this);
    }

    private void placeCaretAtEnd() {
        Platform.runLater(() -> getEditor().positionCaret(getEditor().textProperty().getValueSafe().length()));
    }

    /**
     * Returns the view that is being used by the picker to let the user chose
     * a year.
     *
     * @return the view showing the years
     */
    public YearView getYearView() {
        if (yearView == null) {
            yearView = new YearView();
        }
        return yearView;
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(YearMonthView.class.getResource("year-picker.css")).toExternalForm();
    }

    private final ReadOnlyObjectWrapper<Integer> year = new ReadOnlyObjectWrapper<>(this, "year");

    public final ReadOnlyObjectProperty<Integer> yearProperty() {
        return year.getReadOnlyProperty();
    }

    public final Integer getYear() {
        return year.get();
    }

    private void commitValueAndShow() {
        commit();
        show();
    }

    private void commit() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            Number value = converter.fromString(text);
            if (value != null) {
                setValue(Year.of(value.intValue()));
            } else {
                setValue(null);
            }
        }
    }

    private void updateTextAndHidePopup(Year value) {
        if (value != null) {
            editor.setText(String.valueOf(value.getValue()));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());

        YearPickerSkin skin = (YearPickerSkin) getSkin();
        if (skin != null) {
            skin.hide();
        }
    }

    static class NumberStringFilteredConverter extends NumberStringConverter {

        public NumberStringFilteredConverter() {
            super(new DecimalFormat("####"));
        }

        UnaryOperator<TextFormatter.Change> getFilter() {
            return change -> {
                String newText = change.getControlNewText();

                if (!newText.isEmpty()) {
                    // Convert to number
                    ParsePosition parsePosition = new ParsePosition(0);
                    Number value = getNumberFormat().parse(newText, parsePosition);
                    if (value == null || parsePosition.getIndex() < newText.length()) {
                        return null;
                    }

                    // Validate max length
                    if (newText.length() > 4) {
                        String head = change.getControlNewText().substring(0, 4);
                        change.setText(head);
                        int oldLength = change.getControlText().length();
                        change.setRange(0, oldLength);
                    }
                }

                return change;
            };
        }
    }
}
