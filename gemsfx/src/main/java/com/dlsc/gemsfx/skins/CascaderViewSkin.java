package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CascaderCell;
import com.dlsc.gemsfx.CascaderItem;
import com.dlsc.gemsfx.CascaderItem.LoadState;
import com.dlsc.gemsfx.CascaderPath;
import com.dlsc.gemsfx.CascaderView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.PopupWindow;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * Default skin for {@link CascaderView}. Renders the active path as one
 * {@link ListView} per column inside a horizontal box; cells come from
 * {@link CascaderView#cellFactoryProperty()} or default to
 * {@link CascaderCell}.
 *
 * @param <T> application value type
 */
public class CascaderViewSkin<T> extends GemsSkinBase<CascaderView<T>> {

    // ==================== Constants ====================

    private static final String COLUMN_STYLE_CLASS = "column";
    private static final String EMPTY_PLACEHOLDER_STYLE_CLASS = "placeholder";
    private static final int MIN_VISIBLE_ROW_COUNT = 1;

    // ==================== Nodes ====================

    private final HBox columnsBox = new HBox();
    private final List<ListView<CascaderItem<T>>> columns = new ArrayList<>();

    // Last reveal-scroll revision this skin acted on; the one-shot "already
    // scrolled" state lives here, not on the view, so reading the view's counter
    // stays non-destructive. Starts at 0 so a reveal that ran before this skin
    // existed (first popup show) is still honored on the first layout.
    private int lastScrollToSelectionRevision;

    // Column the keyboard navigation currently acts on; -1 = no keyboard focus.
    // The row lives in that column ListView's own focus model, which the cells
    // render via the :focused pseudo class.
    private int keyboardColumn = -1;

    // ==================== Constructor ====================

    /**
     * Creates a skin for the given view.
     *
     * @param control the skinnable view
     */
    public CascaderViewSkin(CascaderView<T> control) {
        super(control);
        columnsBox.getStyleClass().add("columns");
        getChildren().setAll(columnsBox);
        registerListeners(control);
        syncColumns();
    }

    private void registerListeners(CascaderView<T> control) {
        // Column structure: one explicit signal from the control replaces the old
        // active-path / root-list / loader listeners and the frontier monitor.
        register(control.columnsRevisionProperty(), observable -> syncColumns());
        // Selection / navigation / checked-path / mode changes are observed by
        // the cells themselves (see CascaderCell), so no ListView.refresh()
        // here — refresh rebinds every cell (an updateItem storm that rebuilds
        // custom content and flickers). The mode switch only needs a layout
        // pass for the check-box slot swap.
        register(control.selectionModeProperty(), observable -> control.requestLayout());
        register(control.visibleRowCountProperty(), observable -> control.requestLayout());
        register(control.columnWidthProperty(), observable -> applyColumnSizing());
        register(control.rowHeightProperty(), observable -> applyColumnSizing());
        register(control.converterProperty(), observable -> refreshColumns());
        register(control.emptyTextProperty(), observable -> applyEmptyText());
        // A new cell factory changes the cell type, so every column must be rebuilt;
        // the tail-diff reuses by backing-list identity and would keep stale cells.
        register(control.cellFactoryProperty(), observable -> rebuildAllColumns());
        // Inline keyboard navigation: key events only reach this handler when the
        // view itself is the focus owner (inline use; inside the Cascader popup
        // the owner window keeps key focus and CascaderSkin routes instead).
        registerHandler(control, KeyEvent.KEY_PRESSED, event -> {
            if (handleNavigationKey(event)) {
                event.consume();
            }
        });
        // List-like focus behavior: clicking the inline view focuses it. Inside a
        // popup the click must not move the focus owner of the popup scene — the
        // anchored control keeps driving the keyboard. A capture-phase filter for
        // the same reason as the column highlight sync: the multiple-mode check
        // box consumes the press in its own filter, which would starve a bubbling
        // handler and leave the inline view unfocused after a checkbox click
        // (arrow keys would then never reach it).
        registerFilter(control, MouseEvent.MOUSE_PRESSED, event -> {
            if (control.getScene() != null
                    && !(control.getScene().getWindow() instanceof PopupWindow)) {
                control.requestFocus();
            }
        });
    }

    private void applyEmptyText() {
        String text = getSkinnable().getEmptyText();
        for (ListView<CascaderItem<T>> column : columns) {
            if (column.getPlaceholder() instanceof Label) {
                Label label = (Label) column.getPlaceholder();
                label.setText(text);
            }
        }
    }

    // ==================== Columns ====================

    /**
     * Re-syncs the rendered columns to the desired set with a keep-prefix /
     * replace-tail diff: columns whose backing list is unchanged (by identity) are
     * reused, only the changed tail is disposed and rebuilt, and the ordinal style
     * classes are restamped by position. This avoids the flicker and defensive CSS
     * re-pass of a full teardown.
     */
    private void syncColumns() {
        List<ObservableList<CascaderItem<T>>> desired = desiredBackingLists();
        int keep = 0;
        while (keep < columns.size() && keep < desired.size()
                && columns.get(keep).getItems() == desired.get(keep)) {
            keep++;
        }
        boolean changed = keep < columns.size() || keep < desired.size();
        for (int i = columns.size() - 1; i >= keep; i--) {
            disposeColumn(columns.remove(i));
        }
        for (int i = keep; i < desired.size(); i++) {
            columns.add(createColumn(desired.get(i)));
        }
        restampOrdinals();
        columnsBox.getChildren().setAll(columns);
        // Keyboard focus rows live on the column focus models (rebuilt tail
        // columns lose theirs naturally); only the active column index needs
        // clamping when the tail was rebuilt away.
        if (keyboardColumn >= columns.size()) {
            keyboardColumn = columns.size() - 1;
        }
        // Reused prefix columns need no poke: their cells observe the view-level
        // state (active path, selection, checked paths) themselves and have
        // already re-rendered on the change that bumped the revision.
        // Only newly created columns need a CSS pass (so author CSS overrides the
        // code defaults before pref measurement); reused columns already have it.
        if (changed && getSkinnable().getScene() != null) {
            columnsBox.applyCss();
            // Settle the freshly created skins in the same event: applyCss builds
            // them un-laid-out (the empty-column placeholder label sits at
            // negative y, scroll bars at their default size). Inside the popup
            // those junk bounds would otherwise survive until the next pulse,
            // where PopupWindow's native content-bounds tracking renders them as
            // a visible window jump.
            columnsBox.layout();
        }
    }

    /**
     * The backing lists, in order, that the rendered columns should show: the root
     * items, then the children of each active-path branch that should get a column.
     */
    private List<ObservableList<CascaderItem<T>>> desiredBackingLists() {
        List<ObservableList<CascaderItem<T>>> lists = new ArrayList<>();
        lists.add(getSkinnable().getRootItems());
        for (CascaderItem<T> item : getSkinnable().getActivePath()) {
            if (shouldAddColumn(item)) {
                lists.add(item.getChildren());
            }
        }
        return lists;
    }

    /**
     * Whether the children of an active-path branch should get a column. A
     * loading frontier is deferred (no column until it finishes), and a lazy
     * branch that failed or has not loaded yet shows no empty column. Only an
     * eager branch, a loaded lazy branch, or one with already-attached children
     * gets a column.
     */
    private boolean shouldAddColumn(CascaderItem<T> item) {
        CascaderView<T> view = getSkinnable();
        if (view.isLeaf(item) || item.getLoadState() == LoadState.LOADING) {
            return false;
        }
        return view.getChildrenLoader() == null
                || item.getLoadState() == LoadState.LOADED
                || !item.getChildren().isEmpty();
    }

    private ListView<CascaderItem<T>> createColumn(ObservableList<CascaderItem<T>> items) {
        ListView<CascaderItem<T>> listView = new ListView<>(items);
        listView.getStyleClass().add(COLUMN_STYLE_CLASS);
        listView.setFocusTraversable(false);
        // The cascader view is the selection model; the ListView's own would add
        // a dead per-click row-selection channel (:selected churn on the clicked
        // cells). Inert stand-in rather than null: ListViewBehavior.mousePressed
        // dereferences the model without a null guard.
        listView.setSelectionModel(new NoOpSelectionModel<>());
        // Click-to-focus, unified with the keyboard highlight: the clicked row
        // gets the focus highlight, the clicked column becomes the keyboard
        // column, and every other column drops its focused row — so arrows
        // continue from the clicked row and at most one column shows a
        // highlight. A capture-phase filter, not a bubbling handler: the
        // multiple-mode check box consumes the press in its own filter, which
        // would starve a handler and leave a stale highlight on checkbox
        // clicks; this filter fires on the way down, before that consume, and
        // does not consume anything itself. (The inert selection model above
        // also disabled the stock focus-on-click living inside
        // MultipleSelectionModelBase.)
        listView.addEventFilter(MouseEvent.MOUSE_PRESSED,
                event -> syncKeyboardFocusToPointer(listView, event));
        // Discoverable defaults from the view's -fx-column-width / -fx-row-height;
        // author CSS (-fx-pref-width / -fx-fixed-cell-size) on
        // .cascader-view > .columns > .column still overrides because AUTHOR
        // origin outranks the USER origin of these
        // set calls. min/max are left unset so a single column can be widened via
        // CSS while HBox (hgrow=NEVER) keeps each column at its preferred width.
        listView.setPrefWidth(columnWidthOrDefault());
        listView.setFixedCellSize(rowHeightOrDefault());
        // A column shows this placeholder when its backing list is empty: the
        // always-present root column when there are no root items, or a forced
        // branch (leafHint=false) that resolved to zero children. A loading
        // frontier shows no column and an ordinary loaded-empty branch is a leaf
        // (no column), so those never reach the placeholder.
        Label placeholder = new Label(getSkinnable().getEmptyText());
        placeholder.getStyleClass().add(EMPTY_PLACEHOLDER_STYLE_CLASS);
        listView.setPlaceholder(placeholder);
        Callback<CascaderView<T>, ListCell<CascaderItem<T>>> factory = getSkinnable().getCellFactory();
        listView.setCellFactory(view -> factory != null
                ? factory.call(getSkinnable())
                : new CascaderCell<>(getSkinnable()));
        // Control does not clip its skin children, so a not-yet-laid-out column
        // (placeholder label at negative y, scroll bars at their default size)
        // leaks junk bounds into the ancestor chain — inside the popup that
        // wiggles the native window, which tracks the content bounds. Nothing in
        // a column intentionally renders outside its own bounds, so a plain
        // rectangle clip is safe (0x0 before the first layout kills the leak).
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(listView.widthProperty());
        clip.heightProperty().bind(listView.heightProperty());
        listView.setClip(clip);
        return listView;
    }

    /**
     * Stamps each column's positional ordinal style class ({@code columnN}, no
     * hyphen, matching the chart-style {@code data0} / {@code seriesN} convention),
     * removing any stale ordinal so a tail-diff always leaves lookups and author CSS
     * targeting the column at that position.
     */
    private void restampOrdinals() {
        for (int i = 0; i < columns.size(); i++) {
            ListView<CascaderItem<T>> column = columns.get(i);
            column.getStyleClass().removeIf(CascaderViewSkin::isOrdinalStyleClass);
            column.getStyleClass().add(COLUMN_STYLE_CLASS + i);
        }
    }

    /**
     * Whether a style class is one of our positional ordinals ({@code column}
     * followed by digits). Deliberately excludes the base {@code column} class and
     * any author class that merely shares the prefix (e.g. {@code column-custom}),
     * so restamping never strips those.
     */
    private static boolean isOrdinalStyleClass(String styleClass) {
        if (!styleClass.startsWith(COLUMN_STYLE_CLASS)
                || styleClass.length() == COLUMN_STYLE_CLASS.length()) {
            return false;
        }
        for (int i = COLUMN_STYLE_CLASS.length(); i < styleClass.length(); i++) {
            if (!Character.isDigit(styleClass.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void disposeColumn(ListView<CascaderItem<T>> column) {
        column.setCellFactory(null);
        column.setItems(null);
    }

    private void disposeColumns() {
        for (ListView<CascaderItem<T>> column : columns) {
            disposeColumn(column);
        }
        columnsBox.getChildren().clear();
        columns.clear();
    }

    /**
     * Full rebuild used when the cell factory changes: dispose every column and
     * re-sync so all columns are recreated with the new cell type.
     */
    private void rebuildAllColumns() {
        disposeColumns();
        syncColumns();
    }

    private void refreshColumns() {
        for (ListView<CascaderItem<T>> column : columns) {
            column.refresh();
        }
    }

    /**
     * Re-applies the view's column-width / row-height defaults (USER origin) to the
     * existing columns when those properties change. A column whose size is already
     * set by author CSS (AUTHOR / INLINE origin) is left untouched, so the
     * "author wins" contract holds even for runtime size changes.
     */
    private void applyColumnSizing() {
        for (ListView<CascaderItem<T>> column : columns) {
            if (!cssAuthored(column.prefWidthProperty())) {
                column.setPrefWidth(columnWidthOrDefault());
            }
            if (!cssAuthored(column.fixedCellSizeProperty())) {
                column.setFixedCellSize(rowHeightOrDefault());
            }
        }
        getSkinnable().requestLayout();
    }

    private double columnWidthOrDefault() {
        double width = getSkinnable().getColumnWidth();
        return width > 0.0 ? width : CascaderView.DEFAULT_COLUMN_WIDTH;
    }

    private double rowHeightOrDefault() {
        double height = getSkinnable().getRowHeight();
        return height > 0.0 ? height : CascaderView.DEFAULT_FIXED_CELL_SIZE;
    }

    private static boolean cssAuthored(Object property) {
        if (!(property instanceof StyleableProperty)) {
            return false;
        }
        StyleOrigin origin = ((StyleableProperty<?>) property).getStyleOrigin();
        return origin == StyleOrigin.AUTHOR || origin == StyleOrigin.INLINE;
    }

    // ==================== Keyboard navigation ====================

    /**
     * Handles a column-navigation key: Up / Down move within the current column
     * (skipping disabled items), Right expands into a branch, Left steps back,
     * Home / End jump within the column, and Enter / Space activate the focused
     * item. The first arrow-family key seeds the keyboard focus (on the revealed
     * selection when present, else the first column). Left / Right follow the
     * effective node orientation. Package-private so {@link CascaderSkin} can
     * route keys from the popup owner control; the skin also consumes it
     * directly when the inline view is focused.
     *
     * @param event key event to handle
     * @return whether the event was handled (and should be consumed)
     */
    boolean handleNavigationKey(KeyEvent event) {
        if (event.isShiftDown() || event.isControlDown() || event.isAltDown() || event.isMetaDown()
                || columns.isEmpty()) {
            return false;
        }
        KeyCode code = logicalCode(event.getCode());
        if (keyboardColumn < 0 || keyboardColumn >= columns.size() || focusedRow() < 0) {
            // Only the arrow family seeds: Enter / Space without a keyboard focus
            // keep their host semantics (the popup skin falls back to closing).
            if (isArrowFamily(code)) {
                seedKeyboardFocus();
                return true;
            }
            return false;
        }
        switch (code) {
            case UP:
                return navigateVertical(-1);
            case DOWN:
                return navigateVertical(1);
            case LEFT:
                return navigateBack();
            case RIGHT:
                return navigateInto();
            case HOME:
                return navigateEdge(true);
            case END:
                return navigateEdge(false);
            case ENTER:
            case SPACE:
                return activateFocused();
            default:
                return false;
        }
    }

    /**
     * Makes the clicked column the keyboard column, focuses the clicked row, and
     * drops every other column's highlight. Empty-area presses clear focus entirely.
     */
    private void syncKeyboardFocusToPointer(ListView<CascaderItem<T>> column, MouseEvent event) {
        int index = columns.indexOf(column);
        if (index < 0) {
            return;
        }
        ListCell<?> cell = pressedCell(event);
        if (cell == null || cell.isEmpty()) {
            clearKeyboardFocus();
            return;
        }
        keyboardColumn = index;
        for (int i = 0; i < columns.size(); i++) {
            if (i != index) {
                columns.get(i).getFocusModel().focus(-1);
            }
        }
        column.getFocusModel().focus(cell.getIndex());
    }

    /** The list cell containing the press target, or {@code null} for an empty-area press. */
    private static ListCell<?> pressedCell(MouseEvent event) {
        Node node = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
        while (node != null && !(node instanceof ListCell)) {
            node = node.getParent();
        }
        return (ListCell<?>) node;
    }

    /**
     * Drops the keyboard focus entirely (all columns lose their focused row).
     * The popup skin calls this on every show / hide so each popup session
     * starts without a stale highlight.
     */
    void clearKeyboardFocus() {
        keyboardColumn = -1;
        for (ListView<CascaderItem<T>> column : columns) {
            column.getFocusModel().focus(-1);
        }
    }

    private static boolean isArrowFamily(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT
                || code == KeyCode.RIGHT || code == KeyCode.HOME || code == KeyCode.END;
    }

    /** Mirrors Left / Right under a right-to-left effective orientation. */
    private KeyCode logicalCode(KeyCode code) {
        if (getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            if (code == KeyCode.LEFT) {
                return KeyCode.RIGHT;
            }
            if (code == KeyCode.RIGHT) {
                return KeyCode.LEFT;
            }
        }
        return code;
    }

    /**
     * Seeds the initial keyboard focus: the revealed selection's leaf row in the
     * last column when present, else the first column at its expanded branch row
     * (or first enabled row).
     */
    private void seedKeyboardFocus() {
        CascaderPath<T> selected = getSkinnable().getSelectedPath();
        if (selected != null && selected.getLeaf() != null) {
            int column = columns.size() - 1;
            int row = indexOfIdentity(columns.get(column).getItems(), selected.getLeaf());
            if (row >= 0) {
                focusCell(column, row);
                return;
            }
        }
        ObservableList<CascaderItem<T>> roots = columns.get(0).getItems();
        int row = getSkinnable().getActivePath().isEmpty() ? -1
                : indexOfIdentity(roots, getSkinnable().getActivePath().get(0));
        focusCell(0, row >= 0 ? row : edgeEnabledRow(roots, true));
    }

    private int focusedRow() {
        return columns.get(keyboardColumn).getFocusModel().getFocusedIndex();
    }

    private CascaderItem<T> focusedItem() {
        return columns.get(keyboardColumn).getFocusModel().getFocusedItem();
    }

    /** Focuses one row in one column, clearing every other column's focus. */
    private void focusCell(int columnIndex, int row) {
        keyboardColumn = columnIndex;
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).getFocusModel().focus(i == columnIndex ? row : -1);
        }
        if (row >= 0) {
            columns.get(columnIndex).scrollTo(row);
        }
    }

    /** Moves within the current column, skipping disabled items; clamps at the edges. */
    private boolean navigateVertical(int direction) {
        ObservableList<CascaderItem<T>> items = columns.get(keyboardColumn).getItems();
        for (int i = focusedRow() + direction; i >= 0 && i < items.size(); i += direction) {
            if (!getSkinnable().isEffectivelyDisabled(items.get(i))) {
                focusCell(keyboardColumn, i);
                break;
            }
        }
        return true;
    }

    private boolean navigateEdge(boolean home) {
        int row = edgeEnabledRow(columns.get(keyboardColumn).getItems(), home);
        if (row >= 0) {
            focusCell(keyboardColumn, row);
        }
        return true;
    }

    /** First ({@code home}) or last enabled row of a column, or -1 if none. */
    private int edgeEnabledRow(ObservableList<CascaderItem<T>> items, boolean home) {
        int direction = home ? 1 : -1;
        for (int i = home ? 0 : items.size() - 1; i >= 0 && i < items.size(); i += direction) {
            if (!getSkinnable().isEffectivelyDisabled(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Expands the focused branch and moves the focus into its column when the
     * children are already rendered; a loading frontier keeps the focus in place
     * (press Right again once the column appears). A leaf is a consumed no-op.
     */
    private boolean navigateInto() {
        CascaderItem<T> item = focusedItem();
        if (item == null || getSkinnable().isLeaf(item) || getSkinnable().isEffectivelyDisabled(item)) {
            return true;
        }
        getSkinnable().expand(item);
        int next = keyboardColumn + 1;
        if (next < columns.size() && columns.get(next).getItems() == item.getChildren()) {
            focusCell(next, edgeEnabledRow(item.getChildren(), true));
        }
        return true;
    }

    /** Steps back to the parent column, focusing the expanded ancestor there. */
    private boolean navigateBack() {
        if (keyboardColumn == 0) {
            return true;
        }
        int target = keyboardColumn - 1;
        List<CascaderItem<T>> activePath = getSkinnable().getActivePath();
        int row = target < activePath.size()
                ? indexOfIdentity(columns.get(target).getItems(), activePath.get(target))
                : -1;
        focusCell(target, row >= 0 ? row : edgeEnabledRow(columns.get(target).getItems(), true));
        return true;
    }

    /**
     * Activates the focused item: a leaf goes through
     * {@link CascaderView#activate} (single-selection select, multiple-mode
     * check toggle), a branch expands like Right. Unhandled without a keyboard
     * focus so the host's own Enter semantics apply.
     */
    private boolean activateFocused() {
        CascaderItem<T> item = focusedItem();
        if (item == null) {
            return false;
        }
        if (getSkinnable().isLeaf(item)) {
            getSkinnable().activate(item);
            return true;
        }
        return navigateInto();
    }

    @Override
    public void dispose() {
        disposeColumns();
        super.dispose();
    }

    // ==================== Layout ====================

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        columnsBox.resizeRelocate(x, y, Math.max(0.0, w), Math.max(0.0, h));
        // The columns are now laid out (their ListView skins exist), so a pending
        // reveal can scroll each column to its selected node — doing this earlier
        // (while building columns) would lose the scrollTo before the flow exists.
        // Comparing the view's non-destructive counter against the last one we acted
        // on keeps the one-shot state skin-side.
        int revision = getSkinnable().getScrollToSelectionRevision();
        if (revision != lastScrollToSelectionRevision) {
            lastScrollToSelectionRevision = revision;
            scrollColumnsToSelection();
        }
    }

    private void scrollColumnsToSelection() {
        CascaderPath<T> path = getSkinnable().getSelectedPath();
        if (path == null) {
            return;
        }
        List<CascaderItem<T>> items = path.getItems();
        for (int i = 0; i < columns.size() && i < items.size(); i++) {
            ListView<CascaderItem<T>> column = columns.get(i);
            int index = indexOfIdentity(column.getItems(), items.get(i));
            if (index >= 0) {
                column.scrollTo(index);
            }
        }
    }

    private static <T> int indexOfIdentity(List<CascaderItem<T>> items, CascaderItem<T> target) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        if (columns.isEmpty()) {
            return leftInset + columnWidthOrDefault() + rightInset;
        }
        // Delegate to the HBox so its -fx-spacing and -fx-padding (author-settable on
        // .cascader-view > .columns) are counted; the popup takes this width.
        return leftInset + columnsBox.prefWidth(height) + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        double content = 0.0;
        for (ListView<CascaderItem<T>> column : columns) {
            content = Math.max(content, columnContentHeight(column));
        }
        if (columns.isEmpty()) {
            content = rowHeightOrDefault() * sanitizedVisibleRowCount();
        }
        // The row-slot height stays fixed (columnContentHeight), but the columns box
        // may carry its own vertical padding that must be added.
        Insets boxInsets = columnsBox.getInsets();
        return topInset + boxInsets.getTop() + content + boxInsets.getBottom() + bottomInset;
    }

    /**
     * Height a column needs to show {@code visibleRowCount} fixed-size rows plus
     * its own vertical insets. Item count is intentionally not consulted: the
     * panel keeps a fixed row-slot height so expanding short/long columns does not
     * make the popup jump (decision: fixed height, no shrink).
     */
    private double columnContentHeight(ListView<CascaderItem<T>> column) {
        Insets in = column.getInsets();
        return sanitizedVisibleRowCount() * cellSize(column) + in.getTop() + in.getBottom();
    }

    private double cellSize(ListView<CascaderItem<T>> column) {
        double fixed = column.getFixedCellSize();
        return fixed > 0.0 ? fixed : rowHeightOrDefault();
    }

    private int sanitizedVisibleRowCount() {
        return Math.max(MIN_VISIBLE_ROW_COUNT, getSkinnable().getVisibleRowCount());
    }

    /**
     * Inert selection model for the column ListViews: the cascader view owns
     * selection, so the list-view row-selection channel must never fire. It
     * cannot simply be {@code null} — parts of the JavaFX ListView behavior
     * (e.g. {@code ListViewBehavior.mousePressed}) dereference the model without
     * a null guard. Every mutator is a no-op and the exposed lists stay empty,
     * so cells never receive list-view selection state; click-to-focus is
     * unaffected (the cell behavior drives the focus model independently).
     */
    private static final class NoOpSelectionModel<E> extends MultipleSelectionModel<E> {

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<E> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int index, int... indices) {
        }

        @Override
        public void selectAll() {
        }

        @Override
        public void selectFirst() {
        }

        @Override
        public void selectLast() {
        }

        @Override
        public void clearAndSelect(int index) {
        }

        @Override
        public void select(int index) {
        }

        @Override
        public void select(E item) {
        }

        @Override
        public void clearSelection(int index) {
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {
        }

        @Override
        public void selectNext() {
        }
    }
}
