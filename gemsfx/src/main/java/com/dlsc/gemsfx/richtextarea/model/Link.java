package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "link")
public class Link extends Text {

    public Link() {
    }

    public Link(String href) {
        this(href, href);
    }

    public Link(String text, String href) {
        setValue(text);
        setHref(href);
    }

    private final StringProperty href = new SimpleStringProperty(this, "href");

    public final StringProperty hrefProperty() {
        return href;
    }

    public final void setHref(String href) {
        this.href.set(href);
    }

    @XmlAttribute(name = "href")
    public final String getHref() {
        return href.get();
    }
}
