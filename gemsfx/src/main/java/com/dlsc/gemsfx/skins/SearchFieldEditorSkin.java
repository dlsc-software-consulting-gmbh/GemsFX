package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Set;

public class SearchFieldEditorSkin<T> extends TextFieldSkin {

    private static final PseudoClass HAS_NO_SIDE_NODE = PseudoClass.getPseudoClass("no-side-nodes"); //$NON-NLS-1$
    private static final PseudoClass HAS_LEFT_NODE = PseudoClass.getPseudoClass("left-node-visible"); //$NON-NLS-1$
    private static final PseudoClass HAS_RIGHT_NODE = PseudoClass.getPseudoClass("right-node-visible"); //$NON-NLS-1$

    private final StackPane graphicWrapper = new StackPane();
    private final StackPane searchGraphicWrapper = new StackPane();
    private final Label autoCompletedTextLabel = new Label();
    private final SearchField<T> searchField;
    private StackPane leftPane;
    private StackPane rightPane;

    public SearchFieldEditorSkin(SearchField<T> searchField) {
        super(searchField.getEditor());

        this.searchField = searchField;

        graphicWrapper.getStyleClass().add("graphic-wrapper");
        graphicWrapper.visibleProperty().bind(searchField.searchingProperty().not().and(searchField.showSearchIconProperty()));
        graphicWrapper.setManaged(false);

        searchGraphicWrapper.getStyleClass().addAll("graphic-wrapper", "search-graphic-wrapper");
        searchGraphicWrapper.visibleProperty().bind(searchField.searchingProperty().and(searchField.showSearchIconProperty()));
        searchGraphicWrapper.setManaged(false);

        setupGraphics();

        searchField.graphicProperty().addListener(it -> setupGraphics());

        autoCompletedTextLabel.getStyleClass().add("auto-completion-label");
        autoCompletedTextLabel.textProperty().bind(searchField.autoCompletedTextProperty());
        autoCompletedTextLabel.visibleProperty().bind(searchField.autoCompletedTextProperty().isEmpty().not());
        autoCompletedTextLabel.setManaged(false);
        autoCompletedTextLabel.setPrefHeight(0);

        registerChangeListener(searchField.autoCompletedTextProperty(), it -> getSkinnable().requestLayout());

        getChildren().addAll(autoCompletedTextLabel, graphicWrapper, searchGraphicWrapper);

        SearchFieldPopup<T> autoCompletionPopup = searchField.getPopup();
        autoCompletionPopup.autoHideProperty().bind(searchField.placeholderProperty().isNull());

        registerChangeListener(searchField.leftProperty(), e -> updateChildren());
        registerChangeListener(searchField.rightProperty(), e -> updateChildren());
        registerChangeListener(searchField.showSearchIconProperty(), it -> getSkinnable().requestLayout());

        updateChildren();
    }

    private void updateChildren() {
        Node newLeft = searchField.getLeft();

        // remove left pane in any case
        getChildren().remove(leftPane);
        Node left;
        if (newLeft != null) {
            leftPane = new StackPane(newLeft);
            leftPane.setManaged(false);
            leftPane.visibleProperty().bind(newLeft.visibleProperty());
            leftPane.setAlignment(Pos.CENTER_LEFT);
            leftPane.getStyleClass().add("left-pane"); //$NON-NLS-1$
            getChildren().add(leftPane);
            left = newLeft;
        } else {
            leftPane = null;
            left = null;
        }

        Node newRight = searchField.getRight();

        // remove rightPane in any case
        getChildren().remove(rightPane);
        Node right;
        if (newRight != null) {
            rightPane = new StackPane(newRight);
            rightPane.setManaged(false);
            rightPane.setAlignment(Pos.CENTER_RIGHT);
            rightPane.visibleProperty().bind(newRight.visibleProperty());
            rightPane.getStyleClass().add("right-pane"); //$NON-NLS-1$
            getChildren().add(rightPane);
            right = newRight;
        } else {
            rightPane = null;
            right = null;
        }

        searchField.pseudoClassStateChanged(HAS_LEFT_NODE, left != null);
        searchField.pseudoClassStateChanged(HAS_RIGHT_NODE, right != null);
        searchField.pseudoClassStateChanged(HAS_NO_SIDE_NODE, left == null && right == null);
    }

