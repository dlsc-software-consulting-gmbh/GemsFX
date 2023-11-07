package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhoneNumberField;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    private static final Image WORLD_ICON = new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("phonenumberfield/world.png")).toExternalForm());

    private static final Map<PhoneNumberField.CountryCallingCode, Image> FLAG_IMAGES = new HashMap<>();

    private static final Comparator<PhoneNumberField.CountryCallingCode> NAME_SORT_ASC = (c1, c2) -> {
        String c1Name = new Locale("en", c1.iso2Code()).getDisplayCountry();
        String c2Name = new Locale("en", c2.iso2Code()).getDisplayCountry();
        return c1Name.compareTo(c2Name);
    };

    static {
        for (PhoneNumberField.CountryCallingCode code : PhoneNumberField.CountryCallingCode.Defaults.values()) {
            FLAG_IMAGES.put(code, new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("phonenumberfield/country-flags/20x13/" + code.iso2Code().toLowerCase() + ".png")).toExternalForm()));
        }
    }

    public PhoneNumberFieldSkin(PhoneNumberField field, TextField textField) {
        super(field);

        ObservableList<PhoneNumberField.CountryCallingCode> callingCodes = FXCollections.observableArrayList();
        Runnable callingCodesUpdater = () -> {
            Set<PhoneNumberField.CountryCallingCode> temp1 = new TreeSet<>(NAME_SORT_ASC);
            Set<PhoneNumberField.CountryCallingCode> temp2 = new TreeSet<>(NAME_SORT_ASC);

            field.getAvailableCountryCodes().forEach(code -> {
                if (!field.getPreferredCountryCodes().contains(code)) {
                    temp2.add(code);
                }
            });

            field.getPreferredCountryCodes().forEach(code -> {
                if (field.getAvailableCountryCodes().contains(code)) {
                    temp1.add(code);
                }
            });

            List<PhoneNumberField.CountryCallingCode> temp = new ArrayList<>();
            temp.addAll(temp1);
            temp.addAll(temp2);
            callingCodes.setAll(temp);

            if (field.getCountryCallingCode() != null && !temp.contains(field.getCountryCallingCode())) {
                // Clear up the value in case the country code is not available anymore
                field.setPhoneNumber(null);
            }
        };

        InvalidationListener listener = obs -> callingCodesUpdater.run();
        field.getAvailableCountryCodes().addListener(listener);
        field.getPreferredCountryCodes().addListener(listener);
        field.countryCodeViewFactoryProperty().addListener(listener);
        callingCodesUpdater.run();

        PhoneNumberEditor editor = new PhoneNumberEditor(textField);

        ComboBox<PhoneNumberField.CountryCallingCode> comboBox = new ComboBox<>();
        comboBox.setButtonCell(editor);
        comboBox.setCellFactory(lv -> new CountryCallingCodeCell());
        comboBox.setItems(callingCodes);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setMaxHeight(Double.MAX_VALUE);
        comboBox.setFocusTraversable(false);
        comboBox.valueProperty().bindBidirectional(field.countryCallingCodeProperty());

        // Manually handle mouse event either on the text field or the trigger button box
        field.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
            Bounds buttonBounds = editor.buttonBox.getBoundsInParent();
            if (buttonBounds.contains(evt.getX(), evt.getY())) {
                if (!editor.buttonBox.isDisabled()) {
                    editor.buttonBox.requestFocus();
                    if (!comboBox.isShowing()) {
                        comboBox.show();
                    }
                }
            }
            else {
                comboBox.hide();
                Bounds textFieldBounds = editor.textField.getBoundsInParent();
                if (textFieldBounds.contains(evt.getX(), evt.getY())) {
                    editor.textField.requestFocus();
                    if (editor.textField.getText() != null) {
                        editor.textField.positionCaret(editor.textField.getText().length());
                    } else {
                        editor.textField.positionCaret(0);
                    }
                }
            }
            evt.consume();
        });

        getChildren().addAll(comboBox);
    }

    private final class PhoneNumberEditor extends ListCell<PhoneNumberField.CountryCallingCode> {

        final TextField textField;
        final HBox buttonBox = new HBox();

        public PhoneNumberEditor(TextField textField) {
            getStyleClass().add("editor");

            this.textField = textField;

            StackPane flagBox = new StackPane();
            flagBox.getStyleClass().add("flag-box");

            Runnable flagUpdater = () -> flagBox.getChildren().setAll(getCountryCodeFlagView(getSkinnable().getCountryCallingCode()));
            getSkinnable().countryCallingCodeProperty().addListener(obs -> flagUpdater.run());
            getSkinnable().countryCodeViewFactoryProperty().addListener(obs -> flagUpdater.run());
            flagUpdater.run();

            Region arrow = new Region();
            arrow.getStyleClass().add("arrow");

            StackPane arrowButton = new StackPane();
            arrowButton.getStyleClass().add("arrow-button");
            arrowButton.getChildren().add(arrow);

            buttonBox.getStyleClass().add("button-box");
            buttonBox.getChildren().addAll(flagBox, arrowButton);
            buttonBox.managedProperty().bind(buttonBox.visibleProperty());
            buttonBox.disableProperty().bind(getSkinnable().disableCountryCodeProperty());
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SkinBase<>(this) {
                {
                    getChildren().addAll(buttonBox, textField);
                }

                @Override
                protected void layoutChildren(double x, double y, double w, double h) {
                    final double buttonWidth = snapSizeX(buttonBox.prefWidth(-1));
                    buttonBox.resizeRelocate(x, y, buttonWidth, h);

                    final double textFieldX = snapPositionX(x + buttonWidth);
                    textField.resizeRelocate(textFieldX, y, w - buttonWidth, h);
                }
            };
        }

    }

    private final class CountryCallingCodeCell extends ListCell<PhoneNumberField.CountryCallingCode> {

        private CountryCallingCodeCell() {
            getStyleClass().add("country-calling-code-cell");
        }

        @Override
        public String getUserAgentStylesheet() {
            // This is needed to get the cell styled up
            return getSkinnable().getUserAgentStylesheet();
        }

        @Override
        protected void updateItem(PhoneNumberField.CountryCallingCode item, boolean empty) {
            super.updateItem(item, empty);

            int index = -1;

            if (item != null && !empty) {
                setText("(" + item.phonePrefix() + ") " + new Locale("en", item.iso2Code()).getDisplayCountry());
                setGraphic(getCountryCodeFlagView(item));
                index = getSkinnable().getPreferredCountryCodes().indexOf(item);
            } else {
                setText(null);
                setGraphic(null);
            }

            if (index >= 0) {
                getStyleClass().add("preferred");
                if (index == getSkinnable().getPreferredCountryCodes().size() - 1) {
                    getStyleClass().add("last");
                } else {
                    getStyleClass().remove("last");
                }
            } else {
                getStyleClass().remove("preferred");
                getStyleClass().remove("last");
            }
        }

    }

    private Node getCountryCodeFlagView(PhoneNumberField.CountryCallingCode code) {
        Node flagView;
        if (code != null) {
            if (getSkinnable().getCountryCodeViewFactory() != null) {
                flagView = getSkinnable().getCountryCodeViewFactory().call(code);
            } else {
                flagView = new ImageView(Optional.ofNullable(FLAG_IMAGES.get(code)).orElse(WORLD_ICON));
            }
        } else {
            flagView = new ImageView(WORLD_ICON);
        }
        return flagView;
    }

}
