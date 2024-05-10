package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.FocusUtil;
import com.dlsc.gemsfx.util.ResizingBehaviour;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.ButtonBarSkin;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
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
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import net.synedra.validatorfx.Validator;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

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
 *     dialogPane.showConfirmation("Confirm", "Really delete?").onClose(buttonType -> { ... });
 * </pre>
 * </p>
 * The pane supports factories for creating the header and the footer. This allows application developers
 * to completely replace those elements of the dialogs. Default factories are already registered and will
 * be used if not replaced.
 *
 * @see #setHeaderFactory(Callback)
 * @see #setFooterFactory(Callback)
 * </p>
 */
public class DialogPane extends Pane {

    private static final String MESSAGE_LABEL_STYLE_CLASS = "message-label";

    private final GlassPane glassPane;

    private final ObservableList<ContentPane> dialogContentPanes = FXCollections.observableArrayList();

    private final Map<ContentPane, DoubleProperty> dialogVisibilityMap = new HashMap<>();

    private final EventHandler<KeyEvent> escapeHandler = evt -> {
        if (KeyCombination.keyCombination("ESC+SHIFT").match(evt)) {
            hideAllDialogs();
        } else if (evt.getCode() == KeyCode.ESCAPE) { // hide the last dialog that was opened
            ObservableList<Dialog<?>> dialogs = getDialogs();
            if (!dialogs.isEmpty()) {
                Dialog<?> dialog = dialogs.get(dialogs.size() - 1);
                dialog.cancel();
                evt.consume();
            }
        }
    };

    private final WeakEventHandler<KeyEvent> weakEscapeHandler = new WeakEventHandler<>(escapeHandler);

