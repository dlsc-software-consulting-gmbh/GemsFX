package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.incubator.columnbrowser.ColumnBrowser;
import com.dlsc.gemsfx.incubator.columnbrowser.ColumnValuesList;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class ColumnBrowserApp extends Application {

    @Override
    public void start(Stage stage) {
        parseMoviesFile();

        TableView<Movie> tableView = new TableView<>();

        TableColumn<Movie, String> titleColumn = new TableColumn<>("Title");
        TableColumn<Movie, String> genreColumn = new TableColumn<>("Genre");
        TableColumn<Movie, String> castColumn = new TableColumn<>("Cast");
        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Year");

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        castColumn.setCellValueFactory(new PropertyValueFactory<>("cast"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        tableView.getColumns().setAll(List.of(titleColumn, genreColumn, castColumn, yearColumn));
        ColumnBrowser<Movie> columnBrowser = new ColumnBrowser<>(tableView);

        ColumnValuesList<Movie, String> titleList = new ColumnValuesList<>(columnBrowser, titleColumn);
        ColumnValuesList<Movie, String> genreList = new ColumnValuesList<>(columnBrowser, genreColumn);
        ColumnValuesList<Movie, String> castList = new ColumnValuesList<>(columnBrowser, castColumn);
        ColumnValuesList<Movie, Integer> yearList = new ColumnValuesList<>(columnBrowser, yearColumn);

        columnBrowser.getColumnValuesLists().setAll(List.of(titleList, genreList, castList,yearList));
        columnBrowser.getItems().setAll(parseMoviesFile());

        VBox box = new VBox(10, columnBrowser, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        StackPane stackPane = new StackPane(box);
        stackPane.setPadding(new Insets(20, 20, 20, 20));

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        stage.setTitle("Column Browser");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class Movie {
        public String title;
        public int year;
        public List<String> cast;
        public List<String> genres;
        public String href;
        public String extract;
        public String thumbnail;
        public int thumbnail_width;
        public int thumbnail_height;

        @Override
        public String toString() {
            return "Movie{" +
                    "title='" + title + '\'' +
                    ", year=" + year +
                    ", cast=" + cast +
                    ", genres=" + genres +
                    ", href='" + href + '\'' +
                    ", extract='" + extract + '\'' +
                    ", thumbnail='" + thumbnail + '\'' +
                    ", thumbnail_width=" + thumbnail_width +
                    ", thumbnail_height=" + thumbnail_height +
                    '}';
        }

        public String getTitle() {
            return title;
        }

        public int getYear() {
            return year;
        }

        public String getHref() {
            return href;
        }

        public String getExtract() {
            return extract;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public int getThumbnailWidth() {
            return thumbnail_width;
        }

        public int getThumbnailHeight() {
            return thumbnail_height;
        }

        public String getCast() {
            return String.join(", ", cast);
        }

        public String getGenre() {
            return String.join(", ", genres);
        }
    }

    private List<Movie> parseMoviesFile() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(ColumnBrowserApp.class.getResource("movies-2020s.txt"), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
