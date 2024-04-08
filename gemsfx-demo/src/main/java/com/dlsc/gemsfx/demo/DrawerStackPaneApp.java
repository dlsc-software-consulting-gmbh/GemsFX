package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DrawerStackPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.scenicview.ScenicView;

public class DrawerStackPaneApp extends Application {

    @Override
    public void start(Stage stage) {
        DrawerStackPane drawerStackPane = new DrawerStackPane();
        drawerStackPane.setAnimateDrawer(true);
        drawerStackPane.getToolbarItems().add(new Button("Refresh"));

        Label label = new Label("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.   \n" +
                "\n" +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.   \n" +
                "\n" +
                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.   \n" +
                "\n" +
                "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.   \n" +
                "\n" +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis.   \n" +
                "\n" +
                "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, At accusam aliquyam diam diam dolore dolores duo eirmod eos erat, et nonumy sed tempor et et invidunt justo labore Stet clita ea et gubergren, kasd magna no rebum. sanctus sea sed takimata ut vero voluptua. est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat.   \n" +
                "\n" +
                "Consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus.   \n" +
                "\n" +
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.   \n" +
                "\n" +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.   \n" +
                "\n" +
                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.   \n" +
                "\n" +
                "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo");

        label.setWrapText(true);
        label.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);

        drawerStackPane.setPreferredDrawerWidth(800);
        drawerStackPane.setDrawerContent(scrollPane);

        Button showButton = new Button("Show Drawer");
        drawerStackPane.getChildren().add(showButton);
        showButton.setOnAction(evt -> drawerStackPane.setShowDrawer(true));

        CheckBox animateBox = new CheckBox("Animate");
        animateBox.selectedProperty().bindBidirectional(drawerStackPane.animateDrawerProperty());

        ComboBox<Duration> durationBox = new ComboBox<>();
        durationBox.getItems().addAll(Duration.millis(100), Duration.millis(250), Duration.millis(500), Duration.millis(500), Duration.millis(1000), Duration.millis(2000));
        durationBox.valueProperty().bindBidirectional(drawerStackPane.animationDurationProperty());
        durationBox.getSelectionModel().select(1);

        Slider slider = new Slider(500, 1200, drawerStackPane.getPreferredDrawerWidth());
        slider.valueProperty().bindBidirectional(drawerStackPane.preferredDrawerWidthProperty());

        CheckBox maximizeBox = new CheckBox("Maximize");
        maximizeBox.selectedProperty().addListener(it -> {
            if (maximizeBox.isSelected()) {
                drawerStackPane.setPreferredDrawerWidth(-1);
            } else {
                drawerStackPane.setPreferredDrawerWidth(800);
            }
        });

        Slider topPaddingSlider = new Slider(0, 100, drawerStackPane.getTopPadding());
        topPaddingSlider.valueProperty().bindBidirectional(drawerStackPane.topPaddingProperty());

        Slider sidePaddingSlider = new Slider(0, 100, drawerStackPane.getTopPadding());
        sidePaddingSlider.valueProperty().bindBidirectional(drawerStackPane.sidePaddingProperty());

        slider.disableProperty().bind(maximizeBox.selectedProperty());

        HBox controls = new HBox(10, animateBox, new Separator(Orientation.VERTICAL),
                new Label("Duration"), durationBox, new Separator(Orientation.VERTICAL),
                new Label("Width"), slider, new Separator(Orientation.VERTICAL),
                maximizeBox, new Separator(Orientation.VERTICAL),
                new Label("Top Padding"), topPaddingSlider, new Separator(Orientation.VERTICAL),
                new Label("Side Padding"), sidePaddingSlider);

        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setStyle("-fx-padding: 10px; -fx-background-color: lightgrey, white; -fx-background-insets: 0px, 1px 0px 0px 0px;");

        BorderPane borderPane = new BorderPane(drawerStackPane);
        borderPane.setBottom(controls);
        borderPane.setPrefHeight(850);

        Scene scene = new Scene(borderPane);
        stage.setTitle("Drawer Demo");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        ScenicView.show(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
