package com.dlsc.gemsfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A list cell that displays a remove button on the right side. The remove button is only
 * visible when the mouse hovers over the cell. When the remove button is clicked, the
 * onRemove callback is invoked.
 *
 * @param <T> the type of the list cell item
 */
public class RemovableListCell<T> extends ListCell<T> {

    private final HBox containerBox;
    private final Label label;

    public RemovableListCell() {
        getStyleClass().add("removable-list-cell");

        label = new Label();

        StackPane removeBtn = new StackPane(new FontIcon(MaterialDesign.MDI_CLOSE));
        removeBtn.getStyleClass().add("remove-button");
        removeBtn.setOnMouseClicked(this::onRemoveAction);

        containerBox = new HBox(label, new Spacer(), removeBtn);
        containerBox.getStyleClass().add("container-box");
        containerBox.setAlignment(Pos.CENTER_LEFT);
    }

    public RemovableListCell(BiConsumer<ListView<T>, T> onRemove) {
        this();
        setOnRemove(onRemove);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            label.setText(null);

            setText(null);
            setGraphic(null);
        } else {
            label.setText(item.toString());

            setText(null);
            setGraphic(containerBox);
        }
    }

    public void onRemoveAction(MouseEvent event) {
        if (getOnRemove() != null) {

            // clear selection if the item is selected
            if (isSelected()) {
                getListView().getSelectionModel().clearSelection();
            }

            getOnRemove().accept(getListView(), getItem());
        }
    }

    private ObjectProperty<BiConsumer<ListView<T>, T>> onRemove;

    /**
     * A callback that is invoked when the remove button is clicked.
     *
     * @return the onRemoveProperty
     */
    public final ObjectProperty<BiConsumer<ListView<T>, T>> onRemoveProperty() {
        if (onRemove == null) {
            onRemove = new SimpleObjectProperty<>(this, "onRemove");
        }
        return onRemove;
    }

    public final BiConsumer<ListView<T>, T> getOnRemove() {
        return onRemove == null ? null : onRemoveProperty().get();
    }

    public final void setOnRemove(BiConsumer<ListView<T>, T> onRemove) {
        onRemoveProperty().set(onRemove);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(RemovableListCell.class.getResource("removable-list-cell.css")).toExternalForm();
    }
}


