package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.infocenter.InfoCenterPane;
import com.dlsc.gemsfx.infocenter.InfoCenterView;
import com.dlsc.gemsfx.infocenter.Notification;
import com.dlsc.gemsfx.infocenter.Notification.OnClickBehaviour;
import com.dlsc.gemsfx.infocenter.NotificationAction;
import com.dlsc.gemsfx.infocenter.NotificationGroup;
import com.dlsc.gemsfx.infocenter.NotificationView;
import com.jpro.webapi.WebAPI;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.ZonedDateTime;

public class InfoCenterApp extends Application {

    private NotificationGroup<Mail, MailNotification> mailGroup = new NotificationGroup<>("Mail");
    private NotificationGroup<Object, SlackNotification> slackGroup = new NotificationGroup<>("Slack");
    private NotificationGroup<Object, CalendarNotification> calendarGroup = new NotificationGroup<>("Calendar");

    private InfoCenterPane infoCenterPane = new InfoCenterPane();

    @Override
    public void start(Stage stage) {
        slackGroup.setSortOrder(0);
        calendarGroup.setSortOrder(1);
        mailGroup.setSortOrder(2);

        slackGroup.setViewFactory(n -> {
            NotificationView<Object, SlackNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(InfoCenterApp.class.getResourceAsStream("notification/slack.png"))));
            return view;
        });

        calendarGroup.setViewFactory(n -> {
            NotificationView<Object, CalendarNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(InfoCenterApp.class.getResourceAsStream("notification/calendar.png"))));
            Region region = new Region();
            region.setMinHeight(200);
            region.setBackground(new Background(new BackgroundImage(new Image(CalendarNotification.class.getResource("notification/map.png").toExternalForm()), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true))));
            StackPane stackPane = new StackPane(region);
            stackPane.setStyle("-fx-border-color: gray;");
            view.setContent(stackPane);
            return view;
        });

        mailGroup.setViewFactory(n -> {
            NotificationView<Mail, MailNotification> view = new NotificationView<>(n);
            view.setGraphic(createImageView(new Image(InfoCenterApp.class.getResourceAsStream("notification/mail.png"))));
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
        transparentButton.selectedProperty().addListener(it -> {
            if (transparentButton.isSelected()) {
                infoCenterView.getStyleClass().add("transparent");
            } else {
                infoCenterView.getStyleClass().remove("transparent");
            }
        });

        Label counterLabel = new Label();
        counterLabel.setStyle("-fx-text-fill: white;");
        counterLabel.textProperty().bind(Bindings.createStringBinding(() -> "Count: " + infoCenterView.getUnmodifiableNotifications().size(), infoCenterView.getUnmodifiableNotifications()));

        VBox buttonBox = new VBox(10, showNotifications, hideNotifications, pinNotifications, autoOpenGroups, randomNotification, manyNotifications, clearAll, transparentButton, counterLabel);
        buttonBox.getStyleClass().add("button-box");
        buttonBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        buttonBox.setTranslateX(50);
        buttonBox.setTranslateY(50);

        StackPane.setAlignment(buttonBox, Pos.TOP_LEFT);

        StackPane background = new StackPane(buttonBox);
        background.getStyleClass().add("background");
        infoCenterPane.setContent(background);

        Scene scene = new Scene(infoCenterPane);
        infoCenterPane.getStylesheets().add(InfoCenterApp.class.getResource("notification/scene.css").toExternalForm());
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(800);
        if(!WebAPI.isBrowser()) {
            stage.centerOnScreen();
        }
        stage.setTitle("InfoCenter");
        stage.show();
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
        Mail mail = new Mail("Purchase Order #8774911", "Dear Mr. Smith, the following order has been\nreceived by our service counter.", ZonedDateTime.now());
        MailNotification mailNotification = new MailNotification(mail);

        NotificationAction<Mail> openMailAction = new NotificationAction<>("Open", (notification) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Opening Mail ...");
            alert.initOwner(infoCenterPane.getScene().getWindow());
            alert.show();
            return OnClickBehaviour.HIDE_AND_REMOVE;
        });

        NotificationAction<Mail> deleteMailAction = new NotificationAction<>("Delete", (notification) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Deleting Mail ...");
            alert.initOwner(infoCenterPane.getScene().getWindow());
            alert.show();
            return OnClickBehaviour.HIDE_AND_REMOVE;
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

    public class CalendarNotification extends Notification<Object> {

        public CalendarNotification(String title, String description) {
            super(title, description);
            setOnClick(notification -> OnClickBehaviour.HIDE_AND_REMOVE);
        }
    }

    public class SlackNotification extends Notification<Object> {

        public SlackNotification(String title, String description) {
            super(title, description);
            setOnClick(notification -> OnClickBehaviour.REMOVE);
        }
    }

    public class MailNotification extends Notification<Mail> {

        public MailNotification(Mail mail) {
            super(mail.getTitle(), mail.getDescription(), mail.getDateTime());
            setUserObject(mail);
            setOnClick(notification -> OnClickBehaviour.NONE);
        }
    }

    public class Mail {

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


    public static void main(String[] args) {
        launch();
    }
}
