package com.dlsc.gemsfx.demo;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This is a test case to validate whether a TabPane respects the preferred
 * height of its content.
 * @author Dirk Lemmermann
 */
public class TabPaneBug extends GemApplication {

    @Override
    public void start(Stage stage) { super.start(stage);
        Label label = new Label("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. A");
        label.setWrapText(true);
        label.setMaxWidth(500);

        Tab tab1 = new Tab("Tab 1", label);
        TabPane tabPane = new TabPane(tab1);

        StackPane stackPane = new StackPane(tabPane);
        stackPane.setPadding(new Insets(20));

        stage.setScene(new Scene(stackPane));
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle("Tab Pane Bug");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}