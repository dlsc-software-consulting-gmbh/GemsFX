package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.HistoryPopup;
import com.dlsc.gemsfx.util.HistoryManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A custom button that manages and displays a history of entries based on a generic type {@code T}.
 * This button integrates directly with a {@link HistoryManager} to provide a UI component that allows users
 * to interact with their input history through a popup.
 *
 * <p>The button can be configured to manage the focus behavior of the popup when it is opened,
 * enabling the popup owner to gain focus immediately. This functionality is useful for ensuring
 * that the popup behaves intuitively in various UI contexts.</p>
 *
 * <p>Additional customization includes enabling or disabling the history popup, and configuring
 * how the history items are displayed and managed within the popup. The button's style and behavior
 * can be extensively customized through CSS properties and dynamic property bindings.</p>
 *
 * <p>Usage scenarios include search fields, form inputs, or any other component where users might benefit
 * from being able to see and interact with their previous entries. The generic type {@code T} allows for
 * flexibility, making it suitable for various data types that can represent user input history.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 * <li>Direct integration with a {@link HistoryManager} to manage history data.</li>
 * <li>Customizable focus behavior on popup opening.</li>
 * <li>Support for CSS styling and dynamic properties to control visual and functional aspects.</li>
 * <li>Optional configuration for history management operations such as adding or removing items.</li>
 * </ul>
 *
 * @param <T> the type of the objects that this button manages in its history
 */
public class HistoryButton<T> extends Button {

    private static final String DEFAULT_STYLE_CLASS = "history-button";
    private static final boolean ENABLE_HISTORY_POPUP = true;
    private static final boolean DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN = false;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");
    private static final PseudoClass HISTORY_POPUP_SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("history-popup-showing");

    private final Region popupOwner;
    private final HistoryManager<T> historyManager;
    private HistoryPopup<T> historyPopup;

    /**
     * Creates a new instance of the history button.
     *
     * @param popupOwner     The owner of the popup. can be null. if null, the button will be the popup owner.
     * @param historyManager The history manager.
     */
    public HistoryButton(Region popupOwner, HistoryManager<T> historyManager) {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);
        this.popupOwner = popupOwner;
        this.historyManager = historyManager;

