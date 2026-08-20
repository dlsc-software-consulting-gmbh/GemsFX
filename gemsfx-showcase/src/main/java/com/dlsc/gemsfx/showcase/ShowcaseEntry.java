package com.dlsc.gemsfx.showcase;

import javafx.application.Application;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Describes a single control inside the showcase application. Every entry refers to a PDF
 * manual and may optionally refer to a demo application that can be launched by the user.
 *
 * @param category     the semantic group the control belongs to, e.g. "Layout"
 * @param name         the display name of the control, e.g. "Calendar View"
 * @param manual       the file name of the manual (without extension), e.g. "calendar-view"
 * @param demoFactory  a factory creating the demo application or {@code null} if no demo exists
 */
public record ShowcaseEntry(String category, String name, String manual, Supplier<Application> demoFactory) {

    private static final String MANUALS_PATH = "manuals/";

    public boolean hasDemo() {
        return demoFactory != null;
    }

    /**
     * Opens a stream for the PDF manual of this control.
     *
     * @return the stream or {@code null} if the manual can not be found on the classpath
     */
    public InputStream openManual() {
        return ShowcaseEntry.class.getResourceAsStream(MANUALS_PATH + manual + ".pdf");
    }
}
