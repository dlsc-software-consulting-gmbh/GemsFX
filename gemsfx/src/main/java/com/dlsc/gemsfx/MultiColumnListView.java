package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.MultiColumnListViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
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
import javafx.util.Callback;

import java.util.Comparator;
import java.util.Objects;

/**<
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
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MultiColumnListViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return MultiColumnListView.class.getResource("multi-column-list-view.css").toExternalForm();
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

    private final ObjectProperty<Callback<MultiColumnListView<T>, ColumnListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory", listView -> new ColumnListCell<T>(listView));

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

    /**
     * The model object representing a single column. The type of the items in all columns must be the
     * same.
     *
     * @param <T> the type of items shown by the column
     */
    public static class ListViewColumn<T> {

        private final ObservableList<T> items = FXCollections.observableArrayList();

        /**
         * The data shown in the column.
         *
         * @return the model for this column
         */
        public final ObservableList<T> getItems() {
            return items;
        }

        private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header", new Label("Column Header"));

        public final Node getHeader() {
            return header.get();
        }

        public final ObjectProperty<Node> headerProperty() {
            return header;
        }

        public final void setHeader(Node header) {
            this.header.set(header);
        }

        private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>(this, "comparator", Comparator.comparing(Object::toString));

        public final Comparator getComparator() {
            return comparator.get();
        }

        public final ObjectProperty<Comparator<T>> comparatorProperty() {
            return comparator;
        }

        public final void setComparator(Comparator comparator) {
            this.comparator.set(comparator);
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

    public T getPlaceholderFrom() {
        return placeholderFrom.get();
    }

    public ObjectProperty<T> placeholderFromProperty() {
        return placeholderFrom;
    }

    public void setPlaceholderFrom(T placeholderFrom) {
        this.placeholderFrom.set(placeholderFrom);
    }

    private final ObjectProperty<T> placeholderTo = new SimpleObjectProperty<>(this, "placeholderTo");

    public T getPlaceholderTo() {
        return placeholderTo.get();
    }

    public ObjectProperty<T> placeholderToProperty() {
        return placeholderTo;
    }

    public void setPlaceholderTo(T placeholderTo) {
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

            InvalidationListener updateDraggedPseudoStateListener = it -> updateDraggedPseudoState();

            multiColumnListView.draggedItemProperty().addListener(updateDraggedPseudoStateListener);
            itemProperty().addListener(updateDraggedPseudoStateListener);

            setOnDragDetected(event -> {
                log("drag detected");
                if (isEmpty() || getItem() == null) {
                    return;
                }

                ClipboardContent content = new ClipboardContent();
                content.putString(Integer.toString(getIndex()));

                WritableImage snapshot = snapshot(null, null);

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                dragboard.setContent(content);
                dragboard.setDragView(snapshot);

                event.consume();

                multiColumnListView.setDraggedItem(getItem());

                multiColumnListView.getDraggedItems().setAll(getListView().getSelectionModel().getSelectedItems());

                getListView().getItems().replaceAll(item -> {
                    if (item == getItem()) {
                        return multiColumnListView.getPlaceholderFrom();
                    }

                    return item;
                });
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

            setOnDragEntered(event -> {
                log("drag entered");
            });

            setOnDragExited(event -> {
                log("drag exited");
                getListView().getItems().remove(multiColumnListView.getPlaceholderTo());
            });

            setOnDragDropped(event -> {
                log("drag dropped");

                if (multiColumnListView.getPlaceholderFrom() == getItem()) {
                    log("   not performing drop, drop happened on 'from' placeholder");
                    return;
                }

                log("   performing drop");

                ListView<T> listView = getListView();
                ObservableList<T> items = listView.getItems();

                items.remove(multiColumnListView.getPlaceholderFrom());

                T draggedItem = multiColumnListView.getDraggedItem();
                items.replaceAll(item -> {
                    if (item == multiColumnListView.getPlaceholderTo()) {
                        return draggedItem;
                    }

                    return item;
                });

                listView.getSelectionModel().select(draggedItem);

                event.setDropCompleted(true);

                event.consume();
            });

            setOnDragDone(evt -> {
                log("drag done");
                if (Objects.equals(evt.getAcceptedTransferMode(), TransferMode.MOVE)) {
                    log("   drop was completed, removing the 'from' placeholder");
                    getListView().getItems().removeIf(item -> item == multiColumnListView.getPlaceholderFrom());
                } else {
                    log("   drop was not completed, replacing placeholder with dragged item");
                    getListView().getItems().replaceAll(item -> {
                        if (item == multiColumnListView.getPlaceholderFrom()) {
                            return multiColumnListView.getDraggedItem();
                        }
                        return item;
                    });
                }

                multiColumnListView.setDraggedItem(null);
                evt.consume();
            });
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
            T to = multiColumnListView.getPlaceholderTo();
            pseudoClassStateChanged(PseudoClass.getPseudoClass("dragged"), (from != null && from == getItem()) || to != null && to == getItem());
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

        // for quick and dirty logging / debugging
        private void log(String text) {
            // System.out.println(text);
        }
    }
}
