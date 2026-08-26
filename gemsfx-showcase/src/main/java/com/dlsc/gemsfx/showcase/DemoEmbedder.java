package com.dlsc.gemsfx.showcase;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

/**
 * Embeds a demo application into the showcase window instead of showing it inside its own
 * top-level window. The demo applications are not aware of the showcase, they all create their
 * user interface inside {@link Application#start(Stage)} and finally show the primary stage that
 * was passed to them.
 * <p>
 * This class runs exactly that code, but it passes a stage whose {@code show()} methods have been
 * turned into no-ops. Afterwards the root node of the scene created by the demo is taken out of
 * the scene so that it can be added to any other node graph, for example the content of a dialog
 * shown by a {@link com.dlsc.gemsfx.DialogPane}. Stylesheets that the demo added to its scene are
 * moved to the root node so that they keep being applied.
 * <p>
 * Running a demo standalone is not affected by this class in any way.
 */
public final class DemoEmbedder {

    private static final Logger LOG = System.getLogger(DemoEmbedder.class.getName());

    private static final double MIN_WIDTH = 400;
    private static final double MIN_HEIGHT = 300;

    /**
     * The result of embedding a demo application.
     *
     * @param content    the node showing the user interface of the demo
     * @param prefWidth  the preferred width of the demo, based on its scene or its root node
     * @param prefHeight the preferred height of the demo, based on its scene or its root node
     */
    public record EmbeddedDemo(Node content, double prefWidth, double prefHeight) {
    }

    private DemoEmbedder() {
    }

    /**
     * Starts the given demo application without showing a window and returns its user interface.
     *
     * @param app the demo application, usually created via {@link ShowcaseEntry#createDemo()}
     * @return the embedded demo
     * @throws IllegalStateException if the demo can not be started or does not create a scene
     */
    public static EmbeddedDemo embed(Application app) {
        Objects.requireNonNull(app, "app can not be null");

        Stage stage = createHiddenStage();

        try {
            app.start(stage);
        } catch (Exception ex) {
            hide(stage);
            throw new IllegalStateException("the demo application \"" + app.getClass().getSimpleName() + "\" could not be started", ex);
        }

        hide(stage);

        Scene scene = stage.getScene();
        if (scene == null) {
            throw new IllegalStateException("the demo application \"" + app.getClass().getSimpleName() + "\" did not create a scene");
        }

        Parent root = scene.getRoot();
        if (root == null) {
            throw new IllegalStateException("the demo application \"" + app.getClass().getSimpleName() + "\" did not create a root node");
        }

        if (scene.getUserAgentStylesheet() != null) {
            LOG.log(Level.WARNING, () -> "the demo application \"" + app.getClass().getSimpleName()
                    + "\" uses a scene user agent stylesheet, which can not be applied to an embedded demo");
        }

        double width = scene.getWidth();
        double height = scene.getHeight();

        if (width <= 0 || height <= 0) {
            // the scene was created without an explicit size, so we have to ask the root node
            root.applyCss();
            root.layout();
            width = root.prefWidth(-1);
            height = root.prefHeight(-1);
        }

        // the stylesheets of the scene have to travel with the root node
        scene.getStylesheets().stream()
                .filter(stylesheet -> !root.getStylesheets().contains(stylesheet))
                .forEach(root.getStylesheets()::add);

        // detach the root node from the scene so that it can be used somewhere else
        scene.setRoot(new Group());
        stage.setScene(null);

        StackPane container = new StackPane(root);
        container.getStyleClass().add("demo-container");

        return new EmbeddedDemo(container, Math.max(MIN_WIDTH, width), Math.max(MIN_HEIGHT, height));
    }

    /**
     * Creates a stage that will never really become visible. Demo applications always call
     * {@code show()} at the end of their start method, which we can not prevent because
     * {@link Stage#show()} is final. The stage is therefore made completely transparent and
     * moved off-screen, and it is hidden again as soon as the start method returns, which
     * happens before the JavaFX pulse that would render the window.
     */
    private static Stage createHiddenStage() {
        Stage stage = new Stage();

        stage.setOpacity(0);
        stage.setX(-30000);
        stage.setY(-30000);

        return stage;
    }

    private static void hide(Stage stage) {
        try {
            stage.hide();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "the hidden stage of an embedded demo could not be closed", ex);
        }
    }
}
