package com.dlsc.gemsfx.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.Objects;

/**
 * Applies the GemsFX AtlantaFX companion stylesheet to a JavaFX scene or
 * parent node.
 *
 * <p>GemsFX controls define their own user-agent stylesheets that use
 * Modena-derived CSS variables. When an application switches to an
 * <a href="https://github.com/mkpaz/atlantafx">AtlantaFX</a> theme those
 * controls keep their Modena look. This utility loads a companion stylesheet
 * that overrides the GemsFX control rules with AtlantaFX CSS custom
 * properties, so every control blends in with the active AtlantaFX theme.
 *
 * <p>Typical usage:
 * <pre>{@code
 * // 1. Apply the AtlantaFX theme globally (before showing the stage)
 * Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
 *
 * // 2. Apply the GemsFX companion stylesheet to the scene
 * GemsFXAtlantaFX.applyTo(scene);
 * }</pre>
 *
 * <p>The companion stylesheet references AtlantaFX CSS looked-up color
 * variables (e.g. {@code -color-fg-default}, {@code -color-bg-subtle},
 * {@code -color-accent-emphasis}) which must be present on the {@code .root}
 * node. If no AtlantaFX theme is active those variables are undefined and the
 * controls will fall back to the JavaFX default behavior.
 *
 * <p>This module has <em>no compile or runtime dependency</em> on the
 * AtlantaFX library itself; it only ships CSS that consumes AtlantaFX variables.
 */
public final class GemsFXAtlantaFX {

    public static final String STYLESHEET = Objects.requireNonNull(GemsFXAtlantaFX.class.getResource("gemsfx-atlantafx.css")).toExternalForm();

    /**
     * Adds the GemsFX AtlantaFX companion stylesheet to the given scene's
     * stylesheet list.
     *
     * @param scene the scene to apply the companion stylesheet to
     */
    public static void applyTo(Scene scene) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    /**
     * Adds the GemsFX AtlantaFX companion stylesheet to the given parent
     * node's stylesheet list. Use this overload when the scene is not yet
     * available or when the styles should be scoped to a sub-tree.
     *
     * @param parent the parent node to apply the companion stylesheet to
     */
    public static void applyTo(Parent parent) {
        if (!parent.getStylesheets().contains(STYLESHEET)) {
            parent.getStylesheets().add(STYLESHEET);
        }
    }

    private GemsFXAtlantaFX() {
    }
}
