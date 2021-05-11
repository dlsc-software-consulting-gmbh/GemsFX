package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.BeforeAfterViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class BeforeAfterView extends Control {

    private static final PseudoClass horizontal = PseudoClass.getPseudoClass("horizontal");
    private static final PseudoClass vertical = PseudoClass.getPseudoClass("vertical");

    public BeforeAfterView() {
        getStyleClass().add("before-after-view");

        Label before = new Label();
        before.setPrefSize(600, 400);
        before.setStyle("-fx-background-color: red;");
        setBefore(before);

        Label after = new Label();
        after.setPrefSize(600, 400);
        after.setStyle("-fx-background-color: green;");
        setAfter(after);

        beforeProperty().addListener(it -> {
            Node node = getBefore();
            if (node != null) {
                node.setMouseTransparent(true);
            }
        });

        afterProperty().addListener(it -> {
            Node node = getAfter();
            if (node != null) {
                node.setMouseTransparent(true);
            }
        });

        orientationProperty().addListener(it -> updatePseudoClass());
        updatePseudoClass();
    }

    private void updatePseudoClass() {
        pseudoClassStateChanged(horizontal, getOrientation().equals(Orientation.HORIZONTAL));
        pseudoClassStateChanged(vertical, getOrientation().equals(Orientation.VERTICAL));
    }

    public BeforeAfterView(Node beforeNode, Node afterNode) {
        this();
        setBefore(beforeNode);
        setAfter(afterNode);
    }

    public BeforeAfterView(Image beforeImage, Image afterImage) {
        this(new ImageView(beforeImage), new ImageView(afterImage));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BeforeAfterViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return BeforeAfterView.class.getResource("before-after-view.css").toExternalForm();
    }

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

    public Orientation getOrientation() {
        return orientation.get();
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    private final DoubleProperty dividerPosition = new SimpleDoubleProperty(this, "dividerPosition", .5);

    public final double getDividerPosition() {
        return dividerPosition.get();
    }

    public final DoubleProperty dividerPositionProperty() {
        return dividerPosition;
    }

    public final void setDividerPosition(double dividerPosition) {
        this.dividerPosition.set(dividerPosition);
    }

    private final ObjectProperty<Node> before = new SimpleObjectProperty<>(this, "before", new Label("Before"));

    public final Node getBefore() {
        return before.get();
    }

    public final ObjectProperty<Node> beforeProperty() {
        return before;
    }

    public final void setBefore(Node before) {
        this.before.set(before);
    }

    private final ObjectProperty<Node> after = new SimpleObjectProperty<>(this, "after", new Label("After"));

    public final Node getAfter() {
        return after.get();
    }

    public final ObjectProperty<Node> afterProperty() {
        return after;
    }

    public final void setAfter(Node after) {
        this.after.set(after);
    }
}
