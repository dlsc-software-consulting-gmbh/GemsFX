package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhoneNumberField;
import javafx.beans.InvalidationListener;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    private static final Image WORLD_ICON = new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("countryflags/world.png")).toExternalForm());
    private static final Map<PhoneNumberField.CountryCallingCode, Image> FLAGS = new HashMap<>();

    static {
        Arrays.stream(PhoneNumberField.DefaultCountryCallingCodes.values()).forEach(c -> FLAGS.put(c, new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("countryflags/20x13/" + c.getIso2Code().toLowerCase() + ".png")).toExternalForm())));
    }

    public PhoneNumberFieldSkin(PhoneNumberField control) {
        super(control);

        PhoneNumberEditor editor = new PhoneNumberEditor();

        ComboBox<PhoneNumberField.CountryCallingCode> comboBox = new ComboBox<>();
        comboBox.setMouseTransparent(true);// Disable all mouse events on the combo box
        comboBox.setButtonCell(editor);
        comboBox.setCellFactory(lv -> new CountryCallingCodeCell());
        comboBox.getItems().setAll(PhoneNumberField.DefaultCountryCallingCodes.values());
        comboBox.valueProperty().bindBidirectional(control.countryCallingCodeProperty());

        // Manually handle mouse pressed over either the text field or the country selector
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            Bounds textFieldBounds = editor.textField.getBoundsInParent();
            if (textFieldBounds.contains(evt.getX(), evt.getY())) {
                editor.textField.requestFocus();
                comboBox.hide();
            }
            else {
                Bounds buttonBounds = editor.buttonBox.getBoundsInParent();
                if (buttonBounds.contains(evt.getX(), evt.getY())) {
                    editor.buttonBox.requestFocus();
                    comboBox.show();
                }
                else {
                    comboBox.hide();
                }
            }
            evt.consume();
        });

        getChildren().addAll(comboBox);
    }

    private final class PhoneNumberEditor extends ListCell<PhoneNumberField.CountryCallingCode> {

        private final TextField textField = new TextField();
        private final Label maskLabel = new Label();
        private final HBox buttonBox = new HBox();

        public PhoneNumberEditor() {
            getStyleClass().add("editor");

            StackPane flagBox = new StackPane();
            flagBox.getStyleClass().add("flag-box");

            InvalidationListener updateFlag = it -> {
                PhoneNumberField.CountryCallingCode callingCode = PhoneNumberFieldSkin.this.getSkinnable().getCountryCallingCode();
                Image icon = WORLD_ICON;
                if (callingCode != null) {
                    icon = FLAGS.get(callingCode);
                }
                flagBox.getChildren().setAll(new ImageView(icon));
            };

            PhoneNumberFieldSkin.this.getSkinnable().countryCallingCodeProperty().addListener(updateFlag);
            updateFlag.invalidated(null);

            Region arrow = new Region();
            arrow.getStyleClass().add("arrow");

            StackPane arrowButton = new StackPane();
            arrowButton.getStyleClass().add("arrow-button");
            arrowButton.getChildren().add(arrow);

            buttonBox.getStyleClass().add("button-box");
            buttonBox.getChildren().addAll(flagBox, arrowButton);

            maskLabel.setText("(###) ###-####");
            maskLabel.getStyleClass().add("text-mask");

            textField.setTextFormatter(new TextFormatter<>(change -> {
                if (change.isAdded()) {
                    String text = change.getText();
                    if (!text.matches("[0-9]")) {
                        return null;
                    }
                }
                return change;
            }));
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

    private static class CountryCallingCodeCell extends ListCell<PhoneNumberField.CountryCallingCode> {

        CountryCallingCodeCell() {
            getStyleClass().add("country-calling-code-cell");
        }

        @Override
        protected void updateItem(PhoneNumberField.CountryCallingCode item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                StackPane flagView = new StackPane();
                flagView.getStyleClass().add("flag-icon");
                flagView.getChildren().add(new ImageView(FLAGS.get(item)));
                setText("(+" + item.getCountryCode() + ") " + item.getCountryName("en"));
                setGraphic(flagView);
            } else {
                setText(null);
                setGraphic(null);
            }
        }

    }

}
