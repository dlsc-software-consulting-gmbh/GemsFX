package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.infocenter.Notification.OnClickBehaviour;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Callback;

import java.util.Objects;

/**
 * Actions can be attached to notifications via {@link Notification#getActions()}. The UI
 * will create a control (button or menu item) for each action so that the user can trigger
 * the action. This allows for a quick response on incoming notifications, e.g. "open the
 * mail".
 *
 * @param <T> the type of the user object of the notification
 */
public class NotificationAction<T> {

    private Callback<Notification<T>, OnClickBehaviour> onAction;

    /**
     * Constructs a new action with the given text. The default action behaviour
     * will be to remove the notification from its group. No additional application
     * logic will be executed.
     *
     * @param text the text of the action as shown in the UI
     */
    public NotificationAction(String text) {
        this(text, notification -> OnClickBehaviour.REMOVE);
    }

    /**
     * Constructs a new action with the given text.
     *
     * @param text the text of the action as shown in the UI
     * @param onAction the application logic to perform when the user selects the action
     */
    public NotificationAction(String text, Callback<Notification<T>, OnClickBehaviour> onAction) {
        setText(Objects.requireNonNull(text));
        this.onAction = onAction;
    }

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public final String getText() {
        return text.get();
    }

    /**
     * The text that will be used in the UI when creating controls that will
     * trigger the action.
     *
     * @return the text for the action
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    /**
     * Returns the callback that will be invoked when the user selects the action in the UI.
     *
     * @return the callback to invoke when the user triggers the action
     */
    public final Callback<Notification<T>, OnClickBehaviour> getOnAction() {
        return onAction;
    }

    /**
     * Sets the callback that will be invoked when the user selects the action in the UI.
     *
     * @param onAction the callback to invoke when the user triggers the action
     */
    public final void setOnAction(Callback<Notification<T>, OnClickBehaviour> onAction) {
        this.onAction = onAction;
    }
}