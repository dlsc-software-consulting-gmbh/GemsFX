package com.dlsc.gemsfx;

import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A custom stackpane that supports a drawer view sliding in from bottom to top. The content of the drawer gets added
 * in the normal way via the childrens list. The content for the drawer has to be added by calling {@link #setDrawerContent(Node)}.
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>User can resize the drawer via a handle at the top</li>
 *     <li>The drawer automatically closes completely if the user drags the resize handle below the lower bounds of the stackpane</li>
 *     <li>Opening and closing can be animated (see {@link #setAnimateDrawer(boolean)})</li>
 *     <li>When the drawer is open the content of the stackpane will be blocked from user input via a dark semi-transparent glass pane</li>
 *     <li>The glass pane fades in / out</li>
 *     <li>The drawer can have its own preferred width (see {@link #setPreferredDrawerWidth(double)})</li>
 *     <li>The control can automatically persist the drawer height via the Java preferences API (see {@link #setPreferencesKey(String)})</li>
 *     <li>Auto hiding: drawer will close when the user clicks into the background (onto the glass pane)</li>
 * </ul>
 */
public class DrawerStackPane extends StackPane {

    private static final Logger LOG = Logger.getLogger(DrawerStackPane.class.getName());

    private static final int MAXIMIZE = -1;

    private GlassPane glassPane;

    private VBox drawer;

    private double startY;

    private HBox headerBox;

    private Timeline timeline;

    /**
     * Constructs a new drawer stack pane.
     *
     * @param nodes the children nodes
     */
    public DrawerStackPane(Node... nodes) {
        super(nodes);
        init();
    }

    /**
     * Constructs a new drawer stack pane.
     */
    public DrawerStackPane() {
        super();
        init();
    }

    private void init() {
        getStyleClass().add("drawer-stackpane");

        drawer = createContainer();
        drawer.getStyleClass().add("drawer");
        drawer.setVisible(false);
        drawer.setMinHeight(0);
        drawer.setPrefHeight(0);
        drawer.setManaged(false);

        glassPane = new GlassPane();
        glassPane.fadeInOutDurationProperty().bind(animationDurationProperty());
        glassPane.fadeInOutProperty().bind(animateDrawerProperty());
        glassPane.setOnMouseClicked(evt -> {
            if (isAutoHide() && evt.getButton().equals(MouseButton.PRIMARY) && !evt.isConsumed()) {
                setShowDrawer(false);
            }
        });

        glassPane.hideProperty().bind(showDrawer.not());

        getChildren().addAll(glassPane, drawer);

        drawerContentProperty().addListener((observable, oldContent, newContent) -> {
            if (oldContent != null) {
                drawer.getChildren().remove(oldContent);
            }

            if (newContent != null) {
                drawer.getChildren().add(newContent);
                VBox.setVgrow(newContent, Priority.ALWAYS);
            }
        });

        headerBox.setOnMousePressed(evt -> startY = evt.getScreenY());

        headerBox.setOnMouseReleased(evt -> {
            if (startY != -1 && evt.getY() > drawer.getHeight()) {
                setShowDrawer(false);
                startY = 0;
            } else {
                saveDrawerHeightToUserPreferences();
            }
        });

        headerBox.setOnMouseDragged(evt -> {
            if (startY != -1) {
                double deltaY = startY - evt.getScreenY();
                double height = getDrawerHeight();
                height += (deltaY / getHeight());
                if (height > getMinDrawerHeight() && height <= getMaxDrawerHeight()) {
                    height = Math.max(getMinDrawerHeight(), Math.min(getMaxDrawerHeight(), height));
                    setDrawerHeight(height);
                    startY = evt.getScreenY();
                }
            }
        });

        headerBox.setOnMouseClicked(evt -> {
            if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 2) {
                if (getDrawerHeight() < 1) {
                    setDrawerHeight(1);
                    saveDrawerHeightToUserPreferences();
                } else {
                    setShowDrawer(false);
                }
            }
        });

        setDrawerHeight(loadDrawerHeightFromUserPreferences());

        showDrawerProperty().addListener(it -> {
            stopCurrentlyRunningTimeline();

            if (isShowDrawer()) {
                showDrawer();
            } else {
                hideDrawer();
            }
        });

        InvalidationListener layoutListener = it -> requestLayout();
        drawerHeightProperty().addListener(layoutListener);
        preferredDrawerWidthProperty().addListener(layoutListener);
        topPaddingProperty().addListener(layoutListener);
        sidePaddingProperty().addListener(layoutListener);

        minDrawerHeight.addListener(it -> {
            if (getMinDrawerHeight() < 0) {
                throw new IllegalArgumentException("minimum drawer height can not be smaller than 0 but was " + getMinDrawerHeight());
            }
        });

        maxDrawerHeight.addListener(it -> {
            if (getMaxDrawerHeight() > 1) {
                throw new IllegalArgumentException("maximum drawer height can not be greater than 1 but was " + getMaxDrawerHeight());
            }
        });

        sidePadding.addListener(it -> {
            if (getSidePadding() < 0) {
                throw new IllegalArgumentException("side padding must be larger or equal to 0 but was " + getSidePadding());
            }
        });

        topPadding.addListener(it -> {
            if (getTopPadding() < 0) {
                throw new IllegalArgumentException("top padding must be larger or equal to 0 but was " + getTopPadding());
            }
        });

        addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (isShowDrawer() && evt.getCode().equals(KeyCode.ESCAPE)) {
                setShowDrawer(false);
                evt.consume();
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DrawerStackPane.class.getResource("drawer-stackpane.css")).toExternalForm();
    }

    // fade in / out support

    private final BooleanProperty fadeInOut = new SimpleBooleanProperty(this, "fadeInOut", true);

    /**
     * Specifies whether the glass pane (used for blocking user input to nodes in the background) will
     * use a fade transition when it becomes visible.
     *
     * @return true if the glass pane will fade in / out smoothly when appearing / disappearing
     */
    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    public final void setFadeInOut(boolean fadeInOut) {
        this.fadeInOut.set(fadeInOut);
    }

    // auto hide support

    private final BooleanProperty autoHide = new SimpleBooleanProperty(this, "autoHide", true);

    public boolean isAutoHide() {
        return autoHide.get();
    }

    /**
     * Makes the drawer close if the user clicks in the background (onto the glass pane).
     *
     * @return true if the drawer hides when user clicks on background
     */
    public BooleanProperty autoHideProperty() {
        return autoHide;
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide.set(autoHide);
    }

    private final StringProperty preferencesKey = new SimpleStringProperty(this, "preferencesKey", "drawer.stackpane");

    /**
     * Stores the key used to store the drawer height via the preferences store.
     *
     * @return the preferences key property
     */
    public final StringProperty preferencesKeyProperty() {
        return preferencesKey;
    }

    /**
     * Sets the key that will be used when storing the last drawer height via
     * the Java preferences API.
     *
     * @param key the preferences key
     */
    public final void setPreferencesKey(String key) {
        preferencesKey.set(key);
    }

    /**
     * Returns the key that will be used when storing the last drawer height via
     * the Java preferences API.
     *
     * @return the preferences key
     */
    public final String getPreferencesKey() {
        return preferencesKey.get();
    }

    // max drawer height support

    private final DoubleProperty maxDrawerHeight = new SimpleDoubleProperty(this, "maxDrawerHeight", 1);

    public double getMaxDrawerHeight() {
        return maxDrawerHeight.get();
    }

    /**
     * The maximum drawer height, a value between 0 and 1 with 1 meaning that the drawer can
     * be as high as the stackpane.
     *
     * @return the maximum drawer height (value between 0 and 1)
     */
    public DoubleProperty maxDrawerHeightProperty() {
        return maxDrawerHeight;
    }

    public void setMaxDrawerHeight(double maxDrawerHeight) {
        this.maxDrawerHeight.set(maxDrawerHeight);
    }

    // min drawer height support

    private final DoubleProperty minDrawerHeight = new SimpleDoubleProperty(this, "minDrawerHeight", .1);

    public double getMinDrawerHeight() {
        return minDrawerHeight.get();
    }

    /**
     * The minimum drawer height, a value between 0 and 1 with 0 meaning that the drawer can be made
     * completely invisible. Even with a value larger than 0 the drawer can be made to hide by the user
     * by continuing to drag below the drawer.
     *
     * @return the minimum drawer height (value between 0 and 1)
     */
    public DoubleProperty minDrawerHeightProperty() {
        return minDrawerHeight;
    }

    public void setMinDrawerHeight(double minDrawerHeight) {
        this.minDrawerHeight.set(minDrawerHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double availableHeight = getHeight() - getTopPadding();
        double maxDrawerWidth = getWidth() - 2 * getSidePadding();
        double drawerWidth;

        if (getPreferredDrawerWidth() == MAXIMIZE) {
            drawerWidth = maxDrawerWidth;
        } else {
            drawerWidth = Math.min(getPreferredDrawerWidth(), maxDrawerWidth);
        }

        double drawerHeight = getDrawerHeight();

        double x = (getWidth() - drawerWidth) / 2;
        double y = getHeight() - drawerHeight * availableHeight;

        drawer.resizeRelocate(x, y, drawerWidth, availableHeight * drawerHeight);
    }

    private VBox createContainer() {
        StackPane dragHandle = createDragHandle();
        Label titleLabel = createTitleLabel();
        ToolBar toolBar = createToolBar();

        titleLabel.setMouseTransparent(true);

        StackPane.setAlignment(dragHandle, Pos.CENTER);

        headerBox = new HBox(titleLabel, toolBar);
        headerBox.getStyleClass().add("header");
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setFillHeight(false);
        headerBox.setCursor(Cursor.N_RESIZE);
        headerBox.alignmentProperty().bind(Bindings.createObjectBinding(() -> isShowDrawerTitle() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT, showDrawerTitleProperty()));

        drawerTitleExtraProperty().addListener((obs, oldExtra, newExtra) -> addExtra(headerBox, oldExtra, newExtra));
        addExtra(headerBox, null, getDrawerTitleExtra());

        StackPane topContainer = new StackPane(headerBox, dragHandle);
        topContainer.getStyleClass().add("top");

        VBox box = new VBox(topContainer);
        box.setFillWidth(true);

        return box;
    }

    private final ListProperty<Node> toolbarItems = new SimpleListProperty<>(this, "toolbarItems", FXCollections.observableArrayList());

    public final ObservableList<Node> getToolbarItems() {
        return toolbarItems.get();
    }

    /**
     * The list of items to display in the toolbar.
     *
     * @return the toolbar items / toolbar buttons
     */
    public final ListProperty<Node> toolbarItemsProperty() {
        return toolbarItems;
    }

    public final void setToolbarItems(ObservableList<Node> items) {
        toolbarItems.set(items);
    }

    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        Bindings.bindContent(toolBar.getItems(), toolbarItemsProperty());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(evt -> setShowDrawer(false));
        closeButton.getStyleClass().add("close-button");
        getToolbarItems().add(closeButton);

        return toolBar;
    }

    private Label createTitleLabel() {
        Label titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(drawerTitleProperty());
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        titleLabel.visibleProperty().bind(showDrawerTitleProperty());
        titleLabel.managedProperty().bind(showDrawerTitleProperty());
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        return titleLabel;
    }

    private StackPane createDragHandle() {
        Separator line1 = new Separator();
        Separator line2 = new Separator();
        Separator line3 = new Separator();

        VBox handle = new VBox(line1, line2, line3);
        handle.setAlignment(Pos.CENTER);
        handle.getStyleClass().add("handle");
        handle.setMouseTransparent(true);

        StackPane dragHandle = new StackPane();
        dragHandle.setMouseTransparent(true);
        dragHandle.setMaxWidth(Double.MAX_VALUE);
        dragHandle.getStyleClass().add("drag-handle");
        dragHandle.getChildren().add(handle);

        return dragHandle;
    }

    private void addExtra(HBox headerBox, Node oldExtra, Node newExtra) {
        if (oldExtra != null) {
            headerBox.getChildren().remove(oldExtra);
        }
        if (newExtra != null) {
            headerBox.getChildren().add(newExtra);
        }
    }

    private final ObjectProperty<Runnable> onDrawerClose = new SimpleObjectProperty<>(this, "onDrawerClose");

    public final Runnable getOnDrawerClose() {
        return onDrawerClose.get();
    }

    /**
     * A callback that will be invoked when the drawer gets closed.
     *
     * @return a callback that will be invoked when the drawer gets closed
     */
    public final ObjectProperty<Runnable> onDrawerCloseProperty() {
        return onDrawerClose;
    }

    public final void setOnDrawerClose(Runnable onDrawerClose) {
        this.onDrawerClose.set(onDrawerClose);
    }

    // show drawer title support

    private final BooleanProperty showDrawerTitle = new SimpleBooleanProperty(this, "showDrawerTitle", false);

    public final boolean isShowDrawerTitle() {
        return showDrawerTitle.get();
    }

    /**
     * A flag used to signal whether the drawer should have a title bar or not.
     *
     * @return true if the drawer shows a title
     */
    public final BooleanProperty showDrawerTitleProperty() {
        return showDrawerTitle;
    }

    public final void setShowDrawerTitle(boolean showDrawerTitle) {
        this.showDrawerTitle.set(showDrawerTitle);
    }

    // drawer title support

    private final StringProperty drawerTitle = new SimpleStringProperty(this, "drawerTitle", "Untitled");

    public final String getDrawerTitle() {
        return drawerTitle.get();
    }

    /**
     * The text shown as the title of the drawer.
     *
     * @return the drawer title text
     */
    public final StringProperty drawerTitleProperty() {
        return drawerTitle;
    }

    public void setDrawerTitle(String drawerTitle) {
        this.drawerTitle.set(drawerTitle);
    }

    // drawer content support

    private final ObjectProperty<Node> drawerContent = new SimpleObjectProperty<>(this, "drawerContent");

    public final Node getDrawerContent() {
        return drawerContent.get();
    }

    /**
     * Stores the content of the drawer.
     *
     * @return the drawer content
     */
    public final ObjectProperty<Node> drawerContentProperty() {
        return drawerContent;
    }

    public final void setDrawerContent(Node drawerContent) {
        this.drawerContent.set(drawerContent);
    }

    // drawer title extra

    private final ObjectProperty<Node> drawerTitleExtra = new SimpleObjectProperty<>(this, "drawerTitleExtra");

    public final Node getDrawerTitleExtra() {
        return drawerTitleExtra.get();
    }

    /**
     * An extra node that will be added to the title bar.
     *
     * @return an extra node that will be added to the title bar
     */
    public final ObjectProperty<Node> drawerTitleExtraProperty() {
        return drawerTitleExtra;
    }

    public final void setDrawerTitleExtra(Node drawerTitleExtra) {
        this.drawerTitleExtra.set(drawerTitleExtra);
    }

    private String createPreferenceKey() {
        return getPreferencesKey() + ".drawer.height";
    }

    // show drawer support

    private final BooleanProperty showDrawer = new SimpleBooleanProperty(this, "showDrawer", false);

    public boolean isShowDrawer() {
        return showDrawer.get();
    }

    /**
     * A flag used to control whether the drawer should show itself or not.
     *
     * @return true if the drawer should show itself
     */
    public final BooleanProperty showDrawerProperty() {
        return showDrawer;
    }

    public final void setShowDrawer(boolean showDrawer) {
        this.showDrawer.set(showDrawer);
    }

    // preferred drawer width support

    private final DoubleProperty preferredDrawerWidth = new SimpleDoubleProperty(this, "preferredDrawerWidth", MAXIMIZE);

    public final double getPreferredDrawerWidth() {
        return preferredDrawerWidth.get();
    }

    /**
     * Stores the preferred width of the drawer. Normally this value is equal to -1, which indicates that the
     * drawer should use the entire available width. A value larger than -1 will make the pane use that value
     * for the width of the drawer.
     *
     * @return the preferred drawer width
     */
    public final DoubleProperty preferredDrawerWidthProperty() {
        return preferredDrawerWidth;
    }

    public final void setPreferredDrawerWidth(double preferredDrawerWidth) {
        this.preferredDrawerWidth.set(preferredDrawerWidth);
    }

    private final DoubleProperty topPadding = new SimpleDoubleProperty(this, "topPadding", 20);

    public final double getTopPadding() {
        return topPadding.get();
    }

    /**
     * Specifies a value used for padding at the top of the drawer. This value will
     * always be enforced,.
     *
     * @return the padding used for the top of the drawer
     */
    public final DoubleProperty topPaddingProperty() {
        return topPadding;
    }

    public final void setTopPadding(double topPadding) {
        this.topPadding.set(topPadding);
    }

    private final DoubleProperty sidePadding = new SimpleDoubleProperty(this, "sidePadding", 100);

    public final double getSidePadding() {
        return sidePadding.get();
    }

    /**
     * Specifies a value used for padding to the left and the right of the drawer. This value will
     * always be enforced, not matter what the preferred width of the drawer content is.
     *
     * @return the padding used for the left and right side next to the drawer
     */
    public final DoubleProperty sidePaddingProperty() {
        return sidePadding;
    }

    public final void setSidePadding(double sidePadding) {
        this.sidePadding.set(sidePadding);
    }

    // drawer animation support

    private final BooleanProperty animateDrawer = new SimpleBooleanProperty(this, "animateDrawer", true);

    public final boolean isAnimateDrawer() {
        return animateDrawer.get();
    }

    /**
     * Determines whether the drawer will smoothly slide in / out when the
     * user opens / closes it. If not then the drawer will just appear instantly.
     *
     * @return true if the drawer will be animated (slide in / out)
     */
    public final BooleanProperty animateDrawerProperty() {
        return animateDrawer;
    }

    public final void setAnimateDrawer(boolean animateDrawer) {
        this.animateDrawer.set(animateDrawer);
    }

    // drawer height support

    private final DoubleProperty drawerHeight = new SimpleDoubleProperty(this, "drawerHeight");

    public final double getDrawerHeight() {
        return Math.min(1, Math.max(0, drawerHeight.get()));
    }

    /**
     * Stores the current height of the drawer as a value between 0 (not showing) and 1 (using full height of stackpane).
     *
     * @return the drawer height (value between 0 and 1)
     */
    public final DoubleProperty drawerHeightProperty() {
        return drawerHeight;
    }

    public final void setDrawerHeight(double drawerHeight) {
        this.drawerHeight.set(drawerHeight);
    }

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this, "animationDuration", Duration.millis(250));

    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * The duration it takes to show / hide the drawer.
     *
     * @return the animation duration
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public final void setAnimationDuration(Duration animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    private void showDrawer() {
        glassPane.toFront();

        drawer.setCache(true);
        drawer.setCacheHint(CacheHint.SPEED);
        drawer.toFront();
        drawer.setVisible(true);

        if (isAnimateDrawer() && getAnimationDuration() != null && getAnimationDuration().greaterThan(Duration.ZERO)) {
            setDrawerHeight(0);
            KeyValue keyValue = new KeyValue(drawerHeightProperty(), loadDrawerHeightFromUserPreferences(), Interpolator.EASE_BOTH);
            KeyFrame keyFrame = new KeyFrame(getAnimationDuration(), keyValue);
            timeline = new Timeline(keyFrame);
            timeline.setOnFinished(evt -> drawer.setCache(false));
            timeline.play();
        } else {
            setDrawerHeight(loadDrawerHeightFromUserPreferences());
        }
    }

    private void stopCurrentlyRunningTimeline() {
        if (timeline != null && timeline.getStatus().equals(Animation.Status.RUNNING)) {
            // the timeline used for the last show / hide might still be running
            timeline.stop();
        }
    }

    private double loadDrawerHeightFromUserPreferences() {
        try {
            double height = Preferences.userNodeForPackage(DrawerStackPane.class).getDouble(createPreferenceKey(), .7);
            return Math.min(1, Math.max(.1, height));
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, "problem encountered when trying to load drawer height from user preferences", ex);
        }

        return .9;
    }

    private void saveDrawerHeightToUserPreferences() {
        if (getPreferencesKey() != null) {
            try {
                Preferences.userNodeForPackage(DrawerStackPane.class).putDouble(createPreferenceKey(), getDrawerHeight());
            } catch (SecurityException ex) {
                LOG.log(Level.SEVERE, "problem encountered when trying to save drawer height in user preferences", ex);
            }
        }
    }

    private void hideDrawer() {
        if (isAnimateDrawer()) {
            KeyValue keyValue = new KeyValue(drawerHeightProperty(), 0, Interpolator.EASE_BOTH);
            KeyFrame keyFrame = new KeyFrame(getAnimationDuration(), keyValue);
            timeline = new Timeline(keyFrame);
            timeline.setOnFinished(evt -> postHiding());
            timeline.play();
        } else {
            setDrawerHeight(0);
            postHiding();
        }
    }

    private void postHiding() {
        drawer.setVisible(false);
        Runnable onClose = getOnDrawerClose();
        if (onClose != null) {
            onClose.run();
        }
    }
}