    /**
     * Constructs a new dialog pane.
     */
    public DialogPane() {
        getStyleClass().add("dialog-pane");

        setHeaderFactory(DialogHeader::new);
        setFooterFactory(DialogButtonBar::new);

        showingDialog.bind(dialogs.emptyProperty().not());

        mouseTransparentProperty().bind(showingDialogProperty().not());

        visibleProperty().bind(dialogsProperty().emptyProperty().not());
        managedProperty().bind(dialogsProperty().emptyProperty().not());

        glassPane = new GlassPane();
        glassPane.fadeInOutProperty().bind(fadeInOutProperty());
        glassPane.fadeInOutDurationProperty().bind(animationDurationProperty());
        glassPane.hideProperty().bind(dialogs.emptyProperty());

        dialogs.addListener((ListChangeListener.Change<? extends Dialog<?>> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {

                    change.getAddedSubList().forEach(dialog -> {
                        ContentPane contentPane = new ContentPane(dialog);
                        contentPane.blockedProperty().bind(Bindings.createBooleanBinding(() -> !dialogContentPanes.isEmpty() && dialogContentPanes.get(dialogContentPanes.size() - 1) != contentPane, dialogContentPanes));
                        dialogContentPanes.add(contentPane);

                        SimpleDoubleProperty visibility = new SimpleDoubleProperty();
                        visibility.addListener(it -> requestLayout());
                        dialogVisibilityMap.put(contentPane, visibility);
                        slideInOut(1, visibility, () -> contentPane);
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
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, weakEscapeHandler);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, weakEscapeHandler);
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DialogPane.class.getResource("dialog-pane.css")).toExternalForm();
    }

    /**
     * Retrieves the glass pane associated with this dialog pane.
     * The glass pane is a transparent overlay that can be used to block user input.
     *
     * @return the glass pane object
     */
    public final GlassPane getGlassPane() {
        return glassPane;
    }

    // header factory

    private final ObjectProperty<Callback<Dialog<?>, Node>> headerFactory = new SimpleObjectProperty<>(this, "headerFactory");

    public final Callback<Dialog<?>, Node> getHeaderFactory() {
        return headerFactory.get();
    }

    /**
     * A callback used as a factory for creating the header for each dialog. The dialog pane comes
     * with its own built-in header factory. Applications only need to specify one if they completely
     * want to change the appearance and / or the behaviour of the title area.
     *
     * @return the header factory
     * @see DialogHeader
     * @see #footerFactoryProperty()
     */
    public final ObjectProperty<Callback<Dialog<?>, Node>> headerFactoryProperty() {
        return headerFactory;
    }

    public final void setHeaderFactory(Callback<Dialog<?>, Node> headerFactory) {
        this.headerFactory.set(headerFactory);
    }

    // footer factory

    private final ObjectProperty<Callback<Dialog<?>, Node>> footerFactory = new SimpleObjectProperty<>(this, "footerFactory");

    public final Callback<Dialog<?>, Node> getFooterFactory() {
        return footerFactory.get();
    }

    /**
     * A callback used as a factory for creating the footer for each dialog. The dialog pane comes
     * with its own built-in footer factory. Applications only need to specify one if they completely
     * want to change the appearance and / or the behaviour of the button bar.
     *
     * @return the header factory
     * @see DialogButtonBar
     * @see #headerFactoryProperty()
     */
    public final ObjectProperty<Callback<Dialog<?>, Node>> footerFactoryProperty() {
        return footerFactory;
    }

    public final void setFooterFactory(Callback<Dialog<?>, Node> footerFactory) {
        this.footerFactory.set(footerFactory);
    }

    // dialogs

    private final ListProperty<Dialog<?>> dialogs = new SimpleListProperty<>(this, "dialogs", FXCollections.observableArrayList());

    /**
     * The list of currently active / showing dialogs.
     *
     * @return the list of dialogs currently showing
     */
    public final ObservableList<Dialog<?>> getDialogs() {
        return dialogs.get();
    }

    /**
     * Stores the list of currently active dialogs.
     *
     * @return the list of dialogs
     */
    public final ListProperty<Dialog<?>> dialogsProperty() {
        return dialogs;
    }

    public final void setDialogs(ObservableList<Dialog<?>> dialogs) {
        this.dialogs.set(dialogs);
    }

    // animation duration

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this, "animationDuration", Duration.millis(100));

    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * Stores the duration used to animate the fly-in / fly-out of the dialogs. The
     * default value is 100 milliseconds.
     *
     * @return the animation duration
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public final void setAnimationDuration(Duration animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    /**
     * Makes the given dialog visible in the pane.
     *
     * @param dialog the dialog to show
     * @throws IllegalArgumentException when the given dialog belongs to a different dialog pane
     */
    public void showDialog(Dialog<?> dialog) {
        if (dialog.getDialogPane() != this) {
            throw new IllegalArgumentException("the given dialog does not belong to this dialog pane");
        }
        dialogs.add(dialog);
    }

    /**
     * Hides the given dialog.
     *
     * @param dialog the dialog to hide
     * @throws IllegalArgumentException when the given dialog belongs to a different dialog pane
     */
    public void hideDialog(Dialog<?> dialog) {
        if (!dialogs.contains(dialog)) {
            throw new IllegalArgumentException("the given dialog does not belong to this dialog pane");
        }
        dialogs.remove(dialog);
    }

    /**
     * Hides all currently active dialogs.
     *
     * @see #hideDialog(Dialog)
     */
    public void hideAllDialogs() {
        List<Dialog<?>> list = new ArrayList<>(getDialogs());
        for (int i = list.size() - 1; i >= 0; i--) {
            hideDialog(list.get(i));
        }
    }

    private Dialog<ButtonType> doShowDialog(Type type, String title, String message) {
        return doShowDialog(type, title, message, Collections.emptyList());
    }

    private Dialog<ButtonType> doShowDialog(Type type, String title, String message, List<ButtonType> buttons) {
        Label messageLabel = getLabelSupplier().get();

        messageLabel.setText(message);
        messageLabel.setWrapText(true);

        if (!messageLabel.getStyleClass().contains(MESSAGE_LABEL_STYLE_CLASS)) {
            messageLabel.getStyleClass().add(MESSAGE_LABEL_STYLE_CLASS);
        }

        return showNode(type, title, messageLabel, buttons);
    }

    /**
     * Creates and shows an error message dialog.
     *
     * @param title   the title for the dialog
     * @param message the main error message
     * @return the dialog
     */
    public final Dialog<Void> showError(String title, String message) {
        return showError(title, message, (String) null);
    }

    /**
     * Creates and shows an error message dialog.
     *
     * @param title     the title for the dialog
     * @param message   the main error message
     * @param exception an exception that will be used to provide more details
     * @return the dialog
     */
    public final Dialog<Void> showError(String title, String message, Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return showError(title, message, stringWriter.toString());
    }

    /**
     * Creates and shows an error message dialog.
     *
     * @param title     the title for the dialog
     * @param exception an exception that will be used to provide the main message additional details
     * @return the dialog
     */
    public final Dialog<Void> showError(String title, Exception exception) {
        return showError(title, exception.getMessage(), exception);
    }

    /**
     * Creates and shows an error message dialog.
     *
     * @param title   the title for the dialog
     * @param message the main error message
     * @param details additional details
     * @return the dialog
     */
    public final Dialog<Void> showError(String title, String message, String details) {
        Dialog<Void> dialog = new Dialog<>(this, Type.ERROR);
        dialog.setTitle(title);

        Label messageLabel = getLabelSupplier().get();
        messageLabel.setText(message);
        messageLabel.setWrapText(true);
        if (!messageLabel.getStyleClass().contains(MESSAGE_LABEL_STYLE_CLASS)) {
            messageLabel.getStyleClass().add(MESSAGE_LABEL_STYLE_CLASS);
        }

        if (StringUtils.isBlank(details)) {
            dialog.setContent(messageLabel);
        } else {
            ResizableTextArea textArea = new ResizableTextArea();
            textArea.setText(details);
            textArea.setWrapText(false);
            textArea.setPrefColumnCount(80);
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

            FocusUtil.requestFocus(textArea);
        }

        dialog.show();

        return dialog;
    }

    /**
     * Creates and shows a warning dialog.
     *
     * @param title   the text shown in the header of the dialog
     * @param message the warning message
     * @return the dialog
     */
    public final Dialog<ButtonType> showWarning(String title, String message) {
        return showWarning(title, message, Collections.emptyList());
    }

    /**
     * Creates and shows a warning dialog.
     *
     * @param title   the text shown in the header of the dialog
     * @param message the warning message
     * @return the dialog
     */
    public final Dialog<ButtonType> showWarning(String title, String message, List<ButtonType> buttonTypes) {
        return doShowDialog(Type.WARNING, title, message, buttonTypes);
    }

    /**
     * Creates and shows a confirmation dialog.
     *
     * @param title   the text shown in the header of the dialog
     * @param message the main message, usually a question
     * @return the dialog
     */
    public final Dialog<ButtonType> showConfirmation(String title, String message) {
        return showConfirmation(title, message, Collections.emptyList());
    }

    /**
     * Creates and shows a confirmation dialog.
     *
     * @param title       the text shown in the header of the dialog
     * @param message     the main message, usually a question
     * @param buttonTypes the buttons to show in the footer
     * @return the dialog
     */
    public final Dialog<ButtonType> showConfirmation(String title, String message, List<ButtonType> buttonTypes) {
        return doShowDialog(Type.CONFIRMATION, title, message, buttonTypes);
    }

    /**
     * Creates and shows an information dialog.
     *
     * @param title   the text shown in the header of the dialog
     * @param message the main message, usually a question
     * @return the dialog
     */
    public final Dialog<ButtonType> showInformation(String title, String message) {
        return showInformation(title, message, Collections.emptyList());
    }

    /**
     * Creates and shows an information dialog.
     *
     * @param title   the text shown in the header of the dialog
     * @param message the main message, usually a question
     * @param buttons the list of buttons to create in the footer / button bar
     * @return the dialog
     */
    public final Dialog<ButtonType> showInformation(String title, String message, List<ButtonType> buttons) {
        return doShowDialog(Type.INFORMATION, title, message, buttons);
    }

    /**
     * Creates and shows a text input dialog.
     *
     * @param title the text shown in the header of the dialog
     * @param text  the initial text to show
     * @return the dialog
     */
    public final Dialog<String> showTextInput(String title, String text) {
        return showTextInput(title, null, null, text, false);
    }

    /**
     * Creates and shows a text input dialog.
     *
     * @param title     the text shown in the header of the dialog
     * @param text      the initial text to show
     * @param multiline if true the dialog will show a text area, otherwise a text field
     * @return the dialog
     */
    public final Dialog<String> showTextInput(String title, String text, boolean multiline) {
        return showTextInput(title, null, null, text, multiline);
    }

    /**
     * Creates and shows a text input dialog.
     *
     * @param title     the text shown in the header of the dialog
     * @param message   the main message, usually a question
     * @param text      the initial text to show
     * @param multiline if true the dialog will show a text area, otherwise a text field
     * @return the dialog
     */
    public final Dialog<String> showTextInput(String title, String message, String text, boolean multiline) {
        return showTextInput(title, message, null, text, multiline);
    }

    /**
     * Creates and shows a text input dialog.
     *
     * @param title     the text shown in the header of the dialog
     * @param message   the main message, usually a question
     * @param prompt    the prompt text for the text input control
     * @param text      the initial text to show
     * @param multiline if true the dialog will show a text area, otherwise a text field
     * @return the dialog
     */
    public final Dialog<String> showTextInput(String title, String message, String prompt, String text, boolean multiline) {
        return showTextInput(title, message, prompt, text, multiline, Collections.emptyList());
    }

    /**
     * Creates and shows a text input dialog.
     *
     * @param title     the text shown in the header of the dialog
     * @param message   the main message, usually a question
     * @param prompt    the prompt text for the text input control
     * @param text      the initial text to show
     * @param multiline if true the dialog will show a text area, otherwise a text field
     * @return the dialog
     */
    public final Dialog<String> showTextInput(String title, String message, String prompt, String text, boolean multiline, List<ButtonType> buttonTypes) {
        TextInputControl textInputControl;

        if (multiline) {
            ResizableTextArea textArea = new ResizableTextArea(text);
            textArea.setPromptText(prompt);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(6);
            textArea.setResizeVertical(true);
            textArea.setResizeHorizontal(true);
            textInputControl = textArea;
        } else {
            TextField textField = new TextField(text);
            textField.setPromptText(prompt);
            textField.setPrefColumnCount(20);
            textInputControl = textField;
        }

        FocusUtil.requestFocus(textInputControl);

        VBox box = new VBox();
        box.getStyleClass().add("prompt-node-wrapper");

        if (StringUtils.isNotBlank(message)) {
            Label promptLabel = getLabelSupplier().get();
            promptLabel.getStyleClass().add("prompt-label");
            promptLabel.setText(message);

            if (!promptLabel.getStyleClass().contains(MESSAGE_LABEL_STYLE_CLASS)) {
                promptLabel.getStyleClass().add(MESSAGE_LABEL_STYLE_CLASS);
            }

            box.getChildren().add(promptLabel);
        }

        box.getChildren().add(textInputControl);

        Dialog<String> dialog = showNode(Type.INPUT, title, box);
        dialog.valueProperty().bindBidirectional(textInputControl.textProperty());
        if (buttonTypes != null && !buttonTypes.isEmpty()) {
            dialog.getButtonTypes().setAll(buttonTypes);
        }

        dialog.getValidator().createCheck()
                .dependsOn("text", textInputControl.textProperty())
                .decorates(textInputControl)
                .immediateClear()
                .withMethod(ctx -> {
                    if (dialog.isRequired()) {
                        String str = ctx.get("text");
                        if (StringUtils.isBlank(str)) {
                            ctx.error("Missing text.");
                        }
                    }
                });

        return dialog;
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type  the type of dialog (info, warning, error, ....)
     * @param title the text shown in the title section / header of the dialog
     * @param node  the node to show
     * @param <T>   the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node) {
        return showNode(type, title, node, false, Collections.emptyList());
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type     the type of dialog (info, warning, error, ....)
     * @param title    the text shown in the title section / header of the dialog
     * @param node     the node to show
     * @param maximize if true the dialog will use the maximum size available
     * @param <T>      the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize) {
        return showNode(type, title, node, maximize, Collections.emptyList());
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type    the type of dialog (info, warning, error, ....)
     * @param title   the text shown in the title section / header of the dialog
     * @param node    the node to show
     * @param buttons a list of buttons to create inside the footer / button bar
     * @param <T>     the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node, List<ButtonType> buttons) {
        return showNode(type, title, node, false, buttons);
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type     the type of dialog (info, warning, error, ....)
     * @param title    the text shown in the title section / header of the dialog
     * @param node     the node to show
     * @param maximize if true the dialog will use the maximum size available
     * @param buttons  a list of buttons to create inside the footer / button bar
     * @param <T>      the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons) {
        return showNode(type, title, node, maximize, buttons, true, null);
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type             the type of dialog (info, warning, error, ....)
     * @param title            the text shown in the title section / header of the dialog
     * @param node             the node to show
     * @param maximize         if true the dialog will use the maximum size available
     * @param buttons          a list of buttons to create inside the footer / button bar
     * @param sameWidthButtons if true all buttons in the footer / button bar will have the same width
     * @param <T>              the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons, boolean sameWidthButtons) {
        return showNode(type, title, node, maximize, buttons, sameWidthButtons, null);
    }

    /**
     * Shows an arbitrary node in a dialog.
     *
     * @param type             the type of dialog (info, warning, error, ....)
     * @param title            the text shown in the title section / header of the dialog
     * @param node             the node to show
     * @param maximize         if true the dialog will use the maximum size available
     * @param buttons          a list of buttons to create inside the footer / button bar
     * @param sameWidthButtons if true all buttons in the footer / button bar will have the same width
     * @param validProperty    a validity property that will be bound to the validity property of the dialog
     * @param <T>              the type of the value provided by the dialog
     * @return the dialog
     */
    public final <T> Dialog<T> showNode(Type type, String title, Node node, boolean maximize, List<ButtonType> buttons, boolean sameWidthButtons, BooleanProperty validProperty) {
        Dialog<T> dialog = new Dialog<>(this, type);
        dialog.setTitle(title);
        dialog.setContent(node);
        dialog.setMaximize(maximize);
        dialog.setSameWidthButtons(sameWidthButtons);

        if (buttons != null && !buttons.isEmpty()) {
            dialog.getButtonTypes().setAll(buttons);
        }

        if (validProperty != null) {
            dialog.validProperty().bind(validProperty);
        }

        dialog.show();

        return dialog;
    }

    /**
     * Creates and shows a dialog that shows a busy indicator / busy animation.
     *
     * @return the create dialog
     */
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
        dialog.getStyleClass().add("busy-dialog");
        dialog.setContent(busyIndicator);
        dialog.getButtonTypes().clear();
        dialog.setDelay(Duration.millis(1000));
        dialog.setUsingPadding(true); // by default blank dialogs have no padding, so we are adding it back here
        dialog.show();

        return dialog;
    }

    // label factory

    private final ObjectProperty<Supplier<Label>> labelSupplier = new SimpleObjectProperty<>(this, "labelSupplier", Label::new);

    public final Supplier<Label> getLabelSupplier() {
        return labelSupplier.get();
    }

    /**
     * A supplier used for creating the label instances that will be used by the standard dialogs.
     *
     * @return the label supplier / callback / factory
     * @see #showInformation(String, String)
     * @see #showWarning(String, String)
     * @see #showError(String, String)
     * @see #showConfirmation(String, String)
     */
    public final ObjectProperty<Supplier<Label>> labelSupplierProperty() {
        return labelSupplier;
    }

    public final void setLabelSupplier(Supplier<Label> labelSupplier) {
        this.labelSupplier.set(labelSupplier);
    }

    // animate

    private final BooleanProperty animateDialogs = new SimpleBooleanProperty(this, "animateDialogs", true);

    /**
     * Determines if the dialogs will be animated when they appear. Animation is currently a simple fly-in /
     * fly-out. Other animation types could be considered in the future.
     *
     * @return true if the dialog will be animated when appearing / disappearing
     */
    public final BooleanProperty animateDialogsProperty() {
        return animateDialogs;
    }

    public final boolean isAnimateDialogs() {
        return animateDialogs.get();
    }

    public final void setAnimateDialogs(boolean animate) {
        animateDialogs.set(animate);
    }

    // fade-in / fade-out

    private final BooleanProperty fadeInOut = new SimpleBooleanProperty(this, "fadeInOut", true);

    /**
     * Controls whether the glass pane and the dialogs will animate their opacity during showing or
     * hiding dialogs.
     *
     * @return true if fade-in / fade-out will be used for showing / hiding dialogs
     */
    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    public final void setFadeInOut(boolean animate) {
        fadeInOut.set(animate);
    }

    // showing

    private final ReadOnlyBooleanWrapper showingDialog = new ReadOnlyBooleanWrapper(this, "showingDialog", false);

    /**
     * A read-only property used to signal that the pane is currently showing a dialog or not.
     *
     * @return true if at least one dialog is currently showing
     */
    public final ReadOnlyBooleanProperty showingDialogProperty() {
        return showingDialog.getReadOnlyProperty();
    }

    public final boolean isShowingDialog() {
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

    private final DoubleProperty maximizedPadding = new SimpleDoubleProperty(this, "dialogPadding", 20);

    public final double getMaximizedPadding() {
        return maximizedPadding.get();
    }

    /**
     * Defines the padding around a maximized dialog in the dialog pane. A padding of zero would result in
     * the dialog to use the entire width and height of the dialog pane.
     *
     * @return the padding around a maximized dialog
     */
    public final DoubleProperty maximizedPaddingProperty() {
        return maximizedPadding;
    }

    public final void setMaximizedPadding(double maximizedPadding) {
        this.maximizedPadding.set(maximizedPadding);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();

        double contentY = insets.getTop();
        double contentX = insets.getLeft();
        double contentWidth = getWidth() - insets.getLeft() - insets.getRight();
        double contentHeight = getHeight() - insets.getTop() - insets.getBottom();

        for (ContentPane contentPane : dialogContentPanes) {

            double dialogWidth = Math.min(contentPane.maxWidth(contentHeight), Math.max(contentPane.minWidth(contentHeight), contentPane.prefWidth(contentHeight)));
            double dialogHeight = Math.min(contentPane.maxHeight(contentWidth), Math.max(contentPane.minHeight(contentWidth), contentPane.prefHeight(contentWidth)));

            Dialog<?> dialog = contentPane.getDialog();

            double maxWidth = contentWidth - 2 * getMaximizedPadding();
            double maxHeight = contentHeight - 2 * getMaximizedPadding();

            if (dialog.isMaximize()) {
                dialogWidth = maxWidth;
                dialogHeight = maxHeight;
            } else {
                // make sure the dialog fits into the visible area
                dialogWidth = Math.min(dialogWidth, maxWidth);
                dialogHeight = Math.min(dialogHeight, maxHeight);
            }

            double dialogTargetY = contentY + (contentHeight - dialogHeight) / 2;
            DoubleProperty dialogVisibility = dialogVisibilityMap.get(contentPane);
            if (dialogVisibility != null) {
                contentPane.resizeRelocate(contentX + (contentWidth - dialogWidth) / 2, dialogTargetY * dialogVisibility.get(), dialogWidth, dialogHeight);
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

    /**
     * Represents a dialog that can be displayed to the user.
     *
     * @param <T> the type of value that the dialog can return
     */
    public static class Dialog<T> {

        private final Type type;

        private final DialogPane pane;

        /**
         * Creates a new dialog with the specified dialog pane and type.
         *
         * @param pane the dialog pane where the dialog will be shown
         * @param type the type of the dialog (info, warning, error, ...)
         */
        public Dialog(DialogPane pane, Type type) {
            this.pane = Objects.requireNonNull(pane, "dialog pane can not be null");
            this.type = Objects.requireNonNull(type, "dialog type can not be null");

            getStyleClass().add(type.name().toLowerCase());

            switch (type) {
                case INPUT:
                case WARNING:
                    getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    break;
                case INFORMATION:
                    getButtonTypes().setAll(ButtonType.OK);
                    break;
                case ERROR:
                    getButtonTypes().setAll(ButtonType.CLOSE);
                    break;
                case CONFIRMATION:
                    getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    break;
                case BLANK:
                    setUsingPadding(false);
                    break;
            }

            setOnResize(new DefaultResizeHandler(this));

            preferencesProperty().subscribe(preferences -> {
                if (preferences != null) {
                    double width = preferences.getDouble("width", -1d);
                    if (width != -1d) {
                        setPrefWidth(width);
                    }
                    double height = preferences.getDouble("height", -1d);
                    if (height != -1d) {
                        setPrefHeight(height);
                    }
                }
            });
        }

        /**
         * Returns the type of the dialog, e.g. "info", "error", "warning".
         *
         * @return the dialog's type
         */
        public final Type getType() {
            return type;
        }

        /**
         * Returns the dialog pane where the dialog is showing.
         *
         * @return the dialog pane to which the dialog belongs
         */
        public final DialogPane getDialogPane() {
            return pane;
        }

        // id

        private final StringProperty id = new SimpleStringProperty(this, "id");

        public final String getId() {
            return id.get();
        }

        /**
         * Stores an optional ID for the dialog, which can be handy for various situations.
         *
         * @return the id of the dialog, e.g. "preferences.dialog"
         */
        public final StringProperty idProperty() {
            return id;
        }

        public final void setId(String id) {
            this.id.set(id);
        }

        // preferences

        private final ObjectProperty<Preferences> preferences = new SimpleObjectProperty<>(this, "preferences");

        public final Preferences getPreferences() {
            return preferences.get();
        }

        /**
         * Stores a reference to a preferences object which can be used to persist the width and height of the
         * dialog across user sessions.
         *
         * @return the preferences where we want to store the bounds of the dialog after a resizing operation
         */
        public final ObjectProperty<Preferences> preferencesProperty() {
            return preferences;
        }

        public final void setPreferences(Preferences preferences) {
            this.preferences.set(preferences);
        }

        // pref width

        private final DoubleProperty prefWidth = new SimpleDoubleProperty(this, "prefWidth", Region.USE_COMPUTED_SIZE);

        public final double getPrefWidth() {
            return prefWidth.get();
        }

        public final DoubleProperty prefWidthProperty() {
            return prefWidth;
        }

        public final void setPrefWidth(double prefWidth) {
            this.prefWidth.set(prefWidth);
        }

        private final DoubleProperty prefHeight = new SimpleDoubleProperty(this, "prefHeight", Region.USE_COMPUTED_SIZE);

        public final double getPrefHeight() {
            return prefHeight.get();
        }

        public final DoubleProperty prefHeightProperty() {
            return prefHeight;
        }

        public final void setPrefHeight(double prefHeight) {
            this.prefHeight.set(prefHeight);
        }

        private final DoubleProperty minWidth = new SimpleDoubleProperty(this, "minWidth", Region.USE_COMPUTED_SIZE);

        public final double getMinWidth() {
            return minWidth.get();
        }

        public final DoubleProperty minWidthProperty() {
            return minWidth;
        }

        public final void setMinWidth(double minWidth) {
            this.minWidth.set(minWidth);
        }

        private final DoubleProperty minHeight = new SimpleDoubleProperty(this, "minHeight", Region.USE_COMPUTED_SIZE);

        public final double getMinHeight() {
            return minHeight.get();
        }

        public final DoubleProperty minHeightProperty() {
            return minHeight;
        }

        public final void setMinHeight(double minHeight) {
            this.minHeight.set(minHeight);
        }

        private final DoubleProperty maxWidth = new SimpleDoubleProperty(this, "maxWidth", Region.USE_COMPUTED_SIZE);

        public final double getMaxWidth() {
            return maxWidth.get();
        }

        public final DoubleProperty maxWidthProperty() {
            return maxWidth;
        }

        public final void setMaxWidth(double maxWidth) {
            this.maxWidth.set(maxWidth);
        }

        private final DoubleProperty maxHeight = new SimpleDoubleProperty(this, "maxHeight", Region.USE_COMPUTED_SIZE);

        public final double getMaxHeight() {
            return maxHeight.get();
        }

        public final DoubleProperty maxHeightProperty() {
            return maxHeight;
        }

        public final void setMaxHeight(double maxHeight) {
            this.maxHeight.set(maxHeight);
        }

        // resize callback

        private final ObjectProperty<BiConsumer<Double, Double>> onResize = new SimpleObjectProperty<>(this, "onResize");

        public final BiConsumer<Double, Double> getOnResize() {
            return onResize.get();
        }

        /**
         * A callback used to inform interested parties when the user changed the width or height of the
         * dialog.
         *
         * @return the callback / the consumer of the new width and height
         * @see #resizableProperty()
         */
        public final ObjectProperty<BiConsumer<Double, Double>> onResizeProperty() {
            return onResize;
        }

        public final void setOnResize(BiConsumer<Double, Double> onResize) {
            this.onResize.set(onResize);
        }

        // required

        private final BooleanProperty required = new SimpleBooleanProperty(this, "required", false);

        public final boolean isRequired() {
            return required.get();
        }

        /**
         * A flag used to signal to the framework that input is required in this dialog. This
         * is useful when showing things like text inputs where we automatically want to add
         * a validator.
         *
         * @return true if the dialog requires input
         */
        public final BooleanProperty requiredProperty() {
            return required;
        }

        public final void setRequired(boolean required) {
            this.required.set(required);
        }

        // show close button

        private final BooleanProperty showCloseButton = new SimpleBooleanProperty(this, "showCloseButton", true);

        public final boolean isShowCloseButton() {
            return showCloseButton.get();
        }

        /**
         * Determines if the dialog will make a "close" button available to the user. Whether this
         * property will be observed or not depends on the actual header implementation. The default
         * implementation of the header does contain a close button.
         *
         * @return true if the close button will be shown
         */
        public final BooleanProperty showCloseButtonProperty() {
            return showCloseButton;
        }

        public final void setShowCloseButton(boolean showCloseButton) {
            this.showCloseButton.set(showCloseButton);
        }

        // delay

        private final ObjectProperty<Duration> delay = new SimpleObjectProperty<>(this, "delay", Duration.ZERO);

        public final Duration getDelay() {
            return delay.get();
        }

        /**
         * A property that can be used to delay the appearance of the dialog inside the
         * dialog pane. This is very useful for dialogs that display a busy animation or
         * a progress indicator as usually you only want those to appear if a background
         * operation takes a while.
         *
         * @return the delay before the dialog becomes visible, default is ZERO
         */
        public final ObjectProperty<Duration> delayProperty() {
            return delay;
        }

        public final void setDelay(Duration delay) {
            this.delay.set(delay);
        }

        // padding

        private final BooleanProperty usingPadding = new SimpleBooleanProperty(this, "usingPadding", true);

        public final boolean isUsingPadding() {
            return usingPadding.get();
        }

        /**
         * Controls whether additional padding will be applied "around" the content node. The actual amount
         * of padding depends on the CSS rules.
         *
         * @return true if padding will be added
         * @see #setContent
         */
        public final BooleanProperty usingPaddingProperty() {
            return usingPadding;
        }

        public final void setUsingPadding(boolean usingPadding) {
            this.usingPadding.set(usingPadding);
        }

        // alignment

        private final ObjectProperty<Pos> contentAlignment = new SimpleObjectProperty<>(this, "contentAlignment", Pos.CENTER_LEFT);

        public final Pos getContentAlignment() {
            return contentAlignment.get();
        }

        /**
         * Determines how the content node is positioned within the content pane of the dialog. Default
         * is CENTER_LEFT.
         *
         * @return the position of the content
         */
        public final ObjectProperty<Pos> contentAlignmentProperty() {
            return contentAlignment;
        }

        public final void setContentAlignment(Pos contentAlignment) {
            this.contentAlignment.set(contentAlignment);
        }

        // value

        private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");

        public final T getValue() {
            return value.get();
        }

        /**
         * Stores the value entered or selected by the user in the dialog. If the dialog is, for
         * example, a text input dialog then the value will be the text entered into the text field
         * of the dialog.
         *
         * @return the user entered or selected value
         */
        public final ObjectProperty<T> valueProperty() {
            return value;
        }

        public final void setValue(T value) {
            this.value.set(value);
        }

        public void show() {
            pane.showDialog(this);
        }

        /**
         * Hides the dialog by cancelling it.
         */
        public void cancel() {
            pane.hideDialog(this);
            setValue(null);
            commit(ButtonType.CANCEL);
        }

        // on button pressed

        private Consumer<ButtonType> onButtonPressed;

        public Consumer<ButtonType> getOnButtonPressed() {
            return onButtonPressed;
        }

        /**
         * Specifies a consumer that gets invoked when the user presses on of the standard
         * dialog buttons. The invocation happens as part of the complete() method.
         *
         * @param onButtonPressed an optional consumer that gets invoked when the user finishes the dialog
         */
        public final void setOnButtonPressed(Consumer<ButtonType> onButtonPressed) {
            this.onButtonPressed = onButtonPressed;
        }

        // on commit

        private final ObjectProperty<Consumer<ButtonType>> onClose = new SimpleObjectProperty<>(this, "onClose", buttonType -> {
        });

        public final Consumer<ButtonType> getOnClose() {
            return onClose.get();
        }

        public final ObjectProperty<Consumer<ButtonType>> onCloseProperty() {
            return onClose;
        }

        public final void setOnClose(Consumer<ButtonType> onClose) {
            this.onClose.set(onClose);
        }

        /**
         * A method in fluent-api style that sets the given consumer and returns the dialog
         * again.
         *
         * @param onCommit the handler
         */
        public final Dialog<T> onClose(Consumer<ButtonType> onCommit) {
            Objects.requireNonNull(onCommit, "onCommit handler can not be null");
            setOnClose(onCommit);
            return this;
        }

        private void commit(ButtonType buttonType) {
            if (onButtonPressed != null) {
                onButtonPressed.accept(buttonType);
            }
            getOnClose().accept(buttonType);
        }

        // valid property (important to have default set to true)

        private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid", true);

        public final boolean isValid() {
            return valid.get();
        }

        /**
         * Determines whether the values entered in the dialog are currently valid and will
         * be accepted by the application. The validity is normally determines by the validator
         * object.
         *
         * @return true if the dialog fiels are currently valid
         * @see #validatorProperty()
         */
        public final BooleanProperty validProperty() {
            return valid;
        }

        public final void setValid(boolean valid) {
            this.valid.set(valid);
        }

        // button types

        private final ObservableList<ButtonType> buttonTypes = FXCollections.observableArrayList();

        public final ObservableList<ButtonType> getButtonTypes() {
            return buttonTypes;
        }

        // same width buttons

        private final BooleanProperty sameWidthButtons = new SimpleBooleanProperty(true);

        public final boolean isSameWidthButtons() {
            return sameWidthButtons.get();
        }

        /**
         * Determines if the buttons in the button bar will all have the same width or
         * each one will have its preferred width.
         *
         * @return true if all buttons should have the same width
         */
        public final BooleanProperty sameWidthButtonsProperty() {
            return sameWidthButtons;
        }

        public final void setSameWidthButtons(boolean sameWidthButtons) {
            this.sameWidthButtons.set(sameWidthButtons);
        }

        // maximize

        private final BooleanProperty maximize = new SimpleBooleanProperty();

        /**
         * If maximized the dialog will try to use as much width and height inside
         * the dialog pane as possible.
         *
         * @return true if the dialog will try to maximize itself
         */
        public final BooleanProperty maximizeProperty() {
            return maximize;
        }

        public final void setMaximize(boolean max) {
            maximize.set(max);
        }

        public final boolean isMaximize() {
            return maximize.get();
        }

        // content

        private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

        /**
         * Stores the content shown in the center of the dialog.
         *
         * @return the content node with the actual UI being displayed to the user
         */
        public final ObjectProperty<Node> contentProperty() {
            return content;
        }

        public final void setContent(Node content) {
            this.content.set(content);
        }

        public final Node getContent() {
            return content.get();
        }

        // extras

        private final ObjectProperty<Node> extras = new SimpleObjectProperty<>(this, "extras");

        public final Node getExtras() {
            return extras.get();
        }

        /**
         * An extra node that might be displayed in the header or the footer of the dialog. This
         * node will only be shown if custom header or footer factories make use of it. By default
         * this node will not be visible anywhere.
         *
         * @return an extra node to be placed in the header or footer
         * @see DialogPane#setHeaderFactory(Callback)
         * @see DialogPane#setFooterFactory(Callback)
         */
        public final ObjectProperty<Node> extrasProperty() {
            return extras;
        }

        public final void setExtras(Node extras) {
            this.extras.set(extras);
        }

        // title

        private final StringProperty title = new SimpleStringProperty(this, "title", "Dialog");

        public final StringProperty titleProperty() {
            return title;
        }

        /**
         * The title for the dialog. The default header factory will use this property
         * to populate a label with it.
         *
         * @return the dialog's title
         */
        public final String getTitle() {
            return title.get();
        }

        public final void setTitle(String title) {
            this.title.set(title);
        }

        // custom style

        private final ObservableList<String> styleClass = FXCollections.observableArrayList();

        /**
         * A list with additional styles that will be added to the content pane of the dialog.
         *
         * @return additional css styles
         */
        public final ObservableList<String> getStyleClass() {
            return styleClass;
        }


        // show header / title

        private final BooleanProperty showHeader = new SimpleBooleanProperty(this, "showHeader", true);

        /**
         * Determines if the dialog will show the title / header at the bottom or not.
         *
         * @return true if the dialog shows the button bar
         */
        public final BooleanProperty showHeaderProperty() {
            return showHeader;
        }

        public final boolean isShowHeader() {
            return showHeaderProperty().get();
        }

        public final void setShowHeader(boolean showHeader) {
            showHeaderProperty().set(showHeader);
        }

        // show footer / button bar

        private final BooleanProperty showFooter = new SimpleBooleanProperty(this, "showFooter", true);

        /**
         * Determines if the dialog will show the button bar / footer at the bottom or not.
         *
         * @return true if the dialog shows the button bar
         */
        public final BooleanProperty showFooterProperty() {
            return showFooter;
        }

        public final boolean isShowFooter() {
            return showFooterProperty().get();
        }

        public final void setShowFooter(boolean showFooter) {
            showFooterProperty().set(showFooter);
        }

        private final Map<ButtonType, Button> buttonMap = new HashMap<>();

        /**
         * Returns the button created for the given button type, but only if
         * such a button type was requested for the dialog.
         *
         * @param type the button type
         * @return the button
         * @see #getButtonTypes
         */
        public final Button getButton(ButtonType type) {
            return buttonMap.get(type);
        }

        // validator

        private final ObjectProperty<Validator> validator = new SimpleObjectProperty<>(this, "validator", new Validator());

        public final Validator getValidator() {
            return validator.get();
        }

        /**
         * A validator that can be used in the context of this dialog. Applications
         * can call {@link Validator#createCheck()} to add one or more checks which will
         * be validated once the user tries to commit the dialog.
         *
         * @return the dialog's validator
         */
        public final ObjectProperty<Validator> validatorProperty() {
            return validator;
        }

        public final void setValidator(Validator validator) {
            this.validator.set(validator);
        }

        // resizable

        private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable");

        public final boolean isResizable() {
            return resizable.get();
        }

        /**
         * Determines if the user will be able to interactively resize the dialog. The default is
         * false.
         *
         * @return true if the dialog can be resized (default is false)
         */
        public final BooleanProperty resizableProperty() {
            return resizable;
        }

        public final void setResizable(boolean resizable) {
            this.resizable.set(resizable);
        }
    }

    private class ContentPane extends StackPane {

        private final Dialog<?> dialog;

        private final ChangeListener<Node> focusListener = (o, oldOwner, newOwner) -> {
            if (newOwner != null && !isInsideDialogPane(newOwner.getParent()) && getScene() != null) {
                if (oldOwner != null && isInsideDialogPane(oldOwner.getParent())) {
                    oldOwner.requestFocus();
                } else {
                    requestFocus();
                }
            }
        };

        private final WeakChangeListener<Node> weakFocusListener = new WeakChangeListener<>(focusListener);

        public ContentPane(Dialog<?> dialog) {
            this.dialog = Objects.requireNonNull(dialog);

            prefWidthProperty().bindBidirectional(dialog.prefWidthProperty());
            prefHeightProperty().bindBidirectional(dialog.prefHeightProperty());

            minWidthProperty().bindBidirectional(dialog.minWidthProperty());
            minHeightProperty().bindBidirectional(dialog.minHeightProperty());

            maxWidthProperty().bindBidirectional(dialog.maxWidthProperty());
            maxHeightProperty().bindBidirectional(dialog.maxHeightProperty());

            ResizingBehaviour resizingSupport = ResizingBehaviour.install(this);
            resizingSupport.resizableProperty().bind(dialog.resizableProperty());
            resizingSupport.onResizeProperty().bind(dialog.onResizeProperty());

            setFocusTraversable(false);

            DialogPane shell = this.dialog.getDialogPane();

            Node header = getHeaderFactory().call(dialog);

            StackPane content = new StackPane();
            content.getStyleClass().add("content");

            if (dialog.isUsingPadding()) {
                content.getStyleClass().add("padding");
            }

            sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (oldScene != null) {
                    oldScene.focusOwnerProperty().removeListener(weakFocusListener);
                }
                if (newScene != null) {
                    newScene.focusOwnerProperty().addListener(weakFocusListener);
                }
            });

            VBox.setVgrow(content, Priority.ALWAYS);

            StackPane.setAlignment(this.dialog.getContent(), dialog.getContentAlignment());
            content.getChildren().setAll(this.dialog.getContent());

            boolean blankDialog = this.dialog.getType().equals(Type.BLANK);

            header.setVisible(!blankDialog && dialog.isShowHeader());
            header.setManaged(!blankDialog && dialog.isShowHeader());

            Node footer = getFooterFactory().call(dialog);
            VBox.setVgrow(footer, Priority.NEVER);
            footer.setVisible(!blankDialog && dialog.isShowFooter());
            footer.setManaged(!blankDialog && dialog.isShowFooter());

            getStyleClass().setAll("content-pane");
            getStyleClass().addAll(this.dialog.getStyleClass());

            VBox box = new VBox();
            box.getStyleClass().add("vbox");
            box.setFillWidth(true);

            box.getChildren().setAll(header, content, footer);

            GlassPane glassPane = new GlassPane();
            glassPane.hideProperty().bind(blocked.not());
            glassPane.fadeInOutProperty().bind(shell.fadeInOutProperty());

            getChildren().addAll(box, glassPane);
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.VERTICAL;
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
    }

    /**
     * The default header implementation for dialogs managed by the {@link DialogPane}.
     *
     * @see DialogPane#setHeaderFactory(Callback)
     */
    public static class DialogHeader extends StackPane {

        /**
         * Constructs a new header for the given dialog.
         *
         * @param dialog the model object defining the dialog
         */
        public DialogHeader(Dialog<?> dialog) {
            setAlignment(Pos.CENTER);
            getStyleClass().add("header");

            Label dialogTitle = new Label("Dialog");
            dialogTitle.setMaxWidth(Double.MAX_VALUE);
            dialogTitle.getStyleClass().add("title");
            dialogTitle.textProperty().bind(dialog.titleProperty());
            VBox.setVgrow(dialogTitle, Priority.NEVER);

            ImageView dialogIcon = new ImageView();
            dialogIcon.getStyleClass().addAll("icon");
            dialogIcon.visibleProperty().bind(showIconProperty());
            dialogIcon.managedProperty().bind(showIconProperty());

            VBox vBox = new VBox(dialogIcon, dialogTitle);
            vBox.getStyleClass().add("title-and-icon-box");

            // close icon / button support
            FontIcon fontIcon = new FontIcon(MaterialDesign.MDI_CLOSE);

            Button closeButton = new Button();
            closeButton.setGraphic(fontIcon);
            closeButton.setFocusTraversable(false);
            closeButton.setAlignment(Pos.CENTER);
            closeButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            closeButton.getStyleClass().add("close-button");
            closeButton.visibleProperty().bind(dialog.showCloseButtonProperty().and(showCloseButtonProperty()));
            closeButton.managedProperty().bind(dialog.showCloseButtonProperty().and(showCloseButtonProperty()));
            closeButton.setOnAction(evt -> dialog.cancel());
            StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

            getChildren().setAll(vBox, closeButton);
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

        private final BooleanProperty showIcon = new SimpleBooleanProperty(this, "showIcon", true);

        public final boolean isShowIcon() {
            return showIcon.get();
        }

        public final BooleanProperty showIconProperty() {
            return showIcon;
        }

        public final void setShowIcon(boolean showIcon) {
            this.showIcon.set(showIcon);
        }
    }

    /**
     * The default footer / button bar implementation for dialogs managed by the {@link DialogPane}.
     *
     * @see DialogPane#setHeaderFactory(Callback)
     */
    public static class DialogButtonBar extends ButtonBar {

        private final Dialog<?> dialog;

        /**
         * Constructs a new footer / button bar for the given dialog.
         *
         * @param dialog the model object defining the dialog
         */
        public DialogButtonBar(Dialog<?> dialog) {
            this.dialog = dialog;

            // Setting the skin eagerly to make sure the focus does not
            // switch to one of the buttons in the button bar when it
            // should stay with a node shown by the dialog.
            setSkin(new ButtonBarSkin(this));

            getStyleClass().add("footer");

            if (System.getProperty("os.name").startsWith("Mac")) {
                setButtonOrder("L_NCYOAHE+U+FBIX_R");
            } else if (System.getProperty("os.name").startsWith("Windows")) {
                setButtonOrder("L_YNOCAHE+U+FBXI_R");
            } else {
                setButtonOrder("L_HENYCOA+U+FBIX_R");
            }

            if (!dialog.getType().equals(Type.BLANK)) {
                createButtons();
            }
        }

        /**
         * Creates all buttons as defined by {@link Dialog#getButtonTypes()}.
         */
        protected void createButtons() {
            getButtons().clear();

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

                            // some buttons will trigger dialog validation should a validator exist
                            if (buttonData != null && isTriggeringValidation(buttonData)) {
                                Validator validator = dialog.getValidator();
                                if (validator != null) {
                                    if (!validator.validate()) {
                                        // the dialog is not valid, yet, so do not close / finish it
                                        return;
                                    }
                                }
                            }

                            dialog.getDialogPane().hideDialog(dialog);
                            dialog.commit(buttonType);
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

                getButtons().add(button);
            }
        }

        /**
         * Determines whether validation should be run for the given button data / button type.
         *
         * @param data the button data / button type to check
         */
        protected boolean isTriggeringValidation(ButtonData data) {
            return switch (data) {
                case LEFT -> true;
                case RIGHT -> true;
                case HELP -> false;
                case HELP_2 -> false;
                case YES -> true;
                case NO -> false;
                case NEXT_FORWARD -> true;
                case BACK_PREVIOUS -> true;
                case FINISH -> true;
                case APPLY -> true;
                case CANCEL_CLOSE -> false;
                case OK_DONE -> true;
                case OTHER -> true;
                case BIG_GAP -> true;
                case SMALL_GAP -> true;
            };
        }

        /**
         * Creates an individual button based on the button type definition passed to it.
         *
         * @param buttonType the type of the button to create
         * @return the button for the given type
         */
        protected Button createButton(ButtonType buttonType) {
            String text = buttonType.getText();
            StringConverter<ButtonType> converter = dialog.getDialogPane().getConverter();
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

    private static class BusyIndicator extends CircularProgressIndicator {

        public BusyIndicator() {
            getStyleClass().add("dialog-pane-busy-indicator");
        }

        public void stop() {
            setProgress(0);
        }

        public void start() {
            setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        }
    }

    private static class CircularProgressIndicator extends Region {

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
            if (isRunning) {
                return;
            }

            manageNode(indeterminatePane, true);
            manageNode(progressPane, false);

            timeline.play();
            indeterminatePaneRotation.play();
            isRunning = true;
            indeterminate.set(true);
        }

        private void stopIndeterminate() {
            if (!isRunning) {
                return;
            }

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

    /**
     * The default resize handler is used to persist the dialog's width and height after the
     * user performed a resize operation on the dialog. Both values are stored via the
     * java.util.prefs.Preferences API.
     */
    public static class DefaultResizeHandler implements BiConsumer<Double, Double> {

        private final Dialog<?> dialog;

        public DefaultResizeHandler(Dialog<?> dialog) {
            this.dialog = Objects.requireNonNull(dialog, "dialog can not be null");
        }

        @Override
        public void accept(Double width, Double height) {
            Preferences preferences = dialog.getPreferences();
            if (preferences != null) {
                preferences.put("width", width.toString());
                preferences.put("height", height.toString());
            }
        }
    }
}
