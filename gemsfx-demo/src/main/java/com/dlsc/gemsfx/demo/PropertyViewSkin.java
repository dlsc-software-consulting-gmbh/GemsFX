package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.demo.PropertyView.Item;
import javafx.beans.Observable;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class PropertyViewSkin extends SkinBase<PropertyView> {

    private final GridPane gridPane;

    public PropertyViewSkin(PropertyView propertyView) {
        super(propertyView);

        gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.getStyleClass().add("grid");

        ColumnConstraints con1 = new ColumnConstraints();
        con1.setFillWidth(true);
        con1.setHgrow(Priority.NEVER);
        con1.setPrefWidth(Region.USE_COMPUTED_SIZE);

        ColumnConstraints con2 = new ColumnConstraints();
        con2.setFillWidth(true);
        con2.setHgrow(Priority.ALWAYS);
        con2.setHalignment(HPos.LEFT);

        gridPane.getColumnConstraints().setAll(con1, con2);

        propertyView.getItems().addListener((Observable it) -> buildView());
        buildView();

        getChildren().add(gridPane);
    }

    private void buildView() {
        gridPane.getChildren().clear();

        PropertyView propertyView = getSkinnable();

        for (int i = 0; i < propertyView.getItems().size(); i++) {

            Item<?> item = propertyView.getItems().get(i);

            Label nameLabel = new Label();
            nameLabel.getStyleClass().add("name-label");
            nameLabel.textProperty().bind(item.nameProperty());
            nameLabel.setMouseTransparent(true);
            nameLabel.minWidthProperty().bind(propertyView.minLabelWidthProperty());

            Callback<Item, Node> cellFactory = item.getNodeFactory();
            Node itemNode = cellFactory.call(item);

            Region itemBackground = new Region();
            itemBackground.getStyleClass().add("item-background");
            itemBackground.setCursor(Cursor.HAND);
            itemBackground.setOnMouseClicked(evt -> {
                if (item.isEditable()) {
                    item.edit(itemNode);
                }
            });

            Object value = item.getValue();
            Callback stringProvider = item.getStringProvider();
            Object text = stringProvider.call(value);

            String tooltipText = text.toString();

            GridPane.setColumnSpan(itemBackground, 3);

            HBox valueBox = new HBox(itemNode);
            HBox.setHgrow(itemNode, Priority.ALWAYS);
            valueBox.setMouseTransparent(true);
            valueBox.getStyleClass().add("value-box");
            valueBox.setAlignment(Pos.CENTER_LEFT);

            gridPane.add(itemBackground, 0, i * 2);
            gridPane.add(nameLabel, 0, i * 2);
            gridPane.add(valueBox, 1, i * 2);

            if (i < propertyView.getItems().size() - 1) {
                Separator separator = new Separator(Orientation.HORIZONTAL);
                GridPane.setColumnSpan(separator, 3);
                gridPane.add(separator, 0, i * 2 + 1);
            }

        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        System.out.println(">>> width: " + width);
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        System.out.println(">>> height: " + height);
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
