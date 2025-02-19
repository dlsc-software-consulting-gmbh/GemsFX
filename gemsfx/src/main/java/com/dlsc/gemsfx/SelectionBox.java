package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SelectionBoxSkin;
import com.dlsc.gemsfx.util.CustomMultipleSelectionModel;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collection;
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
    private static final boolean DEFAULT_ANIMATION_ENABLED = false;

    /**
     * Constructs a new SelectionBox instance. This custom control extends functionality to allow
     * for enhanced selection capabilities with predefined style classes and quick selection buttons.
     *
     * The constructor performs the following operations:
     * - Sets style classes for the control, including "combo-box-base," "combo-box," and the default style class.
     * - Adds customizable quick selection buttons to the top of the popup area.
     * - Binds the current selection mode to an internal selection mode property.
     * - Initializes a custom multiple selection model, binding it to the items property.
     * - Configures the component's size to use its preferred size as both minimum and maximum size.
     */
    public SelectionBox() {
        getStyleClass().setAll("combo-box-base", "combo-box", DEFAULT_STYLE_CLASS);

        // Add quick selection buttons to the top of the popup
        setTop(createExtraButtonsBox());

        currentSelectionMode.bind(getCurrentSelectionModeBinding());

        // initialize the selection model
        CustomMultipleSelectionModel<T> model = new CustomMultipleSelectionModel<>();
        model.itemsProperty().bind(itemsProperty());
        setSelectionModel(model);

        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    /**
     * Constructs a new selection box with the specified collection of items.
     *
     * @param items the collection of items to populate the SelectionBox
     */
    public SelectionBox(Collection<T> items) {
        this();
        getItems().setAll(items);
    }

    /**
     * Constructs a new selection boxx and populates it with the provided items.
     *
     * @param items The initial items to populate the selection box.
     *              These items will be added to the selection box's list of choices.
     */
    @SafeVarargs
    public SelectionBox(T... items) {
        this();
        getItems().setAll(items);
    }

    private Node createExtraButtonsBox() {
        Button clearButton = createExtraButton("Clear", () -> getSelectionModel().clearSelection());
        clearButton.getStyleClass().add("clear-button");

        Button selectAllButton = createExtraButton("Select All", () -> getSelectionModel().selectAll());
        selectAllButton.managedProperty().bind(selectAllButton.visibleProperty());
        selectAllButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));
        selectAllButton.getStyleClass().add("select-all-button");

        VBox extraButtonsBox = new VBox(clearButton, selectAllButton);
        extraButtonsBox.getStyleClass().addAll("extra-buttons-box");
        extraButtonsBox.managedProperty().bind(extraButtonsBox.visibleProperty());
        extraButtonsBox.visibleProperty().bind(itemsProperty().emptyProperty().not());

        return extraButtonsBox;
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

    // top

    private final ObjectProperty<Node> top = new SimpleObjectProperty<>(this, "top");

    public final ObjectProperty<Node> topProperty() {
        return top;
    }

    public final Node getTop() {
        return topProperty().get();
    }

    public final void setTop(Node top) {
        topProperty().set(top);
    }

    // bottom

    private final ObjectProperty<Node> bottom = new SimpleObjectProperty<>(this, "bottom");

    public final ObjectProperty<Node> bottomProperty() {
        return bottom;
    }

    public final Node getBottom() {
        return bottomProperty().get();
    }

    public final void setBottom(Node bottom) {
        bottomProperty().set(bottom);
    }

    // left

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final Node getLeft() {
        return leftProperty().get();
    }

    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    // right

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    public final Node getRight() {
        return rightProperty().get();
    }

    public final void setRight(Node right) {
        rightProperty().set(right);
    }

    // placeholder

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder");

    /**
     * Returns the property holding the placeholder node, which is displayed when there are no items.
     *
     * @return the placeholder property
     */
    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final Node getPlaceholder() {
        return placeholderProperty().get();
    }

    public final void setPlaceholder(Node placeholder) {
        placeholderProperty().set(placeholder);
    }

    /**
     * Creates an extra button with the specified text and action.
     * The button will be styled with the "extra-button" class and will adjust its visibility
     * based on its managed property. The button's maximum width will be set to the maximum double value.
     *
     * @param text   the text to be displayed on the button
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
     * @param text    the text to be displayed on the button
     * @param graphic the graphic node to be displayed on the button
     * @param action  the action to be executed when the button is clicked
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
            if (isAutoHideOnSelection()) {
                hide();
            }
        });
        return button;
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

    // promptText

    private StringProperty promptText;

    /**
     * Returns the prompt text property of this SelectionBox. The prompt text is an optional
     * text that can be displayed in the picker when no item is selected.
     *
     * @return the StringProperty containing the prompt text.
     */
    public final StringProperty promptTextProperty() {
        if (promptText == null) {
            promptText = new SimpleStringProperty(this, "promptText");
        }
        return promptText;
    }

    public final void setPromptText(String promptText) {
        promptTextProperty().set(promptText);
    }

    public final String getPromptText() {
        return promptText == null ? null : promptText.get();
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
     * If the selected items list is {@code null} or empty, the {@code promptTextProperty()} value will
     * be used instead, so there is no need to handle these cases within the converter.
     * You can also choose to always return your own string (for example, "Select" or "Please choose")
     * <pre>{@code
     * selectionBox.setSelectedItemsConverter(
     *     new SimpleStringConverter<>(selectedItems -> {
     *         // return "Select";
     *         return selectionBox.getPromptText();
     *     })
     * );
     * }</pre>
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

    // currentSelectionMode (read-only)

    private final ReadOnlyObjectWrapper<SelectionMode> currentSelectionMode = new ReadOnlyObjectWrapper<>(this, "currentSelectionMode");

    public final SelectionMode getCurrentSelectionMode() {
        return currentSelectionMode.get();
    }

    /**
     * Provides a read-only property that directly exposes the current
     * {@link SelectionMode} of this control without requiring multilevel checks
     * on the underlying {@link javafx.scene.control.MultipleSelectionModel}.
     * This is particularly convenient for child classes or external consumers
     * who need to quickly determine whether the mode is
     * {@link SelectionMode#SINGLE}, {@link SelectionMode#MULTIPLE}, or {@code null}
     * (in case there is no active selection model).
     * <p>
     * To modify the selection mode, call
     * {@code getSelectionModel().setSelectionMode(...)} directly,
     * since this property itself is read-only.
     *
     * @return a read-only {@link SelectionMode} property
     */
    public final ReadOnlyObjectProperty<SelectionMode> currentSelectionModeProperty() {
        return currentSelectionMode.getReadOnlyProperty();
    }

    /**
     * Creates and returns an {@code ObjectBinding} that observes changes to the
     * {@code selectionModelProperty()} and its associated {@code selectionModeProperty()}.
     * This binding dynamically updates its value to the current {@code SelectionMode}
     * of the {@code MultipleSelectionModel} associated with the {@code SelectionBox}.
     * <p>
     * The binding ensures that it properly listens to changes in the selection model
     * and updates accordingly when the selection mode changes, even when the
     * selection model is replaced.
     *
     * @return an {@code ObjectBinding} that provides the current {@code SelectionMode}
     * of the {@code MultipleSelectionModel}, or {@code null} if no
     * selection model is set
     */
    private ObjectBinding<SelectionMode> getCurrentSelectionModeBinding() {
        return new ObjectBinding<>() {
            // Listener for selectionModeProperty
            private final InvalidationListener selectionModeInvalidationListener = obs -> invalidate();
            // Stores the old selectionModel
            private MultipleSelectionModel<T> oldSelectionModel;

            {
                // Listen to selectionModelProperty changes
                selectionModelProperty().addListener((observable, oldValue, newValue) -> {
                    // Remove old listener if exists
                    if (oldSelectionModel != null) {
                        oldSelectionModel.selectionModeProperty().removeListener(selectionModeInvalidationListener);
                    }
                    // Add listener to the new model if not null
                    if (newValue != null) {
                        newValue.selectionModeProperty().addListener(selectionModeInvalidationListener);
                    }
                    // Update the reference
                    oldSelectionModel = newValue;
                    // Force recalculation
                    invalidate();
                });

                // Add listener if there's already a selectionModel
                if (getSelectionModel() != null) {
                    oldSelectionModel = getSelectionModel();
                    oldSelectionModel.selectionModeProperty().addListener(selectionModeInvalidationListener);
                }

                // Bind to outer property to trigger computeValue
                bind(selectionModelProperty());
            }

            @Override
            protected SelectionMode computeValue() {
                // Return null if selectionModel is null
                MultipleSelectionModel<T> sm = getSelectionModel();
                return (sm == null) ? null : sm.getSelectionMode();
            }
        };
    }

    // animationEnabled

    private BooleanProperty animationEnabled;

    public final BooleanProperty animationEnabledProperty() {
        if (animationEnabled == null) {
            animationEnabled = new StyleableBooleanProperty(DEFAULT_ANIMATION_ENABLED) {
                @Override
                public Object getBean() {
                    return SelectionBox.this;
                }

                @Override
                public String getName() {
                    return "animationEnabled";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.ANIMATION_ENABLED;
                }
            };
        }
        return animationEnabled;
    }

    public final boolean isAnimationEnabled() {
        return animationEnabled == null ? DEFAULT_ANIMATION_ENABLED : animationEnabled.get();
    }

    public final void setAnimationEnabled(boolean value) {
        animationEnabledProperty().set(value);
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

        private static final CssMetaData<SelectionBox, Boolean> ANIMATION_ENABLED = new CssMetaData<>("-fx-animation-enabled", StyleConverter.getBooleanConverter(), DEFAULT_ANIMATION_ENABLED) {
            @Override
            public boolean isSettable(SelectionBox node) {
                return node.animationEnabled == null || !node.animationEnabled.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(SelectionBox node) {
                return (StyleableProperty<Boolean>) node.animationEnabledProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, READ_ONLY, ANIMATION_ENABLED);
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