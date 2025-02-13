package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ChipsViewContainer;
import com.dlsc.gemsfx.SimpleFilterView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SimpleFilterViewApp extends Application {

    @Override
    public void start(Stage stage) {
        SimpleFilterView filterView = new SimpleFilterView();
        filterView.addSelectionBox("HPos", HPos.class);
        filterView.addSelectionBox("VPos", VPos.class);
        filterView.addSelectionBox("Pos", Pos.class);
        filterView.addSelectionBox("Side", Side.class);
        filterView.addDateRangePicker("Date Range");
        filterView.addCalendarPicker("Date");

        ChipsViewContainer chipsViewContainer = new ChipsViewContainer();
        chipsViewContainer.setOnClear(filterView::clear);
        chipsViewContainer.chipsProperty().bind(filterView.chipsProperty());

        VBox box = new VBox(filterView, chipsViewContainer);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);
        stage.setTitle("Simple Filter / Chips View Container Demo");
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.centerOnScreen();
        stage.show();

        CSSFX.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
