package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.DialogPane.Dialog;

import java.util.List;

import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.dlsc.gemsfx.DialogPane.Type.INFORMATION;

public class DialogsApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DialogPane dialogPane = new DialogPane();

        Button infoButton = new Button("Info");
        infoButton.setOnAction(evt -> dialogPane.showInformation("Information Dialog Title", "Just some plain old information folks."));

        Button warnButton = new Button("Warning");
        warnButton.setOnAction(evt -> dialogPane.showWarning("Warning Title", "A warning message is not so bad, errors are worse."));

        Button errorButton = new Button("Error");
        errorButton.setOnAction(evt -> dialogPane.showError("Error Title", "Error dialog message that can be somewhat longer"));

        Button confirmButton = new Button("Confirmation");
        confirmButton.setOnAction(evt -> dialogPane.showConfirmation("Confirmation Title", "A confirmation requires the user to decide."));

        Button inputSingleLineButton = new Button("Input");
        inputSingleLineButton.setOnAction(evt -> dialogPane.showTextInput("Text Input", "Please enter something, anything really.", "Text already there ...", false));

        Button inputMultiLineButton = new Button("Input Multi");
        inputMultiLineButton.setOnAction(evt -> dialogPane.showTextInput("Multiline Text Input", "Please enter something, anything really.", "Text already there ...", true));

        Button node1Button = new Button("Node 1");
        node1Button.setOnAction(evt -> dialogPane.showNode(INFORMATION, "Select Person", createCustomNode(), List.of(new ButtonType("Send Mail"), new ButtonType("Call"))));

        Button node2Button = new Button("Node 2");
        node2Button.setOnAction(evt -> dialogPane.showNode(INFORMATION, "Generic Node Dialog", createGenericNode()));

        Button busyButton = new Button("Busy");
        busyButton.setOnAction(evt -> dialogPane.showBusyIndicator());

        Button maxButton = new Button("Maximize");
        maxButton.setOnAction(evt -> {
            Dialog<Object> dialog = new Dialog(dialogPane, INFORMATION);
            dialog.setTitle("Maximized");
            dialog.setContent(new Label("Dialog using all available width and height."));
            dialog.setMaximize(true);
            dialogPane.showDialog(dialog);
        });

        Button overlappingButton = new Button("Multiple Dialogs");
        overlappingButton.setOnAction(evt -> {
            dialogPane.showInformation("Information", "some very long information text is coming here.");
            later(() -> dialogPane.showConfirmation("Confirmation", "some info"), 1);
            later(() -> dialogPane.showWarning("Warning", "Again a warning message?"), 2);
            later(() -> dialogPane.showError("Error", "An error was encountered while running this application."), 3);
        });

        FlowPane flowPane = new FlowPane(10, 10, infoButton, warnButton, errorButton, confirmButton,
                inputSingleLineButton, inputMultiLineButton, node1Button, node2Button, busyButton,
                overlappingButton, maxButton);

        flowPane.setAlignment(Pos.CENTER);

        Duration duration0 = Duration.ZERO;
        Duration duration1 = Duration.millis(100);
        Duration duration2= Duration.millis(200);
        Duration duration3 = Duration.millis(500);
        Duration duration4 = Duration.seconds(1);
        Duration duration5 = Duration.seconds(5);

        ComboBox<Duration> durationBox = new ComboBox<>();
        durationBox.getItems().setAll(duration0, duration1, duration2, duration3, duration4, duration5);
        durationBox.valueProperty().bindBidirectional(dialogPane.animationDurationProperty());

        VBox.setVgrow(flowPane, Priority.ALWAYS);
        VBox.setVgrow(durationBox, Priority.ALWAYS);

        VBox vBox = new VBox(flowPane, durationBox);
        vBox.setPadding(new Insets(50));
        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox, dialogPane);
        AnchorPane anchorPane = new AnchorPane(stackPane);
        anchorPane.getStyleClass().add("editor");

        AnchorPane.setTopAnchor(stackPane, 0d);
        AnchorPane.setBottomAnchor(stackPane, 0d);
        AnchorPane.setLeftAnchor(stackPane, 0d);
        AnchorPane.setRightAnchor(stackPane, 0d);

        Scene scene = new Scene(anchorPane);

        scene.focusOwnerProperty().addListener(it -> System.out.println("focus owner: " + scene.getFocusOwner()));
        CSSFX.start(scene);

        primaryStage.setTitle("Dialogs");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1500);
        primaryStage.setHeight(900);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void later(Runnable runnable, int counter) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(counter * 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(runnable);
        });

        thread.start();
    }

    private Node createCustomNode() {
        TableView<Person> tableView = new TableView<>();

        TableColumn<Person, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(120);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Person, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setPrefWidth(250);
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        tableView.getColumns().setAll(nameColumn, addressColumn);

        tableView.setPrefHeight(250);

        tableView.getItems().add(new Person("Jane Doe", "56 Vinal Avenue, Sommerville MA 02129"));
        tableView.getItems().add(new Person("Steve Smith", "101 Highland Avenue, Pittsburgh PA 15106"));
        tableView.getItems().add(new Person("Maria Miller", "13 Baxter Str., San Francisco. CA 94016"));

        return tableView;
    }

    private Node createGenericNode() {
        Rectangle rect = new Rectangle();
        rect.setFill(Color.RED);
        rect.setWidth(300);
        rect.setHeight(300);

        Label label = new Label("300 x 300");
        label.setStyle("-fx-text-fill: white;");

        return new StackPane(rect, label);
    }

    public class Person {

        private String name;
        private String address;

        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
