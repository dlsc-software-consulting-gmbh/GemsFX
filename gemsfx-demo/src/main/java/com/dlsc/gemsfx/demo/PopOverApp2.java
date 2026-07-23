package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PopOver;
import com.dlsc.gemsfx.PopOver.ArrowLocation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PopOverApp2 extends GemApplication {

    private final ObjectProperty<ArrowLocation> preferredArrowLocation = new SimpleObjectProperty<>(ArrowLocation.TOP_CENTER);

    private final BooleanProperty autoHide = new SimpleBooleanProperty(true);

    @Override
    public void start(Stage stage) {
        StackPane buttonPane = new StackPane();
        buttonPane.setStyle("-fx-padding: 20px;");
        for (Pos pos : Pos.values()) {
            switch (pos) {
                case BASELINE_CENTER, BASELINE_LEFT, BASELINE_RIGHT:
                    continue;
                default:
                    createButton(pos, buttonPane);
            }
        }

        BorderPane root = new BorderPane();
        root.setLeft(createSidePanel());
        root.setCenter(buttonPane);

        Scene scene = new Scene(root);
        stage.setTitle("Pop Over");
        stage.setScene(scene);
        stage.setWidth(600);
        stage.setHeight(500);
        stage.show();
    }

    private VBox createSidePanel() {
        Label title = new Label("Preferred Arrow Location");

        ComboBox<ArrowLocation> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ArrowLocation.values());
        comboBox.valueProperty().bindBidirectional(preferredArrowLocation);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("The popover uses this as its preferred arrow location. It may still be flipped automatically to stay on screen.");
        hint.setWrapText(true);

        CheckBox autoHideBox = new CheckBox("Auto hide");
        autoHideBox.selectedProperty().bindBidirectional(autoHide);

        VBox sidePanel = new VBox(10, title, comboBox, hint, autoHideBox);
        sidePanel.setPadding(new Insets(20));
        sidePanel.setPrefWidth(220);
        sidePanel.setStyle("-fx-border-color: -fx-box-border; -fx-border-width: 0 1px 0 0;");
        return sidePanel;
    }

    private void createButton(Pos pos, StackPane root) {
        Button button = new Button(pos.toString());
        StackPane.setAlignment(button, pos);
        root.getChildren().add(button);
        PopOver popOver = new PopOver();
        popOver.arrowLocationProperty().bind(preferredArrowLocation);
        popOver.autoHideProperty().bind(autoHide);
        Label content = new Label("PopOver for " + pos.toString());
        content.setPrefSize(250, 250);
        popOver.setContentNode(content);
        button.setOnAction(evt -> popOver.show(button));
    }
}
