package com.dlsc.gemsfx;

import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A specialized pane that can be used to display a list of tiles (nodes) in one or more rows. The pane first calculates
 * how many tiles fit next to each other in a row and then arranges them in a grid. The tiles are stretched to fill
 * the entire width of each row. All tiles have the same height and width.
 * <br>
 * Note: the main difference to the standard JavaFX TilePane is that the tiles are stretched to fill the entire width of
 * each row.
 */
public class StretchingTilePane extends Pane {

    /**
     * Constructs a new tile pane with an horizontal gap of 0 and a vertical gap of 0.
     */
    public StretchingTilePane() {
        this(0, 0);
    }

    /**
     * Constructs a new tile pane with the given horizontal and vertical gap.
     *
     * @param hgap the horizontal gap between the tiles.
     * @param vgap the vertical gap between the tiles.
     */
    public StretchingTilePane(double hgap, double vgap) {
        this(hgap, vgap, new Node[]{});
    }

    /**
     * Constructs a new tile pane with the specified horizontal and vertical gaps
     * and an optional list of child nodes.
     *
     * @param hgap     the horizontal gap between the tiles.
     * @param vgap     the vertical gap between the tiles.
     * @param children the child nodes to be added to the tile pane.
     */
    public StretchingTilePane(double hgap, double vgap, Node... children) {
        super(children);
        setHgap(hgap);
        setVgap(vgap);
    }

    /**
     * We need the content bias to be horizontal so that the component's width will be considered for layout
     * purposes.
     *
     * @return the content bias.
     */
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    private DoubleProperty hgap;

    /**
     * The horizontal gap between the tiles.
     *
     * @return the horizontal gap property.
     */
    public final DoubleProperty hgapProperty() {
        if (hgap == null) {
            hgap = new StyleableDoubleProperty() {
                @Override
                public void invalidated() {
                    requestLayout();
                }

                @Override
                public CssMetaData<StretchingTilePane, Number> getCssMetaData() {
                    return StretchingTilePane.StyleableProperties.HGAP;
                }

                @Override
                public Object getBean() {
                    return StretchingTilePane.this;
                }

                @Override
                public String getName() {
                    return "hgap";
                }
            };
        }
        return hgap;
    }

    public final void setHgap(double value) {
        hgapProperty().set(value);
    }

    public final double getHgap() {
        return hgap == null ? 0 : hgap.get();
    }

    private DoubleProperty vgap;

    /**
     * The vertical gap between the tiles.
     *
     * @return the vertical gap property.
     */
    public final DoubleProperty vgapProperty() {
        if (vgap == null) {
            vgap = new StyleableDoubleProperty() {
                @Override
                public void invalidated() {
                    requestLayout();
                }

                @Override
                public CssMetaData<StretchingTilePane, Number> getCssMetaData() {
                    return StretchingTilePane.StyleableProperties.VGAP;
                }

                @Override
                public Object getBean() {
                    return StretchingTilePane.this;
                }

                @Override
                public String getName() {
                    return "vgap";
                }
            };
        }
        return vgap;
    }

    public final void setVgap(double value) {
        vgapProperty().set(value);
    }

    public final double getVgap() {
        return vgap == null ? 0 : vgap.get();
    }

    private double computeMaximumPreferredTileHeight(double w) {
        int size = getManagedChildren().size();
        double prefHeight = 0;
        for (int i = 0; i < size; i++) {
            Region node = (Region) getManagedChildren().get(i);
            double ph = node.prefHeight(w);
            if (ph > prefHeight) {
                prefHeight = ph;
            }
        }
        return prefHeight;
    }

    private double computeMaximumPreferredTileWidth() {
        int size = getManagedChildren().size();
        double prefWidth = 0;
        for (int i = 0; i < size; i++) {
            Node node = getManagedChildren().get(i);
            double pw = node.prefWidth(-1);
            if (pw > prefWidth) {
                prefWidth = pw;
            }
        }
        return prefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        List<Node> children = getManagedChildren();
        int totalLen = children.size();
        if (totalLen == 0) {
            return 0;
        }

        double contentWidth = width - getInsets().getLeft() - getInsets().getRight();

        double preferredTileWidth = computeMaximumPreferredTileWidth();
        double preferredTileHeight = computeMaximumPreferredTileHeight(width);

        int columnCount = (int) (contentWidth / (preferredTileWidth + getHgap()));
        if (columnCount == 0) {
            return 0;
        }

        int numberOfRows = (int) Math.ceil((double) children.size() / (double) columnCount);
        double cellHeight = preferredTileHeight * numberOfRows;

        return cellHeight + (numberOfRows - 1) * getVgap() + getInsets().getTop() + getInsets().getBottom();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        List<Node> children = getManagedChildren();
        int totalLen = children.size();
        if (totalLen == 0) {
            return;
        }

        double contentWidth = getWidth() - getInsets().getLeft() - getInsets().getRight();

        double preferredTileWidth = computeMaximumPreferredTileWidth();
        double preferredTileHeight = computeMaximumPreferredTileHeight(contentWidth);

        int columnCount = (int) (contentWidth / (preferredTileWidth + getHgap()));
        if (columnCount == 0) {
            return;
        }

        double availableWidth = contentWidth - (columnCount - 1) * getHgap();
        double w = availableWidth / columnCount;

        int row = 0;
        int col = 0;

        double x = getInsets().getLeft();
        double y = getInsets().getTop();

        for (int i = 0; i < totalLen; i++) {
            Node node = children.get(i);
            node.resizeRelocate(x, y, w, preferredTileHeight);
            x += w + getHgap();
            col++;
            if (col == columnCount) {
                col = 0;
                row++;
                y += preferredTileHeight + getVgap();
                x = getInsets().getLeft();
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<StretchingTilePane, Number> HGAP =
                new CssMetaData<>("-fx-hgap", SizeConverter.getInstance(), 0.0) {

                    @Override
                    public boolean isSettable(StretchingTilePane node) {
                        return node.hgap == null ||
                                !node.hgap.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(StretchingTilePane node) {
                        return (StyleableProperty<Number>) node.hgapProperty();
                    }
                };

        private static final CssMetaData<StretchingTilePane, Number> VGAP =
                new CssMetaData<>("-fx-vgap", SizeConverter.getInstance(), 0.0) {

                    @Override
                    public boolean isSettable(StretchingTilePane node) {
                        return node.vgap == null ||
                                !node.vgap.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(StretchingTilePane node) {
                        return (StyleableProperty<Number>) node.vgapProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Pane.getClassCssMetaData());
            Collections.addAll(styleables, HGAP, VGAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StretchingTilePane.StyleableProperties.STYLEABLES;
    }
}