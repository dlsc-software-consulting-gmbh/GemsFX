package com.dlsc.gemsfx;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class DrawerStackPane extends StackPane {

    private static final Logger LOG = Logger.getLogger(DrawerStackPane.class.getName());

    private String preferenceKey = "drawer.stackpane";

    private StackPane glassPane;

    private VBox drawer;

    private double startY;

    private HBox headerBox;

    public DrawerStackPane(Node... nodes) {
        super(nodes);
        init();
    }

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

        glassPane = new GlassPane();

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

        headerBox.setOnMousePressed(evt -> {
            startY = evt.getScreenY();
        });

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
            if (isShowDrawer()) {
                showDrawer();
            } else {
                hideDrawer();
            }
        });

        drawerHeightProperty().addListener(it -> requestLayout());

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

        addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (isShowDrawer() && evt.getCode().equals(KeyCode.ESCAPE)) {
                setShowDrawer(false);
                evt.consume();
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return DrawerStackPane.class.getResource("drawer-stackpane.css").toExternalForm();
    }

    private final BooleanProperty fadeInOut = new SimpleBooleanProperty(this, "fadeInOut", true);

    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    public final void setFadeInOut(boolean animate) {
        this.fadeInOut.set(animate);
    }

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

    class GlassPane extends StackPane {

        public GlassPane() {
            getStyleClass().add("glass-pane");

            setVisible(false);
            setMouseTransparent(false);
            setOnMouseClicked(evt -> {
                if (isAutoHide() && evt.getButton().equals(MouseButton.PRIMARY) && !evt.isConsumed()) {
                    setShowDrawer(false);
                }
            });

            hide.bind(showDrawer.not());

            hide.addListener(it -> {

                if (isFadeInOut()) {
                    setVisible(true);

                    FadeTransition fadeTransition = new FadeTransition();
                    fadeTransition.setDuration(Duration.millis(200));
                    fadeTransition.setNode(this);
                    fadeTransition.setFromValue(isHide() ? .5 : 0);
                    fadeTransition.setToValue(isHide() ? 0 : .5);
                    fadeTransition.setOnFinished(evt -> {
                        if (isHide()) {
                            setVisible(false);
                        }
                    });
                    fadeTransition.play();
                } else {
                    setOpacity(isHide() ? 0 : .5);
                    setVisible(!isHide());
                }
            });
        }

        private final BooleanProperty hide = new SimpleBooleanProperty(this, "hide");

        public final boolean isHide() {
            return hide.get();
        }
    }

    public void setPreferencesKey(String id) {
        this.preferenceKey = id;
    }

    public String getPreferencesKey() {
        return preferenceKey;
    }

    private final DoubleProperty maxDrawerHeight = new SimpleDoubleProperty(this, "maxDrawerHeight", 1);

    public double getMaxDrawerHeight() {
        return maxDrawerHeight.get();
    }

    public DoubleProperty maxDrawerHeightProperty() {
        return maxDrawerHeight;
    }

    public void setMaxDrawerHeight(double maxDrawerHeight) {
        this.maxDrawerHeight.set(maxDrawerHeight);
    }

    private final DoubleProperty minDrawerHeight = new SimpleDoubleProperty(this, "minDrawerHeight", .1);

    public double getMinDrawerHeight() {
        return minDrawerHeight.get();
    }

    public DoubleProperty minDrawerHeightProperty() {
        return minDrawerHeight;
    }

    public void setMinDrawerHeight(double minDrawerHeight) {
        this.minDrawerHeight.set(minDrawerHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        final double availableHeight = getHeight() - 20;
        final double maxDrawerWidth = getWidth() - 100;
        final double drawerWidth = getPreferredDrawerWidth() != -1 ? Math.min(getPreferredDrawerWidth(), maxDrawerWidth) : maxDrawerWidth;

        drawer.resizeRelocate((getWidth() - drawerWidth) / 2, getHeight() - getDrawerHeight() * availableHeight, drawerWidth, availableHeight * getDrawerHeight());
    }

    private VBox createContainer() {
        StackPane dragHandle = createDragHandle();
        Button closeButton = createCloseButton();
        Label titleLabel = createTitleLabel();

        titleLabel.setMouseTransparent(true);

        StackPane.setAlignment(dragHandle, Pos.CENTER);

        headerBox = new HBox(titleLabel, closeButton);
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

    private Button createCloseButton() {
        Button closeButton = new Button("Close");
        closeButton.setOnAction(evt -> setShowDrawer(false));
        closeButton.getStyleClass().add("close-button");

        return closeButton;
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

    public Runnable getOnDrawerClose() {
        return onDrawerClose.get();
    }

    public ObjectProperty<Runnable> onDrawerCloseProperty() {
        return onDrawerClose;
    }

    public void setOnDrawerClose(Runnable onDrawerClose) {
        this.onDrawerClose.set(onDrawerClose);
    }

    private final BooleanProperty showDrawerTitle = new SimpleBooleanProperty(this, "showDrawerTitle", false);

    public boolean isShowDrawerTitle() {
        return showDrawerTitle.get();
    }

    public BooleanProperty showDrawerTitleProperty() {
        return showDrawerTitle;
    }

    public void setShowDrawerTitle(boolean showDrawerTitle) {
        this.showDrawerTitle.set(showDrawerTitle);
    }

    private final StringProperty drawerTitle = new SimpleStringProperty(this, "drawerTitle", "Untitled");

    public String getDrawerTitle() {
        return drawerTitle.get();
    }

    public StringProperty drawerTitleProperty() {
        return drawerTitle;
    }

    public void setDrawerTitle(String drawerTitle) {
        this.drawerTitle.set(drawerTitle);
    }

    private final ObjectProperty<Node> drawerContent = new SimpleObjectProperty<>(this, "drawerContent");

    public Node getDrawerContent() {
        return drawerContent.get();
    }

    public ObjectProperty<Node> drawerContentProperty() {
        return drawerContent;
    }

    public void setDrawerContent(Node drawerContent) {
        this.drawerContent.set(drawerContent);
    }

    private final ObjectProperty<Node> drawerTitleExtra = new SimpleObjectProperty<>(this, "drawerTitleExtra");

    public Node getDrawerTitleExtra() {
        return drawerTitleExtra.get();
    }

    public ObjectProperty<Node> drawerTitleExtraProperty() {
        return drawerTitleExtra;
    }

    public void setDrawerTitleExtra(Node drawerTitleExtra) {
        this.drawerTitleExtra.set(drawerTitleExtra);
    }

    private String createPreferenceKey() {
        return getPreferencesKey() + ".drawer.height";
    }

    private final BooleanProperty showDrawer = new SimpleBooleanProperty(this, "showDrawer", false);

    public boolean isShowDrawer() {
        return showDrawer.get();
    }

    public BooleanProperty showDrawerProperty() {
        return showDrawer;
    }

    public void setShowDrawer(boolean showDrawer) {
        this.showDrawer.set(showDrawer);
    }

    private final DoubleProperty preferredDrawerWidth = new SimpleDoubleProperty(this, "preferredDrawerWidth", -1);

    public double getPreferredDrawerWidth() {
        return preferredDrawerWidth.get();
    }

    public DoubleProperty preferredDrawerWidthProperty() {
        return preferredDrawerWidth;
    }

    public void setPreferredDrawerWidth(double preferredDrawerWidth) {
        this.preferredDrawerWidth.set(preferredDrawerWidth);
    }

    private final DoubleProperty drawerHeight = new SimpleDoubleProperty(this, "drawerHeight");

    public double getDrawerHeight() {
        return Math.min(1, Math.max(.1, drawerHeight.get()));
    }

    public DoubleProperty drawerHeightProperty() {
        return drawerHeight;
    }

    public void setDrawerHeight(double drawerHeight) {
        this.drawerHeight.set(drawerHeight);
    }

    private void showDrawer() {
        glassPane.toFront();

        drawer.setCache(true);
        drawer.setCacheHint(CacheHint.SPEED);
        drawer.toFront();
        drawer.setVisible(true);

        if (isAnimateDrawer()) {
            setDrawerHeight(0);
            KeyValue keyValue = new KeyValue(drawerHeightProperty(), loadDrawerHeightFromUserPreferences());
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100), keyValue);
            Timeline timeline = new Timeline(keyFrame);
            timeline.setOnFinished(evt -> drawer.setCache(false));
            timeline.play();
        } else {
            setDrawerHeight(loadDrawerHeightFromUserPreferences());
        }
    }

    private double loadDrawerHeightFromUserPreferences() {
        try {
            double height = Preferences.userNodeForPackage(DrawerStackPane.class).getDouble(createPreferenceKey(), .7);
            return Math.min(1, Math.max(.1, height));
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, "problem encountered when trying to load drawer height from user preferences", ex);
            ex.printStackTrace();
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
            KeyValue keyValue = new KeyValue(drawerHeightProperty(), 0);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(100), keyValue);
            Timeline timeline = new Timeline(keyFrame);
            timeline.setOnFinished(evt -> postHiding());
            timeline.play();
        } else {
            setDrawerHeight(0);
            postHiding();
        }
    }

    private void postHiding() {
        drawer.setVisible(false);
        final Runnable onClose = getOnDrawerClose();
        if (onClose != null) {
            onClose.run();
        }
    }

    private final BooleanProperty animateDrawer = new SimpleBooleanProperty(this, "animateDrawer", true);

    public boolean isAnimateDrawer() {
        return animateDrawer.get();
    }

    public BooleanProperty animateDrawerProperty() {
        return animateDrawer;
    }

    public void setAnimateDrawer(boolean animateDrawer) {
        this.animateDrawer.set(animateDrawer);
    }
}
