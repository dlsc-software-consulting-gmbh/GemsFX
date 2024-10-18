package com.dlsc.gemsfx.demo.fake;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * A simple control pane that displays a list of controls with a title above each control.
 * <p>
 * This component is currently only used in the demo module to control the properties of the component.
 */
public class SimpleControlPane extends Control {

    public record ControlItem(String title, Region control, boolean withBorder) {
        public ControlItem(String title, Region control) {
            this(title, control, true);
        }
    }

    public SimpleControlPane(String title, ControlItem... items) {
        getStyleClass().add("simple-control-pane");
        getItems().addAll(items);
        setTitle(title);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SimpleControlPaneSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SimpleControlPane.class.getResource("simple-control-pane.css")).toExternalForm();
    }

    // items

    private final ListProperty<ControlItem> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public ListProperty<ControlItem> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList<ControlItem> items) {
        itemsProperty().set(items);
    }

    public final ObservableList<ControlItem> getItems() {
        return items.get();
    }

    // title

    private final StringProperty title = new SimpleStringProperty(this, "title");

    public final void setTitle(String title) {
        titleProperty().set(title);
    }

    public final String getTitle() {
        return title == null ? null : title.get();
    }

    public final StringProperty titleProperty() {
        return title;
    }

    public static class SimpleControlPaneSkin extends SkinBase<SimpleControlPane> {

        private final VBox cellsBox;

        public SimpleControlPaneSkin(SimpleControlPane control) {
            super(control);

            Label titleLabel = new Label();
            titleLabel.getStyleClass().add("title");
            titleLabel.textProperty().bind(control.titleProperty());
            titleLabel.managedProperty().bind(titleLabel.visibleProperty());
            titleLabel.visibleProperty().bind(control.titleProperty().isNotEmpty());

            cellsBox = new VBox();
            cellsBox.getStyleClass().add("cells-box");
            ScrollPane scrollPane = new ScrollPane(cellsBox);
            scrollPane.setFitToWidth(true);

            VBox container = new VBox(titleLabel, new Separator(), scrollPane);
            container.getStyleClass().add("container");

            getChildren().add(container);

            updateView();
            control.itemsProperty().addListener((InvalidationListener) it -> updateView());
        }

        private void updateView() {
            cellsBox.getChildren().clear();
            getSkinnable().getItems().forEach(item -> cellsBox.getChildren().add(createControlCell(item)));
        }

        private VBox createControlCell(ControlItem item) {
            Label label = new Label(item.title());
            label.getStyleClass().add("control-cell-title");

            Region control = item.control();
            if (!control.maxWidthProperty().isBound()) {
                control.setMaxWidth(Double.MAX_VALUE);
            }
            VBox cell = new VBox(5, label, control);
            cell.getStyleClass().add("simple-control-cell");
            if (item.withBorder()) {
                cell.getStyleClass().add("with-border");
            }
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setMaxHeight(Region.USE_PREF_SIZE);
            return cell;
        }
    }
}
