package com.dlsc.gemsfx.demo.fake;

import javafx.beans.property.*;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class SettingsPane extends VBox {

    private VBox innerBox = new VBox(10);

    private final BooleanProperty showSpotlightIn = new SimpleBooleanProperty(this, "showSpotlightIn", true);
    private final BooleanProperty showSpotlightOut = new SimpleBooleanProperty(this, "showSpotlightOut", true);
    private final BooleanProperty showTextBoxIn = new SimpleBooleanProperty(this, "showTextBoxIn");
    private final BooleanProperty showTextBoxOut = new SimpleBooleanProperty(this, "showTextBoxOut", true);
    private final BooleanProperty showLinesIn = new SimpleBooleanProperty(this, "showLinesIn");
    private final BooleanProperty showLinesOut = new SimpleBooleanProperty(this, "showLinesOut");
    private final BooleanProperty showShapesIn = new SimpleBooleanProperty(this, "showShapesIn", true);
    private final BooleanProperty showShapesOut = new SimpleBooleanProperty(this, "showShapesOut");

    private final DoubleProperty spotlightIn = new SimpleDoubleProperty(this, "spotlightIn", .25);
    private final DoubleProperty spotlightOut = new SimpleDoubleProperty(this, "spotlightOut", .25);
    private final DoubleProperty textBoxIn = new SimpleDoubleProperty(this, "textBoxIn", .25);
    private final DoubleProperty textBoxOut = new SimpleDoubleProperty(this, "textBoxOut", .25);
    private final DoubleProperty linesIn = new SimpleDoubleProperty(this, "linesIn", .25);
    private final DoubleProperty linesOut = new SimpleDoubleProperty(this, "linesOut", .25);
    private final DoubleProperty shapesIn = new SimpleDoubleProperty(this, "shapesIn", .25);
    private final DoubleProperty shapesOut = new SimpleDoubleProperty(this, "shapesOut", .25);

    public SettingsPane() {
        getStyleClass().add("settings-pane");

        setFillWidth(true);

        Label title = new Label("Settings");
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("title");
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox header = new HBox(title);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("header");

        innerBox.getChildren().add(header);

        innerBox.getStyleClass().add("inner-box");
        VBox.setVgrow(innerBox, Priority.ALWAYS);
        getChildren().add(innerBox);

        addSection("Spotlight");
        addSetting("Animate_In", showSpotlightIn, spotlightIn);
        addSetting("Animate_Out", showSpotlightOut, spotlightOut);
        addSeparator();

        addSection("Text_Box");
        addSetting("Animate_In", showTextBoxIn, textBoxIn);
        addSetting("Animate_Out", showTextBoxOut, textBoxOut);
        addSeparator();

        addSection("Lines");
        addSetting("Animate_In", showLinesIn, linesIn);
        addSetting("Animate_Out", showLinesOut, linesOut);
        addSeparator();

        addSection("Shapes");
        addSetting("Animate_In", showShapesIn, shapesIn);
        addSetting("Animate_Out", showShapesOut, shapesOut);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        Node footer = createFooter();
        footer.visibleProperty().bind(onCloseProperty().isNotNull());
        footer.managedProperty().bind(onCloseProperty().isNotNull());

        getChildren().add(footer);

        installDragHandler(this);
    }

    private void addSection(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        innerBox.getChildren().add(label);
    }

    private void addSeparator() {
        innerBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
    }

    private void addSetting(String text, BooleanProperty showProperty, DoubleProperty durationProperty) {
        CheckBox checkbox = new CheckBox(text);
        checkbox.setMaxWidth(Double.MAX_VALUE);
        checkbox.selectedProperty().bindBidirectional(showProperty);
        checkbox.getStyleClass().add("setting-check-box");
        HBox.setHgrow(checkbox, Priority.ALWAYS);

        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.25, 1000, durationProperty.get(), .25);
        durationProperty.addListener(it -> valueFactory.setValue(durationProperty.get()));
        valueFactory.valueProperty().addListener(it -> durationProperty.set(valueFactory.getValue()));

        Spinner<Double> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        spinner.disableProperty().bind(checkbox.selectedProperty().not());

        Label secondsLabel = new Label("seconds");
        secondsLabel.getStyleClass().add("seconds-label");
        secondsLabel.setMaxWidth(Double.MAX_VALUE);

        HBox row = new HBox(checkbox, spinner, secondsLabel);
        row.getStyleClass().add("setting-box");
        row.setAlignment(Pos.CENTER_LEFT);

        innerBox.getChildren().add(row);
    }

    private Node createFooter() {
        Button button = new Button("CLOSE");
        button.getStyleClass().addAll("custom-button", "blue-button");
        button.setOnAction(evt -> getOnClose().run());

        HBox footer = new HBox(button);
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("footer");
        installDragHandler(footer);

        return footer;
    }

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose");

    public Runnable getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose.set(onClose);
    }

    private double startX;
    private double startY;

    private void installDragHandler(Node node) {
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, evt -> {
            startX = evt.getScreenX();
            startY = evt.getScreenY();
        });

        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, evt -> {
            double x = evt.getScreenX();
            double y = evt.getScreenY();

            Window window = getScene().getWindow();

            double deltaX = evt.getScreenX() - startX;
            double deltaY = evt.getScreenY() - startY;

            window.setX(getScene().getWindow().getX() + deltaX);
            window.setY(getScene().getWindow().getY() + deltaY);

            startX = x;
            startY = y;
        });
    }
}
