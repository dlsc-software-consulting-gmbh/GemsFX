package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MultiColumnListViewApp extends Application {

    @Override
    public void start(Stage stage) {
        MultiColumnListView<Issue> multiColumnListView = new MultiColumnListView<>();
        multiColumnListView.setCellFactory(listView -> new IssueListCell(multiColumnListView));
        multiColumnListView.getColumns().setAll(createColumns());
        multiColumnListView.setPlaceholderFrom(new Issue(""));
        multiColumnListView.setPlaceholderTo(new Issue(""));

        StackPane stackPane = new StackPane(multiColumnListView);
        stackPane.setPadding(new Insets(20));
        stackPane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(stackPane);
        scene.getStylesheets().add(MultiColumnListViewApp.class.getResource("multi-column-app.css").toExternalForm());

        CSSFX.start();

        stage.setTitle("MultiColumnListView");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    private List<ListViewColumn<Issue>> createColumns() {
        ListViewColumn<Issue> col1 = new ListViewColumn<>();
        ListViewColumn<Issue> col2 = new ListViewColumn<>();
        ListViewColumn<Issue> col3 = new ListViewColumn<>();
        ListViewColumn<Issue> col4 = new ListViewColumn<>();
        ListViewColumn<Issue> col5 = new ListViewColumn<>();

        col1.getItems().setAll(new Issue("Dirk"), new Issue("Katja"), new Issue("Philip"));
        col2.getItems().setAll(new Issue("Jule"), new Issue("Franz"), new Issue("Paul"), new Issue("Orange"), new Issue("Yellow"), new Issue("Red"), new Issue("Mango"), new Issue("Apple"), new Issue("Pear"), new Issue("Sun"), new Issue("Moon"), new Issue("Saturn"));
        col3.getItems().setAll(new Issue("Armin"));
        col5.getItems().setAll(new Issue("Seattle"), new Issue("New York"), new Issue("Zurich"), new Issue("Berlin"));

        return List.of(col1, col2, col3, col4, col5);
    }

    public class Issue {

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

    public class IssueListCell extends ColumnListCell<Issue> {

        public IssueListCell(MultiColumnListView<Issue> multiColumnListView) {
            super(multiColumnListView);

            getStyleClass().add("issue-list-cell");

            VBox content = new VBox();
            content.getStyleClass().add("content");
            content.visibleProperty().bind(placeholder.not().and(emptyProperty().not()));
            content.managedProperty().bind(placeholder.not().and(emptyProperty().not()));

            VBox contentPlaceholder = new VBox();
            contentPlaceholder.getStyleClass().add("placeholder");
            contentPlaceholder.visibleProperty().bind(placeholder);
            contentPlaceholder.managedProperty().bind(placeholder);

            Label label = new Label();
            label.textProperty().bind(textProperty());

            StackPane wrapper = new StackPane(content, contentPlaceholder, label);
            setGraphic(wrapper);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        private final BooleanProperty placeholder = new SimpleBooleanProperty(this, "placeholder", false);

        @Override
        protected void updateItem(Issue item, boolean empty) {
            super.updateItem(item, empty);

            placeholder.set(false);

            getStyleClass().removeAll("todo", "in-progress", "done");

            if (item != null && !empty) {
                if (item == getMultiColumnListView().getPlaceholderFrom()) {
                    placeholder.set(true);
                    setText("From");
                } else if (item == getMultiColumnListView().getPlaceholderTo()) {
                    placeholder.set(true);
                    setText("To");
                } else {
                    setText(item.getTitle());
                    getStyleClass().add(item.getStatus());
                }
            } else {
                setText("");
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
