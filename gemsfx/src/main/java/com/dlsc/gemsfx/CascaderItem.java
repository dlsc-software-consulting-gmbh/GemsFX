package com.dlsc.gemsfx;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Node model used by {@link CascaderView}. It wraps an application value with
 * children, disabled state, lazy-loading hints, and tri-state check state.
 *
 * <p>Display text is not stored on the item: the owning view derives it from
 * the value via its {@code converter} (falling back to
 * {@code String.valueOf(value)}).
 *
 * @param <T> application value type
 */
public class CascaderItem<T> {

    // ==================== Parent ====================

    private final ReadOnlyObjectWrapper<CascaderItem<T>> parent =
            new ReadOnlyObjectWrapper<>(this, "parent");

    /**
     * Parent item, or {@code null} for root items.
     *
     * @return read-only parent property
     */
    public final ReadOnlyObjectProperty<CascaderItem<T>> parentProperty() {
        return parent.getReadOnlyProperty();
    }

    /**
     * Returns the parent item.
     *
     * @return parent item, or {@code null}
     */
    public final CascaderItem<T> getParent() {
        return parent.get();
    }

    final void setParentItem(CascaderItem<T> value) {
        parent.set(value);
    }

    // ==================== Children ====================

    private final ObservableList<CascaderItem<T>> children =
            new NonNullObservableList<>("child item");

    // ==================== Constructors ====================

    /**
     * Creates an item with null value.
     */
    public CascaderItem() {
        this(null);
    }

    /**
     * Creates an item with the given value. Display text is derived from the
     * value by the owning view's {@code converter} (or {@code value.toString()}
     * as a fallback), not stored on the item.
     *
     * @param value application value
     */
    public CascaderItem(@NamedArg("value") T value) {
        setValue(value);
        children.addListener((ListChangeListener<CascaderItem<T>>) change -> {
            while (change.next()) {
                for (CascaderItem<T> removed : change.getRemoved()) {
                    if (removed.getParent() == this) {
                        removed.setParentItem(null);
                    }
                }
                for (CascaderItem<T> added : change.getAddedSubList()) {
                    if (added != null) {
                        added.setParentItem(this);
                    }
                }
            }
        });
    }

    // ==================== Value ====================

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

    /**
     * Application value represented by this item.
     *
     * @return value property
     */
    public final ObjectProperty<T> valueProperty() {
        return value;
    }

    /**
     * Returns the application value.
     *
     * @return application value
     */
    public final T getValue() {
        return value.get();
    }

    /**
     * Sets the application value.
     *
     * @param value application value
     */
    public final void setValue(T value) {
        this.value.set(value);
    }

    /**
     * Child items. Each node must have a single parent and the tree must be
     * acyclic (as with {@link javafx.scene.control.TreeItem}); adding a node to two
     * parents or introducing a cycle yields undefined path/recursion behavior. Null
     * children are not permitted: inserting {@code null} (including via a bulk
     * {@code addAll} / {@code setAll}) is rejected with a {@link NullPointerException}
     * at the call site, leaving the list unchanged.
     *
     * @return mutable child list
     */
    public final ObservableList<CascaderItem<T>> getChildren() {
        return children;
    }

    // ==================== Disable ====================

    private final BooleanProperty disable =
            new SimpleBooleanProperty(this, "disable", false);

    /**
     * Whether this item itself is disabled. Named {@code disable} (not
     * {@code disabled}) to match the JavaFX writable-disable convention of
     * {@code Node}/{@code MenuItem}/{@code Tab}; the effective disabled state
     * (this item OR-ed with any disabled ancestor) is computed by the owning
     * {@code CascaderView}.
     *
     * @return disable property
     */
    public final BooleanProperty disableProperty() {
        return disable;
    }

    /**
     * Returns whether this item itself is disabled.
     *
     * @return {@code true} if disabled
     */
    public final boolean isDisable() {
        return disable.get();
    }

    /**
     * Sets whether this item itself is disabled.
     *
     * @param value {@code true} if disabled
     */
    public final void setDisable(boolean value) {
        disable.set(value);
    }

    // ==================== Leaf Hint ====================

    private final ObjectProperty<Boolean> leafHint =
            new SimpleObjectProperty<>(this, "leafHint");

    /**
     * Tri-state leaf override consumed by the owning {@link CascaderView}.
     *
     * <ul>
     *   <li>{@code true} — force this item to be a leaf: no expand arrow and no
     *       lazy load. In lazy mode (a children loader is set) this is the
     *       primary way to mark a node whose children are already known to be
     *       empty.</li>
     *   <li>{@code false} — force this item to be a branch even when it has no
     *       children, so an empty eager node still shows as expandable (and
     *       renders an empty column).</li>
     *   <li>{@code null} (default) — the view derives leaf state: eager mode
     *       uses {@code children.isEmpty()}; lazy mode treats an unloaded node
     *       as a branch until it has been loaded.</li>
     * </ul>
     *
     * @return leaf-hint property
     */
    public final ObjectProperty<Boolean> leafHintProperty() {
        return leafHint;
    }

    /**
     * Returns the leaf hint.
     *
     * @return leaf hint, or {@code null}
     */
    public final Boolean getLeafHint() {
        return leafHint.get();
    }

