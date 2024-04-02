package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SVGImageView;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class SVGImageViewApp extends Application {

    /**
     * <a href="https://www.svgrepo.com/svg/288210/phone-call-telephone">telephone svg</a>
     * CC0 License
     */
    private static final String SVG_URL = "https://www.svgrepo.com/show/288210/phone-call-telephone.svg";

    @Override
    public void start(Stage primaryStage) throws Exception {
        SVGImageView imageView = new SVGImageView();
        String svgUrl = Objects.requireNonNull(SVGImageViewApp.class.getResource("microphone.svg")).toExternalForm();
        imageView.setSvgUrl(svgUrl);

        VBox controlBox = createControlBox(primaryStage, imageView);

        StackPane imageWrapper = new StackPane(imageView);
        imageWrapper.getStyleClass().add("image-wrapper");

        ScrollPane scrollPane = new ScrollPane(imageWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        SplitPane root = new SplitPane(scrollPane, controlBox);
        root.setDividerPositions(0.7);
        Scene scene = new Scene(root, 600, 380);
        scene.getStylesheets().add(Objects.requireNonNull(SVGImageViewApp.class.getResource("svg-image-view-app.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("SVGImageView Demo");
        primaryStage.show();
    }

    private VBox createControlBox(Stage primaryStage, SVGImageView imageView) {
        Button loadSvgBtn = new Button("Load Online SVG");
        loadSvgBtn.setOnAction(evt -> imageView.setSvgUrl(SVG_URL));

        Button openSVGFileBtn = createOpenBtn(primaryStage, imageView);

        Node fitWidthControl = createSizeControl(imageView.fitWidthProperty());
        Node fitHeightControl = createSizeControl(imageView.fitHeightProperty());

        CheckBox preserveRatioCheckBox = new CheckBox("Preserve Ratio");
        preserveRatioCheckBox.selectedProperty().bindBidirectional(imageView.preserveRatioProperty());

        CheckBox backgroundLoadingCheckBox = new CheckBox("Background Loading");
        backgroundLoadingCheckBox.selectedProperty().bindBidirectional(imageView.backgroundLoadingProperty());
        VBox controlBox = new VBox(loadSvgBtn, openSVGFileBtn, fitWidthControl, fitHeightControl, preserveRatioCheckBox, backgroundLoadingCheckBox);
        controlBox.getStyleClass().add("control-box");
        return controlBox;
    }

    private Button createOpenBtn(Stage primaryStage, SVGImageView imageView) {
        Button openSVGFileBtn = new Button("Open SVG File");
        openSVGFileBtn.setOnAction(evt -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open SVG File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("SVG Files", "*.svg")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file == null) {
                return;
            }
            imageView.setSvgUrl(file.toURI().toString());
        });
        return openSVGFileBtn;
    }

    private Node createSizeControl(DoubleProperty imageView) {
        Spinner<Double> spinner = new Spinner<>(50, 300, 100, 50);
        imageView.bind(spinner.valueProperty());
        return spinner;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
