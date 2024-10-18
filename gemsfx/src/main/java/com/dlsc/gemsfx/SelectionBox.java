package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SelectionBoxSkin;
import com.dlsc.gemsfx.util.CustomMultipleSelectionModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * A versatile and customizable selection control that combines the features of {@link javafx.scene.control.ComboBox ComboBox}
 * and {@link javafx.scene.control.ChoiceBox ChoiceBox} with enhanced selection capabilities.
 * </p>
 *
 * <p>
 * Unlike {@code ComboBox} and {@code ChoiceBox}, which are limited to single selection modes, {@code SelectionBox}
 * supports both single and multiple selection modes, providing greater flexibility for various UI requirements.
 * Additionally, {@code SelectionBox} offers the ability to add extra buttons, enabling users to perform
 * common selection actions swiftly, such as selecting all items, clearing selections, or applying predefined selection criteria.
 * </p>
 */
public class SelectionBox<T> extends Control {

    private static final String DEFAULT_STYLE_CLASS = "selection-box";
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final boolean DEFAULT_SHOW_EXTRA_BUTTON = true;
    private static final VerticalPosition DEFAULT_EXTRA_BUTTON_POSITION = VerticalPosition.TOP;

    /**
     * The VerticalPosition enum represents the vertical position options available within a SelectionBox component.
     * It can be used to specify the position of additional extra buttons, relative to the main items in the SelectionBox.
     * The available positions are:
     * <p>
     * - TOP: Indicates that the extra buttons should be positioned above the main items.
     * - BOTTOM: Indicates that the extra buttons should be positioned below the main items.
     */
    public enum VerticalPosition {
        TOP, BOTTOM
    }

