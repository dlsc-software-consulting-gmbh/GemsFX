package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.daterange.DateRangeView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class DateRangeViewApp extends Application {

    @Override
    public void start(Stage stage) {
        DateRangeView view = new DateRangeView();
        view.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Button scenicViewButton = new Button("SCENIC VIEW");
        VBox.setMargin(scenicViewButton, new Insets(50, 0, 0, 0));

        ComboBox<Side> sideBox = new ComboBox<>();
        sideBox.getItems().setAll(Side.LEFT, Side.RIGHT);
        sideBox.valueProperty().bindBidirectional(view.presetsLocationProperty());

        ComboBox<Orientation> orientationBox = new ComboBox<>();
        orientationBox.getItems().setAll(Orientation.VERTICAL, Orientation.HORIZONTAL);
        orientationBox.valueProperty().bindBidirectional(view.orientationProperty());

        CheckBox showButtons = new CheckBox("Show buttons");
        showButtons.selectedProperty().bindBidirectional(view.showCancelAndApplyButtonProperty());

        CheckBox showPresets = new CheckBox("Show presets");
        showPresets.selectedProperty().bindBidirectional(view.showPresetsProperty());

        TextField titleField = new TextField();
        titleField.textProperty().bindBidirectional(view.presetTitleProperty());

        HBox optionsBox = new HBox(10, sideBox, orientationBox, scenicViewButton, titleField, showButtons, showPresets);
        optionsBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(20, view, optionsBox);

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
