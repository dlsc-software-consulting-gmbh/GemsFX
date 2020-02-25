package com.dlsc.gemsfx.richtextarea;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class RTTableContainer<SELF extends RTTableContainer> extends RTElement<SELF> {

    private final ObservableList<RTTableRow> rows = FXCollections.observableArrayList();


    public final SELF withRows(RTTableRow... row) {
        rows.setAll(row);
        return (SELF) this;
    }

    public final ObservableList<RTTableRow> getRows() {
        return rows;
    }
}