    public SelectionBox() {
        getStyleClass().setAll("combo-box-base", "combo-box", DEFAULT_STYLE_CLASS);

        // initialize the selection model
        CustomMultipleSelectionModel<T> model = new CustomMultipleSelectionModel<>();
        model.itemsProperty().bind(itemsProperty());
        setSelectionModel(model);

        // initialize the extra buttons provider
        setExtraButtonsProvider(selectionModel -> {
            SelectionMode mode = selectionModel.getSelectionMode();
            if (mode == SelectionMode.SINGLE) {
                return List.of(createExtraButton("Clear", selectionModel::clearSelection));
            } else {
                return List.of(
                        createExtraButton("Select All", selectionModel::selectAll),
                        createExtraButton("Clear", selectionModel::clearSelection)
                );
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SelectionBox.class.getResource("selection-box.css")).toExternalForm();
    }

    /**
     * Hides the popup by setting the "showPopup" property to false on the component's properties.
     */
    public final void hide() {
        getProperties().put("showPopup", false);
    }

    /**
     * Displays the popup by setting the "showPopup" property to true on the component's properties.
     */
    public final void show() {
        getProperties().put("showPopup", true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectionBoxSkin<>(this);
    }

    // items

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    /**
     * The list of items to be displayed in the SelectionBox.
     *
     * @return the list property containing the items
     */
    public final ListProperty<T> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList<T> items) {
        itemsProperty().set(items);
    }

    public final ObservableList<T> getItems() {
        return itemsProperty().get();
    }

    // extraButtonsProvider

    private final ObjectProperty<Callback<MultipleSelectionModel<T>, List<Button>>> extraButtonsProvider = new SimpleObjectProperty<>(this, "extraButtonsProvider");

    /**
     * A callback that provides a list of extra buttons based on the selection mode.
     * The callback is invoked when the control needs to display extra buttons.
     *
     * @return the extra buttons provider property
     */
    public final ObjectProperty<Callback<MultipleSelectionModel<T>, List<Button>>> extraButtonsProviderProperty() {
        return extraButtonsProvider;
    }

    public final Callback<MultipleSelectionModel<T>, List<Button>> getExtraButtonsProvider() {
        return extraButtonsProviderProperty().get();
    }

    public final void setExtraButtonsProvider(Callback<MultipleSelectionModel<T>, List<Button>> extraButtonsProvider) {
        extraButtonsProviderProperty().set(extraButtonsProvider);
    }

    /**
     * Creates an extra button with the specified text and action.
     * The button will be styled with the "extra-button" class and will adjust its visibility
     * based on its managed property. The button's maximum width will be set to the maximum double value.
     *
     * @param text the text to be displayed on the button
     * @param action the action to be executed when the button is clicked
     * @return the created Button instance
     */
    public Button createExtraButton(String text, Runnable action) {
        return createExtraButton(text, null, action);
    }

    /**
     * Creates an extra button with the specified text, graphic, and action.
     * The created button will be styled with "extra-button" class and will adjust its visibility
     * based on its managed property. The button's maximum width will be set to the maximum double value.
     *
     * @param text the text to be displayed on the button
     * @param graphic the graphic node to be displayed on the button
     * @param action the action to be executed when the button is clicked
     * @return the created Button instance
     */
    public Button createExtraButton(String text, Node graphic, Runnable action) {
        Button button = new Button(text, graphic);
        button.getStyleClass().add("extra-button");
        button.managedProperty().bind(button.visibleProperty());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
        return button;
    }

    // extraButtonsPosition

    private ObjectProperty<VerticalPosition> extraButtonsPosition;

    /**
     * Returns the ObjectProperty that controls the vertical position of extra buttons in the SelectionBox.
     * This property determines whether the extra buttons are positioned at the top or bottom relative to the main items.
     *
     * @return the ObjectProperty controlling the vertical position of extra buttons.
     */
    public final ObjectProperty<VerticalPosition> extraButtonsPositionProperty() {
        if (extraButtonsPosition == null) {
            extraButtonsPosition = new StyleableObjectProperty<>(DEFAULT_EXTRA_BUTTON_POSITION) {
                @Override
                public Object getBean() {
                    return SelectionBox.this;
                }

                @Override
                public String getName() {
                    return "extraButtonsPosition";
                }

                @Override
                public CssMetaData<? extends Styleable, VerticalPosition> getCssMetaData() {
                    return StyleableProperties.EXTRA_BUTTONS_POSITION;
                }
            };
        }
        return extraButtonsPosition;
    }

    public final VerticalPosition getExtraButtonsPosition() {
        return extraButtonsPosition == null ? DEFAULT_EXTRA_BUTTON_POSITION : extraButtonsPosition.get();
    }

    public final void setExtraButtonsPosition(VerticalPosition extraButtonsPosition) {
        extraButtonsPositionProperty().set(extraButtonsPosition);
    }

    // graphic

    private ObjectProperty<Node> graphic;

    /**
     * Returns the graphic property of this ChoicePicker. The graphic is an optional
     * graphical representation that can be displayed alongside other text or elements within
     * the picker.
     *
     * @return the ObjectProperty containing the graphical Node.
     */
    public final ObjectProperty<Node> graphicProperty() {
        if (graphic == null) {
            graphic = new SimpleObjectProperty<>(this, "graphic");
        }
        return graphic;
    }

    public final void setGraphic(Node graphic) {
        graphicProperty().set(graphic);
    }

    public final Node getGraphic() {
        return graphic == null ? null : graphic.get();
    }

    // autoHideOnSelection

    private BooleanProperty autoHideOnSelection;

    /**
     * Controls whether the popup should auto-hide when an item or the extra button is clicked.
     * <p>
     * When set to false, the popup remains visible regardless of interaction, allowing continued
     * interaction in both single and multiple selection modes. This is useful when the user needs
     * to make multiple selections or interact with the extra button without closing the popup.
     * <p>
     * When set to true, the behavior differs based on the selection mode:
     * <ul>
     *   <li>In multiple selection mode, clicking the extra button will hide the popup to finalize the selection,
     *       but clicking an item will not hide the popup, allowing further selections.</li>
     *   <li>In single selection mode, clicking any item or the extra button does not automatically hide the popup
     *       unless explicitly handled.</li>
     * </ul>
     * The default value is true.
     *
     * @return a BooleanProperty to control the auto-hide behavior of the popup.
     */
    public final BooleanProperty autoHideOnSelectionProperty() {
        if (autoHideOnSelection == null) {
            autoHideOnSelection = new SimpleBooleanProperty(this, "autoHideOnSelection", true);
        }
        return autoHideOnSelection;
    }

    public final boolean isAutoHideOnSelection() {
        return autoHideOnSelection == null || autoHideOnSelection.get();
    }

    public final void setAutoHideOnSelection(boolean autoHideOnSelection) {
        autoHideOnSelectionProperty().set(autoHideOnSelection);
    }

    // selectedItemsConverter (for the selected items, will be used in the label)

    private ObjectProperty<StringConverter<List<T>>> selectedItemsConverter;

    /**
     * Retrieves the property object for a string converter used to format the display of selected items.
     * This converter allows for customization of how selected items are represented in the UI.
     * For instance, if the selected item collection is {@code [1, 2, 3, 4, 5]}, the converter can format
     * this list to display as "1, 2, 3, 4, 5". By setting a custom converter, it is possible to modify
     * the display to any desired format, such as "1~5".
     * <p>
     * The {@code selectedItemsConverterProperty} provides a way to bind the display logic to UI components,
     * enabling dynamic updates whenever the selected items change or the converter is redefined.
     *
     * @return the property object for the string converter that formats the display of selected items.
     */
    public final ObjectProperty<StringConverter<List<T>>> selectedItemsConverterProperty() {
        if (selectedItemsConverter == null) {
            selectedItemsConverter = new SimpleObjectProperty<>(this, "selectedItemsConverter");
        }
        return selectedItemsConverter;
    }

    public final StringConverter<List<T>> getSelectedItemsConverter() {
        return selectedItemsConverter == null ? null : selectedItemsConverter.get();
    }

    public final void setSelectedItemsConverter(StringConverter<List<T>> selectedItemsConverter) {
        selectedItemsConverterProperty().set(selectedItemsConverter);
    }

    // itemConverter (for the items, will be used in the popup)

    private ObjectProperty<StringConverter<T>> itemConverter;

    /**
     * Retrieves the property object for the string converter that formats the display of individual items.
     * This property allows for customization of how each item is represented as a string in the UI.
     *
     * @return the property object for the string converter used to format individual items.
     */
    public final ObjectProperty<StringConverter<T>> itemConverterProperty() {
        if (itemConverter == null) {
            itemConverter = new SimpleObjectProperty<>(this, "itemConverter");
        }
        return itemConverter;
    }

    public final StringConverter<T> getItemConverter() {
        return itemConverter == null ? null : itemConverter.get();
    }

    public final void setItemConverter(StringConverter<T> itemConverter) {
        itemConverterProperty().set(itemConverter);
    }

    // readOnly

    private BooleanProperty readOnly;

    /**
     * Returns the BooleanProperty that controls the read-only state of the SelectionBox.
     * When set to true, the SelectionBox will be in read-only mode, preventing user interaction.
     * The default value is false.
     *
     * @return the BooleanProperty controlling the read-only state of the SelectionBox.
     */
    public final BooleanProperty readOnlyProperty() {
        if (readOnly == null) {
            readOnly = new StyleableBooleanProperty(DEFAULT_READ_ONLY) {

                @Override
                public Object getBean() {
                    return SelectionBox.this;
                }

                @Override
                public String getName() {
                    return "readOnly";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.READ_ONLY;
                }
            };
        }
        return readOnly;
    }

    public final boolean isReadOnly() {
        return readOnly == null ? DEFAULT_READ_ONLY : readOnly.get();
    }

    public final void setReadOnly(boolean readOnly) {
        readOnlyProperty().set(readOnly);
    }

    // showExtraButtons

    private BooleanProperty showExtraButtons;

    /**
     * Returns the BooleanProperty that determines whether extra buttons are shown in the SelectionBox.
     * This property can be styled via CSS.
     *
     * @return the BooleanProperty controlling the visibility of extra buttons.
     */
    public final BooleanProperty showExtraButtonsProperty() {
        if (showExtraButtons == null) {
            showExtraButtons = new StyleableBooleanProperty(DEFAULT_SHOW_EXTRA_BUTTON) {
                @Override
                public Object getBean() {
                    return SelectionBox.this;
                }

                @Override
                public String getName() {
                    return "showExtraButtons";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.SHOW_EXTRA_BUTTONS;
                }
            };
        }
        return showExtraButtons;
    }

    public final boolean getShowExtraButtons() {
        return showExtraButtons == null ? DEFAULT_SHOW_EXTRA_BUTTON : showExtraButtons.get();
    }

    public final void setShowExtraButtons(boolean showExtraButtons) {
        showExtraButtonsProperty().set(showExtraButtons);
    }

    // selectionModel

    private final ObjectProperty<MultipleSelectionModel<T>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel");

    public final MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel.get();
    }

    /**
     * Returns the property object for the selection model used by this SelectionBox.
     * The selection model defines the APIs responsible for handling selection, primarily
     * in the context of multiple or single selection modes.
     *
     * @return the ObjectProperty containing the selection model.
     */
    public final ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }

