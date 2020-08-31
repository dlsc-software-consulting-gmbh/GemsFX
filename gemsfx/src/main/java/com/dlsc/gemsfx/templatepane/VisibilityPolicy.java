package com.dlsc.gemsfx.templatepane;

public interface VisibilityPolicy {

	default boolean isTileVisible(Tile tile) {
		return true;
	}
}
