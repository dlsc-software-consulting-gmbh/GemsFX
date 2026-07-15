package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.Cascader;
import com.dlsc.gemsfx.CascaderItem;
import com.dlsc.gemsfx.CascaderPath;
import com.dlsc.gemsfx.CascaderView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.StringJoiner;

/**
 * Demonstrates the Cascader and CascaderView controls.
 */
public class CascaderApp extends GemApplication {

    private static final StringConverter<Option> OPTION_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Option option) {
            return option == null ? "" : option.label;
        }

        @Override
        public Option fromString(String string) {
            return null;
        }
    };

    @Override
    public void start(Stage stage) {
        super.start(stage);

        Cascader<Option> cascader = createCascader();
        Label cascaderResult = new Label();
        cascader.selectedPathProperty().addListener((obs, oldPath, newPath) ->
                cascaderResult.setText("Selection: " + pathText(newPath)));
        cascaderResult.setText("Selection: " + pathText(cascader.getSelectedPath()));

        CascaderView<Option> cascaderView = createCascaderView();
        Label viewResult = new Label();
        cascaderView.selectedPathProperty().addListener((obs, oldPath, newPath) ->
                viewResult.setText("View: " + viewText(cascaderView)));
        cascaderView.getCheckedPaths().addListener((ListChangeListener<CascaderPath<Option>>) change ->
                viewResult.setText("View: " + viewText(cascaderView)));
        viewResult.setText("View: " + viewText(cascaderView));

        ComboBox<SelectionMode> modeBox = new ComboBox<>();
        modeBox.getItems().setAll(SelectionMode.SINGLE, SelectionMode.MULTIPLE);
        modeBox.setValue(SelectionMode.SINGLE);
        cascaderView.selectionModeProperty().bind(modeBox.valueProperty());

        CheckBox clearable = new CheckBox("Clearable");
        clearable.selectedProperty().bindBidirectional(cascader.clearableProperty());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            cascader.clearSelection();
            cascaderView.clearSelection();
        });

        Button scenicView = new Button("Dev Tools");
        hideInBrowser(scenicView);
        configureDevToolsButton(scenicView);

        HBox controls = new HBox(10, new Label("View mode:"), modeBox, clearable, clearButton, scenicView);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox popupBox = new VBox(8, new Label("Popup Cascader"), cascader, cascaderResult);
        popupBox.setFillWidth(true);

        VBox viewBox = new VBox(8, new Label("Inline CascaderView"), cascaderView, viewResult);
        VBox.setVgrow(cascaderView, Priority.NEVER);

        VBox root = new VBox(16, popupBox, controls, viewBox);
        root.setPadding(new Insets(24));
        root.setMinSize(620, 430);
        root.setMaxWidth(Region.USE_PREF_SIZE);

        Scene scene = new Scene(root);
        CSSFX.start(scene);

        stage.setTitle("Cascader");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    private Cascader<Option> createCascader() {
        Cascader<Option> cascader = new Cascader<>();
        cascader.setPromptText("Select an office");
        cascader.setClearable(true);
        cascader.setConverter(OPTION_CONVERTER);
        cascader.getRootItems().setAll(sampleItems());
        CascaderItem<Option> berlin = findById(cascader.getRootItems(), "berlin");
        cascader.select(berlin);
        cascader.setMaxWidth(Double.MAX_VALUE);
        return cascader;
    }

    private CascaderView<Option> createCascaderView() {
        CascaderView<Option> view = new CascaderView<>();
        view.setConverter(OPTION_CONVERTER);
        view.getRootItems().setAll(sampleItems());
        view.setMaxWidth(Double.MAX_VALUE);
        return view;
    }

    private static String viewText(CascaderView<Option> view) {
        if (view.getSelectionMode() == SelectionMode.MULTIPLE) {
            if (view.getCheckedPaths().isEmpty()) {
                return "No checked paths";
            }
            StringJoiner joiner = new StringJoiner(", ");
            for (CascaderPath<Option> path : view.getCheckedPaths()) {
                joiner.add(pathText(path));
            }
            return joiner.toString();
        }
        return pathText(view.getSelectedPath());
    }

    private static String pathText(CascaderPath<Option> path) {
        if (path == null) {
            return "None";
        }
        StringJoiner joiner = new StringJoiner(" / ");
        for (CascaderItem<Option> item : path.getItems()) {
            joiner.add(OPTION_CONVERTER.toString(item.getValue()));
        }
        return joiner.toString();
    }

    private static List<CascaderItem<Option>> sampleItems() {
        CascaderItem<Option> europe = item("europe", "Europe");
        CascaderItem<Option> germany = item("germany", "Germany");
        germany.getChildren().setAll(List.of(item("berlin", "Berlin"), item("munich", "Munich")));
        CascaderItem<Option> switzerland = item("switzerland", "Switzerland");
        switzerland.getChildren().setAll(List.of(item("zurich", "Zurich"), item("geneva", "Geneva")));
        europe.getChildren().setAll(List.of(germany, switzerland));

        CascaderItem<Option> southAmerica = item("south-america", "South America");
        CascaderItem<Option> brazil = item("brazil", "Brazil");
        brazil.getChildren().setAll(List.of(item("sao-paulo", "Sao Paulo"), item("rio-de-janeiro", "Rio de Janeiro")));
        CascaderItem<Option> argentina = item("argentina", "Argentina");
        argentina.getChildren().setAll(List.of(item("buenos-aires", "Buenos Aires"), item("cordoba", "Cordoba")));
        southAmerica.getChildren().setAll(List.of(brazil, argentina));

        CascaderItem<Option> asia = item("asia", "Asia");
        CascaderItem<Option> china = item("china", "China");
        china.getChildren().setAll(List.of(
                item("beijing", "Beijing"),
                item("shanghai", "Shanghai"),
                item("guangzhou", "Guangzhou"),
                item("shenzhen", "Shenzhen")));
        CascaderItem<Option> japan = item("japan", "Japan");
        japan.getChildren().setAll(List.of(item("tokyo", "Tokyo"), item("osaka", "Osaka"), item("nagoya", "Nagoya")));
        asia.getChildren().setAll(List.of(china, japan));

        return List.of(europe, southAmerica, asia);
    }

    private static CascaderItem<Option> item(String id, String label) {
        return new CascaderItem<>(new Option(id, label));
    }

    private static CascaderItem<Option> findById(List<CascaderItem<Option>> items, String id) {
        for (CascaderItem<Option> item : items) {
            if (item.getValue().id.equals(id)) {
                return item;
            }
            CascaderItem<Option> found = findById(item.getChildren(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static final class Option {

        private final String id;
        private final String label;

        private Option(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
