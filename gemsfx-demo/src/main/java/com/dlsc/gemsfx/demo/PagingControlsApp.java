package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.PagingControls.MessageLabelStrategy;
import com.dlsc.gemsfx.Spacer;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class PagingControlsApp extends Application {

    @Override
    public void start(Stage stage) {
        VBox vBox1 = createSection(10, 221, false);
        VBox vBox2 = createSection(15, 45, false);
        VBox vBox3 = createSection(20, 1000, true);
        VBox vBox4 = createSection(5, 5, false);

        VBox all = new VBox(20, vBox1, vBox2, vBox3, vBox4);

        StackPane stackPane = new StackPane(all);
        stackPane.setPadding(new Insets(50, 50, 50, 50));

        Scene scene = new Scene(stackPane);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Paging View");
        stage.show();

        CSSFX.start(scene);
    }

    private static VBox createSection(int pageSize, int itemCount, boolean showFirstLastButtons) {
        PagingControls pagingControls = new PagingControls();

        pagingControls.setTotalItemCount(itemCount);
        pagingControls.setPageSize(pageSize);

        pagingControls.setShowGotoFirstPageButton(showFirstLastButtons);
        pagingControls.setShowGotoLastPageButton(showFirstLastButtons);

        pagingControls.setStyle("-fx-border-color: black; -fx-padding: 20px");
        pagingControls.setPrefWidth(800);

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page Index: " + pagingControls.getPage(), pagingControls.pageProperty()));

        Label pageCountLabel = new Label();
        pageCountLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page count: " + pagingControls.getPageCount(), pagingControls.pageCountProperty()));

        CheckBox showGotoFirstPageButton = new CheckBox("First page button");
        showGotoFirstPageButton.selectedProperty().bindBidirectional(pagingControls.showGotoFirstPageButtonProperty());

        CheckBox showGotoLastPageButton = new CheckBox("Last page button");
        showGotoLastPageButton.selectedProperty().bindBidirectional(pagingControls.showGotoLastPageButtonProperty());

        CheckBox showMaxPage = new CheckBox("Show max page");
        showMaxPage.selectedProperty().bindBidirectional(pagingControls.showMaxPageProperty());

        ChoiceBox<MessageLabelStrategy> strategyChoiceBox = new ChoiceBox<>();
        strategyChoiceBox.getItems().addAll(MessageLabelStrategy.values());
        strategyChoiceBox.valueProperty().bindBidirectional(pagingControls.messageLabelStrategyProperty());

        ChoiceBox<Integer> maxPageIndicatorsBox = new ChoiceBox<>();
        maxPageIndicatorsBox.getItems().setAll(List.of(1, 2, 5, 10));
        maxPageIndicatorsBox.valueProperty().bindBidirectional(pagingControls.maxPageIndicatorsCountProperty().asObject());

        HBox strategyBox = new HBox(5, new Label("Label strategy: "), strategyChoiceBox);
        strategyBox.setAlignment(Pos.CENTER_LEFT);

        HBox indicatorBox = new HBox(5, new Label("# Indicators: "), maxPageIndicatorsBox);
        indicatorBox.setAlignment(Pos.CENTER_LEFT);

        FlowPane flowPane = new FlowPane(pageLabel, pageCountLabel, new Spacer(), showGotoFirstPageButton, showGotoLastPageButton, showMaxPage, strategyBox, indicatorBox);
        flowPane.setVgap(10);
        flowPane.setHgap(20);

        VBox vBox = new VBox(10, pagingControls, flowPane);
        vBox.setMaxHeight(Region.USE_PREF_SIZE);

        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}