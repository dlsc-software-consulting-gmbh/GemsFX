package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.PowerPane;
import com.dlsc.gemsfx.infocenter.*;
import com.dlsc.pdfviewfx.PDFView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.HiddenSidesPane;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static com.dlsc.gemsfx.DialogPane.Type.INFORMATION;

public class PowerPaneApp extends Application {

    private DialogPane dialogPane;
    private InfoCenterPane infoCenterPane;
    private HiddenSidesPane hiddenSidesPane;

    @Override
    public void start(Stage stage) {
        Button testMe = new Button("Test Me");
        PowerPane powerPane = new PowerPane(testMe);

        RegionView leftHiddenSide = new RegionView("Left Hidden");
        RegionView rightHiddenSide = new RegionView("Right Hidden");
        RegionView topHiddenSide = new RegionView("Top Hidden");
        RegionView bottomHiddenSide = new RegionView("Bottom Hidden");

        hiddenSidesPane = powerPane.getHiddenSidesPane();
        hiddenSidesPane.setLeft(leftHiddenSide);
        hiddenSidesPane.setRight(rightHiddenSide);
        hiddenSidesPane.setTop(topHiddenSide);
        hiddenSidesPane.setBottom(bottomHiddenSide);

        testMe.setOnAction(evt -> {
            System.out.println("button pressed");
            powerPane.getDialogPane().showInformation("Info", "Button pressed!");
        });

        dialogPane = powerPane.getDialogPane();
        infoCenterPane = powerPane.getInfoCenterPane();

        PDFView pdfView = new PDFView();
        pdfView.setStyle("-fx-border-color: black;");
        pdfView.load(Objects.requireNonNull(PowerPaneApp.class.getResourceAsStream("tesla-manual.pdf")));
        powerPane.getDrawerStackPane().setDrawerContent(pdfView);

        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 1.5em;");

        ToggleButton showDrawerButton = new ToggleButton("Show Drawer");
        showDrawerButton.setMaxWidth(Double.MAX_VALUE);
        showDrawerButton.selectedProperty().bindBidirectional(powerPane.getDrawerStackPane().showDrawerProperty());

        VBox controls = new VBox(10,
                titleLabel,
                new Label("Drawer"),
                showDrawerButton,
                new Label("Dialogs"),
                createDialogControls(),
                new Label("Info Center"),
                createInfoCenterPaneControls(),
                new Label("Hidden Sides"),
                createHiddenSidesPaneControls()
        );

        controls.setStyle("-fx-background-color: aliceblue;");
        controls.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(controls);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(powerPane);
        borderPane.setRight(scrollPane);

        BlueprintCanvas blueprintCanvas = new BlueprintCanvas();

        StackPane stackPane = new StackPane(blueprintCanvas, borderPane);

        blueprintCanvas.widthProperty().bind(stackPane.widthProperty());
        blueprintCanvas.heightProperty().bind(stackPane.heightProperty());

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        stage.setTitle("PowerPane");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(950);
        stage.centerOnScreen();
        stage.show();
    }

    private static class RegionView extends Label {

        public RegionView(String text) {
            super(text);
            setAlignment(Pos.CENTER);
            setMinSize(100, 100);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setStyle("-fx-background-color: transparent; -fx-border-insets: 5px; -fx-border-style: solid; -fx-border-width: 3px; -fx-border-color: white; -fx-font-size: 2em; -fx-text-fill: white;");
        }
    }

