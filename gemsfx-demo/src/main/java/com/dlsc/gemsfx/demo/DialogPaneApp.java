package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.DialogPane.Dialog;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.scenicview.ScenicView;

import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import static com.dlsc.gemsfx.DialogPane.Type.INFORMATION;

public class DialogPaneApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DialogPane dialogPane = new DialogPane();

        Button blankButton = new Button("Blank");
        blankButton.setOnAction(evt -> {
            Dialog<ButtonType> dialog = new Dialog<>(dialogPane, DialogPane.Type.BLANK);
            dialog.setResizable(true);
            dialog.setId("blank.dialog");
            dialog.setPreferences(Preferences.userNodeForPackage(DialogPaneApp.class).node(dialog.getId()));
            dialog.setContentAlignment(Pos.CENTER);

            Label content = new Label("Content");
            content.setAlignment(Pos.CENTER);
            content.setPrefSize(400, 300);
            content.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            content.setOnMouseClicked(e -> dialog.cancel());
            dialog.setContent(content);
            dialog.show();
        });

        Button infoButton = new Button("Info");
        infoButton.setOnAction(evt -> {
            Dialog<ButtonType> dialog = dialogPane.showInformation("Information Dialog Title", "Just some plain old information folks, which can actually be a very long text and we want to make sure it wraps.");
            dialog.setResizable(true);
        });

        Button warnButton = new Button("Warning");
        warnButton.setOnAction(evt -> dialogPane.showWarning("Warning Title", "A warning message is not so bad, errors are worse. Especially when the errors are runtime exceptions of the worst kind."));

        Button errorButton = new Button("Error");
        errorButton.setOnAction(evt -> dialogPane.showError("Error Title", "Error dialog message that can be somewhat longer but that is kind of important because the uer has to take errors very seriously.\n\nSome errors might be so bad that the application needs to be restarted because the process ran out of memory or a heap dump was produced somewhere."));

        Button errorWithDetailsButton = new Button("Error with details");
        errorWithDetailsButton.setOnAction(evt -> dialogPane.showError("Error Title", "Error dialog message that can be somewhat longer", "But that is kind of important because the uer has to take errors very seriously.\n\nSome errors might be so bad that the application needs to be restarted because the process ran out of memory or a heap dump was produced somewhere."));

        Button errorWithExceptionButton = new Button("Error with exception");
        errorWithExceptionButton.setOnAction(evt -> dialogPane.showError("Error Title", "Error dialog message that can be somewhat longer", new NullPointerException("A fake null pointer exception.")));

        Button confirmButton = new Button("Confirmation");
        confirmButton.setOnAction(evt -> dialogPane.showConfirmation("Confirmation Title", "A confirmation requires the user to decide."));

        Button inputSingleLineButton = new Button("Input");
        inputSingleLineButton.setOnAction(evt -> {
            Dialog<String> dialog = dialogPane.showTextInput("Text Input", "Please enter something, anything really.", "Text already there ...", false);
            dialog.setRequired(true);
        });

        Button inputMultiLineButton = new Button("Input Multi");
        inputMultiLineButton.setOnAction(evt -> {
            Dialog<String> dialog = dialogPane.showTextInput("Multiline Text Input", "Please enter something, anything really.", "Text already there ...", true);
            dialog.setRequired(true);
        });

        Button node1Button = new Button("Node 1");
        node1Button.setOnAction(evt -> {
            Dialog<Object> dialog = dialogPane.showNode(INFORMATION, "Select Person", createCustomNode(), List.of(new ButtonType("Send Mail", ButtonBar.ButtonData.OK_DONE), new ButtonType("Call", ButtonBar.ButtonData.APPLY)));
            dialog.setResizable(true);
        });

        Button node2Button = new Button("Node 2");
        node2Button.setOnAction(evt -> {
            Dialog<Object> dialog = dialogPane.showNode(INFORMATION, "Generic Node Dialog", createGenericNode());
            dialog.setResizable(true);
        });

        Button busyButton = new Button("Busy");
        busyButton.setOnAction(evt -> dialogPane.showBusyIndicator().onClose(buttonType -> {
            if (buttonType.equals(ButtonType.CANCEL)) {
                dialogPane.showInformation("Cancelled", "The busy dialog has been cancelled via the ESC key.");
            }
        }));

        Button maxButton = new Button("Maximize");
        maxButton.setOnAction(evt -> {
            Dialog<Object> dialog = new Dialog<>(dialogPane, INFORMATION);
            dialog.setTitle("Maximized");
            dialog.setContent(new Label("Dialog using all available width and height."));
            dialog.setMaximize(true);
            dialogPane.showDialog(dialog);
        });

        Button multipleDialogsButton = new Button("Multiple Dialogs");
        multipleDialogsButton.setOnAction(evt -> {
            dialogPane.showInformation("Information", "some very long information text is coming here.");
            later(() -> dialogPane.showConfirmation("Confirmation", "some info"), 1);
            later(() -> dialogPane.showWarning("Warning", "Again a warning message?"), 2);
            later(() -> dialogPane.showError("Error", "An error was encountered while running this application."), 3);
        });

        FlowPane flowPane = new FlowPane(10, 10, blankButton, infoButton, warnButton, errorButton, errorWithDetailsButton, errorWithExceptionButton,
                confirmButton, inputSingleLineButton, inputMultiLineButton, node1Button, node2Button, busyButton, multipleDialogsButton,
                maxButton);

        flowPane.setAlignment(Pos.CENTER);

        Duration duration0 = Duration.ZERO;
        Duration duration1 = Duration.millis(100);
        Duration duration2 = Duration.millis(200);
        Duration duration3 = Duration.millis(500);
        Duration duration4 = Duration.seconds(1);
        Duration duration5 = Duration.seconds(5);

        ComboBox<Duration> durationBox = new ComboBox<>();
        durationBox.getItems().setAll(duration0, duration1, duration2, duration3, duration4, duration5);
        durationBox.valueProperty().bindBidirectional(dialogPane.animationDurationProperty());

        ComboBox<String> styleBox = new ComboBox<>();
        styleBox.getItems().setAll("Default", "Dark", "Custom");
        styleBox.setValue("Default");
        styleBox.valueProperty().addListener(it -> {
            switch (styleBox.getValue()) {
                case "Default":
                    dialogPane.getStylesheets().setAll(Objects.requireNonNull(DialogPane.class.getResource("dialog-pane.css")).toExternalForm());
                    dialogPane.setConverter(null);
                    break;
                case "Dark":
                    dialogPane.getStylesheets().setAll(Objects.requireNonNull(DialogPane.class.getResource("dialog-pane.css")).toExternalForm());
                    dialogPane.getStylesheets().add(Objects.requireNonNull(DialogPaneApp.class.getResource("dialogs-dark.css")).toExternalForm());
                    dialogPane.setConverter(null);
                    break;
                case "Custom":
                    dialogPane.getStylesheets().setAll(Objects.requireNonNull(DialogPaneApp.class.getResource("dialogs-custom.css")).toExternalForm());
                    dialogPane.setConverter(new StringConverter<>() {
                        @Override
                        public String toString(ButtonType object) {
                            return object.getText().toUpperCase();
                        }

                        @Override
                        public ButtonType fromString(String string) {
                            return null;
                        }
                    });
                    break;
            }
        });

        VBox.setVgrow(flowPane, Priority.ALWAYS);
        VBox.setVgrow(durationBox, Priority.ALWAYS);

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        HBox hBox = new HBox(10, new Label("Animation:"), durationBox, new Label("Style:"), styleBox, scenicView);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(flowPane, hBox);
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

        primaryStage.setTitle("Dialogs");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1500);
        primaryStage.setHeight(900);
        primaryStage.centerOnScreen();
        primaryStage.show();

        CSSFX.start();
    }

    private void later(Runnable runnable, int counter) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(counter * 500L);
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

        Label label = new Label();
        label.textProperty().bind(Bindings.createStringBinding(() -> (int) rect.getWidth() + "x" + (int) rect.getHeight(), rect.widthProperty(), rect.heightProperty()));
        label.setStyle("-fx-text-fill: white;");

        StackPane pane = new StackPane(rect, label);
        pane.setPrefSize(300, 300);
        pane.setMinSize(300, 300);
        rect.widthProperty().bind(pane.widthProperty());
        rect.heightProperty().bind(pane.heightProperty());
        return pane;
    }

    public static class Person {

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
