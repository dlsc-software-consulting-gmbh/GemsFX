package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.util.ResizingBehaviour;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

public class ResizingBehaviourApp extends Application {

    private ResizingBehaviour resizingSupport;

    @Override
    public void start(Stage stage) {
        Label content = new Label("Content");
        content.setMouseTransparent(false);
        content.setStyle("-fx-background-color: orange;");
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(content);
        stackPane.setStyle("-fx-background-color: blue;");
        stackPane.setPadding(new Insets(10));
        stackPane.setPrefSize(250, 250);
        stackPane.setMaxSize(950, 850);

        resizingSupport = ResizingBehaviour.install(stackPane);
        resizingSupport.setResizable(true);
        resizingSupport.setOnResize((width, height) -> System.out.println("width: " + width + ", height: " + height));
        Label installLabel = new Label("ResizingBehaviour already installed.");

        CheckComboBox<ResizingBehaviour.Operation> supportedOperationsBox = createOperationCheckComboBox();

        CheckBox resizeableBox = new CheckBox("Resizable");
        resizeableBox.selectedProperty().bindBidirectional(resizingSupport.resizableProperty());

        Spinner<Double> offsetSpinner = new Spinner<>(5, 20, 5, 5);
        resizingSupport.offsetProperty().bind(offsetSpinner.valueProperty());

        Button uninstallButton = new Button("Uninstall");
        uninstallButton.setOnAction(e -> {
            if (resizingSupport.isInstalled()) {
                resizingSupport.uninstall();
                installLabel.setText("Tips: ResizingBehaviour already uninstalled. All resizing operations are disabled.");
                installLabel.setTextFill(Color.RED);
                installLabel.setFont(Font.font(16));
            }
        });

        HBox controlsBox = new HBox(5,
                new Label("Supported Operations: "), supportedOperationsBox, new Separator(Orientation.VERTICAL),
                resizeableBox, new Separator(Orientation.VERTICAL),
                new Label("Edge Offset:"), offsetSpinner,new Separator(Orientation.VERTICAL),
                uninstallButton);
        controlsBox.setAlignment(Pos.CENTER_RIGHT);
        VBox controlsContainer = new VBox(10, controlsBox, installLabel);
        controlsContainer.setAlignment(Pos.CENTER_RIGHT);
        controlsContainer.setPadding(new Insets(10));

        Group group = new Group(stackPane);

        BorderPane container = new BorderPane(group);
        container.setBottom(controlsContainer);

        Scene scene = new Scene(container);
        CSSFX.start(scene);

        stage.setTitle("Resizable Pane");
        stage.setScene(scene);
        stage.setWidth(1090);
        stage.setHeight(900);
        stage.centerOnScreen();
        stage.show();
    }

    private CheckComboBox<ResizingBehaviour.Operation> createOperationCheckComboBox() {
        CheckComboBox<ResizingBehaviour.Operation> supportedOperationsBox = new CheckComboBox<>();
        supportedOperationsBox.getItems().addAll(ResizingBehaviour.Operation.values());
        supportedOperationsBox.setConverter(createOperationStringConverter());
        supportedOperationsBox.getCheckModel().checkAll();
        supportedOperationsBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) (c) -> {
            resizingSupport.setSupportedOperations(supportedOperationsBox.getCheckModel().getCheckedItems());
        });
        return supportedOperationsBox;
    }

    private StringConverter<ResizingBehaviour.Operation> createOperationStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(ResizingBehaviour.Operation operation) {
                if (operation == null) {
                    return "";
                }
                return switch (operation) {
                    case RESIZE_E -> "Right";
                    case RESIZE_W -> "Left";
                    case RESIZE_N -> "Top";
                    case RESIZE_NE -> "Top Right";
                    case RESIZE_NW -> "Top Left";
                    case RESIZE_S -> "Bottom";
                    case RESIZE_SE -> "Bottom Right";
                    case RESIZE_SW -> "Bottom Left";
                };
            }

            @Override
            public ResizingBehaviour.Operation fromString(String string) {
                return null;
            }
        };
    }

    public static void main(String[] args) {
        launch();
    }
}
