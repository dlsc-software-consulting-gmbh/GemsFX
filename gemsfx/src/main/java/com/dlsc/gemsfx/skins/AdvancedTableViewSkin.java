package com.dlsc.gemsfx.skins;

import javafx.scene.control.Control;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;

public class AdvancedTableViewSkin<T> extends TableViewSkin<T> {

    private AdvancedTableHeaderRow tableHeaderRow;

    /**
     * Creates a new TableViewSkin instance, installing the necessary child
     * nodes into the Control list, as well as the necessary input mappings for
     * handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public AdvancedTableViewSkin(TableView<T> control) {
        super(control);
    }

    /**
     * Creates a new TableHeaderRow instance. By default this method should not be overridden, but in some
     * circumstances it makes sense (e.g. testing, or when extreme customization is desired).
     *
     * @return A new TableHeaderRow instance.
     */
    @Override
    protected AdvancedTableHeaderRow createTableHeaderRow() {
        tableHeaderRow = new AdvancedTableHeaderRow(this);
        return tableHeaderRow;
    }

    @Override
    public AdvancedTableHeaderRow getTableHeaderRow() {
        return tableHeaderRow;
    }
}
