package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.SelectionBox;
import com.dlsc.gemsfx.util.UIUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SelectionBoxSkin<T> extends SkinBase<SelectionBox<T>> {

    private static final PseudoClass READ_ONLY_PSEUDO_CLASS = PseudoClass.getPseudoClass("readonly");
    private static final PseudoClass EMPTY_SELECTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");
    private static final PseudoClass SHOWING_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing");
    private static final PseudoClass SINGLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("single");
    private static final PseudoClass MULTIPLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("multiple");

    private static final String UPDATE_POPUP_CONTENT = "updatePopupContent";
    private static final String UPDATE_SELECTION_IN_POPUP = "updateSelectionInPopup";
    private static final String UPDATE_EXTRA_BUTTONS_POSITION = "updateExtraButtonsPosition";

    private final SelectionBox<T> control;

    // ------ Display Nodes ------
    private final Label displayLabel;
    private final StackPane arrowButton;
    private final Region arrow;

    // ------ Popup ------
    private final SelectionPopup popup;

    private final ChangeListener<T> selectItemChangedListener = (obs, ov, nv) -> handleSelectionChange();
    private final ListChangeListener<T> selectItemsChangeListener = change -> handleSelectionChange();
    private final ChangeListener<SelectionMode> selectionModeChangeListener = (obs, oldMode, newMode) -> updatePseudoAndPopupContent();

    public SelectionBoxSkin(SelectionBox<T> control) {
        super(control);
        this.control = control;

        popup = new SelectionPopup(control);
        popup.showingProperty().subscribe(isShowing -> control.pseudoClassStateChanged(SHOWING_POPUP_PSEUDO_CLASS, isShowing));

        displayLabel = new Label();
        displayLabel.getStyleClass().add("display-label");
        displayLabel.graphicProperty().bind(control.graphicProperty());

        arrow = new Region();
        arrow.getStyleClass().add("arrow");
        arrow.setMaxHeight(Region.USE_PREF_SIZE);
        arrow.setMaxWidth(Region.USE_PREF_SIZE);
        arrow.setMouseTransparent(true);

        arrowButton = new StackPane(arrow);
        arrowButton.getStyleClass().add("arrow-button");
        arrowButton.setFocusTraversable(false);

        // Initialize display text
        updateDisplayLabelText();

        // Initialize pseudo classes
        updateModePseudoClass();
        updateEmptyPseudoClass();

        // Add listener to control
        addListenerToControl();

        getChildren().addAll(displayLabel, arrowButton);
    }

    private void addListenerToControl() {
        control.itemsProperty().addListener((obs, oldItems, newItems) -> updatePseudoAndPopupContent());

        control.extraButtonsPositionProperty().addListener((obs, oldPosition, newPosition) -> popup.updateExtraButtonsPosition());

        control.extraButtonsProviderProperty().addListener((obs, oldProvider, newProvider) -> popup.initializePopupContent());

        control.itemConverterProperty().addListener((obs, oldConverter, newConverter) -> updateDisplayLabelText());
        control.selectedItemsConverterProperty().addListener((obs, oldConverter, newConverter) -> updateDisplayLabelText());

        control.getSelectionModel().selectedItemProperty().addListener(selectItemChangedListener);
        control.getSelectionModel().getSelectedItems().addListener(selectItemsChangeListener);
        control.getSelectionModel().selectionModeProperty().addListener(selectionModeChangeListener);

        control.selectionModelProperty().addListener((obs, oldModel, newModel) -> {
            if (oldModel != null) {
                oldModel.selectedItemProperty().removeListener(selectItemChangedListener);
                oldModel.getSelectedItems().removeListener(selectItemsChangeListener);
                oldModel.selectionModeProperty().removeListener(selectionModeChangeListener);
            }
            if (newModel != null) {
                newModel.selectedItemProperty().addListener(selectItemChangedListener);
                newModel.getSelectedItems().addListener(selectItemsChangeListener);
                newModel.selectionModeProperty().addListener(selectionModeChangeListener);
            }
            popup.initializePopupContent();
        });

        // Handle readOnly property
        control.readOnlyProperty().subscribe(isNowReadOnly -> {
            pseudoClassStateChanged(READ_ONLY_PSEUDO_CLASS, isNowReadOnly);

            arrowButton.setVisible(!isNowReadOnly);
            if (isNowReadOnly) {
                popup.hide();
            }
        });

        // Add event handler to control to show the popup
        control.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!control.isDisabled() && !control.isReadOnly()) {
                control.requestFocus();
                if (popup.isShowing()) {
                    popup.hide();
                } else {
                    popup.show(control);
                }
            }
        });

        control.getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("showPopup")) {
                    if (Boolean.TRUE.equals(change.getValueAdded())) {
                        showPopup();
                    } else {
                        hidePopup();
                    }
                }
            }
        });
    }

    public void showPopup() {
        if (!control.isDisabled() && !control.isReadOnly()) {
            popup.show(control);
        }
    }

    public void hidePopup() {
        popup.hide();
    }

    private void handleSelectionChange() {
        if (!popup.isUpdating()) {
            updateDisplayLabelText();
            popup.updateSelectionInPopup();
            updateEmptyPseudoClass();
        }
    }

    private void updatePseudoAndPopupContent() {
        popup.initializePopupContent();
        updateDisplayLabelText();
        updateEmptyPseudoClass();
        updateModePseudoClass();
    }

    private void updateEmptyPseudoClass() {
        switch (control.getSelectionModel().getSelectionMode()) {
            case SINGLE ->
                    control.pseudoClassStateChanged(EMPTY_SELECTION_PSEUDO_CLASS, control.getSelectionModel().getSelectedItem() == null);
            case MULTIPLE ->
                    control.pseudoClassStateChanged(EMPTY_SELECTION_PSEUDO_CLASS, control.getSelectionModel().getSelectedItems().isEmpty());
        }
    }

    private void updateModePseudoClass() {
        SelectionMode mode = control.getSelectionModel().getSelectionMode();
        control.pseudoClassStateChanged(SINGLE_PSEUDO_CLASS, mode == SelectionMode.SINGLE);
        control.pseudoClassStateChanged(MULTIPLE_PSEUDO_CLASS, mode == SelectionMode.MULTIPLE);
    }

    public void updateDisplayLabelText() {
        SelectionMode mode = control.getSelectionModel().getSelectionMode();
        List<T> selectedItems;

        if (mode == SelectionMode.MULTIPLE) {
            selectedItems = new ArrayList<>(control.getSelectionModel().getSelectedItems());
        } else {
            T selectedItem = control.getSelectionModel().getSelectedItem();
            selectedItems = selectedItem != null ? Collections.singletonList(selectedItem) : Collections.emptyList();
        }

        StringConverter<List<T>> stringConverter = control.getSelectedItemsConverter();
        String text;
        if (stringConverter != null) {
            text = stringConverter.toString(selectedItems);
        } else {
            // Use default conversion logic
            text = getDefaultDisplayText(selectedItems);
        }
        displayLabel.setText(text);
    }

    private String getDefaultDisplayText(List<T> selectedItems) {
        int selectedCount = selectedItems.size();

        if (selectedCount == 0) {
            return "";
        } else if (selectedCount == 1) {
            return convertItemToText(selectedItems.get(0));
        } else {
            // Build the display text
            List<String> elements = selectedItems.stream().map(this::convertItemToText).toList();
            return String.join(", ", elements);
        }
    }

    private String convertItemToText(T item) {
        if (item == null) {
            return "";
        }
        StringConverter<T> itemConverter = control.getItemConverter();
        if (itemConverter == null) {
            return item.toString();
        } else {
            return itemConverter.toString(item);
        }
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computeBestWidth(displayLabel.minWidth(height), leftInset, rightInset);
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computeBestWidth(displayLabel.prefWidth(height), leftInset, rightInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + displayLabel.prefHeight(width) + bottomInset;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double arrowWidth = snapSizeX(arrow.prefWidth(-1));
        double arrowButtonWidth = arrowButton.snappedLeftInset() + arrowWidth + arrowButton.snappedRightInset();

        displayLabel.resizeRelocate(contentX, contentY, contentWidth - arrowButtonWidth, contentHeight);
        arrowButton.resize(arrowButtonWidth, contentHeight);

        positionInArea(arrowButton, (contentX + contentWidth) - arrowButtonWidth, contentY, arrowButtonWidth,
                contentHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    private double computeBestWidth(double displayLabelWidth, double leftInset, double rightInset) {
        final double arrowWidth = snapSizeX(arrow.prefWidth(-1));
        final double arrowButtonWidth = arrowButton.snappedLeftInset() + arrowWidth + arrowButton.snappedRightInset();

        final double totalWidth = displayLabelWidth + arrowButtonWidth;
        return leftInset + totalWidth + rightInset;
    }

    /**
     * Custom popup control to show the items in a popup.
     */
    private class SelectionPopup extends CustomPopupControl {

        private static final String DEFAULT_STYLE_CLASS = "selection-popup";
        private final SelectionBox<T> control;

        public SelectionPopup(SelectionBox<T> owner) {
            getStyleClass().add(DEFAULT_STYLE_CLASS);
            this.control = owner;

            setAutoFix(true);
            setAutoHide(true);
            setHideOnEscape(true);
        }

        public final SelectionBox<T> getOwner() {
            return control;
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SelectionPopupSkin(this);
        }

        public final void updateSelectionInPopup() {
            getProperties().put(UPDATE_SELECTION_IN_POPUP, true);
        }

        public final void initializePopupContent() {
            getProperties().put(UPDATE_POPUP_CONTENT, true);
        }

        public final void updateExtraButtonsPosition() {
            getProperties().put(UPDATE_EXTRA_BUTTONS_POSITION, true);
        }

        public final boolean isUpdating() {
            if (getSkin() instanceof SelectionBoxSkin.SelectionPopupSkin currentSkin) {
                return currentSkin.isUpdating();
            }
            return false;
        }

        @Override
        public void show(Node node) {
            super.show(node);

            Skin<?> skin = getSkin();
            if (skin != null && skin.getNode() instanceof Region popupContent) {
                if (!popupContent.minWidthProperty().isBound()) {
                    popupContent.minWidthProperty().bind(control.widthProperty());
                }
            }
        }
    }

    private class SelectionPopupSkin implements Skin<SelectionPopup> {

        private final SelectionPopup popup;
        private final VBox contentBox;
        private final VBox optionsBox;
        private final VBox extraButtonsBox;
        private final Separator separator;

        // Use indices as keys to handle duplicate items
        private final Map<Integer, BooleanProperty> itemButtonProperties = new LinkedHashMap<>();

        public SelectionPopupSkin(SelectionPopup popup) {
            this.popup = popup;

            contentBox = new VBox(){
                @Override
                public String getUserAgentStylesheet() {
                    return Objects.requireNonNull(SelectionBox.class.getResource("selection-box.css")).toExternalForm();
                }
            };
            contentBox.getStyleClass().add("content");
            contentBox.setPrefWidth(Region.USE_COMPUTED_SIZE);

            // Selection buttons container
            optionsBox = new VBox();
            optionsBox.getStyleClass().add("options-box");
            optionsBox.setFillWidth(true);
            optionsBox.managedProperty().bind(optionsBox.visibleProperty());
            optionsBox.visibleProperty().bind(popup.getOwner().itemsProperty().emptyProperty().not());

            // Extra buttons container
            extraButtonsBox = new VBox();
            extraButtonsBox.getStyleClass().add("extra-buttons-box");
            extraButtonsBox.setFillWidth(true);
            extraButtonsBox.managedProperty().bind(extraButtonsBox.visibleProperty());
            extraButtonsBox.visibleProperty().bind(popup.getOwner().showExtraButtonsProperty());

            separator = new Separator();
            separator.managedProperty().bind(separator.visibleProperty());
            separator.visibleProperty().bind(extraButtonsBox.visibleProperty().and(optionsBox.visibleProperty()));

            // Initialize the popup content
            updatePopupContent();
            updateSelectionInPopup();
            updateExtraButtonsPosition();

            // Listen to changes in the properties of the popup
            popup.getProperties().addListener((MapChangeListener<Object, Object>) change -> {
                if (change.wasAdded()) {
                    if (change.getKey().equals(UPDATE_POPUP_CONTENT)) {
                        updatePopupContent();
                        popup.getProperties().remove(UPDATE_POPUP_CONTENT);
                    }
                    if (change.getKey().equals(UPDATE_SELECTION_IN_POPUP)) {
                        updateSelectionInPopup();
                        popup.getProperties().remove(UPDATE_SELECTION_IN_POPUP);
                    }
                    if (change.getKey().equals(UPDATE_EXTRA_BUTTONS_POSITION)) {
                        updateExtraButtonsPosition();
                        popup.getProperties().remove(UPDATE_EXTRA_BUTTONS_POSITION);
                    }
                }
            });
        }

        private void updatePopupContent() {
            // Clear the content of the popup
            contentBox.getChildren().clear();
            optionsBox.getChildren().clear();
            extraButtonsBox.getChildren().clear();
            itemButtonProperties.clear();

            // Extra buttons
            Callback<MultipleSelectionModel<T>, List<Button>> extraButtonsProvider = popup.getOwner().getExtraButtonsProvider();
            if (extraButtonsProvider != null) {
                List<Button> buttons = extraButtonsProvider.call(popup.getOwner().getSelectionModel());
                if (buttons != null) {
                    buttons.forEach(button -> {
                        UIUtil.addClassIfAbsent(button, "extra-button");
                        if (button.getMaxWidth() == Region.USE_COMPUTED_SIZE && !button.maxWidthProperty().isBound()) {
                            button.setMaxWidth(Double.MAX_VALUE);
                        }
                        button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                            if (popup.getOwner().isAutoHideOnSelection()) {
                                popup.hide();
                            }
                        });
                    });
                    extraButtonsBox.getChildren().addAll(buttons);
                }
            }

            // Get items
            List<T> items = popup.getOwner().getItems();

            if (popup.getOwner().getSelectionModel().getSelectionMode() == SelectionMode.MULTIPLE) {
                // Create CheckBoxes for each item
                for (int i = 0; i < items.size(); i++) {
                    T item = items.get(i);
                    CheckBox checkBox = createCheckBoxItem(item, i);
                    itemButtonProperties.put(i, checkBox.selectedProperty());
                    optionsBox.getChildren().add(checkBox);
                }
            } else {
                // Create RadioButtons for each item
                ToggleGroup toggleGroup = new ToggleGroup();
                for (int i = 0; i < items.size(); i++) {
                    T item = items.get(i);
                    RadioButton radioButton = createRadioButtonItem(item, toggleGroup, i);
                    itemButtonProperties.put(i, radioButton.selectedProperty());
                    optionsBox.getChildren().add(radioButton);
                }
            }

            updateExtraButtonsPosition();
        }

        private RadioButton createRadioButtonItem(T item, ToggleGroup toggleGroup, int index) {
            RadioButton radioButton = new RadioButton();
            radioButton.textProperty().bind(Bindings.createStringBinding(() -> convertItemToText(item), control.itemConverterProperty()));
            radioButton.getStyleClass().addAll("item", "item-" + index, "single-item");
            radioButton.setMaxWidth(Double.MAX_VALUE);
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setSelected(popup.getOwner().getSelectionModel().isSelected(index));
            radioButton.setOnAction(e -> {
                if (!isUpdating()) {
                    if (radioButton.isSelected()) {
                        popup.getOwner().getSelectionModel().clearAndSelect(index);
                        if (popup.getOwner().isAutoHideOnSelection()) {
                            popup.hide();
                        }
                    }
                }
            });
            return radioButton;
        }

        private CheckBox createCheckBoxItem(T item, int index) {
            CheckBox checkBox = new CheckBox();
            checkBox.textProperty().bind(Bindings.createStringBinding(() -> convertItemToText(item), control.itemConverterProperty()));
            checkBox.getStyleClass().addAll("item", "item-" + index, "multiple-item");
            checkBox.setSelected(popup.getOwner().getSelectionModel().isSelected(index));
            checkBox.setMaxWidth(Double.MAX_VALUE);
            checkBox.setOnAction(e -> {
                if (!isUpdating()) {
                    if (checkBox.isSelected()) {
                        popup.getOwner().getSelectionModel().select(index);
                    } else {
                        popup.getOwner().getSelectionModel().clearSelection(index);
                    }
                }
            });
            return checkBox;
        }

        private void updateSelectionInPopup() {
            updating.set(true);

            for (Map.Entry<Integer, BooleanProperty> entry : itemButtonProperties.entrySet()) {
                int index = entry.getKey();
                BooleanProperty selectedProperty = entry.getValue();
                boolean shouldSelect = popup.getOwner().getSelectionModel().isSelected(index);
                if (selectedProperty.get() != shouldSelect) {
                    selectedProperty.set(shouldSelect);
                }
            }

            updating.set(false);
        }

        private void updateExtraButtonsPosition() {
            switch (control.getExtraButtonsPosition()) {
                case TOP -> contentBox.getChildren().setAll(extraButtonsBox, separator, optionsBox);
                case BOTTOM -> contentBox.getChildren().setAll(optionsBox, separator, extraButtonsBox);
            }
        }

        // updating

        private final ReadOnlyBooleanWrapper updating = new ReadOnlyBooleanWrapper(this, "updating", false);

        public final ReadOnlyBooleanProperty updatingProperty() {
            return updating.getReadOnlyProperty();
        }

        public final boolean isUpdating() {
            return updating.get();
        }

        @Override
        public SelectionPopup getSkinnable() {
            return popup;
        }

        @Override
        public Node getNode() {
            return contentBox;
        }

        @Override
        public void dispose() {
        }
    }
}