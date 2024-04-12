package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.AdvancedTableColumnHeader;
import com.dlsc.gemsfx.skins.AdvancedTableViewSkin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;

/**
 * A custom table view with additional features for the standard table view, e.g. the
 * ability to optimize the column width based on their content.
 *
 * @see #autoResizeAllColumns
 * @param <T>
 */
public class AdvancedTableView<T> extends TableView<T> {

    private boolean autoResizeAllColumns;
    private int autoResizeRows;

    public AdvancedTableView() {
        init();
    }

    public AdvancedTableView(ObservableList<T> items) {
        super(items);
        init();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AdvancedTableViewSkin<>(this);
    }

    private void init() {
        skinProperty().addListener(it -> {
            if (getSkin() != null && autoResizeAllColumns) {
                autoResizeAllColumns(autoResizeRows);
            }
        });
    }

    /**
     * Resizes all columns to have a perfect width so that the cell content is fully
     * visible. This will look at the first 100 rows for its calculations.
     */
    public void autoResizeAllColumns() {
        autoResizeAllColumns(100);
    }

    /**
     * Resizes all columns to have a perfect width so that the cell content is fully
     * visible.
     *
     * @param rows the number of rows to look at for the resizing operation
     */
    public void autoResizeAllColumns(int rows) {
        if (rows <= 0) {
            return;
        }

        AdvancedTableViewSkin<?> skin = (AdvancedTableViewSkin<?>) getSkin();
        if (skin != null) {
            autoResizeAllColumns = false;
            Platform.runLater(() -> {
                NestedTableColumnHeader rootHeader = skin.getTableHeaderRow().getRootHeader();
                resize(rootHeader, autoResizeRows);
            });
        } else {
            autoResizeAllColumns = true;
            autoResizeRows = rows;
        }
    }

    private void resize(TableColumnHeader header, int rows) {
        if (header instanceof NestedTableColumnHeader nestedTableColumnHeader) {
            nestedTableColumnHeader.getColumnHeaders().forEach(col -> resize(col, rows));
        } else if (header instanceof AdvancedTableColumnHeader advancedTableColumnHeader) {
            advancedTableColumnHeader.resizeColumnToFitContent(rows);
        }
    }
}
