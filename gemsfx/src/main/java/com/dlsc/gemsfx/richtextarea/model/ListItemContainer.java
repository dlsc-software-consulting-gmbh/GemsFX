package com.dlsc.gemsfx.richtextarea.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "listItemContainer", propOrder = {
        "listItems"
})
@XmlSeeAlso({
        UnorderedList.class,
        OrderedList.class
})
public abstract class ListItemContainer {

    private final ObservableList<ListItem> listItems = FXCollections.observableArrayList();

    @XmlElement(name = "li", required = true, type = ListItem.class)
    public final ObservableList<ListItem> getListItems() {
        return listItems;
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
}
