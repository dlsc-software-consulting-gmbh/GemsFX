open module com.dlsc.gemsfx.demo {
    requires java.prefs;

    requires com.dlsc.gemsfx;
    requires com.dlsc.pdfviewfx;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires one.jpro.platform.utils;
    requires one.jpro.platform.mdfx;

    requires org.scenicview.scenicview;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;

    requires fr.brouillard.oss.cssfx;
    requires net.synedra.validatorfx;
    requires org.apache.commons.lang3;

    exports com.dlsc.gemsfx.demo;
    exports com.dlsc.gemsfx.demo.binding;
    exports com.dlsc.gemsfx.demo.util;
}