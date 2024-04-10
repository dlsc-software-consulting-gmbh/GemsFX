package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ResponsivePane;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ResponsivePaneApp extends Application {

    private final ResponsivePane responsivePane = new ResponsivePane();

    private StackPane mainContent;
    private Region smallSidebar;
    private Region largeSidebar;

    @Override
    public void start(Stage stage) throws Exception {
        Label widthLabel = new Label();
        widthLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        widthLabel.setStyle("-fx-background-color: pink; -fx-alignment: center; -fx-padding: 5px 10px;");
        widthLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("ResponsivePane: %.0f x %.0f", responsivePane.getWidth(), responsivePane.getHeight()), responsivePane.widthProperty(), responsivePane.heightProperty()));

        mainContent = new StackPane(widthLabel);
        mainContent.setStyle("-fx-background-color: #eeeeee;");
        mainContent.setPrefSize(400, 500);

        smallSidebar = new StackPane(new Label("Small"));
        smallSidebar.setPrefSize(50, 50);
        smallSidebar.setStyle("-fx-background-color: -green-70; -fx-border-color: white; -fx-border-insets: 1px;");

        largeSidebar = new StackPane(new Label("Large"));
        largeSidebar.setPrefSize(150, 150);
        largeSidebar.setStyle("-fx-background-color: rgba(43,83,241,0.5); -fx-border-color: white; -fx-border-insets: 1px;");

        responsivePane.setSmallSidebar(smallSidebar);
        responsivePane.setLargeSidebar(largeSidebar);
        responsivePane.setContent(mainContent);
        responsivePane.setForceLargeSidebarDisplay(true);
        responsivePane.setLargeSidebarCoversSmall(false);
        responsivePane.setGap(10);
        responsivePane.setStyle("-fx-background-color: white;");

        BorderPane borderPane = new BorderPane(responsivePane);
        borderPane.setRight(createControlPanel());

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private Node createControlPanel() {
        ComboBox<Side> sideComboBox = new ComboBox<>();
        sideComboBox.getItems().addAll(Side.values());
        sideComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Side side) {
                return side.name();
            }

            @Override
            public Side fromString(String s) {
                return null;
            }
        });
        sideComboBox.valueProperty().bindBidirectional(responsivePane.sideProperty());
        sideComboBox.setMaxWidth(Double.MAX_VALUE);

        Spinner<Double> gapSpinner = new Spinner<>(0, 10, 0, 2);
        responsivePane.gapProperty().bind(gapSpinner.valueProperty());
        gapSpinner.setMaxWidth(Double.MAX_VALUE);

        CheckBox largeSidebarCoverCheckBox = new CheckBox("Large Covers Small");
        largeSidebarCoverCheckBox.selectedProperty().bindBidirectional(responsivePane.largeSidebarCoversSmallProperty());

        CheckBox forceLargeDisplayCheckBox = new CheckBox("Force Display Large");
        forceLargeDisplayCheckBox.selectedProperty().bindBidirectional(responsivePane.forceLargeSidebarDisplayProperty());

        VBox controlBox = new VBox(new Label("Side"), sideComboBox, new Label("Gap"), gapSpinner,
                new Label("Content PrefSize"), createSizeInfoField(mainContent), new Label("Small Sidebar PrefSize"),
                createSizeInfoField(smallSidebar), new Label("Large Sidebar PrefSize"), createSizeInfoField(largeSidebar), forceLargeDisplayCheckBox, largeSidebarCoverCheckBox);
        controlBox.setSpacing(10);
        controlBox.setPadding(new Insets(10));
        return controlBox;
    }

    public TextField createSizeInfoField(Region node) {
        TextField textField = new TextField();
        textField.setEditable(false);
        textField.setDisable(true);
        textField.textProperty().bind(Bindings.createStringBinding(() ->
                        String.format("%.0f x %.0f", node.getPrefWidth(), node.getPrefHeight()),
                node.prefWidthProperty(), node.prefHeightProperty()));
        return textField;
    }
}
