package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.dialog.BusyIndicator;
import com.dlsc.gemsfx.skins.dialog.DialogContentPane;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class DialogPane extends Pane {

    private final GlassPane glassPane;

    private final ObservableList<DialogContentPane> dialogPanes = FXCollections.observableArrayList();

    private final Map<DialogContentPane, DoubleProperty> dialogVisibilityMap = new HashMap<>();

    private final ListProperty<Dialog> dialogs = new SimpleListProperty<>(this, "dialogs", FXCollections.observableArrayList());

    public DialogPane() {
        getStyleClass().add("dialog-pane");

        showingDialog.bind(dialogs.emptyProperty().not());

        mouseTransparentProperty().bind(showingDialogProperty().not());

        glassPane = new GlassPane();
        glassPane.fadeInOutDurationProperty().bind(animationDurationProperty());
        glassPane.hideProperty().bind(dialogs.emptyProperty());
        glassPane.fadeInOutProperty().bind(fadeInOutProperty());

        dialogs.addListener((ListChangeListener.Change<? extends Dialog> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {

                    change.getAddedSubList().forEach(dialog -> {
                        DialogContentPane dialogPane = new DialogContentPane(dialog);
                        dialogPane.blockedProperty().bind(Bindings.createBooleanBinding(() -> !dialogPanes.isEmpty() && dialogPanes.get(dialogPanes.size() - 1) != dialogPane, dialogPanes));
                        dialogPanes.add(dialogPane);

                        SimpleDoubleProperty visibility = new SimpleDoubleProperty();
                        visibility.addListener(it -> requestLayout());
                        dialogVisibilityMap.put(dialogPane, visibility);
                        getChildren().add(dialogPane);
                        slideInOut(1, visibility, () -> dialogPane);
                    });

                } else if (change.wasRemoved()) {

                    change.getRemoved().forEach(dialog -> {
                        Optional<DialogContentPane> dialogOptional = dialogPanes.stream().filter(d -> d.getDialog() == dialog).findFirst();
                        if (dialogOptional.isPresent()) {
                            DialogContentPane dialogPane = dialogOptional.get();
                            DoubleProperty visibility = dialogVisibilityMap.get(dialogPane);
                            slideInOut(0, visibility, () -> dialogPane);
                        }
                    });

                }
            }
        });

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        getChildren().add(glassPane);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
                    switch (evt.getCode()) {
                        case ESCAPE:
                            // hide the last dialog that was opened
                            if (!dialogs.isEmpty()) {
                                Dialog<?> dialog = dialogs.get(dialogs.size() - 1);
                                if (!dialog.isCancelled()) {
                                    dialog.cancel();
                                }
                            }
                            break;
                    }
                });
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return DialogPane.class.getResource("dialog.css").toExternalForm();
    }

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this, "animationDuration", Duration.millis(100));

    public Duration getAnimationDuration() {
        return animationDuration.get();
    }

    public ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public void setAnimationDuration(Duration animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    public final <T> void showDialog(Dialog<T> dialog) {
        dialogs.add(dialog);
    }

    public final void hideDialog(Dialog<?> dialog) {
        dialogs.remove(dialog);
    }

    private Dialog<ButtonType> doShowDialog(Type type, String title, String message) {
        return doShowDialog(type, title, message, Collections.emptyList());
    }

    private Dialog<ButtonType> doShowDialog(Type type, String title, String message, List<ButtonType> buttons) {
        return showNode(type, title, new Label(message), buttons);
    }

    public final Dialog<Void> showError(String title, String message) {
        return showError(title, message, null, null);
    }

    public final Dialog<Void> showError(String title, String message, Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return showError(title, message, stringWriter.toString(), exception);
    }

    public final Dialog<Void> showError(String title, Exception exception) {
        return showError(title, exception.getMessage(), exception);
    }

    public final Dialog<Void> showError(String title, String message, Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return showError(title, message, stringWriter.toString(), exception);
    }

    public final Dialog<Void> showError(String title, String message, String details) {
        return showError(title, message, details, null);
    }

    private Dialog<Void> showError(String title, String message, String details, Throwable exception) {
        Dialog<Void> dialog = new Dialog<>(this, Type.ERROR);
        dialog.setTitle(title);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);

        if (StringUtils.isBlank(details)) {
            dialog.setContent(messageLabel);
        } else {
            ResizableTextArea textArea = new ResizableTextArea();
            textArea.setText(details);
            textArea.getEditor().setWrapText(false);
            textArea.getEditor().setPrefColumnCount(80);
            textArea.setResizeHorizontal(true);
            textArea.setResizeVertical(true);
            textArea.getStyleClass().add("error-text-area");

            TitledPane titledPane = new TitledPane();
            titledPane.getStyleClass().add("error-details-titled-pane");
            titledPane.setText("Details");
            titledPane.setContent(textArea);
            titledPane.setPrefHeight(300);

            VBox content = new VBox(messageLabel, titledPane);
            content.getStyleClass().add("container");
            dialog.setContent(content);
        }

        dialog.setException(exception);

        dialogs.add(dialog);

        return dialog;
    }

    public final Dialog<ButtonType> showWarning(String title, String message) {
        return doShowDialog(Type.WARNING, title, message);
    }

    public final Dialog<ButtonType> showConfirmation(String title, String message) {
        return doShowDialog(Type.CONFIRMATION, title, message);
    }

    public final Dialog<ButtonType> showInformation(String title, String message) {
        return doShowDialog(Type.INFORMATION, title, message);
    }

    public final Dialog<ButtonType> showInformation(String title, String message, List<ButtonType> buttons) {
        return doShowDialog(Type.INFORMATION, title, message, buttons);
    }

    public final Dialog<String> showTextInput(String title, String text, boolean multiline) {
        return showTextInput(title, null, null, text, multiline);
    }

    public final Dialog<String> showTextInput(String title, String message, String text, boolean multiline) {
        return showTextInput(title, message, null, text, multiline);
    }

    public final Dialog<String> showTextInput(String title, String message, String prompt, String text, boolean multiline) {
        TextInputControl textInputControl;
        Node node;

        if (multiline) {
            ResizableTextArea textArea = new ResizableTextArea(text);
            textArea.getEditor().setPromptText(prompt);
            textArea.getEditor().setWrapText(true);
            textArea.getEditor().setPrefRowCount(6);
            textArea.setResizeVertical(true);
            textArea.setResizeHorizontal(true);
            textInputControl = textArea.getEditor();
            node = textArea;
        } else {
            TextField textField = new TextField(text);
            textField.setPromptText(prompt);
            textField.setPrefColumnCount(20);
            textInputControl = textField;
            node = textField;
        }

        VBox box = new VBox();
        box.getStyleClass().add("prompt-node-wrapper");

        if (StringUtils.isNotBlank(message)) {
            Label promptLabel = new Label(message);
            box.getChildren().add(promptLabel);
        }

        box.getChildren().add(node);
        Dialog<String> dialog = showNode(Type.INPUT, title, box);
        textInputControl.textProperty().addListener(it -> dialog.setValue(textInputControl.getText()));
        return dialog;
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node) {
        return showNode(type, title, node, false, Collections.emptyList());
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize) {
        return showNode(type, title, node, maximize, Collections.emptyList());
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node, List<ButtonType> buttons) {
        return showNode(type, title, node, false, buttons);
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons) {
        return showNode(type, title, node, maximize, buttons, true, null);
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons, boolean sameWidthButtons) {
        return showNode(type, title, node, maximize, buttons, sameWidthButtons, null);
    }

    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons, boolean sameWidthButtons, BooleanProperty validProperty) {
        Dialog<T> dialog = new Dialog<>(this, type);
        dialog.setTitle(title);
        dialog.setContent(node);
        dialog.setMaximize(maximize);
        dialog.setSameWidthButtons(sameWidthButtons);

        if (!buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(buttons);
        }

        if (validProperty != null) {
            dialog.validProperty().bind(validProperty);
        }

        dialogs.add(dialog);

        return dialog;
    }

    public final Dialog<Void> showBusyIndicator() {
        BusyIndicator busyIndicator = new BusyIndicator();
        busyIndicator.sceneProperty().addListener(it -> {
            if (busyIndicator.getScene() != null) {
                busyIndicator.start();
            } else {
                busyIndicator.stop();
            }
        });

        Dialog<Void> dialog = new Dialog<>(this, Type.BLANK);
        dialog.setContent(busyIndicator);
        dialog.getButtonTypes().clear();

        dialogs.add(dialog);

        return dialog;
    }

    private final BooleanProperty animateDialogs = new SimpleBooleanProperty(this, "animateDialogs", false);

    public final BooleanProperty animateDialogsProperty() {
        return animateDialogs;
    }

    public final boolean isAnimateDialogs() {
        return animateDialogs.get();
    }

    public final void setAnimateDialogs(boolean animate) {
        animateDialogs.set(animate);
    }

    private final BooleanProperty fadeInOut = new SimpleBooleanProperty(this, "fadeInOut", true);

    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    public final void setFadeInOut(boolean animate) {
        fadeInOut.set(animate);
    }

    private final ReadOnlyBooleanWrapper showingDialog = new ReadOnlyBooleanWrapper(this, "showDialog", false);

    public ReadOnlyBooleanProperty showingDialogProperty() {
        return showingDialog.getReadOnlyProperty();
    }

    public boolean isShowingDialog() {
        return showingDialog.get();
    }

    private void slideInOut(double visibility, DoubleProperty visibilityProperty, Supplier<Node> nodeSupplier) {
        Node node = nodeSupplier.get();

        if (true) {
            if (visibility == 1) {
                ensureNodeInChildrenList(node);
                node.setVisible(true);
                node.setOpacity(0);
            } else {
                node.setOpacity(1);
            }

            KeyValue value1 = new KeyValue(visibilityProperty, visibility);
            KeyValue value2 = new KeyValue(node.opacityProperty(), visibility == 0 ? 0 : 1);
            KeyFrame frame = new KeyFrame(getAnimationDuration(), value1, value2);
            Timeline timeline = new Timeline(frame);
            timeline.setOnFinished(evt -> {
                if (visibility == 0) {
                    node.setVisible(false);
                    getChildren().remove(node);

                    if (node instanceof DialogContentPane) {
                        dialogPanes.remove(node);
                        dialogVisibilityMap.remove(node);
                    }
                }
            });
            timeline.play();
        } else {
            if (visibility == 1) {
                ensureNodeInChildrenList(node);
                node.setVisible(true);
                node.setOpacity(1);
                visibilityProperty.set(1);
            } else {
                node.setOpacity(0);
                visibilityProperty.set(0);
                getChildren().remove(node);

                if (node instanceof DialogContentPane) {
                    dialogPanes.remove(node);
                    dialogVisibilityMap.remove(node);
                }
            }
        }
    }

    private void ensureNodeInChildrenList(Node node) {
        if (!getChildren().contains(node)) {
            getChildren().add(node);
        }
    }

    @Override
    protected void layoutChildren() {
        final Insets insets = getInsets();

        double contentY = insets.getTop();
        double contentX = insets.getLeft();
        double contentWidth = getWidth() - insets.getLeft() - insets.getRight();
        double contentHeight = getHeight() - insets.getTop() - insets.getBottom();

        for (DialogContentPane dialogPane : dialogPanes) {
            double dialogPrefWidth = dialogPane.prefWidth(-1);
            double dialogPrefHeight = dialogPane.prefHeight(-1);

            Dialog<?> dialog = dialogPane.getDialog();

            if (dialog.isMaximize()) {
                dialogPrefWidth = contentWidth * .9;
                dialogPrefHeight = contentHeight * .9;
            } else {
                // make sure the dialog fits into the visible area
                dialogPrefWidth = Math.min(dialogPrefWidth, contentWidth * .9);
                dialogPrefHeight = Math.min(dialogPrefHeight, contentHeight * .9);
            }

            double dialogTargetY = contentY + (contentHeight - dialogPrefHeight) / 2;
            DoubleProperty dialogVisibility = dialogVisibilityMap.get(dialogPane);
            if (dialogVisibility != null) {
                dialogPane.resizeRelocate(contentX + (contentWidth - dialogPrefWidth) / 2, dialogTargetY * dialogVisibility.get(), dialogPrefWidth, dialogPrefHeight);
            }
        }

        if (glassPane.isVisible()) {
            glassPane.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
        }
    }

    public enum Type {
        INPUT,
        INFORMATION,
        ERROR,
        WARNING,
        CONFIRMATION,
        BLANK
    }

    public static class Dialog<T> extends CompletableFuture<ButtonType> {


        private final Type type;

        private final DialogPane glassPane;

        private boolean padding = true;

        public Dialog(DialogPane pane, Type type) {
            glassPane = Objects.requireNonNull(pane);
            this.type = Objects.requireNonNull(type);

            getStyleClass().add(type.name().toLowerCase());

            switch (type) {
                case INPUT:
                case WARNING:
                    getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    break;
                case INFORMATION:
                case ERROR:
                    getButtonTypes().setAll(ButtonType.CLOSE);
                    break;
                case CONFIRMATION:
                    getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    break;
                case BLANK:
                    break;
            }
        }

        public boolean isPadding() {
            return padding;
        }

        public void setPadding(boolean padding) {
            this.padding = padding;
        }

        private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

        public T getValue() {
            return value.get();
        }

        public ObjectProperty<T> valueProperty() {
            return value;
        }

        public void setValue(T value) {
            this.value.set(value);
        }

        public Type getType() {
            return type;
        }

        public DialogPane getGlassPane() {
            return glassPane;
        }

        /**
         * Hides the dialog by cancelling it.
         */
        public void cancel() {
            glassPane.hideDialog(this);
            setValue(null);
            complete(ButtonType.CANCEL);
        }

        // valid property

        // important to have default set to true
        private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);

        public boolean isValid() {
            return valid.get();
        }

        public BooleanProperty validProperty() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid.set(valid);
        }

        // button types

        private final ObservableList<ButtonType> buttonTypes = FXCollections.observableArrayList();

        public ObservableList<ButtonType> getButtonTypes() {
            return buttonTypes;
        }

        private final BooleanProperty sameWidthButtons = new SimpleBooleanProperty(true);

        public boolean isSameWidthButtons() {
            return sameWidthButtons.get();
        }

        public BooleanProperty sameWidthButtonsProperty() {
            return sameWidthButtons;
        }

        public void setSameWidthButtons(boolean sameWidthButtons) {
            this.sameWidthButtons.set(sameWidthButtons);
        }

        // maximize

        private final BooleanProperty maximize = new SimpleBooleanProperty();

        public final BooleanProperty maximizeProperty() {
            return maximize;
        }

        public final void setMaximize(boolean max) {
            maximize.set(max);
        }

        public final boolean isMaximize() {
            return maximize.get();
        }

        private final ObjectProperty<Callable> onCancelled = new SimpleObjectProperty<>(this, "onCancelled");

        public Callable getOnCancelled() {
            return onCancelled.get();
        }

        // content

        private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

        public final ObjectProperty<Node> contentProperty() {
            return content;
        }

        public void setContent(Node content) {
            this.content.set(content);
        }

        public Node getContent() {
            return content.get();
        }

        // title

        private final StringProperty title = new SimpleStringProperty(this, "title", "Dialog");

        public final StringProperty titleProperty() {
            return title;
        }

        public final String getTitle() {
            return title.get();
        }

        public final void setTitle(String title) {
            this.title.set(title);
        }

        // custom style

        private final ObservableList<String> styleClass = FXCollections.observableArrayList();

        public ObservableList<String> getStyleClass() {
            return styleClass;
        }


        // Show buttons bar
        private final BooleanProperty showButtonsBar = new SimpleBooleanProperty(this, "showButtonsBar", true);

        public final BooleanProperty showButtonsBarProperty() {
            return showButtonsBar;
        }

        public final boolean isShowButtonsBar() {
            return showButtonsBarProperty().get();
        }

        public final void setShowButtonsBar(boolean showButtonsBar) {
            showButtonsBarProperty().set(showButtonsBar);
        }

        // exception

        private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>(this, "exception");

        public final ObjectProperty<Throwable> exceptionProperty() {
            return exception;
        }

        public final void setException(Throwable ex) {
            exception.set(ex);
        }

        public final Throwable getException() {
            return exception.get();
        }
    }
}
