package com.dlsc.gemsfx.richtextarea.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType(name = "text", propOrder = {
        "value"
})
@XmlSeeAlso({
        Link.class
})
public class Text {

    public Text() {
    }

    public Text(String text) {
        value.set(text);
    }


    @XmlTransient
    private final StringProperty value = new SimpleStringProperty(this, "value", "");

    public final StringProperty valueProperty() {
        return value;
    }

    @XmlValue
    public final String getValue() {
        return value.get();
    }

    public final void setValue(String value) {
        this.value.set(value);
    }

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


    public boolean isBold() {
        if (bold == null) {
            return false;
        } else {
            return bold;
        }
    }

    public void setBold(Boolean value) {
        this.bold = Boolean.FALSE.equals(value) ? null : value;
    }

    public boolean isItalic() {
        if (italic == null) {
            return false;
        } else {
            return italic;
        }
    }

    public void setItalic(Boolean value) {
        this.italic = Boolean.FALSE.equals(value) ? null : value;
    }

    public boolean isSubscript() {
        if (subscript == null) {
            return false;
        } else {
            return subscript;
        }
    }

    public void setSubscript(Boolean value) {
        this.subscript = Boolean.FALSE.equals(value) ? null : value;
    }

    public boolean isSuperscript() {
        if (superscript == null) {
            return false;
        } else {
            return superscript;
        }
    }

    public void setSuperscript(Boolean value) {
        this.superscript = Boolean.FALSE.equals(value) ? null : value;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String value) {
        this.backgroundColor = value;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(String value) {
        this.foregroundColor = "#000000".equals(value) || "#333333".equals(value) ? null : value;
    }
}
