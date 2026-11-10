package com.dlsc.gemsfx.docking;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.util.Objects;

/**
 * Represents a dockable component with an ID, a mutable display title and optional graphic, and a content node.
 */
public class Dockable {
    
    private final String id;
    private final ReadOnlyStringWrapper title;
    private final BooleanProperty titleEditable = new SimpleBooleanProperty(this, "titleEditable", true);
    private final ReadOnlyObjectWrapper<Node> graphic;
    private final Node content;
    private final ObjectProperty<Node> toolbar = new SimpleObjectProperty<>(this, "toolbar");
    private final ObjectProperty<Runnable> onHelp = new SimpleObjectProperty<>(this, "onHelp");
    
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
        this.graphic = new ReadOnlyObjectWrapper<>(this, "graphic", graphic);
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
     * Whether the user can edit the title by double-clicking the default {@link DockingTab}.
     * Defaults to {@code true}. Setting this to {@code false} cancels an active title edit;
     * programmatic changes through {@link #setTitle(String)} remain allowed.
     *
     * @return the title editing permission property
     */
    public final BooleanProperty titleEditableProperty() {
        return titleEditable;
    }

    public final boolean isTitleEditable() {
        return titleEditable.get();
    }

    public final void setTitleEditable(boolean titleEditable) {
        this.titleEditable.set(titleEditable);
    }
    
    /**
     * The optional graphic displayed in this dockable's tab, or {@code null} for no graphic.
     * The property is read-only; use {@link #setGraphic(Node)} to replace or remove the graphic.
     *
     * @return the graphic property
     */
    public final ReadOnlyObjectProperty<Node> graphicProperty() {
        return graphic.getReadOnlyProperty();
    }

    public final Node getGraphic() {
        return graphic.get();
    }

    public final void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    /**
     * The optional toolbar displayed to the left of the help and close buttons in the default {@link DockingTab}.
     * Defaults to {@code null}. Changes replace or remove the displayed toolbar immediately.
     * The toolbar fades in with the buttons when the tab is hovered.
     *
     * @return the toolbar property
     */
    public final ObjectProperty<Node> toolbarProperty() {
        return toolbar;
    }

    public final Node getToolbar() {
        return toolbar.get();
    }

    public final void setToolbar(Node toolbar) {
        this.toolbar.set(toolbar);
    }

    /**
     * The optional callback invoked when help is requested for this dockable.
     * Defaults to {@code null}, hiding the help button in the default {@link DockingTab}.
     * Changes update the button's availability immediately; the callback runs on the JavaFX application thread
     * when the user activates the button.
     *
     * @return the help callback property
     */
    public final ObjectProperty<Runnable> onHelpProperty() {
        return onHelp;
    }

    public final Runnable getOnHelp() {
        return onHelp.get();
    }

    public final void setOnHelp(Runnable onHelp) {
        this.onHelp.set(onHelp);
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
