package com.dlsc.gemsfx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.skin.VirtualFlow;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility class for binding two virtual flows to each other so that they perform
 * vertical scrolling in sync.
 */
public class VirtualFlowUtil {

    /**
     * Bind the virtual flows found somewhere inside the two given controls to each other
     * so that vertical scrolling will be performed in sync.
     *
     * @param control1 the first control
     * @param control2 the second control
     */
    public static void bindVirtualFlows(Control control1, Control control2) {

        AtomicReference<InvalidationListener> skinListener = new AtomicReference<>();

        AtomicBoolean isBound = new AtomicBoolean(false);

        Runnable maybeBind = () -> {
            if (isBound.get()) {
                return;
            }

            VirtualFlow<?> leftFlow = (VirtualFlow) control1.lookup("VirtualFlow");
            VirtualFlow<?> rightFlow = (VirtualFlow) control2.lookup("VirtualFlow");

            if (leftFlow != null && rightFlow != null) {
                doRealBidirectionalBinding(leftFlow, rightFlow);
                control1.skinProperty().removeListener(skinListener.get());
                control2.skinProperty().removeListener(skinListener.get());
                isBound.set(true);
            }
        };

        skinListener.set(it -> maybeBind.run());

        control1.skinProperty().addListener(skinListener.get());
        control2.skinProperty().addListener(skinListener.get());

        maybeBind.run();
    }

    private static void doRealBidirectionalBinding(VirtualFlow<?> leftFlow, VirtualFlow<?> rightFlow) {
        AtomicBoolean isUpdating = new AtomicBoolean(false);
        doRealBinding(isUpdating, leftFlow, rightFlow);
        doRealBinding(isUpdating, rightFlow, leftFlow);
    }

    private static void doRealBinding(AtomicBoolean isUpdating, VirtualFlow<?> flow1, VirtualFlow<?> flow2) {
        AtomicReference<Cell> lastCell = new AtomicReference(null);

        Runnable doUpdate = () -> {
            if (isUpdating.get()) {
                return;
            }
            isUpdating.set(true);
            addPostLayoutAction(flow1.getScene(), () -> {
                try {
                    updatePosition(flow1, flow2);
                } finally {
                    isUpdating.set(false);
                }
            });
        };
        ChangeListener doUpdateListener = (obs, oldVal, newVal) -> {
            doUpdate.run();
        };

        Runnable updateCellListener = () -> {
            if (lastCell.get() != null) {
                lastCell.get().layoutYProperty().removeListener(doUpdateListener);
            }
            Cell newCell = flow1.getLastVisibleCell();

            if (newCell != null) {
                newCell.layoutYProperty().addListener(doUpdateListener);
            }
            lastCell.set(newCell);
        };

        flow1.positionProperty().addListener((obs, oldVal, newVal) -> {
            updateCellListener.run();
            doUpdate.run();
        });
    }

    private static void updatePosition(VirtualFlow<?> fromFlow, VirtualFlow<?> toFlow) {
        var pos2 = getVFlowPosition(fromFlow);
        setVFlowPosition(toFlow, pos2);
    }

    private static VirtualFlowPosition getVFlowPosition(VirtualFlow<?> flow) {
        flow.applyCss();
        flow.layout();

        IndexedCell cell = flow.getFirstVisibleCell();
        int index = cell.getIndex();
        double offset = -cell.getLayoutY();

        return new VirtualFlowPosition(index, offset);
    }

    private static void setVFlowPosition(VirtualFlow<?> flow, VirtualFlowPosition pos) {
        try {
            flow.scrollToTop(pos.index);
            flow.layout();
            flow.scrollPixels(pos.offset);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class VirtualFlowPosition {

        private int index;
        private double offset;

        public VirtualFlowPosition(int index, double offset) {
            this.index = index;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "VBosPosition{" + "index=" + index + ", offset=" + offset + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            VirtualFlowPosition that = (VirtualFlowPosition) o;
            return index == that.index && Double.compare(that.offset, offset) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, offset);
        }
    }

    private static void addPostLayoutAction(Scene scene, Runnable action) {
        AtomicReference<Runnable> listener = new AtomicReference<>();

        listener.set(() -> {
            scene.removePostLayoutPulseListener(listener.get());
            action.run();
        });

        scene.addPostLayoutPulseListener(listener.get());
    }
}
