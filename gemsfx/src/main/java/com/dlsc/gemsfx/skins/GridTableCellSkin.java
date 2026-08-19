package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.gridtable.GridTableCell;
import javafx.scene.control.skin.CellSkinBase;

/**
 * Skin for {@link GridTableCell}.
 * <p>
 * The skin delegates cell layout and behavior to {@link CellSkinBase}.
 *
 * @param <S> the row item type
 * @param <T> the cell value type
 */
public class GridTableCellSkin<S, T> extends CellSkinBase<GridTableCell<S, T>> {

    /**
     * Creates a skin for the given grid table cell.
     *
     * @param control the grid table cell rendered by this skin
     */
    public GridTableCellSkin(GridTableCell<S, T> control) {
        super(control);
    }
}
