package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dlsc.gemsfx.demo.GemsFXDemoLauncher.DemoEntry;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Utility application that captures screenshots of every registered demo for
 * each supported theme (Modena + all AtlantaFX themes) and saves them under
 * {@code screenshots/{theme-dir}/ClassName.png} on the classpath.
 *
 * Run with:
 *   ./mvnw javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.DemoSnapshotRunner
 */
public class DemoSnapshotRunner extends Application {

    private static final Path SCREENSHOTS_BASE = Path.of(
            "src/main/resources/com/dlsc/gemsfx/demo/screenshots");

    /** Maps each theme to its output directory name. Modena uses {@code null} UA stylesheet. */
    record ThemeDef(String dirName, Theme atlantaFxTheme) {
        boolean isAtlantaFx() { return atlantaFxTheme != null; }
    }

    private static final List<ThemeDef> ALL_THEMES = List.of(
            new ThemeDef("modena",          null),
            new ThemeDef("nord-light",      new NordLight()),
            new ThemeDef("nord-dark",       new NordDark()),
            new ThemeDef("cupertino-light", new CupertinoLight()),
            new ThemeDef("cupertino-dark",  new CupertinoDark()),
            new ThemeDef("primer-light",    new PrimerLight()),
            new ThemeDef("primer-dark",     new PrimerDark()),
            new ThemeDef("dracula",         new Dracula())
    );

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Snapshot Runner");
        primaryStage.show(); // keep the JavaFX toolkit alive

        // Optional: pass a class name via -Dsnapshot.filter to regenerate a single demo.
        // Usage: ./mvnw javafx:run -f gemsfx-demo/pom.xml \
        //            -Dmain.class=com.dlsc.gemsfx.demo.DemoSnapshotRunner \
        //            -Dsnapshot.filter=TimeRangePickerApp
        String filter = System.getProperty("snapshot.filter", "").trim();

        List<DemoEntry> demos = filter.isEmpty()
                ? GemsFXDemoLauncher.ALL_DEMOS
                : GemsFXDemoLauncher.ALL_DEMOS.stream()
                        .filter(e -> e.factory().get().getClass().getSimpleName().equals(filter))
                        .toList();

        if (demos.isEmpty()) {
            System.out.println("No demos matched filter: " + filter);
            Platform.exit();
            return;
        }
        processTheme(ALL_THEMES.iterator(), demos);
    }

    // ── Theme iteration ──────────────────────────────────────────────────────

    private void processTheme(Iterator<ThemeDef> themeIt, List<DemoEntry> demos) {
        if (!themeIt.hasNext()) {
            System.out.println("\nAll themes done. Exiting.");
            Platform.exit();
            return;
        }

        ThemeDef theme = themeIt.next();
        System.out.println("\n=== Theme: " + theme.dirName() + " ===");

        // Apply user-agent stylesheet and atlantafx system property.
        if (theme.isAtlantaFx()) {
            Application.setUserAgentStylesheet(theme.atlantaFxTheme().getUserAgentStylesheet());
            System.setProperty("atlantafx", "true");
        } else {
            Application.setUserAgentStylesheet(null); // Modena
            System.setProperty("atlantafx", "false");
        }

        Path dir = SCREENSHOTS_BASE.resolve(theme.dirName());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.out.println("ERROR creating dir: " + e.getMessage());
        }

        processNext(demos.iterator(), theme, dir, () -> processTheme(themeIt, demos));
    }

    // ── Demo iteration ───────────────────────────────────────────────────────

    private void processNext(Iterator<DemoEntry> demoIt, ThemeDef theme, Path dir, Runnable onDone) {
        if (!demoIt.hasNext()) {
            onDone.run();
            return;
        }

        DemoEntry entry = demoIt.next();
        Application app = entry.factory().get();
        String className = app.getClass().getSimpleName();

        Stage stage = new Stage();
        stage.setX(150);
        stage.setY(100);
        System.out.printf("  %-50s ... ", className);

        try {
            app.start(stage);
        } catch (Exception ex) {
            System.out.println("SKIP (start failed: " + ex.getMessage() + ")");
            stage.close();
            processNext(demoIt, theme, dir, onDone);
            return;
        }

        if (!stage.isShowing()) {
            System.out.println("SKIP (stage not shown)");
            processNext(demoIt, theme, dir, onDone);
            return;
        }

        // For AtlantaFX themes, inject atlantafx.css into the scene if the
        // demo's start() didn't do it via super.start() / GemApplication.
        if (theme.isAtlantaFx() && stage.getScene() != null) {
            String css = Objects.requireNonNull(
                    GemApplication.class.getResource("atlantafx.css")).toExternalForm();
            if (!stage.getScene().getStylesheets().contains(css)) {
                stage.getScene().getStylesheets().add(css);
            }
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            saveSnapshot(stage, className, dir);
            stage.close();
            processNext(demoIt, theme, dir, onDone);
        });
        pause.play();
    }

    // ── Snapshot save ─────────────────────────────────────────────────────────

    private void saveSnapshot(Stage stage, String className, Path dir) {
        if (stage.getScene() == null) {
            System.out.println("SKIP (no scene)");
            return;
        }
        WritableImage img = stage.getScene().snapshot(null);
        Path out = dir.resolve(className + ".png");
        try {
            BufferedImage bi = SwingFXUtils.fromFXImage(img, null);
            BufferedImage scaled = scaleToFit(bi, 600, 600);
            ImageIO.write(scaled, "PNG", out.toFile());
            System.out.println("saved -> " + out.getParent().getFileName() + "/" + out.getFileName());
        } catch (IOException ex) {
            System.out.println("ERROR saving: " + ex.getMessage());
        }
    }

    private static BufferedImage scaleToFit(BufferedImage src, int maxW, int maxH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        double scale = Math.min((double) maxW / srcW, (double) maxH / srcH);
        if (scale >= 1.0) return src; // already smaller than target
        int dstW = (int) Math.round(srcW * scale);
        int dstH = (int) Math.round(srcH * scale);
        BufferedImage dst = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, dstW, dstH, null);
        g.dispose();
        return dst;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
