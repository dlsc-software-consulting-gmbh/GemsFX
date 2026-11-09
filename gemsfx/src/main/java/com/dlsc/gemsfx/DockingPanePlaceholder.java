package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * An empty-workspace illustration with optional text displayed beneath it.
 * Used as the default {@link DockingPane#placeholderProperty() DockingPane placeholder}.
 */
public class DockingPanePlaceholder extends VBox {

    /**
     * Creates a placeholder with an illustration and localized default text.
     */
    public DockingPanePlaceholder() {
        this(ResourceBundleManager.getString(ResourceBundleManager.BundleType.DOCKING_PANE,
                "placeholder.text", "Drag views here to dock them."));
    }

    /**
     * Creates a placeholder with an illustration and the given text.
     *
     * @param text the text below the illustration, or {@code null} for no text
     */
    public DockingPanePlaceholder(String text) {
        getStyleClass().add("docking-pane-placeholder");
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMouseTransparent(true);
        AccessibilityUtil.setRole(this, AccessibleRole.NODE);
        AccessibilityUtil.bindAccessibleText(this, textProperty());

        Region illustration = new Region();
        illustration.getStyleClass().add("illustration");

        Label label = new Label();
        label.textProperty().bind(textProperty());
        label.visibleProperty().bind(textProperty().isNotEmpty());
        label.managedProperty().bind(label.visibleProperty());
        label.setWrapText(true);
        getChildren().addAll(illustration, label);
        setText(text);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DockingPanePlaceholder.class.getResource("docking-pane-placeholder.css")).toExternalForm();
    }

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /**
     * The text displayed below the illustration. An empty or {@code null} value hides the text
     * and its spacing. The no-argument constructor uses a localized version of
     * "Drag views here to dock them.", resolved when the placeholder is created.
     *
     * @return the text property
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return text.get();
    }

    public final void setText(String text) {
        this.text.set(text);
    }
}
