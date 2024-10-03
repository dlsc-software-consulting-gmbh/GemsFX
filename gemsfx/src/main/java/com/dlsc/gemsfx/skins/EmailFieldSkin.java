package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.EmailField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.Objects;

public class EmailFieldSkin extends SkinBase<EmailField> {

    private final CustomTextField customTextField;
    private final DomainPopup domainPopup;

    public EmailFieldSkin(EmailField field) {
        super(field);

        customTextField = field.getEditor();
        domainPopup = new DomainPopup();

        Region mailIcon = new Region();
        mailIcon.getStyleClass().add("mail-icon");
        StackPane leftIconWrapper = new StackPane(mailIcon);
        leftIconWrapper.getStyleClass().add("mail-icon-wrapper");
        leftIconWrapper.managedProperty().bind(leftIconWrapper.visibleProperty());
        leftIconWrapper.visibleProperty().bind(field.showMailIconProperty());
        leftIconWrapper.visibleProperty().addListener(it -> customTextField.requestLayout());

        Region rightIcon = new Region();
        rightIcon.getStyleClass().add("validation-icon");
        StackPane rightIconWrapper = new StackPane(rightIcon);
        rightIconWrapper.getStyleClass().add("validation-icon-wrapper");
        rightIconWrapper.managedProperty().bind(rightIconWrapper.visibleProperty());
        rightIconWrapper.visibleProperty().bind(field.showValidationIconProperty().and(field.validProperty().not()));
        rightIconWrapper.visibleProperty().addListener(it -> customTextField.requestLayout());

        Tooltip invalidToolTip = new Tooltip();
        invalidToolTip.textProperty().bind(field.invalidTextProperty());
        updateTooltipVisibility(field.getInvalidText(), rightIconWrapper, invalidToolTip);
        field.invalidTextProperty().addListener((ob, ov, newValue) -> updateTooltipVisibility(newValue, rightIconWrapper, invalidToolTip));

        customTextField.textProperty().bindBidirectional(field.emailAddressProperty());
        customTextField.promptTextProperty().bind(field.promptTextProperty());
        customTextField.setLeft(leftIconWrapper);
        customTextField.setRight(rightIconWrapper);

        getChildren().setAll(customTextField);

        customTextField.textProperty().subscribe(this::handleSuggestionPopupVisibility);
        customTextField.focusedProperty().subscribe(this::handleSuggestionPopupVisibility);
    }

    private void updateTooltipVisibility(String invalidText, StackPane node, Tooltip invalidToolTip) {
        if (StringUtils.isEmpty(invalidText)) {
            Tooltip.uninstall(node, invalidToolTip);
        } else {
            Tooltip.install(node, invalidToolTip);
        }
    }

    /**
     * Controls the visibility of the domain suggestion popup based on specific conditions.
     * <p>
     * The popup is shown when all the following conditions are met:
     * - There is no exact domain match with the text entered after the '@' symbol.
     * - At least one domain in the domain list starts with the entered text (case-insensitive).
     * - Auto domain completion is enabled.
     * - The custom text field is currently focused.
     * <p>
     * If any of these conditions are not met, the popup will be hidden.
     */
    private void handleSuggestionPopupVisibility() {
        String text = customTextField.textProperty().getValueSafe();
        int atIndex = text.lastIndexOf('@');

        // If no '@' symbol is found, hide the popup and return
        if (atIndex == -1) {
            domainPopup.hide();
            return;
        }

        String enteredText = text.substring(atIndex + 1);
        boolean exactMatch = false;
        boolean startsWithMatch = false;

        for (String domain : getSkinnable().getDomainList()) {
            if (StringUtils.startsWithIgnoreCase(domain, enteredText)) {
                startsWithMatch = true;
                if (StringUtils.equalsIgnoreCase(domain, enteredText)) {
                    exactMatch = true;
                    break;
                }
            }
        }

        boolean shouldShowPopup = !exactMatch && startsWithMatch
                && getSkinnable().getAutoDomainCompletionEnabled()
                && customTextField.isFocused();

        if (shouldShowPopup) {
            Platform.runLater(() -> showSuggestionPopup(atIndex));
        } else {
            domainPopup.hide();
        }
    }

