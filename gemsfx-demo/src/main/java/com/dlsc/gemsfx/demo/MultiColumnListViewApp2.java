package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import com.dlsc.gemsfx.MultiColumnListView.MultiColumnListViewEvent;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
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
        multiColumnListView.getColumns().setAll(createColumns());
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

        HBox optionsBox = new HBox(10, shuffle, clearColumns, restoreColumns, separators, showHeaders, disableDragAndDrop);
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
        stage.setWidth(1000);
        stage.setHeight(850);

        stage.show();
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
