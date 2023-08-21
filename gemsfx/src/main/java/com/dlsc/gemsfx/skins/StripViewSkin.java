package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.MaskedView;
import com.dlsc.gemsfx.StripView;
import com.dlsc.gemsfx.StripView.StripCell;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StripViewSkin<T> extends SkinBase<StripView<T>> {

    private static final String SCROLL_TO_KEY = "scroll.to";

    private final HBox content;
    private final Region leftBtn;
    private final Region rightBtn;

    private final Map<T, Node> nodeMap = new HashMap<>();

    private final MaskedView maskedView;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param strip The strip for which this Skin should attach to.
     */
    public StripViewSkin(StripView<T> strip) {
        super(strip);

        content = new HBox();
        content.getStyleClass().add("container");
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane.setAlignment(content, Pos.CENTER_LEFT);

        maskedView = new MaskedView(content);
        maskedView.fadingSizeProperty().bind(strip.fadingSizeProperty());

        leftBtn = new Region();
        leftBtn.getStyleClass().addAll("scroller", "left");
        leftBtn.setOpacity(0);

        rightBtn = new Region();
        rightBtn.getStyleClass().addAll("scroller", "right");
        rightBtn.setOpacity(0);

        getChildren().addAll(maskedView, leftBtn, rightBtn);
        getChildren().forEach(child -> child.setManaged(false));

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(strip.widthProperty());
        clip.heightProperty().bind(strip.heightProperty());
        getSkinnable().setClip(clip);

        setupListeners();
        setupBindings();
        setupEventHandlers();

        strip.itemsProperty().addListener((Observable it) -> buildContent());
        buildContent();

        strip.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        StripView<T> strip = getSkinnable();

        List<T> itemsList = strip.getItems();
        T currentSelectedItem = strip.getSelectedItem();
        int index = itemsList.indexOf(currentSelectedItem);
        int itemCount = itemsList.size();

        switch (event.getCode()) {
            case RIGHT:
            case ENTER:
                // Check if loop selection is enabled or if we haven't reached the last item yet
                if (strip.isLoopSelection() || index < itemCount - 1) {
                    // Calculate the next index. If loop selection is off, due to the above check,
                    // this won't exceed the bounds.
                    index = (index + 1) % itemCount;
                    strip.setSelectedItem(itemsList.get(index));
                    event.consume();
                }
                break;
            case LEFT:
                // Check if loop selection is enabled or if we haven't reached the first item yet
                if (strip.isLoopSelection() || index > 0) {
                    // Calculate the previous index. If loop selection is off, due to the above check,
                    // this won't go negative.
                    index = (index - 1 + itemCount) % itemCount;
                    strip.setSelectedItem(itemsList.get(index));
                    event.consume();
                }
                break;
            case TAB:
                // If it's the last item and loop selection is off, don't consume the event
                // so that focus can move to the next focusable component.
                // Otherwise, select the next item.
                if (index < itemCount - 1 || strip.isLoopSelection()) {
                    index = (index + 1) % itemCount;
                    strip.setSelectedItem(itemsList.get(index));
                    event.consume();
                }
                break;
            default:
                // For any other key press, do nothing.
                break;
        }
    }

    private void scrollTo(T item) {
        Node node = nodeMap.get(item);

        if (node != null) {
            StripView<T> strip = getSkinnable();

            strip.getProperties().remove(SCROLL_TO_KEY);

            Bounds nodeBounds = node.localToParent(node.getLayoutBounds());

            double x = -nodeBounds.getMinX() + strip.getWidth() / 2 - nodeBounds.getWidth() / 2;

            double x1 = -translateX.get();
            double x2 = x1 + strip.getLayoutBounds().getWidth();

            if (strip.isAlwaysCenter() || x1 > nodeBounds.getMinX() || x2 < nodeBounds.getMaxX()) {
                if (strip.isAnimateScrolling()) {
                    KeyValue keyValue = new KeyValue(translateX, x);
                    KeyFrame keyFrame = new KeyFrame(strip.getAnimationDuration(), keyValue);

                    Timeline timeline = new Timeline(keyFrame);
                    timeline.play();
                } else {
                    translateX.set(x);
                }
            }
        }
    }

    private void buildContent() {
        nodeMap.clear();
        StripView<T> strip = getSkinnable();
        content.getChildren().setAll(strip.getItems().stream().map(item -> {
            StripCell<T> cell = strip.getCellFactory().call(strip);
            nodeMap.put(item, cell);
            cell.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                if (!evt.isConsumed() && evt.getClickCount() == 1 && evt.getButton() == MouseButton.PRIMARY) {
                    cell.requestFocus();
                    boolean wasSelected = item == getSkinnable().getSelectedItem();
                    strip.setSelectedItem(item);
                    strip.scrollTo(item);
                    strip.requestLayout();
                    if (!wasSelected) {
                        evt.consume();
                    }
                }
            });
            cell.setStripView(strip);
            cell.setItem(item);
            return cell;
        }).collect(Collectors.toList()));

        strip.requestLayout();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return maskedView.prefHeight(width);
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return maskedView.minHeight(width);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return maskedView.maxHeight(width);
    }

    private void setupListeners() {
        translateX.addListener(it -> content.setTranslateX(translateX.get()));
        getSkinnable().widthProperty().addListener(it -> fixTranslate());
        translateX.addListener(it -> fixTranslate());

        showLeftScroll.addListener((it, oldShow, newShow) -> fadeSupport(newShow, leftBtn));
        showRightScroll.addListener((it, oldShow, newShow) -> fadeSupport(newShow, rightBtn));
    }

    private void fadeSupport(Boolean newShow, Region button) {
        if (getSkinnable().isAnimateScrolling()) {
            if (newShow) {
                createFadeTransition(button, 0, 1).play();
            } else {
                createFadeTransition(button, 1, 0).play();
            }
        } else {
            button.setOpacity(newShow ? 1 : 0);
        }
    }

    private FadeTransition createFadeTransition(Node node, double from, double to) {
        node.setOpacity(from);
        FadeTransition faderTransition = new FadeTransition();
        faderTransition.setNode(node);
        faderTransition.setFromValue(from);
        faderTransition.setToValue(to);
        faderTransition.setDuration(Duration.millis(200));
        return faderTransition;
    }

    private final BooleanProperty showLeftScroll = new SimpleBooleanProperty(false);

    private final BooleanProperty showRightScroll = new SimpleBooleanProperty(false);

    private void setupBindings() {
        showLeftScroll.bind(translateX.lessThan(0));

        BooleanBinding showRightBinding = Bindings.createBooleanBinding(() -> {
            StripView<T> strip = getSkinnable();
            double rightX = translateX.get() + content.getWidth();
            double stripWidth = strip.getWidth() - strip.getInsets().getLeft() - strip.getInsets().getRight();
            return rightX > stripWidth;
        }, translateX, content.widthProperty());

        showRightScroll.bind(showRightBinding);
    }

    private void setupEventHandlers() {
        leftBtn.setOnMouseClicked(event -> scroll(true));
        rightBtn.setOnMouseClicked(event -> scroll(false));

        getSkinnable().addEventFilter(ScrollEvent.SCROLL, evt -> translateX.set(translateX.get() + evt.getDeltaY()));
    }

    private void fixTranslate() {
        StripView<T> strip = getSkinnable();
        if (content.getWidth() < strip.getWidth()) {
            translateX.set(0);
        } else {
            double newValue = translateX.get();
            newValue = Math.min(newValue, 0);
            newValue = Math.max(newValue, -(content.getWidth() - (strip.getWidth() - strip.getInsets().getLeft() - strip.getInsets().getRight())));
            translateX.set(newValue);
        }
    }

    private Timeline timeline;
    private final DoubleProperty translateX = new SimpleDoubleProperty();

    private void scroll(boolean scrollToRight) {
        // In case of the timeline is already playing the animation must first finish.
        if (timeline != null && timeline.getStatus().equals(Animation.Status.RUNNING)) {
            timeline.stop();
        }

        double scrollDistance = getSkinnable().getWidth() / 2;
        double dist = scrollToRight ? scrollDistance : -scrollDistance;

        if (getSkinnable().isAnimateScrolling()) {
            KeyValue keyValue = new KeyValue(translateX, translateX.get() + dist, Interpolator.EASE_BOTH);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValue);

            timeline = new Timeline(keyFrame);
            timeline.play();
        } else {
            translateX.set(translateX.get() + dist);
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        maskedView.resizeRelocate(contentX, contentY, contentWidth, contentHeight);

        leftBtn.resizeRelocate(contentX, contentY + (contentHeight - leftBtn.prefHeight(-1)) / 2, leftBtn.prefWidth(-1), leftBtn.prefHeight(-1));
        rightBtn.resizeRelocate(contentX + contentWidth - rightBtn.prefWidth(-1), contentY + (contentHeight - rightBtn.prefHeight(-1)) / 2, rightBtn.prefWidth(-1), rightBtn.prefHeight(-1));

        @SuppressWarnings("unchecked")
        T item = (T) getSkinnable().getProperties().get(SCROLL_TO_KEY);
        if (item != null) {
            Platform.runLater(() -> scrollTo(item));
        }
    }
}
