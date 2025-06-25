package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ResponsivePane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ResponsivePaneBugLayoutApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ResponsivePane responsivePane = new ResponsivePane();
        responsivePane.setStyle("-fx-border-color: red; -fx-border-width: 2;");

        StackPane smallSidebar = new StackPane(new Label("Sidebar\nSmall"));
        smallSidebar.setPrefSize(100, 100);
        smallSidebar.setStyle("-fx-background-color: lightblue;");
        responsivePane.setSmallSidebar(smallSidebar);

        StackPane largeSidebar = new StackPane(new Label("Sidebar\nLarge"));
        largeSidebar.setPrefSize(200, 200);
        largeSidebar.setStyle("-fx-background-color: #c5c7fb;");
        responsivePane.setLargeSidebar(largeSidebar);

        TextFlow contentText = new TextFlow(
                new Text("""
                        Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliqu \s
                        Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  \s
                        """)
        );
        contentText.setMouseTransparent(false);
        contentText.setOnMouseClicked(event -> {
            System.out.println(">>> Content Text Width: " + contentText.getWidth() + ", Height: " + contentText.getHeight());
        });
        contentText.setStyle( "-fx-background-color: rgba(248,222,176,0.1);");
        responsivePane.setContent(contentText);

        StackPane contentPane = new StackPane(responsivePane);
        contentPane.setPadding(new Insets(30));
        contentPane.setMaxHeight(Region.USE_PREF_SIZE);

        ScrollPane root = new ScrollPane(contentPane);
        root.setFitToWidth(true);
        // root.setFitToHeight(true);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("ResponsivePane Bug Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}