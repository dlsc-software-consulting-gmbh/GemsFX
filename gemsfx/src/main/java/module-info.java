module com.dlsc.gemsfx {
    requires transitive javafx.controls;

    requires com.sun.xml.bind;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;

    requires java.logging;
    requires java.prefs;

    requires java.xml.bind;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;

    requires retrofit2;
    requires okhttp3;

    exports com.dlsc.gemsfx.util;
    exports com.dlsc.gemsfx;
    exports com.dlsc.gemsfx.keyboard;

    opens com.dlsc.gemsfx.keyboard;
}