package com.dlsc.gemsfx.richtextarea.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paragraph")
public class Paragraph {

    @XmlTransient
    private final ObservableList<Text> texts = FXCollections.observableArrayList();

    @XmlElements({
            @XmlElement(name = "t", type = Text.class),
            @XmlElement(name = "a", type = Link.class)
    })
    public final ObservableList<Text> getTexts() {
        return this.texts;
    }

    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute(name = "b")
    protected Boolean bold;
    @XmlAttribute(name = "i")
    protected Boolean italic;
    @XmlAttribute(name = "sub")
    protected Boolean subscript;
    @XmlAttribute(name = "sup")
    protected Boolean superscript;
    @XmlAttribute(name = "bg")
    protected String backgroundColor;
    @XmlAttribute(name = "fg")
    protected String foregroundColor;


    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public boolean isBold() {
        if (bold == null) {
            return false;
        } else {
            return bold;
        }
    }

    public void setBold(Boolean value) {
        this.bold = value;
    }

    public boolean isItalic() {
        if (italic == null) {
            return false;
        } else {
            return italic;
        }
    }

    public void setItalic(Boolean value) {
        this.italic = value;
    }

    public boolean isSubscript() {
        if (subscript == null) {
            return false;
        } else {
            return subscript;
        }
    }

    public void setSubscript(Boolean value) {
        this.subscript = value;
    }

    public boolean isSuperscript() {
        if (superscript == null) {
            return false;
        } else {
            return superscript;
        }
    }

    public void setSuperscript(Boolean value) {
        this.superscript = value;
    }

    public String getBackgroundColor() {
        if (backgroundColor == null) {
            return "";
        } else {
            return backgroundColor;
        }
    }

    public void setBackgroundColor(String value) {
        this.backgroundColor = value;
    }

    public String getForegroundColor() {
        if (foregroundColor == null) {
            return "";
        } else {
            return foregroundColor;
        }
    }

    public void setForegroundColor(String value) {
        this.foregroundColor = value;
    }


    @XmlAttribute(name = "id")
    protected Integer id;

    public final Integer getId() {
        return id;
    }

    public final void setId(Integer value) {
        this.id = value;
    }
}
