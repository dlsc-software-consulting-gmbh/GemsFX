module com.dlsc.gemsfx {
    requires transitive javafx.controls;

    requires java.logging;
    requires retrofit2;
    requires okhttp3;
    requires java.prefs;

    exports com.dlsc.gemsfx.util;
    exports com.dlsc.gemsfx;

}