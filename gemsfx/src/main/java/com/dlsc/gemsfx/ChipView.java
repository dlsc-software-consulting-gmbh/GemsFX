package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ChipViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class ChipView<T> extends Control {

    public ChipView() {
        getStyleClass().add("chip-view");

        setMinWidth(Region.USE_PREF_SIZE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChipViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("chip-view.css").toExternalForm();
    }

    // value

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    // text

    private final StringProperty text = new SimpleStringProperty(this, "text", "Untitled");

    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    // graphic

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public Node getGraphic() {
        return graphic.get();
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    // content display

    private final ObjectProperty<ContentDisplay> contentDisplay = new SimpleObjectProperty<>(this, "contentDisplay", ContentDisplay.LEFT);

    public ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

    public ObjectProperty<ContentDisplay> contentDisplayProperty() {
        return contentDisplay;
    }

    public void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(contentDisplay);
    }

    // on close

    private final ObjectProperty<Consumer<T>> onClose = new SimpleObjectProperty<>(this, "onClose");

    public ObjectProperty<Consumer<T>> onCloseProperty() {
        return onClose;
    }

    public Consumer<T> getOnClose() {
        return onClose.get();
    }

    public void setOnClose(Consumer<T> onClose) {
        this.onClose.set(onClose);
    }
}
