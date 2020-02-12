package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "table", propOrder = {
        "tableHead",
        "tableBody"
})
public class Table {

    private final ObjectProperty<TableHead> tableHead = new SimpleObjectProperty<>(this, "tableHead");

    public ObjectProperty<TableHead> tableHeadProperty() {
        return tableHead;
    }

    @XmlElement(name = "thead", required = true, type = TableHead.class)
    public TableHead getTableHead() {
        return tableHead.get();
    }

    public void setTableHead(TableHead head) {
        this.tableHead.set(head);
    }

    private final ObjectProperty<TableBody> tableBody = new SimpleObjectProperty<>(this, "tableBody");

    public ObjectProperty<TableBody> tableBodyProperty() {
        return tableBody;
    }

    @XmlElement(name = "tbody", required = true, type = TableBody.class)
    public TableBody getTableBody() {
        return tableBody.get();
    }

    /* required to make getTableBody/setTableBody bean property methods. */
    public void setTableBody(TableBody body) {
        this.tableBody.set(body);
    }


    @XmlAttribute(name = "id")
    protected Integer id;

    @XmlTransient
    public final Integer getId() {
        return id;
    }

    public final void setId(Integer value) {
        this.id = value;
    }

    public final int getColumnCount() {
        return Math.max(getTableHead().getColumnCount(), getTableBody().getColumnCount());
    }
}
