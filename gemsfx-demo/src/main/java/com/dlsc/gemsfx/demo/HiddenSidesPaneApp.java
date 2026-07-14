package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.HiddenSidesPane;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HiddenSidesPaneApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-padding: 30");

        HiddenSidesPane pane = new HiddenSidesPane();

        Label content = new Label("Content Node");
        content.setStyle("-fx-background-color: white; -fx-border-color: black;");
        content.setAlignment(Pos.CENTER);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        pane.setContent(content);

        SideNode top = new SideNode("Top", Side.TOP, pane);
        top.setStyle("-fx-background-color: rgba(0,255,0,.25);");
        pane.setTop(top);

        SideNode right = new SideNode("Right", Side.RIGHT, pane);
        right.setStyle("-fx-background-color: rgba(0,0, 255,.25);");
        pane.setRight(right);

        SideNode bottom = new SideNode("Bottom", Side.BOTTOM, pane);
        bottom.setStyle("-fx-background-color: rgba(255,255,0,.25);");
        pane.setBottom(bottom);

        SideNode left = new SideNode("Left", Side.LEFT, pane);
        left.setStyle("-fx-background-color: rgba(255,0,0,.25);");
        pane.setLeft(left);

        stackPane.getChildren().add(pane);

        stage.setScene(new Scene(stackPane, 800, 700));
        stage.setTitle("History Manager Demo");

        stage.show();
    }

    static class SideNode extends Label {

        public SideNode(final String text, final Side side, final HiddenSidesPane pane) {

            super(text + " (Click to pin / unpin)");

            setAlignment(Pos.CENTER);
            setPrefSize(200, 200);

            setOnMouseClicked(event -> {
                if (pane.getPinnedSide() != null) {
                    setText(text + " (unpinned)");
                    pane.setPinnedSide(null);
                } else {
                    setText(text + " (pinned)");
                    pane.setPinnedSide(side);
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
