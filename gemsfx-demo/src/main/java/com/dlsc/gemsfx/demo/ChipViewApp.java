package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.util.EnumStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.List;

/**
 * Demonstrates the {@link ChipView} control. The demo shows a list of "tags", each of them
 * represented by a chip. The chips can be removed, new ones can be added, and the way the chips
 * present their text and their graphic can be changed at runtime.
 */
public class ChipViewApp extends GemApplication {

    /**
     * The model object represented by a chip.
     *
     * @param name the name of the tag
     * @param icon the icon shown by the chip
     */
    public record Tag(String name, MaterialDesign icon) {
    }

    private static final List<Tag> INITIAL_TAGS = List.of(
            new Tag("Java", MaterialDesign.MDI_COFFEE),
            new Tag("JavaFX", MaterialDesign.MDI_LANGUAGE_JAVASCRIPT),
            new Tag("Desktop", MaterialDesign.MDI_DESKTOP_MAC),
            new Tag("Controls", MaterialDesign.MDI_GAMEPAD_VARIANT),
            new Tag("Open Source", MaterialDesign.MDI_GITHUB_CIRCLE),
            new Tag("Design", MaterialDesign.MDI_PALETTE)
    );

    private final ObservableList<Tag> tags = FXCollections.observableArrayList(INITIAL_TAGS);

    private final ObjectProperty<ContentDisplay> contentDisplay = new SimpleObjectProperty<>(ContentDisplay.LEFT);
    private final BooleanProperty closable = new SimpleBooleanProperty(true);
    private final BooleanProperty showGraphic = new SimpleBooleanProperty(true);

    private final Label statusLabel = new Label("Click on the close icon of a chip to remove it.");

    @Override
    public void start(Stage stage) {
        super.start(stage);

        FlowPane chipsPane = new FlowPane();
        chipsPane.setHgap(10);
        chipsPane.setVgap(10);
        chipsPane.setPadding(new Insets(20));
        chipsPane.setMinHeight(160);
        chipsPane.setAlignment(Pos.TOP_LEFT);

        tags.addListener((javafx.beans.Observable it) -> updateChips(chipsPane));
        contentDisplay.addListener(it -> updateChips(chipsPane));
        closable.addListener(it -> updateChips(chipsPane));
        showGraphic.addListener(it -> updateChips(chipsPane));

        updateChips(chipsPane);

        VBox contentBox = new VBox(10, new Label("Tags"), chipsPane, statusLabel);
        contentBox.setPadding(new Insets(20));
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox box = new HBox(20, contentBox, createOptions());
        box.setFillHeight(true);

        Scene scene = new Scene(box);

        stage.setTitle("Chip View Demo");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(500);
        stage.show();
    }

    /**
     * Creates one chip view for each tag. The chips are recreated whenever the settings of the
     * demo change so that the effect of the settings can be seen immediately.
     */
    private void updateChips(FlowPane chipsPane) {
        chipsPane.getChildren().clear();

        for (Tag tag : tags) {
            ChipView<Tag> chipView = new ChipView<>();
            chipView.setValue(tag);
            chipView.setText(tag.name());
            chipView.setContentDisplay(contentDisplay.get());

            if (showGraphic.get()) {
                chipView.setGraphic(new FontIcon(tag.icon()));
            }

            // the close icon of a chip is only shown when a close handler has been set
            if (closable.get()) {
                chipView.setOnClose(value -> {
                    tags.remove(value);
                    statusLabel.setText("Removed tag: " + value.name());
                });
            }

            chipsPane.getChildren().add(chipView);
        }

        if (chipsPane.getChildren().isEmpty()) {
            Label placeholder = new Label("No tags left, use the text field to add a new one.");
            placeholder.setDisable(true);
            chipsPane.getChildren().add(placeholder);
        }
    }

    /**
     * Creates the panel with the settings that can be changed by the user.
     */
    private VBox createOptions() {
        ComboBox<ContentDisplay> contentDisplayBox = new ComboBox<>();
        contentDisplayBox.setConverter(new EnumStringConverter<>());
        contentDisplayBox.getItems().setAll(ContentDisplay.values());
        contentDisplayBox.valueProperty().bindBidirectional(contentDisplay);
        contentDisplayBox.setMaxWidth(Double.MAX_VALUE);

        CheckBox closableBox = new CheckBox("Closable");
        closableBox.selectedProperty().bindBidirectional(closable);

        CheckBox graphicBox = new CheckBox("Show graphic");
        graphicBox.selectedProperty().bindBidirectional(showGraphic);

        TextField tagField = new TextField();
        tagField.setPromptText("New tag ...");

        Button addButton = new Button("Add");
        addButton.setDefaultButton(true);
        addButton.setMinWidth(Region.USE_PREF_SIZE);
        addButton.disableProperty().bind(tagField.textProperty().isEmpty());
        addButton.setOnAction(evt -> {
            tags.add(new Tag(tagField.getText().strip(), MaterialDesign.MDI_TAG));
            statusLabel.setText("Added tag: " + tagField.getText().strip());
            tagField.clear();
        });

        HBox addBox = new HBox(5, tagField, addButton);
        HBox.setHgrow(tagField, Priority.ALWAYS);

        Button resetButton = new Button("Reset tags");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(evt -> {
            tags.setAll(INITIAL_TAGS);
            statusLabel.setText("Restored the initial tags.");
        });

        Button devTools = configureDevToolsButton(new Button());
        devTools.setMaxWidth(Double.MAX_VALUE);

        VBox optionsBox = new VBox(10,
                new Label("Content display"), contentDisplayBox,
                closableBox,
                graphicBox,
                new Separator(),
                new Label("Add a tag"), addBox,
                resetButton,
                devTools);

        createThemeSwitcher().ifPresent(switcher -> optionsBox.getChildren().add(0, switcher));

        optionsBox.setPrefWidth(240);
        optionsBox.setMinWidth(240);
        optionsBox.setPadding(new Insets(20));
        return optionsBox;
    }

    public static void main(String[] args) {
        launch();
    }
}
