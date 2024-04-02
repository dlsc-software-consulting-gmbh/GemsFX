package com.dlsc.gemsfx.incubator.templatepane;

import com.dlsc.gemsfx.incubator.templatepane.TemplatePane.Position;
import com.dlsc.gemsfx.incubator.templatepane.Tile.TileTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SkinBase;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class TemplatePaneSkin extends SkinBase<TemplatePane> {

	private Map<Tile, Rectangle2D> currentTileBounds;
	private double contentX;
	private double contentY;
	private double contentWidth;
	private double contentHeight;

	public TemplatePaneSkin(TemplatePane pane) {
		super(pane);

		for (Position pos : Position.values()) {
			Tile tile = pane.getTile(pos);
			tile.visibleProperty().addListener(it -> Platform.runLater(() -> update(tile.getTransition())));
		}
	}

	private Tile getTile(Position pos) {
		return getSkinnable().getTile(pos);
	}

	private void update(TileTransition transition) {
		Map<Tile, Rectangle2D> newTileBounds = computeBounds(contentX, contentY, contentWidth, contentHeight);

		Timeline timeline = new Timeline();

		newTileBounds.forEach((tile, bounds) -> update(tile, bounds, timeline, transition));

		if (!timeline.getKeyFrames().isEmpty()) {
			timeline.setOnFinished(evt -> getSkinnable().requestLayout());
			timeline.play();
		}
	}

	private void update(Tile tile, Rectangle2D newBounds, Timeline timeline, TileTransition transition) {
		Rectangle2D currentBounds = currentTileBounds.get(tile);
		System.out.println("tile = " + tile.getPosition());
		System.out.println("   cur bounds = " + currentBounds);
		System.out.println("   new bounds = " + newBounds);

		if (!currentBounds.equals(newBounds)) {

			KeyValue xValue;
			KeyValue yValue;
			KeyValue widthValue;
			KeyValue heightValue;

			switch (transition) {
			case APPEAR:
				xValue = new KeyValue(tile.layoutXProperty(), newBounds.getMinX());
				yValue = new KeyValue(tile.layoutYProperty(), newBounds.getMinY());
				widthValue = new KeyValue(tile.widthProperty(), newBounds.getWidth());
				heightValue = new KeyValue(tile.heightProperty(), newBounds.getHeight());
				timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, xValue, yValue, widthValue, heightValue));
				break;
			case SHRINK:
				xValue = new KeyValue(tile.layoutXProperty(), newBounds.getMinX());
				yValue = new KeyValue(tile.layoutYProperty(), newBounds.getMinY());
				widthValue = new KeyValue(tile.widthProperty(), newBounds.getWidth());
				heightValue = new KeyValue(tile.heightProperty(), newBounds.getHeight());
				timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300), xValue, yValue, widthValue, heightValue));
				break;
			case SLIDE:
				break;
			}
		}
	}

	@Override
	protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {

		this.contentX = contentX;
		this.contentY = contentY;
		this.contentWidth = contentWidth;
		this.contentHeight = contentHeight;

		System.out.println("creating new tile bounds");
		currentTileBounds = computeBounds(contentX, contentY, contentWidth, contentHeight);
		currentTileBounds.forEach((tile, bounds) -> tile.resize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
	}

	private Map<Tile, Rectangle2D> computeBounds(double contentX, double contentY, double contentWidth, double contentHeight) {
		Map<Tile, Rectangle2D> boundsMap = new HashMap<>();

		// fill from the top
		double aboveHeaderHeight = layoutAboveHeader(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentY += aboveHeaderHeight;
		contentHeight -= aboveHeaderHeight;

		double headerHeight = layoutHeader(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentY += headerHeight;
		contentHeight -= headerHeight;

		double belowHeaderHeight = layoutBelowHeader(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentY += belowHeaderHeight;
		contentHeight -= belowHeaderHeight;

		// fill from the bottom

		double belowFooterHeight = layoutBelowFooter(boundsMap, contentX, contentY + contentHeight, contentWidth, contentHeight);
		contentHeight -= belowFooterHeight;

		double footerHeight = layoutFooter(boundsMap, contentX, contentY + contentHeight, contentWidth, contentHeight);
		contentHeight -= footerHeight;

		double aboveFooterHeight = layoutAboveFooter(boundsMap, contentX, contentY + contentHeight, contentWidth, contentHeight);
		contentHeight -= aboveFooterHeight;

		double leftWidth = layoutLeft(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentX += leftWidth;
		contentWidth -= leftWidth;

		double rightWidth = layoutRight(boundsMap, contentX + contentWidth, contentY, contentWidth, contentHeight);
		contentWidth -= rightWidth;

		double aboveSidesHeight = layoutAboveSides(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentHeight -= aboveSidesHeight;
		contentY += aboveSidesHeight;

		double belowSidesHeight = layoutBelowSides(boundsMap, contentX, contentY + contentHeight, contentWidth, contentHeight);
		contentHeight -= belowSidesHeight;

		double contentLeftWidth = layoutContentLeft(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentX += contentLeftWidth;
		contentWidth -= contentLeftWidth;

		double contentRightWidth = layoutContentRight(boundsMap, contentX + contentWidth, contentY, contentWidth, contentHeight);
		contentWidth -= contentRightWidth;

		double aboveContentHeight = layoutAboveContent(boundsMap, contentX, contentY, contentWidth, contentHeight);
		contentHeight -= aboveContentHeight;
		contentY += aboveContentHeight;

		double belowContentHeight = layoutBelowContent(boundsMap, contentX, contentY + contentHeight, contentWidth, contentHeight);
		contentHeight -= belowContentHeight;

		layoutContent(boundsMap, contentX, contentY, contentWidth, contentHeight);

		return boundsMap;
	}

	private void layoutContent(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.CONTENT);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, contentWidth, contentHeight));
	}

	private double layoutBelowContent(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.BELOW_CONTENT);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY - prefHeight, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutAboveContent(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.ABOVE_CONTENT);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutContentRight(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.CONTENT_RIGHT);
		double prefWidth = tile.prefWidth(contentHeight);
		boundsMap.put(tile, new Rectangle2D(contentX - prefWidth, contentY, prefWidth, contentHeight));
		return prefWidth;
	}

	private double layoutContentLeft(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.CONTENT_LEFT);
		double prefWidth = tile.prefWidth(contentHeight);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, prefWidth, contentHeight));
		return prefWidth;
	}

	private double layoutBelowSides(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.BELOW_SIDES);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY - prefHeight, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutAboveSides(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.ABOVE_SIDES);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutLeft(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.LEFT);
		double prefWidth = tile.prefWidth(contentHeight);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, prefWidth, contentHeight));
		return prefWidth;
	}

	private double layoutRight(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.RIGHT);
		double prefWidth = tile.prefWidth(contentHeight);
		boundsMap.put(tile, new Rectangle2D(contentX - prefWidth, contentY, prefWidth, contentHeight));
		return prefWidth;
	}

	private double layoutAboveFooter(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.ABOVE_FOOTER);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY - prefHeight, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutFooter(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile footerLeft = getTile(Position.FOOTER_LEFT);
		Tile footerRight = getTile(Position.FOOTER_RIGHT);
		Tile footer = getTile(Position.FOOTER);

		double prefWidthFooterLeft = footerLeft.prefWidth(-1);
		double prefWidthFooterRight = footerRight.prefWidth(-1);

		double prefHeightFooterLeft = footerLeft.prefHeight(-1);
		double prefHeightFooterRight = footerRight.prefHeight(-1);
		double prefHeightFooter = footer.prefHeight(-1);

		double prefHeight = 0;

		if (prefHeightFooter > 0) {
			prefHeight = Math.max(prefHeightFooter, Math.max(prefHeightFooterLeft, prefHeightFooterRight));
		}

		boundsMap.put(footerLeft, new Rectangle2D(contentX, contentY - prefHeight, prefWidthFooterLeft, prefHeight));
		boundsMap.put(footerRight, new Rectangle2D(contentX + contentWidth - prefWidthFooterRight, contentY - prefHeight, prefWidthFooterRight, prefHeight));
		boundsMap.put(footer, new Rectangle2D(contentX + prefWidthFooterLeft, contentY - prefHeight, contentWidth - prefWidthFooterLeft - prefWidthFooterRight, prefHeight));

		return prefHeight;
	}

	private double layoutBelowFooter(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.BELOW_FOOTER);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY - prefHeight, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutAboveHeader(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.ABOVE_HEADER);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, contentWidth, prefHeight));
		return prefHeight;
	}

	private double layoutHeader(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile headerLeft = getTile(Position.HEADER_LEFT);
		Tile headerRight = getTile(Position.HEADER_RIGHT);
		Tile header = getTile(Position.HEADER);

		double prefWidthHeaderLeft = headerLeft.prefWidth(-1);
		double prefWidthHeaderRight = headerRight.prefWidth(-1);

		double prefHeightHeaderLeft = headerLeft.prefHeight(-1);
		double prefHeightHeaderRight = headerRight.prefHeight(-1);
		double prefHeightHeader = header.prefHeight(-1);

		double prefHeight = 0;

		if (prefHeightHeader > 0) {
			prefHeight = Math.max(prefHeightHeader, Math.max(prefHeightHeaderLeft, prefHeightHeaderRight));
		}

		boundsMap.put(headerLeft, new Rectangle2D(contentX, contentY, prefWidthHeaderLeft, prefHeight));
		boundsMap.put(headerRight, new Rectangle2D(contentX + contentWidth - prefWidthHeaderRight, contentY, prefWidthHeaderRight, prefHeight));
		boundsMap.put(header, new Rectangle2D(contentX + prefWidthHeaderLeft, contentY, contentWidth - prefWidthHeaderLeft - prefWidthHeaderRight, prefHeight));

		System.out.println("bounds: " + boundsMap.get(header));

		return prefHeight;
	}

	private double layoutBelowHeader(Map<Tile, Rectangle2D> boundsMap, double contentX, double contentY, double contentWidth, double contentHeight) {
		Tile tile = getTile(Position.BELOW_HEADER);
		double prefHeight = tile.prefHeight(contentWidth);
		boundsMap.put(tile, new Rectangle2D(contentX, contentY, contentWidth, prefHeight));
		return prefHeight;
	}
}
