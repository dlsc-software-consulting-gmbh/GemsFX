package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.MultiColumnListViewSkin;
import com.dlsc.gemsfx.util.ListUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.Objects;

/**
 * A view for displaying multiple columns where each column consists of a header
 * control and a {@link ListView}. The control allows the user to rearrange the items in each
 * {@link ListView} and also to drag and drop items from one column to another.
 *
 * @param <T> the item types, e.g. "Issues" or "Tickets"
 */
public class MultiColumnListView<T> extends Control {

    /**
     * Constructs a new view.
     */
    public MultiColumnListView() {
        getStyleClass().add("multi-column-list-view");
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

    private final BooleanProperty showHeaders = new SimpleBooleanProperty(this, "showHeaders", true);

    public final boolean isShowHeaders() {
        return showHeaders.get();
    }

    /**
     * Determines whether the headers will be shown or not. Toggling this property will trigger
     * a rebuild of the view.
     *
     * @return true if the headers should be shown
     */
    public final BooleanProperty showHeadersProperty() {
        return showHeaders;
    }

    public final void setShowHeaders(boolean showHeaders) {
        this.showHeaders.set(showHeaders);
    }

    private final ObjectProperty<Callback<MultiColumnListView<T>, ListView<T>>> listViewFactory = new SimpleObjectProperty<>(this, "listViewFactory", m -> new AutoscrollListView<>());

    public final Callback<MultiColumnListView<T>, ListView<T>> getListViewFactory() {
        return listViewFactory.get();
    }

    /**
     * Stores the callback that will be invoked to produce new {@link ListView} instances.
     *
     * @return the factory for creating the required list views, one for each column
     */
    public final ObjectProperty<Callback<MultiColumnListView<T>, ListView<T>>> listViewFactoryProperty() {
        return listViewFactory;
    }

    public final void setListViewFactory(Callback<MultiColumnListView<T>, ListView<T>> listViewFactory) {
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

    private final ObjectProperty<Callback<MultiColumnListView<T>, ColumnListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory", ColumnListCell::new);

    public final Callback<MultiColumnListView<T>, ColumnListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * The cell factory that will be used for each one of the {@link ListView} instances.
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

    private final BooleanProperty disableDragAndDrop = new SimpleBooleanProperty(this, "disableDragAndDrop");

    public final boolean isDisableDragAndDrop() {
        return disableDragAndDrop.get();
    }

    /**
     * Controls whether the user can rearrange items via drag and drop or not.
     *
     * @return "true" if the control allows rearranging items via drag and drop
     */
    public final BooleanProperty disableDragAndDropProperty() {
        return disableDragAndDrop;
    }

    public final void setDisableDragAndDrop(boolean disableDragAndDrop) {
        this.disableDragAndDrop.set(disableDragAndDrop);
    }

    /**
     * The model object representing a single column. The type of the items in all columns must be the
     * same.
     *
     * @param <T> the type of items shown by the column
     */
    public static class ListViewColumn<T> {

        private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

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

        private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header", new Label("Column Header"));

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

    private final ObjectProperty<T> placeholderFrom = new SimpleObjectProperty<>(this, "placeholderFrom");

    public final T getPlaceholderFrom() {
        return placeholderFrom.get();
    }

    /**
     * A model item that represents the "from" location during drag and drop operations.
     *
     * @return the placeholder model item for the "from" location
     */
    public final ObjectProperty<T> placeholderFromProperty() {
        return placeholderFrom;
    }

    public final void setPlaceholderFrom(T placeholderFrom) {
        this.placeholderFrom.set(placeholderFrom);
    }

    private final ObjectProperty<T> placeholderTo = new SimpleObjectProperty<>(this, "placeholderTo");

    public final T getPlaceholderTo() {
        return placeholderTo.get();
    }

    /**
     * A model item that represents the "to" location during drag and drop operations.
     *
     * @return the placeholder model item for the "to" location
     */
    public final ObjectProperty<T> placeholderToProperty() {
        return placeholderTo;
    }

    public final void setPlaceholderTo(T placeholderTo) {
        this.placeholderTo.set(placeholderTo);
    }

    /**
     * A special list cell to be used in combination with the {@link MultiColumnListView} control.
     * The cell adds drag and drop support for re-arranging list cells and for dragging them from
     * one column to another.
     *
     * @param <T> the type of items in the list
     */
    public static class ColumnListCell<T> extends ListCell<T> {

        private final MultiColumnListView<T> multiColumnListView;

        /**
         * Creates a new list cell.
         *
         * @param multiColumnListView reference to the {@link MultiColumnListView} control where the cell is being used
         */
        public ColumnListCell(MultiColumnListView<T> multiColumnListView) {
            this.multiColumnListView = multiColumnListView;

            getStyleClass().add("column-list-cell");

            fromPlaceholder.bind(itemProperty().isEqualTo(multiColumnListView.placeholderFromProperty()));
            toPlaceholder.bind(itemProperty().isEqualTo(multiColumnListView.placeholderToProperty()));
            placeholder.bind(fromPlaceholder.or(toPlaceholder));

            InvalidationListener updateDraggedPseudoStateListener = it -> updateDraggedPseudoState();

            multiColumnListView.draggedItemProperty().addListener(updateDraggedPseudoStateListener);
            itemProperty().addListener(updateDraggedPseudoStateListener);

            setOnDragDetected(event -> {
                if (multiColumnListView.isDisableDragAndDrop()) {
                    return;
                }

                log("drag detected");
                if (isEmpty() || getItem() == null) {
                    return;
                }

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

                multiColumnListView.setDraggedItem(getItem());

                multiColumnListView.getDraggedItems().setAll(getListView().getSelectionModel().getSelectedItems());

                ListUtils.replaceIf(getListView().getItems(), item -> item == getItem(), multiColumnListView.getPlaceholderFrom());
            });

            setOnDragOver(event -> {
                log("drag over");
                if (event.getGestureSource() != this && multiColumnListView.getPlaceholderFrom() != getItem()) {
                    log("   accepting, " + hashCode() + ", txt: " + getText());
                    updateItems(event);
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                } else {
                    log("   not accepting drag");
                    event.acceptTransferModes(TransferMode.NONE);
                }
            });

            setOnDragEntered(event -> log("drag entered"));

            setOnDragExited(event -> {
                log("drag exited");
                getListView().getItems().remove(multiColumnListView.getPlaceholderTo());
            });

            setOnDragDropped(event -> {
                log("drag dropped");

                if (!event.getAcceptedTransferMode().equals(TransferMode.MOVE)) {
                    return;
                }

                if (multiColumnListView.getPlaceholderFrom() == getItem()) {
                    log("   not performing drop, drop happened on 'from' placeholder");
                    return;
                }

                log("   performing drop");

                ListView<T> listView = getListView();
                ObservableList<T> items = listView.getItems();

                items.remove(multiColumnListView.getPlaceholderFrom());

                T draggedItem = multiColumnListView.getDraggedItem();
                ListUtils.replaceIf(items, item -> item == multiColumnListView.getPlaceholderTo(), draggedItem);

                if (!items.contains(draggedItem)) {
                    // probably dropped on same list view / same column (hence no "to" placeholder)
                    items.add(draggedItem);
                }

                listView.getSelectionModel().select(draggedItem);

                event.setDropCompleted(true);

                event.consume();
            });

            setOnDragDone(evt -> {
                if (evt.isAccepted()) {
                    log("drag done, accepted");
                    if (Objects.equals(evt.getAcceptedTransferMode(), TransferMode.MOVE)) {
                        log("   drop was completed, removing the 'from' placeholder");
                        getListView().getItems().removeIf(item -> item == multiColumnListView.getPlaceholderFrom());
                    } else {
                        log("   drop was not completed, replacing placeholder with dragged item");
                        ListUtils.replaceIf(getListView().getItems(), item -> item == multiColumnListView.getPlaceholderFrom(), multiColumnListView.getDraggedItem());
                    }
                } else {
                    log("drag done, not accepted");

                    // put the item back into the "from" location
                    log("putting item back into 'from' location");
                    ListUtils.replaceIf(getListView().getItems(), item -> item == multiColumnListView.getPlaceholderFrom(), multiColumnListView.getDraggedItem());
                }

                multiColumnListView.setDraggedItem(null);
                evt.consume();
            });
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

                T fromItem = multiColumnListView.getPlaceholderFrom();
                T toItem = multiColumnListView.getPlaceholderTo();

                int fromIndex = getListView().getItems().indexOf(fromItem);

                ObservableList<T> items = getListView().getItems();
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

        private void updateDraggedPseudoState() {
            T from = multiColumnListView.getPlaceholderFrom();
            pseudoClassStateChanged(PseudoClass.getPseudoClass("from"), from != null && from == getItem());

            T to = multiColumnListView.getPlaceholderTo();
            pseudoClassStateChanged(PseudoClass.getPseudoClass("to"), to != null && to == getItem());
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            updateDraggedPseudoState();

            if (!empty && item != null) {
                if (item == multiColumnListView.getPlaceholderFrom()) {
                    setText("From");
                } else if (item == multiColumnListView.getPlaceholderTo()) {
                    setText("To");
                } else {
                    setText(item.toString());
                }
            } else {
                setText("");
            }
        }

        private final ReadOnlyBooleanWrapper placeholder = new ReadOnlyBooleanWrapper(this, "placeholder");

        public final boolean isPlaceholder() {
            return placeholder.get();
        }

        /**
         * A read-only property that is being set to true if the item in the cell is currently
         * either one of the two placeholder items (see {@link MultiColumnListView#placeholderFromProperty()}
         * or {@link MultiColumnListView#placeholderToProperty()}).
         *
         * @return true if the currently shown item is either the "from" or the "to" placeholder object
         */
        public final ReadOnlyBooleanWrapper placeholderProperty() {
            return placeholder;
        }

        private final ReadOnlyBooleanWrapper fromPlaceholder = new ReadOnlyBooleanWrapper(this, "fromPlaceholder");

        public final boolean isFromPlaceholder() {
            return fromPlaceholder.get();
        }

        /**
         * A read-only property that is being set to true if the item in the cell is currently
         * the "from" placeholder item (see {@link MultiColumnListView#placeholderFromProperty()}).
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
         * the "to" placeholder item (see {@link MultiColumnListView#placeholderToProperty()}).
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
    }
}
