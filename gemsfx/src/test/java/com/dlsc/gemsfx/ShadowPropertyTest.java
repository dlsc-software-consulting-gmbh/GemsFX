package com.dlsc.gemsfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class ShadowPropertyTest extends FxTestBase {

    @Test
    public void primitiveValueSurvivesPropertyCreationAndBinding() {
        runFx(() -> {
            HiddenSidesPane pane = new HiddenSidesPane();
            assertEquals(16, pane.getTriggerDistance(), 0);
            assertUnallocated(pane, "triggerDistance");

            pane.setTriggerDistance(42);
            assertEquals(42, pane.getTriggerDistance(), 0);
            assertUnallocated(pane, "triggerDistance");

            DoubleProperty property = pane.triggerDistanceProperty();
            assertSame(property, pane.triggerDistanceProperty());
            assertSame(pane, property.getBean());
            assertEquals("triggerDistance", property.getName());
            assertEquals(42, property.get(), 0);

            AtomicInteger changes = new AtomicInteger();
            property.addListener((observable, oldValue, newValue) -> changes.incrementAndGet());
            pane.setTriggerDistance(43);
            assertEquals(1, changes.get());

            SimpleDoubleProperty source = new SimpleDoubleProperty(25);
            property.bind(source);
            assertEquals(25, pane.getTriggerDistance(), 0);
            assertThrows(RuntimeException.class, () -> pane.setTriggerDistance(30));
            source.set(26);
            assertEquals(26, pane.getTriggerDistance(), 0);
            property.unbind();
            pane.setTriggerDistance(27);
            assertEquals(27, property.get(), 0);
        });
    }

    @Test
    public void objectValueAndNullSurvivePropertyCreation() {
        runFx(() -> {
            HiddenSidesPane pane = new HiddenSidesPane();
            assertEquals(Duration.millis(300), pane.getAnimationDelay());
            assertUnallocated(pane, "animationDelay");
            pane.setAnimationDelay(null);
            assertNull(pane.getAnimationDelay());
            assertUnallocated(pane, "animationDelay");
            assertNull(pane.animationDelayProperty().get());
            pane.setAnimationDelay(Duration.seconds(1));
            assertEquals(Duration.seconds(1), pane.animationDelayProperty().get());
        });
    }

    @Test
    public void callbackSetBeforeObservationRemainsCallable() {
        runFx(() -> {
            SearchField<String> field = new SearchField<>();
            AtomicInteger calls = new AtomicInteger();
            Consumer<String> callback = value -> calls.incrementAndGet();
            field.setOnCommit(callback);
            assertUnallocated(field, "onCommit");
            field.getOnCommit().accept("value");
            assertEquals(1, calls.get());
            assertSame(callback, field.onCommitProperty().get());
            assertUnallocated(field, "onCommitValue");
            field.setOnCommit(null);
            assertNull(field.getOnCommit());
        });
    }

    @Test
    public void dialogPropertyKeepsItsOriginalMetadata() {
        runFx(() -> {
            DialogPane.Dialog<Void> dialog = new DialogPane.Dialog<>(new DialogPane(), DialogPane.Type.BLANK);
            assertTrue(dialog.isSameWidthButtons());
            dialog.setSameWidthButtons(false);
            assertUnallocated(dialog, "sameWidthButtons");

            BooleanProperty property = dialog.sameWidthButtonsProperty();
            assertFalse(property.get());
            assertNull(property.getBean());
            assertEquals("", property.getName());
        });
    }

    @Test
    public void defaultSupplierUsesTheCurrentLabelText() {
        runFx(() -> {
            EnhancedLabel label = new EnhancedLabel("first");
            assertEquals("first", label.getCopyContentSupplier().get());
            assertUnallocated(label, "copyContentSupplier");
            label.setText("second");
            assertEquals("second", label.copyContentSupplierProperty().get().get());
            assertUnallocated(label, "copyContentSupplierValue");
        });
    }

    private static void assertUnallocated(Object bean, String fieldName) {
        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            assertNull(fieldName + " should not be allocated", field.get(bean));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
