package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CircleProgressIndicator;
import com.dlsc.gemsfx.BeforeAfterView;
import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.GlassPane;
import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.MaskedView;
import com.dlsc.gemsfx.PopOver;
import com.dlsc.gemsfx.SegmentedBar;
import com.dlsc.gemsfx.SemiCircleProgressIndicator;
import com.dlsc.gemsfx.StretchingTilePane;
import com.dlsc.gemsfx.TextView;
import com.dlsc.gemsfx.ThreeItemsPane;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.gridtable.GridTableView;
import com.dlsc.gemsfx.paging.PagingControlBase;
import com.dlsc.gemsfx.paging.PagingControls;
import com.dlsc.gemsfx.paging.PagingGridTableView;
import com.dlsc.gemsfx.paging.PagingListView;
import com.dlsc.gemsfx.paging.PagingLoadResponse;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Generates PNG screenshots for GemsFX controls that are missing preview images
 * on the documentation website. Screenshots are saved to {@code docs/screenshots/}.
 *
 * <p>Run with:
 * <pre>
 *   mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ScreenshotGenerator
 * </pre>
 */
public class ScreenshotGenerator extends Application {

    private static final Path OUTPUT_DIR = Path.of("../docs/screenshots");

    /**
     * A task that sets up a scene on the stage and calls {@code done.run()} when finished.
     * The {@code done} runnable must always be called exactly once.
     */
    record Task(String filename, BiConsumer<Stage, Runnable> handler) {}

    private final List<Task> tasks = new ArrayList<>();
    private int idx = 0;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        Files.createDirectories(OUTPUT_DIR);

        setupTasks();

        stage.setX(50);
        stage.setY(50);

