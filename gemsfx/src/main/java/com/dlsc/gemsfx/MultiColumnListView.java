package com.dlsc.gemsfx;

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

    private final ObjectProperty<Callback<MultiColumnListView<T>, ListView<T>>> listViewFactory = new SimpleObjectProperty<>(this, "listViewFactory", m -> new ListView<>());

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

    public static class ColumnListCell<T> extends ListCell<T> {

        private final MultiColumnListView<T> multiColumnListView;

        public ColumnListCell(MultiColumnListView<T> multiColumnListView) {
            this.multiColumnListView = multiColumnListView;

            getStyleClass().add("column-list-cell");

            InvalidationListener updateDraggedPseudoStateListener = it -> updateDraggedPseudoState();

            multiColumnListView.draggedItemProperty().addListener(updateDraggedPseudoStateListener);
            itemProperty().addListener(updateDraggedPseudoStateListener);

            setOnDragDetected(event -> {
                System.out.println("drag detected");
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
                System.out.println("drag over");
                if (event.getGestureSource() != this && multiColumnListView.getPlaceholderFrom() != getItem() && multiColumnListView.getPlaceholderTo() != getItem()) {
                    System.out.println("accepting, " + hashCode() + ", txt: " + getText());
                    update(event);
                    event.consume();
                }
                event.acceptTransferModes(TransferMode.MOVE);
            });

            setOnDragEntered(event -> {
                System.out.println("drag entered");
                //update(event);
            });

            setOnDragExited(event -> {
                System.out.println("drag exited");
//                if (event.getGestureSource() != this) {
                    getListView().getItems().remove(multiColumnListView.getPlaceholderTo());
//                }
            });

            setOnDragDropped(event -> {
                System.out.println("drag dropped");

                update(event);

                getListView().getItems().remove(multiColumnListView.getPlaceholderFrom());

                getListView().getItems().replaceAll(item -> {
                    if (item == multiColumnListView.getPlaceholderTo()) {
                        return multiColumnListView.getDraggedItem();
                    }

                    return item;
                });

                event.setDropCompleted(true);

                event.consume();
            });

            setOnDragDone(evt -> {
                System.out.println("drag done");
                evt.consume();
                getListView().getItems().removeIf(item -> item == multiColumnListView.getPlaceholderFrom());
                multiColumnListView.setDraggedItem(null);
            });
        }

        private void update(DragEvent event) {
            if (event.getGestureSource() != this) {
                int index = getIndex();

                T toItem = multiColumnListView.getPlaceholderTo();
                ObservableList<T> items = getListView().getItems();
                System.out.println("item count: " + items.size());
                items.remove(toItem);
                System.out.println("item count now: " + items.size());

                if (event.getY() < getHeight() / 2) {
                    System.out.println("   attempt to add ABOVE");
                    if (index > 0) {
                        System.out.println("      adding dragged item at index " + index);
                        items.add(Math.min(index, items.size()), toItem);
                    } else {
                        System.out.println("      adding dragged item at index 0");
                        items.add(0, toItem);
                    }
                } else {
                    System.out.println("   attempt to add BELOW");
                    if (index < items.size() - 1) {
                        System.out.println("      adding placeholder at index + index");
                        items.add(index, toItem);
                    } else {
                        items.add(toItem);
                    }
                }
            }
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
    }
}
