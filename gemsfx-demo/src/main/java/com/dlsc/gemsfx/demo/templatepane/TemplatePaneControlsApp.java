package com.dlsc.gemsfx.demo.templatepane;

import com.dlsc.gemsfx.templatepane.TemplatePane;
import com.dlsc.gemsfx.templatepane.TemplatePane.Position;
import com.dlsc.gemsfx.templatepane.Tile;
import com.dlsc.gemsfx.templatepane.Visibility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.textfield.CustomTextField;

public class TemplatePaneControlsApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		TemplatePane pane = new TemplatePane();
		fillPane(pane);
		pane.setStyle("-fx-border-color: black; -fx-border-insets: 20px;");
		pane.setPrefSize(1200, 1000);
		pane.getStylesheets().add(TemplatePaneApp.class.getResource("controls-demo.css").toExternalForm());
		Platform.runLater(() -> {
			TemplatePaneControls controls = new TemplatePaneControls(pane);
			controls.show();
		});

		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Tiles Pane");
		primaryStage.sizeToScene();
		primaryStage.centerOnScreen();
		primaryStage.show();
	}

	private void fillPane(TemplatePane pane) {
		createHeader(pane);
		createMenuBar(pane);
		createTreeView(pane);
		createContent(pane);
		createStatusBar(pane);
		createHelpDrawer(pane);
	}

	private void createHelpDrawer(TemplatePane pane) {
		TextArea helpArea = new TextArea();
		helpArea.setText("A help text can be shown here....");
		helpArea.setPrefWidth(150);
		helpArea.setWrapText(true);
		helpArea.setStyle("-fx-background-color: yellow;");
		Tile tile = pane.setNode(Position.CONTENT_RIGHT, helpArea);
		tile.setVisibility(Visibility.minRequiredWidth(1000));
	}

	private void createStatusBar(TemplatePane pane) {
		StatusBar statusBar = new StatusBar();
		pane.setNode(Position.BELOW_FOOTER, statusBar);
	}

	private void createContent(TemplatePane pane) {
		TabPane tabPane = new TabPane();
		Tab tab1 = new Tab("Tab 1");
		Tab tab2 = new Tab("Tab 2");
		Tab tab3 = new Tab("Tab 3");
		tabPane.getTabs().addAll(tab1, tab2, tab3);
		pane.setNode(Position.CONTENT, tabPane);
	}

	private void createTreeView(TemplatePane pane) {
		TreeView<String> treeView = new TreeView<>();
		Tile tile = pane.setNode(Position.LEFT, treeView);
		tile.setVisibility(Visibility.minRequiredWidth(800));
	}

	private void createMenuBar(TemplatePane pane) {
		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		Menu editMenu = new Menu("Edit");
		Menu helpMenu = new Menu("Help");
		menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
		pane.setNode(Position.BELOW_HEADER, menuBar);
	}

	private void createHeader(TemplatePane pane) {
		StackPane stackPane = new StackPane();
		pane.setNode(Position.HEADER, stackPane);
		stackPane.setStyle("-fx-background-color: white");

		ImageView logo = new ImageView(TemplatePaneControlsApp.class.getResource("logo-dlsc.png").toExternalForm());
		StackPane.setAlignment(logo, Pos.CENTER_LEFT);
		stackPane.getChildren().add(logo);

		CustomTextField searchField = new CustomTextField();
		searchField.setMaxWidth(200);
		searchField.setPromptText("Search");
		stackPane.getChildren().add(searchField);
		StackPane.setAlignment(searchField, Pos.TOP_RIGHT);
		StackPane.setMargin(searchField, new Insets(10));
	}

	public static void main(String[] args) {
		launch(args);
	}
}
