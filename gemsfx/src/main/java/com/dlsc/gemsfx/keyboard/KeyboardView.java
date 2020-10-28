package com.dlsc.gemsfx.keyboard;

import com.dlsc.gemsfx.keyboard.Keyboard.SpecialKey;
import com.dlsc.gemsfx.keyboard.Keyboard.SpecialKey.Type;
import com.dlsc.gemsfx.skins.KeyboardViewSkin;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A custom control that displays a keyboard for touch-based text editing.
 */
public class KeyboardView extends Control {

    private static final Logger LOG = Logger.getLogger(KeyboardView.class.getName());

    /**
     * Constructs a new keyboard view.
     */
    public KeyboardView() {
        getStyleClass().add("keyboard");

        setMinHeight(Region.USE_PREF_SIZE);

        setOnClose(() -> new Alert(Alert.AlertType.WARNING, "Keyboard close handler missing.", ButtonType.CLOSE).show());

        setSpecialKeyFactory(type -> {
            switch (type) {
                case SPACE:
                    return new Label("");
                case SHIFT:
                    return new FontIcon(MaterialDesign.MDI_APPLE_KEYBOARD_SHIFT);
                case ENTER:
                    return new FontIcon(MaterialDesign.MDI_KEYBOARD_RETURN);
                case TAB:
                    return new FontIcon(MaterialDesign.MDI_KEYBOARD_TAB);
                case BACKSPACE:
                    return new FontIcon(MaterialDesign.MDI_KEYBOARD_BACKSPACE);
                case CAPS:
                    return new FontIcon(MaterialDesign.MDI_APPLE_KEYBOARD_CAPS);
                case KEYBOARDS:
                    return new FontIcon(MaterialDesign.MDI_EARTH);
                case SPECIAL_CHARACTERS:
                    Label label = new Label(".?123");
                    label.setTextOverrun(OverrunStyle.CLIP);
                    return label;
                case PLUS:
                    return new Label("+");
                case MINUS:
                    return new Label("-");
                case DIVIDE:
                    return new Label("/");
                case MULTIPLY:
                    return new Label("*");
                case EQUALS:
                    return new Label("=");
                case MICROPHONE:
                    return new FontIcon(MaterialDesign.MDI_MICROPHONE);
                case HIDE:
                    return new FontIcon(MaterialDesign.MDI_KEYBOARD_CLOSE);
                case UP:
                    return new FontIcon(MaterialDesign.MDI_ARROW_UP);
                case DOWN:
                    return new FontIcon(MaterialDesign.MDI_ARROW_DOWN);
                case LEFT:
                    return new FontIcon(MaterialDesign.MDI_ARROW_LEFT);
                case RIGHT:
                    return new FontIcon(MaterialDesign.MDI_ARROW_RIGHT);
                default:
                    return new Label("");
            }
        });

        getStylesheets().add(KeyboardView.class.getResource("keyboard.css").toExternalForm());

        darkModeProperty().addListener(it -> {
            if (isDarkMode()) {
                getStylesheets().add(KeyboardView.class.getResource("keyboard-dark.css").toExternalForm());
            } else {
                getStylesheets().remove(KeyboardView.class.getResource("keyboard-dark.css").toExternalForm());
            }
        });

        ObservableList<Keyboard> keyboards = getKeyboards();
        keyboards.addListener((Observable it) -> {
            Keyboard selectedKeyboard = getSelectedKeyboard();
            if (!keyboards.isEmpty() && (selectedKeyboard == null || !keyboards.contains(selectedKeyboard))) {
                setSelectedKeyboard(keyboards.get(0));
            }
        });

        getKeyboards().add(loadKeyboard(Locale.getDefault()));

        widthProperty().addListener(it -> System.out.println("ph kv = " + prefHeight(-1)));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KeyboardViewSkin(this);
    }

    /**
     * Loads a keyboard based on the given locale. This method will look for XML files with
     * a filename that starts with 'keyboard-full-' and that adds the country and the language of
     * the locale, e.g. 'keyboard-full-us-en.xml'. This method will return the "full US" keyboard if
     * it can not find one specific for the given locale.
     *
     * @param locale the locale for which to load a keyboard
     * @return the keyboard for the given locale, the US keyboard as a fallback
     */
    public final Keyboard loadKeyboard(Locale locale) {
        String country = locale.getCountry().toLowerCase();
        String language = locale.getLanguage().toLowerCase();

        Keyboard keyboard = null;
        if (country != null && StringUtils.isNotBlank(country) && StringUtils.isNotBlank(language)) {
            keyboard = loadKeyboard("keyboard-full-" + country + "-" + language + ".xml");
        }

        if (keyboard == null && StringUtils.isNotBlank(country)) {
            keyboard = loadKeyboard("keyboard-full-" + country + ".xml");
        }

        if (keyboard == null && StringUtils.isNotBlank(language)) {
            keyboard = loadKeyboard("keyboard-full-" + language + ".xml");
        }

        if (keyboard == null) {
            keyboard = loadKeyboard("keyboard-full-us.xml");
        }

        return keyboard;
    }

    private Keyboard loadKeyboard(String fileName) {
        try (InputStream in = KeyboardPane.class.getResourceAsStream(fileName)) {
            if (in != null) {
                return loadKeyboard(in);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "error when trying to load keyboard, file name = " + fileName, e);
        }

        return null;
    }

