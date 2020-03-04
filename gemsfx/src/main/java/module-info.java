module com.dlsc.gemsfx {
    requires transitive javafx.controls;
    requires javafx.swing;

    requires com.sun.xml.bind;

    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;

    requires com.dlsc.unitfx;

    requires java.logging;
    requires java.prefs;

    requires java.xml.bind;
    requires commons.validator;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;

    requires retrofit2;
    requires okhttp3;
    requires org.apache.pdfbox;
    requires java.desktop;

    exports com.dlsc.gemsfx.util;
    exports com.dlsc.gemsfx;
    exports com.dlsc.gemsfx.keyboard;
    exports com.dlsc.gemsfx.richtextarea;

    opens com.dlsc.gemsfx.keyboard;
}