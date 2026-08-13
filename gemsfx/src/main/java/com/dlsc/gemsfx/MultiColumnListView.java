package com.dlsc.gemsfx;

import com.dlsc.gemsfx.LoadingPane.Size;
import com.dlsc.gemsfx.LoadingPane.Status;
import com.dlsc.gemsfx.skins.MultiColumnListViewSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ListUtils;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A view for displaying multiple columns where each column consists of a header
 * control and a {@link ListView}. The control allows the user to rearrange the items in each
 * {@link ListView} and also to drag and drop items from one column to another.
 *
 * <p>Internally the control wraps every model object inside an instance of
 * {@link ColumnItem}. This allows the control to create the two marker items that are
 * used to visualize the "from" and the "to" location of a drag and drop operation all
 * by itself. Applications only ever deal with their own model objects of type
 * {@code T}.</p>
 *
 * <p>In addition the view supports a placeholder node (see {@link #placeholderProperty()})
 * which will be shown instead of the columns whenever the list of columns is empty. The
 * default placeholder is a label that reads "No columns defined." and that is surrounded
 * by a dashed border. The placeholder can be replaced with any other node or removed
 * completely by setting the property to null.</p>
 *
 * <p><b>Controlling drag and drop:</b> two callbacks allow applications to restrict which
 * items may be dragged and where they may be dropped. Both callbacks return {@code true}
 * for everything by default, and drag and drop can be switched off completely via
 * {@link #disableDragAndDropProperty()}.</p>
 *
 * <table class="striped">
 *   <caption>Drag and drop callbacks</caption>
 *   <thead><tr><th>Callback</th><th>Signature</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link #dragPossibleCallbackProperty() dragPossibleCallback}</td>
 *       <td>{@code Callback<T, Boolean>}</td>
 *       <td>Invoked when the user starts to drag an item. The callback receives the model
 *           object of type {@code T} that is about to be dragged and returns {@code true}
 *           if the drag operation is allowed for that item. Returning {@code false} makes
 *           the item non-draggable.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #dropPossibleCallbackProperty() dropPossibleCallback}</td>
 *       <td>{@code Callback<DropParameter<T>, Boolean>}</td>
 *       <td>Invoked while the user drags an item over a possible drop location. The callback
 *           receives a {@link DropParameter} which carries the dragged item
 *           ({@link DropParameter#getItem()}) and the target column
 *           ({@link DropParameter#getColumn()}). Returning {@code false} rejects the drop,
 *           so that neither the drop markers appear nor the drop itself is performed.</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <p>Example: only allow "done" items to be dragged and only allow drops into a column
 * that is not full.</p>
 *
 * <pre>{@code
 * MultiColumnListView<Task> view = new MultiColumnListView<>();
 * view.setDragPossibleCallback(task -> task.isDone());
 * view.setDropPossibleCallback(param -> param.getColumn().getItems().size() < 10);
 * }</pre>
 *
 * <p><b>Drag and drop events:</b> the view fires instances of {@link MultiColumnListViewEvent}
 * for all relevant steps of a drag and drop operation, including the steps that were vetoed by
 * the two callbacks described above. Every event carries the dragged item
 * ({@link MultiColumnListViewEvent#getDraggedItem()}), the involved column
 * ({@link MultiColumnListViewEvent#getColumn()}), and the item index inside that column
 * ({@link MultiColumnListViewEvent#getIndex()}).</p>
 *
 * <table class="striped">
 *   <caption>Event types</caption>
 *   <thead><tr><th>Event type</th><th>Fired when</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@link MultiColumnListViewEvent#ANY}</td>
 *         <td>Super type of all events fired by this control, useful for registering a single
 *             handler for all drag and drop events.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#DRAG_STARTED}</td>
 *         <td>The user started to drag an item and the {@code dragPossibleCallback} allowed it.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#DRAG_NOT_POSSIBLE}</td>
 *         <td>The user tried to drag an item but the {@code dragPossibleCallback} returned
 *             {@code false}.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#DRAG_OVER}</td>
 *         <td>A dragged item is hovering over a valid drop location.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#DROP_NOT_POSSIBLE}</td>
 *         <td>A dragged item is hovering over, or was dropped on, a location that the
 *             {@code dropPossibleCallback} rejected.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#ITEM_MOVED}</td>
 *         <td>An item was successfully dropped and hence moved to its new column and index.</td></tr>
 *     <tr><td>{@link MultiColumnListViewEvent#DRAG_ENDED}</td>
 *         <td>The drag operation has finished, no matter whether it was successful or not. This
 *             event type is currently not fired by the control itself and is meant to be used by
 *             applications that implement their own drag and drop handling.</td></tr>
 *   </tbody>
 * </table>
 *
 * <pre>{@code
 * view.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, evt ->
 *         System.out.println(evt.getDraggedItem() + " moved to index " + evt.getIndex()));
 * }</pre>
 *
 * @param <T> the item types, e.g. "Issues" or "Tickets"
 *
 *            <p><b>CSS Styleable Properties:</b>
 *            <table class="striped">
 *              <caption>CSS Properties</caption>
 *              <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *              <tbody>
 *                <tr><td>{@code -fx-disable-drag-and-drop}</td><td>{@code Boolean}</td><td>Whether to disable drag and drop.</td></tr>
 *                <tr><td>{@code -fx-show-headers}</td><td>{@code Boolean}</td><td>Whether to show column headers.</td></tr>
 *              </tbody>
 *            </table>
 */
public class MultiColumnListView<T> extends Control {

    /**
     * Constructs a new view.
     */
    public MultiColumnListView() {
        getStyleClass().add("multi-column-list-view");
        AccessibilityUtil.setRole(this, AccessibleRole.LIST_VIEW);
        setFocusTraversable(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MultiColumnListViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(MultiColumnListView.class.getResource("multi-column-list-view.css")).toExternalForm();
    }

    public static class DropParameter<T> {
        private final T item;
        private final ListViewColumn<T> column;

        public DropParameter(T item, ListViewColumn<T> column) {
            this.item = item;
            this.column = column;
        }

        public T getItem() {
            return item;
        }

        public ListViewColumn<T> getColumn() {
            return column;
        }
    }

    private final ObjectProperty<Callback<DropParameter<T>, Boolean>> dropPossibleCallback = new SimpleObjectProperty<>(this, "dropPossibleCallback", param -> true);

    public final Callback<DropParameter<T>, Boolean> getDropPossibleCallback() {
        return dropPossibleCallback.get();
    }

    public final ObjectProperty<Callback<DropParameter<T>, Boolean>> dropPossibleCallbackProperty() {
        return dropPossibleCallback;
    }

    public final void setDropPossibleCallback(Callback<DropParameter<T>, Boolean> dropPossibleCallback) {
        this.dropPossibleCallback.set(dropPossibleCallback);
    }

    private final ObjectProperty<Callback<T, Boolean>> dragPossibleCallback = new SimpleObjectProperty<>(this, "draggableCallback", item -> true);

    public Callback<T, Boolean> getDragPossibleCallback() {
        return dragPossibleCallback.get();
    }

    public ObjectProperty<Callback<T, Boolean>> dragPossibleCallbackProperty() {
        return dragPossibleCallback;
    }

    public void setDragPossibleCallback(Callback<T, Boolean> dragPossibleCallback) {
        this.dragPossibleCallback.set(dragPossibleCallback);
    }

    private final ObjectProperty<ProgressIndicator> progressIndicator = new SimpleObjectProperty<>(this, "progressIndicator", new CircleProgressIndicator());

    public final ProgressIndicator getProgressIndicator() {
        return progressIndicator.get();
    }

    /**
     * The progress indicator that will be used to display percentage progress or the indeterminate state of the
     * loading progress.
     *
     * @return the progress indicator
     */
    public final ObjectProperty<ProgressIndicator> progressIndicatorProperty() {
        return progressIndicator;
    }

    public final void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator.set(progressIndicator);
    }

    private final ObjectProperty<Status> loadingStatus = new SimpleObjectProperty<>(this, "loadingStatus", Status.OK);

    public final Status getLoadingStatus() {
        return loadingStatus.get();
    }

    /**
     * Provides a property that holds the current loading status of the control.
     * This status indicates whether the control is in a loading state, displaying
     * an error, or ready to display the content.
     *
     * @return an {@link ObjectProperty} representing the {@link Status} of the control
     */
    public final ObjectProperty<Status> loadingStatusProperty() {
        return loadingStatus;
    }

    public final void setLoadingStatus(Status loadingStatus) {
        this.loadingStatus.set(loadingStatus);
    }

    /**
     * Represents the size of the loading status indicator used in the control.
     * This property determines the visual size (e.g., SMALL, MEDIUM, LARGE) of the loading status
     * and can be updated dynamically to reflect size changes in the UI.
     * <p>
     * The default value is {@code Size.MEDIUM}.
     *
     * @see #loadingStatusProperty()
     */
    private final ObjectProperty<Size> loadingStatusSize = new SimpleObjectProperty<>(this, "loadingStatusSize", Size.MEDIUM);

    public final Size getLoadingStatusSize() {
        return loadingStatusSize.get();
    }

    public final ObjectProperty<Size> loadingStatusSizeProperty() {
        return loadingStatusSize;
    }

    public final void setLoadingStatusSize(Size loadingStatusSize) {
        this.loadingStatusSize.set(loadingStatusSize);
    }

    private final StyleableBooleanProperty showHeaders = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return MultiColumnListView.this;
        }

        @Override
        public String getName() {
            return "showHeaders";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.SHOW_HEADERS;
        }
    };

    public final boolean isShowHeaders() {
        return showHeaders.get();
    }

    /**
     * Determines whether the headers will be shown or not. Toggling this property will trigger
     * a rebuild of the view.
     * <p>
     * Can be set via CSS using the {@code -fx-show-headers} property.
     * Valid values are: {@code true}, {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if the headers should be shown
     */
    public final BooleanProperty showHeadersProperty() {
        return showHeaders;
    }

    public final void setShowHeaders(boolean showHeaders) {
        this.showHeaders.set(showHeaders);
    }

    private final ObjectProperty<Callback<MultiColumnListView<T>, ListView<ColumnItem<T>>>> listViewFactory = new SimpleObjectProperty<>(this, "listViewFactory", m -> new AutoscrollListView<>());

    public final Callback<MultiColumnListView<T>, ListView<ColumnItem<T>>> getListViewFactory() {
        return listViewFactory.get();
    }

    /**
     * Stores the callback that will be invoked to produce new {@link ListView} instances.
     *
     * @return the factory for creating the required list views, one for each column
     */
    public final ObjectProperty<Callback<MultiColumnListView<T>, ListView<ColumnItem<T>>>> listViewFactoryProperty() {
        return listViewFactory;
    }

    public final void setListViewFactory(Callback<MultiColumnListView<T>, ListView<ColumnItem<T>>> listViewFactory) {
        this.listViewFactory.set(listViewFactory);
    }

    private final ListProperty<ListViewColumn<T>> columns = new SimpleListProperty<>(this, "columns", FXCollections.observableArrayList());

    public final ObservableList<ListViewColumn<T>> getColumns() {
        return columns.get();
    }

    /**
     * A list of columns that define how many columns will be shown inside the view.
     * The model objects in this list also store the header and the data for each
     * column.
     *
     * @return the list of columns
     */
    public final ListProperty<ListViewColumn<T>> columnsProperty() {
        return columns;
    }

    public final void setColumns(ObservableList<ListViewColumn<T>> columns) {
        this.columns.set(columns);
    }

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder", createDefaultPlaceholder());

    public final Node getPlaceholder() {
        return placeholder.get();
    }

    /**
     * The node that will be shown instead of the columns when no columns have been
     * added to the view (see {@link #columnsProperty()}). The default placeholder is a
     * label that reads "No columns defined." and that is surrounded by a dashed border.
     * <p>
     * The placeholder node will be resized to fill the entire area of the control. No
     * placeholder will be shown if this property is set to null. The placeholder will also
     * not be shown while the control is loading or showing an error (see
     * {@link #loadingStatusProperty()}).
     * </p>
     *
     * @return the node shown when the view does not have any columns
     */
    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final void setPlaceholder(Node placeholder) {
        this.placeholder.set(placeholder);
    }

    private Node createDefaultPlaceholder() {
        Label label = new Label(ResourceBundleManager.getString(ResourceBundleManager.BundleType.MULTI_COLUMN_LIST_VIEW, "placeholder.no-columns", "No columns defined."));
        label.getStyleClass().add("placeholder");
        return label;
    }

    private final ObjectProperty<Callback<MultiColumnListView<T>, ColumnListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory", ColumnListCell::new);

    public final Callback<MultiColumnListView<T>, ColumnListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * The cell factory that will be used for each one of the {@link ListView} instances. The
     * default factory produces {@link ColumnListCell} instances which show the item inside a
     * pane with a dashed border and a centered label.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<MultiColumnListView<T>, ColumnListCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public final void setCellFactory(Callback<MultiColumnListView<T>, ColumnListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    private final ObjectProperty<Callback<Integer, Node>> separatorFactory = new SimpleObjectProperty<>(this, "separatorFactory", index -> {
        Region separator = new Region();
        separator.getStyleClass().add("column-separator");
        return separator;
    });

    public final Callback<Integer, Node> getSeparatorFactory() {
        return separatorFactory.get();
    }

    /**
     * An optional factory for creating separators that will be placed between columns. The default implementation
     * creates a region and adds the style class "column-separator". No separators will be added to the view when
     * the factory is being set to null.
     *
     * @return a separator node
     */
    public final ObjectProperty<Callback<Integer, Node>> separatorFactoryProperty() {
        return separatorFactory;
    }

    public final void setSeparatorFactory(Callback<Integer, Node> separatorFactory) {
        this.separatorFactory.set(separatorFactory);
    }

    private final StyleableBooleanProperty disableDragAndDrop = new StyleableBooleanProperty(false) {
        @Override
        public Object getBean() {
            return MultiColumnListView.this;
        }

        @Override
        public String getName() {
            return "disableDragAndDrop";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.DISABLE_DRAG_AND_DROP;
        }
    };

    public final boolean isDisableDragAndDrop() {
        return disableDragAndDrop.get();
    }

    /**
     * Controls whether the user can rearrange items via drag and drop or not.
     * <p>
     * Can be set via CSS using the {@code -fx-disable-drag-and-drop} property.
     * Valid values are: {@code true}, {@code false}.
     * The default value is {@code false}.
     * </p>
     *
     * @return "true" if the control allows rearranging items via drag and drop
     */
    public final BooleanProperty disableDragAndDropProperty() {
        return disableDragAndDrop;
    }

    public final void setDisableDragAndDrop(boolean disableDragAndDrop) {
        this.disableDragAndDrop.set(disableDragAndDrop);
    }

    private static class StyleableProperties {

        private static final CssMetaData<MultiColumnListView, Boolean> SHOW_HEADERS =
                new CssMetaData<>("-fx-show-headers", BooleanConverter.getInstance(), true) {
                    @Override
                    public boolean isSettable(MultiColumnListView c) {
                        return !c.showHeaders.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(MultiColumnListView c) {
                        return (StyleableProperty<Boolean>) c.showHeaders;
                    }
                };

        private static final CssMetaData<MultiColumnListView, Boolean> DISABLE_DRAG_AND_DROP =
                new CssMetaData<>("-fx-disable-drag-and-drop", BooleanConverter.getInstance(), false) {
                    @Override
                    public boolean isSettable(MultiColumnListView c) {
                        return !c.disableDragAndDrop.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(MultiColumnListView c) {
                        return (StyleableProperty<Boolean>) c.disableDragAndDrop;
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SHOW_HEADERS);
            styleables.add(DISABLE_DRAG_AND_DROP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * The model object representing a single column. The type of the items in all columns must be the
     * same.
     *
     * @param <T> the type of items shown by the column
     */
    public static class ListViewColumn<T> {

        private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

        private final ObservableList<ColumnItem<T>> itemWrappers = FXCollections.observableArrayList();

        private boolean updating;

        public ListViewColumn() {
            items.addListener((ListChangeListener<T>) change -> updateWrappers());
            itemWrappers.addListener((ListChangeListener<ColumnItem<T>>) change -> updateItems());
        }

        /**
         * The list of wrapped items that is being shown by the {@link ListView} of this column. This
         * list is maintained by the control itself. It is kept in sync with the list returned by
         * {@link #getItems()} and it might temporarily contain the two placeholder items that are used
         * to visualize an ongoing drag and drop operation.
         *
         * @return the internal list of wrapped items
         */
        public final ObservableList<ColumnItem<T>> getItemWrappers() {
            return itemWrappers;
        }

        private void updateWrappers() {
            if (updating) {
                return;
            }

            updating = true;

            try {
                Map<T, List<ColumnItem<T>>> reusableWrappers = new IdentityHashMap<>();
                for (ColumnItem<T> wrapper : itemWrappers) {
                    if (!wrapper.isPlaceholder()) {
                        reusableWrappers.computeIfAbsent(wrapper.getUserObject(), key -> new ArrayList<>()).add(wrapper);
                    }
                }

                List<ColumnItem<T>> newWrappers = new ArrayList<>();
                for (T item : items) {
                    List<ColumnItem<T>> candidates = reusableWrappers.get(item);
                    if (candidates != null && !candidates.isEmpty()) {
                        newWrappers.add(candidates.remove(0));
                    } else {
                        newWrappers.add(new ColumnItem<>(item));
                    }
                }

                // the placeholders of an ongoing drag and drop operation are not part of the
                // user's item list, hence they have to be re-inserted at their current location
                for (int index = 0; index < itemWrappers.size(); index++) {
                    ColumnItem<T> wrapper = itemWrappers.get(index);
                    if (wrapper.isPlaceholder()) {
                        newWrappers.add(Math.min(index, newWrappers.size()), wrapper);
                    }
                }

                if (!sameWrappers(newWrappers)) {
                    itemWrappers.setAll(newWrappers);
                }
            } finally {
                updating = false;
            }
        }

        private void updateItems() {
            if (updating || items.get() == null) {
                return;
            }

            List<T> newItems = new ArrayList<>();
            for (ColumnItem<T> wrapper : itemWrappers) {
                if (!wrapper.isPlaceholder()) {
                    newItems.add(wrapper.getUserObject());
                }
            }

            if (sameItems(newItems)) {
                return;
            }

            updating = true;

            try {
                items.setAll(newItems);
            } finally {
                updating = false;
            }
        }

        private boolean sameWrappers(List<ColumnItem<T>> newWrappers) {
            if (itemWrappers.size() != newWrappers.size()) {
                return false;
            }

            for (int i = 0; i < newWrappers.size(); i++) {
                if (itemWrappers.get(i) != newWrappers.get(i)) {
                    return false;
                }
            }

            return true;
        }

        private boolean sameItems(List<T> newItems) {
            ObservableList<T> currentItems = items.get();

            if (currentItems.size() != newItems.size()) {
                return false;
            }

            for (int i = 0; i < newItems.size(); i++) {
                if (currentItems.get(i) != newItems.get(i)) {
                    return false;
                }
            }

            return true;
        }

        public final ObservableList<T> getItems() {
            return items.get();
        }

        /**
         * The data shown in the column.
         *
         * @return the model for this column
         */
        public final ListProperty<T> itemsProperty() {
            return items;
        }

        public final void setItems(ObservableList<T> items) {
            this.items.set(items);
        }

        private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header", new Label(ResourceBundleManager.getString(ResourceBundleManager.BundleType.MULTI_COLUMN_LIST_VIEW, "column.header.default", "Column Header")));

        public final Node getHeader() {
            return header.get();
        }

        /**
         * An optional node that will serve as the column's header. It will be shown above the column.
         *
         * @return the header node / header UI
         */
        public final ObjectProperty<Node> headerProperty() {
            return header;
        }

        public final void setHeader(Node header) {
            this.header.set(header);
        }

        /**
         * An optional user object that can be associated with the column. This can be used to store additional
         * information about the column, such as an identifier or metadata.
         */
        private final ObjectProperty<Object> userObject = new SimpleObjectProperty<>(this, "userObject");

        public final Object getUserObject() {
            return userObject.get();
        }

        public final void setUserObject(Object value) {
            userObject.set(value);
        }

        public final ObjectProperty<Object> userObjectProperty() {
            return userObject;
        }


    }

    private final ObjectProperty<T> draggedItem = new SimpleObjectProperty<>(this, "draggedItem");

    public final T getDraggedItem() {
        return draggedItem.get();
    }

    public final ObjectProperty<T> draggedItemProperty() {
        return draggedItem;
    }

    public final void setDraggedItem(T draggedItem) {
        this.draggedItem.set(draggedItem);
    }

    private final ObservableList<T> draggedItems = FXCollections.observableArrayList();

    public final ObservableList<T> getDraggedItems() {
        return draggedItems;
    }

    private final ColumnItem<T> fromPlaceholder = ColumnItem.createFromPlaceholder();

    private final ColumnItem<T> toPlaceholder = ColumnItem.createToPlaceholder();

    private ColumnItem<T> draggedColumnItem;

    /**
     * The item that is used to visualize the "from" location of an ongoing drag and drop
     * operation. The item is created and managed by the control itself.
     *
     * @return the "from" placeholder item
     */
    public final ColumnItem<T> getFromPlaceholder() {
        return fromPlaceholder;
    }

    /**
     * The item that is used to visualize the "to" location of an ongoing drag and drop
     * operation. The item is created and managed by the control itself.
     *
     * @return the "to" placeholder item
     */
    public final ColumnItem<T> getToPlaceholder() {
        return toPlaceholder;
    }

    /**
     * The wrapper of the item that is currently being dragged, or {@code null} if no drag
     * operation is in progress.
     *
     * @return the wrapper of the currently dragged item
     */
    public final ColumnItem<T> getDraggedColumnItem() {
        return draggedColumnItem;
    }

    private void setDraggedColumnItem(ColumnItem<T> draggedColumnItem) {
        this.draggedColumnItem = draggedColumnItem;
    }

    /**
     * A wrapper around the model objects shown by the {@link MultiColumnListView} control. All
     * list views managed by the control show instances of this type. This allows the control to
     * add and remove the two marker / placeholder items that are used to visualize an ongoing
     * drag and drop operation without requiring the application to provide model objects for
     * these markers.
     *
     * @param <T> the type of the wrapped model object
     */
    public static final class ColumnItem<T> {

        private enum Kind {
            ITEM,
            FROM_PLACEHOLDER,
            TO_PLACEHOLDER
        }

        private final T userObject;

        private final Kind kind;

        /**
         * Creates a new wrapper for the given model object.
         *
         * @param userObject the model object shown by the control
         */
        public ColumnItem(T userObject) {
            this(userObject, Kind.ITEM);
        }

        private ColumnItem(T userObject, Kind kind) {
            this.userObject = userObject;
            this.kind = kind;
        }

        private static <T> ColumnItem<T> createFromPlaceholder() {
            return new ColumnItem<>(null, Kind.FROM_PLACEHOLDER);
        }

        private static <T> ColumnItem<T> createToPlaceholder() {
            return new ColumnItem<>(null, Kind.TO_PLACEHOLDER);
        }

        /**
         * The model object wrapped by this item. Returns {@code null} for the two placeholder
         * items.
         *
         * @return the wrapped model object
         */
        public T getUserObject() {
            return userObject;
        }

        /**
         * Determines if this item is one of the two placeholder items used during drag and drop.
         *
         * @return true if the item is the "from" or the "to" placeholder
         */
        public boolean isPlaceholder() {
            return kind != Kind.ITEM;
        }

        /**
         * Determines if this item is the placeholder marking the location where the currently
         * dragged item came from.
         *
         * @return true if the item is the "from" placeholder
         */
        public boolean isFromPlaceholder() {
            return kind == Kind.FROM_PLACEHOLDER;
        }

        /**
         * Determines if this item is the placeholder marking the location where the currently
         * dragged item will be dropped.
         *
         * @return true if the item is the "to" placeholder
         */
        public boolean isToPlaceholder() {
            return kind == Kind.TO_PLACEHOLDER;
        }

        @Override
        public String toString() {
            return "ColumnItem{kind=" + kind + ", userObject=" + userObject + "}";
        }
    }

    /**
     * A special list cell to be used in combination with the {@link MultiColumnListView} control.
     * The cell adds drag and drop support for re-arranging list cells and for dragging them from
     * one column to another.
     *
     * <p>The cells of the control show instances of {@link ColumnItem}, which are wrappers around
     * the application's model objects. Subclasses do not have to deal with these wrappers as they
     * can simply override {@link ColumnListCell#updateUserObject(Object, boolean)} which receives
     * the model object itself. The model object will be {@code null} whenever the cell shows one of the two
     * placeholders used during drag and drop (see {@link #placeholderProperty()},
     * {@link #fromPlaceholderProperty()}, {@link #toPlaceholderProperty()}).</p>
     *
     * @param <T> the type of the model objects shown by the list
     */
    public static class ColumnListCell<T> extends ListCell<ColumnItem<T>> {

        private static final PseudoClass FROM_PSEUDO_CLASS = PseudoClass.getPseudoClass("from");
        private static final PseudoClass TO_PSEUDO_CLASS = PseudoClass.getPseudoClass("to");

        private final MultiColumnListView<T> multiColumnListView;
        private ListViewColumn<T> column;

        private Label contentLabel;
        private StackPane contentPane;

        /**
         * Creates a new list cell.
         *
         * @param multiColumnListView reference to the {@link MultiColumnListView} control where the cell is being used
         */
        public ColumnListCell(MultiColumnListView<T> multiColumnListView) {
            this.multiColumnListView = multiColumnListView;

            getStyleClass().add("column-list-cell");

            placeholder.bind(fromPlaceholder.or(toPlaceholder));

            setOnDragDetected(event -> {
                if (multiColumnListView.isDisableDragAndDrop()) {
                    return;
                }

                log("drag detected");
                if (isEmpty() || getItem() == null || getItem().isPlaceholder()) {
                    return;
                }

                ColumnItem<T> columnItem = getItem();
                T userObject = columnItem.getUserObject();

                Callback<T, Boolean> dragPossibleCallback = multiColumnListView.getDragPossibleCallback();
                if (dragPossibleCallback.call(userObject)) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(Integer.toString(getIndex()));

                    SnapshotParameters parameters = new SnapshotParameters();
                    parameters.setFill(Color.TRANSPARENT); // important or we get a white frame in many cases
                    WritableImage snapshot = getSnapshotNode().snapshot(parameters, null);

                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    dragboard.setContent(content);
                    dragboard.setDragView(snapshot);

                    dragboard.setDragViewOffsetX(snapshot.getWidth() / 2);
                    dragboard.setDragViewOffsetY(-snapshot.getHeight() / 2);

                    event.consume();

                    multiColumnListView.setDraggedColumnItem(columnItem);
                    multiColumnListView.setDraggedItem(userObject);

                    List<T> selectedItems = new ArrayList<>();
                    for (ColumnItem<T> selectedItem : getListView().getSelectionModel().getSelectedItems()) {
                        if (selectedItem != null && !selectedItem.isPlaceholder()) {
                            selectedItems.add(selectedItem.getUserObject());
                        }
                    }
                    multiColumnListView.getDraggedItems().setAll(selectedItems);

                    ListUtils.replaceIf(getListView().getItems(), item -> item == columnItem, multiColumnListView.getFromPlaceholder());

                    fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DRAG_STARTED, userObject, getColumn(), getIndex()));
                } else {
                    fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DRAG_NOT_POSSIBLE, userObject, getColumn(), getIndex()));
                }
            });

            setOnDragOver(event -> {
                log("drag over");
                if (event.getGestureSource() != this && multiColumnListView.getFromPlaceholder() != getItem()) {
                    DropParameter<T> dropParameter = new DropParameter<>(getUserObject(), column);
                    Callback<DropParameter<T>, Boolean> callback = multiColumnListView.getDropPossibleCallback();
                    if (callback.call(dropParameter)) {
                        log("   drop possible callback is accepting, " + hashCode() + ", txt: " + getText());
                        updateItems(event);
                        event.acceptTransferModes(TransferMode.MOVE);
                        fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DRAG_OVER, getUserObject(), getColumn(), getIndex()));
                    } else {
                        log("   drop possible callback is not accepting drag");
                        event.acceptTransferModes(TransferMode.NONE);
                        fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, getUserObject(), getColumn(), getIndex()));
                    }
                } else {
                    log("   not accepting transfer");
                    event.acceptTransferModes(TransferMode.NONE);
                    fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, getUserObject(), getColumn(), getIndex()));
                }
                event.consume();
            });

            setOnDragEntered(event -> log("drag entered"));

            setOnDragExited(event -> {
                log("drag exited");
                getListView().getItems().remove(multiColumnListView.getToPlaceholder());
            });

            setOnDragDropped(event -> {
                log("drag dropped");

                if (!event.getAcceptedTransferMode().equals(TransferMode.MOVE)) {
                    return;
                }

                if (multiColumnListView.getFromPlaceholder() == getItem()) {
                    log("   not performing drop, drop happened on 'from' placeholder");
                    return;
                }

                Callback<DropParameter<T>, Boolean> dropCallback = multiColumnListView.getDropPossibleCallback();
                if (dropCallback.call(new DropParameter<>(getUserObject(), column))) {
                    log("   drop is possible, performing drop");
                    ListView<ColumnItem<T>> listView = getListView();
                    ObservableList<ColumnItem<T>> items = listView.getItems();

                    items.remove(multiColumnListView.getFromPlaceholder());

                    ColumnItem<T> draggedColumnItem = multiColumnListView.getDraggedColumnItem();
                    if (draggedColumnItem == null) {
                        return;
                    }

                    ListUtils.replaceIf(items, item -> item == multiColumnListView.getToPlaceholder(), draggedColumnItem);

                    if (!items.contains(draggedColumnItem)) {
                        // probably dropped on same list view / same column (hence no "to" placeholder)
                        items.add(draggedColumnItem);
                    }

                    listView.getSelectionModel().select(draggedColumnItem);

                    event.setDropCompleted(true);

                    fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.ITEM_MOVED, draggedColumnItem.getUserObject(), getColumn(), getIndex()));
                } else {
                    fireEvent(new MultiColumnListViewEvent(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, getUserObject(), getColumn(), getIndex()));
                }

                event.consume();
            });

            setOnDragDone(evt -> {
                ColumnItem<T> draggedColumnItem = multiColumnListView.getDraggedColumnItem();

                if (evt.isAccepted()) {
                    log("drag done, accepted");
                    if (Objects.equals(evt.getAcceptedTransferMode(), TransferMode.MOVE)) {
                        log("   drop was completed, removing the 'from' placeholder");
                        getListView().getItems().remove(multiColumnListView.getFromPlaceholder());
                    } else if (draggedColumnItem != null) {
                        log("   drop was not completed, replacing placeholder with dragged item");
                        ListUtils.replaceIf(getListView().getItems(), item -> item == multiColumnListView.getFromPlaceholder(), draggedColumnItem);
                    }
                } else {
                    log("drag done, not accepted");

                    // put the item back into the "from" location
                    log("putting item back into 'from' location");
                    if (draggedColumnItem != null) {
                        ListUtils.replaceIf(getListView().getItems(), item -> item == multiColumnListView.getFromPlaceholder(), draggedColumnItem);
                    }
                }

                multiColumnListView.setDraggedColumnItem(null);
                multiColumnListView.setDraggedItem(null);
                evt.consume();
            });
        }

        public final ListViewColumn<T> getColumn() {
            return column;
        }

        /**
         * Returns the model object currently shown by this cell. The method returns {@code null}
         * if the cell is empty or if the cell currently shows one of the two drag and drop
         * placeholders.
         *
         * @return the model object shown by this cell
         */
        public final T getUserObject() {
            ColumnItem<T> item = getItem();
            return item == null ? null : item.getUserObject();
        }

        /**
         * Retrieves the node that will be used to create a drag image via the {@link Node#snapshot(SnapshotParameters, WritableImage)}
         * method.
         *
         * @return the snapshot node
         */
        protected Node getSnapshotNode() {
            return this;
        }

        /**
         * Returns the {@link MultiColumnListView} control where the cell is being
         * used.
         *
         * @return the parent control
         */
        public final MultiColumnListView<T> getMultiColumnListView() {
            return multiColumnListView;
        }

        private void updateItems(DragEvent event) {
            if (event.getGestureSource() != this) {
                int toIndex = getIndex();

                ColumnItem<T> fromItem = multiColumnListView.getFromPlaceholder();
                ColumnItem<T> toItem = multiColumnListView.getToPlaceholder();

                int fromIndex = getListView().getItems().indexOf(fromItem);

                ObservableList<ColumnItem<T>> items = getListView().getItems();
                log("item count: " + items.size());
                items.remove(toItem);
                log("item count now: " + items.size());

                if (event.getY() < getHeight() / 2) {
                    log("   attempt to add ABOVE");
                    if (toIndex > 0) {
                        int finalToIndex = Math.min(toIndex, items.size());
                        if (notNextToEachOther(fromIndex, finalToIndex)) {
                            log("      adding 'to' placeholder at index " + toIndex);
                            items.add(finalToIndex, toItem);
                        }
                    } else {
                        if (notNextToEachOther(fromIndex, 0)) {
                            log("      adding 'to' placeholder at index 0");
                            items.add(0, toItem);
                        }
                    }
                } else {
                    log("   attempt to add BELOW");
                    if (toIndex < items.size() - 1) {
                        int finalToIndex = toIndex + 1;
                        if (notNextToEachOther(fromIndex, finalToIndex)) {
                            log("      adding 'to' placeholder at index " + finalToIndex);
                            items.add(finalToIndex, toItem);
                        }
                    } else {
                        if (notNextToEachOther(fromIndex, items.size() - 1)) {
                            items.add(toItem);
                        }
                    }
                }
            }
        }

        private boolean notNextToEachOther(int fromIndex, int toIndex) {
            // Only if both indices are not -1 are both placeholders in the same list and need
            // special checks.
            log("from / to index: " + fromIndex + " / " + toIndex);
            if (fromIndex != -1 && toIndex != -1) {
                if (fromIndex < toIndex) {
                    return Math.abs(fromIndex - toIndex) > 1;
                } else {
                    return Math.abs(fromIndex - toIndex) > 0;
                }
            }

            return true;
        }

        @Override
        protected final void updateItem(ColumnItem<T> item, boolean empty) {
            super.updateItem(item, empty);

            boolean from = !empty && item != null && item.isFromPlaceholder();
            boolean to = !empty && item != null && item.isToPlaceholder();

            fromPlaceholder.set(from);
            toPlaceholder.set(to);

            pseudoClassStateChanged(FROM_PSEUDO_CLASS, from);
            pseudoClassStateChanged(TO_PSEUDO_CLASS, to);

            updateUserObject(item == null ? null : item.getUserObject(), empty);
        }

        /**
         * Updates the cell for the given model object. This method will be called by the cell
         * whenever its item changes. Subclasses should override this method instead of
         * {@link #updateItem(ColumnItem, boolean)}, which is final.
         *
         * <p>The given model object will be {@code null} when the cell is empty or when the cell
         * currently shows one of the two drag and drop placeholders. Use
         * {@link #isFromPlaceholder()} and {@link #isToPlaceholder()} to distinguish these
         * cases.</p>
         *
         * <p>The default implementation does not use the text of the cell but rather shows a
         * "rich" graphic (see {@link #getContentPane()}): a pane with a dashed border that
         * contains a centered label (see {@link #getContentLabel()}). Subclasses that override
         * this method can either keep using that pane, replace the graphic with a node of their
         * own, or simply call {@link #setText(String)} for a text-only cell.</p>
         *
         * @param userObject the model object shown by this cell, possibly {@code null}
         * @param empty      true if the cell is empty
         */
        protected void updateUserObject(T userObject, boolean empty) {
            String text;

            if (isFromPlaceholder()) {
                text = ResourceBundleManager.getString(ResourceBundleManager.BundleType.MULTI_COLUMN_LIST_VIEW, "placeholder.from", "From");
            } else if (isToPlaceholder()) {
                text = ResourceBundleManager.getString(ResourceBundleManager.BundleType.MULTI_COLUMN_LIST_VIEW, "placeholder.to", "To");
            } else if (!empty && userObject != null) {
                text = userObject.toString();
            } else {
                text = null;
            }

            setText(null);

            if (text == null) {
                setGraphic(null);
            } else {
                getContentLabel().setText(text);
                setGraphic(getContentPane());
            }
        }

        /**
         * Returns the label that is used by the default implementation of
         * {@link #updateUserObject(Object, boolean)} to show the text of the cell. The label is
         * centered inside the {@link #getContentPane() content pane} and uses the style class
         * "content-label".
         *
         * @return the label showing the text of the cell
         */
        protected final Label getContentLabel() {
            if (contentLabel == null) {
                contentLabel = new Label();
                contentLabel.getStyleClass().add("content-label");
                contentLabel.setWrapText(true);
                contentLabel.setAlignment(Pos.CENTER);
                contentLabel.setTextAlignment(TextAlignment.CENTER);
                contentLabel.setMaxWidth(Double.MAX_VALUE);
            }

            return contentLabel;
        }

        /**
         * Returns the pane that is used as the graphic of the cell by the default implementation
         * of {@link #updateUserObject(Object, boolean)}. The pane uses the style class
         * "content-pane", is styled with a dashed border, and centers the
         * {@link #getContentLabel() content label}. The pane always spans the entire width of
         * the cell.
         *
         * @return the pane used as the graphic of the cell
         */
        protected final StackPane getContentPane() {
            if (contentPane == null) {
                contentPane = new StackPane(getContentLabel());
                contentPane.getStyleClass().add("content-pane");
                contentPane.setAlignment(Pos.CENTER);
                contentPane.setMinWidth(0);

                // the graphic of a labeled node is sized to its preferred width, hence the pane
                // has to follow the width of the cell explicitly
                contentPane.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    Insets insets = getInsets();
                    return Math.max(0, getWidth() - insets.getLeft() - insets.getRight());
                }, widthProperty(), insetsProperty()));
            }

            return contentPane;
        }

        private final ReadOnlyBooleanWrapper placeholder = new ReadOnlyBooleanWrapper(this, "placeholder");

        public final boolean isPlaceholder() {
            return placeholder.get();
        }

        /**
         * A read-only property that is being set to true if the item in the cell is currently
         * either the "from" or the "to" placeholder item used during drag and drop operations.
         *
         * @return true if the currently shown item is either the "from" or the "to" placeholder object
         */
        public final ReadOnlyBooleanProperty placeholderProperty() {
            return placeholder.getReadOnlyProperty();
        }

        private final ReadOnlyBooleanWrapper fromPlaceholder = new ReadOnlyBooleanWrapper(this, "fromPlaceholder");

        public final boolean isFromPlaceholder() {
            return fromPlaceholder.get();
        }

        /**
         * A read-only property that is being set to true if the item in the cell is currently
         * the "from" placeholder item.
         *
         * @return true if the currently shown item is the "from" placeholder object
         */
        public final ReadOnlyBooleanProperty fromPlaceholderProperty() {
            return fromPlaceholder.getReadOnlyProperty();
        }

        private final ReadOnlyBooleanWrapper toPlaceholder = new ReadOnlyBooleanWrapper(this, "toPlaceholder");

        public final boolean isToPlaceholder() {
            return toPlaceholder.get();
        }

        /**
         * A read-only property that is being set to true if the item in the cell is currently
         * the "to" placeholder item.
         *
         * @return true if the currently shown item is the "to" placeholder object
         */
        public final ReadOnlyBooleanProperty toPlaceholderProperty() {
            return toPlaceholder.getReadOnlyProperty();
        }

        // for quick and dirty logging / debugging
        private void log(String text) {
            // System.out.println(text);
        }

        public void updateColumn(ListViewColumn<T> column) {
            this.column = column;
        }
    }


    /**
     * Represents an event specific to the {@code MultiColumnListView} component.
     * This event is triggered during user interactions such as drag and drop activities
     * or changes in the state of the list view.
     * <p>
     * The class provides a set of predefined event types to handle common scenarios,
     * such as item movement, drag operations, and drop validation.
     * <p>
     * Event Types:
     * - {@link #ANY}: Represents a generic {@code MultiColumnListViewEvent}.
     * - {@link #ITEM_MOVED}: Indicates that an item has been moved to a new location.
     * - {@link #DRAG_NOT_POSSIBLE}: Signifies a rejected drag operation, see
     *   {@link MultiColumnListView#dragPossibleCallbackProperty()}.
     * - {@link #DROP_NOT_POSSIBLE}: Signifies a failed drop operation due to validation failures.
     * - {@link #DRAG_OVER}: Fired when a drag operation hovers over the target area.
     * - {@link #DRAG_STARTED}: Specifies the start of a drag operation.
     * - {@link #DRAG_ENDED}: Fired when a drag operation has completed.
     * <p>
     * This event also provides access to the dragged item and the associated column
     * where the interaction occurs.
     */
    public static class MultiColumnListViewEvent extends Event {

        public static final EventType<MultiColumnListViewEvent> ANY = new EventType<>(Event.ANY, "MULTI_COLUMN_LIST_VIEW_EVENT");
        public static final EventType<MultiColumnListViewEvent> ITEM_MOVED = new EventType<>(MultiColumnListViewEvent.ANY, "ITEM_MOVED");
        public static final EventType<MultiColumnListViewEvent> DRAG_NOT_POSSIBLE = new EventType<>(MultiColumnListViewEvent.ANY, "DRAG_NOT_POSSIBLE");
        public static final EventType<MultiColumnListViewEvent> DROP_NOT_POSSIBLE = new EventType<>(MultiColumnListViewEvent.ANY, "DROP_NOT_POSSIBLE");
        public static final EventType<MultiColumnListViewEvent> DRAG_OVER = new EventType<>(MultiColumnListViewEvent.ANY, "DRAG_OVER");
        public static final EventType<MultiColumnListViewEvent> DRAG_STARTED = new EventType<>(MultiColumnListViewEvent.ANY, "DRAG_STARTED");
        public static final EventType<MultiColumnListViewEvent> DRAG_ENDED = new EventType<>(MultiColumnListViewEvent.ANY, "DRAG_ENDED");

        private final Object draggedItem;
        private final ListViewColumn column;
        private final int index;


        public MultiColumnListViewEvent(EventType<? extends Event> eventType, Object draggedItem, ListViewColumn column, int index) {
            super(eventType);
            this.draggedItem = draggedItem;
            this.column = column;
            this.index = index;
        }

        public Object getDraggedItem() {
            return draggedItem;
        }

        public ListViewColumn getColumn() {
            return column;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return "MultiColumnListViewEvent{" +
                    "column=" + column +
                    ", eventType=" + eventType +
                    ", target=" + target +
                    ", consumed=" + consumed +
                    ", source=" + source +
                    '}';
        }
    }
}
