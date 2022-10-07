package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ScreensView;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ScreensViewSkin extends SkinBase<ScreensView> {

    private Group scalingGroup = new Group();

    public ScreensViewSkin(ScreensView view) {
        super(view);

        scalingGroup.setScaleX(0.2);
        scalingGroup.setScaleY(0.2);
        Group group = new Group(scalingGroup);
        getChildren().add(group);

        Screen.getScreens().addListener((Observable it) -> updateView());
        Window.getWindows().addListener((Observable it) -> updateView());

        view.showWindowsProperty().addListener(it -> updateView());

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
