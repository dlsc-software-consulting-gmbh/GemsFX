/**
 * GemsFX, a collection of custom controls and utilities for JavaFX.
 * <p>
 * The controls themselves are located in {@link com.dlsc.gemsfx} and its subpackages, while their
 * skin implementations can be found in {@link com.dlsc.gemsfx.skins}. Each control ships with its
 * own user agent stylesheet, so no additional setup is required to use it.
 */
open module com.dlsc.gemsfx {
    requires java.desktop;
    requires java.logging;
    requires java.prefs;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;

    requires com.github.weisj.jsvg;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.material; // referenced in CSS files via -fx-icon-code, prefix "gmi-"
    requires org.kordamp.ikonli.materialdesign; // referenced in CSS files via -fx-icon-code, prefix "mdi-"
    requires org.kordamp.ikonli.bootstrapicons; // referenced in CSS files via -fx-icon-code, prefix "bi-"

    requires net.synedra.validatorfx;

    requires com.dlsc.pickerfx;

    exports com.dlsc.gemsfx;
    exports com.dlsc.gemsfx.daterange;
    exports com.dlsc.gemsfx.util;
    exports com.dlsc.gemsfx.skins;
    exports com.dlsc.gemsfx.binding;
    exports com.dlsc.gemsfx.infocenter;
    exports com.dlsc.gemsfx.treeview;
    exports com.dlsc.gemsfx.treeview.link;
    exports com.dlsc.gemsfx.gridtable;
    exports com.dlsc.gemsfx.paging;
}
