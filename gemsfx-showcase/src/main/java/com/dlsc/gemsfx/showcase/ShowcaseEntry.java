package com.dlsc.gemsfx.showcase;

import javafx.application.Application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Describes a single control inside the showcase application. Every entry refers to a PDF
 * manual and may optionally refer to a demo application that can be launched by the user.
 *
 * @param category   the semantic group the control belongs to, e.g. "Layout"
 * @param name       the display name of the control, e.g. "Calendar View"
 * @param manual     the file name of the manual (without extension), e.g. "calendar-view"
 * @param demoClass  the class of the demo application or {@code null} if no demo exists
 */
public record ShowcaseEntry(String category, String name, String manual, Class<? extends Application> demoClass) {

    private static final String MANUALS_PATH = "manuals/";

    public boolean hasDemo() {
        return demoClass != null;
    }

    /**
     * Creates a new instance of the demo application of this control.
     *
     * @return the demo application
     * @throws IllegalStateException if no demo exists or if it can not be instantiated
     */
    public Application createDemo() {
        if (demoClass == null) {
            throw new IllegalStateException("the control \"" + name + "\" does not have a demo application");
        }

        try {
            return demoClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("the demo application \"" + demoClass.getSimpleName() + "\" can not be created", ex);
        }
    }

    /**
     * Loads the source code of the demo application. The demo module adds its Java files to the
     * build output, hence the source code can be read from the classpath.
     *
     * @return the source code or {@code null} if this control has no demo or if the source code
     * can not be found
     */
    public String loadDemoSource() {
        if (demoClass == null) {
            return null;
        }

        try (InputStream stream = demoClass.getResourceAsStream(demoClass.getSimpleName() + ".java")) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }
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
