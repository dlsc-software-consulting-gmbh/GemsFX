package com.dlsc.gemsfx;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DialogPane}.
 */
public class DialogPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.getStyleClass().contains("dialog-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        DialogPane pane = invoke(DialogPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultAnimateDialogs() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.isAnimateDialogs());
    }

    @Test
    public void defaultFadeInOut() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.isFadeInOut());
    }

    @Test
    public void defaultNotShowingDialog() {
        DialogPane pane = invoke(DialogPane::new);
        assertFalse(pane.isShowingDialog());
    }

    @Test
    public void defaultAnimationDuration() {
        DialogPane pane = invoke(DialogPane::new);
        assertEquals(Duration.millis(100), pane.getAnimationDuration());
    }

    @Test
    public void getGlassPaneNotNull() {
        DialogPane pane = invoke(DialogPane::new);
        assertNotNull(pane.getGlassPane());
    }

    @Test
    public void showDialogAddsToList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        // Turn off animations so dialog is added synchronously
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("Title", "Message"));
        assertFalse(pane.getDialogs().isEmpty());
    }

    @Test
    public void hideDialogRemovesFromList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("Title", "Message"));
        runFx(() -> pane.hideDialog(dialog));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
    }

    @Test
    public void hideAllDialogsClearsAll() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> {
            pane.setAnimateDialogs(false);
            pane.showWarning("A", "msg");
            pane.showWarning("B", "msg");
        });
        runFx(pane::hideAllDialogs);
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
    }

    @Test
    public void showingDialogBindsToDialogList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> d = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(pane.isShowingDialog());
        runFx(() -> pane.hideDialog(d));
        waitForFxEvents();
        assertFalse(pane.isShowingDialog());
    }

    @Test
    public void dialogWithCancelButtonIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void confirmationDialogIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showConfirmation("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void singleButtonDialogIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showInformation("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void dialogWithoutButtonsIsNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Collections.emptyList()));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void dialogWithMultipleNonCancelButtonsIsNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Arrays.asList(ButtonType.OK, ButtonType.APPLY)));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void requestCancelDoesNothingWhenNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Collections.emptyList()));
        assertFalse(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertFalse(pane.getDialogs().isEmpty());
    }

    @Test
    public void requestCancelCommitsCancelWhenCancelButtonPresent() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        List<ButtonType> pressed = new ArrayList<>();
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        runFx(() -> dialog.setOnClose(pressed::add));
        assertTrue(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
        assertEquals(Collections.singletonList(ButtonType.CANCEL), pressed);
    }

    @Test
    public void requestCancelPressesSingleButton() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        List<ButtonType> pressed = new ArrayList<>();
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showInformation("T", "M"));
        runFx(() -> dialog.setOnClose(pressed::add));
        assertTrue(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
        assertEquals(Collections.singletonList(ButtonType.OK), pressed);
    }

    @Test
    public void cancellableUpdatesWhenButtonTypesChange() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(dialog.isCancellable());
        runFx(() -> dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.APPLY));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void animateDialogsPropertyRoundTrip() {
        DialogPane pane = invoke(DialogPane::new);
        runFx(() -> pane.setAnimateDialogs(false));
        assertFalse(pane.isAnimateDialogs());
    }

    @Test
    public void showAndWaitReturnsButtonAndPreservesCloseHandler() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            AtomicInteger closeCalls = new AtomicInteger();
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = pane.showConfirmation("Confirm", "Continue?");
                dialog.onClose(button -> closeCalls.incrementAndGet());
                Platform.runLater(() -> dialog.getButton(ButtonType.YES).fire());
                return dialog.showAndWait();
            });
            assertEquals(ButtonType.YES, result);
            assertEquals(1, closeCalls.get());
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void showAndWaitReturnsNullWhenDialogIsRemoved() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            AtomicInteger closeCalls = new AtomicInteger();
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = pane.showConfirmation("Confirm", "Continue?");
                dialog.onClose(button -> closeCalls.incrementAndGet());
                Platform.runLater(() -> pane.hideDialog(dialog));
                return dialog.showAndWait();
            });
            assertNull(result);
            assertEquals(0, closeCalls.get());
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void showAndWaitReturnsNullWhenWindowIsHidden() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = pane.showConfirmation("Confirm", "Continue?");
                Platform.runLater(stage::hide);
                return dialog.showAndWait();
            });
            assertNull(result);
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    private static Stage showStage(DialogPane pane) {
        return invoke(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(pane), 400, 300));
            stage.show();
            return stage;
        });
    }

    @Test
    public void hidingDelayedDialogUnblocksWaitingWithoutShowingIt() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            AtomicBoolean returned = new AtomicBoolean();
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = new DialogPane.Dialog<>(pane, DialogPane.Type.CONFIRMATION);
                dialog.setDelay(Duration.seconds(1));
                dialog.show();
                Platform.runLater(() -> {
                    pane.hideDialog(dialog);
                    Platform.runLater(() -> {
                        if (!returned.get()) {
                            dialog.cancel();
                        }
                    });
                });
                ButtonType answer = dialog.showAndWait();
                returned.set(true);
                return answer;
            });
            assertNull(result);
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void showAndWaitAllowsNestedDialogInCloseCallback() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            List<ButtonType> results = invoke(() -> {
                List<ButtonType> answers = new ArrayList<>();
                DialogPane.Dialog<ButtonType> outer = pane.showConfirmation("Outer", "Continue?");
                outer.onClose(button -> {
                    DialogPane.Dialog<ButtonType> inner = pane.showConfirmation("Inner", "Continue?");
                    Platform.runLater(() -> inner.press(ButtonType.NO));
                    answers.add(inner.showAndWait());
                });
                Platform.runLater(() -> outer.press(ButtonType.YES));
                answers.add(outer.showAndWait());
                return answers;
            });
            assertEquals(Arrays.asList(ButtonType.NO, ButtonType.YES), results);
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void showAndWaitShowsNewDialogAndReturnsCancel() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = new DialogPane.Dialog<>(pane, DialogPane.Type.CONFIRMATION);
                Platform.runLater(dialog::cancel);
                return dialog.showAndWait();
            });
            assertEquals(ButtonType.CANCEL, result);
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void showAndWaitRejectsInvalidCallingContexts() {
        DialogPane pane = invoke(DialogPane::new);
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> new DialogPane.Dialog<>(pane, DialogPane.Type.CONFIRMATION));
        assertThrows(IllegalStateException.class, dialog::showAndWait);
        runFx(() -> assertThrows(IllegalStateException.class, dialog::showAndWait));
    }

    @Test
    public void showAndWaitRejectsAnimationAndRemovesNewDialog() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            AtomicBoolean rejected = new AtomicBoolean();
            CountDownLatch finished = new CountDownLatch(1);
            runFx(() -> {
                AnimationTimer timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        stop();
                        DialogPane.Dialog<ButtonType> dialog = new DialogPane.Dialog<>(pane, DialogPane.Type.CONFIRMATION);
                        try {
                            dialog.showAndWait();
                        } catch (IllegalStateException expected) {
                            rejected.set(true);
                        } finally {
                            finished.countDown();
                        }
                    }
                };
                timer.start();
            });
            try {
                assertTrue("The animation callback should complete", finished.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AssertionError(ex);
            }
            assertTrue("JavaFX must reject a nested loop during animation", rejected.get());
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void detachingPaneEndsWait() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = pane.showConfirmation("Confirm", "Continue?");
                Platform.runLater(() -> ((StackPane) stage.getScene().getRoot()).getChildren().remove(pane));
                return dialog.showAndWait();
            });
            assertNull(result);
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void hidingDelayedDialogAllowsItToBeShownAgain() {
        DialogPane pane = invoke(DialogPane::new);
        Stage stage = showStage(pane);
        try {
            ButtonType result = invoke(() -> {
                DialogPane.Dialog<ButtonType> dialog = new DialogPane.Dialog<>(pane, DialogPane.Type.CONFIRMATION);
                dialog.setDelay(Duration.seconds(1));
                dialog.show();
                pane.hideDialog(dialog);
                dialog.setDelay(null);
                Platform.runLater(() -> dialog.press(ButtonType.YES));
                return dialog.showAndWait();
            });
            assertEquals(ButtonType.YES, result);
            assertTrue(invoke(() -> pane.getDialogs().isEmpty()));
        } finally {
            runFx(stage::hide);
        }
    }
}
