package com.dlsc.gemsfx.richtextarea.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.StringReader;
import java.io.StringWriter;

@XmlType(name = "document")
public class Document extends BlockElementsContainer {

    private static final ObjectFactory FACTORY = new ObjectFactory();

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    static {
        try {
            JAXBContext ctx = JAXBContext.newInstance("com.dlsc.gemsfx.richtextarea.model");

            unmarshaller = ctx.createUnmarshaller();

            marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }


    @XmlAttribute(name = "nextId")
    private Integer nextId;

    public int getNextId() {
        if (nextId == null) {
            return 1;
        } else {
            return nextId;
        }
    }

    public void setNextId(Integer value) {
        if (value == null || value == 1) {
            this.nextId = null;
        } else {
            this.nextId = value;
        }
    }

    @SuppressWarnings("unchecked")
    public static Document fromString(String text) {
        if (text != null) {
            try {
                return ((JAXBElement<Document>) unmarshaller.unmarshal(new StringReader(text))).getValue();
            } catch (JAXBException e) {
               e.printStackTrace();
            }
        }

        return new Document();
    }

    public static String toString(Document document) {
        return toString(document, false);
    }

    public static String toString(Document document, boolean formattedOutput) {
        try {
            StringWriter writer = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
            marshaller.marshal(FACTORY.createRoot(document), writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    public final boolean isEmpty() {
        return getBlockElements().isEmpty();
    }
}
