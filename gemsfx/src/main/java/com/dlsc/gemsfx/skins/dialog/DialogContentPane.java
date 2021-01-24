package com.dlsc.gemsfx.skins.dialog;

import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.DialogPane.Dialog;
import com.dlsc.gemsfx.DialogPane.Type;
import com.dlsc.gemsfx.GlassPane;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class DialogContentPane extends StackPane {

    private final ButtonBar dialogButtonBar;
    private final Dialog<?> dialog;

    public DialogContentPane(Dialog<?> dialog) {
        this.dialog = Objects.requireNonNull(dialog);

        setFocusTraversable(false);

        DialogPane shell = this.dialog.getDialogPane();

        VBox box = new VBox();
        box.getStyleClass().add("vbox");
        box.setFillWidth(true);

        VBox dialogHeader = new VBox();
        dialogHeader.setAlignment(Pos.CENTER);
        dialogHeader.getStyleClass().add("header");
        dialogHeader.managedProperty().bind(dialogHeader.visibleProperty());

        Label dialogTitle = new Label("Dialog");
        dialogTitle.setMaxWidth(Double.MAX_VALUE);
        dialogTitle.getStyleClass().add("title");

        ImageView dialogIcon = new ImageView();
        dialogIcon.getStyleClass().addAll("icon");

        dialogHeader.getChildren().setAll(dialogIcon, dialogTitle);

        StackPane dialogContentPane = new StackPane();
        dialogContentPane.getStyleClass().add("content");
        if (dialog.isPadding()) {
            dialogContentPane.getStyleClass().add("padding");
        }

        dialogButtonBar = new ButtonBar();

        if (System.getProperty("os.name").startsWith("Mac")) {
            dialogButtonBar.setButtonOrder("L_NCYOAHE+U+FBIX_R");
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            dialogButtonBar.setButtonOrder("L_YNOCAHE+U+FBXI_R");
        } else {
            dialogButtonBar.setButtonOrder("L_HENYCOA+U+FBIX_R");
        }

        dialogButtonBar.getStyleClass().add("button-bar");
        dialogButtonBar.managedProperty().bind(dialogButtonBar.visibleProperty());

        VBox.setVgrow(dialogTitle, Priority.NEVER);
        VBox.setVgrow(dialogContentPane, Priority.ALWAYS);
        VBox.setVgrow(dialogButtonBar, Priority.NEVER);

        dialogContentPane.getChildren().setAll(this.dialog.getContent());

        boolean blankDialog = this.dialog.getType().equals(Type.BLANK);

        dialogHeader.setVisible(!blankDialog);
        dialogButtonBar.setVisible(!blankDialog);

        dialogTitle.textProperty().bind(this.dialog.titleProperty());
        getStyleClass().setAll("content-pane");
        getStyleClass().addAll(this.dialog.getStyleClass());

        if (!blankDialog) {
            createButtons();
        }

        box.getChildren().setAll(dialogHeader, dialogContentPane, dialogButtonBar);

        GlassPane glassPane = new GlassPane();
        glassPane.hideProperty().bind(blocked.not());
        glassPane.fadeInOutProperty().bind(shell.fadeInOutProperty());
        getChildren().addAll(box, glassPane);
    }

    private final BooleanProperty blocked = new SimpleBooleanProperty(this, "blocked");

    public boolean isBlocked() {
        return blocked.get();
    }

    public BooleanProperty blockedProperty() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked.set(blocked);
    }

    public Dialog<?> getDialog() {
        return dialog;
    }

    private void createButtons() {
        dialogButtonBar.getButtons().clear();
        dialogButtonBar.setVisible(dialog.isShowButtonsBar());

        boolean hasDefault = false;
        for (ButtonType buttonType : dialog.getButtonTypes()) {
            Button button = createButton(buttonType);

            switch (buttonType.getButtonData()) {
                case LEFT:
                case RIGHT:
                case HELP:
                case HELP_2:
                case NO:
                case NEXT_FORWARD:
                case BACK_PREVIOUS:
                case APPLY:
                case CANCEL_CLOSE:
                case OTHER:
                case BIG_GAP:
                case SMALL_GAP:
                    break;
                case YES:
                case OK_DONE:
                case FINISH:
                    button.disableProperty().bind(dialog.validProperty().not());
                    break;

            }

            if (button != null) {
                ButtonData buttonData = buttonType.getButtonData();

                button.setDefaultButton(!hasDefault && buttonData != null && buttonData.isDefaultButton());
                button.setCancelButton(buttonData != null && buttonData.isCancelButton());
                button.setOnAction(evt -> {
                    if (buttonType.equals(ButtonType.CANCEL)) {
                        dialog.cancel();
                    } else {
                        dialog.getDialogPane().hideDialog(dialog);
                        dialog.complete(buttonType);
                    }
                });

                hasDefault |= buttonData != null && buttonData.isDefaultButton();

                /*
                 * Special support for list views. A double click inside of them triggers
                 * the default button.
                 */
                if (button.isDefaultButton()) {

                    Node content = dialog.getContent();
                    if (content instanceof ListView) {
                        content.setOnMouseClicked(evt -> {
                            if (evt.getClickCount() == 2) {
                                button.fire();
                            }
                        });
                    }
                }
            }

            dialogButtonBar.getButtons().add(button);
        }
    }

    protected Button createButton(ButtonType buttonType) {
        String text = buttonType.getText();
        StringConverter<ButtonType> converter = getDialog().getDialogPane().getConverter();
        if (converter != null) {
            text = converter.toString(buttonType);
        }
        Button button = new Button(text);
        ButtonData buttonData = buttonType.getButtonData();
        ButtonBar.setButtonUniformSize(button, dialog.isSameWidthButtons());
        ButtonBar.setButtonData(button, buttonData);
        button.setDefaultButton(buttonData.isDefaultButton());
        button.setCancelButton(buttonData.isCancelButton());
        return button;
    }
}
