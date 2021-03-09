package com.dlsc.gemsfx.incubator.templatepane;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class TemplatePane extends Control {

	public enum Position {
		ABOVE_HEADER, HEADER_LEFT, HEADER, HEADER_RIGHT, BELOW_HEADER,

		LEFT, RIGHT, ABOVE_SIDES, BELOW_SIDES, CONTENT_LEFT, CONTENT_RIGHT, ABOVE_CONTENT, BELOW_CONTENT, CONTENT,

		ABOVE_FOOTER, FOOTER_LEFT, FOOTER_RIGHT, FOOTER, BELOW_FOOTER,

		OVERLAY_TOP, OVERLAY_BOTTOM, OVERLAY_LEFT, OVERLAY_RIGHT;
	}

	public TemplatePane() {
		for (Position pos : Position.values()) {
			Tile tile = new Tile(this, pos);
			tilesMap.put(pos, tile);
		}
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new TemplatePaneSkin(this);
	}

	private final ObservableMap<Position, Tile> tilesMap = FXCollections.observableHashMap();

	public final Tile setNode(Position pos, Node node) {
		Tile tile = tilesMap.get(pos);
		tile.setNode(node);
		getChildren().add(node);
		node.setManaged(false);
		return tile;
	}

	public final Node getNode(Position pos) {
		return tilesMap.get(pos).getNode();
	}

	public final Tile getTile(Position pos) {
		return tilesMap.get(pos);
	}
}
