package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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

        col1.getItems().setAll(new Issue("Dirk"), new Issue("Katja"), new Issue("Philip"));
        col2.getItems().setAll(new Issue("Jule"), new Issue("Franz"), new Issue("Paul"), new Issue("Orange"));
        col3.getItems().setAll(new Issue("Armin"));

        return List.of(col1, col2, col3);
    }

    public class Issue {

        private String title;

        public Issue(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public class IssueListCell extends ColumnListCell<Issue> {

        public IssueListCell(MultiColumnListView<Issue> multiColumnListView) {
            super(multiColumnListView);
        }

        @Override
        protected void updateItem(Issue item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                setText(item.getTitle());
            } else {
                setText("");
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