    private void showSuggestionPopup(int atIndex) {
        // Position the popup
        Bounds textFieldBounds = customTextField.localToScreen(customTextField.getBoundsInLocal());
        TextFieldSkin skin = (TextFieldSkin) customTextField.getSkin();
        if (skin != null) {
            Rectangle2D atSymbolBounds = skin.getCharacterBounds(atIndex);
            if (atSymbolBounds != null) {
                double popupX = atSymbolBounds.getMaxX();
                double popupY = atSymbolBounds.getMaxY();
                Point2D popupLocation = customTextField.localToScreen(popupX, popupY);
                domainPopup.show(customTextField, popupLocation.getX(), textFieldBounds.getMaxY());
            } else if (textFieldBounds != null) {
                domainPopup.show(customTextField, textFieldBounds.getMinX(), textFieldBounds.getMaxY());
            }
        }
    }

    private void handleSuggestionSelection(ListView<String> listView) {
        String selectedDomain = listView.getSelectionModel().getSelectedItem();
        String text = customTextField.getText();
        int atIndex = text.indexOf('@');
        if (atIndex != -1 && selectedDomain != null) {
            customTextField.replaceText(atIndex + 1, text.length(), selectedDomain);
            customTextField.positionCaret(customTextField.getText().length());
        }
        domainPopup.hide();
    }

    /**
     * DomainPopup is a private class that extends PopupControl to create a custom
     * popup used for displaying domain suggestions in the context of an EmailField component.
     * <p>
     * It has properties that automatically fix and hide the popup as needed.
     */
    private class DomainPopup extends PopupControl {

        public static final String DEFAULT_STYLE_CLASS = "suggestion-popup";

        public DomainPopup() {
            getStyleClass().add(DEFAULT_STYLE_CLASS);

            setAutoFix(true);
            setAutoHide(true);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new DomainPopupSkin(this);
        }
    }

    private class DomainPopupSkin implements Skin<DomainPopup> {

        private final DomainPopup popup;
        private final StackPane root;

        public DomainPopupSkin(DomainPopup popup) {
            this.popup = popup;

            // Suggestion list view
            ListView<String> suggestionListView = new ListView<>();
            suggestionListView.getStyleClass().add("suggestion-list-view");
            initializeSuggestionListView(suggestionListView);

            // Root pane
            root = new StackPane() {
                @Override
                public String getUserAgentStylesheet() {
                    return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
                }
            };
            root.getStyleClass().add("content-pane");
            root.getChildren().add(suggestionListView);
        }

        @Override
        public DomainPopup getSkinnable() {
            return popup;
        }

        @Override
        public Node getNode() {
            return root;
        }

        @Override
        public void dispose() {
        }
    }

    private void initializeSuggestionListView(ListView<String> listView) {
        FilteredList<String> filteredList = new FilteredList<>(getSkinnable().getDomainList());
        filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> item -> {
            String text = customTextField.getText();
            int atIndex = text.lastIndexOf('@');
            if (atIndex != -1) {
                String enteredText = text.substring(atIndex + 1);
                return StringUtils.startsWithIgnoreCase(item, enteredText);
            }
            return true;
        }, customTextField.textProperty()));

        listView.setItems(filteredList);
        listView.cellFactoryProperty().bind(getSkinnable().domainListCellFactoryProperty());

        // Handle mouse click events on list items
        listView.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && !event.isConsumed()) {
                handleSuggestionSelection(listView);
                event.consume();
            }
        });

        // Handle keyboard events
        listView.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSuggestionSelection(listView);
                event.consume();
            }
        });
    }
}
