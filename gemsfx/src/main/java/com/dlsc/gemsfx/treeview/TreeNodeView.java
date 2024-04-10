package com.dlsc.gemsfx.treeview;

import com.dlsc.gemsfx.treeview.link.LinkStrategy;
import com.dlsc.gemsfx.treeview.link.StraightLineLink;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * A visual control for displaying trees.
 * <p>
 * Built on the {@link TreeNode} class, this control visualizes hierarchical structures, allowing nodes to have children.
 * <p>
 * Customizable in layout, alignment, and style, it's ideal for representing data like file systems or organizational charts.
 */
public class TreeNodeView<T> extends Control {
    private static final int DEFAULT_CELL_WIDTH = 60;
    private static final int DEFAULT_CELL_HEIGHT = 30;
    private static final double DEFAULT_VGAP = 50;
    private static final double DEFAULT_HGAP = 20;
    private static final double DEFAULT_NODE_LINE_GAP = 10;
    private static final VPos DEFAULT_ROW_ALIGNMENT = VPos.CENTER;
    private static final HPos DEFAULT_COLUMN_ALIGNMENT = HPos.CENTER;
    private static final LayoutType DEFAULT_LAYOUT_TYPE = LayoutType.REGULAR;
    private static final LayoutDirection DEFAULT_LAYOUT_DIRECTION = LayoutDirection.TOP_TO_BOTTOM;
    private static final PseudoClass LTR_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("ltr");
    private static final PseudoClass RTL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("rtl");
    private static final PseudoClass TTB_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("ttb");
    private static final PseudoClass BTT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("btt");
    private static final String DEFAULT_STYLE_CLASS = "tree-node-view";

