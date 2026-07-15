package com.dlsc.gemsfx;

import com.dlsc.gemsfx.CascaderItem.LoadState;
import com.dlsc.gemsfx.skins.CascaderViewSkin;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Standalone multi-column cascader view. It owns the active path, single
 * selection, multiple checked paths, disabled inheritance, and cascading
 * tri-state check logic.
 *
 * <p>This control is usable on its own — drop it inline into any layout to get
 * a multi-column cascader without an input field or popup, and observe
 * {@link #selectedPathProperty()} / {@link #getCheckedPaths()} for the result.
 * {@link Cascader} reuses it as its popup content.
 *
 * <p><strong>Tree mutation contract.</strong> Structural change is observed at the
 * root level ({@link #getRootItems()}): swapping roots, swapping the children
 * loader, and {@link #reload()} are the supported reset points. Mutating a deep
 * branch that is currently expanded or lazily loading is not a supported runtime
 * operation — the view defends against a late load completing on a branch that was
 * detached from the tree (its result is dropped), but does not otherwise prune the
 * active path for such edits. Like {@link javafx.scene.control.TreeItem}, the item
 * tree must be acyclic and each node must have a single parent.
 *
 * <p><strong>Threading.</strong> All operation methods (activate / expand / select
 * / toggleCheck / setCheckedCascade / seedChecked / reload / loadChildren) and
 * tree mutations must be invoked on the JavaFX Application Thread. A children
 * loader's stage may complete on any thread; the view marshals the completion
 * back to the FX thread before touching the tree.
 *
 * <p><strong>Keyboard.</strong> The view is focus-traversable. When focused,
 * Up / Down move the keyboard focus within the current column (skipping
 * disabled items), Right expands into a branch, Left steps back to the parent
 * column, Home / End jump within the column, and Enter or Space activates the
 * focused item (selecting a leaf, or toggling its check box in multiple mode).
 * Left / Right follow the effective node orientation.
 *
 * @param <T> application value type
 */
public class CascaderView<T> extends Control {

    /**
     * Default column width in pixels.
     */
    public static final double DEFAULT_COLUMN_WIDTH = 180.0;

    /**
     * Default fixed cell size (row height) in pixels.
     */
    public static final double DEFAULT_FIXED_CELL_SIZE = 34.0;

    /**
     * Default visible row count.
     */
    public static final int DEFAULT_VISIBLE_ROW_COUNT = 6;

    /**
     * Default empty-column placeholder text.
     */
    public static final String DEFAULT_EMPTY_TEXT = "No data";

    private static final String DEFAULT_STYLE_CLASS = "cascader-view";

    private final ObservableList<CascaderItem<T>> rootItems =
            new NonNullObservableList<>("root item");

    private final ObservableList<CascaderItem<T>> activePath =
            FXCollections.observableArrayList();

    private final ObservableList<CascaderItem<T>> readOnlyActivePath =
            FXCollections.unmodifiableObservableList(activePath);

    private final ObservableList<CascaderPath<T>> checkedPaths =
            FXCollections.observableArrayList();

    private final ObservableList<CascaderPath<T>> readOnlyCheckedPaths =
            FXCollections.unmodifiableObservableList(checkedPaths);

    /** Sentinel returned by {@link #startLoad} when no load was started; live tokens are >= 1. */
    private static final long NO_LOAD = 0L;

    /**
     * Items with a load currently in flight, keyed by identity. Membership is the
     * liveness gate: {@link #completeLoad} and {@link #runLoad} both require the
     * item to still be present, and {@link #cancelInFlight} clears the whole set
     * at once so any late completion bails.
     */
    private final Set<CascaderItem<T>> liveLoads = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Monotonic and never reset: its monotonicity is the stale-completion guarantee. */
    private long nextLoadToken;

    /**
     * Creates an empty cascader view.
     */
    public CascaderView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        // List-like control: tab-focusable inline so the skin's keyboard
        // navigation is reachable (inside the Cascader popup the owner
        // control keeps key focus and routes navigation instead).
        setFocusTraversable(true);
        rootItems.addListener((ListChangeListener<CascaderItem<T>>) change -> {
            while (change.next()) {
                for (CascaderItem<T> removed : change.getRemoved()) {
                    removed.setParentItem(null);
                }
                for (CascaderItem<T> added : change.getAddedSubList()) {
                    if (added != null) {
                        added.setParentItem(null);
                    }
                }
            }
            // Swapping roots is one of the three reset entry points: drop all
            // navigation and in-flight loads, but keep the new roots' children
            // and any seeded check state, then rebuild the derived paths.
            clearNavAndPending();
            refreshCheckedPaths();
            requestLayout();
        });
        childrenLoader.addListener((obs, oldLoader, newLoader) -> {
            if (newLoader != null) {
                resetTree();
            } else {
                switchToEager();
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CascaderViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(CascaderView.class.getResource("cascader.css")).toExternalForm();
    }

    // ==================== Items ====================

    /**
     * Root items shown in the first column. Null items are not permitted: inserting
     * {@code null} (including via a bulk {@code addAll} / {@code setAll}) is rejected
     * with a {@link NullPointerException} at the call site, leaving the list
     * unchanged.
     *
     * @return mutable root item list
     */
    public final ObservableList<CascaderItem<T>> getRootItems() {
        return rootItems;
    }

    /**
     * Expanded branch path.
     *
     * @return read-only active path list
     */
    public final ObservableList<CascaderItem<T>> getActivePath() {
        return readOnlyActivePath;
    }

    // ==================== Columns Revision ====================

    private final ReadOnlyIntegerWrapper columnsRevision =
            new ReadOnlyIntegerWrapper(this, "columnsRevision", 0);

    /**
     * Monotonic counter bumped whenever the rendered column structure should
     * change — navigation (active path), a frontier load completing, or a root /
     * loader change. The skin observes this single signal to re-sync its columns
     * instead of reaching into individual item load states.
     *
     * @return read-only columns-revision property
     */
    public final ReadOnlyIntegerProperty columnsRevisionProperty() {
        return columnsRevision.getReadOnlyProperty();
    }

    /**
     * Returns the current columns revision.
     *
     * @return columns revision counter
     */
    public final int getColumnsRevision() {
        return columnsRevision.get();
    }

    private void bumpColumnsRevision() {
        columnsRevision.set(columnsRevision.get() + 1);
    }

    // ==================== Selection Mode ====================

    private SelectionMode effectiveSelectionMode = SelectionMode.SINGLE;

    private final ObjectProperty<SelectionMode> selectionMode =
            new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.SINGLE) {
                @Override
                protected void invalidated() {
                    SelectionMode mode = selectionModeOrDefault();
                    if (mode != effectiveSelectionMode) {
                        effectiveSelectionMode = mode;
                        // Clear only the state the target mode cannot express, so a
                        // pre-seeded selection survives the natural seed-then-set-mode
                        // order: entering MULTIPLE drops the single selection; entering
                        // SINGLE drops the checked state.
                        if (mode == SelectionMode.MULTIPLE) {
                            clearSingleSelection();
                        } else {
                            clearMultipleSelection();
                        }
                    }
                    requestLayout();
                }
            };

    private SelectionMode selectionModeOrDefault() {
        SelectionMode mode = getSelectionMode();
        return mode == null ? SelectionMode.SINGLE : mode;
    }

    /**
     * Selection mode. {@link SelectionMode#SINGLE SINGLE} selects a single leaf
     * path (observe {@link #selectedPathProperty()}); {@link SelectionMode#MULTIPLE
     * MULTIPLE} checks multiple paths with cascading tri-state check boxes (observe
     * {@link #getCheckedPaths()}). This is the cascader's own meaning of the shared
     * JavaFX {@link SelectionMode} enum, not the row multi-select of a list view.
     * A {@code null} value is not rejected; it resolves to single-selection
     * behavior at the use site.
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
     * leaves have loaded, even though its check box shows checked. Observe an
     * item's {@link CascaderItem#checkedProperty()} for that optimistic state.
     *
     * @return read-only checked path list maintained by this view
     */
    public final ObservableList<CascaderPath<T>> getCheckedPaths() {
        return readOnlyCheckedPaths;
    }

    // ==================== Visible Row Count ====================

    private final IntegerProperty visibleRowCount =
            new SimpleIntegerProperty(this, "visibleRowCount", DEFAULT_VISIBLE_ROW_COUNT);

    /**
     * Preferred visible row count: the panel shows this many row slots. Fewer
     * items leave blank space, more items scroll within the column. (This is a
     * fixed row-slot count, not an "at most N rows" cap.)
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
            new SimpleStringProperty(this, "emptyText", DEFAULT_EMPTY_TEXT);

    /**
     * Placeholder text shown in a rendered column that has no items — the root
     * column when there are no root items, or a forced branch
     * ({@code leafHint=false}) that resolved to zero children. (An ordinary empty
     * node is a leaf and simply ends the cascade, so it renders no column.) A
     * {@code null} value renders a blank placeholder.
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

    // ==================== Column Width ====================

    private final DoubleProperty columnWidth = new StyleableDoubleProperty(DEFAULT_COLUMN_WIDTH) {
        @Override
        public Object getBean() {
            return CascaderView.this;
        }

        @Override
        public String getName() {
            return "columnWidth";
        }

        @Override
        public CssMetaData<CascaderView<?>, Number> getCssMetaData() {
            return StyleableProperties.COLUMN_WIDTH;
        }
    };

    /**
     * Preferred width of each column, in pixels, settable via CSS
     * {@code -fx-column-width}. It is a discoverable default that the skin applies
     * to each column with USER origin, so author CSS targeting
     * {@code .cascader-view > .columns > .column} still wins. Defaults to
     * {@link #DEFAULT_COLUMN_WIDTH}.
     *
     * @return column-width property
     */
    public final DoubleProperty columnWidthProperty() {
        return columnWidth;
    }

    /**
     * Returns the preferred column width.
     *
     * @return column width in pixels
     */
    public final double getColumnWidth() {
        return columnWidth.get();
    }

    /**
     * Sets the preferred column width.
     *
     * @param value column width in pixels
     */
    public final void setColumnWidth(double value) {
        columnWidth.set(value);
    }

    // ==================== Row Height ====================

    private final DoubleProperty rowHeight = new StyleableDoubleProperty(DEFAULT_FIXED_CELL_SIZE) {
        @Override
        public Object getBean() {
            return CascaderView.this;
        }

        @Override
        public String getName() {
            return "rowHeight";
        }

        @Override
        public CssMetaData<CascaderView<?>, Number> getCssMetaData() {
            return StyleableProperties.ROW_HEIGHT;
        }
    };

    /**
     * Fixed height of each row, in pixels, settable via CSS {@code -fx-row-height}.
     * It is a discoverable default that the skin applies to each column with USER
     * origin, so author CSS targeting
     * {@code .cascader-view > .columns > .column} still wins.
     * Defaults to {@link #DEFAULT_FIXED_CELL_SIZE}.
     *
     * @return row-height property
     */
    public final DoubleProperty rowHeightProperty() {
        return rowHeight;
    }

    /**
     * Returns the fixed row height.
     *
     * @return row height in pixels
     */
    public final double getRowHeight() {
        return rowHeight.get();
    }

    /**
     * Sets the fixed row height.
     *
     * @param value row height in pixels
     */
    public final void setRowHeight(double value) {
        rowHeight.set(value);
    }

    // ==================== Cell Factory ====================

    private final ObjectProperty<Callback<CascaderView<T>, ListCell<CascaderItem<T>>>> cellFactory =
            new SimpleObjectProperty<>(this, "cellFactory");

    /**
     * Optional factory for the cells of each column. When {@code null} the view
     * uses the built-in {@link CascaderCell}. The factory receives this view so
     * a custom cell can route interaction back to it; it may return an
     * {@link CascaderCell} subclass (recommended) or any {@link ListCell}.
     * A plain {@code ListCell} must itself observe whatever view state it renders
     * (selected path, active path, checked paths): the skin deliberately does not
     * rebuild cells when that state changes.
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

    // ==================== Converter ====================

    private final ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<>(this, "converter");

    /**
     * Converts an item value to its display text. When {@code null} the view
     * falls back to {@code String.valueOf(value)}. A {@code null} value, or a
     * converter that returns {@code null}, yields the empty string. Items do not
     * store text; this is the single source of the visible node text, used by the
     * built-in cell and by the field's default path text. Only {@code toString}
     * is used; {@code fromString} is never called.
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

    // ==================== Children Loader ====================

    private final ObjectProperty<Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>>>
            childrenLoader = new SimpleObjectProperty<>(this, "childrenLoader");

    /**
     * Optional asynchronous loader used when an unloaded branch is expanded or
     * checked. The returned stage should complete with the loaded child items.
     * A {@code null} stage is treated as an empty successful result (the branch
     * becomes a loaded leaf), as is a stage that completes with {@code null}
     * children.
     *
     * <p>In multiple-selection mode, checking an unloaded branch resolves it
     * eagerly: the loader runs for that branch and, as results arrive,
     * recursively for its descendant branches (children not marked leaves via
     * {@code leafHint}) until the checked paths resolve to leaves — see
     * {@link #getCheckedPaths()}. Budget the loader for that fan-out when deep
     * lazy trees are checkable.
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
     * fails, either because the loader stage completes exceptionally or because
     * {@code childrenLoader.apply} throws synchronously. A failure delivered as
     * a {@link CompletionException} (the standard async-supplier wrapper) is
     * unwrapped to its cause. The failing item stays a retriable branch (no
     * column is added); expanding it again retries. When {@code null} the
     * failure is silent.
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

    // ==================== Public Operations ====================

    /**
     * Handles normal row activation. Ignored when the item is not currently
     * reachable from {@link #getRootItems()}.
     *
     * @param item item to activate
     */
    public final void activate(CascaderItem<T> item) {
        if (item == null || !isInCurrentTree(item) || isEffectivelyDisabled(item)) {
            return;
        }
        if (getSelectionMode() == SelectionMode.MULTIPLE) {
            if (isLeaf(item)) {
                toggleCheck(item);
            } else {
                expand(item);
            }
            return;
        }
        if (isLeaf(item)) {
            activateLeaf(item);
        } else {
            expand(item);
        }
    }

    private void activateLeaf(CascaderItem<T> leaf) {
        List<CascaderItem<T>> items = pathItems(leaf);
        List<CascaderItem<T>> ancestors = items.subList(0, items.size() - 1);
        if (!activePath.equals(ancestors)) {
            activePath.setAll(ancestors);
            bumpColumnsRevision();
            requestLayout();
        }
        selectedPath.set(new CascaderPath<>(items));
    }

    /**
     * Expands a branch item. Ignored when the item is not currently reachable
     * from {@link #getRootItems()}.
     *
     * @param item branch item
     */
    public final void expand(CascaderItem<T> item) {
        if (item == null || !isInCurrentTree(item) || isEffectivelyDisabled(item) || isLeaf(item)) {
            return;
        }
        // Three-step order: (1) establish the loading state so the loading frontier
        // is deferred (shouldAddColumn returns false while LOADING, so no premature
        // empty column), (2) retarget the active path and bump the columns revision,
        // (3) only then invoke the loader. Invoking last means a loader that
        // completes or fails inline runs its completion (and any error callback)
        // after the active path is in place, matching the async path and avoiding a
        // later setAll clobbering it.
        long token = startLoad(item);
        if (token != NO_LOAD && (item.getLoadToken() != token || !liveLoads.contains(item))) {
            // startLoad's LOADING state write fired a listener that re-entered and
            // cancelled or superseded this load (reload / loader swap / root change,
            // possibly followed by a fresh load of the same item with a new token).
            // Respect that instead of resurrecting the active path here.
            return;
        }
        activePath.setAll(pathItems(item));
        bumpColumnsRevision();
        if (token != NO_LOAD) {
            runLoad(item, token);
        }
        requestLayout();
    }

    /**
     * Programmatically sets the single selection to the path ending at the given
     * leaf. Applies only in single-selection mode; ignored in multiple mode, or
     * when the item is {@code null}, effectively disabled, or not a leaf. Unlike
     * {@link #activate} it never expands a branch — it is the programmatic
     * counterpart of clicking a leaf in single-selection mode. Like
     * {@code TreeView}'s selection model, this method may keep a selection whose
     * item is not currently reachable from the roots; the columns are not navigated
     * here, and {@link #revealSelectedPath()} (invoked when the popup opens) expands
     * to the selection only when the leaf is reachable.
     *
     * @param leaf leaf item to select
     */
    public final void select(CascaderItem<T> leaf) {
        if (getSelectionMode() == SelectionMode.MULTIPLE
                || leaf == null || isEffectivelyDisabled(leaf) || !isLeaf(leaf)) {
            return;
        }
        selectedPath.set(createPath(leaf));
    }

    private int scrollToSelectionRevision;

    /**
     * Navigates the columns to the current single selection so a freshly opened
     * popup reveals it: the selected path's ancestor branches become the active
     * path (expanding their columns and highlighting them), which brings the
     * selected leaf into view with its selection mark, and bumps
     * {@link #getScrollToSelectionRevision()} so the skin scrolls the columns to the
     * selected rows even in tall columns. Since it reuses the path's own item
     * instances, no lazy loading is triggered. A no-op when there is no selected
     * path or the selection is no longer in the current tree.
     */
    public final void revealSelectedPath() {
        CascaderPath<T> path = getSelectedPath();
        if (path == null) {
            return;
        }
        List<CascaderItem<T>> items = path.getItems();
        if (items.isEmpty() || !isInCurrentTree(path.getLeaf())) {
            return;
        }
        activePath.setAll(items.subList(0, items.size() - 1));
        scrollToSelectionRevision++;
        bumpColumnsRevision();
        requestLayout();
    }

    /**
     * Monotonic counter bumped by {@link #revealSelectedPath()}. It is read-only and
     * non-destructive: the skin remembers the last value it acted on and scrolls its
     * columns to the selection when the counter advances, so a freshly revealed
     * selection is visible instead of left below the fold in tall columns. Reading
     * it never changes state, so it cannot disturb a pending scroll.
     *
     * @return current reveal-scroll revision counter
     */
    public final int getScrollToSelectionRevision() {
        return scrollToSelectionRevision;
    }

    /**
     * Toggles a check state in multiple-selection mode. Ignored when the item is
     * not currently reachable from {@link #getRootItems()}.
     *
     * @param item item to toggle
     */
    public final void toggleCheck(CascaderItem<T> item) {
        if (item == null || !isInCurrentTree(item) || getSelectionMode() != SelectionMode.MULTIPLE) {
            return;
        }
        setCheckedCascade(item, !areEnabledLeavesChecked(item));
    }

    /**
     * Sets a cascading check state. Applies only in multiple-selection mode;
     * ignored in single mode, or when the item is {@code null} or effectively
     * disabled, or not currently reachable from {@link #getRootItems()}.
     *
     * <p>Targeting an unresolved lazy branch records the intent, starts (or
     * reuses) the branch's load, and replays the check once the children
     * arrive; checking with {@code true} keeps resolving recursively until the
     * checked paths reach leaves (see {@link #childrenLoaderProperty()}).
     *
     * @param item item to update
     * @param checked target checked state
     */
    public final void setCheckedCascade(CascaderItem<T> item, boolean checked) {
        if (getSelectionMode() != SelectionMode.MULTIPLE
                || item == null || !isInCurrentTree(item) || isEffectivelyDisabled(item)) {
            return;
        }
        if (isUnresolvedLazyBranch(item)) {
            recordPendingCheckAndLoad(item, checked);
            updateUp(item.getParent());
            refreshCheckedPaths();
            requestLayout();
            return;
        }
        applyDown(item, checked);
        updateUp(item.getParent());
        refreshCheckedPaths();
        requestLayout();
    }

    /**
     * Seeds an initial multiple-selection checked state: marks each given item
     * checked — a leaf directly, a branch by cascading down to its enabled
     * descendants — rolls the tri-state up to ancestors, and refreshes the checked
     * paths once. Use this instead of writing item state directly (an item's
     * checked state is read-only); for runtime check changes use
     * {@link #setCheckedCascade}. It is the multiple-selection counterpart of
     * {@link #select} and may be called before or after switching to
     * {@link SelectionMode#MULTIPLE} — a seed made before the switch survives it.
     *
     * <p>An effectively-disabled branch is ignored as a whole (the seed is a
     * no-op for its entire subtree); to pre-seed a locked subtree, pass its
     * leaves individually — a disabled leaf given directly is honored.
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
        if (items == null) {
            return;
        }
        for (CascaderItem<T> item : items) {
            if (item == null) {
                continue;
            }
            if (isLeaf(item)) {
                // Set the leaf directly so even a disabled (locked) leaf can be
                // seeded as pre-checked.
                item.setChecked(true);
                item.setIndeterminate(false);
            } else {
                // A branch must be cascaded down to stay self-consistent; a bare
                // set would show it checked while its leaves and the checked paths
                // disagree, and the next rollup would silently drop the seed.
                applyDown(item, true);
            }
        }
        for (CascaderItem<T> item : items) {
            if (item != null) {
                updateUp(item.getParent());
            }
        }
        refreshCheckedPaths();
        requestLayout();
    }

    /**
     * Forces a same-source reload of the whole lazy tree: every root is reset to
     * an unloaded branch, navigation and selection are cleared, and children are
     * lazily fetched again with the current loader. In eager mode (no loader set)
     * this is a no-op.
     */
    public final void reload() {
        if (getChildrenLoader() == null) {
            return;
        }
        resetTree();
    }

    /**
     * Clears both single and multiple selection state.
     */
    public final void clearSelection() {
        clearSingleSelection();
        clearMultipleSelection();
        requestLayout();
    }

    private void clearSingleSelection() {
        selectedPath.set(null);
    }

    private void clearMultipleSelection() {
        // Drop any deferred check intent on in-flight branches so a completion
        // does not replay a check that was just cleared; the loads themselves
        // continue, only their pending check is dropped.
        for (CascaderItem<T> item : liveLoads) {
            item.setPendingCheck(null);
        }
        for (CascaderItem<T> root : rootItems) {
            clearCheckState(root);
        }
        checkedPaths.clear();
    }

    /**
     * Returns whether an item is effectively disabled.
     *
     * @param item item to test
     * @return {@code true} if disabled directly or by an ancestor
     */
    public final boolean isEffectivelyDisabled(CascaderItem<T> item) {
        if (item == null) {
            return false;
        }
        return item.isDisable() || isEffectivelyDisabled(item.getParent());
    }

    /**
     * Returns whether an item is a leaf.
     *
     * @param item item to test
     * @return {@code true} if leaf
     */
    public final boolean isLeaf(CascaderItem<T> item) {
        if (item == null) {
            return true;
        }
        Boolean hint = item.getLeafHint();
        if (hint != null) {
            return hint;
        }
        if (getChildrenLoader() == null) {
            return item.getChildren().isEmpty();
        }
        return item.getLoadState() == LoadState.LOADED && item.getChildren().isEmpty();
    }

    /**
     * Whether this is a lazy branch whose children are not yet available: a
     * loader is present, the item is unloaded, childless, and not declared a
     * leaf. A check on such a node is recorded as pending and replayed once the
     * children arrive. Unlike {@link #needsLoad}, this stays {@code true} while
     * the node is already loading, so a later check still overwrites the pending
     * intent instead of being ignored.
     *
     * @param item item to test
     * @return {@code true} if the children are not yet resolved
     */
    private boolean isUnresolvedLazyBranch(CascaderItem<T> item) {
        return getChildrenLoader() != null
                && item.getLoadState() != LoadState.LOADED
                && item.getChildren().isEmpty()
                && !Boolean.TRUE.equals(item.getLeafHint());
    }

    /**
     * Whether expanding or checking this item should start a new lazy load: an
     * unresolved lazy branch that is not already loading.
     *
     * @param item item to test
     * @return {@code true} if a lazy load should be started
     */
    private boolean needsLoad(CascaderItem<T> item) {
        return isUnresolvedLazyBranch(item) && item.getLoadState() != LoadState.LOADING;
    }

    /**
     * Starts loading children for an unloaded branch when a loader is present.
     * Ignored when the item is not currently reachable from {@link #getRootItems()}.
     *
     * @param item branch item to load
     */
    public final void loadChildren(CascaderItem<T> item) {
        if (item == null || !isInCurrentTree(item)) {
            return;
        }
        long token = startLoad(item);
        if (token != NO_LOAD) {
            runLoad(item, token);
        }
    }

    /**
     * Establishes the loading state for a branch that needs a lazy load: assigns
     * a fresh token, registers the item in {@link #liveLoads}, and transitions it
     * to {@link LoadState#LOADING} in a single state write, without yet invoking
     * the loader. Returning the token lets the caller defer the actual
     * {@link #runLoad} until after navigation is in place (see {@link #expand}).
     *
     * @param item branch item to load
     * @return the load token, or {@link #NO_LOAD} if no load is needed
     */
    private long startLoad(CascaderItem<T> item) {
        if (item == null || !needsLoad(item)) {
            return NO_LOAD;
        }
        long token = ++nextLoadToken;
        item.setLoadToken(token);
        liveLoads.add(item);
        item.setLoadState(LoadState.LOADING);
        return token;
    }

    /**
     * Invokes the loader and routes its result (or failure) to
     * {@link #completeLoad}. Must be paired with a prior {@link #startLoad} that
     * returned {@code token}.
     */
    private void runLoad(CascaderItem<T> item, long token) {
        Function<CascaderItem<T>, CompletionStage<List<CascaderItem<T>>>> loader = getChildrenLoader();
        if (loader == null || !liveLoads.contains(item) || item.getLoadToken() != token) {
            // A listener that ran during the active-path update between startLoad
            // and here (reload(), a loader swap, or a root change) already
            // canceled this load (removing it from liveLoads); do not invoke the
            // possibly side-effecting loader for a superseded request.
            return;
        }

        CompletionStage<List<CascaderItem<T>>> stage;
        try {
            stage = loader.apply(item);
        } catch (RuntimeException e) {
            // A synchronous throw routes to the same failure path as a stage
            // error: a retriable branch plus the error callback, never rethrown.
            completeLoad(item, token, null, e);
            return;
        }

        if (stage == null) {
            completeLoad(item, token, Collections.emptyList(), null);
            return;
        }
        stage.whenComplete((children, error) ->
                runOnFxThread(() -> completeLoad(item, token, children, error)));
    }

    /**
     * Creates an immutable path snapshot for an item.
     *
     * @param item leaf or branch item
     * @return path snapshot
     */
    public final CascaderPath<T> createPath(CascaderItem<T> item) {
        return new CascaderPath<>(pathItems(item));
    }

    // ==================== State helpers ====================

    /**
     * Optimistically applies a check to an unresolved lazy branch and starts its
     * load: records the pending intent (replayed by {@link #completeLoad} once the
     * children arrive), reflects the check immediately, and kicks off the loader.
     * Starting is a no-op when a load is already in flight, so a later check still
     * overwrites the pending value to honor the user's latest action.
     *
     * @param item    unresolved lazy branch
     * @param checked target checked state to record and replay
     */
    private void recordPendingCheckAndLoad(CascaderItem<T> item, boolean checked) {
        item.setPendingCheck(checked);
        item.setChecked(checked);
        item.setIndeterminate(false);
        loadChildren(item);
    }

    private void applyDown(CascaderItem<T> item, boolean checked) {
        if (isEffectivelyDisabled(item)) {
            return;
        }
        if (isUnresolvedLazyBranch(item)) {
            // Cascading "unchecked" into a never-checked branch with no load in
            // flight is already satisfied: recording it would start a load (and,
            // through the completion replay, recursively fetch the whole subtree)
            // only to apply a no-op. A checked or in-flight branch still records
            // the intent so the latest action wins over a pending or
            // loader-supplied check; a directly targeted branch goes through
            // setCheckedCascade's own lazy path, which always records.
            if (checked || item.getLoadState() == LoadState.LOADING || item.isChecked()) {
                recordPendingCheckAndLoad(item, checked);
            }
            return;
        }
        // Snapshot: a checked listener fired from the cascade may re-enter a
        // structural reset that clears this children list mid-iteration.
        for (CascaderItem<T> child : List.copyOf(item.getChildren())) {
            applyDown(child, checked);
        }
        if (isLeaf(item)) {
            item.setChecked(checked);
            item.setIndeterminate(false);
        } else {
            updateFromChildren(item);
        }
    }

    /**
     * Replays a pending check onto a branch whose children have just been set to
     * their final result but whose state is still {@link LoadState#LOADING} (it is
     * set to LOADED only after the replay, so a re-entrant loadState listener keeps
     * the final say). The loadState-keyed {@link #isLeaf} / {@link
     * #isUnresolvedLazyBranch} tests cannot drive the replay in this window: they
     * read a populated branch as unresolved and an empty result as not-yet-loaded.
     * The children are final, so discriminate on them directly — an empty result is
     * the branch's own (new) leaf unless {@code leafHint} forces a branch, otherwise
     * cascade into the children, which carry their own independent load state.
     *
     * <p>The cascade iterates a snapshot and re-reads the pending intent around
     * each child: a checked listener that re-enters a structural reset or
     * {@link #clearSelection} revokes the intent (directly or via
     * {@link #cancelInFlight}), which aborts the remaining replay instead of
     * overwriting the listener's outcome or tripping over the mutated children.
     *
     * <p>A branch that became effectively disabled while loading cannot be
     * half-honored: its optimistic check is rolled back, mirroring the failure
     * path (the caller's updateUp / refreshCheckedPaths repair the ancestors).
     *
     * @param item    just-loaded branch
     * @param checked target checked state to replay
     */
    private void replayResolvedCheck(CascaderItem<T> item, boolean checked) {
        if (isEffectivelyDisabled(item)) {
            item.setChecked(false);
            item.setIndeterminate(false);
            return;
        }
        for (CascaderItem<T> child : List.copyOf(item.getChildren())) {
            if (item.getPendingCheck() == null) {
                return;
            }
            applyDown(child, checked);
        }
        if (item.getPendingCheck() == null) {
            return;
        }
        if (item.getChildren().isEmpty()) {
            if (Boolean.FALSE.equals(item.getLeafHint())) {
                // A forced branch with zero children cannot be checked — the same
                // rule the eager rollup applies — or one later toggle would drop
                // it into an unrecoverable unchecked state.
                updateFromChildren(item);
            } else {
                item.setChecked(checked);
                item.setIndeterminate(false);
            }
        } else {
            updateFromChildren(item);
        }
    }

    private void updateUp(CascaderItem<T> item) {
        CascaderItem<T> current = item;
        while (current != null) {
            updateFromChildren(current);
            current = current.getParent();
        }
    }

    /**
     * Rolls a branch's tri-state up from its children using integer counts. The
     * total intentionally includes disabled children — an unchecked disabled child
     * keeps an ancestor indeterminate — and only the cascade-down
     * ({@link #applyDown}) and toggle ({@link #enabledLeafSummary}) paths exclude
     * disabled. A branch is checked only when every child is fully checked, and
     * indeterminate when there is a partial signal without a full one, so it is
     * never both.
     */
    private void updateFromChildren(CascaderItem<T> item) {
        int total = item.getChildren().size();
        int fullyChecked = 0;
        int indeterminate = 0;
        for (CascaderItem<T> child : item.getChildren()) {
            if (child.isIndeterminate()) {
                indeterminate++;
            } else if (child.isChecked()) {
                fullyChecked++;
            }
        }
        boolean checked = total > 0 && fullyChecked == total;
        item.setChecked(checked);
        item.setIndeterminate(total > 0 && !checked && (fullyChecked > 0 || indeterminate > 0));
    }

    private boolean areEnabledLeavesChecked(CascaderItem<T> item) {
        EnabledLeafSummary summary = enabledLeafSummary(item);
        return summary.hasLeaf && summary.allChecked;
    }

    private EnabledLeafSummary enabledLeafSummary(CascaderItem<T> item) {
        if (isEffectivelyDisabled(item)) {
            return new EnabledLeafSummary(false, true);
        }
        if (isLeaf(item) || isUnresolvedLazyBranch(item)) {
            return new EnabledLeafSummary(true, item.isChecked());
        }

        boolean hasLeaf = false;
        boolean allChecked = true;
        for (CascaderItem<T> child : item.getChildren()) {
            EnabledLeafSummary childSummary = enabledLeafSummary(child);
            if (childSummary.hasLeaf) {
                hasLeaf = true;
                if (!childSummary.allChecked) {
                    allChecked = false;
                }
            }
        }
        return new EnabledLeafSummary(hasLeaf, allChecked);
    }

    private static final class EnabledLeafSummary {

        private final boolean hasLeaf;
        private final boolean allChecked;

        private EnabledLeafSummary(boolean hasLeaf, boolean allChecked) {
            this.hasLeaf = hasLeaf;
            this.allChecked = allChecked;
        }
    }

    /**
     * A {@link NullPointerException} if the loader result contains a {@code null}
     * child (an illegal result routed to the failure path), otherwise {@code null}.
     */
    private Throwable firstNullChildError(List<CascaderItem<T>> children) {
        if (children == null) {
            return null;
        }
        for (CascaderItem<T> child : children) {
            if (child == null) {
                return new NullPointerException("child item must not be null");
            }
        }
        return null;
    }

    private void completeLoad(CascaderItem<T> item, long token,
                              List<CascaderItem<T>> children, Throwable error) {
        // Stale completion (token mismatch) or already-cancelled load (no longer a
        // member): bail. Token first so a stale completion never evicts a newer
        // live load for the same item; check membership WITHOUT removing yet,
        // because the success path re-validates after children.setAll (which can
        // re-enter via a list listener, e.g. user code calling reload()).
        if (item.getLoadToken() != token || !liveLoads.contains(item)) {
            return;
        }

        if (!isInCurrentTree(item)) {
            // The branch left the current tree (a deep ancestor list was mutated
            // outside the supported reset points) while loading: drop the result
            // and return to a stable, retriable state rather than populating a
            // detached subtree.
            liveLoads.remove(item);
            item.setLoadState(LoadState.NOT_LOADED);
            item.setPendingCheck(null);
            return;
        }

        // Async loaders surface a throwing supplier as a CompletionException;
        // hand the error callback the root cause, not the framework wrapper.
        Throwable resolvedError = error;
        if (resolvedError instanceof CompletionException && resolvedError.getCause() != null) {
            resolvedError = resolvedError.getCause();
        }
        // A loader result carrying a null child is a loader-contract violation:
        // treat it exactly like a load failure. Detecting it here (rather than
        // letting the null-rejecting children list throw mid-populate) keeps the
        // branch retriable instead of stranding it in LOADING with an uncaught NPE.
        if (resolvedError == null) {
            resolvedError = firstNullChildError(children);
        }

        if (resolvedError != null) {
            // FAILED is retriable (re-expanding reloads). Consume this load's
            // pending intent and roll back the optimistic check BEFORE surfacing
            // FAILED, so a reentrant retry from the loadState listener starts
            // clean: a plain expand carries no intent (must not resurrect the
            // failed check), while an explicit re-check (setCheckedCascade)
            // records its own fresh intent. The failure is surfaced through the
            // callback afterward.
            liveLoads.remove(item);
            Boolean pendingCheck = item.getPendingCheck();
            item.setPendingCheck(null);
            if (pendingCheck != null) {
                item.setChecked(false);
                item.setIndeterminate(false);
                updateUp(item.getParent());
                refreshCheckedPaths();
            }
            item.setLoadState(LoadState.FAILED);
            BiConsumer<CascaderItem<T>, Throwable> handler = getOnChildrenLoadError();
            if (handler != null) {
                handler.accept(item, resolvedError);
            }
            requestLayout();
            return;
        }

        // Success: populate children first. children.setAll can fire a list
        // listener that re-enters (for example user code calling reload(), which
        // clears the children and resets the state). Re-validate token + membership
        // afterward, but keep the item registered until the replay below is done,
        // so the whole replay window stays cancellable by a re-entrant reset.
        List<CascaderItem<T>> loadedChildren = children == null ? Collections.emptyList() : children;
        item.getChildren().setAll(loadedChildren);
        if (item.getLoadToken() != token || !liveLoads.contains(item)) {
            return;
        }
        // Apply any pending check to the now-final children, then make LOADED the
        // last mutation. Replaying first means a re-entrant loadState listener
        // (reload / clearSelection) runs after it and has the final say, instead of
        // being overwritten by it. The replay must not route through applyDown here:
        // while the state is still LOADING the loadState-keyed isLeaf /
        // isUnresolvedLazyBranch tests misread the branch as unresolved (and an
        // empty result as unloaded), which would re-record the pending intent rather
        // than consume it. replayResolvedCheck trusts the now-final children instead.
        // The intent is consumed only after the replay: while the replay runs it
        // doubles as the liveness ticket that a re-entrant clearSelection or
        // cancelInFlight revokes to abort the remaining replay.
        Boolean pendingCheck = item.getPendingCheck();
        if (pendingCheck != null) {
            replayResolvedCheck(item, pendingCheck);
            updateUp(item.getParent());
            item.setPendingCheck(null);
        }
        // The deferred liveLoads removal is the LOADED commit gate: a checked /
        // indeterminate listener that re-entered a structural reset (reload, a
        // loader swap, a root change) during the replay went through
        // cancelInFlight, which evicted this item — the reset then owns the final
        // state and LOADED must not be pinned over it.
        if (item.getLoadToken() != token || !liveLoads.remove(item)) {
            return;
        }
        item.setLoadState(LoadState.LOADED);
        if (pendingCheck != null) {
            // After LOADED so an empty-loaded branch — now a leaf — is counted;
            // before it, isLeaf() reports false and refreshCheckedPaths drops it.
            refreshCheckedPaths();
        }
        // Bump after children and any replayed check state are final, so the
        // skin's re-sync renders the newly appearing column in its correct state.
        bumpColumnsRevision();
        requestLayout();
    }

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void clearCheckState(CascaderItem<T> item) {
        item.setChecked(false);
        item.setIndeterminate(false);
        for (CascaderItem<T> child : item.getChildren()) {
            clearCheckState(child);
        }
    }

    // ==================== Reset and invalidation ====================

    /**
     * Cancels all in-flight loads: each in-flight node is reset to the stable,
     * retriable {@link LoadState#NOT_LOADED} and its pending check is cleared, so
     * late completions bail in {@link #completeLoad} (they are no longer in
     * {@link #liveLoads}). The set is snapshotted and cleared <em>before</em> any
     * state change fires, so a listener that re-enters (for example calling
     * {@link #reload()} from a {@code loadState} listener) sees an already
     * invalidated set and cannot corrupt the iteration.
     *
     * <p>A dropped pending check on a node that stays an unresolved lazy branch
     * is rolled back like the failure path — the intent can never be replayed, so
     * leaving the optimistic check would strand a checked parent over unchecked
     * children once the branch is eventually loaded. When the loader was just
     * cleared (eager switch) the node is about to become an eager leaf and keeps
     * its check: {@link #isUnresolvedLazyBranch} is false without a loader.
     */
    private void cancelInFlight() {
        if (liveLoads.isEmpty()) {
            return;
        }
        List<CascaderItem<T>> snapshot = new ArrayList<>(liveLoads);
        liveLoads.clear();
        for (CascaderItem<T> item : snapshot) {
            Boolean pendingCheck = item.getPendingCheck();
            item.setPendingCheck(null);
            item.setLoadState(LoadState.NOT_LOADED);
            if (pendingCheck != null && isUnresolvedLazyBranch(item)) {
                item.setChecked(false);
                item.setIndeterminate(false);
                updateUp(item.getParent());
            }
        }
    }

    /**
     * Shared invalidation core for the three reset entry points: cancels
     * in-flight loads and clears navigation plus single selection. It
     * intentionally leaves {@code checkedPaths} and each item's checked state
     * alone — those are handled per entry point.
     */
    private void clearNavAndPending() {
        cancelInFlight();
        activePath.clear();
        bumpColumnsRevision();
        selectedPath.set(null);
    }

    /**
     * Full-tree reset shared by {@link #reload()} and switching to a non-null
     * loader: clears navigation and in-flight loads, then discards everything
     * below the same roots and all check state so the tree returns to a blank
     * slate ready to lazily reload.
     */
    private void resetTree() {
        clearNavAndPending();
        for (CascaderItem<T> root : rootItems) {
            clearCheckState(root);
            root.getChildren().clear();
            root.setLoadState(LoadState.NOT_LOADED);
        }
        checkedPaths.clear();
        requestLayout();
    }

    /**
     * Switches to eager mode when the loader is cleared: cancels in-flight loads
     * but keeps the current tree, navigation, and item check state as a static
     * tree. The derived checked paths are recomputed because clearing the loader
     * changes which nodes are leaves.
     */
    private void switchToEager() {
        cancelInFlight();
        refreshCheckedPaths();
        bumpColumnsRevision();
        requestLayout();
    }

    private void refreshCheckedPaths() {
        List<CascaderPath<T>> paths = new ArrayList<>();
        for (CascaderItem<T> root : rootItems) {
            collectCheckedLeafPaths(root, paths);
        }
        // Skip the setAll when the resolved set is unchanged (path equality is
        // the identity chain): many operations re-derive an identical set, and
        // an unconditional setAll would fire a spurious full-replace event on
        // every one of them.
        if (!paths.equals(checkedPaths)) {
            checkedPaths.setAll(paths);
        }
    }

    private void collectCheckedLeafPaths(CascaderItem<T> item, List<CascaderPath<T>> paths) {
        if (isLeaf(item)) {
            if (item.isChecked()) {
                paths.add(createPath(item));
            }
            return;
        }
        for (CascaderItem<T> child : item.getChildren()) {
            collectCheckedLeafPaths(child, paths);
        }
    }

    private List<CascaderItem<T>> pathItems(CascaderItem<T> item) {
        List<CascaderItem<T>> path = new ArrayList<>();
        CascaderItem<T> current = item;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        return path;
    }

    /**
     * Whether the item is still reachable from the current roots: its topmost
     * ancestor is one of {@link #getRootItems()}. Used to drop a lazy load that
     * completes after its branch was detached from the tree.
     */
    private boolean isInCurrentTree(CascaderItem<T> item) {
        if (item == null) {
            return false;
        }
        CascaderItem<T> current = item;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return rootItems.contains(current);
    }

    // ==================== CSS Metadata ====================

    private static final class StyleableProperties {

        private static final CssMetaData<CascaderView<?>, Number> COLUMN_WIDTH =
                new CssMetaData<>("-fx-column-width", SizeConverter.getInstance(), DEFAULT_COLUMN_WIDTH) {
                    @Override
                    public boolean isSettable(CascaderView<?> view) {
                        return !view.columnWidth.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(CascaderView<?> view) {
                        return (StyleableProperty<Number>) view.columnWidthProperty();
                    }
                };

        private static final CssMetaData<CascaderView<?>, Number> ROW_HEIGHT =
                new CssMetaData<>("-fx-row-height", SizeConverter.getInstance(), DEFAULT_FIXED_CELL_SIZE) {
                    @Override
                    public boolean isSettable(CascaderView<?> view) {
                        return !view.rowHeight.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(CascaderView<?> view) {
                        return (StyleableProperty<Number>) view.rowHeightProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, COLUMN_WIDTH, ROW_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns the CSS metadata associated with this class.
     *
     * @return the CSS metadata
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
