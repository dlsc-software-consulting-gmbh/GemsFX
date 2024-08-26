package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TextViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A text view that allows you to display multiline text and supports the selection of
 * text, which can then be copied to the clipboard. The user can copy the selected text
 * via the OS-specific shortcut for copying content (for example, CTRL-C on Windows or
 * Command-C on Mac). Additionally, a context menu is available for copying.
 * <p>
 *     The user can select text by pressing and dragging the mouse, or by double clicking
 *     on a word. A triple click selects an entire paragraph.
 * </p>
 */
public class TextView extends Control {

    /**
     * Constructs a new text view.
     */
    public TextView() {
        getStyleClass().add("text-view");

        setFocusTraversable(false);

        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.getKey().equals("selected.text")) {
                selectedText.set((String) change.getValueAdded());
            }
        });

        setOnContextMenuRequested(evt -> {
            if (getContextMenu() == null) {
                MenuItem copySelectionItem = new MenuItem("Copy Selection");
                copySelectionItem.setOnAction(e -> copySelection());

                MenuItem copyAllItem = new MenuItem("Copy All");
                copyAllItem.setOnAction(e -> copyAll());

                ContextMenu contextMenu = new ContextMenu(copyAllItem, copySelectionItem);
                setContextMenu(contextMenu);
                contextMenu.show(this, evt.getScreenX(), evt.getScreenY());
            }
        });
    }

    /**
     * Constructs a new text view.
     *
     * @param text The initial text
     */
    public TextView(String text) {
        this();
        setText(text);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(TextView.class.getResource("text-view.css")).toExternalForm();
    }

    /**
     * Copy the text to the clipboard. This method is intentionally non-final so
     * that applications can implement their own logic.
     */
    public void copySelection() {
        doCopy(getSelectedText());
    }

    /**
     * Copy the entire text to the clipboard. This method is intentionally non-final so
     * that applications can implement their own logic.
     */
    public void copyAll() {
        doCopy(getText());
    }

    private void doCopy(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    // text

    private final StringProperty text = new SimpleStringProperty(this, "text");

    /**
     * Stores the text displayed by the view.
     *
     * @return the text shown by the control
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return textProperty().get();
    }

    public final void setText(String text) {
        textProperty().set(text);
    }

    // selected text

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper(this, "selectedText");

    /**
     * String property used to save selected skin text
     *
     * @return The String property.
     */
    public final String getSelectedText() {
        return selectedText.get();
    }

    public final ReadOnlyStringProperty selectedTextProperty() {
        return selectedText.getReadOnlyProperty();
    }

    // highlight fill

    /**
     * The fill to use for the text when highlighted.
     */
    private final ObjectProperty<Paint> highlightFill = new StyleableObjectProperty<>(Color.BLUE) {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "highlightFill";
        }

        @Override
        public CssMetaData<TextView, Paint> getCssMetaData() {
            return StyleableProperties.HIGHLIGHT_FILL;
        }
    };

    /**
     * The fill {@code Paint} used for the background of selected text.
     *
     * @param value the highlight fill
     */
    public final void setHighlightFill(Paint value) {
        highlightFill.set(value);
    }

    public final Paint getHighlightFill() {
        return highlightFill.get();
    }

    public final ObjectProperty<Paint> highlightFillProperty() {
        return highlightFill;
    }

    // highlight stroke

    /**
     * The fill to use for the text when highlighted.
     */
    private final ObjectProperty<Paint> highlightStroke = new StyleableObjectProperty<>(Color.TRANSPARENT) {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "highlightStroke";
        }

        @Override
        public CssMetaData<TextView, Paint> getCssMetaData() {
            return StyleableProperties.HIGHLIGHT_STROKE;
        }
    };

    /**
     * The fill {@code Paint} used for the background of selected text.
     *
     * @param value the highlight fill
     */
    public final void setHighlightStroke(Paint value) {
        highlightStroke.set(value);
    }

    public final Paint getHighlightStroke() {
        return highlightStroke.get();
    }

    public final ObjectProperty<Paint> highlightStrokeProperty() {
        return highlightStroke;
    }

    // highlight text fill

    /**
     * The fill {@code Paint} used for the foreground of selected text.
     */
    private final ObjectProperty<Paint> highlightTextFill = new StyleableObjectProperty<>(Color.WHITE) {

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "highlightTextFill";
        }

        @Override
        public CssMetaData<TextView, Paint> getCssMetaData() {
            return StyleableProperties.HIGHLIGHT_TEXT_FILL;
        }
    };

    /**
     * The fill {@code Paint} used for the foreground of selected text.
     *
     * @param value the highlight text fill
     */
    public final void setHighlightTextFill(Paint value) {
        highlightTextFill.set(value);
    }

    public final Paint getHighlightTextFill() {
        return highlightTextFill.get();
    }

    public final ObjectProperty<Paint> highlightTextFillProperty() {
        return highlightTextFill;
    }

    private static class StyleableProperties {

        private static final CssMetaData<TextView, Paint> HIGHLIGHT_TEXT_FILL = new CssMetaData<>(
                "-fx-highlight-text-fill", PaintConverter.getInstance(), Color.TRANSPARENT) {

            @Override
            public boolean isSettable(TextView c) {
                return !c.highlightTextFill.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<Paint> getStyleableProperty(TextView n) {
                return (StyleableProperty<Paint>) n.highlightTextFill;
            }
        };

        private static final CssMetaData<TextView, Paint> HIGHLIGHT_FILL = new CssMetaData<>(
                "-fx-highlight-fill", PaintConverter.getInstance(), Color.TRANSPARENT
        ) {

            @Override
            public boolean isSettable(TextView c) {
                return !c.highlightFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextView c) {
                return (StyleableProperty<Paint>) c.highlightFillProperty();
            }
        };

        private static final CssMetaData<TextView, Paint> HIGHLIGHT_STROKE = new CssMetaData<>(
                "-fx-highlight-stroke", PaintConverter.getInstance(), Color.TRANSPARENT
        ) {

            @Override
            public boolean isSettable(TextView c) {
                return !c.highlightStroke.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextView c) {
                return (StyleableProperty<Paint>) c.highlightStroke;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(HIGHLIGHT_FILL);
            styleables.add(HIGHLIGHT_STROKE);
            styleables.add(HIGHLIGHT_TEXT_FILL);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return TextView.StyleableProperties.STYLEABLES;
    }
}