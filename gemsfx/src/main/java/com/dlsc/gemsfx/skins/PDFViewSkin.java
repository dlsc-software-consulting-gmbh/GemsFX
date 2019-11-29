package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PDFView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PDFViewSkin extends SkinBase<PDFView> {

    private static final float LOW_RES = .5f;

    private static final float HIGH_RES = 4;

    private final ObservableList<Integer> pdfFilePages = FXCollections.observableArrayList();

    private ListView<Integer> thumbnailListView = new ListView<>();

    private ListView<Integer> pageListView = new ListView<>();

    private PDFRenderer renderer;

    public PDFViewSkin(PDFView pdfView) {
        super(pdfView);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(thumbnailListView);
        borderPane.setCenter(pageListView);

        thumbnailListView.getStyleClass().add("thumbnail-list-view");
        thumbnailListView.setPlaceholder(null);
        thumbnailListView.getSelectionModel().selectedItemProperty().addListener(it -> pageListView.scrollTo(thumbnailListView.getSelectionModel().getSelectedItem()));

        thumbnailListView.setCellFactory(view -> new PdfPageListCell(LOW_RES, true));
        thumbnailListView.setItems(pdfFilePages);

        pageListView.setCellFactory(view -> new PdfPageListCell(HIGH_RES, false));
        pageListView.setItems(pdfFilePages);

        pdfView.documentProperty().addListener(it -> updateView());
        updateView();

        getChildren().add(borderPane);
    }

    private void updateView() {
        final PDDocument document = getSkinnable().getDocument();
        pdfFilePages.clear();
        if (document != null) {
            renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                pdfFilePages.add(i);
            }
        }
    }

    class PdfPageListCell extends ListCell<Integer> {

        private ImageView imageView = new ImageView();
        private Label pageNumberLabel = new Label();

        private float dpi;
        private boolean thumbnail;

        public PdfPageListCell(float dpi, boolean thumbnail) {
            this.dpi = dpi;
            this.thumbnail = thumbnail;

            pageNumberLabel.getStyleClass().add("page-number-label");

            StackPane stackPane = new StackPane(imageView);
            stackPane.getStyleClass().add("image-view-wrapper");
            stackPane.setMaxWidth(Region.USE_PREF_SIZE);

            VBox vBox = new VBox(5, stackPane, pageNumberLabel);
            vBox.setAlignment(Pos.CENTER);
            vBox.setFillWidth(true);

            setGraphic(vBox);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            vBox.visibleProperty().bind(emptyProperty().not());

            pageNumberLabel.setVisible(thumbnail);
            pageNumberLabel.setManaged(thumbnail);

            imageView.fitWidthProperty().bind(widthProperty().multiply(thumbnail ? .7 : .9));

            imageView.setPreserveRatio(true);
            setAlignment(Pos.CENTER);

            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setMinSize(0, 0);
        }

        @Override
        protected void updateItem(Integer page, boolean empty) {
            super.updateItem(page, empty);

            if (page != null && !empty) {
                pageNumberLabel.setText(Integer.toString(getIndex() + 1));

                try {
                    BufferedImage bufferedImage = renderer.renderImage(page, dpi, ImageType.RGB, RenderDestination.VIEW);
                    Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                    imageView.setImage(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
