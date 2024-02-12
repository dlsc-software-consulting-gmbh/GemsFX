package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.MaskedViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A view that takes a content node and applies advanced clipping so that its
 * left and right side will "fade out". This is especially useful in combination
 * with controls that want to show scrolling controls on both sides for moving
 * things to the left and right as those controls will then be nicely visible.
 * The {@link StripView} control is using the masked view inside its skin.
 */
public class MaskedView extends Control {

    /**
     * Constructs a new masked view. Content can be specified later by calling
     * {@link #setContent(Node)}.
     */
    public MaskedView() {
        getStyleClass().add("masked-view");
        setFocusTraversable(false);
    }

    /**
     * Constructs a new masked view for the given content.
     */
    public MaskedView(Node content) {
        this();
        setContent(content);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaskedViewSkin(this);
    }

    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    /**
     * The content that will be shown inside the masked view.
     *
     * @return the content node
     */
    public final SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    // TODO: make styleable
    private final DoubleProperty fadingSize = new SimpleDoubleProperty(this, "fadingSize", 120);

    public final double getFadingSize() {
        return fadingSize.get();
    }

    /**
     * The width of the clips on the left and right hand side of the view. This property
     * defines how big the fade in / out areas will be.
     *
     * @return the size of the side fading areas / clip areas
     */
    public final DoubleProperty fadingSizeProperty() {
        return fadingSize;
    }

    public final void setFadingSize(double fadingSize) {
        this.fadingSize.set(fadingSize);
    }
}