    public final void setSelectionModel(MultipleSelectionModel<T> selectionModel) {
        this.selectionModel.set(selectionModel);
    }

    private static class StyleableProperties {
        private static final CssMetaData<SelectionBox, Boolean> READ_ONLY = new CssMetaData<>("-fx-read-only", StyleConverter.getBooleanConverter(), DEFAULT_READ_ONLY) {

            @Override
            public boolean isSettable(SelectionBox styleable) {
                return styleable.readOnly == null || !styleable.readOnly.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(SelectionBox SelectionBox) {
                return (StyleableProperty<Boolean>) SelectionBox.readOnlyProperty();
            }
        };

        private static final CssMetaData<SelectionBox, Boolean> SHOW_EXTRA_BUTTONS = new CssMetaData<>("-fx-show-extra-buttons", StyleConverter.getBooleanConverter(), DEFAULT_SHOW_EXTRA_BUTTON) {
            @Override
            public boolean isSettable(SelectionBox styleable) {
                return styleable.showExtraButtons == null || !styleable.showExtraButtons.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(SelectionBox SelectionBox) {
                return (StyleableProperty<Boolean>) SelectionBox.showExtraButtonsProperty();
            }
        };

        private static final CssMetaData<SelectionBox, VerticalPosition> EXTRA_BUTTONS_POSITION = new CssMetaData<>("-fx-extra-buttons-position", StyleConverter.getEnumConverter(VerticalPosition.class), DEFAULT_EXTRA_BUTTON_POSITION) {
            @Override
            public boolean isSettable(SelectionBox styleable) {
                return styleable.extraButtonsPosition == null || !styleable.extraButtonsPosition.isBound();
            }

            @Override
            public StyleableProperty<VerticalPosition> getStyleableProperty(SelectionBox SelectionBox) {
                return (StyleableProperty<VerticalPosition>) SelectionBox.extraButtonsPositionProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, READ_ONLY, SHOW_EXTRA_BUTTONS, EXTRA_BUTTONS_POSITION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}