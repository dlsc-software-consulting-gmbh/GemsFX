package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "orderedList")
public class OrderedList extends ListItemContainer {

    private final IntegerProperty start = new SimpleIntegerProperty(this, "start", 1);

    public OrderedList() {
    }

    public OrderedList(int startAt) {
        setStart(startAt);
    }

    public final IntegerProperty startProperty() {
        return start;
    }

    public final int getStart() {
        return start.get();
    }

    public final void setStart(Integer start) {
        this.start.set(start != null? start : 1);
    }

    @XmlAttribute(name = "start")
    public final Integer getStartAttribute() {
        final int result = start.get();
        return result == 1 ? null : result;
    }

    public final void setStartAttribute(Integer start) {
        setStart(start);
    }
}
