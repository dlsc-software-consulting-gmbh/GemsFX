package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CountryCallingCode;
import com.dlsc.gemsfx.PhoneNumberField;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    private static final Image WORLD_ICON = new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("phonenumberfield/world.png")).toExternalForm());

    public PhoneNumberFieldSkin(PhoneNumberField field) {
        super(field);

        ObservableList<CountryCallingCode> callingCodes = FXCollections.observableArrayList();
        InvalidationListener callingCodesUpdater = obs -> {
            List<CountryCallingCode> temp = new ArrayList<>(getSkinnable().getPreferredCountryCodes());
            getSkinnable().getAvailableCountryCodes().forEach(code -> {
                if (!temp.contains(code)) {
                    temp.add(code);
                }
            });
            callingCodes.setAll(temp);
        };

        getSkinnable().getAvailableCountryCodes().addListener(callingCodesUpdater);
        getSkinnable().getPreferredCountryCodes().addListener(callingCodesUpdater);
        callingCodesUpdater.invalidated(null);

        PhoneNumberEditor editor = new PhoneNumberEditor();

        ComboBox<CountryCallingCode> comboBox = new ComboBox<>();
        comboBox.setButtonCell(editor);
        comboBox.setCellFactory(lv -> new CountryCallingCodeCell());
        comboBox.setItems(callingCodes);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setMaxHeight(Double.MAX_VALUE);
        comboBox.setFocusTraversable(false);

        comboBox.valueProperty().addListener((obs, oldCode, newCode) -> {
            if (newCode != null) {
                StringBuilder value = new StringBuilder();
                value.append(newCode.countryCode());
                if (newCode.areaCodes().length > 0) {
                    value.append(newCode.areaCodes()[0]);
                }
                field.setPhoneNumber(value.toString());
            } else {
                field.setPhoneNumber(null);
            }
        });

        // Manually handle mouse event over either the text field or the trigger button box
        field.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
            Bounds buttonBounds = editor.buttonBox.getBoundsInParent();
            if (buttonBounds.contains(evt.getX(), evt.getY())) {
                editor.buttonBox.requestFocus();
                if (!comboBox.isShowing()) {
                    comboBox.show();
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

    private final class PhoneNumberEditor extends ListCell<CountryCallingCode> {

        final TextField textField = new TextField();
        final Label maskLabel = new Label();
        final HBox buttonBox = new HBox();

        public PhoneNumberEditor() {
            getStyleClass().add("editor");

            StackPane flagBox = new StackPane();
            flagBox.getStyleClass().add("flag-box");

            InvalidationListener updateFlag = it -> flagBox.getChildren()
                .setAll(Optional.ofNullable(getSkinnable().getCountryCode())
                    .map(CountryCallingCode::flagView)
                    .orElse(new ImageView(WORLD_ICON)));

            getSkinnable().countryCodeProperty().addListener(updateFlag);
            updateFlag.invalidated(null);

            Region arrow = new Region();
            arrow.getStyleClass().add("arrow");

            StackPane arrowButton = new StackPane();
            arrowButton.getStyleClass().add("arrow-button");
            arrowButton.getChildren().add(arrow);

            buttonBox.getStyleClass().add("button-box");
            buttonBox.getChildren().addAll(flagBox, arrowButton);

            maskLabel.getStyleClass().add("text-mask");

            textField.setTextFormatter(new TextFormatter<>(change -> {
                if (change.isAdded() || change.isReplaced()) {
                    String text = change.getText();
                    if (!text.matches("[0-9]+")) {
                        return null;
                    }
                }
                return change;
            }));

            textField.textProperty().bindBidirectional(getSkinnable().phoneNumberProperty());
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SkinBase<>(this) {
                {
                    getChildren().addAll(buttonBox, textField, maskLabel);
                }

                @Override
                protected void layoutChildren(double x, double y, double w, double h) {
                    final double buttonWidth = snapSizeX(buttonBox.prefWidth(-1));
                    buttonBox.resizeRelocate(x, y, buttonWidth, h);

                    final double textFieldX = snapPositionX(x + buttonWidth);
                    textField.resizeRelocate(textFieldX, y, w - buttonWidth, h);

                    final Node textNode = textField.lookup(".text");
                    final double maskX = snapPositionX(textFieldX + textNode.getLayoutBounds().getWidth());
                    final double maskWidth = snapSizeX(Math.max(0, Math.min(maskLabel.prefWidth(-1), w - maskX)));
                    maskLabel.resizeRelocate(maskX, y, maskWidth, h);
                }
            };
        }

    }

    private final class CountryCallingCodeCell extends ListCell<CountryCallingCode> {

        private CountryCallingCodeCell() {
            getStyleClass().add("country-calling-code-cell");
        }

        @Override
        public String getUserAgentStylesheet() {
            // This is needed to get the cell styled up
            return getSkinnable().getUserAgentStylesheet();
        }

        @Override
        protected void updateItem(CountryCallingCode item, boolean empty) {
            super.updateItem(item, empty);

            int index = -1;

            if (item != null && !empty) {
                setText("(+" + item.countryCode() + ") " + item.displayName("en"));
                setGraphic(item.flagView());
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

}
