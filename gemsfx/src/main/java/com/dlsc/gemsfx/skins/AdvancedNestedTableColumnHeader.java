package com.dlsc.gemsfx.skins;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;

public class AdvancedNestedTableColumnHeader extends NestedTableColumnHeader {

    /**
     * Creates a new NestedTableColumnHeader instance to visually represent the given
     * {@link TableColumnBase} instance.
     *
     * @param tc The table column to be visually represented by this instance.
     */
    public AdvancedNestedTableColumnHeader(TableColumnBase tc) {
        super(tc);
    }

    /**
     * Creates a new TableColumnHeader instance for the given TableColumnBase instance. The general pattern for
     * implementing this method is as follows:
     *
     * <ul>
     *     <li>If the given TableColumnBase instance is null, has no child columns, or if the given TableColumnBase
     *         instance equals the TableColumnBase instance returned by calling {@link #getTableColumn()}, then it is
     *         suggested to return a {@link TableColumnHeader} instance comprised of the given column.</li>
     *     <li>Otherwise, we can presume that the given TableColumnBase instance has child columns, and in this case
     *         it is suggested to return a {@link NestedTableColumnHeader} instance instead.</li>
     * </ul>
     *
     * <strong>Note: </strong>In most circumstances this method should not be overridden, but in some circumstances it
     * makes sense (e.g. testing, or when extreme customization is desired).
     *
     * @param col the table column
     * @return A new TableColumnHeader instance.
     */
    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
        return col == null || col.getColumns().isEmpty() || col == getTableColumn() ?
                new AdvancedTableColumnHeader(col) :
                new AdvancedNestedTableColumnHeader(col);
    }
}
