package com.dlsc.gemsfx.richtextarea.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "blockElementsContainer", propOrder = {
        "blockElements"
})
@XmlSeeAlso({
        Document.class,
        ListItem.class,
        TableCell.class
})
public class BlockElementsContainer {

    private final ObservableList<Object> blockElements = FXCollections.observableArrayList();

    @XmlElements({
            @XmlElement(name = "h", type = Heading.class),
            @XmlElement(name = "p", type = Paragraph.class),
            @XmlElement(name = "img", type = Image.class),
            @XmlElement(name = "ul", type = UnorderedList.class),
            @XmlElement(name = "ol", type = OrderedList.class),
            @XmlElement(name = "table", type = Table.class)
    })
    public final ObservableList<Object> getBlockElements() {
        return blockElements;
    }

}
