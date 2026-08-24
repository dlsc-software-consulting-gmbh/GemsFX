package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Temporary verification harness: embeds every demo of the registry and reports failures.
 */
public class EmbedCheckApp extends Application {

    @Override
    public void start(Stage stage) {
        System.setProperty("atlantafx", "true");
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        int ok = 0;
        int failed = 0;

        for (ShowcaseEntry entry : ShowcaseRegistry.ALL_ENTRIES) {
            if (!entry.hasDemo()) {
                continue;
            }

            String filter = System.getenv().getOrDefault("EMBED_FILTER", "").trim();
            if (!filter.isEmpty() && !entry.name().toLowerCase().contains(filter.toLowerCase())) {
                continue;
            }

            try {
                DemoEmbedder.EmbeddedDemo demo = DemoEmbedder.embed(entry.demoFactory().get());
                System.out.println("OK    " + entry.name() + " -> " + Math.round(demo.prefWidth()) + "x" + Math.round(demo.prefHeight()));
                ok++;
            } catch (Throwable ex) {
                System.out.println("FAIL  " + entry.name() + " -> " + ex);
                Throwable cause = ex.getCause();
                if (cause != null) {
                    System.out.println("      cause: " + cause);
                }
                failed++;
            }
        }

        System.out.println("RESULT ok=" + ok + " failed=" + failed);
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
