package com.dlsc.gemsfx.rtf;

import com.dlsc.gemsfx.rtf.RTList.Type;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Consumer;

public class RichTextArea2Skin extends SkinBase<RichTextArea2> {

    private final Label placeholder = new Label();

    private Section rootSection;

    private VBox container = new VBox();

    public RichTextArea2Skin(RichTextArea2 control) {
        super(control);

        control.documentProperty().addListener(it -> updateText());

        placeholder.textProperty().bind(placeholderText);

        container.setFillWidth(true);

        placeholder.getStyleClass().add("placeholder");
        placeholder.setWrapText(true);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setTextAlignment(TextAlignment.CENTER);
        placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(placeholder, Priority.ALWAYS);
        container.getChildren().add(placeholder);
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

    private void updateText() {
        final RTDocument document = getSkinnable().getDocument();
        getChildren().clear();

        if (document == null || document.getElements().isEmpty()) {
            getChildren().add(placeholder);
        } else {
            rootSection = new Section();
            getChildren().add(rootSection);

            processBlockElements(document, rootSection);
        }
    }

    private void processBlockElements(RTElementContainer<?> container, Section section) {
        if (container == null) {
            return;
        }

        for (RTElement element : container.getElements()) {

            if (element != null && section != null) {
                if (element instanceof RTList) {
                    createList((RTList) element, section);
                } else if (element instanceof RTHeading) {
                    createHeading((RTHeading) element, section);
                } else if (element instanceof RTParagraph) {
                    createParagraph((RTParagraph) element, section);
                } else if (element instanceof RTTable) {
                    createTable((RTTable) element, section);
                } else if (element instanceof RTImage) {
                    createImage((RTImage) element, section);
                } else if (element instanceof RTText) {
                    createText((RTText) element, section);
                } else if (element instanceof RTLink) {
                    createLink((RTLink) element, section);
                }
            }
        }
    }

    private void createImage(RTImage image, Section section) {
        ImageView imageView = new ImageView(image.getImage());
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

    private void createTable(RTTable table, Section section) {
        GridPane gridPane = new GridPane();
        section.addChild(gridPane);

        int row = 0;

        RTTableHead tableHead = table.getHead();
        if (tableHead != null) {
            row = createTableHead(tableHead, gridPane);
        }

        RTTableBody tableBody = table.getBody();
        if (tableBody != null) {
            createTableBody(tableBody, gridPane, row);
        }
    }

    private int createTableHead(RTTableHead tableHead, GridPane gridPane) {

        int row = 0;
        for (RTTableRow tableRow : tableHead.getRows()) {

            Section section = null;
            int column = 0;

            for (RTTableCell tableCell : tableRow.getCells()) {

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

    private void createTableBody(RTTableBody tableBody, GridPane gridPane, int row) {

        for (RTTableRow tableRow : tableBody.getRows()) {

            Section section = null;
            int column = 0;

            for (RTTableCell tableCell : tableRow.getCells()) {

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

    private void createHeading(RTHeading heading, Section section) {
        Label label = new Label(heading.getText());
        label.getStyleClass().addAll("text", "heading");
        section.addChild(label);
    }

    private Consumer<String> hyperlinkConsumer;

    public void setHyperlinkConsumer(Consumer<String> hyperlinkConsumer) {
        this.hyperlinkConsumer = hyperlinkConsumer;
    }

    private void createLink(RTLink link, Section section) {
        String target = link.getUrl().toExternalForm();
        javafx.scene.text.Text uiText = createText(link, section);

        uiText.setOnMouseClicked(evt -> {
            if (hyperlinkConsumer != null) {
                hyperlinkConsumer.accept(target);
            }
        });

        uiText.getStyleClass().add("link");
        uiText.setUnderline(true);
    }

    private void createParagraph(RTParagraph paragraph, Section section) {
        Section newSection = new Section();

        if (paragraph.getElements().isEmpty()) {
            newSection.getStyleClass().add("gap");
        } else {
            processBlockElements(paragraph, newSection);
        }

        // add early, so that the lookup of the top comment wrapper pane works properly
        section.addChild(newSection);

        List<String> styleClass = paragraph.getStyleClass();
        if (styleClass != null && !styleClass.isEmpty()) {
            newSection.getStyleClass().addAll(styleClass);
        }
    }

    private javafx.scene.text.Text createText(RTTextElement text, Section section) {
        javafx.scene.text.Text uiText = new javafx.scene.text.Text(text.getText());
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

        uiText.getStyleClass().add("text");

        Color textColor = text.getTextFill();
        if (textColor != null) {
            uiText.setFill(textColor);
        }

        section.addText(uiText);

        return uiText;
    }

    private void createList(RTList list, Section section) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("list-block");

        if (list.getType().equals(Type.ORDERED)) {
            gridPane.getStyleClass().add("ordered-list");
        } else {
            gridPane.getStyleClass().add("bullet-list");
        }

        section.addChild(gridPane);

        int startNumber = list.getStart();
        int row = 0;

        for (RTListItem item : list.getItems()) {
            if (list.getType().equals(Type.ORDERED)) {
                createOrderedListItem(startNumber, item, gridPane, row++);
                startNumber++;
            } else {
                createBulletListItem(item, gridPane, row++);
            }
        }
    }

    private void createOrderedListItem(int number, RTListItem item, GridPane gridPane, int row) {
        javafx.scene.text.Text bullet = new javafx.scene.text.Text(number + ".");
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

    private void createBulletListItem(RTListItem item, GridPane gridPane, int row) {
        javafx.scene.text.Text bullet = new javafx.scene.text.Text("\u2022");
        bullet.getStyleClass().add("text");

        Section itemSection = new Section();

        /*
         * If we are starting a sublist then we do not need a bullet.
         */
        if (item.getElements().isEmpty() || item.getElements().get(0) instanceof RTList) {
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
