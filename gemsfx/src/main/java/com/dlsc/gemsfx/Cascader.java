package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.CascaderSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Popup cascader control backed by a reusable {@link CascaderView}. The control
 * owns the popup shell, the display field, and its own configuration properties;
 * the embedded view (kept private and used as popup content) owns path expansion,
 * single selection, multiple checked paths, disabled inheritance, lazy loading,
 * and tri-state check logic.
 *
 * <p>Configuration properties declared here ({@code selectionMode},
 * {@code converter}, {@code visibleRowCount}, {@code cellFactory},
 * {@code childrenLoader}, {@code onChildrenLoadError}) drive the embedded view
 * through one-way bindings; the read-only {@code selectedPath} mirrors it back.
 * Each property's bean is this control, per the JavaFX convention. The root item
 * and result lists are the embedded view's own lists, shared directly.
 *
 * <p>{@code columnWidth} / {@code rowHeight} are plain write-only forwards (no
 * {@code xxxProperty()}, not observable): the embedded view holds the
 * CSS-settable {@code -fx-column-width} / {@code -fx-row-height} authority, and
 * binding a wrapper styleable property would stop CSS from reaching it. Style the
 * popup via {@code .cascader-popup .cascader-view} /
 * {@code .cascader-view > .columns > .column}.
 *
 * <p><strong>Threading.</strong> All operation methods (select / setCheckedCascade
 * / seedChecked / reload / show / hide / clearSelection) must be invoked on the
 * JavaFX Application Thread.
 *
 * <p><strong>Keyboard.</strong> Space / F4 / Alt+Up / Alt+Down toggle the popup
 * and Escape closes it; plain arrows on a closed cascader deliberately stay
 * inert (available for form focus traversal). While the popup is showing, the
 * arrow keys navigate the columns — the first arrow seeds the keyboard focus
 * (on the revealed selection when one exists), Up / Down move within the column
 * skipping disabled items, Right expands into a branch, Left steps back,
 * Home / End jump within the column — and Enter activates the focused item
 * (selecting a leaf, or toggling its check box in multiple mode); an Enter
 * without a keyboard focus closes the popup. Left / Right follow the effective
 * node orientation.
 *
 * @param <T> application value type
 */
public class Cascader<T> extends Control {

    // ==================== Constants ====================

    private static final String DEFAULT_STYLE_CLASS = "cascader";

    /** Default field separator joining the levels of a selected path. */
    public static final String DEFAULT_SEPARATOR = " / ";

    // ==================== Fields ====================

    private final CascaderView<T> view = new CascaderView<>();

    // ==================== Constructor ====================

