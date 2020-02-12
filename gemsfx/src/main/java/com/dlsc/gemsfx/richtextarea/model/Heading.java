package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "heading", propOrder = {
        "value"
})
public class Heading {

    public Heading() {
    }

    public Heading(String text) {
        setValue(text);
    }


    private final StringProperty value = new SimpleStringProperty(this, "value", "");

    public final StringProperty valueProperty() {
        return value;
    }

    @XmlValue
    public final String getValue() {
        return value.get();
    }

    public final void setValue(String value) {
        this.value.setValue(value);
    }


    @XmlAttribute(name = "id", required = true)
    protected Integer id;

    @XmlTransient
    public final Integer getId() {
        return id;
    }

    public final void setId(Integer value) {
        this.id = value;
    }
}
