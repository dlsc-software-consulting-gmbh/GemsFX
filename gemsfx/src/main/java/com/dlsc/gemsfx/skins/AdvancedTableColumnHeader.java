package com.dlsc.gemsfx.skins;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.TableColumnHeader;

public class AdvancedTableColumnHeader extends TableColumnHeader {

    /**
     * Creates a new TableColumnHeader instance to visually represent the given
     * {@link TableColumnBase} instance.
     *
     * @param tc The table column to be visually represented by this instance.
     */
    public AdvancedTableColumnHeader(TableColumnBase tc) {
        super(tc);
    }

    @Override
    public void resizeColumnToFitContent(int maxRows) {
        super.resizeColumnToFitContent(maxRows);
    }
}
