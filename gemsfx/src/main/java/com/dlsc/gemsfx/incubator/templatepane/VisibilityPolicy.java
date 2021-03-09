package com.dlsc.gemsfx.incubator.templatepane;

public interface VisibilityPolicy {

	default boolean isTileVisible(Tile tile) {
		return true;
	}
}
