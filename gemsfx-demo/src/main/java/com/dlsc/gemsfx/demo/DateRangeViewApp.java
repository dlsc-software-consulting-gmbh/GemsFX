package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.daterange.DateRangeView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class DateRangeViewApp extends Application {

    @Override
    public void start(Stage stage) {
        DateRangeView view = new DateRangeView();

        Button scenicViewButton = new Button("Scenic View");
        VBox.setMargin(scenicViewButton, new Insets(50, 0, 0, 0));

        ComboBox<Side> sideBox = new ComboBox<Side>();
        sideBox.getItems().setAll(Side.LEFT, Side.RIGHT);
        sideBox.valueProperty().bindBidirectional(view.presetsLocationProperty());

        VBox vBox = new VBox(20, view, sideBox, scenicViewButton);

        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        scenicViewButton.setOnAction(evt -> ScenicView.show(scene));

        stage.setTitle("Date Range View");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        CSSFX.start(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
