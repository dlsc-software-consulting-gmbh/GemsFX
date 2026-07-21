package com.dlsc.gemsfx;

import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangeView;
import com.dlsc.gemsfx.gridtable.GridTableView;
import com.dlsc.gemsfx.infocenter.InfoCenterView;
import com.dlsc.gemsfx.paging.PagingControls;
import com.dlsc.gemsfx.paging.PagingGridTableView;
import com.dlsc.gemsfx.paging.PagingListView;
import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.application.Platform;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the GemsFX controls expose a sensible {@link AccessibleRole} (and, where
 * applicable, an {@code accessibleText}) as part of the baseline accessibility support.
 */
public class AccessibilityTest {

    @BeforeClass
    public static void initToolkit() {
        // The JavaFX toolkit must be running before controls (fonts, skins) can be created.
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // toolkit was started by a previous test - that is fine
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Node create(Supplier<Node> supplier) {
        AtomicReference<Node> ref = new AtomicReference<>();
        AtomicReference<RuntimeException> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ref.set(supplier.get());
            } catch (RuntimeException e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            assertTrue("Timed out creating control on FX thread", latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (error.get() != null) {
            throw error.get();
        }
        return ref.get();
    }

    private static Map<String, Supplier<Node>> controls() {
        Map<String, Supplier<Node>> map = new LinkedHashMap<>();

        // text inputs
        map.put("EmailField", EmailField::new);
        map.put("SearchField", SearchField::new);
        map.put("SearchTextField", SearchTextField::new);
        map.put("CustomTextField", CustomTextField::new);
        map.put("EnhancedPasswordField", EnhancedPasswordField::new);
        map.put("ExpandingTextArea", ExpandingTextArea::new);
        map.put("ResizableTextArea", ResizableTextArea::new);
        map.put("LimitedTextArea", LimitedTextArea::new);
        map.put("TextView", TextView::new);
        map.put("EnhancedLabel", EnhancedLabel::new);

        // pickers
        map.put("TimePicker", TimePicker::new);
        map.put("DurationPicker", DurationPicker::new);
        map.put("YearPicker", YearPicker::new);
        map.put("YearMonthPicker", YearMonthPicker::new);
        map.put("CalendarPicker", CalendarPicker::new);
        map.put("DateRangePicker", DateRangePicker::new);
        map.put("SelectionBox", SelectionBox::new);
        map.put("DayOfWeekPicker", DayOfWeekPicker::new);
        map.put("TimeRangePicker", TimeRangePicker::new);
        map.put("TagsField", TagsField::new);

        // date grid views
        map.put("CalendarView", CalendarView::new);
        map.put("YearView", YearView::new);
        map.put("YearMonthView", YearMonthView::new);
        map.put("DateRangeView", DateRangeView::new);

        // image controls
        map.put("AvatarView", AvatarView::new);
        map.put("PhotoView", PhotoView::new);
        map.put("SVGImageView", SVGImageView::new);
        map.put("MaskedView", MaskedView::new);

        // progress indicators
        map.put("CircleProgressIndicator", CircleProgressIndicator::new);
        map.put("SemiCircleProgressIndicator", SemiCircleProgressIndicator::new);

        // collection controls
        map.put("GridTableView", GridTableView::new);
        map.put("PagingListView", PagingListView::new);
        map.put("PagingGridTableView", PagingGridTableView::new);
        map.put("MultiColumnListView", MultiColumnListView::new);
        map.put("StripView", StripView::new);
        map.put("InfoCenterView", InfoCenterView::new);
        map.put("TreeNodeView", TreeNodeView::new);
        map.put("PagingControls", PagingControls::new);

        // misc controls
        map.put("ChipView", ChipView::new);
        map.put("HistoryButton", HistoryButton::new);
        map.put("BeforeAfterView", BeforeAfterView::new);
        map.put("SegmentedBar", SegmentedBar::new);
        map.put("FilterView", FilterView::new);
        map.put("SimpleFilterView", SimpleFilterView::new);
        map.put("ScreensView", ScreensView::new);
        map.put("Skeleton", Skeleton::new);

        return map;
    }

    private static Map<String, AccessibleRole> expectedRoles() {
        Map<String, AccessibleRole> map = new LinkedHashMap<>();

        map.put("EmailField", AccessibleRole.TEXT_FIELD);
        map.put("SearchField", AccessibleRole.TEXT_FIELD);
        map.put("SearchTextField", AccessibleRole.TEXT_FIELD);
        map.put("CustomTextField", AccessibleRole.TEXT_FIELD);
        map.put("EnhancedPasswordField", AccessibleRole.PASSWORD_FIELD);
        map.put("ExpandingTextArea", AccessibleRole.TEXT_AREA);
        map.put("ResizableTextArea", AccessibleRole.TEXT_AREA);
        map.put("LimitedTextArea", AccessibleRole.TEXT_AREA);
        map.put("TextView", AccessibleRole.TEXT);
        map.put("EnhancedLabel", AccessibleRole.TEXT);

        map.put("TimePicker", AccessibleRole.COMBO_BOX);
        map.put("DurationPicker", AccessibleRole.COMBO_BOX);
        map.put("YearPicker", AccessibleRole.COMBO_BOX);
        map.put("YearMonthPicker", AccessibleRole.COMBO_BOX);
        map.put("CalendarPicker", AccessibleRole.DATE_PICKER);
        map.put("DateRangePicker", AccessibleRole.DATE_PICKER);
        map.put("SelectionBox", AccessibleRole.COMBO_BOX);
        map.put("DayOfWeekPicker", AccessibleRole.COMBO_BOX);
        map.put("TimeRangePicker", AccessibleRole.COMBO_BOX);
        map.put("TagsField", AccessibleRole.COMBO_BOX);

        map.put("CalendarView", AccessibleRole.DATE_PICKER);
        map.put("YearView", AccessibleRole.DATE_PICKER);
        map.put("YearMonthView", AccessibleRole.DATE_PICKER);
        map.put("DateRangeView", AccessibleRole.DATE_PICKER);

        map.put("AvatarView", AccessibleRole.IMAGE_VIEW);
        map.put("PhotoView", AccessibleRole.IMAGE_VIEW);
        map.put("SVGImageView", AccessibleRole.IMAGE_VIEW);
        map.put("MaskedView", AccessibleRole.IMAGE_VIEW);

        map.put("CircleProgressIndicator", AccessibleRole.PROGRESS_INDICATOR);
        map.put("SemiCircleProgressIndicator", AccessibleRole.PROGRESS_INDICATOR);

        map.put("GridTableView", AccessibleRole.TABLE_VIEW);
        map.put("PagingListView", AccessibleRole.LIST_VIEW);
        map.put("PagingGridTableView", AccessibleRole.TABLE_VIEW);
        map.put("MultiColumnListView", AccessibleRole.LIST_VIEW);
        map.put("StripView", AccessibleRole.LIST_VIEW);
        map.put("InfoCenterView", AccessibleRole.LIST_VIEW);
        map.put("TreeNodeView", AccessibleRole.TREE_VIEW);
        map.put("PagingControls", AccessibleRole.PAGINATION);

        map.put("ChipView", AccessibleRole.BUTTON);
        map.put("HistoryButton", AccessibleRole.BUTTON);
        map.put("BeforeAfterView", AccessibleRole.SLIDER);
        map.put("SegmentedBar", AccessibleRole.NODE);
        map.put("FilterView", AccessibleRole.NODE);
        map.put("SimpleFilterView", AccessibleRole.NODE);
        map.put("ScreensView", AccessibleRole.NODE);
        map.put("Skeleton", AccessibleRole.NODE);

        return map;
    }

    @Test
    public void everyControlHasExpectedAccessibleRole() {
        Map<String, Supplier<Node>> controls = controls();
        Map<String, AccessibleRole> expected = expectedRoles();

        for (Map.Entry<String, AccessibleRole> entry : expected.entrySet()) {
            String name = entry.getKey();
            Supplier<Node> supplier = controls.get(name);
            assertNotNull("No control supplier registered for " + name, supplier);

            Node node = create(supplier);
            assertNotNull("Control " + name + " could not be created", node);
            assertEquals("Unexpected accessible role for " + name, entry.getValue(), node.getAccessibleRole());
        }
    }

    @Test
    public void valueBearingControlsExposeAccessibleText() {
        // EmailField derives its accessible text from the email address.
        EmailField emailField = (EmailField) create(() -> {
            EmailField f = new EmailField();
            f.setEmailAddress("jane@example.com");
            return f;
        });
        assertNotNull(emailField.getAccessibleText());
        assertTrue(emailField.getAccessibleText().contains("jane@example.com"));

        // Progress indicators describe their progress as a percentage.
        CircleProgressIndicator progress = (CircleProgressIndicator) create(() -> {
            CircleProgressIndicator p = new CircleProgressIndicator();
            p.setProgress(0.5);
            return p;
        });
        assertNotNull(progress.getAccessibleText());
        assertTrue(progress.getAccessibleText().contains("50"));
    }

    @Test
    public void accessibleTextYieldsToApplicationValue() {
        // An explicitly set accessible text must not be overwritten by the automatic binding.
        EmailField emailField = (EmailField) create(() -> {
            EmailField f = new EmailField();
            f.setEmailAddress("jane@example.com");
            f.setAccessibleText("custom label");
            f.setEmailAddress("john@example.com");
            return f;
        });
        assertEquals("custom label", emailField.getAccessibleText());
    }

    @Test
    public void accessibleTextIsLocalized() {
        // The accessibility strings must honour the ResourceBundleManager locale.
        java.util.Locale original = com.dlsc.gemsfx.util.ResourceBundleManager.getLocale();
        try {
            com.dlsc.gemsfx.util.ResourceBundleManager.setLocale(Locale.GERMAN);
            AvatarView avatar = (AvatarView) create(() -> {
                AvatarView a = new AvatarView();
                a.setInitials("JD");
                return a;
            });
            assertEquals("Avatar von JD", avatar.getAccessibleText());
        } finally {
            com.dlsc.gemsfx.util.ResourceBundleManager.setLocale(original);
        }
    }
}
