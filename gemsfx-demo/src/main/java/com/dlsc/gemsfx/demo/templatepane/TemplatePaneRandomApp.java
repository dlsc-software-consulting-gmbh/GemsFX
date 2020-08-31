package com.dlsc.gemsfx.demo.templatepane;

import com.dlsc.gemsfx.templatepane.TemplatePane;
import com.dlsc.gemsfx.templatepane.TemplatePane.Position;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TemplatePaneRandomApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		TemplatePane pane = new TemplatePane();
		fillPane(pane);
		pane.setStyle("-fx-border-color: black; -fx-border-insets: 20px;");
		pane.setPrefSize(1200, 1000);
		pane.getStylesheets().add(TemplatePaneApp.class.getResource("template.css").toExternalForm());

		Scene scene = new Scene(pane);
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
			label.setPrefHeight(Math.max(10, Math.random() * 100));
			label.setPrefWidth(Math.max(10,  Math.random() * 100));
			pane.setNode(pos, label);

			if (Math.random() > .8) {
				pane.getTile(pos).setVisible(false);
				System.out.println("hiding " + pos);
			}

			switch (pos) {
			case ABOVE_CONTENT:
				break;
			case ABOVE_FOOTER:
				break;
			case ABOVE_HEADER:
				break;
			case ABOVE_SIDES:
				break;
			case BELOW_CONTENT:
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
