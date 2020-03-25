package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PDFView;
import com.dlsc.gemsfx.PDFView.SearchResult;
import com.dlsc.unitfx.IntegerInputField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PDFViewSkin extends SkinBase<PDFView> {

    // Access to PDF document must be single threaded (see Apache PdfBox website FAQs)
    private final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final ObservableList<Integer> pdfFilePages = FXCollections.observableArrayList();
    private final BorderPane borderPane;

    private ListView<Integer> thumbnailListView = new ListView<>();

    private ListView<PageSearchResult> searchResultListView = new ListView<>();

    private PDFRenderer renderer;

    private final Map<Integer, Image> imageCache = new HashMap<>();

    private final Map<SearchResult, Rectangle2D> resultBounds = new HashMap<>();

    public PDFViewSkin(PDFView view) {
        super(view);

        view.getSearchResults().addListener((Observable it) -> {
            final ObservableList<SearchResult> searchResults = view.getSearchResults();
            final Map<Integer, PageSearchResult> itemMap = new HashMap<>();

            searchResults.forEach(result -> {
                final PageSearchResult pageSearchResult = itemMap.computeIfAbsent(result.getPageNumber(),
                        key -> new PageSearchResult(result.getPageNumber(), result.getSearchText()));
                pageSearchResult.getItems().add(result);
            });

            List<PageSearchResult> list = new ArrayList<>(itemMap.values());
            Collections.sort(list);
            pageSearchResults.setAll(list);
        });
        
        searchResultListView.getStyleClass().add("search-result-list-view");
        searchResultListView.visibleProperty().bind(Bindings.isNotEmpty(pageSearchResults).and(view.showSearchResultsProperty()));
        searchResultListView.managedProperty().bind(Bindings.isNotEmpty(pageSearchResults).and(view.showSearchResultsProperty()));
        searchResultListView.setPlaceholder(null);
        searchResultListView.setCellFactory(listView -> new SearchResultListCell());
        searchResultListView.setItems(pageSearchResults);
        searchResultListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            final PageSearchResult result = searchResultListView.getSelectionModel().getSelectedItem();
            if (result != null) {
                view.setSelectedSearchResult(result.getItems().get(0));
            }
        });

        view.selectedSearchResultProperty().addListener(it -> {
            final SearchResult result = view.getSelectedSearchResult();
            if (result != null) {
                pageSearchResults.stream()
                        .filter(r -> r.getPageNumber() == result.getPageNumber()).findFirst()
                        .ifPresent(r -> {
                            searchResultListView.getSelectionModel().select(r);

                            /*
                             * We want to make sure that the selected result will be visible within the list view,
                             * but we do not want to scroll every time the selected search result changes. We really
                             * only want to perform a scrolling if the newly selected search result is not within the
                             * currently visible rows of the list view.
                             */
                            final VirtualFlow virtualFlow = (VirtualFlow) searchResultListView.lookup("VirtualFlow");
                            if (virtualFlow != null) {

                                final IndexedCell firstVisibleCell = virtualFlow.getFirstVisibleCell();
                                final IndexedCell lastVisibleCell = virtualFlow.getLastVisibleCell();

                                if (firstVisibleCell != null && lastVisibleCell != null) {

                                    /*
                                     * Adding 1 to start and subtracting 1 from the end as the calculations of the
                                     * currently visible cells doesn't seem to work perfectly. Also, if only a fraction
                                     * of a cell is visible then it requires scrolling, too.
                                     */
                                    final int start = Math.max(0, firstVisibleCell.getIndex() + 1);
                                    final int end = Math.max(1, lastVisibleCell.getIndex() - 1);
                                    final int index = searchResultListView.getItems().indexOf(r);

                                    if (index < start || index > end) {
                                        searchResultListView.scrollTo(r);
                                    }
                                }
                            }
                        });
            }
        });

        thumbnailListView.getStyleClass().add("thumbnail-list-view");
        thumbnailListView.setPlaceholder(null);
        thumbnailListView.setCellFactory(listView -> new PdfPageListCell());
        thumbnailListView.setItems(pdfFilePages);
        thumbnailListView.prefWidthProperty().bind(view.thumbnailSizeProperty().multiply(1.25));
        thumbnailListView.requestFocus();
        thumbnailListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            final Integer selectedItem = thumbnailListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                view.setPage(selectedItem);
            }
        });

        view.pageProperty().addListener(it -> {
            thumbnailListView.getSelectionModel().select(view.getPage());

            if (!pageSearchResults.isEmpty()) {
                /*
                 * We have page search results and the current page changes. Let's make sure we select a
                 * matching page result, if one exits.
                 */
                pageSearchResults.stream()
                        .filter(result -> result.getPageNumber() == view.getPage())
                        .findFirst()
                        .ifPresent(result -> searchResultListView.getSelectionModel().select(result));
            }
        });

        pdfFilePages.addListener((Observable it) -> {
            if (!pdfFilePages.isEmpty()) {
                thumbnailListView.getSelectionModel().select(0);
            }
        });

        view.documentProperty().addListener(it -> updatePagesList());
        updatePagesList();

        final ToolBar toolBar = createToolBar(view);
        toolBar.getStylesheets().add(PDFView.class.getResource("pdf-view.css").toExternalForm());
        toolBar.visibleProperty().bind(view.showToolBarProperty());
        toolBar.managedProperty().bind(view.showToolBarProperty());

        final HBox searchNavigator = createSearchNavigator();

        MainAreaScrollPane mainArea = new MainAreaScrollPane();
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox rightSide = new VBox(searchNavigator, mainArea);
        rightSide.setFillWidth(true);

        StackPane leftSide = new StackPane(thumbnailListView, searchResultListView);
        leftSide.visibleProperty().bind(view.showThumbnailsProperty());
        leftSide.managedProperty().bind(view.showThumbnailsProperty());

        borderPane = new BorderPane();
        borderPane.setTop(toolBar);
        borderPane.setLeft(leftSide);
        borderPane.setCenter(rightSide);
        borderPane.setFocusTraversable(false);

        getChildren().add(borderPane);

        view.documentProperty().addListener(it -> {
            imageCache.clear();
            view.setPage(-1);
            view.setPage(0);
        });

        view.searchTextProperty().addListener(it -> search());
    }

    private SearchService searchService;

    private void search() {
        if (searchService == null) {
            searchService = new SearchService();
            searchService.setOnSucceeded(evt -> {
                getSkinnable().getSearchResults().setAll(searchService.getValue());
                if (!searchService.getValue().isEmpty()) {
                    getSkinnable().setSelectedSearchResult(searchService.getValue().get(0));
                } else {
                    getSkinnable().setSelectedSearchResult(null);
                }
            });
        }

        searchService.restart();
    }

    class SearchService extends Service<List<SearchResult>> {

        public SearchService() {
        }

        @Override
        protected Task<List<SearchResult>> createTask() {
            return new SearchTask(getSkinnable().getDocument(), getSkinnable().getSearchText());
        }
    }

    class SearchTask extends Task<List<SearchResult>> {

        private final PDDocument document;
        private final String searchText;

        public SearchTask(PDDocument document, String searchText) {
            this.document = document;
            this.searchText = searchText;
        }

        @Override
        protected List<SearchResult> call() throws Exception {
            if (StringUtils.isBlank(searchText)) {
                return Collections.emptyList();
            }

            Thread.sleep(300);

            if (isCancelled()) {
                return Collections.emptyList();
            }

            List<SearchResult> results = new ArrayList<>();

            PDFTextStripper stripper = new PDFTextStripper() {

                private int pageNumber = -1;

                @Override
                protected void startPage(PDPage page) {
                    pageNumber++;
                }

                @Override
                protected void writeString(String text, List<TextPosition> textPositions) {
                    if (StringUtils.containsIgnoreCase(text, searchText)) {
                        SearchResult result = new SearchResult(pageNumber, text, searchText, textPositions);
                        results.add(result);
                    }
                }
            };

            Writer writer = new Writer() {

                @Override
                public void write(char[] cbuf, int off, int len) {
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() {
                }
            };

            stripper.writeText(document, writer);

            return results;
        }
    }

    private final DoubleProperty requestedVValue = new SimpleDoubleProperty(-1);

    private ToolBar createToolBar(PDFView pdfView) {
        final PDFView view = getSkinnable();

        // show all
        ToggleButton showAll = new ToggleButton();
        showAll.setGraphic(new FontIcon(MaterialDesign.MDI_FULLSCREEN));
        showAll.getStyleClass().addAll("tool-bar-button", "show-all-button");
        showAll.setTooltip(new Tooltip("Show all / whole page"));
        showAll.selectedProperty().bindBidirectional(pdfView.showAllProperty());

        // paging
        Button goLeft = new Button();
        goLeft.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_LEFT));
        goLeft.setTooltip(new Tooltip("Show previous page"));
        goLeft.setOnAction(evt -> view.gotoPreviousPage());
        goLeft.getStyleClass().addAll("tool-bar-button", "previous-page-button");
        goLeft.disableProperty().bind(Bindings.createBooleanBinding(() -> view.getPage() <= 0, view.pageProperty(), view.documentProperty()));

        Button goRight = new Button();
        goRight.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT));
        goRight.setTooltip(new Tooltip("Show next page"));
        goRight.setOnAction(evt -> view.gotoNextPage());
        goRight.getStyleClass().addAll("tool-bar-button", "next-page-button");
        goRight.disableProperty().bind(Bindings.createBooleanBinding(() -> view.getDocument() == null || view.getDocument().getNumberOfPages() <= view.getPage() + 1, view.pageProperty(), view.documentProperty()));

        IntegerInputField pageField = new IntegerInputField();
        pageField.setTooltip(new Tooltip("Current page number"));
        pageField.getStyleClass().add("page-field");
        pageField.setAllowNegatives(false);
        pageField.setMinimumValue(1);
        pageField.setAlignment(Pos.CENTER);
        pageField.setValue(view.getPage() + 1);
        view.pageProperty().addListener(it -> pageField.setValue(view.getPage() + 1));
        pageField.valueProperty().addListener(it -> {
            final Integer value = pageField.getValue();
            if (value != null) {
                view.setPage(value - 1);
            }
        });
        updateMaximumValue(pageField);
        view.documentProperty().addListener(it -> updateMaximumValue(pageField));

        HBox pageControl = new HBox(goLeft, pageField, goRight);
        pageControl.disableProperty().bind(view.documentProperty().isNull());
        pageControl.getStyleClass().add("page-control");

        // rotate buttons
        Button rotateLeft = new Button();
        rotateLeft.getStyleClass().addAll("tool-bar-button", "rotate-left");
        rotateLeft.setTooltip(new Tooltip("Rotate page left"));
        rotateLeft.setGraphic(new FontIcon(MaterialDesign.MDI_ROTATE_LEFT));
        rotateLeft.setOnAction(evt -> view.rotateLeft());

        Button rotateRight = new Button();
        rotateRight.getStyleClass().addAll("tool-bar-button", "rotate-right");
        rotateRight.setTooltip(new Tooltip("Rotate page right"));
        rotateRight.setGraphic(new FontIcon(MaterialDesign.MDI_ROTATE_RIGHT));
        rotateRight.setOnAction(evt -> view.rotateRight());

        // zoom slider
        Slider zoomSlider = new Slider();
        zoomSlider.setMin(1);
        zoomSlider.maxProperty().bind(view.maxZoomFactorProperty());
        zoomSlider.valueProperty().bindBidirectional(view.zoomFactorProperty());
        zoomSlider.disableProperty().bind(view.showAllProperty());

        final Label zoomLabel = new Label("Zoom");
        zoomLabel.disableProperty().bind(view.showAllProperty());

        // search icon / field
        final FontIcon searchClearIcon = new FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE);
        searchClearIcon.visibleProperty().bind(view.searchTextProperty().isNotEmpty());
        searchClearIcon.setOnMouseClicked(evt -> view.setSearchText(null));
        Tooltip.install(searchClearIcon, new Tooltip("Clear search text"));

        CustomTextField searchField = new CustomTextField();
        searchField.setText("Search text");
        searchField.getStyleClass().add("search-field");
        searchField.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode() == KeyCode.ESCAPE) {
                searchField.setText("");
            }
        });

        searchField.setRight(searchClearIcon);
        searchField.setPromptText("Search ...");
        searchField.textProperty().bindBidirectional(view.searchTextProperty());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // toolbar
        return new ToolBar(
                showAll,
                new Separator(Orientation.VERTICAL),
                zoomLabel,
                zoomSlider,
                new Separator(Orientation.VERTICAL),
                pageControl,
                new Separator(Orientation.VERTICAL),
                rotateLeft,
                rotateRight,
                spacer,
                searchField
        );
    }

    private HBox createSearchNavigator() {
        final PDFView view = getSkinnable();

        final Label searchLabel = new Label();
        searchLabel.textProperty().bind(Bindings.createObjectBinding(() -> "Found " + view.getSearchResults().size() + " occurrences on " + pageSearchResults.size() + " pages", view.getSearchResults(), pageSearchResults));
        searchLabel.getStyleClass().add("search-result-label");

        final Button previousResultButton = new Button();
        previousResultButton.getStyleClass().addAll("search-bar-button", "previous-search-result");
        previousResultButton.setTooltip(new Tooltip("Go to previous search result"));
        previousResultButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_LEFT));
        previousResultButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        previousResultButton.setOnAction(evt -> showPreviousSearchResult());
        previousResultButton.setMaxHeight(Double.MAX_VALUE);

        final Button nextResultButton = new Button();
        nextResultButton.getStyleClass().addAll("search-bar-button", "next-search-result");
        nextResultButton.setTooltip(new Tooltip("Go to next search result"));
        nextResultButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT));
        nextResultButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        nextResultButton.setOnAction(evt -> showNextSearchResult());
        nextResultButton.setMaxHeight(Double.MAX_VALUE);

        final Button doneButton = new Button("Done");
        doneButton.setOnAction(evt -> view.setSearchText(null));
        doneButton.getStyleClass().addAll("search-bar-button");

        final BooleanBinding searchResultsAvailable = Bindings.isNotEmpty(view.getSearchResults());

        HBox buttonBox = new HBox(previousResultButton, nextResultButton);

        HBox searchBox = new HBox(searchLabel, buttonBox, doneButton);
        searchBox.getStyleClass().add("search-navigator");
        searchBox.visibleProperty().bind(searchResultsAvailable);
        searchBox.managedProperty().bind(searchResultsAvailable);

        return searchBox;
    }

    public final void showNextSearchResult() {
        final PDFView view = getSkinnable();
        int index = view.getSearchResults().indexOf(view.getSelectedSearchResult());
        if (index < view.getSearchResults().size() - 1) {
            view.setSelectedSearchResult(view.getSearchResults().get(index + 1));
        } else {
            view.setSelectedSearchResult(view.getSearchResults().get(0));
        }
    }

    public final void showPreviousSearchResult() {
        final PDFView view = getSkinnable();
        int index = view.getSearchResults().indexOf(view.getSelectedSearchResult());
        if (index > 0) {
            view.setSelectedSearchResult(view.getSearchResults().get(index - 1));
        } else {
            view.setSelectedSearchResult(view.getSearchResults().get(view.getSearchResults().size() - 1));
        }
    }

    private void updateMaximumValue(IntegerInputField pageField) {
        final PDDocument document = getSkinnable().getDocument();
        if (document != null) {
            pageField.setMaximumValue(document.getNumberOfPages());
        }
    }

    class PagerService extends Service<Void> {
        private boolean up;

        public void setUp(boolean up) {
            this.up = up;
        }

        @Override
        protected Task<Void> createTask() {
            return new PagerTask(up);
        }
    }

    class PagerTask extends Task<Void> {
        private boolean up;

        public PagerTask(boolean up) {
            this.up = up;
        }

        @Override
        protected Void call() throws Exception {
            Thread.sleep(100);
            Platform.runLater(() -> {
                if (up) {
                    getSkinnable().gotoPreviousPage();
                    requestedVValue.set(1);
                } else {
                    getSkinnable().gotoNextPage();
                    requestedVValue.set(0);
                }
            });

            return null;
        }
    }

    private final PagerService pagerService = new PagerService();

    class MainAreaScrollPane extends ScrollPane {

        private final StackPane wrapper;
        private Pane pane;
        private Group group;
        private RenderService mainAreaRenderService = new RenderService(false);
        private Region bouncer = new Region();

        public MainAreaScrollPane() {

            bouncer.setStyle("-fx-background-color: orange; -fx-border-color: red;");
            bouncer.setManaged(false);
            getSkinnable().selectedSearchResultProperty().addListener(it -> boundsSearchResult());

            getSkinnable().getSearchResults().addListener((Observable it) -> mainAreaRenderService.restart());

            mainAreaRenderService.setOnSucceeded(evt -> {
                double vValue = requestedVValue.get();
                if (vValue != -1) {
                    setVvalue(vValue);
                    requestedVValue.set(-1);
                }

                boundsSearchResult();
            });

            addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                switch (evt.getCode()) {
                    case UP:
                    case LEFT:
                    case PAGE_UP:
                    case HOME:
                        if (getVvalue() == 0 || getSkinnable().isShowAll() || evt.getCode() == KeyCode.LEFT) {
                            requestedVValue.set(1);
                            getSkinnable().gotoPreviousPage();
                        }
                        break;
                    case DOWN:
                    case RIGHT:
                    case PAGE_DOWN:
                    case END:
                        if (getVvalue() == 1 || getSkinnable().isShowAll() || evt.getCode() == KeyCode.RIGHT) {
                            requestedVValue.set(0);
                            getSkinnable().gotoNextPage();
                        }
                        break;
                }
            });

            addEventHandler(ScrollEvent.SCROLL, evt -> {
                if (evt.isInertia()) {
                    return;
                }

                boolean success;

                if (evt.getDeltaY() > 0) {
                    success = getSkinnable().getPage() > 1;
                    pagerService.setUp(true);
                } else {
                    success = getSkinnable().getPage() < getSkinnable().getDocument().getNumberOfPages() - 1;
                    pagerService.setUp(false);
                }

                if (success) {
                    pagerService.restart();
                    evt.consume();
                }

            });

            setFitToWidth(true);
            setFitToHeight(true);
            setPannable(true);

            pane = new
                    Pane() {
                        @Override
                        protected void layoutChildren() {
                            wrapper.resizeRelocate((getWidth() - wrapper.prefWidth(-1)) / 2, (getHeight() - wrapper.prefHeight(-1)) / 2, wrapper.prefWidth(-1), wrapper.prefHeight(-1));
                        }
                    }

            ;

            wrapper = new StackPane();
            wrapper.getStyleClass().add("image-view-wrapper");
            wrapper.setMaxWidth(Region.USE_PREF_SIZE);
            wrapper.setMaxHeight(Region.USE_PREF_SIZE);
            wrapper.rotateProperty().bind(getSkinnable().pageRotationProperty());

            group = new Group(wrapper);
            pane.getChildren().addAll(group, bouncer);

            viewportBoundsProperty().addListener(it -> {
                final Bounds bounds = getViewportBounds();

                pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
                pane.setMinWidth(Region.USE_COMPUTED_SIZE);

                pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
                pane.setMinHeight(Region.USE_COMPUTED_SIZE);

                if (isPortrait()) {

                    final double prefWidth = bounds.getWidth() * getSkinnable().getZoomFactor() - 5;
                    pane.setPrefWidth(prefWidth);
                    pane.setMinWidth(prefWidth);

                    if (getSkinnable().isShowAll()) {
                        pane.setPrefHeight(bounds.getHeight() - 5);
                    } else {
                        Image image = getImage();
                        if (image != null) {
                            double scale = bounds.getWidth() / image.getWidth();
                            double scaledImageHeight = image.getHeight() * scale;
                            final double prefHeight = scaledImageHeight * getSkinnable().getZoomFactor();
                            pane.setPrefHeight(prefHeight);
                            pane.setMinHeight(prefHeight);
                        }
                    }

                } else {

                    /*
                     * Image has been rotated.
                     */

                    final double prefHeight = bounds.getHeight() * getSkinnable().getZoomFactor() - 5;
                    pane.setPrefHeight(prefHeight);
                    pane.setMinHeight(prefHeight);

                    if (getSkinnable().isShowAll()) {
                        pane.setPrefWidth(bounds.getWidth() - 5);
                    } else {
                        Image image = getImage();
                        if (image != null) {
                            double scale = bounds.getHeight() / image.getWidth();
                            double scaledImageHeight = image.getHeight() * scale;
                            final double prefWidth = scaledImageHeight * getSkinnable().getZoomFactor();
                            pane.setPrefWidth(prefWidth);
                            pane.setMinWidth(prefWidth);
                        }
                    }

                }
            });

            setContent(pane);

            mainAreaRenderService.setExecutor(EXECUTOR);
            mainAreaRenderService.scaleProperty().bind(getSkinnable().pageScaleProperty().multiply(getSkinnable().zoomFactorProperty()));
            mainAreaRenderService.pageProperty().bind(getSkinnable().pageProperty());
            mainAreaRenderService.valueProperty().addListener(it -> {
                Image image = mainAreaRenderService.getValue();
                if (image != null) {
                    setImage(image);
                }
            });

            getSkinnable().showAllProperty().addListener(it -> {
                updateScrollbarPolicies();
                layoutImage();
                requestLayout();
            });

            getSkinnable().pageRotationProperty().addListener(it -> {
                updateScrollbarPolicies();
                layoutImage();
            });

            getSkinnable().zoomFactorProperty().addListener(it -> {
                updateScrollbarPolicies();
                requestLayout();
            });

            updateScrollbarPolicies();

            layoutImage();
        }

        private void boundsSearchResult() {
            final SearchResult result = getSkinnable().getSelectedSearchResult();
            if (result != null) {
                final Rectangle2D bounds = resultBounds.get(result);
                if (bounds != null) {
                    System.out.println(bounds);
                    bouncer.setLayoutX(bounds.getMinX());
                    bouncer.setLayoutY(bounds.getMinY());
                    bouncer.setPrefWidth(bounds.getWidth());
                    bouncer.setPrefHeight(bounds.getHeight());
                }
            }
        }

        private final ObjectProperty<Image> image = new SimpleObjectProperty<>(this, "image");

        private void setImage(Image image) {
            this.image.set(image);
        }

        private Image getImage() {
            return image.get();
        }

        protected void layoutImage() {
            ImageView imageView = new ImageView();
            imageView.imageProperty().bind(image);
            imageView.setPreserveRatio(true);
            wrapper.getChildren().setAll(imageView);

            requestLayout();

            if (getSkinnable().isShowAll()) {
                fitAll(imageView);
            } else {
                fitWidth(imageView);
            }
        }

        private void fitWidth(ImageView imageView) {
            if (isPortrait()) {
                imageView.fitWidthProperty().bind(pane.widthProperty().subtract(40));
                imageView.fitHeightProperty().unbind();
            } else {
                imageView.fitWidthProperty().bind(pane.heightProperty().subtract(40));
                imageView.fitHeightProperty().unbind();
            }
        }

        private void fitAll(ImageView imageView) {
            if (isPortrait()) {
                imageView.fitWidthProperty().bind(pane.widthProperty().subtract(40));
                imageView.fitHeightProperty().bind(pane.heightProperty().subtract(40));
            } else {
                imageView.fitWidthProperty().bind(pane.heightProperty().subtract(40));
                imageView.fitHeightProperty().bind(pane.widthProperty().subtract(40));
            }
        }

        private void updateScrollbarPolicies() {
            if (getSkinnable().isShowAll()) {
                setVbarPolicy(ScrollBarPolicy.NEVER);
                setHbarPolicy(ScrollBarPolicy.NEVER);
            } else {
                if (getSkinnable().getZoomFactor() > 1) {
                    setVbarPolicy(ScrollBarPolicy.ALWAYS);
                    setHbarPolicy(ScrollBarPolicy.ALWAYS);
                } else {
                    if (isPortrait()) {
                        setVbarPolicy(ScrollBarPolicy.ALWAYS);
                        setHbarPolicy(ScrollBarPolicy.NEVER);
                    } else {
                        setVbarPolicy(ScrollBarPolicy.NEVER);
                        setHbarPolicy(ScrollBarPolicy.ALWAYS);
                    }
                }
            }
        }

        private boolean isPortrait() {
            return getSkinnable().getPageRotation() % 180 == 0;
        }
    }

    private class RenderService extends Service<Image> {

        private final boolean thumbnailRenderer;

        public RenderService(boolean thumbnailRenderer) {
            this.thumbnailRenderer = thumbnailRenderer;

            setExecutor(EXECUTOR);

            final InvalidationListener restartListener = it -> restart();
            page.addListener(restartListener);
            scale.addListener(restartListener);
        }

        private final FloatProperty scale = new SimpleFloatProperty();

        private float getScale() {
            return scale.get();
        }

        FloatProperty scaleProperty() {
            return scale;
        }

        // initialize with -1 to ensure property fires
        private final IntegerProperty page = new SimpleIntegerProperty(-1);

        private final void setPage(int page) {
            this.page.set(page);
        }

        private int getPage() {
            return page.get();
        }

        IntegerProperty pageProperty() {
            return page;
        }

        @Override
        protected Task<Image> createTask() {
            return new RenderTask(thumbnailRenderer, getPage(), getScale());
        }
    }

    private class RenderTask extends Task<Image> {

        private final int page;
        private final float scale;
        private final boolean thumbnail;

        public RenderTask(boolean thumbnail, int page, float scale) {
            this.thumbnail = thumbnail;
            this.page = page;
            this.scale = scale;
        }

        @Override
        protected Image call() {
            if (page >= 0 && page < getSkinnable().getDocument().getNumberOfPages()) {
                if (!isCancelled()) {
                    try {

                        double s = scale;
                        final Image renderedImage = renderPDFPage(page, (float) s);
                        if (getSkinnable().isCacheThumbnails() && thumbnail) {
                            imageCache.put(page, renderedImage);
                        }
                        return renderedImage;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        private Image renderPDFPage(int pageNumber, float scale) throws IOException {
            BufferedImage bufferedImage = renderer.renderImage(pageNumber, scale, ImageType.ARGB, RenderDestination.VIEW);

            // only highlight search results in the main view (for performance reasons)
            if (!thumbnail) {
                highlightSearchResults(pageNumber, scale, bufferedImage);
            }

            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            return image;
        }

        private void highlightSearchResults(int pageNumber, float scale, BufferedImage bufferedImage) {
            final List<SearchResult> searchResults = getSkinnable().getSearchResults().stream().filter(result -> result.getPageNumber() == pageNumber).collect(Collectors.toList());

            if (!searchResults.isEmpty()) {
                final PDDocument document = getSkinnable().getDocument();
                final PDPage page = document.getPage(pageNumber);

                final Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));

                final Color searchResultColor = getSkinnable().getSearchResultColor();

                graphics.setStroke(new BasicStroke(8));
                graphics.setColor(new java.awt.Color((int) (255 * searchResultColor.getRed()), (int) (255 * searchResultColor.getGreen()), (int) (255 * searchResultColor.getBlue())));

                searchResults.forEach(result -> {
                    final String searchText = result.getSearchText();
                    final List<TextPosition> textPositions = result.getTextPositions();
                    final int startIndex = result.getText().toLowerCase().indexOf(searchText.toLowerCase());

                    float x1 = Float.MAX_VALUE;
                    float x2 = 0;
                    float y1 = Float.MAX_VALUE;
                    float y2 = 0;

                    for (int i = startIndex; i < startIndex + searchText.length(); i++) {
                        TextPosition position = textPositions.get(i);

                        x1 = Math.min(x1, position.getXDirAdj() * scale);
                        x2 = Math.max(x2, (position.getXDirAdj() + position.getWidth()) * scale);
                        y1 = Math.min(y1, (position.getYDirAdj() - position.getHeight()) * scale);
                        y2 = Math.max(y2, position.getYDirAdj() * scale);
                    }

                    x1 -= 10;
                    x2 += 10;
                    y1 -= 10;
                    y2 += 10;

                    graphics.fillRect((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1));

                    resultBounds.put(result, new Rectangle2D(x1, x2, y1, y2));
                });
            }
        }
    }

    private void updatePagesList() {
        final PDDocument document = getSkinnable().getDocument();
        pdfFilePages.clear();
        if (document != null) {
            renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                pdfFilePages.add(i);
            }
        }
    }

    class SearchResultListCell extends ListCell<PageSearchResult> {

        private final Label pageLabel = new Label();
        private final Label matchesLabel = new Label();
        private final Label summaryLabel = new Label();
        private final ImageView imageView = new ImageView();
        private final RenderService renderService = new RenderService(true);

        public SearchResultListCell() {
            pageLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(pageLabel, Priority.ALWAYS);

            HBox header = new HBox(pageLabel, matchesLabel);
            header.setFillHeight(true);
            header.setAlignment(Pos.TOP_LEFT);

            VBox.setVgrow(summaryLabel, Priority.ALWAYS);
            VBox box = new VBox(5, header, summaryLabel);
            box.setFillWidth(true);

            imageView.setPreserveRatio(true);

            StackPane stackPane = new StackPane(imageView);
            stackPane.getStyleClass().add("image-view-wrapper");
            stackPane.setMaxWidth(Region.USE_PREF_SIZE);
            stackPane.visibleProperty().bind(imageView.imageProperty().isNotNull());

            pageLabel.getStyleClass().add("page-label");
            matchesLabel.getStyleClass().add("matches-label");
            summaryLabel.getStyleClass().add("summary-label");
            summaryLabel.setWrapText(true);

            HBox.setHgrow(box, Priority.ALWAYS);
            HBox finalBox = new HBox(10, stackPane, box);
            finalBox.setFillHeight(false);
            finalBox.setAlignment(Pos.TOP_LEFT);

            finalBox.visibleProperty().bind(itemProperty().isNotNull());

            setGraphic(finalBox);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            renderService.scaleProperty().bind(PDFViewSkin.this.getSkinnable().thumbnailPageScaleProperty());
            renderService.valueProperty().addListener(it -> imageView.setImage(renderService.getValue()));

            itemProperty().addListener(it -> {
                final PageSearchResult item = getItem();
                if (item != null) {
                    final Image image = imageCache.get(item.getPageNumber());
                    if (getSkinnable().isCacheThumbnails() && image != null) {
                        imageView.setImage(image);
                    } else {
                        renderService.setPage(item.getPageNumber());
                    }
                } else {
                    imageView.setImage(null);
                }
            });

            setPrefWidth(0);
        }

        @Override
        protected void updateItem(PageSearchResult item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {

                pageLabel.setText("Page " + (item.getPageNumber() + 1));

                int matchCount = item.getItems().size();
                if (matchCount == 1) {
                    matchesLabel.setText(matchCount + " match");
                } else {
                    matchesLabel.setText(matchCount + " matches");
                }

                final String text = item.getItems().stream().map(resultItem -> resultItem.getText()).collect(Collectors.joining("... "));
                summaryLabel.setText(text.substring(0, Math.min(120, text.length())));

                final PDDocument document = getSkinnable().getDocument();
                final PDPage page = document.getPage(item.getPageNumber());
                final PDRectangle cropBox = page.getCropBox();

                if (cropBox.getHeight() < cropBox.getWidth()) {
                    imageView.fitWidthProperty().bind(getSkinnable().thumbnailSizeProperty().divide(3));
                    imageView.fitHeightProperty().unbind();
                } else {
                    imageView.fitWidthProperty().unbind();
                    imageView.fitHeightProperty().bind(getSkinnable().thumbnailSizeProperty().divide(3));
                }
            }
        }
    }

    class PdfPageListCell extends ListCell<Integer> {

        private final ImageView imageView = new ImageView();
        private final Label pageNumberLabel = new Label();
        private final RenderService renderService = new RenderService(true);

        public PdfPageListCell() {
            StackPane stackPane = new StackPane(imageView);
            stackPane.getStyleClass().add("image-view-wrapper");
            stackPane.setMaxWidth(Region.USE_PREF_SIZE);
            stackPane.visibleProperty().bind(imageView.imageProperty().isNotNull());

            pageNumberLabel.getStyleClass().add("page-number-label");
            pageNumberLabel.visibleProperty().bind(imageView.imageProperty().isNotNull());

            VBox vBox = new VBox(5, stackPane, pageNumberLabel);
            vBox.setAlignment(Pos.CENTER);
            vBox.setFillWidth(true);
            vBox.visibleProperty().bind(emptyProperty().not());
            setGraphic(vBox);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            imageView.setPreserveRatio(true);

            setAlignment(Pos.CENTER);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setMinSize(0, 0);

            indexProperty().addListener(it -> {
                final Image image = imageCache.get(getIndex());
                if (getSkinnable().isCacheThumbnails() && image != null) {
                    imageView.setImage(image);
                } else {
                    renderService.setPage(getIndex());
                }
            });

            renderService.scaleProperty().bind(PDFViewSkin.this.getSkinnable().thumbnailPageScaleProperty());
            renderService.valueProperty().addListener(it -> imageView.setImage(renderService.getValue()));
        }

        @Override
        protected void updateItem(Integer pageNumber, boolean empty) {
            super.updateItem(pageNumber, empty);

            if (pageNumber != null && !empty) {
                final PDDocument document = getSkinnable().getDocument();
                final PDPage page = document.getPage(pageNumber);
                final PDRectangle cropBox = page.getCropBox();

                if (cropBox.getHeight() < cropBox.getWidth()) {
                    imageView.fitWidthProperty().bind(getSkinnable().thumbnailSizeProperty());
                    imageView.fitHeightProperty().unbind();
                } else {
                    imageView.fitWidthProperty().unbind();
                    imageView.fitHeightProperty().bind(getSkinnable().thumbnailSizeProperty());
                }

                pageNumberLabel.setText(Integer.toString(getIndex() + 1));
            }
        }
    }

    private final ObservableList<PageSearchResult> pageSearchResults = FXCollections.observableArrayList();

    public static class PageSearchResult implements Comparable<PageSearchResult> {

        private final int pageNumber;
        private final String searchText;
        private final List<SearchResult> items = new ArrayList<>();

        public PageSearchResult(int pageNumber, String searchText) {
            this.pageNumber = pageNumber;
            this.searchText = searchText;
        }

        public String getSearchText() {
            return searchText;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public List<SearchResult> getItems() {
            return items;
        }

        @Override
        public int compareTo(PageSearchResult o) {
            return getPageNumber() - o.getPageNumber();
        }
    }
}
