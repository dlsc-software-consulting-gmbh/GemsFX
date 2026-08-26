package com.dlsc.gemsfx.showcase;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Properties;

/**
 * Gives access to the version of the GemsFX library presented by the showcase. The version is
 * written into the resource "build.properties" by the Maven build, hence it always matches the
 * version of the library module the showcase was built with.
 */
public final class ShowcaseVersion {

    private static final Logger LOG = System.getLogger(ShowcaseVersion.class.getName());

    private static final String RESOURCE = "build.properties";

    private static final String VERSION = readVersion();

    private ShowcaseVersion() {
    }

    /**
     * Returns the version of the GemsFX library, for example "4.4.2", or an empty string if the
     * version could not be determined, which happens when the application runs outside of a
     * Maven build.
     *
     * @return the version of the library, never {@code null}
     */
    public static String get() {
        return VERSION;
    }

    private static String readVersion() {
        try (InputStream in = ShowcaseVersion.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                LOG.log(Level.WARNING, () -> "missing resource \"" + RESOURCE + "\"");
                return "";
            }

            Properties properties = new Properties();
            properties.load(in);

            String version = properties.getProperty("version", "").trim();

            // an unresolved placeholder means that the resource filtering did not run
            if (version.startsWith("${")) {
                return "";
            }

            return version;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "could not read the version of the library", ex);
            return "";
        }
    }
}
