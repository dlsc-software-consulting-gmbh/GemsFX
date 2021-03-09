package com.dlsc.gemsfx.incubator.templatepane;

import com.dlsc.gemsfx.incubator.templatepane.TemplatePane.Position;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Tile {

	private final InvalidationListener resizeListener = it -> resizeNode();

	private final Position position;

	private final Rectangle clip;

	private TemplatePane templatePane;

	private VisibilityPolicy visibility;

	private TileTransition transition = TileTransition.SHRINK;

	public enum TileTransition {
		APPEAR, SHRINK, SLIDE;
	}

	public Tile(TemplatePane templatePane, Position position) {
		this.templatePane = Objects.requireNonNull(templatePane);
		this.position = Objects.requireNonNull(position);

		layoutX.addListener(resizeListener);
		layoutY.addListener(resizeListener);
		width.addListener(resizeListener);
		height.addListener(resizeListener);

		clip = new Rectangle();
		clip.widthProperty().bind(width);
		clip.heightProperty().bind(height);

		nodeProperty().addListener(it -> {
			Node node = getNode();
			if (node != null) {
				for (Position p : Position.values()) {
					node.getStyleClass().removeAll("tile-node", createStyleName(p));
				}

				node.getStyleClass().addAll("tile-node", createStyleName(getPosition()));
				node.setClip(clip);
			}
		});

		//visible.addListener(it -> executeTransition());
	}

	private final DoubleProperty xOffset = new SimpleDoubleProperty(this, "xOffset", 0);

	public final DoubleProperty xOffsetProperty() {
		return this.xOffset;
	}

	public final double getXOffset() {
		return this.xOffsetProperty().get();
	}

	public final void setXOffset(final double xOffset) {
		this.xOffsetProperty().set(xOffset);
	}

	private final DoubleProperty yOffset = new SimpleDoubleProperty(this, "yOffset", 0);

	public final DoubleProperty yOffsetProperty() {
		return this.yOffset;
	}

	public final double getYOffset() {
		return this.yOffsetProperty().get();
	}

	public final void setYOffset(final double yOffset) {
		this.yOffsetProperty().set(yOffset);
	}

	public final TemplatePane getTemplatePane() {
		return templatePane;
	}

	public final void setTransition(TileTransition tileTransition) {
		this.transition = tileTransition;
	}

	public final TileTransition getTransition() {
		return transition;
	}

	public final void setVisibility(VisibilityPolicy visibilityPolicy) {
		this.visibility = visibilityPolicy;
	}

	public final VisibilityPolicy getVisibility() {
		return visibility;
	}

	private String createStyleName(Position p) {
		return p.name().toLowerCase().replace('_', '-');
	}

	public final Position getPosition() {
		return position;
	}

	private void resizeNode() {
		if (!resizingTile) {
			Node node = getNode();
			if (node != null) {
				node.resizeRelocate(getLayoutX(), getLayoutY(), getWidth(), getHeight());
			}
		}
	}

	private boolean resizingTile;

	public void resize(double x, double y, double w, double h) {
		resizingTile = true;
		try {
			setLayoutX(x);
			setLayoutY(y);
			setWidth(w);
			setHeight(h);
		} finally {
			resizingTile = false;
			resizeNode();
		}
	}

	public double prefHeight(double width) {
		if (isVisible() && isTileVisible()) {
			Node node = getNode();
			if (node != null) {
				return node.prefHeight(width);
			}
		}

		return 0;
	}

	public double prefWidth(double height) {
		if (isVisible() && isTileVisible()) {
			Node node = getNode();
			if (node != null) {
				return node.prefWidth(height);
			}
		}

		return 0;
	}

	private boolean isTileVisible() {
		if (visibility != null) {
			return visibility.isTileVisible(this);
		}

		return true;
	}

	private final ObjectProperty<Node> node = new SimpleObjectProperty<>(this, "node");

	public final ObjectProperty<Node> nodeProperty() {
		return node;
	}

	public final Node getNode() {
		return node.get();
	}

	public final void setNode(Node node) {
		this.node.set(node);
	}

	private final BooleanProperty visible = new SimpleBooleanProperty(this, "visible", true);

	public final BooleanProperty visibleProperty() {
		return this.visible;
	}

	public final boolean isVisible() {
		return this.visibleProperty().get();
	}

	public final void setVisible(final boolean visible) {
		this.visibleProperty().set(visible);
	}

	private final DoubleProperty layoutX = new SimpleDoubleProperty(this, "layoutX", 0);

	public final DoubleProperty layoutXProperty() {
		return this.layoutX;
	}

	public final double getLayoutX() {
		return this.layoutXProperty().get();
	}

	public final void setLayoutX(final double layoutX) {
		this.layoutXProperty().set(layoutX);
	}

	private final DoubleProperty layoutY = new SimpleDoubleProperty(this, "layoutY", 0);

	public final DoubleProperty layoutYProperty() {
		return this.layoutY;
	}

	public final double getLayoutY() {
		return this.layoutYProperty().get();
	}

	public final void setLayoutY(final double layoutY) {
		this.layoutYProperty().set(layoutY);
	}

	private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 0);

	public final DoubleProperty widthProperty() {
		return this.width;
	}

	public final double getWidth() {
		return this.widthProperty().get();
	}

	public final void setWidth(final double width) {
		this.widthProperty().set(width);
	}

	private final DoubleProperty height = new SimpleDoubleProperty(this, "height", 0);

	public final DoubleProperty heightProperty() {
		return this.height;
	}

	public final double getHeight() {
		return this.heightProperty().get();
	}

	public final void setHeight(final double height) {
		this.heightProperty().set(height);
	}
}
