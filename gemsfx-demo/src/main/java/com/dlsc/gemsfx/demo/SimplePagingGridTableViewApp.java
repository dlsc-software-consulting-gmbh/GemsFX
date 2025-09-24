package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.paging.SimplePagingGridTableView;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.util.StageManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.scenicview.ScenicView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimplePagingGridTableViewApp extends GemApplication {

    private final BooleanProperty simulateNoData = new SimpleBooleanProperty(false);

    private final IntegerProperty count = new SimpleIntegerProperty(55);

    @Override
    public void start(Stage stage) { super.start(stage);
        List<Movie> movies = parseMovieFiles();

        SimplePagingGridTableView<Movie> pagingGridTableView = new SimplePagingGridTableView<>();
        pagingGridTableView.setPrefWidth(800);
        pagingGridTableView.setPageSize(10);
        pagingGridTableView.getItems().setAll(movies);

        GridTableColumn<Movie, Integer> indexColumn = new GridTableColumn<>("#");
        GridTableColumn<Movie, String> titleColumn = new GridTableColumn<>("Title");
        GridTableColumn<Movie, String> genreColumn = new GridTableColumn<>("Genre");
        GridTableColumn<Movie, String> castColumn = new GridTableColumn<>("Cast");
        GridTableColumn<Movie, Integer> yearColumn = new GridTableColumn<>("Year");

        indexColumn.setCellValueFactory(movie -> movies.indexOf(movie) + 1);
        titleColumn.setCellValueFactory(Movie::getTitle);
        genreColumn.setCellValueFactory(Movie::getGenre);
        castColumn.setCellValueFactory(Movie::getCast);
        yearColumn.setCellValueFactory(Movie::getYear);

        indexColumn.setMinWidth(50);
        indexColumn.setPrefWidth(50);
        indexColumn.setMaxWidth(50);
        castColumn.setMaxWidth(500);

        pagingGridTableView.getColumns().setAll(List.of(indexColumn, titleColumn, genreColumn, castColumn, yearColumn));

        simulateNoData.addListener(it -> pagingGridTableView.reload());

        Button scenicView = new Button("Scenic View");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        CheckBox fillBox = new CheckBox("Fill last page");
        fillBox.selectedProperty().bindBidirectional(pagingGridTableView.fillLastPageProperty());

        CheckBox simulateDelay = new CheckBox("Simulate delay");
        pagingGridTableView.getLoadingService().loadDelayInMillisProperty().bind(Bindings.createLongBinding(() -> simulateDelay.isSelected() ? 3000L : 0L, simulateDelay.selectedProperty()));

        CheckBox showPagingControls = new CheckBox("Show paging controls");
        showPagingControls.selectedProperty().bindBidirectional(pagingGridTableView.showPagingControlsProperty());

        ComboBox<Side> location = new ComboBox<>();
        location.getItems().addAll(Side.TOP, Side.BOTTOM);
        location.valueProperty().bindBidirectional(pagingGridTableView.pagingControlsLocationProperty());

        Button clearSetData = new Button("Clear Set Data");
        clearSetData.setOnAction(evt -> movies.clear());

        Button reduceItemCount = new Button("Reduce Count");
        reduceItemCount.setOnAction(evt -> count.set(count.get() - 1));

        Button increaseItemCount = new Button("Increase Count");
        increaseItemCount.setOnAction(evt -> count.set(count.get() + 1));

        HBox settingsBox = new HBox(10, fillBox, simulateDelay, showPagingControls, location, clearSetData, reduceItemCount, increaseItemCount);
        settingsBox.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(20, pagingGridTableView, settingsBox, new PagingControlsSettingsView(pagingGridTableView), scenicView);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);

        scene.focusOwnerProperty().addListener(it -> System.out.println(scene.getFocusOwner()));

        stage.setTitle("Simple Paging Grid Table View");
        stage.setScene(scene);
        stage.centerOnScreen();

        StageManager.install(stage, "movie.table.view");
        stage.show();

        Platform.runLater(stage::sizeToScene);
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

    private List<Movie> parseMovieFiles() {
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
            return objectMapper.readValue(SimplePagingGridTableViewApp.class.getResource(name), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static void main(String[] args) {
        launch();
    }
}
