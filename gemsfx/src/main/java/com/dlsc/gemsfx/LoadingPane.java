package com.dlsc.gemsfx;

import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Objects;

/**
 * A custom pane that supports the visual feedback for data loading / refreshing its content. The pane is a wrapper
 * around a given content node. This node will be hidden and a progress indicator shown instead when the status of
 * this pane changes to {@link Status#LOADING}. Once the status changes back to {@link Status#OK} the node will be
 * shown again. If anything goes wrong while loading new data or refreshing the content a third state called
 * {@link Status#ERROR} can be applied resulting in an error icon and error text being shown. The pane also supports
 * a {@link #progressProperty()} for detailed progress feedback.
 */
@DefaultProperty("content")
public class LoadingPane extends StackPane {

    /**
     * The possible states that the loading pane can be in.
     *
     * @see #statusProperty()
     */
    public enum Status {

        /**
         * The pane will show the wrapped node.
         */
        OK,

        /**
         * The pane will show the progress indicator.
         */
        LOADING,

        /**
         * The pane will show the error message.
         */
        ERROR
    }

    /**
     * The possible sizes for the pane. Sizes will also be reflected by matching pseudo-classes.
     *
     * @see #sizeProperty()
     */
    public enum Size {
        SMALL,
        MEDIUM,
        LARGE
    }

    private CommitStatusThread commitStatusThread;

    private final ObjectProperty<Status> committedStatus = new SimpleObjectProperty<>(this, "committedStatus", Status.OK);

