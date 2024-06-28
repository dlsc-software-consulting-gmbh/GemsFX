package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ColumnBrowserListView;
import com.dlsc.gemsfx.ColumnBrowserTableView;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;

public class ColumnBrowserTableViewApp extends Application {

    @Override
    public void start(Stage stage) {
        TableColumn<Movie, String> titleColumn = new TableColumn<>("Title");
        TableColumn<Movie, String> genreColumn = new TableColumn<>("Genre");
        TableColumn<Movie, String> castColumn = new TableColumn<>("Cast");
        TableColumn<Movie, Integer> yearColumn = new TableColumn<>("Year");

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        castColumn.setCellValueFactory(new PropertyValueFactory<>("cast"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        ColumnBrowserTableView<Movie> columnBrowser = new ColumnBrowserTableView<>();
        List<Movie> movies = parseMoviesFile();
        System.out.println("number of movies: " + movies.size());
        columnBrowser.getItems().setAll(movies);

        TableView<Movie> tableView = columnBrowser.getTableView();
        tableView.getColumns().setAll(List.of(titleColumn, genreColumn, yearColumn, castColumn));

        ColumnBrowserListView<Movie, String> genreList = new ColumnBrowserListView<>("Genre");
        ColumnBrowserListView<Movie, String> castList = new ColumnBrowserListView<>("Cast");
        ColumnBrowserListView<Movie, Integer> yearList = new ColumnBrowserListView<>("Year");

        genreList.getItems().setAll(movies.stream().flatMap(movie -> movie.getGenreList().stream()).distinct().toList());
        castList.getItems().setAll(movies.stream().flatMap(movie -> movie.getCastList().stream()).distinct().filter(removeMoviesWithBadCastData()).toList());
        yearList.getItems().setAll(movies.stream().map(Movie::getYear).distinct().toList());

        genreList.setPredicate(movie -> {
            ObservableList<String> selectedItems = genreList.getListView().getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                return true;
            }
            for (String selectedItem : selectedItems) {
                if (movie.getGenreList().contains(selectedItem)) {
                    return true;
                }
            }
            return false;
        });

        castList.setPredicate(movie -> {
            ObservableList<String> selectedItems = castList.getListView().getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                return true;
            }
            for (String selectedItem : selectedItems) {
                if (movie.getCastList().contains(selectedItem)) {
                    return true;
                }
            }
            return false;
        });

        yearList.setPredicate(movie -> {
            ObservableList<Integer> selectedItems = yearList.getListView().getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                return true;
            }

            return selectedItems.contains(movie.getYear());
        });

        columnBrowser.getColumnValuesLists().setAll(List.of(genreList, castList, yearList));
        columnBrowser.getItems().setAll(parseMoviesFile());

        VBox.setVgrow(columnBrowser, Priority.ALWAYS);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setPreserveRatio(true);
        imageView.visibleProperty().bind(imageView.imageProperty().isNotNull());
        imageView.managedProperty().bind(imageView.imageProperty().isNotNull());

        Label descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);

        tableView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Movie movie = tableView.getSelectionModel().getSelectedItem();
            imageView.setImage(null);
            descriptionLabel.setText("");
            if (movie != null) {
                descriptionLabel.setText(movie.getExtract());
                String thumbnail = movie.getThumbnail();
                if (StringUtils.isNotBlank(thumbnail)) {
                    imageView.setImage(new Image(thumbnail, true));
                }
            }
        });

        VBox detailsBox = new VBox(20, imageView, descriptionLabel);
        detailsBox.setAlignment(Pos.TOP_CENTER);
        detailsBox.setStyle("-fx-background-color: white; -fx-padding: 20px;");
        detailsBox.setPrefWidth(300);

        HBox hBox = new HBox(columnBrowser, detailsBox);
        HBox.setHgrow(columnBrowser, Priority.ALWAYS);

        Label copyrightLabel = new Label();
        copyrightLabel.setMaxWidth(Double.MAX_VALUE);
        copyrightLabel.setText("Movie data source: https://github.com/prust/wikipedia-movie-data");
        copyrightLabel.setPadding(new Insets(10, 20, 10, 20));
        copyrightLabel.setStyle("-fx-background-color: beige; -fx-border-color: black;");

        VBox.setVgrow(hBox, Priority.ALWAYS);
        VBox wrapper = new VBox(hBox, copyrightLabel);

        StackPane stackPane = new StackPane(wrapper);

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        stage.setTitle("Column Browser");
        stage.setScene(scene);
        stage.centerOnScreen();

        StageManager.install(stage, Preferences.userNodeForPackage(getClass()).node("column-browser-app14"), 1000, 850);

        stage.show();

        Platform.runLater(() -> tableView.getSelectionModel().select(0));
    }

    private static Predicate<String> removeMoviesWithBadCastData() {
        return g -> !(g.startsWith("(") || g.endsWith(")") || g.startsWith(".") || g.startsWith("1972") ||
                g.startsWith("\"") || g.startsWith("'") || g.startsWith("&"));
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

        public List<String> getCastList() {
            return cast;
        }

        public String getGenre() {
            return String.join(", ", genres);
        }

        public List<String> getGenreList() {
            return genres;
        }
    }

    private List<Movie> parseMoviesFile() {
        List<Movie> movies = new ArrayList<>();
        movies.addAll(parseMovieFile("movies-1970s.json"));
        movies.addAll(parseMovieFile("movies-1980s.json"));
        movies.addAll(parseMovieFile("movies-1990s.json"));
        movies.addAll(parseMovieFile("movies-2000s.json"));
        movies.addAll(parseMovieFile("movies-2010s.json"));
        movies.addAll(parseMovieFile("movies-2020s.json"));
        movies.removeIf(movie -> StringUtils.isEmpty(movie.getThumbnail()));
        return movies;
    }

    private List<Movie> parseMovieFile(String name) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(ColumnBrowserTableViewApp.class.getResource(name), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