        setGraphic(new FontIcon(MaterialDesign.MDI_HISTORY));
        setOnAction(this::onActionHandler);
    }

    protected void onActionHandler(ActionEvent event) {
        Region popupOwner = getPopupOwner();
        HistoryManager<T> historyManager = getHistoryManager();

        if (popupOwner != null && popupOwner != this && !popupOwner.isFocused() && getFocusPopupOwnerOnOpen()) {
            popupOwner.requestFocus();
        }

        if (!isEnableHistoryPopup()) {
            return;
        }

        if (historyPopup == null) {
            historyPopup = new HistoryPopup<>(historyManager);
            // basic settings
            historyPopup.setHistoryCellFactory(view -> new RemovableListCell<>((listView, item) -> historyManager.remove(item)));
            historyPopupShowing.bind(historyPopup.showingProperty());

            // Set up the popup
            if (getConfigureHistoryPopup() != null) {
                getConfigureHistoryPopup().accept(historyPopup);
            }
        }
        if (historyPopup.isShowing()) {
            historyPopup.hide();
        } else {
            historyPopup.show(popupOwner == null ? this : popupOwner);
        }
    }

    private BooleanProperty focusPopupOwnerOnOpen;

    /**
     * Controls whether the Popup Owner should gain focus when the popup is displayed after a button click.
     * <p>
     * This property determines whether the Popup Owner, which is the reference component for the popup's position,
     * should receive focus when the popup is opened. If set to true, the Popup Owner will be focused
     * when the popup becomes visible. If set to false, the Popup Owner will retain its current focus state.
     * <p>
     * The default value is false.
     *
     * @return the BooleanProperty that enables or disables focus on the Popup Owner when the popup opens
     */
    public final BooleanProperty focusPopupOwnerOnOpenProperty() {
        if (focusPopupOwnerOnOpen == null) {
            focusPopupOwnerOnOpen = new StyleableBooleanProperty(DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN) {
                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "popupOwnerFocusOnClick";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.FOCUS_POPUP_OWNER_ON_OPEN;
                }
            };
        }
        return focusPopupOwnerOnOpen;
    }

    public final boolean getFocusPopupOwnerOnOpen() {
        return focusPopupOwnerOnOpen == null ? DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN : focusPopupOwnerOnOpen.get();
    }

    public final void setFocusPopupOwnerOnOpen(boolean focusPopupOwnerOnOpen) {
        focusPopupOwnerOnOpenProperty().set(focusPopupOwnerOnOpen);
    }

    private ObjectProperty<Consumer<HistoryPopup<T>>> configureHistoryPopup;

    public final Consumer<HistoryPopup<T>> getConfigureHistoryPopup() {
        return configureHistoryPopup == null ? null : configureHistoryPopup.get();
    }

    public final ObjectProperty<Consumer<HistoryPopup<T>>> configureHistoryPopupProperty() {
        if (configureHistoryPopup == null) {
            configureHistoryPopup = new SimpleObjectProperty<>(this, "configureHistoryPopup");
        }
        return configureHistoryPopup;
    }

    public final void setConfigureHistoryPopup(Consumer<HistoryPopup<T>> configureHistoryPopup) {
        configureHistoryPopupProperty().set(configureHistoryPopup);
    }

    private BooleanProperty enableHistoryPopup;

    /**
     * Indicates whether the history popup should be enabled.
     *
     * @return true if the history popup should be enabled, false otherwise
     */
    public final BooleanProperty enableHistoryPopupProperty() {
        if (enableHistoryPopup == null) {
            enableHistoryPopup = new SimpleBooleanProperty(this, "enableHistoryPopup", ENABLE_HISTORY_POPUP) {
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DISABLED_POPUP_PSEUDO_CLASS, !get());
                }
            };
        }
        return enableHistoryPopup;
    }

    public final boolean isEnableHistoryPopup() {
        return enableHistoryPopup == null ? ENABLE_HISTORY_POPUP : enableHistoryPopup.get();
    }

    public final void setEnableHistoryPopup(boolean enableHistoryPopup) {
        enableHistoryPopupProperty().set(enableHistoryPopup);
    }

    private final ReadOnlyBooleanWrapper historyPopupShowing = new ReadOnlyBooleanWrapper(this, "historyPopupShowing") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HISTORY_POPUP_SHOWING_PSEUDO_CLASS, get());
        }
    };

    public final boolean isHistoryPopupShowing() {
        return historyPopupShowing.get();
    }

    /**
     * Indicates whether the history popup is showing. This is a read-only property.
     *
     * @return true if the history popup is showing, false otherwise
     */
    public final ReadOnlyBooleanProperty historyPopupShowingProperty() {
        return historyPopupShowing.getReadOnlyProperty();
    }

    private static class StyleableProperties {

        private static final CssMetaData<HistoryButton, Boolean> FOCUS_POPUP_OWNER_ON_OPEN = new CssMetaData<>(
                "-fx-focus-popup-owner-on-open", BooleanConverter.getInstance(), DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(HistoryButton control) {
                return (StyleableProperty<Boolean>) control.focusPopupOwnerOnOpenProperty();
            }

            @Override
            public boolean isSettable(HistoryButton control) {
                return control.focusPopupOwnerOnOpen == null || !control.focusPopupOwnerOnOpen.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Button.getClassCssMetaData());
            styleables.add(FOCUS_POPUP_OWNER_ON_OPEN);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return HistoryButton.StyleableProperties.STYLEABLES;
    }

    /**
     * Returns the HistoryManager instance used by this control.
     *
     * @return the history manager
     */
    public final HistoryManager<T> getHistoryManager() {
        return historyManager;
    }

    /**
     * Returns the popup owner node.
     *
     * @return the popup owner node
     */
    public final Region getPopupOwner() {
        return popupOwner;
    }

    /**
     * Returns the history popup.
     */
    public final HistoryPopup<T> getHistoryPopup() {
        return historyPopup;
    }

    /**
     * Hides the history popup.
     */
    public final void hideHistoryPopup() {
        if (historyPopup != null) {
            historyPopup.hide();
        }
    }

    /**
     * Shows the history popup.
     */
    public final void showHistoryPopup() {
        if (historyPopup != null) {
            historyPopup.show(popupOwner == null ? this : popupOwner);
        }
    }

}
