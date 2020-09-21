package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PaymentOptionView;
import com.dlsc.gemsfx.PaymentOptionView.Option;
import com.dlsc.gemsfx.PaymentOptionView.Theme;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public class PaymentOptionTilesApp extends Application {

    @Override
    public void start(Stage stage) {
        TilePane pane = new TilePane();
        pane.setStyle("-fx-background-color: navy;");
        pane.setPadding(new Insets(20));
        pane.setHgap(10);
        pane.setVgap(10);

        for (Option option : Option.values()) {
            PaymentOptionView view = new PaymentOptionView();
            view.setFitWidth(200);
            view.setTheme(Theme.LIGHT);
            view.setPreserveRatio(true);
            view.setOption(option);
            pane.getChildren().add(view);
        }

        Scene scene = new Scene(pane);
        stage.setTitle("Payment Option View");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
