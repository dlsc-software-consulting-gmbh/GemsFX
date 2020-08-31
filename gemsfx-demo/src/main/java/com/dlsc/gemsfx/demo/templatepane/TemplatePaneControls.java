package com.dlsc.gemsfx.demo.templatepane;

import com.dlsc.gemsfx.templatepane.TemplatePane;
import com.dlsc.gemsfx.templatepane.TemplatePane.Position;
import com.dlsc.gemsfx.templatepane.Tile;
import com.dlsc.gemsfx.templatepane.Tile.TileTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class TemplatePaneControls extends BorderPane {

	private TemplatePane controlsPane;
	private TemplatePane pane;
	private CheckBox checkbox;

	public TemplatePaneControls(TemplatePane pane) {
		this.pane = Objects.requireNonNull(pane);

		getStylesheets().add(TemplatePaneApp.class.getResource("controls.css").toExternalForm());

		this.checkbox = new CheckBox("Show all tiles");
		this.checkbox.setSelected(false);
		this.checkbox.setOnAction(evt -> controlsPane.requestLayout());

		this.controlsPane = new TemplatePane();

		BorderPane.setMargin(checkbox, new Insets(5));
		BorderPane.setMargin(controlsPane, new Insets(5));

		setTop(checkbox);
		setCenter(controlsPane);

		fillPane();
		setPrefSize(200, 300);
	}

	private Stage stage;

	public final void show() {
		if (stage == null) {
			stage = new Stage();
			stage.setTitle("Controls");
			stage.initStyle(StageStyle.UTILITY);
			stage.initOwner(pane.getScene().getWindow());
			Scene scene = new Scene(this);
			stage.setScene(scene);
			stage.sizeToScene();
			stage.show();
		}
	}

	private void fillPane() {
		for (Position pos : Position.values()) {
			final ToggleButton button = new ToggleButton(pos.name());
			button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			button.setPrefHeight(20);
			button.setPrefWidth(20);
			button.setStyle("-fx-font-size: 8px");
			button.setSelected(true);

			controlsPane.setNode(pos, button);
			Tile controlsTile = controlsPane.getTile(pos);

			Tile tile = pane.getTile(pos);
			controlsTile.visibleProperty().bind(Bindings.or(Bindings.isNotNull(tile.nodeProperty()), checkbox.selectedProperty()));
			tile.visibleProperty().bind(button.selectedProperty());

			ContextMenu menu = new ContextMenu();
			RadioMenuItem appearTransition = new RadioMenuItem("Appear");
			RadioMenuItem slideTransition = new RadioMenuItem("Slide");
			RadioMenuItem shrinkTransition = new RadioMenuItem("Shrink");

			switch (tile.getTransition()) {
			case APPEAR:
				appearTransition.setSelected(true);
				break;
			case SHRINK:
				shrinkTransition.setSelected(true);
				break;
			case SLIDE:
				slideTransition.setSelected(true);
				break;
			default:
				break;

			}
			ToggleGroup group = new ToggleGroup();
			group.getToggles().addAll(appearTransition, slideTransition, shrinkTransition);

			appearTransition.setOnAction(evt -> tile.setTransition(TileTransition.APPEAR));
			slideTransition.setOnAction(evt -> tile.setTransition(TileTransition.SLIDE));
			shrinkTransition.setOnAction(evt -> tile.setTransition(TileTransition.SHRINK));

			menu.getItems().addAll(appearTransition, slideTransition, shrinkTransition);

			button.setContextMenu(menu);
		}
	}
}
