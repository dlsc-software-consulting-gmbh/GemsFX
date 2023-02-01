package com.dlsc.gemsfx.demo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

public class PropertyView extends Control {

    public PropertyView() {
        getStyleClass().add("property-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PropertyViewSkin(this);
    }

    private final ObservableList<Item<?>> items = FXCollections.observableArrayList();

    public final ObservableList<Item<?>> getItems() {
        return items;
    }

    private final DoubleProperty minLabelWidth = new SimpleDoubleProperty(this, "minLabelWidth", Region.USE_PREF_SIZE);

    public double getMinLabelWidth() {
        return minLabelWidth.get();
    }

    public DoubleProperty minLabelWidthProperty() {
        return minLabelWidth;
    }

    public void setMinLabelWidth(double minLabelWidth) {
        this.minLabelWidth.set(minLabelWidth);
    }

    public static class StringItem extends Item<String> {

        private boolean wrapText;

        private double wrapWidth = 500;

        public StringItem(String name, String value) {
            super(name, value);
        }

        public StringItem(String name, String value, boolean editable) {
            super(name, value, editable);
        }

        public StringItem(String name, String value, boolean editable, Callback<String, String> stringProvider) {
            super(name, value, editable, stringProvider);
        }

        public boolean isWrapText() {
            return wrapText;
        }

        public void setWrapText(boolean wrapText) {
            this.wrapText = wrapText;
        }

        public double getWrapWidth() {
            return wrapWidth;
        }

        public void setWrapWidth(double wrapWidth) {
            this.wrapWidth = wrapWidth;
        }

        @Override
        public void edit(Node owner) {
            TextField textfield = new TextField();
            textfield.setPrefColumnCount(30);
            textfield.setMaxWidth(Double.MAX_VALUE);
            textfield.textProperty().bindBidirectional(valueProperty());
            textfield.setOnAction(evt -> textfield.getScene().getWindow().hide());
            showNode(owner, textfield);
        }
    }

    public static class Item<T> {

        public Item(String name, T value) {
            this(name, value, true);
        }

        public Item(String name, T value, boolean editable) {
            this(name, value, editable, null);
        }

        public Item(String name, T value, boolean editable, Callback<T, String> stringProvider) {
            setName(name);
            setValue(value);
            setEditable(editable);

            if (stringProvider != null) {
                setStringProvider(stringProvider);
            }

            Callback<Item, Node> nodeFactory = item -> {
                Label label = new Label();
                label.getStyleClass().add("value-label");
                label.setMouseTransparent(true);
                label.setWrapText(true);

                updateLabel(label);

                if (item instanceof StringItem) {
                    StringItem stringItem = (StringItem) item;
                    if (stringItem.isWrapText()) {
                        label.setWrapText(true);
                        label.setPrefWidth(stringItem.getWrapWidth());
                        label.setMinHeight(Region.USE_PREF_SIZE);
                    }
                }

                valueProperty().addListener(it -> updateLabel(label));
                return label;
            };

            setNodeFactory(nodeFactory);
        }

        private void updateLabel(Label label) {
            Object itemValue = getValue();
            Callback itemStringProvider = getStringProvider();
            Object text = itemStringProvider.call(itemValue);
            label.setText(text.toString());
        }

        public void edit(Node owner) {
        }

        protected PopOver showNode(Node owner, Node content) {
            PopOver popOver = getPopOver();
            popOver.setContentNode(content);
            popOver.setArrowLocation(findPopOverArrowLocation(owner));
            popOver.show(owner);
            return popOver;
        }

        private PopOver.ArrowLocation findPopOverArrowLocation(Node view) {
            Bounds localBounds = view.getBoundsInLocal();
            Bounds entryBounds = view.localToScreen(localBounds);

            ObservableList<Screen> screens = Screen.getScreensForRectangle(
                    entryBounds.getMinX(), entryBounds.getMinY(),
                    entryBounds.getWidth(), entryBounds.getHeight());
            Rectangle2D screenBounds = screens.get(0).getVisualBounds();

//        double spaceLeft = entryBounds.getMinX();
//        double spaceRight = screenBounds.getWidth() - entryBounds.getMaxX();
            double spaceTop = entryBounds.getMinY();
            double spaceBottom = screenBounds.getHeight() - entryBounds.getMaxY();

            if (spaceBottom > spaceTop) {
                return PopOver.ArrowLocation.TOP_CENTER;
            }

            return PopOver.ArrowLocation.BOTTOM_CENTER;
        }

        private PopOver getPopOver() {
            PopOver popOver = new PopOver();
            popOver.setAutoFix(true);
            popOver.setAutoHide(true);
            popOver.setDetachable(false);
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            return popOver;
        }

        private Callback<Item, Node> nodeFactory;

        public Callback<Item, Node> getNodeFactory() {
            return nodeFactory;
        }

        public void setNodeFactory(Callback<Item, Node> nodeFactory) {
            this.nodeFactory = nodeFactory;
        }

        private final StringProperty name = new SimpleStringProperty();

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

        public T getValue() {
            return value.get();
        }

        public ObjectProperty<T> valueProperty() {
            return value;
        }

        public void setValue(T value) {
            this.value.set(value);
        }

        private final ObjectProperty<Callback<T, String>> stringProvider = new SimpleObjectProperty<>(this, "stringProvider", value -> {
            if (value != null) {
                return value.toString();
            }
            return "";
        });

        public Callback<T, String> getStringProvider() {
            return stringProvider.get();
        }

        public ObjectProperty<Callback<T, String>> stringProviderProperty() {
            return stringProvider;
        }

        public void setStringProvider(Callback<T, String> stringProvider) {
            this.stringProvider.set(stringProvider);
        }

        private final ObjectProperty<Callback<T, Node>> editorProvider = new SimpleObjectProperty<>(this, "editorProvider");

        public Callback<T, Node> getEditorProvider() {
            return editorProvider.get();
        }

        public ObjectProperty<Callback<T, Node>> editorProviderProperty() {
            return editorProvider;
        }

        public void setEditorProvider(Callback<T, Node> editorProvider) {
            this.editorProvider.set(editorProvider);
        }

        private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

        public boolean isEditable() {
            return editable.get();
        }

        public BooleanProperty editableProperty() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable.set(editable);
        }
    }

    private boolean editing;

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
