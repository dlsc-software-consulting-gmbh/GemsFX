open module com.dlsc.gemsfx.showcase {
    requires java.prefs;

    requires com.dlsc.gemsfx;
    requires com.dlsc.gemsfx.demo;
    requires com.dlsc.pdfviewfx;

    requires atlantafx.base;
    requires atlantafx.decorations;
    requires com.dlsc.atlantafx.themes;
    requires devtoolsfx.connector;
    requires devtoolsfx.gui;

    requires javafx.controls;
    requires javafx.graphics;

    requires one.jpro.platform.mdfx;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;

    exports com.dlsc.gemsfx.showcase;
}
