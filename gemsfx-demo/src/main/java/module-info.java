module com.dlsc.gemsfx.demo {
    requires java.prefs;

    requires com.dlsc.gemsfx;
    requires com.dlsc.pdfviewfx;

    requires javafx.web;
    requires javafx.controls;

    requires org.scenicview.scenicview;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;

    requires fr.brouillard.oss.cssfx;
    requires net.synedra.validatorfx;

    exports com.dlsc.gemsfx.demo;
    exports com.dlsc.gemsfx.demo.binding;
}