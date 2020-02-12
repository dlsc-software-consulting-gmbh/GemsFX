package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "image")
public class Image {

    private final LongProperty imageId = new SimpleLongProperty(this, "imageId");

    public final LongProperty imageIdProperty() {
        return imageId;
    }

    public final void setImageId(Long imageId) {
        this.imageId.set(imageId);
    }

    @XmlAttribute
    public final Long getImageId() {
        return imageId.get();
    }

    @XmlAttribute(name = "id")
    protected String id;

    @XmlTransient
    public final String getId() {
        return id;
    }

    public final void setId(String value) {
        this.id = value;
    }
}