    /**
     * Sets the leaf hint.
     *
     * @param value leaf hint, or {@code null}
     */
    public final void setLeafHint(Boolean value) {
        leafHint.set(value);
    }

    // ==================== Load State ====================

    /**
     * Lifecycle of a branch's children with respect to a lazy
     * {@link CascaderView#childrenLoaderProperty() children loader}.
     */
    public enum LoadState {
        /**
         * Initial state, before any load is attempted. Eager items stay EAGER; a
         * lazy branch is also EAGER until first expanded, where the coordinator
         * treats it the same as {@link #NOT_LOADED}.
         */
        EAGER,
        /**
         * A lazy branch reset after a prior load (for example by reload); awaiting
         * (re)load and equivalent to {@link #EAGER} for load decisions.
         */
        NOT_LOADED,
        /** A loader stage is in flight for this branch. */
        LOADING,
        /** The loader populated this branch's children successfully. */
        LOADED,
        /** The loader failed; the branch is retriable by expanding it again. */
        FAILED
    }

    private final ReadOnlyObjectWrapper<LoadState> loadState =
            new ReadOnlyObjectWrapper<>(this, "loadState", LoadState.EAGER);

    /**
     * Lazy-loading lifecycle state of this item, observable but not writable. The
     * owning {@link CascaderView} drives it: a fresh lazy branch resolves
     * through {@link LoadState#LOADING} to {@link LoadState#LOADED} or
     * {@link LoadState#FAILED}; eager items stay {@link LoadState#EAGER}. It is
     * the single observable load signal, and each transition fires exactly once.
     *
     * @return read-only load-state property
     */
    public final ReadOnlyObjectProperty<LoadState> loadStateProperty() {
        return loadState.getReadOnlyProperty();
    }

    /**
     * Returns the lazy-loading lifecycle state.
     *
     * @return current load state, never {@code null}
     */
    public final LoadState getLoadState() {
        return loadState.get();
    }

    final void setLoadState(LoadState value) {
        loadState.set(value);
    }

    // ==================== Load coordination (package-private, FX thread only) ====================

    // Coordination state, not UI state: plain non-observable fields read and
    // written only by CascaderView on the JavaFX application thread. loadToken
    // guards against stale completions; pendingCheck holds a deferred check
    // intent recorded while a branch is still loading.
    private long loadToken;
    private Boolean pendingCheck;

    final long getLoadToken() {
        return loadToken;
    }

    final void setLoadToken(long value) {
        loadToken = value;
    }

    final Boolean getPendingCheck() {
        return pendingCheck;
    }

    final void setPendingCheck(Boolean value) {
        pendingCheck = value;
    }

    // ==================== Checked ====================

    private final ReadOnlyBooleanWrapper checked =
            new ReadOnlyBooleanWrapper(this, "checked", false);

    /**
     * Whether this item is checked, observable but not writable. The owning
     * {@link CascaderView}'s tri-state machine drives it through the cascading
     * operations ({@code setCheckedCascade} / {@code toggleCheck}) and the
     * {@code seedChecked} pre-display entry point.
     *
     * @return read-only checked property
     */
    public final ReadOnlyBooleanProperty checkedProperty() {
        return checked.getReadOnlyProperty();
    }

    /**
     * Returns whether this item is checked.
     *
     * @return {@code true} if checked
     */
    public final boolean isChecked() {
        return checked.get();
    }

    final void setChecked(boolean value) {
        checked.set(value);
    }

    // ==================== Indeterminate ====================

    private final ReadOnlyBooleanWrapper indeterminate =
            new ReadOnlyBooleanWrapper(this, "indeterminate", false);

    /**
     * Whether this item is in the indeterminate (partially-checked) state,
     * observable but not writable. It is a derived display state maintained by
     * the owning {@link CascaderView}'s tri-state machine; an item is never both
     * checked and indeterminate.
     *
     * @return read-only indeterminate property
     */
    public final ReadOnlyBooleanProperty indeterminateProperty() {
        return indeterminate.getReadOnlyProperty();
    }

    /**
     * Returns whether this item is in the indeterminate state.
     *
     * @return {@code true} if indeterminate
     */
    public final boolean isIndeterminate() {
        return indeterminate.get();
    }

    final void setIndeterminate(boolean value) {
        indeterminate.set(value);
    }

    // ==================== User Data ====================

    private final ObjectProperty<Object> userData =
            new SimpleObjectProperty<>(this, "userData");

    /**
     * Optional user data associated with this item.
     *
     * @return user-data property
     */
    public final ObjectProperty<Object> userDataProperty() {
        return userData;
    }

    /**
     * Returns user data.
     *
     * @return user data
     */
    public final Object getUserData() {
        return userData.get();
    }

    /**
     * Sets user data.
     *
     * @param value user data
     */
    public final void setUserData(Object value) {
        userData.set(value);
    }

    /**
     * Returns a debug representation based on the value. This is a fallback for
     * loggers and {@code CascaderPath.toString()}; the visible cascader text is
     * produced by the owning view's {@code converter}, not by this method.
     *
     * @return {@code String.valueOf(getValue())}
     */
    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
