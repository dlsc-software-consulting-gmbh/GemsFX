package com.dlsc.gemsfx.incubator;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class GemScrollPane extends ScrollPane {

    private Timeline timeline;

    public GemScrollPane() {
        init();
    }

    public GemScrollPane(Node content) {
        super(content);
        init();
    }

    private void init() {
//        visibleNodesProperty().addListener((Observable it) -> {
//            System.out.println("--------------");
//            getVisibleNodes().forEach(System.out::println);
//        });
//
//        final InvalidationListener updateListener = it -> {
//                updateVisibleNodes();
//        };
//        vvalueProperty().addListener(updateListener);
//        hvalueProperty().addListener(updateListener);
    }

    private final ObjectProperty<Duration> scrollToDuration = new SimpleObjectProperty<>(this, "scrollToDuration", Duration.millis(333));

    public final Duration getScrollToDuration() {
        return scrollToDuration.get();
    }

    public final ObjectProperty<Duration> scrollToDurationProperty() {
        return scrollToDuration;
    }

    public final void setScrollToDuration(Duration scrollToDuration) {
        this.scrollToDuration.set(scrollToDuration);
    }

    public void showNode(Node node, boolean animated) {
        double targetVValue = -1;
        double targetHValue = -1;

        Bounds nodeBounds = getContent().sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        if (getVbarPolicy() != ScrollBarPolicy.NEVER) {
            double minV = computeMinVerticalValue(nodeBounds);
            double maxV = computeMaxVerticalValue(nodeBounds);

            System.out.println("vv: " + getVvalue() + ", minV: " + minV + ", maxV: " + maxV);
            if (maxV < getVvalue()) {
                // node is above
                System.out.println("node is above, maxV = " + maxV + ", vv = " + getVvalue());
                System.out.println("scrolling to minV = " + minV);
                targetVValue = minV;
            } else if (minV > getVvalue()) {
                // node is below
                System.out.println("node is below, minV = " + minV + ", vv = " + getVvalue());
                System.out.println("scrolling to maxV = " + maxV);
                targetVValue = maxV;
            }
        }

        if (getHbarPolicy() != ScrollBarPolicy.NEVER) {
            double minH = computeMinHorizontalValue(nodeBounds);
            double maxH = computeMaxHorizontalValue(nodeBounds);

            if (maxH < getHvalue()) {
                // node is left
                targetHValue = minH;
            } else if (minH > getHvalue()) {
                // node is right
                targetHValue = maxH;
            }
        }

        if (animated && getScene() != null && isVisible()) {
            if (timeline != null && timeline.getStatus().equals(Animation.Status.RUNNING)) {
                timeline.stop();
            }

            timeline = new Timeline();

            KeyValue verticalValue = new KeyValue(vvalueProperty(), targetVValue);
            KeyValue horizontalValue = new KeyValue(hvalueProperty(), targetHValue);

            if (targetVValue != -1) {
                KeyFrame verticalFrame = new KeyFrame(getScrollToDuration(), verticalValue);
                timeline.getKeyFrames().add(verticalFrame);
            }

            if (targetHValue != -1) {
                KeyFrame horizontalFrame = new KeyFrame(getScrollToDuration(), horizontalValue);
                timeline.getKeyFrames().add(horizontalFrame);
            }

            timeline.play();

         } else {

            setVvalue(targetVValue);
            setHvalue(targetHValue);

        }
    }

    private double computeMinHorizontalValue(Bounds nodeBounds) {
        double width = getContent().getBoundsInLocal().getWidth();
        double minX = nodeBounds.getMinX();
        double minHValue = minX / width;
        return minHValue;
    }

    private double computeMaxHorizontalValue(Bounds nodeBounds) {
        double width = getContent().getBoundsInLocal().getWidth();
        double maxX = nodeBounds.getMaxX();
        double maxHValue = maxX / width;
        return maxHValue;
    }

    private double computeMinVerticalValue(Bounds nodeBounds) {
        double height = getContent().getBoundsInLocal().getHeight();
        double minY = nodeBounds.getMinY();
        double minVValue = minY / height;
        return minVValue;
    }

    private double computeMaxVerticalValue(Bounds nodeBounds) {
        Region viewport = (Region) lookup(".viewport");
        double viewportHeight = viewport.getHeight();
        double height = getContent().getBoundsInLocal().getHeight();
        double maxY = nodeBounds.getMaxY();
        return (maxY - viewportHeight) / height;
    }

    public void centerNode(Node node) {
        double h = getContent().getBoundsInLocal().getHeight();
        double y = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
        double v = getViewportBounds().getHeight();
        setVvalue(getVmax() * ((y - 0.5 * v) / (h - v)));
    }

    public void topNode(Node node) {
        double h = getContent().getBoundsInLocal().getHeight();
        double y = node.getBoundsInParent().getMinY();
        setVvalue(getVmax() * (y / h));
    }

    public void bottomNode(Node node) {
        double h = getContent().getBoundsInLocal().getHeight();
        double y = node.getBoundsInParent().getMaxY();
        double v = getViewportBounds().getHeight();
        setVvalue(getVmax() * ((y - v) / (h - v)));
    }

    private final ListProperty<Node> visibleNodes = new SimpleListProperty<>(this, "visibleNodes", FXCollections.observableArrayList());

    public ObservableList<Node> getVisibleNodes() {
        return visibleNodes.get();
    }

    public ListProperty<Node> visibleNodesProperty() {
        return visibleNodes;
    }

    public void setVisibleNodes(ObservableList<Node> visibleNodes) {
        this.visibleNodes.set(visibleNodes);
    }

    private void updateVisibleNodes() {
        System.out.println("--------------");
        List<Node> visibleNodes = new ArrayList<>();
        Node viewport = lookup(".viewport");
        if (viewport != null) {
            Bounds viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
            if (getContent() instanceof Parent) {
                for (Node n : ((Parent) getContent()).getChildrenUnmodifiable()) {
                    Bounds nodeBounds = n.localToScene(n.getBoundsInLocal());
                    System.out.println("ch: " + n);
                    System.out.println("vb: " + viewportBounds);
                    System.out.println("nb: " + nodeBounds);
                    if (viewportBounds.contains(nodeBounds)) {
                        visibleNodes.add(n);
                    }
                }
            }
        }

        getVisibleNodes().setAll(visibleNodes);
    }
}
