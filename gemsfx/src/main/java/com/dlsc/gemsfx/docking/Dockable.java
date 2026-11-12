package com.dlsc.gemsfx.docking;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Represents a dockable component with an ID, a mutable display title and optional graphic, and a content node.
 */
public class Dockable {
    
    private final String id;
    private final ReadOnlyStringWrapper title;
    private final BooleanProperty titleEditable = new SimpleBooleanProperty(this, "titleEditable", true);
    private final BooleanProperty closeable = new SimpleBooleanProperty(this, "closeable", true);
    private final ObjectProperty<BooleanSupplier> onCloseRequest = new SimpleObjectProperty<>(this, "onCloseRequest");
    private final ReadOnlyObjectWrapper<Node> graphic;
    private final Node content;
    private final ObjectProperty<Node> toolbar = new SimpleObjectProperty<>(this, "toolbar");
    private final ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
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
     * Whether this dockable can be closed through normal close requests. Defaults to {@code true}.
     * The default tab hides its close button when this property is {@code false}. Layout restore and
     * teardown of an already hidden window can still remove the dockable.
     *
     * @return the close permission property
     */
    public final BooleanProperty closeableProperty() {
        return closeable;
    }

    public final boolean isCloseable() {
        return closeable.get();
    }

    public final void setCloseable(boolean closeable) {
        this.closeable.set(closeable);
    }

    /**
     * The optional callback invoked on a normal close request after close permissions are checked.
     * Return {@code false} to veto the request or {@code true} to allow it. A {@code null} callback
     * allows the request. The callback runs on the JavaFX application thread and is not invoked
     * by permission queries, layout restore, or teardown of an already hidden window.
     *
     * @return the close request callback property
     */
    public final ObjectProperty<BooleanSupplier> onCloseRequestProperty() {
        return onCloseRequest;
    }

    public final BooleanSupplier getOnCloseRequest() {
        return onCloseRequest.get();
    }

    public final void setOnCloseRequest(BooleanSupplier onCloseRequest) {
        this.onCloseRequest.set(onCloseRequest);
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
     * The optional toolbar displayed to the left of the menu, help and close buttons in the default {@link DockingTab}.
     * Defaults to {@code null}. Changes replace or remove the displayed toolbar immediately.
     * The toolbar shares the action buttons' hover fade.
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
     * Returns the live list of actions displayed in the default {@link DockingTab}'s More dropdown.
     * The list is initially empty, hiding the menu button without reserving layout space.
     * Adding, removing or reordering items updates the dropdown immediately.
     * Each menu item should belong to only one dockable.
     *
     * @return the modifiable menu item list
     */
    public final ObservableList<MenuItem> getMenuItems() {
        return menuItems;
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

    /**
     * Releases resources after this dockable is removed from its docking context. Subclasses may
     * override this method; it is not called when the dockable merely moves between panes. If the
     * same instance is reopened, it can be disposed again when subsequently removed.
     */
    protected void dispose() {
    }

    private static String validateTitle(String title) {
        Objects.requireNonNull(title, "title must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title;
    }
}
