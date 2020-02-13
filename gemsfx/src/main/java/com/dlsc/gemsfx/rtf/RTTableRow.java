package com.dlsc.gemsfx.rtf;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RTTableRow extends RTElement<RTTableRow> {

    private final ObservableList<RTTableCell> cells = FXCollections.observableArrayList();

    private RTTableRow() {
    }

    public static RTTableRow create() {
        return new RTTableRow();
    }

    public RTTableRow withCells(RTTableCell... cells) {
        this.cells.setAll(cells);
        return this;
    }

    public ObservableList<RTTableCell> getCells() {
        return cells;
    }
}
