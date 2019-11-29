package com.dlsc.gemsfx.keyboard;

import com.dlsc.gemsfx.keyboard.KeyboardView.Mode;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Objects;


/**
 * This is the model required by the {@link KeyboardView} to configure its layout /
 * its keys. A keyboard defines a list of rows and each row a list of keys. Each key
 * manages a list of characters, a list of "shift" characters, and a list of symbols.
 * Depending on the mode the keyboard view is in the user will see characters from
 * either one of these lists (see {@link KeyboardView#modeProperty()}.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "minWidth", "prefWidth", "maxWidth", "rows"})
public class Keyboard {

    /**
     * The keys of a keyboard are laid out via a standard JavaFX GridPane.
     * The column span of each key is defined by this constant and equals "4".
     * Regular character keys like "a", "b", etc ... use this default value.
     * Keys like ENTER on the other hand are bigger / span more columns.
     */
    public static final int DEFAULT_COLUMN_SPAN = 4;

    /**
     * Constructs a new keyboard with the given name. This name will be shown
     * inside the keyboard selector popover when the user wants to switch from
     * one keyboard to another.
     *
     * @param name the keyboard's name
     */
    public Keyboard(String name) {
        setName(name);
    }

    /**
     * Constructs a new keyboard.
     */
    public Keyboard() {
    }

    /**
     * Creates a new keyboard instance. Starting point of the fluid API approach.
     *
     * @return a new keyboard instance
     */
    public static Keyboard create() {
        return new Keyboard();
    }

    private final StringProperty name = new SimpleStringProperty(this, "name", "Untitled");

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get();
    }

    /**
     * Stores the name of the keyboard.
     */
    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public final Keyboard withName(String name) {
        setName(name);
        return this;
    }

    private final DoubleProperty minWidth = new SimpleDoubleProperty(this, "minWidth", 0);

    @XmlAttribute(name = "minWidth")
    public double getMinWidth() {
        return minWidth.get();
    }

    /**
     * Stores the minimum width of the keyboard.
     */
    public DoubleProperty minWidthProperty() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        this.minWidth.set(minWidth);
    }

    public final Keyboard withMinWidth(double width) {
        setMinWidth(width);
        return this;
    }

    private final DoubleProperty maxWidth = new SimpleDoubleProperty(this, "maxWidth", Double.MAX_VALUE);

    @XmlAttribute(name = "maxWidth")
    public double getMaxWidth() {
        return maxWidth.get();
    }

    /**
     * Stores the maximum width of the keyboard.
     */
    public DoubleProperty maxWidthProperty() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth.set(maxWidth);
    }

    public final Keyboard withMaxWidth(double width) {
        setMaxWidth(width);
        return this;
    }

    private final DoubleProperty prefWidth = new SimpleDoubleProperty(this, "prefWidth", Region.USE_COMPUTED_SIZE);

    @XmlAttribute(name = "prefWidth")
    public double getPrefWidth() {
        return prefWidth.get();
    }

    /**
     * Stores the preferred width of the keyboard.
     */
    public DoubleProperty prefWidthProperty() {
        return prefWidth;
    }

    public void setPrefWidth(double prefWidth) {
        this.prefWidth.set(prefWidth);
    }

    public final Keyboard withPrefWidth(double width) {
        setPrefWidth(width);
        return this;
    }

    private final ObservableList<Row> rows = FXCollections.observableArrayList();

    /**
     * The list of rows.
     *
     * @return the rows
     */
    @XmlElement(name = "row")
    public final ObservableList<Row> getRows() {
        return rows;
    }

    public final Keyboard withRows(Row... rows) {
        getRows().setAll(rows);
        return this;
    }

    public static class Row {

        public Row() {
            getKeys().addListener((Observable it) -> {
                if (getNode() != null) {
                    throw new IllegalArgumentException("the row already contains a node and can not support keys at the same time");
                }
            });

            nodeProperty().addListener((Observable it) -> {
                if (!getKeys().isEmpty()) {
                    throw new IllegalArgumentException("the row already contains keys and can not support a node at the same time");
                }
            });
        }

        public static Row create() {
            return new Row();
        }

        public Row withKeys(KeyBase... keys) {
            getKeys().setAll(keys);
            return this;
        }

        public Row withNode(Node node) {
            setNode(node);
            return this;
        }

        private final ObjectProperty<Node> node = new SimpleObjectProperty<>(this, "node");

        /**
         * A node that will represent the row. This is a way to add a toolbar to the keyboard
         * that could contain things like undo / redo or cut / copy / paste buttons. The node
         * should be a custom control with an empty constructor so that it can be marshalled
         * and unmarshalled via JAXB. A custom java type adapter has been added to this field
         * for this to work.
         */
        @XmlElement(name = "node")
        @XmlJavaTypeAdapter(NodeAdapter.class)
        public Node getNode() {
            return node.get();
        }

        public ObjectProperty<Node> nodeProperty() {
            return node;
        }

        public void setNode(Node node) {
            this.node.set(node);
        }

        @XmlElementWrapper(name = "keys")
        @XmlElements({
                @XmlElement(name = "key", type = Key.class),
                @XmlElement(name = "special", type = SpecialKey.class)
        })
        private final ObservableList<KeyBase> keys = FXCollections.observableArrayList();

        /**
         * The list of keys.
         *
         * @return the keys
         */
        public final ObservableList<KeyBase> getKeys() {
            return keys;
        }
    }

    public abstract static class KeyBase<SELF extends KeyBase> {

        public KeyBase() {
        }

        private final ObservableList<String> styleClass = FXCollections.observableArrayList();

        /**
         * Each key can be customized by adding a style class to it and defining it in the
         * application's CSS file.
         *
         * @return the key's extra style class
         */
        public final ObservableList<String> getStyleClass() {
            return styleClass;
        }

        public final SELF withColumnSpan(int span) {
            setColumnSpan(span);
            return (SELF) this;
        }

        public final SELF withRowSpan(int span) {
            setRowSpan(span);
            return (SELF) this;
        }

        private final IntegerProperty columnSpan = new SimpleIntegerProperty(this, "columnSpan", DEFAULT_COLUMN_SPAN);

        @XmlAttribute(name = "cols")
        public int getColumnSpan() {
            return columnSpan.get();
        }

        /**
         * The number of columns the key is spanning inside the parent
         * GridPane container. The default is equal to {@link #DEFAULT_COLUMN_SPAN}.
         *
         * @return the column span property
         */
        public IntegerProperty columnSpanProperty() {
            return columnSpan;
        }

        public void setColumnSpan(int columnSpan) {
            this.columnSpan.set(columnSpan);
        }

        private final IntegerProperty rowSpan = new SimpleIntegerProperty(this, "rowSpan", 1);

        @XmlAttribute(name = "rows")
        public int getRowSpan() {
            return rowSpan.get();
        }

        /**
         * The number of rows the key is spanning inside the parent
         * GridPane container.
         *
         * @return the row span property
         */
        public IntegerProperty rowSpanProperty() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan.set(rowSpan);
        }
    }

    /**
     * The model class representing a regular character key (e.g. "a", "s", "d", ...).
     * The key manages three lists of type String. One list containing the characters
     * that can be entered / typed in {@link Mode#STANDARD}, one list for {@link Mode#SHIFT} or
     * {@link Mode#CAPS}, and one list for {@link Mode#SYMBOLS}.
     *
     * @see Row#getKeys()
     */
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class Key extends KeyBase<Key> {

        public Key() {
        }

        public static final Key create() {
            return new Key();
        }

        @XmlList
        @XmlAttribute(name = "chars")
        private final ObservableList<String> characters = FXCollections.observableArrayList();

        public final ObservableList<String> getCharacters() {
            return characters;
        }

        public final void setCharacters(List<String> characters) {
            getCharacters().setAll(characters);
        }

        @XmlList
        @XmlAttribute(name = "shifts")
        private final ObservableList<String> shiftCharacters = FXCollections.observableArrayList();

        public final ObservableList<String> getShiftCharacters() {
            return shiftCharacters;
        }

        @XmlList
        @XmlAttribute(name = "symbols")
        private final ObservableList<String> symbols = FXCollections.observableArrayList();

        public final ObservableList<String> getSymbols() {
            return symbols;
        }


        public final Key withCharacters(String... characters) {
            getCharacters().setAll(characters);
            return this;
        }

        public final Key withShiftCharacters(String... characters) {
            getShiftCharacters().setAll(characters);
            return this;
        }

        public final Key withSymbols(String... symbols) {
            getSymbols().setAll(symbols);
            return this;
        }

        public final Key withShowShift(boolean show) {
            setShowShiftSymbol(show);
            return this;
        }

        private final BooleanProperty showShiftSymbol = new SimpleBooleanProperty(this, "showShiftSymbol", false);

        @XmlAttribute(name = "show-shift")
        public boolean isShowShiftSymbol() {
            return showShiftSymbol.get();
        }

        public BooleanProperty showShiftSymbolProperty() {
            return showShiftSymbol;
        }

        public void setShowShiftSymbol(boolean showShiftSymbol) {
            this.showShiftSymbol.set(showShiftSymbol);
        }
    }

    /**
     * The model class representing a special keys (e.g. "TAB", "SHIFT", "CAPS", ...).
     *
     * @see Row#getKeys()
     */
    public static class SpecialKey extends KeyBase<SpecialKey> {

        /**
         * Types of special keys (e.g. "space", "shift", "caps", ...).
         */
        public enum Type {
            SPACE,
            SHIFT,
            ENTER,
            TAB,
            BACKSPACE,
            CAPS,
            KEYBOARDS,
            SPECIAL_CHARACTERS,
            MICROPHONE,
            HIDE,
            PLUS,
            MINUS,
            MULTIPLY,
            DIVIDE,
            EQUALS,
            UP,
            DOWN,
            LEFT,
            RIGHT
        }

        private Type type;

        private Pos alignment;

        /**
         * Constructs a new special key.
         *
         * @param type the type of the key (e.g. "SPACE")
         * @param alignment the alignment of the content inside the key (e.g. for the text or icon shown by the key)
         */
        public SpecialKey(Type type, Pos alignment) {
            this.type = Objects.requireNonNull(type);
            this.alignment = Objects.requireNonNull(alignment);
        }

        /**
         * Constructs a new special key with default content alignment of CENTER.
         *
         * @param type the type of the key (e.g. "SPACE")
         */
        public SpecialKey(Type type) {
            this(type, Pos.CENTER);
        }

        /**
         * Constructs a new special key.
         */
        public SpecialKey() {
        }

        /**
         * Starting point of the fluid API to create and configure special keys.
         *
         * @return a new special key
         */
        public static final SpecialKey create() {
            return new SpecialKey();
        }

        public SpecialKey withType(Type type) {
            setType(type);
            return this;
        }

        public SpecialKey withAlignment(Pos alignment) {
            setAlignment(alignment);
            return this;
        }

        /**
         * Returns the type of the special key, e.g. "space".
         *
         * @return the type of the key
         */
        @XmlAttribute(name = "type")
        public Type getType() {
            return type;
        }

        /**
         * Sets the type of the special key, e.g. "space".
         *
         * @param type the type of the key
         */
        public void setType(Type type) {
            this.type = type;
        }

        /**
         * Returns the content alignment of the special key, e.g. "center".
         *
         * @return the content alignment of the key
         */
        @XmlAttribute(name = "alignment")
        public Pos getAlignment() {
            return alignment;
        }

        /**
         * Sets the content alignment of the special key, e.g. "center".
         *
         * @param alignment the content alignment of the key
         */
        public void setAlignment(Pos alignment) {
            this.alignment = alignment;
        }
    }

    /**
     * A custom XML adapter for marshalling and unmarshalling JavaFX nodes.
     * This adapter is required for nodes set on a row (see {@link Row#setNode(Node)}).
     * Row nodes require a custom control with an empty constructor.
     */
    public static class NodeAdapter extends XmlAdapter<String, Node> {

        @Override
        public Node unmarshal(String v) throws Exception {
            return (Node) Class.forName(v).newInstance();
        }

        @Override
        public String marshal(Node v) {
            return v.getClass().getName();
        }
    }
}
