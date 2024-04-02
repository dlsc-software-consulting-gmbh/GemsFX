package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.incubator.templatepane.TemplatePane;
import com.dlsc.gemsfx.incubator.templatepane.TemplatePane.Position;
import com.dlsc.gemsfx.incubator.templatepane.TemplatePaneControls;
import com.dlsc.gemsfx.incubator.templatepane.Tile;
import com.dlsc.gemsfx.incubator.templatepane.Visibility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Objects;

public class TemplatePaneApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		TemplatePane pane = new TemplatePane();
		fillPane(pane);
		pane.setStyle("-fx-border-color: black; -fx-border-insets: 20px;");
		pane.setPrefSize(1200, 1000);
		Platform.runLater(() -> {
			TemplatePaneControls controls = new TemplatePaneControls(pane);
			controls.show();
		});
		Scene scene = new Scene(pane);
		pane.getStylesheets().add(Objects.requireNonNull(TemplatePaneApp.class.getResource("template.css")).toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Tiles Pane");
		primaryStage.sizeToScene();
		primaryStage.centerOnScreen();
		primaryStage.show();
	}

	private void fillPane(TemplatePane pane) {
		for (Position pos : Position.values()) {
			Label label = new Label(pos.name());
			label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			label.setPrefHeight(50);
			label.setPrefWidth(110);
			pane.setNode(pos, label);

			Tile tile = pane.getTile(pos);
			//tile.setTransition(new SlideInOutTransition(Direction.RIGHT));
			label.setOnMouseClicked(evt -> {
				System.out.println("setting tile " + tile.getPosition() + " to visible = false");
				tile.setVisible(!tile.isVisible());
			});

			switch (pos) {
			case ABOVE_CONTENT:
				tile.setVisibility(Visibility.minRequiredHeight(800));
				break;
			case ABOVE_FOOTER:
				break;
			case ABOVE_HEADER:
				break;
			case ABOVE_SIDES:
				break;
			case BELOW_CONTENT:
				tile.setVisibility(Visibility.minRequiredHeight(900));
				break;
			case BELOW_FOOTER:
				break;
			case BELOW_HEADER:
				break;
			case BELOW_SIDES:
				break;
			case FOOTER:
				break;
			case FOOTER_LEFT:
				break;
			case FOOTER_RIGHT:
				break;
			case HEADER:
				break;
			case HEADER_LEFT:
				break;
			case HEADER_RIGHT:
				break;
			case CONTENT_LEFT:
				break;
			case LEFT:
				tile.setVisibility(Visibility.minRequiredWidth(1000));
				break;
			case OVERLAY_BOTTOM:
				break;
			case OVERLAY_LEFT:
				break;
			case OVERLAY_RIGHT:
				break;
			case OVERLAY_TOP:
				break;
			case CONTENT_RIGHT:
				break;
			case RIGHT:
				tile.setVisibility(Visibility.minRequiredWidth(1000));
				break;
			default:
				break;

			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
