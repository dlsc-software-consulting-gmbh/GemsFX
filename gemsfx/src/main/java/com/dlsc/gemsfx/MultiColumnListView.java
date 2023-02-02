package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.MultiColumnListViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
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

public class MultiColumnListView<T> extends Control {

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

    private final ObjectProperty<Callback<MultiColumnListView<T>, ListView<T>>> listViewFactory = new SimpleObjectProperty<>(this, "listViewFactory", m -> new AutoscrollListView<>());

    public Callback<MultiColumnListView<T>, ListView<T>> getListViewFactory() {
        return listViewFactory.get();
    }

    public ObjectProperty<Callback<MultiColumnListView<T>, ListView<T>>> listViewFactoryProperty() {
        return listViewFactory;
    }

    public void setListViewFactory(Callback<MultiColumnListView<T>, ListView<T>> listViewFactory) {
        this.listViewFactory.set(listViewFactory);
    }

    private final ListProperty<ListViewColumn<T>> columns = new SimpleListProperty<>(this, "columns", FXCollections.observableArrayList());

    public final ObservableList<ListViewColumn<T>> getColumns() {
        return columns.get();
    }

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

    public final ObjectProperty<Callback<MultiColumnListView<T>, ColumnListCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public final void setCellFactory(Callback<MultiColumnListView<T>, ColumnListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public static class ListViewColumn<T> {

        private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

        public ObservableList<T> getItems() {
            return items.get();
        }

        public ListProperty<T> itemsProperty() {
            return items;
        }

        public void setItems(ObservableList<T> items) {
            this.items.set(items);
        }

        private final ObjectProperty<Node> header = new SimpleObjectProperty<>(this, "header", new Label("Column"));

        public Node getHeader() {
            return header.get();
        }

        public ObjectProperty<Node> headerProperty() {
            return header;
        }

        public void setHeader(Node header) {
            this.header.set(header);
        }

        private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>(this, "comparator", Comparator.comparing(Object::toString));

        public Comparator getComparator() {
            return comparator.get();
        }

        public ObjectProperty<Comparator<T>> comparatorProperty() {
            return comparator;
        }

        public void setComparator(Comparator comparator) {
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

    private void log(String text) {
        System.out.println(text);
    }

    public static class ColumnListCell<T> extends ListCell<T> {

        private final MultiColumnListView<T> multiColumnListView;

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
            System.out.println("from / to index: " + fromIndex + " / " + toIndex);
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

        private void log(String text) {
            System.out.println(text);
        }
    }
}