        System.out.println("Generating " + tasks.size() + " screenshots…");
        processNext();
    }

    private void setupTasks() {
        addSimpleTask("arc-progress-indicator.png", 320, 320, this::createArcProgressIndicator, 0.2);
        addSimpleTask("semi-circle-progress-indicator.png", 320, 260, this::createSemiCircleProgressIndicator, 0.2);
        addSimpleTask("stretching-tile-pane.png", 700, 340, this::createStretchingTilePane, 0.2);
        addSimpleTask("three-items-pane.png", 700, 120, this::createThreeItemsPane, 0.2);
        addSimpleTask("glass-pane.png", 480, 340, this::createGlassPane, 0.2);
        addSimpleTask("loading-pane.png", 480, 300, this::createLoadingPane, 0.2);
        addSimpleTask("grid-table-view.png", 700, 360, this::createGridTableView, 0.2);
        addSimpleTask("paging-controls.png", 700, 200, this::createPagingControls, 0.2);
        addSimpleTask("paging-list-view.png", 500, 380, this::createPagingListView, 1.0);
        addSimpleTask("paging-grid-table-view.png", 750, 400, this::createPagingGridTableView, 1.0);
        addSimpleTask("before-after-view.png", 700, 420, this::createBeforeAfterView, 0.3);
        addSimpleTask("chip-view.png", 600, 160, this::createChipViews, 0.2);
        addSimpleTask("masked-view.png", 500, 120, this::createMaskedView, 0.2);
        addSimpleTask("segmented-bar.png", 700, 220, this::createSegmentedBar, 0.2);
        addSimpleTask("text-view.png", 560, 340, this::createTextView, 0.2);
        tasks.add(new Task("pop-over.png", this::handlePopOverTask));
    }

    /** Convenience: adds a task that snapshots a Node after a pause. */
    private void addSimpleTask(String filename, int width, int height,
                               java.util.function.Supplier<Node> nodeSupplier,
                               double pauseSeconds) {
        tasks.add(new Task(filename, (stage, done) -> {
            Node content = nodeSupplier.get();
            StackPane root = new StackPane(content);
            root.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 16;");
            StackPane.setAlignment(content, Pos.CENTER);

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle("Generating: " + filename);
            stage.show();

            PauseTransition pause = new PauseTransition(Duration.seconds(pauseSeconds));
            pause.setOnFinished(e -> {
                snapshot(scene, filename);
                done.run();
            });
            pause.play();
        }));
    }

    private void processNext() {
        if (idx >= tasks.size()) {
            System.out.println("\nDone! Screenshots saved to: " + OUTPUT_DIR.toAbsolutePath());
            Platform.exit();
            return;
        }
        Task task = tasks.get(idx++);
        task.handler().accept(primaryStage, this::processNext);
    }

    private void snapshot(Scene scene, String filename) {
        WritableImage image = scene.snapshot(null);
        File outFile = OUTPUT_DIR.resolve(filename).toFile();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", outFile);
            System.out.println("  ✓ " + filename);
        } catch (IOException ex) {
            System.err.println("  ✗ Failed to save " + filename + ": " + ex.getMessage());
        }
    }

    // ---- Special task handlers ----

    private void handlePopOverTask(Stage stage, Runnable done) {
        Button trigger = new Button("Show Calendar ▾");
        trigger.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-padding: 8 16; -fx-font-size: 13;");

        StackPane root = new StackPane(trigger);
        root.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 460, 340);
        stage.setScene(scene);
        stage.setTitle("Generating: pop-over.png");
        stage.show();

        Platform.runLater(() -> {
            PopOver.CalendarPopOver popOver = new PopOver.CalendarPopOver();
            popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            popOver.setArrowSize(10);
            popOver.setCornerRadius(8);
            popOver.show(trigger);

            PauseTransition wait = new PauseTransition(Duration.millis(400));
            wait.setOnFinished(ev -> {
                if (popOver.getScene() != null) {
                    WritableImage img = popOver.getScene().snapshot(null);
                    File outFile = OUTPUT_DIR.resolve("pop-over.png").toFile();
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", outFile);
                        System.out.println("  ✓ pop-over.png");
                    } catch (IOException ex) {
                        System.err.println("  ✗ Failed to save pop-over.png: " + ex.getMessage());
                    }
                } else {
                    System.err.println("  ✗ pop-over.png: PopOver scene not available");
                }
                popOver.hide();
                done.run();
            });
            wait.play();
        });
    }

    // ---- Node factories ----

    private Node createArcProgressIndicator() {
        CircleProgressIndicator indicator = new CircleProgressIndicator();
        indicator.setProgress(0.68);
        indicator.setPrefSize(220, 220);
        return indicator;
    }

    private Node createSemiCircleProgressIndicator() {
        SemiCircleProgressIndicator indicator = new SemiCircleProgressIndicator();
        indicator.setProgress(0.68);
        indicator.setPrefSize(240, 180);
        return indicator;
    }

    private Node createStretchingTilePane() {
        StretchingTilePane pane = new StretchingTilePane(10, 10);
        pane.setPadding(new Insets(10));
        String[] colors = {"#4f46e5", "#0891b2", "#059669", "#d97706",
                "#dc2626", "#7c3aed", "#0d9488", "#b45309"};
        for (int i = 0; i < 8; i++) {
            Label label = new Label("Tile " + (i + 1));
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(140, 70);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setStyle("-fx-background-color: " + colors[i] + "; -fx-text-fill: white; "
                    + "-fx-font-size: 14; -fx-background-radius: 6;");
            pane.getChildren().add(label);
        }
        pane.setPrefSize(660, 280);
        return pane;
    }

    private Node createThreeItemsPane() {
        ThreeItemsPane pane = new ThreeItemsPane();
        pane.setStyle("-fx-background-color: white; -fx-padding: 8px; "
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 4;");
        pane.setSpacing(10);

        Label left = new Label("☰  Application Title");
        left.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label center = new Label("Dashboard");
        center.setStyle("-fx-font-size: 14; -fx-text-fill: #6366f1;");

        HBox right = new HBox(8,
                styledButton("Notifications", "#6366f1"),
                styledButton("Profile", "#0891b2"));
        right.setAlignment(Pos.CENTER_RIGHT);

        pane.setItem1(left);
        pane.setItem2(center);
        pane.setItem3(right);
        pane.setPrefWidth(660);
        return pane;
    }

    private Node createGlassPane() {
        VBox content = new VBox(14,
                titled("User Settings"),
                row("Name", "Jane Doe"),
                row("Email", "jane@example.com"),
                row("Role", "Administrator"),
                styledButton("Save Changes", "#6366f1"));
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        content.setPrefSize(420, 260);

        GlassPane glassPane = new GlassPane();
        glassPane.setHide(false);
        glassPane.setFadeInOut(false);

        StackPane stack = new StackPane(content, glassPane);
        stack.setPrefSize(420, 260);
        return stack;
    }

    private Node createLoadingPane() {
        Label contentLabel = new Label("Content goes here…");
        contentLabel.setAlignment(Pos.CENTER);
        contentLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888;");

        LoadingPane loadingPane = new LoadingPane(contentLabel);
        loadingPane.setStatus(LoadingPane.Status.LOADING);
        loadingPane.setPrefSize(420, 240);
        loadingPane.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 6;");
        return loadingPane;
    }

    private Node createGridTableView() {
        record Row(String name, int age, String city, String role) {}

        List<Row> rows = List.of(
                new Row("Alice Johnson", 29, "New York", "Engineer"),
                new Row("Bob Smith", 34, "Chicago", "Manager"),
                new Row("Carol White", 27, "San Francisco", "Designer"),
                new Row("David Lee", 42, "Boston", "Director"),
                new Row("Eva Martinez", 31, "Austin", "Developer")
        );

        GridTableView<Row> table = new GridTableView<>();
        table.setMinNumberOfRows(5);

        GridTableColumn<Row, String> nameCol = new GridTableColumn<>("Name");
        nameCol.setCellValueFactory(Row::name);
        nameCol.setPercentWidth(30);

        GridTableColumn<Row, Integer> ageCol = new GridTableColumn<>("Age");
        ageCol.setCellValueFactory(Row::age);
        ageCol.setPercentWidth(15);

        GridTableColumn<Row, String> cityCol = new GridTableColumn<>("City");
        cityCol.setCellValueFactory(Row::city);
        cityCol.setPercentWidth(25);

        GridTableColumn<Row, String> roleCol = new GridTableColumn<>("Role");
        roleCol.setCellValueFactory(Row::role);
        roleCol.setPercentWidth(30);

        table.getColumns().addAll(nameCol, ageCol, cityCol, roleCol);
        table.getItems().setAll(rows);
        table.setPrefSize(660, 300);
        return table;
    }

    private Node createPagingControls() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        int[][] configs = {{100, 10, 0}, {150, 15, 2}, {200, 20, 1}};
        for (int[] cfg : configs) {
            PagingControls controls = new PagingControls();
            controls.setTotalItemCount(cfg[0]);
            controls.setPageSize(cfg[1]);
            controls.setPage(cfg[2]);
            controls.setAlignment(HPos.CENTER);
            controls.setMaxWidth(Double.MAX_VALUE);
            controls.setStyle("-fx-background-color: white; -fx-padding: 6; "
                    + "-fx-border-color: #e0e0e0; -fx-border-radius: 4;");
            box.getChildren().add(controls);
        }
        box.setPrefWidth(660);
        return box;
    }

    private Node createPagingListView() {
        PagingListView<String> listView = new PagingListView<>();
        listView.setPageSize(6);
        listView.setMessageLabelStrategy(PagingControlBase.MessageLabelStrategy.SHOW_WHEN_NEEDED);
        listView.setLoader(req -> {
            List<String> data = new ArrayList<>();
            int offset = req.getPage() * req.getPageSize();
            for (int i = 0; i < req.getPageSize(); i++) {
                data.add("List Item " + (offset + i + 1));
            }
            return new PagingLoadResponse<>(data, 30);
        });
        listView.setPrefSize(460, 320);
        return listView;
    }

    private Node createPagingGridTableView() {
        record Person(String name, String dept, int year) {}

        List<Person> people = List.of(
                new Person("Alice Johnson", "Engineering", 2019),
                new Person("Bob Smith", "Marketing", 2021),
                new Person("Carol White", "Design", 2020),
                new Person("David Lee", "Engineering", 2018),
                new Person("Eva Martinez", "HR", 2022),
                new Person("Frank Brown", "Engineering", 2017),
                new Person("Grace Kim", "Finance", 2023),
                new Person("Hank Wilson", "Marketing", 2016)
        );

        PagingGridTableView<Person> table = new PagingGridTableView<>();
        table.setPageSize(5);
        table.setMessageLabelStrategy(PagingControlBase.MessageLabelStrategy.SHOW_WHEN_NEEDED);
        table.setLoader(req -> {
            int offset = req.getPage() * req.getPageSize();
            List<Person> page = people.subList(offset,
                    Math.min(people.size(), offset + req.getPageSize()));
            return new PagingLoadResponse<>(page, people.size());
        });

        GridTableColumn<Person, String> nameCol = new GridTableColumn<>("Name");
        nameCol.setCellValueFactory(Person::name);
        nameCol.setPercentWidth(36);

        GridTableColumn<Person, String> deptCol = new GridTableColumn<>("Department");
        deptCol.setCellValueFactory(Person::dept);
        deptCol.setPercentWidth(36);

        GridTableColumn<Person, Integer> yearCol = new GridTableColumn<>("Since");
        yearCol.setCellValueFactory(Person::year);
        yearCol.setPercentWidth(28);

        table.getColumns().addAll(nameCol, deptCol, yearCol);
        table.setPrefSize(700, 340);
        return table;
    }

    private Node createBeforeAfterView() {
        Image before = new Image(Objects.requireNonNull(
                BeforeAfterViewApp.class.getResource("berlin/before1.png")).toExternalForm());
        Image after = new Image(Objects.requireNonNull(
                BeforeAfterViewApp.class.getResource("berlin/after1.png")).toExternalForm());

        BeforeAfterView view = new BeforeAfterView(before, after);
        view.setPrefSize(660, 380);
        return view;
    }

    private Node createChipViews() {
        FlowPane flow = new FlowPane(10, 10);
        flow.setPadding(new Insets(12));
        flow.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        flow.setPrefWidth(560);

        String[] labels = {"JavaFX", "Java 24", "Controls", "CSS", "FXML",
                "Responsive", "Dark Theme", "Open Source"};
        String[] colors = {"#6366f1", "#0891b2", "#059669", "#d97706",
                "#dc2626", "#7c3aed", "#0d9488", "#b45309"};
        for (int i = 0; i < labels.length; i++) {
            ChipView<String> chip = new ChipView<>();
            chip.setValue(labels[i]);
            chip.setText(labels[i]);
            flow.getChildren().add(chip);
        }
        return flow;
    }

    private Node createMaskedView() {
        HBox hbox = new HBox(8);
        hbox.setPadding(new Insets(12));
        String[] colors = {"#6366f1", "#0891b2", "#059669", "#d97706",
                "#dc2626", "#7c3aed", "#0d9488", "#b45309"};
        for (int i = 0; i < 8; i++) {
            Label lbl = new Label("  Item " + (i + 1) + "  ");
            lbl.setStyle("-fx-background-color: " + colors[i] + "; -fx-text-fill: white; "
                    + "-fx-padding: 8 16; -fx-background-radius: 4; -fx-font-size: 13;");
            hbox.getChildren().add(lbl);
        }

        MaskedView maskedView = new MaskedView(hbox);
        maskedView.setPrefSize(460, 60);
        return maskedView;
    }

    private static class ColoredSegment extends SegmentedBar.Segment {
        final String color;
        ColoredSegment(double value, String text, String color) {
            super(value, text);
            this.color = color;
        }
    }

    private static SegmentedBar<ColoredSegment> coloredSegmentBar(ColoredSegment... segs) {
        SegmentedBar<ColoredSegment> bar = new SegmentedBar<>();
        bar.setSegmentViewFactory(seg -> {
            javafx.scene.layout.Region r = new javafx.scene.layout.Region();
            r.setStyle("-fx-background-color: " + seg.color + ";");
            return r;
        });
        bar.getSegments().addAll(segs);
        bar.setPrefHeight(36);
        bar.setMaxWidth(Double.MAX_VALUE);
        return bar;
    }

    private Node createSegmentedBar() {
        SegmentedBar<ColoredSegment> bar1 = coloredSegmentBar(
                new ColoredSegment(14, "Photos", "#7c3aed"),
                new ColoredSegment(32, "Video", "#0891b2"),
                new ColoredSegment(9,  "Apps",  "#d97706"),
                new ColoredSegment(40, "Music", "#6366f1"),
                new ColoredSegment(5,  "Other", "#059669"),
                new ColoredSegment(35, "Free",  "#94a3b8")
        );

        SegmentedBar<ColoredSegment> bar2 = coloredSegmentBar(
                new ColoredSegment(72, "Completed",   "#059669"),
                new ColoredSegment(18, "In Progress", "#d97706"),
                new ColoredSegment(5,  "Blocked",     "#dc2626"),
                new ColoredSegment(5,  "Pending",     "#94a3b8")
        );

        Label lbl1 = new Label("Disk Usage");
        lbl1.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        Label lbl2 = new Label("Task Progress");
        lbl2.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        VBox box = new VBox(8, lbl1, bar1, lbl2, bar2);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        box.setPrefWidth(660);
        return box;
    }

    private Node createTextView() {
        TextView tv = new TextView();
        tv.setText("GemsFX provides a rich collection of custom JavaFX controls that are "
                + "fully styleable via CSS.\n\n"
                + "The library includes date/time pickers, search fields, paging views, "
                + "progress indicators, and many more controls designed for modern JavaFX applications.\n\n"
                + "All controls follow the standard JavaFX Control/Skin/CSS pattern and integrate "
                + "seamlessly with existing JavaFX applications.");
        tv.setPrefSize(520, 260);
        tv.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 6;");
        return tv;
    }

    // ---- Helpers ----

    private Button styledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-background-radius: 4; -fx-padding: 6 14;");
        return btn;
    }

    private Label titled(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        return lbl;
    }

    private HBox row(String label, String value) {
        Label k = new Label(label + ":");
        k.setStyle("-fx-text-fill: #888; -fx-min-width: 80;");
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold;");
        HBox hbox = new HBox(8, k, v);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return hbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
