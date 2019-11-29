package com.dlsc.gemsfx.keyboard;

import com.dlsc.gemsfx.keyboard.Keyboard.SpecialKey.Type;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.Locale;

/**
 * A pane used to display a content node and a keyboard. The keyboard is always shown
 * at the bottom of the pane. It will slide in whenever the keyboard lookup strategy finds a
 * keyboard that it wants to display for the currently focused node. The pane ensures that the
 * focused node will not be overlapped / hidden by the keyboard. If necessary the content node
 * will be translated with a negative value on the y-axis (as in "moving up").
 *
 * @see #setKeyboardLookupStrategy(Callback)
 */
public class KeyboardPane extends Pane {

    private final KeyboardView keyboardView = new KeyboardView();

    private final InvalidationListener focusListener = it -> maybeShowKeyboard();

    private final WeakInvalidationListener weakFocusListener = new WeakInvalidationListener(focusListener);

    /**
     * Constructs a new keyboard pane.
     */
    public KeyboardPane() {

        /*
         * Clipping as otherwise either content node or the keyboard might stick out
         * of the pane.
         */
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        keyboardView.setManaged(false);
        keyboardView.setVisible(false);
        keyboardView.setOnClose(() -> hideKeyboard());
        keyboardView.selectedKeyboardProperty().addListener(it -> requestLayout());
        keyboardView.setSpecialKeyCallback(specialKey -> {
            if (specialKey.getType().equals(Type.ENTER)) {
                final Callback<Node, Boolean> autoCloseStrategy = getAutoCloseStrategy();
                if (autoCloseStrategy != null) {
                    if (autoCloseStrategy.call(getScene().getFocusOwner())) {
                        hideKeyboard();
                    }
                }
            }
        });

        contentProperty().addListener(it -> updateView());
        updateView();

        focusOwnerProperty().addListener(focusListener);

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            focusOwner.unbind();

            if (newScene != null) {
                focusOwner.bind(newScene.focusOwnerProperty());
            }
        });

        keyboardVisibility.addListener(it -> requestLayout());

        setKeyboardLookupStrategy(node -> {
            if (node instanceof TextField || node instanceof PasswordField || node instanceof TextArea) {
                Keyboard[] keyboards = new Keyboard[getDefaultKeyboards().size()];
                return getDefaultKeyboards().toArray(keyboards);
            }

            return null;
        });

        getDefaultKeyboards().add(keyboardView.loadKeyboard(new Locale("fi", "FI")));
        getDefaultKeyboards().add(keyboardView.loadKeyboard(Locale.GERMAN));
        getDefaultKeyboards().add(keyboardView.loadKeyboard(Locale.US));
    }

    private void hideKeyboard() {
        requestFocus();
    }

    public KeyboardPane(Node content) {
        this();
        setContent(content);
    }

    /**
     * Returns the keyboard view.
     *
     * @return the keyboard view
     */
    public final KeyboardView getKeyboardView() {
        return keyboardView;
    }

    // default keyboards

    private final ListProperty<Keyboard> defaultKeyboards = new SimpleListProperty<>(this, "defaultKeyboards", FXCollections.observableArrayList());

    /**
     * A list of keyboards that will be returned as the default keyboards by the
     * standard keyboard lookup strategy.
     *
     * @return the list of default keyboards
     * @see #keyboardLookupStrategyProperty()
     */
    public ListProperty<Keyboard> defaultKeyboardsProperty() {
        return defaultKeyboards;
    }

    public ObservableList<Keyboard> getDefaultKeyboards() {
        return defaultKeyboards.get();
    }

    public void setDefaultKeyboards(ObservableList<Keyboard> defaultKeyboards) {
        this.defaultKeyboards.set(defaultKeyboards);
    }

    private final ObjectProperty<Callback<Node, Boolean>> autoCloseStrategy = new SimpleObjectProperty<>(this, "autoCloseStrategy", node -> false);

    /**
     * A callback used to determine if the keyboard should automatically hide when the user pressed the enter
     * key.
     *
     * @return the auto close strategy
     */
    public ObjectProperty<Callback<Node, Boolean>> autoCloseStrategyProperty() {
        return autoCloseStrategy;
    }

    public Callback<Node, Boolean> getAutoCloseStrategy() {
        return autoCloseStrategy.get();
    }

    public void setAutoCloseStrategy(Callback<Node, Boolean> autoCloseStrategy) {
        this.autoCloseStrategy.set(autoCloseStrategy);
    }

    private final ObjectProperty<Callback<Node, Keyboard[]>> keyboardLookupStrategy = new SimpleObjectProperty<>(this, "keyboardLookupStrategy");

    /**
     * The lookup strategy can be used to determine which keyboards to return for the
     * currently focused node.
     */
    public ObjectProperty<Callback<Node, Keyboard[]>> keyboardLookupStrategyProperty() {
        return keyboardLookupStrategy;
    }

    public Callback<Node, Keyboard[]> getKeyboardLookupStrategy() {
        return keyboardLookupStrategy.get();
    }

    public void setKeyboardLookupStrategy(Callback<Node, Keyboard[]> keyboardLookupStrategy) {
        this.keyboardLookupStrategy.set(keyboardLookupStrategy);
    }

    private Timeline timeline;

    private final ReadOnlyObjectWrapper<Node> focusOwner = new ReadOnlyObjectWrapper<>(this, "focusOwner");

    public Node getFocusOwner() {
        return focusOwner.get();
    }

    /**
     * A read-only property which gets managed by the keyboard pane itself.
     *
     * @return the focus owner
     */
    public ReadOnlyObjectProperty<Node> focusOwnerProperty() {
        return focusOwner.getReadOnlyProperty();
    }

    private void maybeShowKeyboard() {
        final Scene scene = getScene();

        if (scene != null) {

            if (timeline != null) {
                timeline.stop();
            }

            final Node focusOwner = getFocusOwner();

            if (focusOwner == null) {
                resetTranslate();
                return;
            }

            final Callback<Node, Keyboard[]> keyboardLookupStrategy = getKeyboardLookupStrategy();
            final Keyboard[] keyboards = keyboardLookupStrategy.call(focusOwner);

            if (keyboards != null && keyboards.length > 0) {
                keyboardView.getKeyboards().setAll(keyboards);
                keyboardView.setVisible(true);
                keyboardView.setManaged(true);

                KeyValue visibility = new KeyValue(keyboardVisibility, 1, Interpolator.EASE_OUT);
                KeyValue translate = new KeyValue(getContent().translateYProperty(), getTranslate(), Interpolator.EASE_OUT);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(200), visibility, translate);
                timeline = new Timeline(keyFrame);
                timeline.play();

            } else {

                resetTranslate();

                if (keyboardVisibility.get() > 0) {
                    KeyValue visibility = new KeyValue(keyboardVisibility, 0, Interpolator.EASE_OUT);
                    KeyValue translate = new KeyValue(getContent().translateYProperty(), 0, Interpolator.EASE_OUT);
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(200 * keyboardVisibility.get()), visibility, translate);
                    timeline = new Timeline(keyFrame);
                    timeline.setOnFinished(it -> {
                        keyboardView.setManaged(false);
                        keyboardView.setVisible(false);
                    });
                    timeline.play();
                }
            }
        }
    }

    private double getTranslate() {
        double translate = 0;

        final Node focusOwner = getScene().getFocusOwner();
        if (focusOwner != null && focusOwner != this) {
            final Bounds bounds = focusOwner.localToScene(focusOwner.getBoundsInLocal());
            keyboardView.applyCss();
            double keyboardHeight = keyboardView.prefHeight(-1);
            double minDistance = getMinDistanceToKeyboard();
            if (bounds.getMinY() + bounds.getHeight() - getContent().getTranslateY() > getHeight() - keyboardHeight - minDistance) {
                double goodLocation = getHeight() - keyboardHeight - bounds.getHeight() - minDistance;
                translate = goodLocation - bounds.getMinY() + getContent().getTranslateY();
            }
        }

        return translate;
    }

    private void resetTranslate() {
        KeyValue translate = new KeyValue(getContent().translateYProperty(), 0, Interpolator.EASE_OUT);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200 * (1 - keyboardVisibility.get())), translate);
        timeline = new Timeline(keyFrame);
        timeline.play();
    }

    private final DoubleProperty keyboardVisibility = new SimpleDoubleProperty(this, "keyboardVisibility", 0);

    private void updateView() {
        if (getContent() != null) {
            getChildren().setAll(getContent(), keyboardView);
        } else {
            getChildren().setAll(keyboardView);
        }
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        final Node content = getContent();
        double contentWidth = getWidth() - getInsets().getLeft() - getInsets().getRight();

        if (content != null) {
            content.resizeRelocate(insets.getLeft(), insets.getTop(), contentWidth, getHeight() - getInsets().getTop() - getInsets().getBottom());
        }

        double ph = keyboardView.prefHeight(contentWidth);
        keyboardView.resizeRelocate(insets.getLeft(), getHeight() - getInsets().getBottom() - ph * keyboardVisibility.get(), contentWidth, ph);
    }

    private final DoubleProperty minDistanceToKeyboard = new SimpleDoubleProperty(this, "minDistanceToKeyboard", 20);

    /**
     * A property that allows the application to define the minimum distance between the keyboard
     * and the currently focused node.
     *
     * @return the minimum distance between keyboard and input field / focused node
     */
    public final DoubleProperty minDistanceToKeyboardProperty() {
        return minDistanceToKeyboard;
    }

    public final double getMinDistanceToKeyboard() {
        return minDistanceToKeyboard.get();
    }

    public final void setMinDistanceToKeyboard(double minDistanceToKeyboard) {
        this.minDistanceToKeyboard.set(minDistanceToKeyboard);
    }

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    /**
     * The primary content to be shown by the pane.
     *
     * @return the content node
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return content.get();
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }
}