    /**
     * Constructs a new loading pane.
     */
    public LoadingPane() {
        getStyleClass().add("loading-pane");

        CircleProgressIndicator indicator = new CircleProgressIndicator();
        indicator.getStyleClass().add("busy-indicator");
        setProgressIndicator(indicator);

        // error label
        Region icon = new Region();
        icon.getStyleClass().add("icon");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setGraphic(icon);
        errorLabel.textProperty().bind(errorProperty());

        setErrorNode(errorLabel);

        progressProperty().addListener(it -> {
            if (getProgress() == 1.0) {
                setStatus(Status.OK);
            }
        });

        commitDelay.addListener(it -> {
            if (getCommitDelay() < 0) {
                throw new IllegalArgumentException("commit delay must be greater than or equal to zero");
            }
        });

        /*
         * Delay the actual new status so that operations returning quickly do not create
         * a flicker in the UI.
         */
        statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (commitStatusThread != null) {
                commitStatusThread.abort();
            }

            if (newStatus != null) {
                commitStatusThread = new CommitStatusThread(newStatus);
                commitStatusThread.start();
            }
        });

        InvalidationListener updateViewListener = it -> updateView();
        progressIndicator.addListener(updateViewListener);
        errorNodeProperty().addListener(updateViewListener);
        updateView();

        updatePseudoClass(null, committedStatus.get());
        updatePseudoClass(null, getSize());
    }

    /**
     * Constructs a new loading pane.
     *
     * @param node the wrapped node
     */
    public LoadingPane(Node node) {
        this();
        setContent(node);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(LoadingPane.class.getResource("loading-pane.css")).toExternalForm();
    }

    private void updateView() {
        ProgressIndicator indicator = getProgressIndicator();

        if (indicator == null) {
            throw new IllegalArgumentException("progress indicator can not be null");
        }

        indicator.progressProperty().bindBidirectional(progressProperty());

        getChildren().clear();

        toggleIndicator();
        committedStatus.addListener(it -> toggleIndicator());

        StackPane indicatorWrapper = new StackPane(indicator);
        indicatorWrapper.getStyleClass().add("progress-indicator-wrapper");
        indicatorWrapper.visibleProperty().bind(committedStatus.isEqualTo(Status.LOADING));

        // make sure progress indicator thread stops
        indicator.visibleProperty().bind(indicatorWrapper.visibleProperty());

        contentProperty().addListener((obs, oldNode, newNode) -> {
            if (oldNode != null) {
                oldNode.visibleProperty().unbind();
            }

            if (newNode != null) {
                newNode.visibleProperty().bind(committedStatus.isEqualTo(Status.OK));
                getChildren().setAll(newNode, indicatorWrapper, wrapErrorNode(getErrorNode()));
            } else {
                getChildren().setAll(indicatorWrapper, wrapErrorNode(getErrorNode()));
            }
        });

        committedStatus.addListener((obs, oldStatus, newStatus) -> updatePseudoClass(oldStatus, newStatus));
        sizeProperty().addListener((obs, oldSize, newSize) -> updatePseudoClass(oldSize, newSize));
    }

    private Node wrapErrorNode(Node errorNode) {
        StackPane errorWrapper = new StackPane(errorNode);
        errorWrapper.getStyleClass().add("error-pane");
        errorWrapper.visibleProperty().bind(committedStatus.isEqualTo(Status.ERROR));
        return errorWrapper;
    }

    private final ObjectProperty<Node> errorNode = new SimpleObjectProperty<>(this, "errorNode");

    public final Node getErrorNode() {
        return errorNode.get();
    }

    /**
     * The node that will be shown when the loading pane is in status {@link Status#ERROR}. The default node
     * for this is a simple label which binds to the {@link #errorProperty()}.
     *
     * @return the error node
     */
    public final ObjectProperty<Node> errorNodeProperty() {
        return errorNode;
    }

    public final void setErrorNode(Node errorNode) {
        this.errorNode.set(errorNode);
    }

    private final ObjectProperty<ProgressIndicator> progressIndicator = new SimpleObjectProperty<>(this, "progressIndicator");

    public final ProgressIndicator getProgressIndicator() {
        return progressIndicator.get();
    }

    /**
     * The progress indicator that will be used to display percentage progress or the indeterminate state of the
     * loading progress.
     *
     * @return the progress indicator
     */
    public final ObjectProperty<ProgressIndicator> progressIndicatorProperty() {
        return progressIndicator;
    }

    public final void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator.set(progressIndicator);
    }

    /**
     * A thread used to delay the actual status change. Delaying is important so that a fast loading process
     * does not cause a quick flicker inside the UI.
     */
    private class CommitStatusThread extends Thread {

        private final Status status;
        private boolean stopped;

        CommitStatusThread(Status status) {
            this.status = status;
        }

        @Override
        public void run() {
            try {
                if (status.equals(Status.LOADING)) {
                    // only delay the change when switching to the state that represents that loading is currently ongoing.
                    Thread.sleep(getCommitDelay());
                }
                if (!stopped) {
                    Platform.runLater(() -> committedStatus.set(status));
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        public void abort() {
            stopped = true;
        }
    }

    private final LongProperty commitDelay = new SimpleLongProperty(this, "commitDelay", 200L);

    public final long getCommitDelay() {
        return commitDelay.get();
    }

    /**
     * The commit delay duration that will be applied before the control does change to a new state. The default is
     * 200 milliseconds.
     *
     * @return the delay duration in milliseconds
     */
    public final LongProperty commitDelayProperty() {
        return commitDelay;
    }

    public final void setCommitDelay(long commitDelay) {
        this.commitDelay.set(commitDelay);
    }

    private void toggleIndicator() {
        if (Status.LOADING.equals(committedStatus.get())) {
            setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        } else {
            setProgress(0);
        }
    }

    private void updatePseudoClass(Enum<?> oldValue, Enum<?> newValue) {
        if (oldValue != null) {
            pseudoClassStateChanged(PseudoClass.getPseudoClass(oldValue.name().toLowerCase()), false);
        }

        if (newValue != null) {
            pseudoClassStateChanged(PseudoClass.getPseudoClass(newValue.name().toLowerCase()), true);
        }
    }

    private final StringProperty error = new SimpleStringProperty(this, "error");

    /**
     * The error text that will be shown if the pane is in status {@link Status#ERROR}.
     *
     * @return the error message
     */
    public final StringProperty errorProperty() {
        return error;
    }

    public final String getError() {
        return error.get();
    }

    public final void setError(String error) {
        this.error.set(error);
    }

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    /**
     * The wrapped content node.
     *
     * @return the node / the view wrapped by the loading pane, e.g. a list or a table view
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status", Status.OK);

    /**
     * The current status of the loading pane.
     *
     * @see Status
     *
     * @return the status of the loading pane
     */
    public final ObjectProperty<Status> statusProperty() {
        return status;
    }

    public final Status getStatus() {
        return status.get();
    }

    public final void setStatus(Status status) {
        this.status.set(status);
    }

    private final ObjectProperty<Size> size = new SimpleObjectProperty<>(this, "size", Size.MEDIUM);

    /**
     * The size of the progress indicator. The size should match the size of the wrapped node.
     *
     * @return the size of the progress indicator
     */
    public final ObjectProperty<Size> sizeProperty() {
        return size;
    }

    public final Size getSize() {
        return size.get();
    }

    public final void setSize(Size size) {
        this.size.set(size);
    }

    private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);

    public final double getProgress() {
        return progress.get();
    }

    public final void setProgress(double progress) {
        this.progress.set(progress);
    }

    /**
     * The progress (value between 0 and 1).
     *
     * @return the progress of the loading process
     */
    public final DoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Convenience method to change the status of the pane to {@link Status#LOADING}. Automatically clears
     * the error text. This method is thread-safe.
     */
    public final void load() {
        Platform.runLater(() -> {
            setStatus(Status.LOADING);
            setError(null);
        });
    }

    /**
     * Convenience method to change the status of the pane to {@link Status#OK}. Automatically clears
     * the error text. This method is thread-safe.
     */
    public final void ok() {
        Platform.runLater(() -> {
            setStatus(Status.OK);
            setError(null);
        });
    }

    /**
     * Convenience method to change the status of the pane to {@link Status#ERROR}. Also changes the error text to
     * the given value. This method is thread-safe.
     */
    public final void error(String message) {
        Platform.runLater(() -> {
            setStatus(Status.ERROR);
            setError(message);
        });
    }

    /**
     * Convenience method to change the status of the pane to {@link Status#ERROR}. Also changes the error text to
     * the message provided by the throwable. This method is thread-safe.
     */
    public final void error(Throwable ex) {
        Platform.runLater(() -> {
            setStatus(Status.ERROR);
            setError(ex.getMessage());
        });
    }

    /**
     * Convenience method to change the status of the pane to {@link Status#ERROR}. There will be no error message
     * shown. This method is thread-safe.
     */
    public final void error() {
        Platform.runLater(() -> error((String) null));
    }
}
