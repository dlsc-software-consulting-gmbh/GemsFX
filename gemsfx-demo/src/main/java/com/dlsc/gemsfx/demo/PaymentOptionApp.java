package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PaymentOptionView;
import com.dlsc.gemsfx.PaymentOptionView.Option;
import com.dlsc.gemsfx.PaymentOptionView.Theme;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PaymentOptionApp extends Application {

    @Override
    public void start(Stage stage) {
        PaymentOptionView paymentOptionView = new PaymentOptionView();

        ComboBox<Option> optionsBox = new ComboBox<>();
        optionsBox.getItems().setAll(Option.values());
        optionsBox.valueProperty().bindBidirectional(paymentOptionView.optionProperty());

        ComboBox<Theme> themeBox = new ComboBox<>();
        themeBox.getItems().setAll(Theme.values());
        themeBox.valueProperty().bindBidirectional(paymentOptionView.themeProperty());

        HBox box = new HBox(20, optionsBox, themeBox);
        box.setAlignment(Pos.CENTER);

        VBox.setVgrow(paymentOptionView, Priority.ALWAYS);

        final VBox parent = new VBox(40, paymentOptionView, box);
        parent.setAlignment(Pos.CENTER);

        themeBox.valueProperty().addListener(it -> {
            switch (themeBox.getValue()) {
                case DARK:
                    parent.setStyle("-fx-background-color: white;");
                    break;
                case LIGHT:
                    parent.setStyle("-fx-background-color: rgb(57,73,92);");
                    break;
            }
        });

        Scene scene = new Scene(parent);
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
