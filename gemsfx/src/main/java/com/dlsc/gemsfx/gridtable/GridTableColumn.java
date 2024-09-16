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
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * @param <S> table item type
 * @param <T> table cell value type
 */
public class GridTableColumn<S, T> extends ColumnConstraints {

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

    public GridTableColumn(Node graphic) {
        this(null, graphic);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public GridTableColumn(String text) {
        this(text, null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    public GridTableColumn() {
        this("", null);
    }

    private final ObservableList<String> styleClass = FXCollections.observableArrayList();

    public ObservableList<String> getStyleClass() {
        return styleClass;
    }

    private final ObjectProperty<ContentDisplay> contentDisplay = new SimpleObjectProperty<>(this, "contentDisplay", ContentDisplay.LEFT);

    public ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

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

    public ReadOnlyObjectProperty<Node> headerProperty() {
        return header.getReadOnlyProperty();
    }

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
        cell.updateItem(item, rowItem == null);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // step 5: return the cell
        return cell;
    }
}