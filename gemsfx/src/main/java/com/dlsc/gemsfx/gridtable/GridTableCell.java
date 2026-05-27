package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.skins.GridTableCellSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.geometry.Pos;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single cell in a {@link GridTableView} row.
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-mouse-transparent}</td><td>{@code boolean}</td><td>Whether the cell ignores mouse events.</td></tr>
 *   </tbody>
 * </table>
 *
 * @param <S> the type of the row item
 * @param <T> the type of the cell item
 */
public class GridTableCell<S, T> extends Cell<T> {

    private static final boolean DEFAULT_TRANSPARENT = false;
    private static final PseudoClass PSEUDO_CLASS_ODD = PseudoClass.getPseudoClass("odd");

    private static final PseudoClass PSEUDO_CLASS_EVEN = PseudoClass.getPseudoClass("even");

    private static final String DEFAULT_STYLE_CLASS = "grid-table-cell";

    /**
     * Constructs a new grid table cell.
     */
    public GridTableCell() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        setAlignment(Pos.BASELINE_LEFT);

        index.addListener(it -> {
            int index = getIndex();
            boolean active = index % 2 == 0;

            pseudoClassStateChanged(PSEUDO_CLASS_EVEN, active);
            pseudoClassStateChanged(PSEUDO_CLASS_ODD, !active);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new GridTableCellSkin<>(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param item the item value
     *
     * @param empty the empty value
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            GridTableColumn column = getColumn();
            if (column != null) {
                StringConverter converter = column.getConverter();
                if (converter != null) {
                    setText(converter.toString(item));
                } else {
                    setText(item.toString());
                }
            } else {
                setText(item.toString());
            }
        } else {
            setText("");
        }
    }

    private final ObjectProperty<S> rowItem = new SimpleObjectProperty<>(this, "rowItem");

    public S getRowItem() {
        return rowItem.get();
    }

    /**
     * Stores the row item shown by this cell.
     *
     * @return the row item property
     */
    public ObjectProperty<S> rowItemProperty() {
        return rowItem;
    }

    public void setRowItem(S rowItem) {
        this.rowItem.set(rowItem);
    }

    private final IntegerProperty index = new SimpleIntegerProperty(this, "index");

    /**
     * Stores the row index shown by this cell.
     *
     * @return the index property
     */
    public final IntegerProperty indexProperty() {
        return index;
    }

    public final int getIndex() {
        return index.get();
    }

    final void setIndex(int index) {
        this.index.set(index);
    }

    private final ObjectProperty<GridTableColumn<S, T>> column = new SimpleObjectProperty<>(this, "column");

    public final GridTableColumn getColumn() {
        return column.get();
    }

    /**
     * Stores the column that created this cell.
     *
     * @return the column property
     */
    public final ObjectProperty<GridTableColumn<S, T>> columnProperty() {
        return column;
    }

    final void setColumn(GridTableColumn column) {
        this.column.set(column);
    }


    private StyleableBooleanProperty transparent;

    /**
     * A custom property to enable CSS-based control of mouse transparency.
     * Since mouseTransparent cannot be directly set through CSS,
     * transparentProperty allows for CSS-driven adjustments,
     * facilitating easier styling without altering existing code.
     * <p>
     * Can be set via CSS using the {@code -fx-mouse-transparent} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code false}.
     * </p>
     *
     * @return the transparent property
     */
    public final StyleableBooleanProperty transparentProperty() {
        if (transparent == null) {
            transparent = new StyleableBooleanProperty(DEFAULT_TRANSPARENT) {

                /**
                 * {@inheritDoc}
                 */
                @Override
                protected void invalidated() {
                    if (!mouseTransparentProperty().isBound()) {
                        setMouseTransparent(get());
                    }
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the owning bean
                 */
                @Override
                public Object getBean() {
                    return GridTableCell.this;
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the property name
                 */
                @Override
                public String getName() {
                    return "transparent";
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the CSS metadata for this property
                 */
                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.TRANSPARENT;
                }
            };
        }
        return this.transparent;
    }

    public final boolean isTransparent() {
        return transparent == null || transparent.get();
    }

    public final void setTransparent(final boolean transparent) {
        this.transparentProperty().set(transparent);
    }

    private static class StyleableProperties {

        private static final CssMetaData<GridTableCell, Boolean> TRANSPARENT = new CssMetaData<>(
                "-fx-mouse-transparent", BooleanConverter.getInstance(), DEFAULT_TRANSPARENT) {

            /**
             * {@inheritDoc}
             *
             * @return the styleable property
             *
             * @param control the control to inspect
             */
            @Override
            public StyleableProperty<Boolean> getStyleableProperty(GridTableCell control) {
                return control.transparentProperty();
            }

            /**
             * {@inheritDoc}
             *
             * @return true if the property can be styled
             *
             * @param control the control to inspect
             */
            @Override
            public boolean isSettable(GridTableCell control) {
                return control.transparent == null || !control.transparent.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Cell.getClassCssMetaData());
            Collections.addAll(styleables, TRANSPARENT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the supported CSS metadata
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the CSS metadata supported by this control
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

}
