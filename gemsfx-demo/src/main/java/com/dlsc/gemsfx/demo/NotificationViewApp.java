package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.infocenter.InfoCenterView;
import com.dlsc.gemsfx.infocenter.Notification;
import com.dlsc.gemsfx.infocenter.NotificationAction;
import com.dlsc.gemsfx.infocenter.NotificationGroup;
import com.dlsc.gemsfx.infocenter.NotificationView;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.Objects;

public class NotificationViewApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);

        NotificationGroup<Object, Notification<Object>> group = new NotificationGroup<>("Group");
        Notification<Object> notification = new Notification<>("Notification", "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");
        group.getNotifications().add(notification);
        notification.setType(Notification.Type.WARNING);

        NotificationAction<InfoCenterApp.Mail> openMailAction = new NotificationAction<>("Action 1", (n) -> Notification.OnClickBehaviour.HIDE_AND_REMOVE);
        NotificationAction<InfoCenterApp.Mail> deleteMailAction = new NotificationAction<>("Action 2", (n) -> Notification.OnClickBehaviour.HIDE_AND_REMOVE);

        notification.getActions().add(openMailAction);
        notification.getActions().add(deleteMailAction);

        NotificationView<Object, Notification<Object>> view = new NotificationView<>(notification);
        view.setPrefWidth(500);

        view.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane root = new StackPane(view);
        root.getStylesheets().add(Objects.requireNonNull(InfoCenterView.class.getResource("info-center-view.css")).toExternalForm());
        root.getStyleClass().add("info-center-view"); // fake the normal parent container

        ComboBox<Notification.Type> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(Notification.Type.values());
        typeComboBox.valueProperty().bindBidirectional(notification.typeProperty());

        CheckBox expandedBox = new CheckBox("Expanded");
        notification.expandedProperty().bindBidirectional(expandedBox.selectedProperty());

        HBox box = new HBox(10, typeComboBox, expandedBox);
        box.setStyle("-fx-padding: 10px; -fx-border-width: 1px;");

        box.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane(root);
        borderPane.setBottom(box);
        borderPane.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(borderPane);

        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.setTitle("Notification View Demo");
        stage.show();

        ScenicView.show(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
