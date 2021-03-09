package com.dlsc.gemsfx.incubator.templatepane;

public class Visibility implements VisibilityPolicy {

	private double minRequiredHeight;
	private double minRequiredWidth;

	private double maxSupportedHeight = Double.MAX_VALUE;
	private double maxSupportedWidth = Double.MAX_VALUE;

	public Visibility(double minRequiredWidth, double minRequiredHeight, double maxSupportedWidth, double maxSupportedHeight) {
		setMinRequiredWidth(minRequiredWidth);
		setMinRequiredHeight(minRequiredHeight);
		setMaxSupportedWidth(maxSupportedWidth);
		setMaxSupportedHeight(maxSupportedHeight);
	}

	public static final Visibility minRequiredWidth(double minRequiredWidth) {
		return new Visibility(minRequiredWidth, 0, Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public static final Visibility minRequiredHeight(double minRequiredHeight) {
		return new Visibility(0, minRequiredHeight, Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public static final Visibility minRequiredSize(double minRequiredWidth, double minRequiredHeight) {
		return new Visibility(minRequiredWidth, minRequiredHeight, Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public static final Visibility of(double minRequiredWidth, double minRequiredHeight, double maxSupportedWidth, double maxSupportedHeight) {
		return new Visibility(minRequiredWidth, minRequiredHeight, maxSupportedWidth, maxSupportedHeight);
	}

	@Override
	public boolean isTileVisible(Tile tile) {
		TemplatePane pane = tile.getTemplatePane();

		double w = pane.getWidth();
		double h = pane.getHeight();

		return w > minRequiredWidth && w < maxSupportedWidth && h > minRequiredHeight && h < maxSupportedHeight;
	}

	public final double getMinRequiredHeight() {
		return minRequiredHeight;
	}

	public final void setMinRequiredHeight(double minRequiredHeight) {
		this.minRequiredHeight = minRequiredHeight;
	}

	public final double getMinRequiredWidth() {
		return minRequiredWidth;
	}

	public final void setMinRequiredWidth(double minRequiredWidth) {
		this.minRequiredWidth = minRequiredWidth;
	}

	public final double getMaxSupportedHeight() {
		return maxSupportedHeight;
	}

	public final void setMaxSupportedHeight(double maxSupportedHeight) {
		this.maxSupportedHeight = maxSupportedHeight;
	}

	public final double getMaxSupportedWidth() {
		return maxSupportedWidth;
	}

	public final void setMaxSupportedWidth(double maxSupportedWidth) {
		this.maxSupportedWidth = maxSupportedWidth;
	}
}
