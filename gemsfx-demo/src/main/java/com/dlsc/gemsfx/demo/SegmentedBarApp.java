package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SegmentedBar;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SegmentedBarApp extends GemApplication {

    private final VBox vbox = new VBox(40);

    private final HBox hbox = new HBox(40);

    private final SegmentedBar<IssueStatusSegment> issueStatusBar = new SegmentedBar<>();

    private final SegmentedBar<SegmentedBar.Segment> simpleBar = new SegmentedBar<>();

    private final SegmentedBar<TypeSegment> typesBar = new SegmentedBar<>();

    private final StackPane contentPane = new StackPane();

    private final DoubleProperty minSegmentSize = new SimpleDoubleProperty(5d);

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    @Override
    public void start(Stage primaryStage) {
        hbox.setAlignment(Pos.CENTER);
        vbox.setAlignment(Pos.CENTER);

        ComboBox<Orientation> orientationComboBox = new ComboBox<>();
        orientationComboBox.getItems().addAll(Orientation.values());
        orientationComboBox.getSelectionModel().select(Orientation.HORIZONTAL);
        orientationComboBox.valueProperty().bindBidirectional(orientation);

        ComboBox<Double> minSizeComboBox = new ComboBox<>();
        minSizeComboBox.getItems().addAll(5d, 10d, 20d, 30d, 40d);
        minSizeComboBox.valueProperty().bindBidirectional(minSegmentSize.asObject());

        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(20));
        
        hbox.setFillHeight(true);
        hbox.setPadding(new Insets(20));

        // The out-of-the-box bar. It uses the already set default cell factory.
        simpleBar.orientationProperty().bind(orientation);
        simpleBar.minSegmentSizeProperty().bind(minSegmentSize);
        simpleBar.getSegments().addAll(
                new SegmentedBar.Segment(1, "1"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(40, "40"),
                new SegmentedBar.Segment(30, "30"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(50, "50"));

        // A bar used for visualising the number of issues (e.g. JIRA) based on
        // their status.
        issueStatusBar.orientationProperty().bind(orientation);
        issueStatusBar.setSegmentViewFactory(IssueStatusSegmentView::new);
        issueStatusBar.minSegmentSizeProperty().bind(minSegmentSize);
        issueStatusBar.getSegments().addAll(
                new IssueStatusSegment(1, IssueStatus.TODO),
                new IssueStatusSegment(1000, IssueStatus.IN_PROGRESS));

        // A bar used to visualize the disk space used by various media types (e.g. iTunes).
        typesBar.orientationProperty().bind(orientation);
        typesBar.setSegmentViewFactory(TypeSegmentView::new);
        typesBar.minSegmentSizeProperty().bind(minSegmentSize);
        typesBar.getSegments().addAll(
                new TypeSegment(14, MediaType.PHOTOS),
                new TypeSegment(32, MediaType.VIDEO),
                new TypeSegment(9, MediaType.APPS),
                new TypeSegment(40, MediaType.MUSIC),
                new TypeSegment(5, MediaType.OTHER),
                new TypeSegment(35, MediaType.FREE)
        );


        StackPane.setAlignment(vbox, Pos.CENTER);
        StackPane.setAlignment(hbox, Pos.CENTER);

        orientation.addListener(it -> updateView());

        updateView();

        HBox settingsBox = new HBox(10, new Label("Min Segment Size"), minSizeComboBox, new Label("Orientation"), orientationComboBox);
        settingsBox.setAlignment(Pos.CENTER);

        VBox contentWrapper = new VBox(20, contentPane, settingsBox);
        contentWrapper.setAlignment(Pos.CENTER);
        contentWrapper.setStyle("-fx-padding: 20px;");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        Scene scene = new Scene(contentWrapper);
        primaryStage.setTitle("Segmented Bar Demo");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();

        orientationComboBox.valueProperty().addListener(it -> primaryStage.sizeToScene());
    }

    private void updateView() {
        contentPane.getChildren().clear();

        if (orientation.get().equals(Orientation.HORIZONTAL)) {
            vbox.getChildren().clear();
            vbox.getChildren().add(new WrapperPane("Simple Bar", simpleBar));
            vbox.getChildren().add(new WrapperPane("Issue Status (Hover for Tooltip)", issueStatusBar));
            vbox.getChildren().add(new WrapperPane("Disk Usage (Hover for Tooltip)", typesBar));
            contentPane.getChildren().setAll(vbox);
        } else {
            hbox.getChildren().clear();
            hbox.getChildren().add(simpleBar);
            hbox.getChildren().add(issueStatusBar);
            hbox.getChildren().add(typesBar);
            contentPane.getChildren().setAll(hbox);
        }
    }

    private class WrapperPane extends VBox {

        public WrapperPane(String title, SegmentedBar bar) {
            this(title, bar, bar);
        }

        public WrapperPane(String title, SegmentedBar bar, Node content) {
            BorderPane.setMargin(content, new Insets(5, 0, 0, 0));
            getChildren().add(new Label(title));
            getChildren().add(content);

            Label total = new Label();
            getChildren().add(total);
            total.setText("Total: " + bar.getTotal());
            bar.totalProperty().addListener(it -> total.setText("Total: " + bar.getTotal()));
        }
    }

    public class TypeSegmentView extends StackPane {

        private final Label label;

        public TypeSegmentView(TypeSegment segment) {
            label = new Label();
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.2em;");
            label.setTextOverrun(OverrunStyle.CLIP);
            label.textProperty().bind(segment.textProperty());
            StackPane.setAlignment(label, Pos.CENTER_LEFT);

            getChildren().add(label);
            switch (segment.getType()) {
                case APPS:
                    setStyle("-fx-background-color: orange;");
                    break;
                case FREE:
                    setStyle("-fx-border-width: 1px; -fx-background-color: steelblue;");
                    break;
                case OTHER:
                    setStyle("-fx-background-color: green;");
                    break;
                case PHOTOS:
                    setStyle("-fx-background-color: purple;");
                    break;
                case VIDEO:
                    setStyle("-fx-background-color: cadetblue;");
                    break;
                case MUSIC:
                    setStyle("-fx-background-color: lightcoral;");
                    break;
            }
            setPadding(new Insets(5));
            setPrefHeight(30);

            Tooltip.install(this, new Tooltip(segment.getType().name() +": " + segment.getValue()));
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            label.setVisible(label.prefWidth(-1) < getWidth() - getPadding().getLeft() - getPadding().getRight());
        }
    }

    public class IssueStatusSegmentView extends Region {

        public IssueStatusSegmentView(final IssueStatusSegment segment) {
            setPrefHeight(16);
            setPrefWidth(16);

            switch (segment.getStatus()) {
                case DONE:
                    setStyle("-fx-background-color: green;");
                    break;
                case IN_PROGRESS:
                    setStyle("-fx-background-color: orange;");
                    break;
                case TODO:
                    setStyle("-fx-background-color: steelblue;");
                    break;
            }

            ContextMenu menu = new ContextMenu();
            for (int i = 1; i <= 10; i++) {
                MenuItem item = new MenuItem(Integer.toString(i));
                final int value = i;
                item.setOnAction(evt -> segment.setValue(value));
                menu.getItems().add(item);
            }

            setOnContextMenuRequested(evt -> menu.show(getScene().getWindow()));

            Tooltip.install(this, new Tooltip(segment.getStatus().name() +": " + segment.getValue()));
        }
    }

    public enum MediaType {
        MUSIC,
        VIDEO,
        FREE,
        OTHER,
        PHOTOS,
        APPS;
    }

    public static class TypeSegment extends SegmentedBar.Segment {

        private final MediaType type;

        public TypeSegment(double value, MediaType type) {
            super(value);
            this.type = type;

            switch (type) {
                case APPS:
                    setText("Apps");
                    break;
                case FREE:
                    setText("Free");
                    break;
                case OTHER:
                    setText("Other");
                    break;
                case PHOTOS:
                    setText("Photos");
                    break;
                case VIDEO:
                    setText("Video");
                    break;
                case MUSIC:
                    setText("Music");
                    break;
            }
        }

        public MediaType getType() {
            return type;
        }
    }

    public enum IssueStatus {
        DONE,
        IN_PROGRESS,
        TODO
    }

    public static class IssueStatusSegment extends SegmentedBar.Segment {

        private final IssueStatus status;

        public IssueStatusSegment(double value, IssueStatus status) {
            super(value);
            this.status = status;
        }

        public IssueStatus getStatus() {
            return status;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
