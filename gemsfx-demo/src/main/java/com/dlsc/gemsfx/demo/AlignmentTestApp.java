package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.ChipsViewContainer;
import com.dlsc.gemsfx.DurationPicker;
import com.dlsc.gemsfx.SelectionBox;
import com.dlsc.gemsfx.SimpleFilterView;
import com.dlsc.gemsfx.TimePicker;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.util.StageManager;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.time.LocalDate;

public class AlignmentTestApp extends Application {

    private final ObjectProperty<SimpleFilterView.LayoutMode> layoutMode = new SimpleObjectProperty<>(SimpleFilterView.LayoutMode.STANDARD);

    @Override
    public void start(Stage stage) {
        HBox hbox = new HBox(10);
        hbox.setStyle("-fx-background-color: red; -fx-padding: 1px;");

        // selection box
        SelectionBox<HPos> selectionBox = new SelectionBox<>(HPos.values());
        selectionBox.setPromptText("HPos");
        hbox.getChildren().add(selectionBox);

        // combo box
        ComboBox<String> com = new ComboBox<>();
        com.getItems().addAll("A", "B", "C");
        com.setValue("A");
        hbox.getChildren().add(com);

        // date range picker
        hbox.getChildren().add(new DateRangePicker());

        // duration picker
        hbox.getChildren().add(new DurationPicker());

        // time picker
        hbox.getChildren().add(new TimePicker());

        // calendar picker
        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setValue(LocalDate.now());
        hbox.getChildren().add(calendarPicker);

        // menu button
        hbox.getChildren().add(new MenuButton("Test"));

        // date picker
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        hbox.getChildren().add(datePicker);

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        VBox box = new VBox(20, hbox, scenicView);
        box.setAlignment(Pos.CENTER);
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);

        StageManager.install(stage, "alignment.test.demo", 800, 500);

        stage.setTitle("Alignment Test");
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
