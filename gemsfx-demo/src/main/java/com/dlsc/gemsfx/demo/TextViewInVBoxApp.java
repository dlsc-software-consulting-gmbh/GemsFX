package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.TextView;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TextViewInVBoxApp extends Application {

    @Override
    public void start(Stage stage) {
        TextView textViewA = new TextView("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere.");
        TextView textViewB = new TextView("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere.");
        TextView textViewC = new TextView("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere.");

        VBox vBox = new VBox(10, textViewA, textViewB, textViewC);
        vBox.setPrefWidth(600);
        vBox.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(vBox) {
            @Override
            public Orientation getContentBias() {
                return Orientation.HORIZONTAL;
            }
        };

        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle("Text View (VBox)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}