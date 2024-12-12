package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PagingControlBase;
import com.dlsc.gemsfx.PagingControlBase.MessageLabelStrategy;
import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.scenicview.ScenicView;

import java.util.Objects;

public class PagingControlsApp extends Application {

    private final ObjectProperty<HPos> alignmentProperty = new SimpleObjectProperty<>(HPos.RIGHT);

    @Override
    public void start(Stage stage) {
        ObjectProperty<Integer> obj = new SimpleObjectProperty<>();

        IntegerProperty inti = new SimpleIntegerProperty(10);
        obj.bindBidirectional(inti.asObject());

        System.out.println("Obj: " + obj.get());
        obj.set(20);
        System.out.println("inti: " + inti.get());

        VBox vBox1 = createSection(10, 221, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS, 1);
        VBox vBox2 = createSection(10, 221, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.HIDE, 2);
        VBox vBox3 = createSection(10, 221, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.HIDE, 3);
        VBox vBox4 = createSection(15, 45, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.HIDE, 4);
        VBox vBox5 = createSection(20, 1000, MessageLabelStrategy.SHOW_WHEN_NEEDED, PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS, 5);
        VBox vBox6 = createSection(5, 5, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.HIDE, 6);
        VBox vBox7 = createSection(5, 0, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.HIDE, 7);
        VBox vBox8 = createSection(10, 200, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS, 8);
        VBox vBox9 = createSection(10, 200, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS, 9);
        VBox vBox10 = createSection(10, 200, MessageLabelStrategy.ALWAYS_SHOW, PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS, 10);

        ChoiceBox<HPos> alignmentChoiceBox = new ChoiceBox<>();
        alignmentChoiceBox.getItems().setAll(HPos.values());
        alignmentChoiceBox.valueProperty().bindBidirectional(alignmentProperty);

        Button scenicViewButton = new Button("Scenic View");
        scenicViewButton.setOnAction(event -> ScenicView.show(stage.getScene()));

        HBox hBox = new HBox(10, alignmentChoiceBox, scenicViewButton);
        hBox.setAlignment(Pos.CENTER_LEFT);

        VBox all = new VBox(20, hBox, vBox1, vBox2, vBox3, vBox4, vBox5, vBox6, vBox7, vBox8, vBox9, vBox10);

        StackPane stackPane = new StackPane(all);
        stackPane.setPadding(new Insets(50, 50, 50, 50));

        ScrollPane scrollPane = new ScrollPane(stackPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);

        CSSFX.start(stackPane);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Paging View");

        StageManager.install(stage, "paging-controls-app");

        stage.show();

        CSSFX.start(stackPane);
    }

    private VBox createSection(int pageSize, int itemCount, MessageLabelStrategy messageLabelStrategy, PagingControls.FirstLastPageDisplayMode displayMode, int index) {
        PagingControls pagingControls = new PagingControls();
        pagingControls.alignmentProperty().bind(alignmentProperty);
        pagingControls.setMessageLabelStrategy(messageLabelStrategy);
        pagingControls.setTotalItemCount(itemCount);
        pagingControls.setPageSize(pageSize);
        pagingControls.setFirstLastPageDisplayMode(displayMode);
        pagingControls.getStylesheets().add(Objects.requireNonNull(PagingControlsApp.class.getResource("paging-controls-" + index + ".css")).toExternalForm());
        pagingControls.setStyle("-fx-border-color: black; -fx-padding: 20px");
        pagingControls.setPrefWidth(800);
        HBox.setHgrow(pagingControls, Priority.ALWAYS);

        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_DOUBLE_DOWN));
        toggleButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        PagingControlsSettingsView pagingControlsSettingsView = new PagingControlsSettingsView(pagingControls);
        pagingControlsSettingsView.visibleProperty().bind(toggleButton.selectedProperty());
        pagingControlsSettingsView.managedProperty().bind(toggleButton.selectedProperty());

        HBox hBox = new HBox(10, pagingControls, toggleButton);
        hBox.setFillHeight(true);

        VBox vBox = new VBox(10, hBox, pagingControlsSettingsView);
        vBox.setMaxHeight(Region.USE_PREF_SIZE);

        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}