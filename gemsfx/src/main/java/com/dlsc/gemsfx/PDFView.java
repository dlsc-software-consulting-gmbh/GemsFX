package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PDFViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A PDF viewer based on Apache PDFBox. The view shows thumbnails
 * on the left and the full page on the right. The user can zoom in,
 * rotate, fit size, etc...
 */
public class PDFView extends Control {

    /**
     * Constructs a new view.
     */
    public PDFView() {
        super();

        getStyleClass().add("pdf-view");
        setFocusTraversable(false);

        zoomFactorProperty().addListener(it -> {
            if (getZoomFactor() < 1) {
                throw new IllegalArgumentException("zoom factor can not be smaller than 1");
            } else if (getZoomFactor() > getMaxZoomFactor()) {
                throw new IllegalArgumentException("zoom factor can not be larger than max zoom factor, but " + getZoomFactor() + " > " + getMaxZoomFactor());
            }
        });

        showAllProperty().addListener(it -> {
            if (isShowAll()) {
                setZoomFactor(1);
            }
        });

        selectedSearchResultProperty().addListener(it -> {
            final SearchResult result = getSelectedSearchResult();
            if (result != null) {
                setPage(result.getPageNumber());
            }
        });

        documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (oldDoc != null) {
                try {
                    oldDoc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setSearchText(null);
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PDFViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return PDFView.class.getResource("pdf-view.css").toExternalForm();
    }

    /**
     * A flag used to control whether the view will display a thumbnail version of the pages
     * on the left-hand side.
     */
    private final BooleanProperty showThumbnails = new SimpleBooleanProperty(this, "showThumbnails", true);

    public final boolean isShowThumbnails() {
        return showThumbnails.get();
    }

    public final BooleanProperty showThumbnailsProperty() {
        return showThumbnails;
    }

    public final void setShowThumbnails(boolean showThumbnails) {
        this.showThumbnails.set(showThumbnails);
    }


    /**
     * A flag used to control whether the view will include a toolbar with zoom, search, rotation
     * controls.
     */
    private final BooleanProperty showToolBar = new SimpleBooleanProperty(this, "showToolBar", true);

    public final boolean isShowToolBar() {
        return showToolBar.get();
    }

    public final BooleanProperty showToolBarProperty() {
        return showToolBar;
    }

    public final void setShowToolBar(boolean showToolBar) {
        this.showToolBar.set(showToolBar);
    }

    /**
     * A flag used to control whether the view will display aggregated search results
     * on the left-hand side.
     */
    private final BooleanProperty showSearchResults = new SimpleBooleanProperty(this, "showSearchResults", true);

    public final boolean isShowSearchResults() {
        return showSearchResults.get();
    }

    public final BooleanProperty showSearchResultsProperty() {
        return showSearchResults;
    }

    public final void setShowSearchResults(boolean showSearchResults) {
        this.showSearchResults.set(showSearchResults);
    }

    /**
     * Caching thumbnails can be useful for low powered systems with enough memory. The default value
     * is "false". When set to "true" each thumbnail image will be added to a hashmap cache, hence making it
     * necessary to only render once.
     */
    private final BooleanProperty cacheThumbnails = new SimpleBooleanProperty(this, "cacheThumbnails", false);

    public final boolean isCacheThumbnails() {
        return cacheThumbnails.get();
    }

    public final BooleanProperty cacheThumbnailsProperty() {
        return cacheThumbnails;
    }

    public final void setCacheThumbnails(boolean cacheThumbnails) {
        this.cacheThumbnails.set(cacheThumbnails);
    }

    /**
     * Sets the upper bounds for zoom operations. The default value is "4".
     */
    private final DoubleProperty maxZoomFactor = new SimpleDoubleProperty(this, "maxZoomFactor", 4);

    public final double getMaxZoomFactor() {
        return maxZoomFactor.get();
    }

    public final DoubleProperty maxZoomFactorProperty() {
        return maxZoomFactor;
    }

    public final void setMaxZoomFactor(double maxZoomFactor) {
        this.maxZoomFactor.set(maxZoomFactor);
    }

    /**
     * The current zoom factor. The default value is "1".
     */
    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(this, "zoomFactor", 1);

    public final double getZoomFactor() {
        return zoomFactor.get();
    }

    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public final void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }

    /**
     * The page rotation in degrees. Supported values are only "0", "90", "180", "270", "360", ...
     * multiples of "90".
     */
    private final DoubleProperty pageRotation = new SimpleDoubleProperty(this, "pageRotation", 0) {
        @Override
        public void set(double newValue) {
            super.set(newValue % 360d);
        }
    };

    public final double getPageRotation() {
        return pageRotation.get();
    }

    public final DoubleProperty pageRotationProperty() {
        return pageRotation;
    }

    public final void setPageRotation(double pageRotation) {
        this.pageRotation.set(pageRotation);
    }

    /**
     * Convenience method to rotate the generated image by -90 degrees.
     */
    public final void rotateLeft() {
        setPageRotation(getPageRotation() - 90);
    }

    /**
     * Convenience method to rotate the generated image by +90 degrees.
     */
    public final void rotateRight() {
        setPageRotation(getPageRotation() + 90);
    }

    /**
     * Stores the number of the currently showing page.
     */
    private final IntegerProperty page = new SimpleIntegerProperty(this, "page");

    public final int getPage() {
        return page.get();
    }

    public final IntegerProperty pageProperty() {
        return page;
    }

    public final void setPage(int page) {
        this.page.set(page);
    }

    /**
     * Convenience method to show the next page. This simply increases the {@link #pageProperty()} value
     * by 1.
     *
     * @return true if the operation actually did cause a page change
     */
    public final boolean gotoNextPage() {
        int currentPage = getPage();
        setPage(Math.min(getDocument().getNumberOfPages() - 1, getPage() + 1));
        return currentPage != getPage();
    }

    /**
     * Convenience method to show the previous page. This simply decreases the {@link #pageProperty()} value
     * by 1.
     *
     * @return true if the operation actually did cause a page change
     */
    public final boolean gotoPreviousPage() {
        int currentPage = getPage();
        setPage(Math.max(0, getPage() - 1));
        return currentPage != getPage();
    }

    /**
     * A flag that controls whether we always want to show the entire page. If "true" then the page
     * will be constantly resized to fit the viewport of the scroll pane in which it is showing. In
     * this mode zooming is not possible.
     */
    private final BooleanProperty showAll = new SimpleBooleanProperty(this, "showAll", false);

    public final boolean isShowAll() {
        return showAll.get();
    }

    public final BooleanProperty showAllProperty() {
        return showAll;
    }

    public final void setShowAll(boolean showAll) {
        this.showAll.set(showAll);
    }

    /**
     * The resolution / scale at which the thumbnails will be rendered. The default value is ".5".
     */
    private final FloatProperty thumbnailPageScale = new SimpleFloatProperty(this, "thumbnailScale", .5f);

    public final float getThumbnailPageScale() {
        return thumbnailPageScale.get();
    }

    public final FloatProperty thumbnailPageScaleProperty() {
        return thumbnailPageScale;
    }

    public final void setThumbnailPageScale(float thumbnailPageScale) {
        this.thumbnailPageScale.set(thumbnailPageScale);
    }

    /**
     * The resolution / scale at which the main page will be rendered. The default value is "4".
     * The value has direct impact on the size of the images being generated and the memory requirements.
     * Keep low on low powered / low resolution systems and high on large systems with hires displays.
     */
    private final FloatProperty pageScale = new SimpleFloatProperty(this, "pageScale", 4f);

    public final float getPageScale() {
        return pageScale.get();
    }

    public final FloatProperty pageScaleProperty() {
        return pageScale;
    }

    public final void setPageScale(float pageScale) {
        this.pageScale.set(pageScale);
    }

    /**
     * The size used for the images displayed in the thumbnail view. The default value is "200".
     */
    private final DoubleProperty thumbnailSize = new SimpleDoubleProperty(this, "thumbnailSize", 200d);

    public final double getThumbnailSize() {
        return thumbnailSize.get();
    }

    public final DoubleProperty thumbnailSizeProperty() {
        return thumbnailSize;
    }

    public final void setThumbnailSize(double thumbnailSize) {
        this.thumbnailSize.set(thumbnailSize);
    }

    /**
     * The currently loaded and displayed PDF document.
     */
    private final ObjectProperty<PDDocument> document = new SimpleObjectProperty<>(this, "document");

    public final ObjectProperty<PDDocument> documentProperty() {
        return document;
    }

    public final PDDocument getDocument() {
        return document.get();
    }

    public final void setDocument(PDDocument document) {
        this.document.set(document);
    }

    /**
     * A text used for searching inside the document. Results will be highlighted.
     */
    private final StringProperty searchText = new SimpleStringProperty(this, "searchText");

    public final String getSearchText() {
        return searchText.get();
    }

    public final StringProperty searchTextProperty() {
        return searchText;
    }

    public final void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    private final ListProperty<SearchResult> searchResults = new SimpleListProperty<>(this, "searchResults", FXCollections.observableArrayList());

    public final ObservableList<SearchResult> getSearchResults() {
        return searchResults.get();
    }

    public final ListProperty<SearchResult> searchResultsProperty() {
        return searchResults;
    }

    public final void setSearchResults(ObservableList<SearchResult> searchResults) {
        this.searchResults.set(searchResults);
    }

    private final ObjectProperty<SearchResult> selectedSearchResult = new SimpleObjectProperty<>(this, "selectedSearchResult");

    public final SearchResult getSelectedSearchResult() {
        return selectedSearchResult.get();
    }

    public final ObjectProperty<SearchResult> selectedSearchResultProperty() {
        return selectedSearchResult;
    }

    public final void setSelectedSearchResult(SearchResult selectedSearchResult) {
        this.selectedSearchResult.set(selectedSearchResult);
    }

    private final ObjectProperty<Color> searchResultColor = new SimpleObjectProperty<>(this, "searchResultColor", Color.RED);

    public final Color getSearchResultColor() {
        return searchResultColor.get();
    }

    public final ObjectProperty<Color> searchResultColorProperty() {
        return searchResultColor;
    }

    public final void setSearchResultColor(Color searchResultColor) {
        this.searchResultColor.set(searchResultColor);
    }

    public static class SearchResult {

        private final int pageNumber;
        private String text;
        private String searchText;
        private final List<TextPosition> textPositions;

        public SearchResult(int pageNumber, String text, String searchText, List<TextPosition> textPositions) {
            this.pageNumber = pageNumber;
            this.text = text;
            this.searchText = searchText;
            this.textPositions = textPositions;
        }

        public String getSearchText() {
            return searchText;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public String getText() {
            return text;
        }

        public List<TextPosition> getTextPositions() {
            return textPositions;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("text", text)
                    .toString();
        }
    }

    /**
     * Loads the given PDF file.
     *
     * @param file a file containing a PDF document
     * @throws IOException in case of problems while loading the file
     */
    public final void load(File file) throws IOException {
        Objects.requireNonNull(file, "file can not be null");
        load(() -> {
            try {
                return PDDocument.load(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    /**
     * Loads the given PDF file.
     *
     * @param stream a stream returning a PDF document
     * @throws IOException in case of problems while loading the file
     */
    public final void load(InputStream stream) {
        Objects.requireNonNull(stream, "stream can not be null");
        load(() -> {
            try {
                return PDDocument.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    /**
     * Sets the document retrieved from the given supplier.
     *
     * @param supplier
     */
    public final void load(Supplier<PDDocument> supplier) {
        Objects.requireNonNull(supplier, "supplier can not be null");
        setDocument(supplier.get());
    }
}