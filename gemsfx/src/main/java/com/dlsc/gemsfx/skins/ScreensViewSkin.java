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
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class ScreensViewSkin extends SkinBase<ScreensView> {

    private Group scalingGroup = new Group();

    public ScreensViewSkin(ScreensView view) {
        super(view);

        scalingGroup.setScaleX(0.2);
        scalingGroup.setScaleY(0.2);
        Group group = new Group(scalingGroup);
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
            scalingGroup.getChildren().add(new ScreenView(screen, false));
            scalingGroup.getChildren().add(new ScreenView(screen, true));

            Rectangle2D bounds = screen.getBounds();
            minX = Math.min(minX, bounds.getMinX());
            minY = Math.min(minY, bounds.getMinY());
            maxX = Math.max(maxX, bounds.getMinX() + bounds.getWidth());
            maxY = Math.max(maxY, bounds.getMinY() + bounds.getHeight());
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

    private class ScreenView extends StackPane {

        public ScreenView(Screen screen, boolean visual) {
            getStyleClass().add("screen");

            Rectangle2D bounds = screen.getBounds();
            if (visual) {
                getStyleClass().add("visual-bounds");
                bounds = screen.getVisualBounds();
            }

            setLayoutX(bounds.getMinX());
            setLayoutY(bounds.getMinY());
            setPrefWidth(bounds.getWidth());
            setPrefHeight(bounds.getHeight());

            if (visual) {
                Label label = new Label("Screen " + Screen.getScreens().indexOf(screen));
                if (Screen.getPrimary().equals(screen)) {
                    label.setText("Primary");
                }
                getChildren().add(label);
            }

            ScreensView view = getSkinnable();

            if (visual && view.isShowWallpaper()) {
                getStyleClass().add("wallpaper");

                Callback<Screen, Image> wallpaperProvider = view.getWallpaperProvider();
                if (wallpaperProvider != null) {
                    Image image = wallpaperProvider.call(screen);
                    if (image != null) {
                        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS)));
                        setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true))));
                    }
                }
            }
        }
    }

    private class WindowView extends StackPane {

        public WindowView(Window window) {
            getStyleClass().add("window");

            layoutXProperty().bind(window.xProperty());
            layoutYProperty().bind(window.yProperty());
            prefWidthProperty().bind(window.widthProperty());
            prefHeightProperty().bind(window.heightProperty());

            BooleanBinding visibleBinding = Bindings.createBooleanBinding(() -> window.isShowing(), window.showingProperty());

            if (window instanceof Stage) {
                Label label = new Label();
                Stage stage = (Stage) window;
                visibleBinding = visibleBinding.and(stage.iconifiedProperty().not());
                label.textProperty().bind(stage.titleProperty());
                getChildren().add(label);
            }

            visibleProperty().bind(visibleBinding);

            setOnMouseClicked(evt -> {
                if (evt.isStillSincePress() && evt.getButton().equals(MouseButton.PRIMARY)) {
                    toFront();
                }
            });
        }
    }
}
