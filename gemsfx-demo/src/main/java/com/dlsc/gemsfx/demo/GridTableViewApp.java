package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.gridtable.GridTableCell;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.gridtable.GridTablePropertyValueFactory;
import com.dlsc.gemsfx.gridtable.GridTableView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.List;
import java.util.Random;

public class GridTableViewApp extends Application {

    private int index;

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridTableView<Student> tableView = new GridTableView<>();
        tableView.setMinNumberOfRows(8);

        GridTableColumn<Student, String> nameColumn = new GridTableColumn<>("Name");
        GridTableColumn<Student, Integer> mathColumn = new GridTableColumn<>("Math");
        GridTableColumn<Student, Integer> physicsColumn = new GridTableColumn<>("Physics");
        GridTableColumn<Student, Integer> artColumn = new GridTableColumn<>("Art");
        GridTableColumn<Student, Integer> ageColumn = new GridTableColumn<>("Age");

        nameColumn.setCellValueFactory(new GridTablePropertyValueFactory<>("name"));
        mathColumn.setCellValueFactory(new GridTablePropertyValueFactory<>("math"));
        physicsColumn.setCellValueFactory(new GridTablePropertyValueFactory<>("physics"));
        artColumn.setCellValueFactory(new GridTablePropertyValueFactory<>("art"));
        ageColumn.setCellValueFactory(new GridTablePropertyValueFactory<>("age"));

        nameColumn.setPercentWidth(28);
        mathColumn.setPercentWidth(18);
        physicsColumn.setPercentWidth(18);
        artColumn.setPercentWidth(18);
        ageColumn.setPercentWidth(18);

        artColumn.setCellFactory(view -> new GridTableCell<>() {
            {
                setStyle("-fx-padding: 10px;");

                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.getItems().setAll("Dirk", "Katja", "Philip", "Jule", "Armin");
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.visibleProperty().bind(emptyProperty().not());

                setGraphic(comboBox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

        tableView.getColumns().addAll(
                List.of(
                        nameColumn,
                        ageColumn,
                        mathColumn,
                        physicsColumn,
                        artColumn)
        );

        tableView.getItems().addAll(
                new Student("Tom", 12, 95, 72, 85),
                new Student("Lucy", 11, 83, 96, 75),
                new Student("Alice", 10, 76, 99, 92),
                new Student("Nicole", 12, 68, 78, 90)
        );

        tableView.getStyleClass().add("styled-grid-table-view");

        CheckBox artBox = new CheckBox("Show Art");
        artBox.setSelected(true);
        artBox.selectedProperty().addListener((ob, ov, selected) -> {
            if (selected) {
                tableView.getColumns().add(artColumn);
            } else {
                tableView.getColumns().remove(artColumn);
            }
        });

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(event -> {
            Random random = new Random();
            tableView.getItems().add(new Student("New " + (++index), random.nextInt(8, 15), random.nextInt(70, 100), random.nextInt(70, 100), random.nextInt(70, 100)));
        });

        Button btnRemove = new Button("Remove");
        btnRemove.setOnAction(event -> {
            if (!tableView.getItems().isEmpty()) {
                tableView.getItems().remove(tableView.getItems().size() - 1);
            }
        });

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(tableView.getScene()));

        HBox bottom = new HBox(30, artBox, btnAdd, btnRemove, scenicView);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));

        BorderPane.setMargin(tableView, new Insets(20));

        tableView.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane stackPane = new StackPane(tableView);
        stackPane.setStyle("-fx-background-color: grey;");
        StackPane.setAlignment(tableView, Pos.CENTER);

        BorderPane pane = new BorderPane(stackPane);
        pane.setBottom(bottom);

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();

        CSSFX.start(scene);
    }

    public static class Student {
        private String name;
        private int age;
        private int math;
        private int physics;
        private int art;

        public Student() {
        }

        public Student(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public Student(String name, int age, int math, int physics, int art) {
            this.name = name;
            this.age = age;
            this.math = math;
            this.physics = physics;
            this.art = art;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public int getMath() {
            return math;
        }

        public void setMath(int math) {
            this.math = math;
        }

        public int getPhysics() {
            return physics;
        }

        public void setPhysics(int physics) {
            this.physics = physics;
        }

        public int getArt() {
            return art;
        }

        public void setArt(int art) {
            this.art = art;
        }
    }
}
