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
    private final SuggestionPopup suggestionPopup;

    public EmailFieldSkin(EmailField field) {
        super(field);

        customTextField = field.getEditor();
        suggestionPopup = new SuggestionPopup();

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

    private void handleSuggestionPopupVisibility() {
        String text = customTextField.getText();

        boolean shouldShowPopup = text != null
                && text.contains("@")
                && getSkinnable().isAutoSuffixEnabled()
                && customTextField.isFocused();

        if (shouldShowPopup) {
            Platform.runLater(this::showSuggestionPopup);
        } else {
            suggestionPopup.hide();
        }
    }

    private void showSuggestionPopup() {
        EmailField emailField = getSkinnable();

        String text = customTextField.getText();
        int atIndex = text.lastIndexOf('@');
        if (atIndex == -1) {
            suggestionPopup.hide();
            return;
        }

        String enteredText = text.substring(atIndex + 1);

        // if entered suffix is already in the list, do not show suggestions
        if (emailField.getSuffixList().stream().anyMatch(suffix -> suffix.equals(enteredText))) {
            suggestionPopup.hide();
            return;
        }

        // Position the popup
        Bounds textFieldBounds = customTextField.localToScreen(customTextField.getBoundsInLocal());
        TextFieldSkin skin = (TextFieldSkin) customTextField.getSkin();
        if (skin != null) {
            Rectangle2D atSymbolBounds = skin.getCharacterBounds(atIndex);
            if (atSymbolBounds != null) {
                double popupX = atSymbolBounds.getMaxX();
                double popupY = atSymbolBounds.getMaxY();
                Point2D popupLocation = customTextField.localToScreen(popupX, popupY);
                suggestionPopup.show(customTextField, popupLocation.getX(), textFieldBounds.getMaxY());
            } else if (textFieldBounds != null) {
                suggestionPopup.show(customTextField, textFieldBounds.getMinX(), textFieldBounds.getMaxY());
            }
        }
    }

    private void handleSuggestionSelection(ListView<String> listView) {
        String selectedSuffix = listView.getSelectionModel().getSelectedItem();
        String text = customTextField.getText();
        int atIndex = text.indexOf('@');
        if (atIndex != -1 && selectedSuffix != null) {
            customTextField.replaceText(atIndex + 1, text.length(), selectedSuffix);
            customTextField.positionCaret(customTextField.getText().length());
        }
        suggestionPopup.hide();
    }

    /**
     * SuggestionPopup is a custom popup control used for displaying suggestions
     * related to the content of an associated text field. It automatically hides
     * and adjusts its position as needed.
     */
    private class SuggestionPopup extends PopupControl {

        public static final String DEFAULT_STYLE_CLASS = "suggestion-popup";

        public SuggestionPopup() {
            getStyleClass().add(DEFAULT_STYLE_CLASS);

            setAutoFix(true);
            setAutoHide(true);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SuggestionPopupSkin(this);
        }
    }

    private class SuggestionPopupSkin implements Skin<SuggestionPopup> {

        private final SuggestionPopup popup;
        private final StackPane root;

        public SuggestionPopupSkin(SuggestionPopup popup) {
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
        public SuggestionPopup getSkinnable() {
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
        FilteredList<String> filteredList = new FilteredList<>(getSkinnable().getSuffixList());
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
        listView.cellFactoryProperty().bind(getSkinnable().suffixListCellFactoryProperty());

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
