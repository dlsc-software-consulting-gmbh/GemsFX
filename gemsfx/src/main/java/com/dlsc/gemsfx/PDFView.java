package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PDFViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * A simple PDF viewer based on Apache PDFBox. The view shows thumbnails
 * on the left and the full page on the right.
 */
public class PDFView extends Control {

    /**
     * Constructs a new view.
     */
    public PDFView() {
        super();

        getStyleClass().add("pdf-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PDFViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return PDFView.class.getResource("pdf-view.css").toExternalForm();
    }

    private final ObjectProperty<PDDocument> document = new SimpleObjectProperty<>(this, "document");

    public final PDDocument getDocument() {
        return document.get();
    }

    /**
     * Stores the PDF document model object.
     *
     * @return the document
     */
    public final ObjectProperty<PDDocument> documentProperty() {
        return document;
    }

    public final void setDocument(PDDocument document) {
        this.document.set(document);
    }

    public final void load(File file) throws IOException {
        Objects.requireNonNull(file, "file can not be null");
        setDocument(PDDocument.load(file));
    }
}