open module com.dlsc.gemsfx {
    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires com.github.weisj.jsvg;

    requires jpro.utils.treeshowing;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.material;
    requires org.kordamp.ikonli.bootstrapicons;

    requires java.logging;
    requires java.prefs;

    requires commons.validator;
    requires net.synedra.validatorfx;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;

    requires retrofit2;
    requires okhttp3;
    requires java.desktop;
    requires com.dlsc.pickerfx;
    requires com.dlsc.unitfx;

    exports com.dlsc.gemsfx;
    exports com.dlsc.gemsfx.daterange;
    exports com.dlsc.gemsfx.incubator;
    exports com.dlsc.gemsfx.incubator.columnbrowser;
    exports com.dlsc.gemsfx.incubator.templatepane;
    exports com.dlsc.gemsfx.util;
    exports com.dlsc.gemsfx.skins;
    exports com.dlsc.gemsfx.binding;
    exports com.dlsc.gemsfx.infocenter;
    exports com.dlsc.gemsfx.treeview;
    exports com.dlsc.gemsfx.treeview.link;
}