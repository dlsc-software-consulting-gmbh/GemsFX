package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.PagingControls.MessageLabelStrategy;
import com.dlsc.gemsfx.Spacer;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
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

    private final ObjectProperty<HPos> alignmentProperty = new SimpleObjectProperty<>(HPos.RIGHT);

    @Override
    public void start(Stage stage) {
        VBox vBox1 = createSection(10, 221, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.HIDE);
        VBox vBox2 = createSection(15, 45, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS);
        VBox vBox3 = createSection(20, 1000, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS);
        VBox vBox4 = createSection(5, 5, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.HIDE);
        VBox vBox5 = createSection(5, 0, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.HIDE);

        ChoiceBox<HPos> alignmentChoiceBox = new ChoiceBox<>();
        alignmentChoiceBox.getItems().setAll(HPos.values());
        alignmentChoiceBox.valueProperty().bindBidirectional(alignmentProperty);

        VBox all = new VBox(20, alignmentChoiceBox, vBox1, vBox2, vBox3, vBox4, vBox5);

        StackPane stackPane = new StackPane(all);
        stackPane.setPadding(new Insets(50, 50, 50, 50));

        Scene scene = new Scene(stackPane);

        CSSFX.start(stackPane);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Paging View");
        stage.show();
    }

    private VBox createSection(int pageSize, int itemCount, MessageLabelStrategy messageLabelStrategy, PagingControls.FirstLastPageDisplayMode displayMode) {
        PagingControls pagingControls = new PagingControls();
        pagingControls.alignmentProperty().bind(alignmentProperty);
        pagingControls.setMessageLabelStrategy(messageLabelStrategy);
        pagingControls.setTotalItemCount(itemCount);
        pagingControls.setPageSize(pageSize);
        pagingControls.setFirstLastPageDisplayMode(displayMode);

        pagingControls.setStyle("-fx-border-color: black; -fx-padding: 20px");
        pagingControls.setPrefWidth(800);

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page Index: " + pagingControls.getPage(), pagingControls.pageProperty()));

        Label pageCountLabel = new Label();
        pageCountLabel.textProperty().bind(Bindings.createStringBinding(() -> "Page count: " + pagingControls.getPageCount(), pagingControls.pageCountProperty()));

        ChoiceBox<PagingControls.FirstLastPageDisplayMode> displayModeChoiceBox = new ChoiceBox<>();
        displayModeChoiceBox.getItems().setAll(PagingControls.FirstLastPageDisplayMode.values());
        displayModeChoiceBox.valueProperty().bindBidirectional(pagingControls.firstLastPageDisplayModeProperty());

        CheckBox showPreviousNextButton = new CheckBox("Show prev / next buttons");
        showPreviousNextButton.selectedProperty().bindBidirectional(pagingControls.showPreviousNextPageButtonProperty());

        ChoiceBox<MessageLabelStrategy> strategyChoiceBox = new ChoiceBox<>();
        strategyChoiceBox.getItems().addAll(MessageLabelStrategy.values());
        strategyChoiceBox.valueProperty().bindBidirectional(pagingControls.messageLabelStrategyProperty());

        ChoiceBox<Integer> maxPageIndicatorsBox = new ChoiceBox<>();
        maxPageIndicatorsBox.getItems().setAll(List.of(1, 2, 5, 10));
        maxPageIndicatorsBox.valueProperty().bindBidirectional(pagingControls.maxPageIndicatorsCountProperty().asObject());

        HBox displayModeBox = new HBox(5, new Label("Display mode: "), displayModeChoiceBox);
        displayModeBox.setAlignment(Pos.CENTER_LEFT);

        HBox strategyBox = new HBox(5, new Label("Label strategy: "), strategyChoiceBox);
        strategyBox.setAlignment(Pos.CENTER_LEFT);

        HBox indicatorBox = new HBox(5, new Label("# Indicators: "), maxPageIndicatorsBox);
        indicatorBox.setAlignment(Pos.CENTER_LEFT);

        FlowPane flowPane = new FlowPane(pageLabel, pageCountLabel, new Spacer(), showPreviousNextButton, displayModeBox, strategyBox, indicatorBox);
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