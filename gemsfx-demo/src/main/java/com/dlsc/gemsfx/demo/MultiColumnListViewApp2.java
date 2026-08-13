package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.LoadingPane.Status;
import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import com.dlsc.gemsfx.MultiColumnListView.MultiColumnListViewEvent;
import com.dlsc.gemsfx.Skeleton;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A variant of the {@link MultiColumnListViewApp} that uses plain {@link String} objects as its
 * model and that relies on the default cell factory of the control, hence no custom cell
 * implementation is needed.
 */
public class MultiColumnListViewApp2 extends GemApplication {

    /**
     * Time that passes before the columns are added to the view. Simulates a slow backend
     * so that the skeleton placeholder can be seen when the demo starts.
     */
    private static final Duration LOADING_DELAY = Duration.seconds(2);

    private static final List<String> CITIES = Arrays.asList(
            "Amsterdam", "Athens", "Auckland", "Bangkok", "Barcelona", "Beijing", "Berlin",
            "Bogotá", "Buenos Aires", "Cairo", "Cape Town", "Chicago", "Copenhagen", "Delhi",
            "Dubai", "Dublin", "Hanoi", "Helsinki", "Hong Kong", "Istanbul", "Jakarta",
            "Johannesburg", "Lagos", "Lima", "Lisbon", "London", "Los Angeles", "Madrid",
            "Manila", "Melbourne", "Mexico City", "Montréal", "Moscow", "Mumbai", "Nairobi",
            "New York", "Oslo", "Paris", "Prague", "Reykjavík", "Rio de Janeiro", "Rome",
            "San Francisco", "Santiago", "São Paulo", "Seoul", "Shanghai", "Singapore",
            "Stockholm", "Sydney", "Tokyo", "Toronto", "Vancouver", "Vienna", "Warsaw", "Zurich");

    private final ListViewColumn<String> col1 = new ListViewColumn<>();
    private final ListViewColumn<String> col2 = new ListViewColumn<>();
    private final ListViewColumn<String> col3 = new ListViewColumn<>();
    private final ListViewColumn<String> col4 = new ListViewColumn<>();
    private final ListViewColumn<String> col5 = new ListViewColumn<>();

    @Override
    public void start(Stage stage) {
        super.start(stage);

        MultiColumnListView<String> multiColumnListView = new MultiColumnListView<>();

        Node placeholder = createSkeletonPlaceholder();
        multiColumnListView.setPlaceholder(placeholder);
        VBox.setVgrow(multiColumnListView, Priority.ALWAYS);

        CheckBox showHeaders = new CheckBox("Show Headers");
        showHeaders.selectedProperty().bindBidirectional(multiColumnListView.showHeadersProperty());

        CheckBox disableDragAndDrop = new CheckBox("Disable Editing");
        disableDragAndDrop.selectedProperty().bindBidirectional(multiColumnListView.disableDragAndDropProperty());

        Callback<Integer, Node> separatorFactory = multiColumnListView.getSeparatorFactory();

        CheckBox separators = new CheckBox("Use Separators");
        separators.setSelected(true);
        separators.selectedProperty().addListener(it -> {
            if (separators.isSelected()) {
                multiColumnListView.setSeparatorFactory(separatorFactory);
            } else {
                multiColumnListView.setSeparatorFactory(null);
            }
        });

        Button shuffle = new Button("Shuffle Cities");
        shuffle.setOnAction(evt -> distributeCities());
        shuffle.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty());

        Button clearColumns = new Button("Clear Columns");
        clearColumns.setOnAction(evt -> multiColumnListView.getColumns().clear());
        clearColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty());

        Button restoreColumns = new Button("Restore Columns");
        restoreColumns.setOnAction(evt -> multiColumnListView.getColumns().setAll(col1, col2, col3, col4, col5));
        restoreColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty().not());

        ComboBox<Status> loadingStatusBox = new ComboBox<>();
        loadingStatusBox.getItems().addAll(Status.values());
        loadingStatusBox.valueProperty().bindBidirectional(multiColumnListView.loadingStatusProperty());

        HBox loadingStatusOption = new HBox(5, new Label("Loading Status:"), loadingStatusBox);
        loadingStatusOption.setAlignment(Pos.CENTER_LEFT);

        HBox optionsBox = new HBox(10, shuffle, clearColumns, restoreColumns, createShimmerToggle(placeholder), loadingStatusOption, separators, showHeaders, disableDragAndDrop);
        optionsBox.setAlignment(Pos.CENTER_RIGHT);
        createThemeSwitcher().ifPresent(switcher -> optionsBox.getChildren().add(0, switcher));

        StatusBar statusBar = new StatusBar();
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, e -> statusBar.setText(e.getDraggedItem() + " was moved to column: " + e.getColumn().getUserObject() + " at index: " + e.getIndex()));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DRAG_NOT_POSSIBLE, e -> statusBar.setText(e.getDraggedItem() + " can not be dragged"));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, e -> statusBar.setText("Drop not possible at index " + e.getIndex() + " in column: " + e.getColumn().getUserObject()));

        VBox vbox = new VBox(10, multiColumnListView, optionsBox);
        vbox.setAlignment(Pos.TOP_RIGHT);
        vbox.setPadding(new Insets(20));

        VBox outerBox = new VBox(vbox, statusBar);
        VBox.setVgrow(vbox, Priority.ALWAYS);

        Scene scene = new Scene(outerBox);

        CSSFX.start();

        stage.setTitle("MultiColumnListView (Strings)");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(850);

        stage.show();

        // start out empty so that the skeleton placeholder becomes visible, then "load" the data
        List<ListViewColumn<String>> columns = createColumns();
        PauseTransition loadingDelay = new PauseTransition(LOADING_DELAY);
        loadingDelay.setOnFinished(evt -> multiColumnListView.getColumns().setAll(columns));
        loadingDelay.play();
    }

    /**
     * Creates the placeholder that will be shown as long as no columns have been added to the
     * view. It uses the {@link Skeleton} control to mimic five columns of cells that are still
     * being loaded.
     *
     * @return the placeholder node
     */
    private Node createSkeletonPlaceholder() {
        HBox placeholder = new HBox(10);
        placeholder.setFillHeight(true);

        for (int i = 0; i < 5; i++) {
            VBox column = new VBox(12);
            column.setPadding(new Insets(10));

            for (int j = 0; j < 8; j++) {
                Skeleton cell = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
                cell.setPrefHeight(30);
                cell.setMinHeight(30);
                column.getChildren().add(cell);
            }

            HBox.setHgrow(column, Priority.ALWAYS);
            placeholder.getChildren().add(column);
        }

        return placeholder;
    }

    private List<ListViewColumn<String>> createColumns() {
        col1.setHeader(new Label("Europe"));
        col2.setHeader(new Label("Americas"));
        col3.setHeader(new Label("Asia"));
        col4.setHeader(new Label("Africa"));
        col5.setHeader(new Label("Oceania"));

        col1.setUserObject("col1");
        col2.setUserObject("col2");
        col3.setUserObject("col3");
        col4.setUserObject("col4");
        col5.setUserObject("col5");

        distributeCities();

        return List.of(col1, col2, col3, col4, col5);
    }

    /**
     * Randomly distributes a couple of cities across the five columns.
     */
    private void distributeCities() {
        List<String> cities = new ArrayList<>(CITIES);
        Collections.shuffle(cities);

        List<ListViewColumn<String>> columns = List.of(col1, col2, col3, col4, col5);
        columns.forEach(column -> column.getItems().clear());

        for (int i = 0; i < cities.size(); i++) {
            columns.get(i % columns.size()).getItems().add(cities.get(i));
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
