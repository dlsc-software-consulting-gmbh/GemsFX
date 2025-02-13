package com.dlsc.gemsfx;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.FlowPane;

import java.util.Objects;

/**
 * A container used for displaying several {@link ChipView} instances. This view is usually used in combination
 * with a parent filter view (e.g. {@link SimpleFilterView}). The view also manages a hyperlink for clearing the
 * current selection settings iin the parent view. The view manages its own visibility based on whether there are
 * any chip views to show or not.
 *
 * @see #chipsProperty()
 * @see SimpleFilterView#chipsProperty()
 */
public class ChipsViewContainer extends FlowPane {

    /**
     * Constructs a new chips view container.
     */
    public ChipsViewContainer() {
        getStyleClass().add("chips-view-container");

        chips.addListener((Observable it) -> updateChips());

        visibleProperty().bind(chips.emptyProperty().not());
        managedProperty().bind(visibleProperty());
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(ChipsViewContainer.class.getResource("chips-view-container.css")).toExternalForm();
    }

    private final ListProperty<ChipView<?>> chips = new SimpleListProperty<>(FXCollections.observableArrayList());

    public final ObservableList<ChipView<?>> getChips() {
        return chips.get();
    }

    /**
     * The list of chip views that will be shown by the view.
     *
     * @return the chip views
     */
    public final ListProperty<ChipView<?>> chipsProperty() {
        return chips;
    }

    public final void setChips(ObservableList<ChipView<?>> chips) {
        this.chips.set(chips);
    }

    // clearing callback

    public final ObjectProperty<Runnable> onClear = new SimpleObjectProperty<>(this, "onClear");

    public final Runnable getOnClear() {
        return onClear.get();
    }

    /**
     * A callback that will be invoked when the user clicks on the "clear" hyperlink.
     *
     * @see #clearTextProperty()
     *
     * @return the "on clear" callback
     */
    public final ObjectProperty<Runnable> onClearProperty() {
        return onClear;
    }

    public final void setOnClear(Runnable onClear) {
        this.onClear.set(onClear);
    }

    // clear hyperlink text

    private final StringProperty clearText = new SimpleStringProperty(this, "clearText", "Clear");

    public final String getClearText() {
        return clearText.get();
    }

    /**
     * A property storing the text for the hyperlink that is being used to clear the view.
     *
     * @see #onClearProperty()
     * @return the text property used for the text of the hyperlink used to clear the view
     */
    public final StringProperty clearTextProperty() {
        return clearText;
    }

    public final void setClearText(String clearText) {
        this.clearText.set(clearText);
    }

    private void updateChips() {
        getChildren().clear();

        getChips().forEach(c -> getChildren().add(c));

        if (!getChildren().isEmpty()) {
            Hyperlink clear = new Hyperlink();
            clear.textProperty().bind(clearTextProperty());
            clear.visibleProperty().bind(onClear.isNotNull());
            clear.managedProperty().bind(onClear.isNotNull());
            clear.setOnAction(e -> getOnClear().run());
            getChildren().add(clear);
        }
    }
}
