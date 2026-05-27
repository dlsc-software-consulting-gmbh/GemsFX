package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.util.SimpleStringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * @param <S> table item type
 * @param <T> table cell value type
 */
public class GridTableColumn<S, T> extends ColumnConstraints {

    /**
     * Constructs a new grid table column.
     *
     * @param text the column text
     * @param graphic the column graphic
     */
    public GridTableColumn(String text, Node graphic) {
        setText(text);
        setGraphic(graphic);

        setHgrow(Priority.ALWAYS);
        setFillWidth(true);

        Label label = new Label();
        label.textProperty().bind(textProperty());
        label.graphicProperty().bind(graphicProperty());
        label.contentDisplayProperty().bind(contentDisplayProperty());
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        header.addListener(it -> {
            Node header = getHeader();
            if (header != null && !header.getStyleClass().contains("column-header")) {
                header.getStyleClass().add("column-header");
            }
        });

        header.set(label);
    }

    /**
     * Constructs a new grid table column that only shows a graphic.
     *
     * @param graphic the column graphic
     */
    public GridTableColumn(Node graphic) {
        this(null, graphic);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Constructs a new grid table column that only shows text.
     *
     * @param text the column text
     */
    public GridTableColumn(String text) {
        this(text, null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    /**
     * Constructs a new grid table column with an empty header.
     */
    public GridTableColumn() {
        this("", null);
    }

    private final ObservableList<String> styleClass = FXCollections.observableArrayList();

    /**
     * Returns the style classes applied to cells created by this column.
     *
     * @return the style classes
     */
    public ObservableList<String> getStyleClass() {
        return styleClass;
    }

    private final ObjectProperty<ContentDisplay> contentDisplay = new SimpleObjectProperty<>(this, "contentDisplay", ContentDisplay.LEFT);

    public ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

    /**
     * Stores how the text and graphic should be arranged in the header.
     *
     * @return the content display property
     */
    public ObjectProperty<ContentDisplay> contentDisplayProperty() {
        return contentDisplay;
    }

    public void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(contentDisplay);
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public Node getGraphic() {
        return graphic.get();
    }

    /**
     * Stores the graphic shown in the header.
     *
     * @return the graphic property
     */
    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    private final StringProperty text = new SimpleStringProperty(this, "text", "Header");

    public String getText() {
        return text.get();
    }

    /**
     * Stores the text shown in the header.
     *
     * @return the text property
     */
    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter", new SimpleStringConverter<>());

    public StringConverter<T> getConverter() {
        return converter.get();
    }

    /**
     * Stores the converter used to turn cell values into text.
     *
     * @return the converter property
     */
    public ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter.set(converter);
    }

    // table cell factory

    private final ObjectProperty<Callback<GridTableView<S>, GridTableCell<S, T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory", param -> new GridTableCell<>());

    public final Callback<GridTableView<S>, GridTableCell<S, T>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * Stores the factory used to create cells for this column.
     *
     * @return the cell factory property
     */
    public final ObjectProperty<Callback<GridTableView<S>, GridTableCell<S, T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public final void setCellFactory(Callback<GridTableView<S>, GridTableCell<S, T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public ObjectProperty<Callback<S, T>> cellValueFactory = new SimpleObjectProperty<>(this, "cellValueFactory");

    public Callback<S, T> getCellValueFactory() {
        return cellValueFactory.get();
    }

    /**
     * Stores the factory used to extract cell values from row items.
     *
     * @return the cell value factory property
     */
    public ObjectProperty<Callback<S, T>> cellValueFactoryProperty() {
        return cellValueFactory;
    }

    public void setCellValueFactory(Callback<S, T> cellValueFactory) {
        this.cellValueFactory.set(cellValueFactory);
    }

    private final ReadOnlyObjectWrapper<Node> header = new ReadOnlyObjectWrapper<>(this, "header");

    public Node getHeader() {
        return header.get();
    }

    /**
     * A read-only property containing the header node.
     *
     * @return the header property
     */
    public ReadOnlyObjectProperty<Node> headerProperty() {
        return header.getReadOnlyProperty();
    }

    /**
     * Creates a new cell for the given table view and row index.
     *
     * @param tableView the owning table view
     * @param index the row index
     * @return the created cell
     */
    public final GridTableCell<S, T> createCell(GridTableView<S> tableView, int index) {
        // step 1: retrieve the model object for the given row (index) from the table view
        S rowItem = null;
        T item = null;

        if (index < tableView.getItems().size()) {
            rowItem = tableView.getItems().get(index);
            // step 2: retrieve the cell value from the model object
            Callback<S, T> valueFactory = getCellValueFactory();

            if (valueFactory != null) {
                item = valueFactory.call(rowItem);
            }
        }

        // step 3: create the cell
        Callback<GridTableView<S>, GridTableCell<S, T>> cellFactory = getCellFactory();
        GridTableCell<S, T> cell = cellFactory.call(tableView);
        cell.getStyleClass().addAll(getStyleClass());

        // step 4: configure the cell
        cell.setColumn(this);
        cell.setRowItem(rowItem);
        cell.setIndex(index);
        cell.updateItem(item, rowItem == null);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        cell.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, evt -> {
            if (evt.isConsumed()) {
                return;
            }

            Callback<S, ContextMenu> contextMenuCallback = tableView.getOnContextMenuForItemRequested();
            if (contextMenuCallback != null) {
                ContextMenu con = contextMenuCallback.call(cell.getRowItem());
                if (con != null) {
                    evt.consume();
                    con.show(cell, evt.getScreenX(), evt.getScreenY());
                }
            }
        });

        // step 5: return the cell
        return cell;
    }
}
