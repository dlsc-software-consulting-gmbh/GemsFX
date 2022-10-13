package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ScreensView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ScreensViewSkin extends SkinBase<ScreensView> {

    private Group scalingGroup = new Group();

    public ScreensViewSkin(ScreensView view) {
        super(view);

        Group group = new Group(scalingGroup);
        group.getStyleClass().add("container");
        group.effectProperty().bind(Bindings.createObjectBinding(() -> {
            DropShadow shadow = view.getShadow();
            Reflection reflection = view.getReflection();

            if (view.isShowShadow() && view.isShowReflection()) {
                reflection.setInput(shadow);
                return reflection;
            } else if (view.isShowShadow()) {
                return shadow;
            } else if (view.isShowReflection()) {
                reflection.setInput(null);
                return reflection;
            }

            return null;
        }, view.showReflectionProperty(), view.showShadowProperty()));
        getChildren().add(group);

        InvalidationListener updateViewListener = (Observable it) -> updateView();

        view.showWallpaperProperty().addListener(updateViewListener);
        view.showWindowsProperty().addListener(updateViewListener);
        view.getShapes().addListener(updateViewListener);

        Screen.getScreens().addListener(updateViewListener);
        Window.getWindows().addListener(updateViewListener);

        updateView();
    }

    private void updateView() {
        scalingGroup.getChildren().clear();

        ObservableList<Screen> screens = Screen.getScreens();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Screen screen : screens) {
            Group group = new Group();

            Rectangle2D bounds = screen.getBounds();

            Rectangle clip = new Rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            clip.setArcWidth(32);
            clip.setArcHeight(32);
            group.setClip(clip);

            group.getChildren().add(new BackgroundView(screen));
            group.getChildren().add(new ScreenView(screen));
            group.getChildren().add(new VisibleAreaView(screen));
            group.getChildren().add(new GlassView(screen));

            minX = Math.min(minX, bounds.getMinX());
            minY = Math.min(minY, bounds.getMinY());
            maxX = Math.max(maxX, bounds.getMinX() + bounds.getWidth());
            maxY = Math.max(maxY, bounds.getMinY() + bounds.getHeight());

            scalingGroup.getChildren().add(group);
        }

        ScreensView view = getSkinnable();

        for (Shape shape : view.getShapes()) {
            scalingGroup.getChildren().add(shape);

            minX = Math.min(minX, shape.getLayoutX());
            minY = Math.min(minY, shape.getLayoutY());
            maxX = Math.max(maxX, shape.getLayoutX() + shape.prefWidth(-1));
            maxY = Math.max(maxY, shape.getLayoutY() + shape.prefHeight(-1));
        }

        if (view.isShowWindows()) {
            for (Window window : Window.getWindows()) {
                scalingGroup.getChildren().add(new WindowView(window));
            }
        }

        Rectangle2D totalBounds = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);

        DoubleBinding scale = Bindings.createDoubleBinding(() -> Math.min(view.getWidth() / totalBounds.getWidth(), view.getHeight() / totalBounds.getHeight()) * .75, view.widthProperty(), view.heightProperty(), Screen.getScreens(), Window.getWindows());

        scalingGroup.scaleXProperty().bind(scale);
        scalingGroup.scaleYProperty().bind(scale);
    }

    public class BackgroundView extends StackPane {

        public BackgroundView(Screen screen) {
            getStyleClass().add("background");

            ScreensView view = getSkinnable();

            if (view.isShowWallpaper()) {
                Callback<Screen, Image> wallpaperProvider = view.getWallpaperProvider();
                if (wallpaperProvider != null) {
                    Image image = wallpaperProvider.call(screen);
                    if (image != null) {
                        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS)));
                        setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true))));
                    }
                }
            }

            Rectangle2D bounds = screen.getBounds();
            setLayoutX(bounds.getMinX());
            setLayoutY(bounds.getMinY());
            setPrefWidth(bounds.getWidth());
            setPrefHeight(bounds.getHeight());
        }
    }

    public class ScreenView extends StackPane {

        public ScreenView(Screen screen) {
            getStyleClass().add("screen");

            ScreensView view = getSkinnable();
            if (!view.isShowWallpaper()) {
                getStyleClass().add("no-wallpaper");
            }

            Rectangle2D bounds = screen.getBounds();
            setLayoutX(bounds.getMinX());
            setLayoutY(bounds.getMinY());
            setPrefWidth(bounds.getWidth());
            setPrefHeight(bounds.getHeight());

            Label label = new Label("Screen " + Screen.getScreens().indexOf(screen));
            label.setTextAlignment(TextAlignment.CENTER);
            label.setWrapText(true);
            if (Screen.getPrimary().equals(screen)) {
                label.setText("Primary");
            }
            getChildren().add(label);
        }
    }

    static class VisibleAreaView extends StackPane {

        public VisibleAreaView(Screen screen) {
            getStyleClass().add("visible-area");

            Rectangle2D bounds = screen.getBounds();
            setLayoutX(bounds.getMinX());
            setLayoutY(bounds.getMinY());
            setPrefWidth(bounds.getWidth());
            setPrefHeight(bounds.getHeight());

            Rectangle2D visualBounds = screen.getVisualBounds();

            Rectangle clipRectangle = new Rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            clipRectangle.setArcWidth(32);
            clipRectangle.setArcHeight(32);

            Shape visibleAreaClip = clipRectangle;
            visibleAreaClip = Shape.subtract(visibleAreaClip, new Rectangle(visualBounds.getMinX(), visualBounds.getMinY(), visualBounds.getWidth(), visualBounds.getHeight()));
            setClip(visibleAreaClip);
        }
    }

    static class GlassView extends StackPane {

        public GlassView(Screen screen) {
            getStyleClass().add("glass");

            Rectangle2D bounds = screen.getBounds();
            setLayoutX(bounds.getMinX());
            setLayoutY(bounds.getMinY());
            setPrefWidth(bounds.getWidth());
            setPrefHeight(bounds.getHeight());
        }
    }

    class WindowView extends StackPane {

        private double startX;
        private double startY;

        public WindowView(Window window) {
            getStyleClass().add("window");

            layoutXProperty().bind(window.xProperty());
            layoutYProperty().bind(window.yProperty());
            prefWidthProperty().bind(window.widthProperty());
            prefHeightProperty().bind(window.heightProperty());

            BooleanBinding visibleBinding = Bindings.createBooleanBinding(window::isShowing, window.showingProperty());

            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                visibleBinding = visibleBinding.and(stage.iconifiedProperty().not());

                Label label = new Label();
                label.setWrapText(true);
                label.setTextAlignment(TextAlignment.CENTER);
                label.textProperty().bind(stage.titleProperty());
                getChildren().add(label);
            }

            visibleProperty().bind(visibleBinding);

            setOnMouseClicked(evt -> {
                if (evt.isStillSincePress() && evt.getButton().equals(MouseButton.PRIMARY)) {
                    toFront();
                }
            });

            setOnMousePressed(evt -> {
                startX = evt.getScreenX();
                startY = evt.getScreenY();
            });

            setOnMouseDragged(evt -> {
                if (getSkinnable().isEnableWindowDragging()) {
                    window.setX(window.getX() + ((evt.getScreenX() - startX) * (1 / scalingGroup.getScaleX())));
                    window.setY(window.getY() + ((evt.getScreenY() - startY) * (1 / scalingGroup.getScaleY())));

                    startX = evt.getScreenX();
                    startY = evt.getScreenY();
                }
            });
        }
    }
}
