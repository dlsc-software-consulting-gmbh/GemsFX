package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ChipsViewContainer;
import com.dlsc.gemsfx.SelectionBox;
import com.dlsc.gemsfx.SimpleFilterView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class SimpleFilterViewApp extends Application {

    private final ObjectProperty<SimpleFilterView.LayoutMode> layoutMode = new SimpleObjectProperty<>(SimpleFilterView.LayoutMode.STANDARD);

    @Override
    public void start(Stage stage) {
        SimpleFilterView filterView1 = new SimpleFilterView();
        filterView1.addSelectionBox("HPos", HPos.class);
        filterView1.addSelectionBox("VPos", VPos.class);
        filterView1.addSelectionBox("Pos", Pos.class);
        filterView1.addSelectionBox("Side", Side.class);
        filterView1.addDateRangePicker("Date Range");
        filterView1.addCalendarPicker("Date");
        filterView1.layoutModeProperty().bindBidirectional(layoutMode);
        filterView1.getChildren().add(new MenuButton("Test"));
        filterView1.getChildren().add(new DatePicker());
        ChipsViewContainer chipsViewContainer1 = new ChipsViewContainer();
        chipsViewContainer1.setOnClear(filterView1::clear);
        chipsViewContainer1.chipsProperty().bind(filterView1.chipsProperty());

        VBox box1 = new VBox(5, new Label("Boxes with multiple selection support"), filterView1, chipsViewContainer1);
        box1.setPadding(new Insets(20));

        SimpleFilterView filterView2 = new SimpleFilterView();
        SelectionBox<HPos> hPos = filterView2.addSelectionBox("HPos", HPos.class);
        SelectionBox<VPos> vPos = filterView2.addSelectionBox("VPos", VPos.class);
        SelectionBox<Pos> pos = filterView2.addSelectionBox("Pos", Pos.class);
        SelectionBox<Side> side = filterView2.addSelectionBox("Side", Side.class);
        filterView2.addDateRangePicker("Date Range");
        filterView2.addCalendarPicker("Date");
        filterView2.layoutModeProperty().bindBidirectional(layoutMode);

        hPos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        vPos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        pos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        side.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ChipsViewContainer chipsViewContainer2 = new ChipsViewContainer();
        chipsViewContainer2.setOnClear(filterView2::clear);
        chipsViewContainer2.chipsProperty().bind(filterView2.chipsProperty());

        VBox box2 = new VBox(5, new Label("Boxes with single selection support"), filterView2, chipsViewContainer2);
        box2.setPadding(new Insets(20));

        ComboBox<SimpleFilterView.LayoutMode> layoutModeComboBox = new ComboBox<>();
        layoutModeComboBox.getItems().addAll(SimpleFilterView.LayoutMode.values());
        layoutModeComboBox.valueProperty().bindBidirectional(layoutMode);
        layoutModeComboBox.setValue(SimpleFilterView.LayoutMode.COMPACT);

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        VBox box = new VBox(20, box1, box2, layoutModeComboBox, scenicView);
        box.setAlignment(Pos.CENTER);
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);
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