    private void setupGraphics() {
        graphicWrapper.getChildren().clear();
        Node graphic = searchField.getGraphic();
        if (graphic != null) {
            graphicWrapper.getChildren().setAll(graphic);
        }

        searchGraphicWrapper.getChildren().clear();
        Node searchGraphic = searchField.getBusyGraphic();
        if (searchGraphic != null) {
            searchGraphicWrapper.getChildren().setAll(searchGraphic);
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double fullHeight = h + snappedTopInset() + snappedBottomInset();

        double leftWidth = leftPane == null ? 0.0 : snapSizeX(leftPane.prefWidth(fullHeight));
        double rightWidth = rightPane == null ? 0.0 : snapSizeX(rightPane.prefWidth(fullHeight));

        double textFieldStartX = snapPositionX(x) + snapSizeX(leftWidth);

        // standard graphic
        double iconWidthA = 0;

        if (searchField.isShowSearchIcon()) {
            iconWidthA = graphicWrapper.prefWidth(-1);
            double iconHeightA = graphicWrapper.prefHeight(-1);
            graphicWrapper.resizeRelocate(x + w - iconWidthA, y + h / 2 - iconHeightA / 2, iconWidthA, iconHeightA);
        }

        // search / busy graphic
        double iconWidthB = 0;

        if (searchField.isShowSearchIcon()) {
            iconWidthB = searchGraphicWrapper.prefWidth(-1);
            double iconHeightB = searchGraphicWrapper.prefHeight(-1);
            searchGraphicWrapper.resizeRelocate(x + w - iconWidthB, y + h / 2 - iconHeightB / 2, iconWidthB, iconHeightB);
        }

        double maxIconWidth = Math.max(iconWidthA, iconWidthB);

        double textFieldWidth = w - snapSizeX(leftWidth) - snapSizeX(rightWidth) - maxIconWidth;

        super.layoutChildren(textFieldStartX, 0, textFieldWidth, fullHeight);

        if (leftPane != null && leftPane.isVisible() && searchField.getLeft().isManaged()) {
            leftPane.resizeRelocate(0, 0, leftWidth, fullHeight);
        }

        if (rightPane != null && rightPane.isVisible() && searchField.getRight().isManaged()) {
            double rightStartX = w - rightWidth + snappedLeftInset() + snappedRightInset() - maxIconWidth;
            if (searchField.isShowSearchIcon()) {
                rightStartX -= snappedRightInset();
            }
            rightPane.resizeRelocate(rightStartX, 0, rightWidth, fullHeight);
        }

        TextField textField = getSkinnable();

        double gap = Math.max(0, searchField.getAutoCompletionGap());

        Node lookup = null;
        Set<Node> nodes = textField.lookupAll(".text");
        if (nodes.size() > 2) { // normal text, prompt text, autocomplete tex

            // there might be two nodes with ".text" style class if the field uses a prompt text
            Optional<Node> lookupOptional = nodes.stream().filter(n -> {
                if (n instanceof Text) {
                    Text textNode = (Text) n;
                    String text = textNode.getText();
                    if (text != null) {
                        return !(text.equals(textField.getPromptText()));
                    }
                }
                return false;
            }).findFirst();

            if (lookupOptional.isPresent()) {
                lookup = lookupOptional.get();
            }
        } else {
            lookup = textField.lookup(".text");
        }

        if (lookup == null) {
            return;
        }

        Bounds standardTextBounds = lookup.getLayoutBounds();

        double autoCompleteWidth = autoCompletedTextLabel.prefWidth(-1);

        double autoCompletionX = textFieldStartX + snapSizeX(gap) + standardTextBounds.getWidth();
        autoCompletedTextLabel.resizeRelocate(
                snapPositionX(autoCompletionX),
                y,
                Math.max(0, Math.min(autoCompleteWidth, w - autoCompletionX)),
                h);
    }
}
