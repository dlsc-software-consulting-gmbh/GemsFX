package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import com.dlsc.gemsfx.MultiColumnListView.MultiColumnListViewEvent;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.StatusBar;

import java.util.List;
import java.util.Objects;

public class MultiColumnListViewApp extends GemApplication {
    private final ListViewColumn<Issue> col1 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col2 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col3 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col4 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col5 = new ListViewColumn<>();

    @Override
    public void start(Stage stage) { super.start(stage);
        MultiColumnListView<Issue> multiColumnListView = new MultiColumnListView<>();
        multiColumnListView.setCellFactory(listView -> new IssueListCell(multiColumnListView));
        multiColumnListView.getColumns().setAll(createColumns());
        multiColumnListView.setDragPossibleCallback(issue -> !issue.getStatus().equals("done"));
        multiColumnListView.setDropPossibleCallback(para -> !para.getColumn().getUserObject().equals("col1"));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.ANY, System.out::println);
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

        Button clearColumns = new Button("Clear Columns");
        clearColumns.setOnAction(evt -> multiColumnListView.getColumns().clear());
        clearColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty());

        Button restoreColumns = new Button("Restore Columns");
        restoreColumns.setOnAction(evt -> multiColumnListView.getColumns().setAll(col1, col2, col3, col4, col5));
        restoreColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty().not());

        HBox optionsBox = new HBox(10, clearColumns, restoreColumns, separators, showHeaders, disableDragAndDrop);
        optionsBox.setAlignment(Pos.CENTER_RIGHT);

        StatusBar statusBar = new StatusBar();
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DRAG_NOT_POSSIBLE, e-> statusBar.setText("Drag not possible"));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, e-> statusBar.setText("Drop here not possible at index " + e.getIndex() + " in column: " + e.getColumn().getUserObject()));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, e-> statusBar.setText("Item was moved to column: " + e.getColumn().getUserObject() + " at index: " + e.getIndex()));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DRAG_OVER, e-> statusBar.setText("Item dragged over column: " + e.getColumn().getUserObject() + " at index: " + e.getIndex()));

        VBox vbox = new VBox(10, multiColumnListView, optionsBox);
        vbox.setAlignment(Pos.TOP_RIGHT);
        vbox.setPadding(new Insets(20));

        VBox outerBox = new VBox(vbox, statusBar);
        VBox.setVgrow(vbox, Priority.ALWAYS);

        Scene scene = new Scene(outerBox);
        scene.getStylesheets().add(Objects.requireNonNull(MultiColumnListViewApp.class.getResource("multi-column-app.css")).toExternalForm());

        CSSFX.start();

        stage.setTitle("MultiColumnListView");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);

        stage.show();
    }

    private List<ListViewColumn<Issue>> createColumns() {
        col1.setHeader(new Label("Column 1"));
        col2.setHeader(new Label("Column 2"));
        col3.setHeader(new Label("Column 3"));
        col4.setHeader(new Label("Column 4"));
        col5.setHeader(new Label("Column 5"));

        col1.setUserObject("col1");
        col2.setUserObject("col2");
        col3.setUserObject("col3");
        col4.setUserObject("col4");
        col5.setUserObject("col5");

        col1.getItems().setAll(new Issue("Dirk"), new Issue("Katja"), new Issue("Philip"));
        col2.getItems().setAll(new Issue("Jule"), new Issue("Franz"), new Issue("Paul"), new Issue("Orange"), new Issue("Yellow"), new Issue("Red"), new Issue("Mango"), new Issue("Apple"), new Issue("Pear"), new Issue("Sun"), new Issue("Moon"), new Issue("Saturn"));
        col3.getItems().setAll(new Issue("Armin"));
        col5.getItems().setAll(new Issue("Seattle"), new Issue("New York"), new Issue("Zurich"), new Issue("Berlin"));

        return List.of(col1, col2, col3, col4, col5);
    }

    public static class Issue {

        private String title;
        private String status;

        public Issue(String title) {
            this.title = title;

            switch ((int) (Math.random() * 3)) {
                case 0:
                    this.status = "todo";
                    break;
                case 1:
                    this.status = "in-progress";
                    break;
                case 2:
                    this.status = "done";
                    break;
            }
        }

        public String getTitle() {
            return title;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class IssueListCell extends ColumnListCell<Issue> {

        private final StackPane wrapper;

        public IssueListCell(MultiColumnListView<Issue> multiColumnListView) {
            super(multiColumnListView);

            getStyleClass().add("issue-list-cell");

            VBox content = new VBox();
            content.getStyleClass().add("content");
            content.visibleProperty().bind(placeholderProperty().not().and(emptyProperty().not()));
            content.managedProperty().bind(placeholderProperty().not().and(emptyProperty().not()));

            VBox contentPlaceholder = new VBox();
            contentPlaceholder.getStyleClass().add("placeholder");
            contentPlaceholder.visibleProperty().bind(placeholderProperty());
            contentPlaceholder.managedProperty().bind(placeholderProperty());

            Label label = new Label();
            label.textProperty().bind(textProperty());

            wrapper = new StackPane(content, contentPlaceholder, label);
            setGraphic(wrapper);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected Node getSnapshotNode() {
            return wrapper;
        }

        @Override
        protected void updateUserObject(Issue item, boolean empty) {
            getStyleClass().removeAll("todo", "in-progress", "done");

            if (isFromPlaceholder()) {
                setText("From");
            } else if (isToPlaceholder()) {
                setText("To");
            } else if (item != null && !empty) {
                setText(item.getTitle() + "\n(" + item.getStatus() + ")");
                getStyleClass().add(item.getStatus());
            } else {
                setText("");
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