    private Node createDialogControls() {
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
        node1Button.setOnAction(evt -> dialogPane.showNode(INFORMATION, "Select Person", createCustomNode(), List.of(new ButtonType("Send Mail", ButtonBar.ButtonData.OK_DONE), new ButtonType("Call", ButtonBar.ButtonData.APPLY))));

        Button node2Button = new Button("Node 2");
        node2Button.setOnAction(evt -> dialogPane.showNode(INFORMATION, "Generic Node Dialog", createGenericNode()));

        Button busyButton = new Button("Busy");
        busyButton.setOnAction(evt -> dialogPane.showBusyIndicator().onClose(buttonType -> {
            if (buttonType.equals(ButtonType.CANCEL)) {
                dialogPane.showInformation("Cancelled", "The busy dialog has been cancelled via the ESC key.");
            }
        }));

        Button maxButton = new Button("Maximize");
        maxButton.setOnAction(evt -> {
            DialogPane.Dialog<Object> dialog = new DialogPane.Dialog<>(dialogPane, INFORMATION);
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

        VBox vBox = new VBox(10, infoButton, warnButton, errorButton, confirmButton,
                inputSingleLineButton, inputMultiLineButton, node1Button, node2Button, busyButton,
                overlappingButton, maxButton);

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

        HBox hBox = new HBox(10, new Label("Animation:"), durationBox, new Label("Style:"), styleBox);
        hBox.setAlignment(Pos.CENTER);

        vBox.getChildren().add(hBox);

        vBox.getChildren().forEach(node -> ((Region) node).setMaxWidth(Double.MAX_VALUE));
        return vBox;
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
        rect.setWidth(300);
        rect.setHeight(300);

        Label label = new Label("300 x 300");
        label.setStyle("-fx-text-fill: white;");

        return new StackPane(rect, label);
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

    private Node createHiddenSidesPaneControls() {
        Button unpin = new Button("Unpin");
        unpin.setOnAction(evt -> hiddenSidesPane.setPinnedSide(null));
        unpin.setMaxWidth(Double.MAX_VALUE);

        Button pinLeft = new Button("Pin Left");
        pinLeft.setOnAction(evt -> hiddenSidesPane.setPinnedSide(Side.LEFT));
        pinLeft.setMaxWidth(Double.MAX_VALUE);

        Button pinRight = new Button("Pin Right");
        pinRight.setOnAction(evt -> hiddenSidesPane.setPinnedSide(Side.RIGHT));
        pinRight.setMaxWidth(Double.MAX_VALUE);

        Button pinTop = new Button("Pin Top");
        pinTop.setOnAction(evt -> hiddenSidesPane.setPinnedSide(Side.TOP));
        pinTop.setMaxWidth(Double.MAX_VALUE);

        Button pinBottom = new Button("Pin Bottom");
        pinBottom.setOnAction(evt -> hiddenSidesPane.setPinnedSide(Side.BOTTOM));
        pinBottom.setMaxWidth(Double.MAX_VALUE);

        return new VBox(10, unpin, pinLeft, pinRight, pinTop, pinBottom);
    }

    private final NotificationGroup<Mail, MailNotification> mailGroup = new NotificationGroup<>("Mail");
    private final NotificationGroup<Object, SlackNotification> slackGroup = new NotificationGroup<>("Slack");
    private final NotificationGroup<Object, CalendarNotification> calendarGroup = new NotificationGroup<>("Calendar");

    private Node createInfoCenterPaneControls() {
        slackGroup.setSortOrder(0);
        calendarGroup.setSortOrder(1);
        mailGroup.setSortOrder(2);

        slackGroup.maximumNumberOfNotificationsProperty().bind(Bindings.createIntegerBinding(() -> slackGroup.isPinned() ? 3 : 10, slackGroup.pinnedProperty()));
        calendarGroup.maximumNumberOfNotificationsProperty().bind(Bindings.createIntegerBinding(() -> calendarGroup.isPinned() ? 3 : 10, calendarGroup.pinnedProperty()));
        mailGroup.maximumNumberOfNotificationsProperty().bind(Bindings.createIntegerBinding(() -> mailGroup.isPinned() ? 3 : 10, mailGroup.pinnedProperty()));

        slackGroup.setViewFactory(n -> {
            NotificationView<Object, SlackNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(Objects.requireNonNull(PowerPaneApp.class.getResourceAsStream("notification/slack.png")))));
            return view;
        });

        calendarGroup.setViewFactory(n -> {
            NotificationView<Object, CalendarNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(Objects.requireNonNull(PowerPaneApp.class.getResourceAsStream("notification/calendar.png")))));
            Region region = new Region();
            region.setMinHeight(200);
            region.setBackground(new Background(new BackgroundImage(new Image(Objects.requireNonNull(CalendarNotification.class.getResource("notification/map.png")).toExternalForm()), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true))));
            StackPane stackPane = new StackPane(region);
            stackPane.setStyle("-fx-border-color: grey;");
            view.setContent(stackPane);
            return view;
        });

        mailGroup.setViewFactory(n -> {
            NotificationView<Mail, MailNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(Objects.requireNonNull(PowerPaneApp.class.getResourceAsStream("notification/mail.png")))));
            return view;
        });

        InfoCenterView infoCenterView = infoCenterPane.getInfoCenterView();
        infoCenterView.getGroups().setAll(mailGroup, slackGroup, calendarGroup);

        for (int i = 0; i < 10; i++) {
            assignNotification(createNotification(true));
        }

        Button randomNotification = new Button("Random Notification (Current Time)");
        randomNotification.setOnAction(evt -> assignNotification(createNotification(false)));
        randomNotification.setMaxWidth(Double.MAX_VALUE);

        Button manyNotifications = new Button("Many Notifications (Random Times)");
        manyNotifications.setOnAction(evt -> {
            for (int i = 0; i < 10; i++) {
                assignNotification(createNotification(true));
            }
        });
        manyNotifications.setMaxWidth(Double.MAX_VALUE);

        Button clearAll = new Button("Clear All");
        clearAll.setOnAction(evt -> infoCenterView.clearAll());
        clearAll.setMaxWidth(Double.MAX_VALUE);

        Button showNotifications = new Button("Show Info Center");
        showNotifications.setOnAction(evt -> infoCenterPane.setShowInfoCenter(true));
        showNotifications.setMaxWidth(Double.MAX_VALUE);

        Button hideNotifications = new Button("Hide Info Center");
        hideNotifications.setOnAction(evt -> infoCenterPane.setShowInfoCenter(false));
        hideNotifications.setMaxWidth(Double.MAX_VALUE);

        ToggleButton pinNotifications = new ToggleButton("Pin Info Center");
        pinNotifications.selectedProperty().bindBidirectional(infoCenterPane.pinnedProperty());
        pinNotifications.setMaxWidth(Double.MAX_VALUE);

        ToggleButton autoOpenGroups = new ToggleButton("Auto Open Groups");
        autoOpenGroups.selectedProperty().bindBidirectional(infoCenterPane.getInfoCenterView().autoOpenGroupProperty());
        autoOpenGroups.setMaxWidth(Double.MAX_VALUE);

        ToggleButton transparentButton = new ToggleButton("Make Transparent");
        transparentButton.setMaxWidth(Double.MAX_VALUE);
        transparentButton.selectedProperty().bindBidirectional(infoCenterView.transparentProperty());

        Label counterLabel = new Label();
        counterLabel.setStyle("-fx-text-fill: white;");
        counterLabel.textProperty().bind(Bindings.createStringBinding(() -> "Count: " + infoCenterView.getUnmodifiableNotifications().size(), infoCenterView.getUnmodifiableNotifications()));

        return new VBox(10, showNotifications, hideNotifications, pinNotifications, autoOpenGroups, randomNotification, manyNotifications, clearAll, transparentButton, counterLabel);
    }

    private void assignNotification(Notification<?> notification) {
        if (notification instanceof MailNotification) {
            mailGroup.getNotifications().add((MailNotification) notification);
        } else if (notification instanceof CalendarNotification) {
            calendarGroup.getNotifications().add((CalendarNotification) notification);
        } else if (notification instanceof SlackNotification) {
            slackGroup.getNotifications().add((SlackNotification) notification);
        }
    }

    private static ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(42);
        imageView.setFitHeight(42);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private Notification<?> createNotification(boolean randomizeTimeStamp) {
        Notification notification;
        switch ((int) (Math.random() * 3)) {
            case 0:
                notification = createMailNotification();
                break;
            case 1:
                notification = new SlackNotification("DLSC GmbH\nDirk Lemmermann", "Please send the material I requested.");
                break;
            case 2:
            default:
                notification = new CalendarNotification("Calendar", "Meeting with shareholders");
        }

        if (randomizeTimeStamp) {
            notification.setDateTime(createTimeStamp());
        }

        return notification;
    }

    private MailNotification createMailNotification() {
        Mail mail = new Mail("Purchase Order #8774911", "Dear Mr. Smith, the following order has been received by our service counter.", ZonedDateTime.now());
        MailNotification mailNotification = new MailNotification(mail);

        NotificationAction<Mail> openMailAction = new NotificationAction<>("Open", (notification) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Opening Mail ...");
            alert.initOwner(infoCenterPane.getScene().getWindow());
            alert.show();
            return Notification.OnClickBehaviour.HIDE_AND_REMOVE;
        });

        NotificationAction<Mail> deleteMailAction = new NotificationAction<>("Delete", (notification) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Deleting Mail ...");
            alert.initOwner(infoCenterPane.getScene().getWindow());
            alert.show();
            return Notification.OnClickBehaviour.HIDE_AND_REMOVE;
        });

        mailNotification.getActions().add(openMailAction);
        mailNotification.getActions().add(deleteMailAction);

        return mailNotification;
    }

    private ZonedDateTime createTimeStamp() {
        ZonedDateTime time = ZonedDateTime.now();
        time = time.minusDays((int) (Math.random() * 2));
        time = time.minusMinutes((int) (Math.random() * 30));
        time = time.minusSeconds((int) (Math.random() * 45));
        return time;
    }

    public static class CalendarNotification extends Notification<Object> {

        public CalendarNotification(String title, String description) {
            super(title, description);
            setOnClick(notification -> OnClickBehaviour.HIDE_AND_REMOVE);
        }
    }

    public static class SlackNotification extends Notification<Object> {

        public SlackNotification(String title, String description) {
            super(title, description);
            setOnClick(notification -> OnClickBehaviour.REMOVE);
        }
    }

    public static class MailNotification extends Notification<Mail> {

        public MailNotification(Mail mail) {
            super(mail.getTitle(), mail.getDescription(), mail.getDateTime());
            setUserObject(mail);
            setOnClick(notification -> OnClickBehaviour.NONE);
        }
    }

    public static class Mail {

        private String title;
        private String description;
        private ZonedDateTime dateTime;

        public Mail(String title, String description, ZonedDateTime dateTime) {
            this.title = title;
            this.description = description;
            this.dateTime = dateTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ZonedDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(ZonedDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    static class BlueprintCanvas extends Canvas {

        public BlueprintCanvas() {
            widthProperty().addListener(it -> draw());
            heightProperty().addListener(it -> draw());
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        private void draw() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.setGlobalAlpha(1);

            gc.clearRect(0, 0, getWidth(), getHeight());
            gc.setFill(Color.CORNFLOWERBLUE);
            gc.fillRect(0, 0, getWidth(), getHeight());
            gc.setStroke(Color.WHITE);
            gc.setGlobalAlpha(.5);

            int GRID_SIZE = 20;

            for (int x = 0; x < getWidth(); x += GRID_SIZE) {
                gc.strokeLine(x, 0, x, getHeight());
            }

            for (int y = 0; y < getHeight(); y += GRID_SIZE) {
                gc.strokeLine(0, y, getWidth(), y);
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
