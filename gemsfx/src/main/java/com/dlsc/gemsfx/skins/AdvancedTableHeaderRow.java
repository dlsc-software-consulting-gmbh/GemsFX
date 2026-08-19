package com.dlsc.gemsfx.skins;

import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkinBase;

/**
 * A table header row that creates advanced nested column headers.
 * <p>
 * The row is installed by {@link AdvancedTableViewSkin} and supplies an
 * {@link AdvancedNestedTableColumnHeader} as its root header.
 */
public class AdvancedTableHeaderRow extends TableHeaderRow {

    /**
     * Creates a new TableHeaderRow instance to visually represent the column
     * header area of controls such as {@link TableView} and
     * {@link TreeTableView}.
     *
     * @param skin The skin used by the UI control.
     */
    public AdvancedTableHeaderRow(TableViewSkinBase skin) {
        super(skin);
    }

    /**
     * Creates a new NestedTableColumnHeader instance. By default this method should not be overridden, but in some
     * circumstances it makes sense (e.g., testing, or when extreme customization is desired).
     *
     * @return A new NestedTableColumnHeader instance.
     */
    @Override
    protected AdvancedNestedTableColumnHeader createRootHeader() {
        return new AdvancedNestedTableColumnHeader(null);
    }
}