    /**
     * Creates an empty cascader.
     */
    public Cascader() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setFocusTraversable(true);
        // Configuration flows down into the embedded popup view; the result
        // (selected path) flows back up into a read-only mirror. The item and
        // result lists are shared directly (see the getters), not bound here.
        view.selectionModeProperty().bind(selectionMode);
        view.converterProperty().bind(converter);
        view.visibleRowCountProperty().bind(visibleRowCount);
        view.emptyTextProperty().bind(emptyText);
        view.cellFactoryProperty().bind(cellFactory);
        view.childrenLoaderProperty().bind(childrenLoader);
        view.onChildrenLoadErrorProperty().bind(onChildrenLoadError);
        selectedPath.bind(view.selectedPathProperty());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CascaderSkin<>(this, view);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(Cascader.class.getResource("cascader.css")).toExternalForm();
    }

    // ==================== Items ====================

    /**
     * Root items shown in the first cascader column. The supported reset entry
     * points for the tree are replacing this list, switching the
     * {@link #childrenLoaderProperty() childrenLoader}, and {@link #reload()};
     * mutating already-loaded deep child lists at runtime is not tracked by the
     * view. Null items are not permitted; inserting {@code null} leads to a
     * {@link NullPointerException} while derived state is maintained.
     *
     * @return mutable root item list
     */
    public final ObservableList<CascaderItem<T>> getRootItems() {
        return view.getRootItems();
    }

    /**
     * Expanded branch path.
     *
     * @return read-only active path list
     */
    public final ObservableList<CascaderItem<T>> getActivePath() {
        return view.getActivePath();
    }

    // ==================== Selection Mode ====================

    private final ObjectProperty<SelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.SINGLE);

    /**
     * Selection mode. {@link SelectionMode#SINGLE SINGLE} selects a single leaf
     * path (observe {@link #selectedPathProperty()}); {@link SelectionMode#MULTIPLE
     * MULTIPLE} checks multiple paths with cascading tri-state check boxes (observe
     * {@link #getCheckedPaths()}). This is the cascader's own meaning of the shared
     * JavaFX {@link SelectionMode} enum, not the row multi-select of a list view.
     *
     * @return selection-mode property
     */
    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /**
     * Returns the selection mode.
     *
     * @return selection mode
     */
    public final SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    /**
     * Sets the selection mode.
     *
     * @param value selection mode
     */
    public final void setSelectionMode(SelectionMode value) {
        selectionMode.set(value);
    }

    // ==================== Selected Path ====================

    private final ReadOnlyObjectWrapper<CascaderPath<T>> selectedPath =
            new ReadOnlyObjectWrapper<>(this, "selectedPath");

    /**
     * Selected path in single-selection mode.
     *
     * @return read-only selected-path property
     */
    public final ReadOnlyObjectProperty<CascaderPath<T>> selectedPathProperty() {
        return selectedPath.getReadOnlyProperty();
    }

    /**
     * Returns the selected path.
     *
     * @return selected path, or {@code null}
     */
    public final CascaderPath<T> getSelectedPath() {
        return selectedPath.get();
    }

    /**
     * Checked leaf paths in multiple-selection mode. Paths are derived only from
     * <em>resolved</em> checked leaves: a checked but not-yet-loaded lazy branch
     * (or one whose load failed) contributes nothing here until its descendant
     * leaves have loaded, even though its check box shows checked.
     *
     * @return read-only checked path list maintained by the embedded view
     */
    public final ObservableList<CascaderPath<T>> getCheckedPaths() {
        return view.getCheckedPaths();
    }

    // ==================== Prompt Text ====================

    private final StringProperty promptText =
            new SimpleStringProperty(this, "promptText", "");

    /**
     * Placeholder text shown when no path is selected.
     *
     * @return prompt-text property
     */
    public final StringProperty promptTextProperty() {
        return promptText;
    }

    /**
     * Returns the prompt text.
     *
     * @return prompt text
     */
    public final String getPromptText() {
        return promptText.get();
    }

    /**
     * Sets the prompt text.
     *
     * @param value prompt text, or {@code null}
     */
    public final void setPromptText(String value) {
        promptText.set(value);
    }

    // ==================== Converter ====================

    private final ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<>(this, "converter");

    /**
     * Converts an item value to its display text (single source of the visible
     * node text). When {@code null}, {@code String.valueOf(value)} is used. A
     * {@code null} value, or a converter that returns {@code null}, yields the
     * empty string. Only {@code toString} is used; {@code fromString} is never
     * called (a cascader has no free-text path).
     *
     * @return the converter property
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Returns the converter.
     *
     * @return the converter, or {@code null}
     */
    public final StringConverter<T> getConverter() {
        return converter.get();
    }

    /**
     * Sets the converter.
     *
     * @param value the converter, or {@code null}
     */
    public final void setConverter(StringConverter<T> value) {
        converter.set(value);
    }

    // ==================== Path Text Factory ====================

    private final ObjectProperty<Function<CascaderPath<T>, String>> pathTextFactory =
            new SimpleObjectProperty<>(this, "pathTextFactory");

    /**
     * Optional formatter from a selected path to the single string shown in the
     * field. When {@code null}, the field shows the per-node display texts
     * (resolved by {@link #getConverter() converter}) joined with the
     * {@link #separatorProperty() separator}, or only the last level when
     * {@link #showAllLevelsProperty() showAllLevels} is {@code false}.
     *
     * <p>To keep the field consistent with the columns, resolve node text from
     * each item's value via the same {@link #getConverter() converter}
     * rather than {@code value.toString()}, which bypasses it.
     *
     * @return path-text factory property
     */
    public final ObjectProperty<Function<CascaderPath<T>, String>> pathTextFactoryProperty() {
        return pathTextFactory;
    }

    /**
     * Returns the path-text factory.
     *
     * @return path-text factory, or {@code null}
     */
    public final Function<CascaderPath<T>, String> getPathTextFactory() {
        return pathTextFactory.get();
    }

    /**
     * Sets the path-text factory.
     *
     * @param value path-text factory, or {@code null}
     */
    public final void setPathTextFactory(Function<CascaderPath<T>, String> value) {
        pathTextFactory.set(value);
    }

    // ==================== Separator ====================

    private final StringProperty separator =
            new SimpleStringProperty(this, "separator", DEFAULT_SEPARATOR);

    /**
     * Separator used by the default field text to join the levels of a selected
     * path. Ignored when a {@link #pathTextFactoryProperty() pathTextFactory} is
     * set. A {@code null} value falls back to {@link #DEFAULT_SEPARATOR}.
     *
     * @return separator property
     */
    public final StringProperty separatorProperty() {
        return separator;
    }

    /**
     * Returns the field separator.
     *
     * @return separator, or {@code null}
     */
    public final String getSeparator() {
        return separator.get();
    }

    /**
     * Sets the field separator.
     *
     * @param value separator, or {@code null} for the default
     */
    public final void setSeparator(String value) {
        separator.set(value);
    }

    // ==================== Show All Levels ====================

    private final BooleanProperty showAllLevels =
            new SimpleBooleanProperty(this, "showAllLevels", true);

    /**
     * Whether the default field text shows the full path (all levels joined by the
     * {@link #separatorProperty() separator}) or only the last level. Ignored when
     * a {@link #pathTextFactoryProperty() pathTextFactory} is set. Defaults to
     * {@code true}.
     *
     * @return show-all-levels property
     */
    public final BooleanProperty showAllLevelsProperty() {
        return showAllLevels;
    }

    /**
     * Returns whether the field shows all levels.
     *
     * @return {@code true} if all levels are shown
     */
    public final boolean isShowAllLevels() {
        return showAllLevels.get();
    }

    /**
     * Sets whether the field shows all levels.
     *
     * @param value {@code true} to show all levels, {@code false} for the last only
     */
    public final void setShowAllLevels(boolean value) {
        showAllLevels.set(value);
    }

    // ==================== Clearable ====================

    private final BooleanProperty clearable =
            new SimpleBooleanProperty(this, "clearable", false);

    /**
     * Whether a clear affordance is shown when a selection exists.
     *
     * @return clearable property
     */
    public final BooleanProperty clearableProperty() {
        return clearable;
    }

    /**
     * Returns whether the control is clearable.
     *
     * @return {@code true} if clearable
     */
    public final boolean isClearable() {
        return clearable.get();
    }

    /**
     * Sets whether the control is clearable.
     *
     * @param value {@code true} if clearable
     */
    public final void setClearable(boolean value) {
        clearable.set(value);
    }

    // ==================== Showing ====================

    private final ReadOnlyBooleanWrapper showing =
            new ReadOnlyBooleanWrapper(this, "showing", false);

    /**
     * Whether the popup is showing.
     *
     * @return read-only showing property
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /**
     * Returns whether the popup is showing.
     *
     * @return {@code true} if showing
     */
    public final boolean isShowing() {
        return showing.get();
    }

    /**
     * Requests the popup to show.
     */
    public final void show() {
        if (isDisabled()) {
            return;
        }
        showing.set(true);
    }

    /**
     * Requests the popup to hide.
     */
    public final void hide() {
        showing.set(false);
    }

    // ==================== Visible Row Count ====================

    private final IntegerProperty visibleRowCount =
            new SimpleIntegerProperty(this, "visibleRowCount", CascaderView.DEFAULT_VISIBLE_ROW_COUNT);

    /**
     * Number of visible popup rows used for the preferred popup height.
     *
     * @return visible-row-count property
     */
    public final IntegerProperty visibleRowCountProperty() {
        return visibleRowCount;
    }

    /**
     * Returns the visible row count.
     *
     * @return visible row count
     */
    public final int getVisibleRowCount() {
        return visibleRowCount.get();
    }

    /**
     * Sets the visible row count.
     *
     * @param value visible row count
     */
    public final void setVisibleRowCount(int value) {
        visibleRowCount.set(value);
    }

    // ==================== Empty Text ====================

    private final StringProperty emptyText =
            new SimpleStringProperty(this, "emptyText", CascaderView.DEFAULT_EMPTY_TEXT);

    /**
     * Placeholder text shown in an empty popup column — the root column when there
     * are no root items, or a forced branch ({@code leafHint=false}) that resolved
     * to zero children. A {@code null} value renders a blank placeholder.
     *
     * @return empty-column placeholder text property
     */
    public final StringProperty emptyTextProperty() {
        return emptyText;
    }

    /**
     * Returns the empty-column placeholder text.
     *
     * @return empty-column placeholder text
     */
    public final String getEmptyText() {
        return emptyText.get();
    }

    /**
     * Sets the empty-column placeholder text.
     *
     * @param value empty-column placeholder text
     */
    public final void setEmptyText(String value) {
        emptyText.set(value);
    }

    // ==================== Column Width / Row Height ====================

    /**
     * Returns the preferred popup column width.
     *
     * @return column width in pixels
     */
    public final double getColumnWidth() {
        return view.getColumnWidth();
    }

    /**
     * Sets the preferred popup column width. Forwarded to the embedded view, which
     * is the CSS authority for {@code -fx-column-width}; this is a plain setter (not
     * a styleable property) so the view's property stays CSS-settable.
     *
     * @param value column width in pixels
     */
    public final void setColumnWidth(double value) {
        view.setColumnWidth(value);
    }

    /**
     * Returns the fixed popup row height.
     *
     * @return row height in pixels
     */
    public final double getRowHeight() {
        return view.getRowHeight();
    }

    /**
     * Sets the fixed popup row height. Forwarded to the embedded view, which is the
     * CSS authority for {@code -fx-row-height}; this is a plain setter (not a
     * styleable property) so the view's property stays CSS-settable.
     *
     * @param value row height in pixels
     */
    public final void setRowHeight(double value) {
        view.setRowHeight(value);
    }

    // ==================== Cell Factory ====================

    private final ObjectProperty<Callback<CascaderView<T>, ListCell<CascaderItem<T>>>> cellFactory =
            new SimpleObjectProperty<>(this, "cellFactory");

    /**
     * Optional factory for the cells of each popup column.
     *
     * @return cell-factory property
     */
    public final ObjectProperty<Callback<CascaderView<T>, ListCell<CascaderItem<T>>>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Returns the cell factory.
     *
     * @return cell factory, or {@code null}
     */
    public final Callback<CascaderView<T>, ListCell<CascaderItem<T>>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * Sets the cell factory.
     *
     * @param value cell factory, or {@code null}
     */
    public final void setCellFactory(Callback<CascaderView<T>, ListCell<CascaderItem<T>>> value) {
        cellFactory.set(value);
    }

    // ==================== Children Loader ====================

    private final ObjectProperty<Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>>>
            childrenLoader = new SimpleObjectProperty<>(this, "childrenLoader");

    /**
     * Optional asynchronous loader used by unloaded branches. The stage should
     * complete with the loaded child items; a {@code null} stage, or a stage that
     * completes with {@code null} children, is treated as an empty successful result
     * (the branch becomes a loaded leaf).
     *
     * <p>Setting or swapping a non-{@code null} loader resets the tree: navigation,
     * loaded children and all check state are cleared (the same effect as
     * {@link #reload()}). Clearing the loader to {@code null} keeps the current tree.
     *
     * <p>In multiple-selection mode, checking an unloaded branch resolves it
     * eagerly: the loader runs for that branch and, as results arrive,
     * recursively for its descendant branches until the checked paths resolve
     * to leaves — budget the loader for that fan-out when deep lazy trees are
     * checkable.
     *
     * @return children-loader property
     */
    public final ObjectProperty<Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>>>
            childrenLoaderProperty() {
        return childrenLoader;
    }

    /**
     * Returns the children loader.
     *
     * @return children loader, or {@code null}
     */
    public final Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>> getChildrenLoader() {
        return childrenLoader.get();
    }

    /**
     * Sets the children loader.
     *
     * @param value children loader, or {@code null}
     */
    public final void setChildrenLoader(
            Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>> value) {
        childrenLoader.set(value);
    }

    // ==================== Children Load Error ====================

    private final ObjectProperty<BiConsumer<CascaderItem<T>, Throwable>> onChildrenLoadError =
            new SimpleObjectProperty<>(this, "onChildrenLoadError");

    /**
     * Optional callback invoked on the JavaFX thread when a lazy children load
     * fails. A failure delivered as a {@code CompletionException} (the standard
     * async-supplier wrapper) is unwrapped to its cause.
     *
     * @return children-load-error callback property
     */
    public final ObjectProperty<BiConsumer<CascaderItem<T>, Throwable>> onChildrenLoadErrorProperty() {
        return onChildrenLoadError;
    }

    /**
     * Returns the children-load-error callback.
     *
     * @return callback, or {@code null}
     */
    public final BiConsumer<CascaderItem<T>, Throwable> getOnChildrenLoadError() {
        return onChildrenLoadError.get();
    }

    /**
     * Sets the children-load-error callback.
     *
     * @param value callback, or {@code null}
     */
    public final void setOnChildrenLoadError(BiConsumer<CascaderItem<T>, Throwable> value) {
        onChildrenLoadError.set(value);
    }

    // ==================== Operations ====================

    /**
     * Forces a same-source reload of the whole lazy tree. In eager mode (no
     * loader set) this is a no-op.
     */
    public final void reload() {
        view.reload();
    }

    /**
     * Clears both single and multiple selection state.
     */
    public final void clearSelection() {
        view.clearSelection();
    }

    /**
     * Programmatically sets the single selection to the path ending at the given
     * leaf. Applies only in single-selection mode; ignored in multiple mode, or
     * when the item is {@code null}, effectively disabled, or not a leaf. Like
     * {@code TreeView}'s selection model, this method may keep a selection whose
     * item is not currently reachable from the roots; the popup reveals it only
     * when the leaf is reachable.
     *
     * @param leaf leaf item to select
     */
    public final void select(CascaderItem<T> leaf) {
        view.select(leaf);
    }

    /**
     * Sets a cascading check state: the item and its enabled descendants are
     * (un)checked and ancestors roll up to the matching tri-state. Applies only in
     * multiple-selection mode; ignored in single mode, or when the item is not
     * currently reachable from {@link #getRootItems()}. This is the runtime entry
     * point for programmatic checking (an item's checked state is read-only); to
     * seed an initial selection before display use {@link #seedChecked}.
     *
     * <p>Targeting an unresolved lazy branch records the intent, starts (or
     * reuses) the branch's load, and replays the check once the children
     * arrive; checking with {@code true} keeps resolving recursively until the
     * checked paths reach leaves (see {@link #childrenLoaderProperty()}).
     *
     * @param item    item to update
     * @param checked target checked state
     */
    public final void setCheckedCascade(CascaderItem<T> item, boolean checked) {
        view.setCheckedCascade(item, checked);
    }

    /**
     * Seeds an initial multiple-selection checked state: marks each given item
     * checked, rolls the tri-state up to ancestors, and refreshes the checked
     * paths once. Use this instead of writing item state directly (an item's
     * checked state is read-only); for runtime check changes use
     * {@link #setCheckedCascade}. It may be called before or after switching to
     * {@link SelectionMode#MULTIPLE} — a seed made before the switch survives it.
     *
     * <p>An effectively-disabled branch given as a seed is ignored as a whole (the
     * cascade skips disabled descendants); to lock a disabled subtree checked, pass
     * its leaves individually — a disabled leaf given directly is honored.
     *
     * <p>In lazy mode, seeding an unresolved branch that is already reachable
     * from the roots behaves like {@link #setCheckedCascade}: the intent is
     * recorded and the branch's load starts immediately (recursively, until the
     * seed resolves to leaves). A detached item can still be pre-marked, but it
     * does not start loading until it is reachable and operated on. Prefer
     * seeding leaves or resolved branches when that fetch is unwanted.
     *
     * @param items items to mark checked (leaves, or branches with resolved children)
     */
    public final void seedChecked(Collection<CascaderItem<T>> items) {
        view.seedChecked(items);
    }
}
