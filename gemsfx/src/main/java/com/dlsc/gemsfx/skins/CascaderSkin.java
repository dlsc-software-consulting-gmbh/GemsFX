package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.Cascader;
import com.dlsc.gemsfx.CascaderItem;
import com.dlsc.gemsfx.CascaderPath;
import com.dlsc.gemsfx.CascaderView;
import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.internal.CascaderText;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Default skin for {@link Cascader}.
 *
 * <p>This skin is not an application extension point: the constructor needs the
 * control's private embedded view, which only {@code Cascader.createDefaultSkin()}
 * can supply, so application code can neither construct a working instance nor
 * replace it via CSS {@code -fx-skin}. Customize rendering through
 * {@link Cascader#cellFactoryProperty() cellFactory} and
 * {@link Cascader#pathTextFactoryProperty() pathTextFactory} instead.
 *
 * @param <T> application value type
 */
public class CascaderSkin<T> extends GemsSkinBase<Cascader<T>> {

    // ==================== Constants ====================

    private static final double DEFAULT_PREF_WIDTH = 220.0;
    private static final double DEFAULT_PREF_HEIGHT = 34.0;
    private static final PseudoClass EMPTY = PseudoClass.getPseudoClass("empty");
    private static final PseudoClass SHOWING = PseudoClass.getPseudoClass("showing");

    // ==================== Nodes ====================

    private final HBox display = new HBox();
    private final Label textLabel = new Label();
    private final StackPane clearButton = new StackPane();
    private final Region clearGraphic = new Region();
    private final StackPane arrowButton = new StackPane();
    private final Region arrow = new Region();

    // ==================== State ====================

    /** The embedded view used as popup content, injected by the control. */
    private final CascaderView<T> view;

    private final EventHandler<WindowEvent> popupHiddenHandler = event -> onPopupHidden();
    /** Popup shell hosting {@link #view}. */
    private final CascaderPopup popup = new CascaderPopup();

    private boolean suppressReopen;

    /** Items of the currently displayed path(s) whose value the field text mirrors. */
    private final List<CascaderItem<T>> observedPathItems = new ArrayList<>();
    // Application items can outlive the skin (a cached option tree reused across
    // dialogs); observe their value weakly so a discarded, never-disposed skin stays
    // collectible. The strong delegate is kept as a field so the weak listener holds.
    private final InvalidationListener pathValueListener = observable -> updateDisplay();
    private final WeakInvalidationListener weakPathValueListener =
            new WeakInvalidationListener(pathValueListener);

    // ==================== Constructor ====================

    /**
     * Creates a skin for the given cascader. Intended to be created by the
     * control's {@code createDefaultSkin()}: {@code view} must be that control's
     * own embedded popup view, otherwise the popup is wired to a foreign view and
     * will not reflect the control's state.
     *
     * @param control the skinnable cascader
     * @param view    the control's embedded view to host as popup content
     */
    public CascaderSkin(Cascader<T> control, CascaderView<T> view) {
        super(control);
        this.view = view;
        initializeNodes(control);
        registerListeners(control);
        getChildren().setAll(display);
        // Observe the value of any selection that already existed before this skin
        // was created (skins are created lazily, after a select() can have run).
        rebindPathValueListeners();
        updateDisplay();
        syncPopupShowing();
    }

    private void initializeNodes(Cascader<T> control) {
        display.getStyleClass().add("display");
        textLabel.setMaxWidth(Double.MAX_VALUE);

        clearGraphic.getStyleClass().add("graphic");
        clearGraphic.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        clearGraphic.setMouseTransparent(true);
        clearButton.getStyleClass().add("clear-button");
        clearButton.getChildren().add(clearGraphic);

        arrow.getStyleClass().add("arrow");
        arrow.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        arrow.setMouseTransparent(true);
        arrowButton.getStyleClass().add("arrow-button");
        arrowButton.getChildren().add(arrow);

        HBox.setHgrow(textLabel, Priority.ALWAYS);
        display.getChildren().setAll(textLabel, clearButton, arrowButton);

        registerHandler(display, MouseEvent.MOUSE_CLICKED, event -> handleDisplayClicked(control, event));
        registerHandler(clearButton, MouseEvent.MOUSE_CLICKED, event -> handleClearClicked(control, event));
        registerHandler(control, KeyEvent.KEY_PRESSED, event -> handleKeyPressed(control, event));
    }

    private void registerListeners(Cascader<T> control) {
        register(control.showingProperty(), observable -> syncPopupShowing());
        register(control.disabledProperty(), observable -> {
            if (control.isDisabled()) {
                control.hide();
            }
        });
        register(control.selectedPathProperty(), observable -> handleSelectionChanged(control));
        register(control.getCheckedPaths(), (ListChangeListener<CascaderPath<T>>) change -> onSelectionItemsChanged());
        register(control.selectionModeProperty(), observable -> onSelectionItemsChanged());
        register(control.promptTextProperty(), observable -> updateDisplay());
        register(control.pathTextFactoryProperty(), observable -> updateDisplay());
        register(control.converterProperty(), observable -> updateDisplay());
        register(control.separatorProperty(), observable -> updateDisplay());
        register(control.showAllLevelsProperty(), observable -> updateDisplay());
        register(control.clearableProperty(), observable -> updateDisplay());
    }

    // ==================== Events ====================

    private void handleDisplayClicked(Cascader<T> control, MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || control.isDisabled()) {
            return;
        }
        control.requestFocus();
        if (control.isShowing()) {
            control.hide();
        } else if (!suppressReopen) {
            // Guard against the auto-hide/reopen race: a press on the display
            // can auto-hide the popup before this click runs, which would
            // otherwise immediately reopen it.
            control.show();
        }
        event.consume();
    }

    private void handleClearClicked(Cascader<T> control, MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || !control.isClearable()) {
            return;
        }
        control.clearSelection();
        control.requestFocus();
        event.consume();
    }

    private void handleKeyPressed(Cascader<T> control, KeyEvent event) {
        if (control.isDisabled()) {
            return;
        }
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE) {
            // Only consume Escape when there is a popup to close; otherwise let it
            // bubble so an enclosing dialog / cancel button still sees it.
            if (control.isShowing()) {
                control.hide();
                event.consume();
            }
        } else if (isPopupToggleKey(code, event)) {
            // Dedicated popup toggle keys, always consuming: F4 and Alt+Up/Down
            // match ComboBox, and Space follows the common dropdown convention.
            togglePopup(control);
            event.consume();
        } else if (control.isShowing() && isColumnNavigationKey(code, event)) {
            // The popup never takes key focus, so column navigation is routed from
            // here into the view skin: arrows / Home / End move the keyboard focus,
            // Enter activates the focused item. An Enter with no keyboard focus
            // falls back to the historical close-the-popup semantics below.
            if (navigateColumns(event)) {
                event.consume();
            } else if (code == KeyCode.ENTER) {
                control.hide();
                event.consume();
            }
            // Note: plain arrows on a CLOSED cascader deliberately do nothing
            // (pinned behavior) — they stay available for form focus traversal;
            // the dedicated openers are Space / F4 / Alt+Up / Alt+Down.
        } else if (code == KeyCode.ENTER) {
            // Enter mirrors ComboBox: it closes an open popup (consuming, so the same
            // keystroke does not also fire an enclosing dialog's default button —
            // ComboBox's popup content consumes it for the same reason), but when the
            // popup is closed it neither opens it nor consumes, letting Enter reach a
            // default / submit button of an enclosing form.
            if (control.isShowing()) {
                control.hide();
                event.consume();
            }
        }
    }

    private static boolean hasNoModifiers(KeyEvent event) {
        return !event.isShiftDown() && !event.isControlDown()
                && !event.isAltDown() && !event.isMetaDown();
    }

    private static boolean isColumnNavigationKey(KeyCode code, KeyEvent event) {
        if (!hasNoModifiers(event)) {
            return false;
        }
        switch (code) {
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
            case HOME:
            case END:
            case ENTER:
                return true;
            default:
                return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean navigateColumns(KeyEvent event) {
        Skin<?> skin = view.getSkin();
        return skin instanceof CascaderViewSkin
                && ((CascaderViewSkin<T>) skin).handleNavigationKey(event);
    }

    @SuppressWarnings("unchecked")
    private void clearViewKeyboardFocus() {
        Skin<?> skin = view.getSkin();
        if (skin instanceof CascaderViewSkin) {
            ((CascaderViewSkin<T>) skin).clearKeyboardFocus();
        }
    }

    private static boolean isPopupToggleKey(KeyCode code, KeyEvent event) {
        // Match ComboBox's KeyBinding specificity exactly: bare Space / F4 (no other
        // modifiers), and Alt+Up/Down with Alt only. Being stricter than "code plus
        // altDown" avoids hijacking OS/app combos such as Alt+F4 (close window),
        // Ctrl+Space (IME), or Shift+Alt+Down.
        boolean noModifiers = hasNoModifiers(event);
        boolean altOnly = event.isAltDown() && !event.isShiftDown()
                && !event.isControlDown() && !event.isMetaDown();
        if (code == KeyCode.SPACE || code == KeyCode.F4) {
            return noModifiers;
        }
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            return altOnly;
        }
        return false;
    }

    private void togglePopup(Cascader<T> control) {
        if (control.isShowing()) {
            control.hide();
        } else {
            control.show();
        }
    }

    /**
     * Driven by the popup on every hide path: pull the control's showing state
     * back and arm the reopen guard for the current pulse.
     */
    private void onPopupHidden() {
        getSkinnable().hide();
        suppressReopen = true;
        Platform.runLater(() -> suppressReopen = false);
    }

    private void handleSelectionChanged(Cascader<T> control) {
        rebindPathValueListeners();
        updateDisplay();
        if (control.getSelectionMode() != SelectionMode.MULTIPLE
                && control.getSelectedPath() != null) {
            control.hide();
        }
    }

    private void onSelectionItemsChanged() {
        rebindPathValueListeners();
        updateDisplay();
    }

    /**
     * Rebinds the field's value listeners to the items of the currently displayed
     * path(s) — the selected path in single mode, or every checked path's items in
     * multiple mode — so changing a displayed item's value refreshes the field.
     */
    private void rebindPathValueListeners() {
        clearPathValueListeners();
        Cascader<T> control = getSkinnable();
        if (control.getSelectionMode() == SelectionMode.MULTIPLE) {
            for (CascaderPath<T> path : control.getCheckedPaths()) {
                for (CascaderItem<T> item : path.getItems()) {
                    if (!observedPathItems.contains(item)) {
                        observedPathItems.add(item);
                    }
                }
            }
        } else {
            CascaderPath<T> selected = control.getSelectedPath();
            if (selected != null) {
                observedPathItems.addAll(selected.getItems());
            }
        }
        for (CascaderItem<T> item : observedPathItems) {
            item.valueProperty().addListener(weakPathValueListener);
        }
    }

    private void clearPathValueListeners() {
        for (CascaderItem<T> item : observedPathItems) {
            item.valueProperty().removeListener(weakPathValueListener);
        }
        observedPathItems.clear();
    }

    // ==================== Display ====================

    private void updateDisplay() {
        Cascader<T> control = getSkinnable();
        boolean hasSelection = hasSelection(control);
        String displayText = hasSelection ? selectedText(control) : promptText(control);

        textLabel.setText(displayText);
        clearButton.setVisible(control.isClearable() && hasSelection);
        clearButton.setManaged(control.isClearable() && hasSelection);

        control.pseudoClassStateChanged(EMPTY, !hasSelection);
        control.pseudoClassStateChanged(SHOWING, control.isShowing());
        textLabel.pseudoClassStateChanged(EMPTY, !hasSelection);
    }

    private boolean hasSelection(Cascader<T> control) {
        if (control.getSelectionMode() == SelectionMode.MULTIPLE) {
            return !control.getCheckedPaths().isEmpty();
        }
        return control.getSelectedPath() != null;
    }

    private String selectedText(Cascader<T> control) {
        if (control.getSelectionMode() == SelectionMode.MULTIPLE) {
            StringJoiner joiner = new StringJoiner(", ");
            for (CascaderPath<T> path : control.getCheckedPaths()) {
                joiner.add(formatPath(control, path));
            }
            return joiner.toString();
        }
        return formatPath(control, control.getSelectedPath());
    }

    private String promptText(Cascader<T> control) {
        String promptText = control.getPromptText();
        return promptText == null ? "" : promptText;
    }

    private String formatPath(Cascader<T> control, CascaderPath<T> path) {
        if (path == null) {
            return "";
        }
        Function<CascaderPath<T>, String> factory = control.getPathTextFactory();
        String text = factory == null
                ? defaultPathText(control, path)
                : factory.apply(path);
        return text == null ? "" : text;
    }

    private String defaultPathText(Cascader<T> control, CascaderPath<T> path) {
        StringConverter<T> converter = control.getConverter();
        if (!control.isShowAllLevels()) {
            CascaderItem<T> leaf = path.getLeaf();
            return leaf == null ? "" : CascaderText.resolve(converter, leaf.getValue());
        }
        String separator = control.getSeparator();
        StringJoiner joiner = new StringJoiner(separator == null ? Cascader.DEFAULT_SEPARATOR : separator);
        for (CascaderItem<T> item : path.getItems()) {
            joiner.add(CascaderText.resolve(converter, item.getValue()));
        }
        return joiner.toString();
    }

    // ==================== Popup ====================

    private void syncPopupShowing() {
        updateDisplay();
        // Each popup session starts without a stale keyboard highlight; the first
        // arrow key seeds it (on the revealed selection when one exists).
        clearViewKeyboardFocus();
        if (getSkinnable().isShowing()) {
            // Reveal the current selection before showing so the popup opens already
            // expanded to (and highlighting) the selected path, at the right size.
            view.revealSelectedPath();
            if (getSkinnable().getScene() == null) {
                getSkinnable().hide();
            } else {
                popup.show(getSkinnable());
            }
        } else {
            popup.hide();
        }
    }

    @Override
    public void dispose() {
        clearPathValueListeners();
        popup.hide();
        popup.removeEventHandler(WindowEvent.WINDOW_HIDDEN, popupHiddenHandler);
        popup.setSkin(null);
        super.dispose();
    }

    private final class CascaderPopup extends CustomPopupControl {

        private CascaderPopup() {
            getStyleClass().add("cascader-popup");
            setAutoFix(true);
            setAutoHide(true);
            setHideOnEscape(true);
            setConsumeAutoHidingEvents(true);
            setSkin(new CascaderPopupSkin(this));
            addEventHandler(WindowEvent.WINDOW_HIDDEN, popupHiddenHandler);
        }

        @Override
        public void show(Node node) {
            getScene().setNodeOrientation(node.getEffectiveNodeOrientation());
            view.applyCss();
            view.autosize();
            super.show(node);
            view.applyCss();
            view.layout();
        }

    }

    private final class CascaderPopupSkin implements Skin<PopupControl> {

        private final PopupControl popup;

        private CascaderPopupSkin(PopupControl popup) {
            this.popup = popup;
        }

        @Override
        public PopupControl getSkinnable() {
            return popup;
        }

        @Override
        public Node getNode() {
            return view;
        }

        @Override
        public void dispose() {
        }
    }

    // ==================== Layout ====================

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        display.resizeRelocate(x, y, Math.max(0.0, w), Math.max(0.0, h));
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return leftInset + Math.max(DEFAULT_PREF_WIDTH, display.prefWidth(height)) + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        return topInset + Math.max(DEFAULT_PREF_HEIGHT, display.prefHeight(width)) + bottomInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset,
                                     double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }
}
