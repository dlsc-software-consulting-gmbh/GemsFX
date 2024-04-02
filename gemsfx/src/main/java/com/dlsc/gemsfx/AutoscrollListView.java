package com.dlsc.gemsfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

/**
 * A specialization of {@link ListView} that will automatically start to scroll
 * up or down during drag and drop operations whenever the mouse cursor reaches
 * the top or bottom of the view. The view currently only supports vertical orientation,
 * although it should be straight-forward to add horizontal orientation as well.
 *
 * @param <T> the item type
 */
public class AutoscrollListView<T> extends ListView<T> {

    final double proximity = 20;

    /**
     * Constructs a new list view.
     */
    public AutoscrollListView() {
        this(FXCollections.observableArrayList());
    }

    /**
     * Constructs a new list view.
     *
     * @param items the list of items to show
     */
    public AutoscrollListView(ObservableList<T> items) {
        super(items);

        // start scrolling when ...
        addEventFilter(DragEvent.DRAG_OVER, this::autoscrollIfNeeded);

        // stop scrolling when ...
        addEventFilter(DragEvent.DRAG_EXITED, this::stopAutoScrollIfNeeded);
        addEventFilter(DragEvent.DRAG_DROPPED, this::stopAutoScrollIfNeeded);
        addEventFilter(DragEvent.DRAG_DONE, this::stopAutoScrollIfNeeded);
    }

    private void autoscrollIfNeeded(DragEvent evt) {
        evt.acceptTransferModes(TransferMode.ANY);

        /*
         * Determine the "hot" region that will trigger automatic scrolling.
         * Ideally we use the clipped container of the list view skin but when
         * the rows are empty the dimensions of the clipped container will be
         * 0x0. In this case we try to use the virtual flow.
         */
        Region hotRegion = getClippedContainer();
        if (hotRegion == null) {
            return;
        }

        if (hotRegion.getBoundsInLocal().getWidth() < 1) {
            hotRegion = this;
            if (hotRegion.getBoundsInLocal().getWidth() < 1) {
                stopAutoScrollIfNeeded(evt);
                return;
            }
        }

        double yOffset = 0;

        // y offset

        double delta = evt.getSceneY() - hotRegion.localToScene(0, 0).getY();
        if (delta < proximity) {
            yOffset = -(proximity - delta);
        }

        delta = hotRegion.localToScene(0, 0).getY() + hotRegion.getHeight() - evt.getSceneY();
        if (delta < proximity) {
            yOffset = proximity - delta;
        }

        if (yOffset != 0) {
            autoscroll(yOffset);
        } else {
            stopAutoScrollIfNeeded(evt);
        }
    }

    private VirtualFlow<?> getVirtualFlow() {
        return (VirtualFlow<?>) lookup("VirtualFlow");
    }

    private Region getClippedContainer() {

        /*
         * Safest way to find the clipped container. lookup() does not work at
         * all.
         */
        for (Node child : getVirtualFlow().getChildrenUnmodifiable()) {
            if (child.getStyleClass().contains("clipped-container")) {
                return (Region) child;
            }
        }

        return null;
    }

    private class ScrollThread extends Thread {

        private boolean running = true;
        private double yOffset;

        ScrollThread() {
            super("Auto-Scrolling List View");
            setDaemon(true);
        }

        @Override
        public void run() {

            /*
             * Some initial delay, especially useful when
             * dragging something in from the outside.
             */

            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            while (running) {

                Platform.runLater(this::scrollY);

                try {
                    sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void scrollY() {
            VirtualFlow<?> flow = getVirtualFlow();
            flow.scrollPixels(yOffset);
        }

        public void stopRunning() {
            this.running = false;
        }

        public void setDelta(double yOffset) {
            this.yOffset = yOffset;
        }
    }

    private ScrollThread scrollThread;

    private void autoscroll(double yOffset) {
        if (scrollThread == null) {
            scrollThread = new ScrollThread();
            scrollThread.start();
        }

        scrollThread.setDelta(yOffset);
    }

    private void stopAutoScrollIfNeeded(DragEvent evt) {
        if (scrollThread != null) {
            scrollThread.stopRunning();
            scrollThread = null;
        }
    }
}