package com.dlsc.gemsfx;

import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A customized text area that will never show scrollbars but instead will
 * grow as high as needed in order to completely fit its text inside of it.
 */
public class ExpandingTextArea extends TextArea {

    private Text text;

    private double offsetTop;
    private double offsetBottom;

    public ExpandingTextArea() {
        init();
    }

    public ExpandingTextArea(String text) {
        super(text);
        init();
    }

    private void init() {
        getStyleClass().add("expanding-text-area");
        setWrapText(true);

        sceneProperty().addListener(it -> {
            if (getScene() != null) {
                performBinding();
            }
        });

        skinProperty().addListener(it -> {
            if (getSkin() != null) {
                performBinding();
            }
        });
    }

    private double computeHeight() {
        computeOffsets();

        Bounds layoutBounds = localToScreen(text.getLayoutBounds());
        if (layoutBounds != null) {

            double minY = layoutBounds.getMinY();
            double maxY = layoutBounds.getMaxY();

            return maxY - minY + offsetTop + offsetBottom;
        }

        return 0;
    }

    private void computeOffsets() {
        offsetTop = getInsets().getTop();
        offsetBottom = getInsets().getBottom();

        ScrollPane scrollPane = (ScrollPane) lookup(".scroll-pane");
        if (scrollPane != null) {
            Region viewport = (Region) scrollPane.lookup(".viewport");
            Region content = (Region) scrollPane.lookup(".content");

            offsetTop += viewport.getInsets().getTop();
            offsetTop += content.getInsets().getTop();

            offsetBottom += viewport.getInsets().getBottom();
            offsetBottom += content.getInsets().getBottom();
        }
    }

    private void performBinding() {
        ScrollPane scrollPane = (ScrollPane) lookup(".scroll-pane");
        if (scrollPane != null) {
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.skinProperty().addListener(it -> {
                if (scrollPane.getSkin() != null) {
                    if (text == null) {
                        text = findTextNode();
                        if (text != null) {
                            prefHeightProperty().bind(Bindings.createDoubleBinding(this::computeHeight, text.layoutBoundsProperty()));
                        }
                    }
                }
            });
        }
    }

    /*
     * We need to find the node of type Text that is owned by a Group because
     * there might be two Text instances if a prompt text has been specified.
     */
    private Text findTextNode() {
        final Set<Node> nodes = lookupAll(".text");
        for (Node node : nodes) {
            if (node.getParent() instanceof Group) {
                return (Text) node;
            }
        }
        return null;
    }
}