    /**
     * Loads a keyboard via the given input stream. The stream has to return the content
     * of a keyboard map xml configuration file.
     *
     * @param in the input stream
     * @return a fully configured keyboard
     * @throws JAXBException if the unmarshaller detects an issue
     */
    public Keyboard loadKeyboard(InputStream in) throws JAXBException {
        Objects.requireNonNull(in, "input stream for keyboard map xml file can not be null");
        JAXBContext jc = JAXBContext.newInstance(Keyboard.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Keyboard) unmarshaller.unmarshal(in);
    }

    // dark mode

    private final BooleanProperty darkMode = new SimpleBooleanProperty(this, "darkMode", false);

    /**
     * Controls whether the keyboard will apply dark styling / load the "dark" CSS file on top
     * of the standard user agent stylesheet.
     *
     * @return true if the keyboard should be shown using a dark color theme
     */
    public final BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public final boolean isDarkMode() {
        return darkMode.get();
    }

    public final void setDarkMode(boolean darkMode) {
        this.darkMode.set(darkMode);
    }

    // close callback

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");

    /**
     * A runnable that will be executed when the user types on the special key of type {@link Type#HIDE}.
     *
     * @return the "on close" runnable
     */
    public final ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public final Runnable getOnClose() {
        return onClose.get();
    }

    public final void setOnClose(Runnable onClose) {
        this.onClose.set(onClose);
    }

    public enum Mode {
        STANDARD,
        SHIFT,
        CAPS,
        SYMBOLS
    }

    // mode support

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(this, "mode", Mode.STANDARD);

    /**
     * Stores the current mode that the keyboard is in: standard, caps, shift, symbol. Keys will
     * behave differently based on the mode.
     *
     * @return the currently active mode
     */
    public final ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public final Mode getMode() {
        return mode.get();
    }

    public final void setMode(Mode mode) {
        this.mode.set(mode);
    }

    // keyboards

    private final ListProperty<Keyboard> keyboards = new SimpleListProperty<>(this, "keyboards", FXCollections.observableArrayList());

    /**
     * Stores the list of keyboards that this keyboard view can support. The keyboard
     * will allow the user to switch between these keyboards on-the-fly.
     *
     * @return the list of available keyboards
     */
    public final ListProperty<Keyboard> keyboardsProperty() {
        return keyboards;
    }

    public final ObservableList<Keyboard> getKeyboards() {
        return keyboards.get();
    }

    public final void setKeyboards(ObservableList<Keyboard> keyboards) {
        this.keyboards.set(keyboards);
    }

    // selected keyboard support

    private final ObjectProperty<Keyboard> selectedKeyboard = new SimpleObjectProperty<>(this, "selectedKeyboard");

    /**
     * The currently selected keyboard. This is one of the keyboards that can be found
     * in {@link #keyboardsProperty()}.
     *
     * @return the currently / the active keyboard
     */
    public final ObjectProperty<Keyboard> selectedKeyboardProperty() {
        return selectedKeyboard;
    }

    public final Keyboard getSelectedKeyboard() {
        return selectedKeyboard.get();
    }

    public final void setSelectedKeyboard(Keyboard selectedKeyboard) {
        this.selectedKeyboard.set(selectedKeyboard);
    }

    // special key callback

    private final ObjectProperty<Consumer<SpecialKey>> specialKeyCallback = new SimpleObjectProperty<>(this, "specialKeyCallback");

    /**
     * A callback that will be invoked for any special key pressed by the user.
     *
     * @return a consumer for the special key
     */
    public ObjectProperty<Consumer<SpecialKey>> specialKeyCallbackProperty() {
        return specialKeyCallback;
    }

    public Consumer<SpecialKey> getSpecialKeyCallback() {
        return specialKeyCallback.get();
    }

    public void setSpecialKeyCallback(Consumer<SpecialKey> specialKeyCallback) {
        this.specialKeyCallback.set(specialKeyCallback);
    }

    // special key factory

    private final ObjectProperty<Callback<Type, Node>> specialKeyFactory = new SimpleObjectProperty<>(this, "specialKeyFactory");

    /**
     * A factory used to create the content nodes for special keys. Often the content of
     * a special key is either a label or an icon (e.g. the shift key displays an "arrow up"
     * icon).
     *
     * @return the special key content node factory
     */
    public final ObjectProperty<Callback<Type, Node>> specialKeyFactoryProperty() {
        return specialKeyFactory;
    }

    public final Callback<Type, Node> getSpecialKeyFactory() {
        return specialKeyFactory.get();
    }

    public final void setSpecialKeyFactory(Callback<Type, Node> specialKeyFactory) {
        this.specialKeyFactory.set(specialKeyFactory);
    }

    // extra keys popover delay

    private final LongProperty extraKeysPopOverDelay = new SimpleLongProperty(this, "extraKeysPopoverDelay", 666);

    /**
     * A long value used to delay the appearance of the popover that displays the list of
     * extra keys that the user can select from for certain keys.
     *
     * @return the extra keys popover delay in milliseconds
     */
    public final LongProperty extraKeysPopOverDelayProperty() {
        return extraKeysPopOverDelay;
    }

    public final long getExtraKeysPopOverDelay() {
        return extraKeysPopOverDelay.get();
    }

    public final void setExtraKeysPopOverDelay(long extraKeysPopOverDelay) {
        this.extraKeysPopOverDelay.set(extraKeysPopOverDelay);
    }
}