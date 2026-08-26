package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;

/**
 * The welcome page of the showcase application. The page covers the entire window and greets
 * the user with the GemsFX logo, a tagline, some statistics about the library, and a button
 * that reveals the actual showcase. In the background, stylized mockups of user interface
 * elements - a calendar, a table, a color palette, and so on - rain down the window, the same
 * effect that is used on the hero section of the DLSC website. The graphics immediately express
 * that the application presents a large collection of widgets.
 *
 * @see ControlRain
 */
public class WelcomePane extends StackPane {

    private final ControlRain controlRain = new ControlRain();
    private final VBox contentBox;
    private final CheckBox doNotShowBox;

    private Runnable onExplore;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean dragging;

    public WelcomePane(Stage stage) {
        getStyleClass().add("welcome-pane");

        // ── foreground: logo, texts, and the call-to-action button ───────────
        ImageView logoView = new ImageView(new Image(Objects.requireNonNull(WelcomePane.class.getResourceAsStream("gems.png"))));
        logoView.setPreserveRatio(true);
        logoView.setFitHeight(80);

        Label titleLabel = new Label(ShowcaseBundle.get("app.title"));
        titleLabel.getStyleClass().add("welcome-title");

        Label taglineLabel = new Label(ShowcaseBundle.get("welcome.tagline"));
        taglineLabel.getStyleClass().add("welcome-tagline");
        taglineLabel.setWrapText(true);

        int controlCount = ShowcaseRegistry.ALL_ENTRIES.size();
        int categoryCount = ShowcaseRegistry.groupByCategory(ShowcaseRegistry.ALL_ENTRIES).size();

        Label statsLabel = new Label(ShowcaseBundle.format("welcome.stats", controlCount, categoryCount));
        statsLabel.getStyleClass().add("welcome-stats");

        Button exploreButton = new Button(ShowcaseBundle.get("welcome.explore"), new FontIcon(MaterialDesign.MDI_ARROW_RIGHT));
        exploreButton.getStyleClass().addAll("welcome-cta", Styles.ACCENT);
        exploreButton.setContentDisplay(ContentDisplay.RIGHT);
        exploreButton.setDefaultButton(true);
        exploreButton.setOnAction(evt -> {
            if (onExplore != null) {
                onExplore.run();
            }
        });

        doNotShowBox = new CheckBox(ShowcaseBundle.get("welcome.doNotShow"));
        doNotShowBox.getStyleClass().add("welcome-do-not-show-box");
        doNotShowBox.setFocusTraversable(false);

        contentBox = new VBox(logoView, titleLabel, taglineLabel, statsLabel, exploreButton, doNotShowBox);
        contentBox.getStyleClass().add("welcome-content");
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(Region.USE_PREF_SIZE);
        contentBox.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(contentBox, Pos.CENTER);

        // ── header strip: the window buttons ─────────────────────────────────
        // the welcome page covers the entire window, including the header bar of the showcase,
        // hence it needs window buttons of its own so that the window can still be closed,
        // minimized, and maximized
        HeaderBar headerBar = new HeaderBar();
        headerBar.getStyleClass().add("welcome-header-bar");
        headerBar.setLeadingSystemPadding(false);
        headerBar.setTrailingSystemPadding(false);
        headerBar.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(headerBar, Pos.TOP_LEFT);
        ShowcaseApp.createHeaderButtonGroup().install(headerBar, stage);

        // the rain of user interface graphics sits behind the foreground content
        getChildren().addAll(controlRain, contentBox, headerBar);

        installWindowDragging(stage, headerBar);
    }

    /**
     * Makes the entire welcome page behave like the drag area of the window. The page covers the
     * whole window, hence the header bar of the showcase - the area that usually moves the window -
     * is not reachable while the page is showing. Dragging anywhere on the page therefore moves the
     * window, except on the header bar of the page itself, which is a native drag area already, and
     * except while the window is maximized or in full screen.
     *
     * @param stage     the window moved by the drag gesture
     * @param headerBar the header bar of the page, which handles dragging on its own
     */
    private void installWindowDragging(Stage stage, HeaderBar headerBar) {
        addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
            dragging = evt.getButton() == MouseButton.PRIMARY
                    && !stage.isMaximized()
                    && !stage.isFullScreen()
                    && !isInsideHeaderBar(evt, headerBar);

            if (dragging) {
                dragOffsetX = evt.getScreenX() - stage.getX();
                dragOffsetY = evt.getScreenY() - stage.getY();
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, evt -> {
            if (dragging) {
                stage.setX(evt.getScreenX() - dragOffsetX);
                stage.setY(evt.getScreenY() - dragOffsetY);
            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, evt -> dragging = false);
    }

    /**
     * Determines whether the given mouse event happened on the header bar of the page, which
     * includes the window buttons.
     */
    private boolean isInsideHeaderBar(MouseEvent evt, HeaderBar headerBar) {
        Node node = evt.getPickResult().getIntersectedNode();

        while (node != null) {
            if (node == headerBar) {
                return true;
            }
            node = node.getParent();
        }

        return false;
    }

    /**
     * Sets the callback invoked when the user presses the call-to-action button.
     *
     * @param onExplore the callback revealing the main user interface
     */
    public void setOnExplore(Runnable onExplore) {
        this.onExplore = onExplore;
    }

    /**
     * The selection state of the "do not show again" checkbox.
     *
     * @return the property backing the checkbox
     */
    public BooleanProperty doNotShowAgainProperty() {
        return doNotShowBox.selectedProperty();
    }

    public boolean isDoNotShowAgain() {
        return doNotShowBox.isSelected();
    }

    public void setDoNotShowAgain(boolean doNotShowAgain) {
        doNotShowBox.setSelected(doNotShowAgain);
    }

    /**
     * Restarts the welcome page: replays the entrance animation of the foreground content and
     * starts the endless rain of user interface graphics in the background.
     */
    public void play() {
        playEntrance();
        controlRain.play();
    }

    /**
     * Stops the rain of user interface graphics.
     */
    public void stopAnimation() {
        controlRain.stop();
    }

    /**
     * Fades and slides the foreground content in.
     */
    private void playEntrance() {
        contentBox.setOpacity(0);
        contentBox.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(700), contentBox);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(700), contentBox);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition entrance = new ParallelTransition(fade, slide);
        entrance.setDelay(Duration.millis(150));
        entrance.play();
    }
}
