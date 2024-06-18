package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.treeview.TreeNode;
import com.dlsc.gemsfx.treeview.TreeNodeView;
import com.dlsc.gemsfx.treeview.link.ClockHandLinkStrategy;
import com.dlsc.gemsfx.treeview.link.CurvedLineLink;
import com.dlsc.gemsfx.treeview.link.LinkStrategy;
import com.dlsc.gemsfx.treeview.link.LogarithmicLink;
import com.dlsc.gemsfx.treeview.link.PolyLineLink;
import com.dlsc.gemsfx.treeview.link.QuadCurveLink;
import com.dlsc.gemsfx.treeview.link.SimpleCatmullRomLink;
import com.dlsc.gemsfx.treeview.link.SineWaveDecayLink;
import com.dlsc.gemsfx.treeview.link.StraightLineLink;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Objects;

public class TreeNodeViewApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TreeNodeView<String> treePane = new TreeNodeView<>();
        treePane.setVgap(50);
        treePane.setHgap(10);
        treePane.setRowAlignment(VPos.TOP);
        TreeNode<String> root = createTree();
        treePane.setRoot(root);
        treePane.setStyle("-fx-border-color: #4da2d2;-fx-background-color: #e0e7ec;-fx-padding: 10px;");
        BorderPane parent = new BorderPane(new ScrollPane(treePane));

        ComboBox<LinkStrategy<String>> linkStrategyComboBox = new ComboBox<>();
        linkStrategyComboBox.getItems().addAll(List.of(
                new CurvedLineLink<>(),
                new QuadCurveLink<>(),
                new LogarithmicLink<>(),
                new SineWaveDecayLink<>(),
                new SimpleCatmullRomLink<>(),
                new ClockHandLinkStrategy<>(),
                new PolyLineLink<>(),
                new StraightLineLink<>()));

        linkStrategyComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(LinkStrategy<String> object) {
                return object.getClass().getSimpleName();
            }

            @Override
            public LinkStrategy<String> fromString(String string) {
                return null;
            }
        });
        linkStrategyComboBox.getSelectionModel().select(0);
        treePane.linkStrategyProperty().bind(linkStrategyComboBox.valueProperty());
        VBox linkStrategy = createControlBox("Link Strategy", linkStrategyComboBox);

        Spinner<Double> nodeLineGapSpinner = new Spinner<>(5, 20, 10, 5);
        treePane.nodeLineGapProperty().bind(nodeLineGapSpinner.valueProperty());
        VBox nodeLineGapBox = createControlBox("Node Line Gap", nodeLineGapSpinner);

        Spinner<Double> hGapSpinner = new Spinner<>(15, 80, 20, 5);
        treePane.hgapProperty().bind(hGapSpinner.valueProperty());
        VBox hGapBox = createControlBox("HGap", hGapSpinner);

        Spinner<Double> vGapSpinner = new Spinner<>(15, 100, 50, 5);
        treePane.vgapProperty().bind(vGapSpinner.valueProperty());
        VBox vGapBox = createControlBox("VGap", vGapSpinner);

        ComboBox<TreeNodeView.LayoutType> layoutTypeComboBox = new ComboBox<>();
        layoutTypeComboBox.getItems().setAll(TreeNodeView.LayoutType.values());
        layoutTypeComboBox.setValue(TreeNodeView.LayoutType.REGULAR);
        treePane.layoutTypeProperty().bind(layoutTypeComboBox.valueProperty());
        VBox layoutTypeBox = createControlBox("Layout Type", layoutTypeComboBox);

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            if (!treePane.getRoot().getChildren().isEmpty()) {
                treePane.getRoot().getChildren().remove(0);
            }
        });
        VBox removeBox = createControlBox("Remove First", removeButton);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            treePane.refresh();
        });
        VBox refreshBox = createControlBox("Refresh", refreshButton);

        Button changeWidthButton = new Button("Change Width");
        changeWidthButton.setOnAction(e -> {
            double width = Math.random() * 50 + 35;
            treePane.getRoot().getChildren().get(0).setWidth(width);
            treePane.getRoot().getChildren().get(1).getChildren().get(0).setHeight(Math.random() * 50 + 20);
        });

        Button changeRootButton = createChangeRootBtn(treePane);
        VBox changeRootBox = createControlBox("Change Root", changeRootButton);

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            treePane.setRoot(null);
        });
        VBox clearTree = createControlBox("Clear Tree", clearBtn);

        ComboBox<TreeNodeView.LayoutDirection> layoutDirectionComboBox = new ComboBox<>();
        layoutDirectionComboBox.getItems().setAll(TreeNodeView.LayoutDirection.values());
        layoutDirectionComboBox.setValue(TreeNodeView.LayoutDirection.TOP_TO_BOTTOM);
        treePane.layoutDirectionProperty().bind(layoutDirectionComboBox.valueProperty());
        VBox layoutDirectionBox = createControlBox("Layout Direction", layoutDirectionComboBox);

        ComboBox<HPos> colAlignmentComboBox = new ComboBox<>();
        colAlignmentComboBox.getItems().addAll(HPos.LEFT, HPos.CENTER, HPos.RIGHT);
        colAlignmentComboBox.setValue(HPos.LEFT);
        treePane.columnAlignmentProperty().bind(colAlignmentComboBox.valueProperty());
        colAlignmentComboBox.disableProperty().bind(treePane.layoutDirectionProperty().isNotEqualTo(TreeNodeView.LayoutDirection.LEFT_TO_RIGHT)
                .and(treePane.layoutDirectionProperty().isNotEqualTo(TreeNodeView.LayoutDirection.RIGHT_TO_LEFT)));
        VBox colAlignmentBox = createControlBox("Column Alignment", colAlignmentComboBox);

        ComboBox<VPos> rowAlignment = new ComboBox<>();
        rowAlignment.getItems().addAll(VPos.TOP, VPos.CENTER, VPos.BOTTOM);
        rowAlignment.setValue(VPos.TOP);
        treePane.rowAlignmentProperty().bind(rowAlignment.valueProperty());
        rowAlignment.disableProperty().bind(treePane.layoutDirectionProperty().isNotEqualTo(TreeNodeView.LayoutDirection.TOP_TO_BOTTOM)
                .and(treePane.layoutDirectionProperty().isNotEqualTo(TreeNodeView.LayoutDirection.BOTTOM_TO_TOP)));
        VBox rowAlignmentBox = createControlBox("Row Alignment", rowAlignment);

        VBox bottom = new VBox(10, linkStrategy, nodeLineGapBox,
                hGapBox, vGapBox, layoutTypeBox,
                removeBox, refreshBox, changeRootBox,
                clearTree, layoutDirectionBox, rowAlignmentBox, colAlignmentBox);
        bottom.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(bottom);
        scrollPane.setPrefWidth(220);
        scrollPane.setFitToWidth(true);
        parent.setRight(scrollPane);
        Scene scene = new Scene(parent, 1280, 800);
        scene.getStylesheets().add(Objects.requireNonNull(TreeNodeViewApp.class.getResource("tree-node-view-app.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        CSSFX.start();

    }

    private Button createChangeRootBtn(TreeNodeView<String> treePane) {
        Button changeRootButton = new Button("Change Root");
        changeRootButton.setOnAction(e -> {
            if (treePane.getRoot()!=null && "ROOT".equalsIgnoreCase(treePane.getRoot().getValue())) {
                TreeNode<String> root2 = createTree2();
                treePane.setCellWidth(135);
                treePane.setRoot(root2);
            } else {
                TreeNode<String> root1 = createTree();
                treePane.setCellWidth(60);
                treePane.setRoot(root1);
            }
        });
        return changeRootButton;
    }

    private VBox createControlBox(String description, Node node) {
        Label label = new Label(description);
        label.setStyle("-fx-text-fill: gray;-fx-font-size: 15px");
        VBox box = new VBox(5, label, node);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-padding:5px 10px;-fx-border-color: rgba(128,128,128,0.52);-fx-border-width: 0 0 1px 0;");
        return box;
    }

    public TreeNode<String> createTree() {
        TreeNode<String> treeRoot = new TreeNode<>("root");
        treeRoot.setWidth(180);
        //treeRoot.setHeight(60);

        TreeNode<String> node1 = new TreeNode<>("A");
        node1.setWidth(138);
        node1.setHeight(50);
        TreeNode<String> node2 = new TreeNode<>("B");
        node2.setExpanded(false);
        TreeNode<String> node3 = new TreeNode<>("C");

        treeRoot.getChildren().addAll(node1, node2, node3);

        TreeNode<String> node11 = new TreeNode<>("A1");
        TreeNode<String> node12 = new TreeNode<>("A2");
        TreeNode<String> node13 = new TreeNode<>("A3");

        node1.getChildren().addAll(node11, node12, node13);

        TreeNode<String> node131 = new TreeNode<>("A3-1");
        node13.getChildren().add(node131);

        node131.getChildren().add(new TreeNode<>("A3-1-1"));

        TreeNode<String> node21 = new TreeNode<>("B1");
        TreeNode<String> node22 = new TreeNode<>("B2");
        TreeNode<String> b2_2 = new TreeNode<>("B2-2");
        b2_2.getChildren().addAll(new TreeNode<>("B2-2-1"));
        node22.getChildren().addAll(new TreeNode<>("B2-1"), b2_2);

        node2.getChildren().addAll(node21, node22);


        TreeNode<String> node31 = new TreeNode<>("C1");
        TreeNode<String> node32 = new TreeNode<>("C2");
        TreeNode<String> node33 = new TreeNode<>("C3");

        node3.getChildren().addAll(node31, node32, node33);

        TreeNode<String> node111 = new TreeNode<>("A1-1");
        TreeNode<String> node112 = new TreeNode<>("A1-2");

        node111.getChildren().addAll(node111, node112,new TreeNode<>("A1-1-1"),new TreeNode<>("A1-1-2"));

        TreeNode<String> node211 = new TreeNode<>("B1-1");
        TreeNode<String> node212 = new TreeNode<>("B1-2");


        node21.getChildren().addAll(node211, node212);

        TreeNode<String> node311 = new TreeNode<>("C1-1-1");

        node311.getChildren().addAll(new TreeNode<>("C1-1-1-1"),new TreeNode<>("C1-1-1-2"));

        TreeNode<String> nodec32 = new TreeNode<>("C3-2");
        nodec32.getChildren().addAll(new TreeNode<>("C3-2-1"),new TreeNode<>("C3-2-2"));

        node33.getChildren().addAll(new TreeNode<>("C3-1"),nodec32);

        return treeRoot;
    }

    public static TreeNode<String> createTree2() {
        TreeNode<String> root = new TreeNode<>("DO155 for DO");

        TreeNode<String> node1 = new TreeNode<>("DO153 for DO");
        //node1.setExpanded(false);
        TreeNode<String> node2 = new TreeNode<>("DO155 for MOP");
        //node2.setExpanded(false);
        TreeNode<String> node3 = new TreeNode<>("DO155 for DC");
        //node3.setExpanded(false);
        root.getChildren().addAll(node1, node2, node3);

        TreeNode<String> node11 = new TreeNode<>("D0011 from DA");
        node11.setName("da");
        TreeNode<String> node12 = new TreeNode<>("D0011 from MOP");
        node12.setName("mop");
        TreeNode<String> node13 = new TreeNode<>("D0011 from DC");
        node13.setName("dc");

        node1.getChildren().add(node11);
        node2.getChildren().add(node12);
        node3.getChildren().add(node13);


        TreeNode<String> node121 = new TreeNode<>("Agent Concensus");
        node121.setName("agent");
        //node121.setExpanded(false);
        node12.getChildren().add(node121);

        node11.getLinkedNodes().addAll(node121);
        node13.getLinkedNodes().add(node121);


        TreeNode<String> node1211 = new TreeNode<>("D0148  for MOP");
        TreeNode<String> node1212 = new TreeNode<>("D0148 for DC");

        node121.getChildren().addAll(node1211, node1212);

        TreeNode<String> node12111 = new TreeNode<>("D0268");
        node1211.getChildren().add(node12111);

        TreeNode<String> node12121 = new TreeNode<>("D0012");
        node1212.getChildren().add(node12121);

        TreeNode<String> node121211 = new TreeNode<>("D0265");
        node12121.getChildren().add(node121211);

        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
