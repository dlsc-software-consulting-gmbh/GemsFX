package com.dlsc.gemsfx;

import com.dlsc.gemsfx.infocenter.InfoCenterPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.HiddenSidesPane;

/**
 * The "PowerPane" combines several other highly specialized panes of GemsFX and
 * ControlsFX. They are:
 * <ul>
 *     <li>InfoCenterPane - to display notifications</li>
 *     <li>DialogPane - to display dialogs</li>
 *     <li>DrawerStackPane - to display a drawer / a tray that slides in from the bottom</li>
 *     <li>HiddenSidesPane - to display trays sliding in from either one of the four sides</li>
 * </ul>
 * This pane is a good candidate to be used as the basis for a rich client application.
 */
public class PowerPane extends StackPane {

    private final InfoCenterPane infoCenterPane;
    private final DialogPane dialogPane;
    private final DrawerStackPane drawerStackPane;
    private final HiddenSidesPane hiddenSidesPane;

    /**
     * Constructs a new power pane.
     */
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

    /**
     * Constructs a new power pane with the given content node.
     *
     * @param content the main UI to be shown in the center of the pane
     */
    public PowerPane(Node content) {
        this();
        setContent(content);
    }

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    /**
     * Stores the content node / the main view (UI).
     *
     * @return the content ndoe
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    /**
     * Creates the {@link InfoCenterPane} instance to be used by this pane. Applications
     * can override this method to return a customized version.
     *
     * @return the info center pane
     */
    protected InfoCenterPane createInfoCenterPane() {
        return new InfoCenterPane();
    }

    /**
     * Creates the {@link DialogPane} instance to be used by this pane. Applications
     * can override this method to return a customized version.
     *
     * @return the dialog pane
     */
    protected DialogPane createDialogPane() {
        return new DialogPane();
    }

    /**
     * Creates the {@link DrawerStackPane} instance to be used by this pane. Applications
     * can override this method to return a customized version.
     *
     * @return the drawer stack pane
     */
    protected DrawerStackPane createDrawerStackPane() {
        return new DrawerStackPane();
    }

    /**
     * Creates the {@link HiddenSidesPane} instance to be used by this pane. Applications
     * can override this method to return a customized version.
     *
     * @return the "hidden sides" pane
     */
    protected HiddenSidesPane createHiddenSidesPane() {
        return new HiddenSidesPane();
    }

    /**
     * Returns the {@link InfoCenterPane} used by this pane.
     *
     * @return the info center pane
     */
    public final InfoCenterPane getInfoCenterPane() {
        return infoCenterPane;
    }

    /**
     * Returns the {@link DialogPane} used by this pane.
     *
     * @return the dialog pane
     */
    public final DialogPane getDialogPane() {
        return dialogPane;
    }

    /**
     * Returns the {@link DrawerStackPane} used by this pane.
     *
     * @return the drawer stack pane
     */
    public final DrawerStackPane getDrawerStackPane() {
        return drawerStackPane;
    }

    /**
     * Returns the {@link HiddenSidesPane} used by this pane.
     *
     * @return the "hidden sides" pane
     */
    public final HiddenSidesPane getHiddenSidesPane() {
        return hiddenSidesPane;
    }
}