    public TreeNodeView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        layoutDirection.addListener(it -> activateDirectionPseudoClass());
    }

    public TreeNodeView(TreeNode<T> root) {
        this();
        setRoot(root);
    }

    private void activateDirectionPseudoClass() {
        pseudoClassStateChanged(LTR_PSEUDOCLASS_STATE, getLayoutDirection() == LayoutDirection.LEFT_TO_RIGHT);
        pseudoClassStateChanged(RTL_PSEUDOCLASS_STATE, getLayoutDirection() == LayoutDirection.RIGHT_TO_LEFT);
        pseudoClassStateChanged(TTB_PSEUDOCLASS_STATE, getLayoutDirection() == LayoutDirection.TOP_TO_BOTTOM);
        pseudoClassStateChanged(BTT_PSEUDOCLASS_STATE, getLayoutDirection() == LayoutDirection.BOTTOM_TO_TOP);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeNodeViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(getClass().getResource("tree-view.css")).toExternalForm();
    }

    private final ObjectProperty<Callback<T, TreeNodeCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory", TreeNodeCell::new);

    public final void setCellFactory(Callback<T, TreeNodeCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    public final Callback<T, TreeNodeCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public final ObjectProperty<Callback<T, TreeNodeCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    private final ObjectProperty<TreeNode<T>> root = new SimpleObjectProperty<>(this, "root");

    public TreeNode<T> getRoot() {
        return root.get();
    }

    public ObjectProperty<TreeNode<T>> rootProperty() {
        return root;
    }

    public void setRoot(TreeNode<T> root) {
        this.root.set(root);
    }

    /**
     * Alignment on the same level;
     * When the layout direction is btt or ttb,
     * this attribute indicates the alignment of nodes at the same level;
     */
    private final StyleableObjectProperty<VPos> rowAlignment = new StyleableObjectProperty<>(DEFAULT_ROW_ALIGNMENT) {
        @Override
        public CssMetaData<? extends Styleable, VPos> getCssMetaData() {
            return StyleableProperties.ROW_ALIGNMENT;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "rowAlignment";
        }
    };

    public VPos getRowAlignment() {
        return rowAlignment.get();
    }

    public ObjectProperty<VPos> rowAlignmentProperty() {
        return rowAlignment;
    }

    public void setRowAlignment(VPos rowAlignment) {
        this.rowAlignment.set(rowAlignment);
    }

    /**
     * Alignment on the same level;
     * When the layout direction is ltr or rtl,
     * this attribute indicates the alignment of nodes at the same level;
     */
    private final StyleableObjectProperty<HPos> columnAlignment = new StyleableObjectProperty<>(DEFAULT_COLUMN_ALIGNMENT) {
        @Override
        public CssMetaData<? extends Styleable, HPos> getCssMetaData() {
            return StyleableProperties.COLUMN_ALIGNMENT;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "columnAlignment";
        }
    };

    public HPos getColumnAlignment() {
        return columnAlignment.get();
    }

    public ObjectProperty<HPos> columnAlignmentProperty() {
        return columnAlignment;
    }

    public void setColumnAlignment(HPos columnAlignment) {
        this.columnAlignment.set(columnAlignment);
    }

    private final DoubleProperty cellWidth = new StyleableDoubleProperty(DEFAULT_CELL_WIDTH) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.CELL_WIDTH;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "cellWidth";
        }
    };

    public double getCellWidth() {
        return cellWidth.get();
    }

    public DoubleProperty cellWidthProperty() {
        return cellWidth;
    }

    public void setCellWidth(double cellWidth) {
        this.cellWidth.set(cellWidth);
    }

    private final DoubleProperty cellHeight = new StyleableDoubleProperty(DEFAULT_CELL_HEIGHT) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.CELL_HEIGHT;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "cellHeight";
        }

    };

    public double getCellHeight() {
        return cellHeight.get();
    }

    public DoubleProperty cellHeightProperty() {
        return cellHeight;
    }

    public void setCellHeight(double cellHeight) {
        this.cellHeight.set(cellHeight);
    }

    private final DoubleProperty hgap = new StyleableDoubleProperty(DEFAULT_HGAP) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.H_GAP;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "hgap";
        }
    };

    public double getHgap() {
        return hgap.get();
    }

    public DoubleProperty hgapProperty() {
        return hgap;
    }

    public void setHgap(double hgap) {
        this.hgap.set(hgap);
    }

    private final DoubleProperty vgap = new StyleableDoubleProperty(DEFAULT_VGAP) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.V_GAP;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "vgap";
        }
    };

    public double getVgap() {
        return vgap.get();
    }

    public DoubleProperty vgapProperty() {
        return vgap;
    }

    public void setVgap(double vgap) {
        this.vgap.set(vgap);
    }

    private final DoubleProperty nodeLineGap = new StyleableDoubleProperty(DEFAULT_NODE_LINE_GAP) {
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.NODE_LINE_GAP;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "nodeLineGap";
        }
    };

    public double getNodeLineGap() {
        return nodeLineGap.get();
    }

    public DoubleProperty nodeLineGapProperty() {
        return nodeLineGap;
    }

    public void setNodeLineGap(double nodeLineGap) {
        this.nodeLineGap.set(nodeLineGap);
    }

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder", createDefaultPlaceholder());

    public Node getPlaceholder() {
        return placeholder.get();
    }

    public ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public void setPlaceholder(Node placeholder) {
        this.placeholder.set(placeholder);
    }

    private final ObjectProperty<LinkStrategy<T>> linkStrategy = new SimpleObjectProperty<>(this, "linkStrategy", new StraightLineLink<>());

    public LinkStrategy<T> getLinkStrategy() {
        return linkStrategy.get();
    }

    public ObjectProperty<LinkStrategy<T>> linkStrategyProperty() {
        return linkStrategy;
    }

    public void setLinkStrategy(LinkStrategy<T> linkStrategy) {
        this.linkStrategy.set(linkStrategy);
    }

    private Node createDefaultPlaceholder() {
        Label label = new Label("No tree root.");
        label.getStyleClass().add("default-placeholder");
        return label;
    }

    public enum LayoutType {
        /**
         * The tree node view will be layout in a regular way. may be wider
         */
        REGULAR,
        /**
         * The tree node view will be layout in a compact way. may be narrower.
         */
        COMPACT;
    }

    private final ObjectProperty<LayoutType> layoutType = new StyleableObjectProperty<>(DEFAULT_LAYOUT_TYPE) {
        @Override
        public CssMetaData<? extends Styleable, LayoutType> getCssMetaData() {
            return StyleableProperties.LAYOUT_TYPE;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "layoutType";
        }
    };

    public LayoutType getLayoutType() {
        return layoutType.get();
    }

    public ObjectProperty<LayoutType> layoutTypeProperty() {
        return layoutType;
    }

    public void setLayoutType(LayoutType layoutType) {
        this.layoutType.set(layoutType);
    }

    public enum LayoutDirection {
        /**
         * The tree node view will be layout from left to right.
         */
        LEFT_TO_RIGHT,
        /**
         * The tree node view will be layout from right to left.
         */
        RIGHT_TO_LEFT,
        /**
         * The tree node view will be layout from top to bottom.
         */
        TOP_TO_BOTTOM,
        /**
         * The tree node view will be layout from bottom to top.
         */
        BOTTOM_TO_TOP;
    }

    private final ObjectProperty<LayoutDirection> layoutDirection = new StyleableObjectProperty<>(DEFAULT_LAYOUT_DIRECTION) {
        @Override
        public CssMetaData<? extends Styleable, LayoutDirection> getCssMetaData() {
            return StyleableProperties.LAYOUT_DIRECTION;
        }

        @Override
        public Object getBean() {
            return TreeNodeView.this;
        }

        @Override
        public String getName() {
            return "layoutDirection";
        }
    };

    public LayoutDirection getLayoutDirection() {
        return layoutDirection.get();
    }

    public ObjectProperty<LayoutDirection> layoutDirectionProperty() {
        return layoutDirection;
    }

    public void setLayoutDirection(LayoutDirection layoutDirection) {
        this.layoutDirection.set(layoutDirection);
    }

    public void refresh() {
        Skin<?> skin = getSkin();
        if (skin instanceof TreeNodeViewSkin) {
            @SuppressWarnings("unchecked")
            TreeNodeViewSkin<T> treeNodeSkin = (TreeNodeViewSkin<T>) skin;
            treeNodeSkin.refresh();
        }
    }

    private static class StyleableProperties {

        public static final CssMetaData<TreeNodeView<?>, HPos> COLUMN_ALIGNMENT =
                new CssMetaData<>("-fx-column-alignment", new EnumConverter<>(HPos.class), DEFAULT_COLUMN_ALIGNMENT) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> n) {
                        return !n.columnAlignment.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<HPos> getStyleableProperty(TreeNodeView<?> n) {
                        return (StyleableProperty<HPos>) n.columnAlignmentProperty();
                    }
                };
        public static final CssMetaData<TreeNodeView<?>, LayoutType> LAYOUT_TYPE =
                new CssMetaData<>("-fx-layout-type", new EnumConverter<>(LayoutType.class), DEFAULT_LAYOUT_TYPE) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.layoutType.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<LayoutType> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<LayoutType>) styleable.layoutTypeProperty();
                    }
                };


        private static final CssMetaData<TreeNodeView<?>, VPos> ROW_ALIGNMENT =
                new CssMetaData<>("-fx-row-alignment", new EnumConverter<>(VPos.class), DEFAULT_ROW_ALIGNMENT) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> n) {
                        return !n.rowAlignment.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<VPos> getStyleableProperty(TreeNodeView<?> n) {
                        return (StyleableProperty<VPos>) n.rowAlignmentProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, Number> CELL_WIDTH =
                new CssMetaData<>("-fx-cell-width", SizeConverter.getInstance(), DEFAULT_CELL_WIDTH) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.cellWidth.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<Number>) styleable.cellWidthProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, Number> CELL_HEIGHT =
                new CssMetaData<>("-fx-cell-height", SizeConverter.getInstance(), DEFAULT_CELL_HEIGHT) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.cellHeight.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<Number>) styleable.cellHeightProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, Number> H_GAP =
                new CssMetaData<>("-fx-hgap", SizeConverter.getInstance(), DEFAULT_HGAP) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.hgap.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<Number>) styleable.hgapProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, Number> V_GAP =
                new CssMetaData<>("-fx-vgap", SizeConverter.getInstance(), DEFAULT_VGAP) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.vgap.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<Number>) styleable.vgapProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, Number> NODE_LINE_GAP =
                new CssMetaData<>("-fx-node-line-gap", SizeConverter.getInstance(), DEFAULT_NODE_LINE_GAP) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.nodeLineGap.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<Number>) styleable.nodeLineGapProperty();
                    }
                };

        public static final CssMetaData<TreeNodeView<?>, LayoutDirection> LAYOUT_DIRECTION =
                new CssMetaData<>("-fx-layout-direction", new EnumConverter<>(LayoutDirection.class), DEFAULT_LAYOUT_DIRECTION) {
                    @Override
                    public boolean isSettable(TreeNodeView<?> styleable) {
                        return !styleable.layoutDirection.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<LayoutDirection> getStyleableProperty(TreeNodeView<?> styleable) {
                        return (StyleableProperty<LayoutDirection>) styleable.layoutDirectionProperty();
                    }
                };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, ROW_ALIGNMENT, COLUMN_ALIGNMENT, CELL_WIDTH, CELL_HEIGHT, H_GAP, V_GAP, NODE_LINE_GAP, LAYOUT_TYPE, LAYOUT_DIRECTION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}
