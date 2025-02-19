package com.dlsc.gemsfx.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TableViewExample extends GemApplication {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        TableView tableView = new TableView();

        TableColumn<Person, String> column1 =
                new TableColumn<>("First Name");

        column1.setCellValueFactory(
                new PropertyValueFactory<>("firstName"));


        TableColumn<Person, String> column2 =
                new TableColumn<>("Last Name");

        column2.setCellValueFactory(
                new PropertyValueFactory<>("lastName"));


        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);

        tableView.getItems().add(
                new Person("John", "Doe"));
        tableView.getItems().add(
                new Person("Jane", "Deer"));

        VBox vbox = new VBox(10, tableView, new Button("temp"));
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public class Person {

        private String firstName = null;
        private String lastName = null;

        public Person() {
        }

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}