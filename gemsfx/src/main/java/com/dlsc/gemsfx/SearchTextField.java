package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SearchTextFieldHistoryPopup;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class SearchTextField extends CustomTextField {

    private static final int DEFAULT_MAX_HISTORY_SIZE = 30;
    private static final boolean DEFAULT_ENABLE_HISTORY_POPUP = true;
    private static final Callback<ListView<String>, ListCell<String>> DEFAULT_CELL_FACTORY = param -> new SimpleHistoryListCell();
    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");

    private final Logger LOG = Logger.getLogger(SearchTextField.class.getName());
    private SearchTextFieldHistoryPopup historyPopup;
    private StackPane searchIconWrapper;

    public SearchTextField() {
        this(false);
    }

    public SearchTextField(boolean round) {
        if (round) {
            getStyleClass().add("round");
        }

        getStyleClass().add("search-text-field");

        setPromptText("Search...");
        setHistoryPlaceholder(new Label("No history available."));
        setEnableHistoryPopup(false);

        searchIconWrapper = createLeftNode();
        setLeft(searchIconWrapper);

        StackPane clearIconWrapper = createRightNode();
        setRight(clearIconWrapper);

        registerHandlersAndListeners();
    }

    private void registerHandlersAndListeners() {
        focusedProperty().addListener(it -> hideHistoryPopup());

        maxHistorySizeProperty().addListener(it -> {
            if (getMaxHistorySize() < 0) {
                LOG.warning("Max history size must be greater than or equal to 0. ");
            }

            if (history.size() > getSafetyMaxHistorySize()) {
                history.remove(getSafetyMaxHistorySize(), history.size());
            }
        });

        // On Action event, add the text to the history
        addEventHandler(ActionEvent.ANY, e -> addHistory(getText()));

        // On mouse pressed, hide the history popup
        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            EventTarget target = e.getTarget();
            boolean clickHistoryButton = (target instanceof Node && searchIconWrapper.getChildren().contains(target)) || searchIconWrapper.equals(target);
            if (!clickHistoryButton) {
                hideHistoryPopup();
            }
        });

        addEventHandler(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                e.consume();
            } else {
                hideHistoryPopup();
            }
        });
    }

    private StackPane createRightNode() {
        FontIcon clearIcon = new FontIcon(MaterialDesign.MDI_CLOSE);
        clearIcon.getStyleClass().add("clear-icon");
        clearIcon.setCursor(Cursor.DEFAULT);
        clearIcon.setOnMouseClicked(evt -> setText(""));
        clearIcon.visibleProperty().bind(textProperty().isNotEmpty());
        clearIcon.managedProperty().bind(textProperty().isNotEmpty());

        StackPane clearIconWrapper = new StackPane(clearIcon);
        clearIconWrapper.getStyleClass().addAll("wrapper", "clear-icon-wrapper");
        return clearIconWrapper;
    }

    private StackPane createLeftNode() {
        FontIcon searchIcon = new FontIcon(MaterialDesign.MDI_MAGNIFY);
        searchIcon.getStyleClass().add("search-icon");

        StackPane searchIconWrapper = new StackPane(searchIcon);
        searchIconWrapper.getStyleClass().addAll("wrapper", "search-icon-wrapper");
        searchIconWrapper.setOnMouseClicked(this::clickIconWrapperHandler);
        return searchIconWrapper;
    }

    private void clickIconWrapperHandler(MouseEvent event) {

        if (!isFocused()) {
            requestFocus();
        }

        if (event.getButton() != MouseButton.PRIMARY || !isEnableHistoryPopup()) {
            return;
        }

        if (historyPopup == null) {
            historyPopup = new SearchTextFieldHistoryPopup(this);
        }

        if (historyPopup.isShowing()) {
            historyPopup.hide();
        } else {
            historyPopup.show(this);
        }

        positionCaret(textProperty().getValueSafe().length());
    }

    private void hideHistoryPopup() {
        if (historyPopup != null && historyPopup.isShowing()) {
            historyPopup.hide();
        }
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SearchTextField.class.getResource("search-text-field.css")).toExternalForm();
    }

    private final ObservableList<String> history =FXCollections.observableArrayList();

    public final void setHistory(List<String> history) {
        this.history.setAll(convertToUniqueList(history));
    }

    public final void addHistory(String item) {
        if (StringUtils.isNotEmpty(item)) {
            history.remove(item);
            history.add(0, item);
        }
        if (history.size() > getSafetyMaxHistorySize()) {
            history.remove(getSafetyMaxHistorySize(),history.size());
        }
    }

    public final void addHistory(List<String> items) {
        List<String> uniqueItems = convertToUniqueList(items);
        for (String item : uniqueItems) {
            addHistory(item);
        }
    }

    public final void removeHistory(String item) {
        history.remove(item);
    }

    public final void removeHistory(List<String> items) {
        history.removeAll(items);
    }

    public final void clearHistory() {
        history.clear();
    }

    private final ObservableList<String> unmodifiableHistory = FXCollections.unmodifiableObservableList(history);

    public final ObservableList<String> getUnmodifiableHistory() {
        return unmodifiableHistory;
    }

    private IntegerProperty maxHistorySize;

    public final IntegerProperty maxHistorySizeProperty() {
        if (maxHistorySize == null) {
            maxHistorySize = new SimpleIntegerProperty(this, "maxHistorySize", DEFAULT_MAX_HISTORY_SIZE);
        }
        return maxHistorySize;
    }

    public final int getMaxHistorySize() {
        return maxHistorySize == null ? DEFAULT_MAX_HISTORY_SIZE : maxHistorySize.get();
    }

    public final void setMaxHistorySize(int maxHistorySize) {
        maxHistorySizeProperty().set(maxHistorySize);
    }
    
    private ObjectProperty<Node> historyPlaceholder = new SimpleObjectProperty<>(this, "historyPlaceholder");

    public final ObjectProperty<Node> historyPlaceholderProperty() {
        if (historyPlaceholder == null) {
            historyPlaceholder = new SimpleObjectProperty<>(this, "historyPlaceholder");
        }
        return historyPlaceholder;
    }

    public final Node getHistoryPlaceholder() {
        return historyPlaceholder == null ? null : historyPlaceholder.get();
    }

    public final void setHistoryPlaceholder(Node historyPlaceholder) {
        historyPlaceholderProperty().set(historyPlaceholder);
    }

    private ObjectProperty<Callback<ListView<String>, ListCell<String>>> cellFactory;

    public final Callback<ListView<String>, ListCell<String>> getCellFactory() {
        return cellFactory == null ? DEFAULT_CELL_FACTORY : cellFactory.get();
    }

    public final ObjectProperty<Callback<ListView<String>, ListCell<String>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory", DEFAULT_CELL_FACTORY);
        }
        return cellFactory;
    }

    public final void setCellFactory(Callback<ListView<String>, ListCell<String>> cellFactory) {
        cellFactoryProperty().set(cellFactory);
    }

    private BooleanProperty enableHistoryPopup;

    public final BooleanProperty enableHistoryPopupProperty() {
        if (enableHistoryPopup == null) {
            enableHistoryPopup = new SimpleBooleanProperty(this, "enableHistoryPopup", DEFAULT_ENABLE_HISTORY_POPUP){
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DISABLED_POPUP_PSEUDO_CLASS, !get());
                }
            };
        }
        return enableHistoryPopup;
    }

    public final boolean isEnableHistoryPopup() {
        return enableHistoryPopup == null ? DEFAULT_ENABLE_HISTORY_POPUP : enableHistoryPopup.get();
    }

    public final void setEnableHistoryPopup(boolean enableHistoryPopup) {
        enableHistoryPopupProperty().set(enableHistoryPopup);
    }

    private static class SimpleHistoryListCell extends ListCell<String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null && empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
            }
        }
    }

    private List<String> convertToUniqueList(List<String> history) {
        return history.stream().distinct().filter(StringUtils::isNotEmpty).limit(getSafetyMaxHistorySize()).toList();
    }

    public final int getSafetyMaxHistorySize() {
        return Math.max(0, getMaxHistorySize());
    }

}
