package com.dlsc.gemsfx.richtextarea.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "tableRowContainer", propOrder = {
        "tableRows"
})
@XmlSeeAlso({
        TableHead.class,
        TableBody.class
})
public class TableRowContainer {

    private final ObservableList<TableRow> tableRows = FXCollections.observableArrayList();

    @XmlElement(name = "tr", required = true, type = TableRow.class)
    public ObservableList<TableRow> getTableRows() {
        return this.tableRows;
    }

    public int getColumnCount() {
        int max = 0;
        final List<Integer> cellsFromPrevRows = new ArrayList<>();
        final ObservableList<TableRow> rows = getTableRows();
        final int numRows = rows.size();

        for (int r = 0; r < numRows; r++) {
            final TableRow row = rows.get(r);

            if (r >= cellsFromPrevRows.size()) {
                cellsFromPrevRows.add(0);
            }
            int count = cellsFromPrevRows.get(r);

            for (TableCell tableCell : row.getTableCells()) {
                final int colSpan = tableCell.getColSpan();
                count += colSpan;

                final int rowSpan = tableCell.getRowSpan() + r;
                for (int i = r + 1; i < rowSpan; i++) {
                    if (i >= cellsFromPrevRows.size()) {
                        cellsFromPrevRows.add(0);
                    }
                    cellsFromPrevRows.set(i, cellsFromPrevRows.get(i) + colSpan);
                }
            }

            max = Math.max(max, count);
        }

        return max;
    }
}
