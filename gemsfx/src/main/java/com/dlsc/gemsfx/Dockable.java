package com.dlsc.gemsfx;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;

import java.util.Objects;

/**
 * Represents a dockable component with an ID, a mutable display title, an optional graphic, and a content node.
 */
public final class Dockable {
    
    private final String id;
    private final ReadOnlyStringWrapper title;
    private final Node graphic;
    private final Node content;
    
    /**
     * Constructs a Dockable without a graphic.
     *
     * @param id      the unique identifier, must not be null or blank
     * @param title   the display title, must not be null or blank
     * @param content the content node, must not be null
     * @throws IllegalArgumentException if id or title is null/blank, or content is null
     */
    public Dockable(String id, String title, Node content) {
        this(id, title, null, content);
    }
    
    /**
     * Constructs a Dockable with an optional graphic.
     *
     * @param id      the unique identifier, must not be null or blank
     * @param title   the display title, must not be null or blank
     * @param graphic the optional icon/graphic, may be null
     * @param content the content node, must not be null
     * @throws IllegalArgumentException if id or title is null/blank, or content is null
     */
    public Dockable(String id, String title, Node graphic, Node content) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.title = new ReadOnlyStringWrapper(this, "title", validateTitle(title));
        this.graphic = graphic;
        this.content = Objects.requireNonNull(content, "content must not be null");
    }
    
    /**
     * Returns the unique identifier of this Dockable.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * The display title property. The property is read-only; use {@link #setTitle(String)} to change the title.
     *
     * @return the title property
     */
    public final ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    public final String getTitle() {
        return title.get();
    }

    public final void setTitle(String title) {
        this.title.set(validateTitle(title));
    }
    
    /**
     * Returns the optional icon/graphic of this Dockable.
     *
     * @return the graphic, or null if not set
     */
    public Node getGraphic() {
        return graphic;
    }
    
    /**
     * Returns the content node of this Dockable.
     *
     * @return the content
     */
    public Node getContent() {
        return content;
    }

    private static String validateTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
    }
}
