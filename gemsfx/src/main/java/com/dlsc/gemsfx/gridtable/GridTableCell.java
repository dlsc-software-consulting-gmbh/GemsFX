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

public class GridTableCell<S, T> extends Cell<T> {

    private static final boolean DEFAULT_TRANSPARENT = false;
    private static final PseudoClass PSEUDO_CLASS_ODD = PseudoClass.getPseudoClass("odd");

    private static final PseudoClass PSEUDO_CLASS_EVEN = PseudoClass.getPseudoClass("even");

    private static final String DEFAULT_STYLE_CLASS = "grid-table-cell";

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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GridTableCellSkin<>(this);
    }

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

    public ObjectProperty<S> rowItemProperty() {
        return rowItem;
    }

    public void setRowItem(S rowItem) {
        this.rowItem.set(rowItem);
    }

    private final IntegerProperty index = new SimpleIntegerProperty(this, "index");

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
     */
    public final StyleableBooleanProperty transparentProperty() {
        if (transparent == null) {
            transparent = new StyleableBooleanProperty(DEFAULT_TRANSPARENT) {

                @Override
                protected void invalidated() {
                    if (!mouseTransparentProperty().isBound()) {
                        setMouseTransparent(get());
                    }
                }

                @Override
                public Object getBean() {
                    return GridTableCell.this;
                }

                @Override
                public String getName() {
                    return "transparent";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.TRANSPARENT;
                }
            };
        }
        return this.transparent;
    }

    public final boolean getTransparent() {
        return transparent == null || transparent.get();
    }

    public final void setTransparent(final boolean transparent) {
        this.transparentProperty().set(transparent);
    }

    private static class StyleableProperties {

        private static final CssMetaData<GridTableCell, Boolean> TRANSPARENT = new CssMetaData<>(
                "-fx-mouse-transparent", BooleanConverter.getInstance(), DEFAULT_TRANSPARENT) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(GridTableCell control) {
                return control.transparentProperty();
            }

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

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

}
