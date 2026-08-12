package com.dlsc.gemsfx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.BeforeClass;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Base class for all tests that need a running JavaFX toolkit.
 * <p>
 * The toolkit is started exactly once per JVM. On CI servers (or any other machine without a
 * display) the Monocle headless platform is used, which is activated via the system properties
 * configured for the surefire plugin (see the POM). As a safety net this class sets those
 * properties itself in case the tests are launched directly from an IDE on a headless machine.
 */
public abstract class FxTestBase {

    private static final long TIMEOUT_SECONDS = 30;

    private static boolean toolkitStarted;

    @BeforeClass
    public static void startToolkit() {
        startJavaFxToolkit();
    }

    private static synchronized void startJavaFxToolkit() {
        if (toolkitStarted) {
            return;
        }

        if (isHeadless()) {
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.order", "sw");
            System.setProperty("java.awt.headless", "true");
        }

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                fail("JavaFX toolkit did not start within " + TIMEOUT_SECONDS + " seconds");
            }
        } catch (IllegalStateException alreadyStarted) {
            // the toolkit was started by a previously executed test class, which is fine
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while starting the JavaFX toolkit");
        }

        Platform.setImplicitExit(false);
        toolkitStarted = true;
    }

    private static boolean isHeadless() {
        if (System.getProperty("glass.platform") != null) {
            return true;
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("windows")) {
            return false;
        }

        // Linux / Unix without a display server
        return System.getenv("DISPLAY") == null;
    }

    /**
     * Executes the given runnable on the JavaFX application thread and waits for its completion.
     *
     * @param runnable the code to execute
     */
    protected static void runFx(Runnable runnable) {
        invoke(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Executes the given supplier on the JavaFX application thread and returns its result.
     *
     * @param supplier the code to execute
     * @param <T>      the type of the returned value
     * @return the value produced by the supplier
     */
    protected static <T> T invoke(Supplier<T> supplier) {
        if (Platform.isFxApplicationThread()) {
            return supplier.get();
        }

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue("Timed out while waiting for the JavaFX application thread",
                    latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for the JavaFX application thread");
        }

        Throwable throwable = error.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }
            throw new IllegalStateException(throwable);
        }

        return result.get();
    }

    /**
     * Waits until all currently pending JavaFX events have been processed.
     */
    protected static void waitForFxEvents() {
        for (int i = 0; i < 3; i++) {
            runFx(() -> {
            });
        }
    }

    /**
     * Attaches the given node to a scene inside a (never shown) stage and performs a layout pass.
     * This forces the creation of the skin, which is required to test skin related behaviour.
     *
     * @param node the node to lay out
     * @param <T>  the type of the node
     * @return the very same node
     */
    protected static <T extends Node> T layout(T node) {
        return invoke(() -> {
            StackPane parent = new StackPane(node);
            Scene scene = new Scene(parent, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            parent.applyCss();
            parent.layout();
            return node;
        });
    }
}
