package com.dlsc.gemsfx.richtextarea.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "tableRow", propOrder = {
        "tableCells"
})
public class TableRow {

    private final ObservableList<TableCell> tableCells = FXCollections.observableArrayList();

    @XmlElement(name = "td", required = true, type = TableCell.class)
    public final ObservableList<TableCell> getTableCells() {
        return tableCells;
    }
}
