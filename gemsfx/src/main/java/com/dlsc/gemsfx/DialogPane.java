package com.dlsc.gemsfx;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

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

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * A pane that allows applications to display a lightweight dialog right inside the application
 * window instead of a separate window. The dialogs are animated and fly-in from top to center and
 * fly-out from center to top.
 * <p>
 * The dialog pane should be added to the application in such a way that it covers the entire area
 * of the window. If the application is using a StackPane as its root container then the dialog
 * pane can simply be added as the last child of the StackPane.
 * </p>
 * <h3>Example</h3>
 * To show an information dialog one can simply call:
 * <p>
 * <pre>
 *    dialogPane.showInformation("My Title", "My message");
 * </pre>
 * </p>
 * <p>
 * If an application wants to act upon the button that was pressed after showing a confirmation dialog
 * then the following can be written:
 * <p>
 * <pre>
 *     dialogPane.showConfirmation("Confirm", "Really delete?").thenAccept(buttonType -> { ... });
 * </pre>
 * </p>
 *
 * </p>
 */
public class DialogPane extends Pane {

    private final GlassPane glassPane;

    private final ObservableList<ContentPane> dialogContentPanes = FXCollections.observableArrayList();

    private final Map<ContentPane, DoubleProperty> dialogVisibilityMap = new HashMap<>();

    private final ListProperty<Dialog> dialogs = new SimpleListProperty<>(this, "dialogs", FXCollections.observableArrayList());

    private final EventHandler<KeyEvent> escapeHandler = evt -> {
        if (evt.getCode() == KeyCode.ESCAPE) {// hide the last dialog that was opened
            if (!dialogs.isEmpty()) {
                Dialog<?> dialog = dialogs.get(dialogs.size() - 1);
                if (!dialog.isCancelled()) {
                    dialog.cancel();
                    evt.consume();
                }
            }
        }
    };

    private final WeakEventHandler<KeyEvent> weakEscapeHandler = new WeakEventHandler<>(escapeHandler);

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
                        ContentPane dialogPane = new ContentPane(dialog);
                        dialogPane.blockedProperty().bind(Bindings.createBooleanBinding(() -> !dialogContentPanes.isEmpty() && dialogContentPanes.get(dialogContentPanes.size() - 1) != dialogPane, dialogContentPanes));
                        dialogContentPanes.add(dialogPane);

