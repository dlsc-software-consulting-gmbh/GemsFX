package com.dlsc.gemsfx;

import com.dlsc.gemsfx.infocenter.InfoCenterPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.HiddenSidesPane;

public class PowerPane extends StackPane {

    private final InfoCenterPane infoCenterPane;
    private final DialogPane dialogPane;
    private final DrawerStackPane drawerStackPane;
    private final HiddenSidesPane hiddenSidesPane;

    public PowerPane() {
        getStyleClass().add("power-pane");

        infoCenterPane = createInfoCenterPane();
        dialogPane = createDialogPane();
        drawerStackPane = createDrawerStackPane();
        hiddenSidesPane = createHiddenSidesPane();

        InfoCenterPane infoCenterPane = getInfoCenterPane();

        hiddenSidesPane.contentProperty().bind(contentProperty());

        DrawerStackPane drawerStackPane = getDrawerStackPane();
        drawerStackPane.getChildren().add(hiddenSidesPane);

        infoCenterPane.setContent(new StackPane(drawerStackPane, getDialogPane()));

        getChildren().add(infoCenterPane);
    }

    public PowerPane(Node content) {
        this();
        setContent(content);
    }

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    protected InfoCenterPane createInfoCenterPane() {
        return new InfoCenterPane();
    }

    protected DialogPane createDialogPane() {
        return new DialogPane();
    }

    protected DrawerStackPane createDrawerStackPane() {
        return new DrawerStackPane();
    }

    protected HiddenSidesPane createHiddenSidesPane() {
        return new HiddenSidesPane();
    }

    public InfoCenterPane getInfoCenterPane() {
        return infoCenterPane;
    }

    public DialogPane getDialogPane() {
        return dialogPane;
    }

    public DrawerStackPane getDrawerStackPane() {
        return drawerStackPane;
    }

    public HiddenSidesPane getHiddenSidesPane() {
        return hiddenSidesPane;
    }
}
