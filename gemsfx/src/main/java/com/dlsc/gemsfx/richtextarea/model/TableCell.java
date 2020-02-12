package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "tableCell")
public class TableCell extends BlockElementsContainer {

    private final IntegerProperty rowSpan = new SimpleIntegerProperty(this, "rowSpan", 1);

    public final IntegerProperty rowSpanProperty() {
        return rowSpan;
    }

    public final int getRowSpan() {
        return rowSpan.get();
    }

    public final void setRowSpan(Integer rowSpan) {
        this.rowSpan.set(rowSpan != null? rowSpan : 1);
    }


    @XmlAttribute(name="rowspan")
    public final Integer getRowSpanAttribute() {
        final int result = rowSpan.get();
        return result == 1 ? null : result;
    }

    public final void setRowSpanAttribute(Integer rowSpan) {
        setRowSpan(rowSpan);
    }

    private final IntegerProperty colSpan = new SimpleIntegerProperty(this, "colSpan", 1);

    public final IntegerProperty colSpanProperty() {
        return colSpan;
    }

    public final int getColSpan() {
        return colSpan.get();
    }

    public final void setColSpan(Integer colSpan) {
        this.colSpan.set(colSpan != null? colSpan : 1);
    }

    @XmlAttribute(name="colspan")
    public final Integer getColSpanAttribute() {
        final int result = colSpan.get();
        return result == 1 ? null : result;
    }

    public final void setColSpanAttribute(Integer start) {
        setColSpan(start);
    }

}