                        SimpleDoubleProperty visibility = new SimpleDoubleProperty();
                        visibility.addListener(it -> requestLayout());
                        dialogVisibilityMap.put(dialogPane, visibility);
                        getChildren().add(dialogPane);
                        slideInOut(1, visibility, () -> dialogPane);
                    });

                } else if (change.wasRemoved()) {

                    change.getRemoved().forEach(dialog -> {
                        Optional<ContentPane> dialogOptional = dialogContentPanes.stream().filter(d -> d.getDialog() == dialog).findFirst();
                        if (dialogOptional.isPresent()) {
                            ContentPane dialogPane = dialogOptional.get();
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
            if (oldScene != null) {
                oldScene.removeEventHandler(KeyEvent.KEY_PRESSED, weakEscapeHandler);
            }
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, weakEscapeHandler);
            }
        });

        getStylesheets().add(getUserAgentStylesheet());
    }

    @Override
    public String getUserAgentStylesheet() {
        return DialogPane.class.getResource("dialog.css").toExternalForm();
    }

    private final BooleanProperty showCloseButton = new SimpleBooleanProperty(this, "showCloseButton", true);

    public boolean isShowCloseButton() {
        return showCloseButton.get();
    }

    public BooleanProperty showCloseButtonProperty() {
        return showCloseButton;
    }

    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton.set(showCloseButton);
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

    public final Dialog<String> showTextInput(String title, String text) {
        return showTextInput(title, null, null, text, false);
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
            textArea.getEditor().skinProperty().addListener(it -> Platform.runLater(() -> textArea.getEditor().requestFocus()));
            textInputControl = textArea.getEditor();
            node = textArea;
        } else {
            TextField textField = new TextField(text);
            textField.setPromptText(prompt);
            textField.setPrefColumnCount(20);
            textField.skinProperty().addListener(it -> Platform.runLater(() -> textField.requestFocus()));
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

    private final BooleanProperty animateDialogs = new SimpleBooleanProperty(this, "animateDialogs", true);

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

        if (!getAnimationDuration().equals(Duration.ZERO) && isAnimateDialogs()) {
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

                    if (node instanceof ContentPane) {
                        dialogContentPanes.remove(node);
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

                if (node instanceof ContentPane) {
                    dialogContentPanes.remove(node);
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
        Insets insets = getInsets();

        double contentY = insets.getTop();
        double contentX = insets.getLeft();
        double contentWidth = getWidth() - insets.getLeft() - insets.getRight();
        double contentHeight = getHeight() - insets.getTop() - insets.getBottom();

        for (ContentPane dialogContentPane : dialogContentPanes) {
            double dialogWidth = Math.min(dialogContentPane.maxWidth(-1), Math.max(dialogContentPane.minWidth(-1), dialogContentPane.prefWidth(-1)));
            double dialogHeight = Math.min(dialogContentPane.maxHeight(-1), Math.max(dialogContentPane.minHeight(-1), dialogContentPane.prefHeight(-1)));

            Dialog<?> dialog = dialogContentPane.getDialog();

            if (dialog.isMaximize()) {
                dialogWidth = contentWidth * .9;
                dialogHeight = contentHeight * .9;
            } else {
                // make sure the dialog fits into the visible area
                dialogWidth = Math.min(dialogWidth, contentWidth * .9);
                dialogHeight = Math.min(dialogHeight, contentHeight * .9);
            }

            double dialogTargetY = contentY + (contentHeight - dialogHeight) / 2;
            DoubleProperty dialogVisibility = dialogVisibilityMap.get(dialogContentPane);
            if (dialogVisibility != null) {
                dialogContentPane.resizeRelocate(contentX + (contentWidth - dialogWidth) / 2, dialogTargetY * dialogVisibility.get(), dialogWidth, dialogHeight);
            }
        }

        if (glassPane.isVisible()) {
            glassPane.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
        }
    }

    /**
     * The various dialog types supported by {@link DialogPane}.
     *
     * @see Dialog#type
     */
    public enum Type {

        /**
         * A dialog type that is asking for user input.
         */
        INPUT,

        /**
         * A dialog type used for showing any kind of information to the user.
         */
        INFORMATION,

        /**
         * A dialog type used for showing any kind of (severe) error to the user.
         */
        ERROR,

        /**
         * A dialog type used for warning the user.
         */
        WARNING,

        /**
         * A dialog type used for asking the user for confirmation before executing an action.
         */
        CONFIRMATION,

        /**
         * A blank dialog that can be used for anything.
         */
        BLANK
    }

    // converter

    private final ObjectProperty<StringConverter<ButtonType>> converter = new SimpleObjectProperty<>(this, "converter", new StringConverter<ButtonType>() {
        @Override
        public String toString(ButtonType buttonType) {
            if (buttonType != null) {
                return buttonType.getText();
            }
            return "";
        }

        @Override
        public ButtonType fromString(String string) {
            return null;
        }
    });

    public final StringConverter<ButtonType> getConverter() {
        return converter.get();
    }

    /**
     * A string converter can be applied to easily manipulate the default text representation of
     * the various button types. An example could for example be that the application design asks
     * for all UPPER case letters to be used for the buttons.
     *
     * @return a string converter
     */
    public final ObjectProperty<StringConverter<ButtonType>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<ButtonType> converter) {
        this.converter.set(converter);
    }

    public static class Dialog<T> extends CompletableFuture<ButtonType> {

        private final Type type;

        private final DialogPane pane;

        private boolean padding = true;

        public Dialog(DialogPane pane, Type type) {
            this.pane = Objects.requireNonNull(pane);
            this.type = Objects.requireNonNull(type);

            getStyleClass().add(type.name().toLowerCase());

            switch (type) {
                case INPUT:
                case WARNING:
                    getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    break;
                case INFORMATION:
                case ERROR:
                    getButtonTypes().setAll(ButtonType.OK);
                    break;
                case CONFIRMATION:
                    getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    break;
                case BLANK:
                    break;
            }
        }

        private final BooleanProperty showCloseButton = new SimpleBooleanProperty(this, "showCloseButton", true);

        public final boolean isShowCloseButton() {
            return showCloseButton.get();
        }

        public final BooleanProperty showCloseButtonProperty() {
            return showCloseButton;
        }

        public final void setShowCloseButton(boolean showCloseButton) {
            this.showCloseButton.set(showCloseButton);
        }

        private final ObjectProperty<Duration> delay = new SimpleObjectProperty<>(this, "delay", Duration.millis(100));

        public final Duration getDelay() {
            return delay.get();
        }

        public final ObjectProperty<Duration> delayProperty() {
            return delay;
        }

        public final void setDelay(Duration delay) {
            this.delay.set(delay);
        }

        public final boolean isPadding() {
            return padding;
        }

        public final void setPadding(boolean padding) {
            this.padding = padding;
        }

        private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

        public final T getValue() {
            return value.get();
        }

        public final ObjectProperty<T> valueProperty() {
            return value;
        }

        public final void setValue(T value) {
            this.value.set(value);
        }

        public final Type getType() {
            return type;
        }

        public final DialogPane getDialogPane() {
            return pane;
        }

        /**
         * Hides the dialog by cancelling it.
         */
        public void cancel() {
            pane.hideDialog(this);
            setValue(null);
            complete(ButtonType.CANCEL);
        }

        // valid property (important to have default set to true)
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

        Map<ButtonType, Button> buttonMap = new HashMap<>();

        public final Button getButton(ButtonType type) {
            return buttonMap.get(type);
        }
    }

    private class ContentPane extends StackPane {

        private final ButtonBar dialogButtonBar;
        private final Dialog<?> dialog;

        private final ChangeListener<Node> focusListener = (o, oldOwner, newOwner) -> {
            if (newOwner != null && !isInsideDialogPane(newOwner.getParent()) && getScene() != null) {
                requestFocus();
            }
        };

        private final WeakChangeListener<Node> weakFocusListener = new WeakChangeListener<>(focusListener);

        public ContentPane(Dialog<?> dialog) {
            this.dialog = Objects.requireNonNull(dialog);

            DialogPane shell = this.dialog.getDialogPane();

            Label dialogTitle = new Label("Dialog");
            dialogTitle.setMaxWidth(Double.MAX_VALUE);
            dialogTitle.getStyleClass().add("title");

            ImageView dialogIcon = new ImageView();
            dialogIcon.getStyleClass().addAll("icon");

            VBox dialogHeader = new VBox();
            dialogHeader.setAlignment(Pos.CENTER);
            dialogHeader.getStyleClass().add("header");
            dialogHeader.managedProperty().bind(dialogHeader.visibleProperty());
            dialogHeader.getChildren().setAll(dialogIcon, dialogTitle);

            StackPane content = new StackPane();
            content.getStyleClass().add("content");

            if (dialog.isPadding()) {
                content.getStyleClass().add("padding");
            }

            dialogButtonBar = new ButtonBar();

            if (System.getProperty("os.name").startsWith("Mac")) {
                dialogButtonBar.setButtonOrder("L_NCYOAHE+U+FBIX_R");
            } else if (System.getProperty("os.name").startsWith("Windows")) {
                dialogButtonBar.setButtonOrder("L_YNOCAHE+U+FBXI_R");
            } else {
                dialogButtonBar.setButtonOrder("L_HENYCOA+U+FBIX_R");
            }

            dialogButtonBar.getStyleClass().add("button-bar");
            dialogButtonBar.managedProperty().bind(dialogButtonBar.visibleProperty());
            dialogButtonBar.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (oldScene != null) {
                    oldScene.focusOwnerProperty().removeListener(weakFocusListener);
                }
                if (newScene != null) {
                    newScene.focusOwnerProperty().addListener(weakFocusListener);
                }
            });

            VBox.setVgrow(dialogTitle, Priority.NEVER);
            VBox.setVgrow(content, Priority.ALWAYS);
            VBox.setVgrow(dialogButtonBar, Priority.NEVER);

            content.getChildren().setAll(this.dialog.getContent());

            boolean blankDialog = this.dialog.getType().equals(Type.BLANK);

            dialogHeader.setVisible(!blankDialog);
            dialogButtonBar.setVisible(!blankDialog);

            dialogTitle.textProperty().bind(this.dialog.titleProperty());
            getStyleClass().setAll("content-pane");
            getStyleClass().addAll(this.dialog.getStyleClass());

            if (!blankDialog) {
                createButtons();
            }

            VBox box = new VBox();
            box.getStyleClass().add("vbox");
            box.setFillWidth(true);
            box.getChildren().setAll(dialogHeader, content, dialogButtonBar);

            GlassPane glassPane = new GlassPane();
            glassPane.hideProperty().bind(blocked.not());
            glassPane.fadeInOutProperty().bind(shell.fadeInOutProperty());

            FontIcon fontIcon = new FontIcon(MaterialDesign.MDI_CLOSE);

            Button closeButton = new Button();
            closeButton.setGraphic(fontIcon);
            closeButton.setAlignment(Pos.CENTER);
            closeButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            closeButton.getStyleClass().add("close-button");
            closeButton.visibleProperty().bind(dialog.showCloseButtonProperty().and(showCloseButtonProperty()));
            closeButton.managedProperty().bind(dialog.showCloseButtonProperty().and(showCloseButtonProperty()));
            closeButton.setOnAction(evt -> dialog.cancel());

            StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

            getChildren().addAll(box, closeButton, glassPane);
        }

        private boolean isInsideDialogPane(Parent parent) {
            if (parent == null) {
                return false;
            } else if (parent instanceof DialogPane) {
                return true;
            }

            return isInsideDialogPane(parent.getParent());
        }

        private final BooleanProperty blocked = new SimpleBooleanProperty(this, "blocked");

        public boolean isBlocked() {
            return blocked.get();
        }

        public BooleanProperty blockedProperty() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked.set(blocked);
        }

        public Dialog<?> getDialog() {
            return dialog;
        }

        private void createButtons() {
            dialogButtonBar.getButtons().clear();
            dialogButtonBar.setVisible(dialog.isShowButtonsBar());

            boolean hasDefault = false;
            for (ButtonType buttonType : dialog.getButtonTypes()) {
                Button button = createButton(buttonType);
                dialog.buttonMap.put(buttonType, button);

                switch (buttonType.getButtonData()) {
                    case LEFT:
                    case RIGHT:
                    case HELP:
                    case HELP_2:
                    case NO:
                    case NEXT_FORWARD:
                    case BACK_PREVIOUS:
                    case APPLY:
                    case CANCEL_CLOSE:
                    case OTHER:
                    case BIG_GAP:
                    case SMALL_GAP:
                        break;
                    case YES:
                    case OK_DONE:
                    case FINISH:
                        button.disableProperty().bind(dialog.validProperty().not());
                        break;

                }

                if (button != null) {
                    ButtonData buttonData = buttonType.getButtonData();

                    button.setDefaultButton(!hasDefault && buttonData != null && buttonData.isDefaultButton());
                    button.setCancelButton(buttonData != null && buttonData.isCancelButton());
                    button.setOnAction(evt -> {
                        if (buttonType.equals(ButtonType.CANCEL)) {
                            dialog.cancel();
                        } else {
                            dialog.getDialogPane().hideDialog(dialog);
                            dialog.complete(buttonType);
                        }
                    });

                    hasDefault |= buttonData != null && buttonData.isDefaultButton();

                    /*
                     * Special support for list views. A double click inside of them triggers
                     * the default button.
                     */
                    if (button.isDefaultButton()) {

                        Node content = dialog.getContent();
                        if (content instanceof ListView) {
                            content.setOnMouseClicked(evt -> {
                                if (evt.getClickCount() == 2) {
                                    button.fire();
                                }
                            });
                        }
                    }
                }

                dialogButtonBar.getButtons().add(button);
            }
        }

        protected Button createButton(ButtonType buttonType) {
            String text = buttonType.getText();
            StringConverter<ButtonType> converter = getDialog().getDialogPane().getConverter();
            if (converter != null) {
                text = converter.toString(buttonType);
            }

            Button button = new Button(text);
            button.getStyleClass().add(buttonType.getButtonData().getTypeCode().toLowerCase());
            ButtonData buttonData = buttonType.getButtonData();
            button.setMinWidth(Region.USE_PREF_SIZE);
            ButtonBar.setButtonUniformSize(button, dialog.isSameWidthButtons());
            ButtonBar.setButtonData(button, buttonData);
            button.setDefaultButton(buttonData.isDefaultButton());
            button.setCancelButton(buttonData.isCancelButton());

            return button;
        }
    }

    private class BusyIndicator extends CircularProgressIndicator {

        public BusyIndicator() {
            getStyleClass().add("busy-indicator");
        }

        public void stop() {
            setProgress(0);
        }

        public void start() {
            setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        }
    }

    /**
     * Created by hansolo on 08.04.16.
     */
    private class CircularProgressIndicator extends Region {
        private static final double PREFERRED_WIDTH = 24;
        private static final double PREFERRED_HEIGHT = 24;
        private static final double MINIMUM_WIDTH = 12;
        private static final double MINIMUM_HEIGHT = 12;
        private static final double MAXIMUM_WIDTH = 1024;
        private static final double MAXIMUM_HEIGHT = 1024;
        private final DoubleProperty dashOffset = new SimpleDoubleProperty(0);
        private final DoubleProperty dashArray_0 = new SimpleDoubleProperty(1);
        private StackPane indeterminatePane;
        private Pane progressPane;
        private Circle circle;
        private Arc arc;
        private final Timeline timeline;
        private RotateTransition indeterminatePaneRotation;
        private final InvalidationListener listener;
        private final DoubleProperty progress;
        private final BooleanProperty indeterminate;
        private final BooleanProperty roundLineCap;
        private boolean isRunning;


        // ******************** Constructors **************************************
        public CircularProgressIndicator() {
            getStyleClass().add("circular-progress");
            progress = new DoublePropertyBase(0) {
                @Override
                public void invalidated() {
                    if (get() < 0) {
                        startIndeterminate();
                    } else {
                        stopIndeterminate();
                        set(Math.max(0, Math.min(1, get())));
                        redraw();
                    }
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "progress";
                }
            };
            indeterminate = new BooleanPropertyBase(false) {
                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "indeterminate";
                }
            };
            roundLineCap = new BooleanPropertyBase(false) {
                @Override
                public void invalidated() {
                    if (get()) {
                        circle.setStrokeLineCap(StrokeLineCap.ROUND);
                        arc.setStrokeLineCap(StrokeLineCap.ROUND);
                    } else {
                        circle.setStrokeLineCap(StrokeLineCap.SQUARE);
                        arc.setStrokeLineCap(StrokeLineCap.SQUARE);
                    }
                }

                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "roundLineCap";
                }
            };
            isRunning = false;
            timeline = new Timeline();
            listener = observable -> {
                circle.setStrokeDashOffset(dashOffset.get());
                circle.getStrokeDashArray().setAll(dashArray_0.getValue(), 200d);
            };
            init();
            initGraphics();
            registerListeners();
        }


        // ******************** Initialization ************************************
        private void init() {
            if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
                    Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
                if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                    setPrefSize(getPrefWidth(), getPrefHeight());
                } else {
                    setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
                }
            }

            if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
                setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
            }

            if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
                setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
            }
        }

        private void initGraphics() {
            double center = PREFERRED_WIDTH * 0.5;
            double radius = PREFERRED_WIDTH * 0.45;
            circle = new Circle();
            circle.setCenterX(center);
            circle.setCenterY(center);
            circle.setRadius(radius);
            circle.getStyleClass().add("indicator");
            circle.setStrokeLineCap(isRoundLineCap() ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
            circle.setStrokeWidth(PREFERRED_WIDTH * 0.10526316);
            circle.setStrokeDashOffset(dashOffset.get());
            circle.getStrokeDashArray().setAll(dashArray_0.getValue(), 200d);

            arc = new Arc(center, center, radius, radius, 90, -360.0 * getProgress());
            arc.setStrokeLineCap(isRoundLineCap() ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
            arc.setStrokeWidth(PREFERRED_WIDTH * 0.1);
            arc.getStyleClass().add("indicator");

            indeterminatePane = new StackPane(circle);
            indeterminatePane.setVisible(false);

            progressPane = new Pane(arc);
            progressPane.setVisible(Double.compare(getProgress(), 0.0) != 0);

            getChildren().setAll(progressPane, indeterminatePane);

            // Setup timeline animation
            KeyValue kvDashOffset_0 = new KeyValue(dashOffset, 0, Interpolator.EASE_BOTH);
            KeyValue kvDashOffset_50 = new KeyValue(dashOffset, -32, Interpolator.EASE_BOTH);
            KeyValue kvDashOffset_100 = new KeyValue(dashOffset, -64, Interpolator.EASE_BOTH);

            KeyValue kvDashArray_0_0 = new KeyValue(dashArray_0, 5, Interpolator.EASE_BOTH);
            KeyValue kvDashArray_0_50 = new KeyValue(dashArray_0, 89, Interpolator.EASE_BOTH);
            KeyValue kvDashArray_0_100 = new KeyValue(dashArray_0, 89, Interpolator.EASE_BOTH);

            KeyValue kvRotate_0 = new KeyValue(circle.rotateProperty(), -10, Interpolator.LINEAR);
            KeyValue kvRotate_100 = new KeyValue(circle.rotateProperty(), 370, Interpolator.LINEAR);

            KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvDashOffset_0, kvDashArray_0_0, kvRotate_0);
            KeyFrame kf1 = new KeyFrame(Duration.millis(1000), kvDashOffset_50, kvDashArray_0_50);
            KeyFrame kf2 = new KeyFrame(Duration.millis(1500), kvDashOffset_100, kvDashArray_0_100, kvRotate_100);

            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.getKeyFrames().setAll(kf0, kf1, kf2);

            // Setup additional pane rotation
            indeterminatePaneRotation = new RotateTransition();
            indeterminatePaneRotation.setNode(indeterminatePane);
            indeterminatePaneRotation.setFromAngle(0);
            indeterminatePaneRotation.setToAngle(-360);
            indeterminatePaneRotation.setInterpolator(Interpolator.LINEAR);
            indeterminatePaneRotation.setCycleCount(Animation.INDEFINITE);
            indeterminatePaneRotation.setDuration(new Duration(4500));

            indeterminatePane.rotateProperty().addListener(it -> {
                boolean stop;
                Scene scene = indeterminatePane.getScene();
                if (scene != null) {
                    Window window = scene.getWindow();
                    if (window == null) {
                        stop = true;
                    } else {
                        stop = !window.isShowing();
                    }
                } else {
                    stop = true;
                }

                if (stop) {
                    stopIndeterminate();
                }
            });

        }

        private void registerListeners() {
            widthProperty().addListener(o -> resize());
            heightProperty().addListener(o -> resize());
            progress.addListener(o -> redraw());
            dashOffset.addListener(listener);
        }


        // ******************** Methods *******************************************
        public double getProgress() {
            return progress.get();
        }

        public void setProgress(double PROGRESS) {
            progress.set(PROGRESS);
        }

        public DoubleProperty progressProperty() {
            return progress;
        }

        private void startIndeterminate() {
            if (isRunning) return;
            manageNode(indeterminatePane, true);
            manageNode(progressPane, false);
            timeline.play();
            indeterminatePaneRotation.play();
            isRunning = true;
            indeterminate.set(true);
        }

        private void stopIndeterminate() {
            if (!isRunning) return;
            timeline.stop();
            indeterminatePaneRotation.stop();
            indeterminatePane.setRotate(0);
            manageNode(progressPane, true);
            manageNode(indeterminatePane, false);
            isRunning = false;
            indeterminate.set(false);
        }

        public boolean isIndeterminate() {
            return Double.compare(ProgressIndicator.INDETERMINATE_PROGRESS, getProgress()) == 0;
        }

        public ReadOnlyBooleanProperty indeterminateProperty() {
            return indeterminate;
        }

        public boolean isRoundLineCap() {
            return roundLineCap.get();
        }

        public void setRoundLineCap(boolean BOOLEAN) {
            roundLineCap.set(BOOLEAN);
        }

        public BooleanProperty roundLineCapProperty() {
            return roundLineCap;
        }

        private void manageNode(Node NODE, boolean MANAGED) {
            if (MANAGED) {
                NODE.setManaged(true);
                NODE.setVisible(true);
            } else {
                NODE.setVisible(false);
                NODE.setManaged(false);
            }
        }


        // ******************** Resizing ******************************************
        private void resize() {
            double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
            double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
            double size = Math.min(width, height);

            if (width > 0 && height > 0) {
                indeterminatePane.setMaxSize(size, size);
                indeterminatePane.setPrefSize(size, size);
                indeterminatePane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

                progressPane.setMaxSize(size, size);
                progressPane.setPrefSize(size, size);
                progressPane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

                double center = size * 0.5;
                double radius = size * 0.45;

                arc.setCenterX(center);
                arc.setCenterY(center);
                arc.setRadiusX(radius);
                arc.setRadiusY(radius);
                arc.setStrokeWidth(size * 0.10526316);

                double factor = size / 24;
                circle.setScaleX(factor);
                circle.setScaleY(factor);
            }
        }

        private void redraw() {
            double progress = getProgress();
            progressPane.setVisible(Double.compare(progress, 0) > 0);
            arc.setLength(-360.0 * progress);
        }
    }
}
