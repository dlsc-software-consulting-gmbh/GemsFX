package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PaymentOptionView;
import com.dlsc.gemsfx.PaymentOptionView.Option;
import com.dlsc.gemsfx.PaymentOptionView.Theme;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PaymentOptionTilesApp extends Application {

    @Override
    public void start(Stage stage) {
        TilePane pane = new TilePane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(20));
        pane.setHgap(10);
        pane.setVgap(10);

        for (Option option : Option.values()) {
            PaymentOptionView view = new PaymentOptionView();
            Tooltip.install(view, new Tooltip("Option: " + option.name()));
            view.setFitWidth(100);
            view.themeProperty().bind(theme);
            view.setPreserveRatio(true);
            view.setOption(option);
            pane.getChildren().add(view);
        }

        ComboBox<Theme> themeBox = new ComboBox<>();
        themeBox.getItems().setAll(Theme.values());
        themeBox.valueProperty().bindBidirectional(theme);

        VBox.setVgrow(pane, Priority.ALWAYS);

        final VBox parent = new VBox(40, pane, themeBox);
        parent.setPadding(new Insets(20));
        parent.setAlignment(Pos.CENTER);

        theme.addListener(it -> {
            switch (theme.get()) {
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

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.DARK);

    public static void main(String[] args) {
        launch();
    }
}
