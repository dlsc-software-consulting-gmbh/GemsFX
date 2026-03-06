package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EnhancedPasswordField;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;

public class EnhancedPasswordFieldApp extends GemApplication {

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        EnhancedPasswordField passwordField1 = new EnhancedPasswordField();
        passwordField1.setPromptText("Enter your password");
        passwordField1.setEchoChar('★');
        passwordField1.setText("1234567890");

        Label label = new Label("EchoChar: ");
        ComboBox<Character> echoCharBox = createCharacterComboBox();
        echoCharBox.valueProperty().bindBidirectional(passwordField1.echoCharProperty());
        HBox echoCharBoxWrapper = new HBox(label, echoCharBox);
        echoCharBoxWrapper.getStyleClass().add("echo-char-box");

        Label tips = new Label("Tips: setEchoChar(char) to change the echo character");
        tips.setTextFill(Color.GREY);

        EnhancedPasswordField passwordField2 = new EnhancedPasswordField("1234567890");
        passwordField2.setLeft(createIconNode(MaterialDesign.MDI_KEY));
        passwordField2.setStyle("-fx-echo-char: '■';");

        EnhancedPasswordField passwordField3 = new EnhancedPasswordField();
        passwordField3.setText("1234567890");
        passwordField3.setShowPassword(true);

        StackPane closeIconWrapper = createIconNode(MaterialDesign.MDI_CLOSE);
        closeIconWrapper.setOnMouseClicked(e -> passwordField2.clear());
        passwordField2.setRight(closeIconWrapper);

        VBox root = new VBox(echoCharBoxWrapper, tips, passwordField1, new Separator(), passwordField2, new Separator(), passwordField3);
        root.getStyleClass().add("content-box");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/dlsc/gemsfx/demo/enhanced-password-filed-demo.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Enhanced Password Field");
        primaryStage.sizeToScene();

        primaryStage.show();
    }

    private ComboBox<Character> createCharacterComboBox() {
        ComboBox<Character> echoCharBox = new ComboBox<>();
        echoCharBox.getItems().addAll('●', '*', '∿', '❂', '■', '★');
        echoCharBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(echoCharBox, Priority.ALWAYS);
        return echoCharBox;
    }

    private StackPane createIconNode(MaterialDesign mdiKey) {
        FontIcon leftIcon = new FontIcon(mdiKey);
        StackPane leftIconWrapper = new StackPane(leftIcon);
        leftIconWrapper.getStyleClass().add("icon-wrapper");
        return leftIconWrapper;
    }

        @Override
    public String getDescription() {
        return """
                ### EnhancedPasswordField
                
                A custom password field that enhances the standard `PasswordField` with additional features.
                
                This component allows for the display of icons or nodes on both the left and right sides of the password field.
                It also provides the capability to toggle the visibility of the password, allowing the password to be shown
                as plain text or masked with a customizable echo character. The echo character and visibility can be styled
                and controlled through CSS or programmatically.
                
                Key features:
                
                  - Customizable echo character: The character used to mask the password can be customized.
                  - Toggle password visibility: Users can toggle between hiding and showing the password as plain text.
                  - Left and right nodes: Allows adding custom nodes (like buttons or icons) to either side of the field.
                
                Usage example:
                ```
                EnhancedPasswordField passwordField = new EnhancedPasswordField();
                passwordField.setLeft(new ImageView(new Image("path/to/icon.png")));
                passwordField.setRight(new Button("Show", e -> passwordField.setShowPassword(!passwordField.isShowPassword())));
                ```
                """;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
