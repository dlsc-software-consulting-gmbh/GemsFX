package com.dlsc.gemsfx.richtextarea;

import com.dlsc.gemsfx.richtextarea.model.BlockElementsContainer;
import com.dlsc.gemsfx.richtextarea.model.Document;
import com.dlsc.gemsfx.richtextarea.model.Heading;
import com.dlsc.gemsfx.richtextarea.model.Image;
import com.dlsc.gemsfx.richtextarea.model.Link;
import com.dlsc.gemsfx.richtextarea.model.ListItem;
import com.dlsc.gemsfx.richtextarea.model.ListItemContainer;
import com.dlsc.gemsfx.richtextarea.model.OrderedList;
import com.dlsc.gemsfx.richtextarea.model.Paragraph;
import com.dlsc.gemsfx.richtextarea.model.Table;
import com.dlsc.gemsfx.richtextarea.model.TableBody;
import com.dlsc.gemsfx.richtextarea.model.TableCell;
import com.dlsc.gemsfx.richtextarea.model.TableHead;
import com.dlsc.gemsfx.richtextarea.model.TableRow;
import com.dlsc.gemsfx.richtextarea.model.Text;
import com.dlsc.gemsfx.richtextarea.model.UnorderedList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RichTextArea extends VBox {

    private final Label placeholder = new Label();

    private Section rootSection;

    public RichTextArea() {
        getStyleClass().add("rich-text-area");

        documentProperty().addListener(it -> updateText());

        placeholder.textProperty().bind(placeholderText);

        setFillWidth(true);

        placeholder.getStyleClass().add("placeholder");
        placeholder.setWrapText(true);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setTextAlignment(TextAlignment.CENTER);
        placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(placeholder, Priority.ALWAYS);
        getChildren().add(placeholder);

        getStylesheets().add(RichTextArea.class.getResource("richtextarea.css").toExternalForm());
    }

    @Override
    public String getUserAgentStylesheet() {
        return RichTextArea.class.getResource("richtextarea.css").toExternalForm();
    }

    private final StringProperty placeholderText = new SimpleStringProperty(this, "placeholderText", "No content");

    public final StringProperty placeholderTextProperty() {
        return placeholderText;
    }

    public void setPlaceholderText(String text) {
        placeholderText.set(text);
    }

    public final String getPlaceholderText() {
        return placeholderText.get();
    }

    @Override
    protected double computePrefWidth(double height) {
        return 100;
    }

    /**
     * Defines horizontal text alignment.
     *
     * @defaultValue TextAlignment.LEFT
     */
    private final ObjectProperty<TextAlignment> textAlignment = new SimpleObjectProperty<>(this, "textAlignment");

    public final void setTextAlignment(TextAlignment value) {
        textAlignment.set(value);
    }

    public final TextAlignment getTextAlignment() {
        return textAlignment.get();
    }

    public final ObjectProperty<TextAlignment> textAlignmentProperty() {
        return textAlignment;
    }

    /**
     * Defines the vertical space in pixel between lines.
     *
     * @defaultValue 0
     */
    private final DoubleProperty lineSpacing = new SimpleDoubleProperty(this, "lineSpacing", 0);

    public final void setLineSpacing(double spacing) {
        lineSpacing.set(spacing);
    }

    public final double getLineSpacing() {
        return lineSpacing.get();
    }

    public final DoubleProperty lineSpacingProperty() {
        return lineSpacing;
    }

    private final ObjectProperty<Document> document = new SimpleObjectProperty<>(this, "document");

    public final ObjectProperty<Document> documentProperty() {
        return document;
    }

    public final Document getDocument() {
        return document.get();
    }

    public final void setDocument(Document documentFX) {
        document.set(documentFX);
    }

    // show / hide comments

    private final BooleanProperty showComments = new SimpleBooleanProperty(this, "showComments", false);

    public final BooleanProperty showCommentsProperty() {
        return showComments;
    }

    public final boolean isShowComments() {
        return showComments.get();
    }

    public final void setShowComments(boolean show) {
        showComments.set(show);
    }

    private void updateText() {
        getChildren().clear();

        Document document = getDocument();
        final String s = Document.toString(document);

        if (document == null || document.isEmpty()) {
            getChildren().add(placeholder);
        } else {
            rootSection = new Section();
            getChildren().add(rootSection);

            processBlockElements(document, rootSection);
        }
    }

    private void processBlockElements(BlockElementsContainer container, Section section) {
        if (container == null) {
            return;
        }

        for (Object element : container.getBlockElements()) {

            if (element != null && section != null) {
                if (element instanceof OrderedList) {
                    createList((OrderedList) element, section);
                } else if (element instanceof UnorderedList) {
                    createList((UnorderedList) element, section);
                } else if (element instanceof Heading) {
                    createHeading((Heading) element, section);
                } else if (element instanceof Paragraph) {
                    createParagraph((Paragraph) element, section);
                } else if (element instanceof Table) {
                    createTable((Table) element, section);
                } else if (element instanceof Image) {
                    createImage((Image) element, section);
                }
            }
        }
    }

    private void processParagraph(Paragraph paragraph, Section section) {
        for (final Text text : paragraph.getTexts()) {
            if (text instanceof Link) {
                createLink((Link) text, section);
            } else {
                createText(text, section);
            }
        }
    }

    private void createImage(Image image, Section section) {
        ImageView imageView = new ImageView(getImageProvider().call(image.getId()));
        section.getChildren().add(imageView);
    }

    private final ObjectProperty<Callback<String, javafx.scene.image.Image>> imageProvider = new SimpleObjectProperty<>(this, "imageProvider");

    public Callback<String, javafx.scene.image.Image> getImageProvider() {
        return imageProvider.get();
    }

    public ObjectProperty<Callback<String, javafx.scene.image.Image>> imageProviderProperty() {
        return imageProvider;
    }

    public void setImageProvider(Callback<String, javafx.scene.image.Image> imageProvider) {
        this.imageProvider.set(imageProvider);
    }

    private void createTable(Table table, Section section) {
        GridPane gridPane = new GridPane();
        section.addChild(gridPane);

        int row = 0;

        TableHead tableHead = table.getTableHead();
        if (tableHead != null) {
            row = createTableHead(tableHead, gridPane);
        }

        TableBody tableBody = table.getTableBody();
        if (tableBody != null) {
            createTableBody(tableBody, gridPane, row);
        }
    }

    private int createTableHead(TableHead tableHead, GridPane gridPane) {

        int row = 0;
        for (TableRow tableRow : tableHead.getTableRows()) {

            Section section = null;
            int column = 0;

            for (TableCell tableCell : tableRow.getTableCells()) {

                section = new Section();
                section.getStyleClass().add("table-column-header");

                GridPane.setHgrow(section, Priority.ALWAYS);
                gridPane.add(section, column++, row);

                processBlockElements(tableCell, section);
            }

            if (section != null) {
                section.getStyleClass().add("last");
            }

            row++;
        }


        return row;
    }

    private void createTableBody(TableBody tableBody, GridPane gridPane, int row) {

        for (TableRow tableRow : tableBody.getTableRows()) {

            Section section = null;
            int column = 0;

            for (TableCell tableCell : tableRow.getTableCells()) {

                section = new Section();
                section.getStyleClass().add("table-cell");
                GridPane.setHgrow(section, Priority.ALWAYS);

                gridPane.add(section, column++, row);

                processBlockElements(tableCell, section);
            }

            if (section != null) {
                section.getStyleClass().add("last");
            }

            row++;
        }
    }

    private void createHeading(Heading heading, Section section) {
        Label label = new Label(heading.getValue());
        label.getStyleClass().addAll("text", "heading");
        section.addChild(label);
    }

    private Consumer<String> hyperlinkConsumer;

    public void setHyperlinkConsumer(Consumer<String> hyperlinkConsumer) {
        this.hyperlinkConsumer = hyperlinkConsumer;
    }

    private void createLink(Link link, Section section) {
        String target = link.getHref();
        javafx.scene.text.Text uiText = createText(link, section);

        uiText.setOnMouseClicked(evt -> {
            if (hyperlinkConsumer != null) {
                hyperlinkConsumer.accept(target);
            }
        });

        uiText.getStyleClass().add("link");
        uiText.setUnderline(true);
    }

    private void createParagraph(Paragraph paragraph, Section section) {
        Section newSection = new Section();

        if (paragraph.getTexts().isEmpty()) {
            newSection.getStyleClass().add("gap");
        } else {
            processParagraph(paragraph, newSection);
        }

        // add early, so that the lookup of the top comment wrapper pane works properly
        section.addChild(newSection);

        String styleClass = paragraph.getClazz();
        if (styleClass != null && !styleClass.trim().equals("")) {
            newSection.getStyleClass().add(styleClass);
        }
    }

    private javafx.scene.text.Text createText(Text text, Section section) {
        javafx.scene.text.Text uiText = new javafx.scene.text.Text(text.getValue());
        if (text.isItalic()) {
            uiText.getStyleClass().add("italic");
        }

        if (text.isBold()) {
            uiText.getStyleClass().add("bold");
        }

        if (text.isSubscript()) {
            uiText.getStyleClass().add("subscript");
        }

        if (text.isSuperscript()) {
            uiText.getStyleClass().add("superscript");
        }

        String textColor = text.getForegroundColor();
        if (isNotBlank(textColor)) {
            uiText.setFill(Color.web(textColor));
        } else {
            uiText.getStyleClass().add("text");
        }

        section.addText(uiText);

        return uiText;
    }

    private void createList(OrderedList list, Section section) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().addAll("list-block", "ordered-list");
        section.addChild(gridPane);

        int startNumber = list.getStart();
        int row = 0;

        for (ListItem item : list.getListItems()) {
            createOrderedListItem(startNumber, item, gridPane, row++);
            startNumber++;
        }
    }

    private void createOrderedListItem(int number, ListItem item, GridPane gridPane, int row) {
        javafx.scene.text.Text bullet = new javafx.scene.text.Text(Integer.toString(number) + ".");
        bullet.getStyleClass().add("text");
        Section itemSection = new Section();
        gridPane.add(bullet, 0, row);
        gridPane.add(itemSection, 1, row);
        GridPane.setHgrow(itemSection, Priority.ALWAYS);
        GridPane.setValignment(itemSection, VPos.TOP);
        GridPane.setHalignment(bullet, HPos.RIGHT);
        GridPane.setValignment(bullet, VPos.TOP);
        processBlockElements(item, itemSection);
    }

    private void createList(UnorderedList list, Section section) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().addAll("list-block", "bullet-list");

        section.addChild(gridPane);

        int row = 0;

        for (ListItem item : list.getListItems()) {
            createBulletListItem(item, gridPane, row++);
        }
    }

    private void createBulletListItem(ListItem item, GridPane gridPane, int row) {
        javafx.scene.text.Text bullet = new javafx.scene.text.Text("\u2022");
        bullet.getStyleClass().add("text");

        Section itemSection = new Section();

        /*
         * If we are starting a sublist then we do not need a bullet.
         */
        if (item.getBlockElements().isEmpty() || item.getBlockElements().get(0) instanceof ListItemContainer) {
            bullet.setVisible(false);
            itemSection.getStyleClass().add("nested-list");
        }

        gridPane.add(bullet, 0, row);
        gridPane.add(itemSection, 1, row);
        GridPane.setHgrow(itemSection, Priority.ALWAYS);
        GridPane.setValignment(itemSection, VPos.TOP);
        GridPane.setHalignment(bullet, HPos.RIGHT);
        GridPane.setValignment(bullet, VPos.TOP);

        processBlockElements(item, itemSection);
    }

    private class Section extends VBox {

        private TextFlow textFlow;

        public Section() {
            getStyleClass().add("section");
            setMinWidth(0);
            setMaxWidth(Double.MAX_VALUE);
        }

        public void addText(javafx.scene.text.Text text) {
            if (textFlow == null) {
                textFlow = new TextFlow();
                textFlow.textAlignmentProperty().bind(textAlignmentProperty());
                getChildren().add(textFlow);
            }

            textFlow.getChildren().add(text);
        }

        public void addChild(Node child) {
            getChildren().add(child);
            textFlow = null;
        }

        @Override
        protected double computePrefHeight(double width) {
            double ph = getInsets().getTop() + getInsets().getBottom();
            for (Node node : getChildren()) {
                if (node instanceof ImageView) {
                    final ImageView imageView = (ImageView) node;
                    javafx.scene.image.Image image = imageView.getImage();
                    double w = Math.min(image.getWidth(), getWidth());
                    ph += image.getHeight() * (w / image.getWidth());
                } else {
                    ph += node.prefHeight(width);
                }
            }

            ph += (getChildren().size() - 1) * getSpacing();

            return ph;
        }

        @Override
        protected void layoutChildren() {
            final double width = getWidth();
            final Insets insets = getInsets();
            final double contentWidth = width - insets.getLeft() - insets.getRight();

            double y = insets.getTop();
            for (Node node : getChildren()) {
                double w;
                double h;
                if (node instanceof ImageView) {
                    final ImageView imageView = (ImageView) node;
                    javafx.scene.image.Image image = imageView.getImage();
                    w = Math.min(image.getWidth(), contentWidth);
                    h = image.getHeight() * (w / image.getWidth());
                    imageView.setFitWidth(w);
                    imageView.setFitHeight(h);
                } else {
                    h = node.prefHeight(width);
                }

                node.resizeRelocate(insets.getLeft(), y, contentWidth, h);
                y += h;
                y += getSpacing();
            }
        }
    }
}
