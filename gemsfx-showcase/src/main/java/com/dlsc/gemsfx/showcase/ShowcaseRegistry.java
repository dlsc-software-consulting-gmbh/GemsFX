package com.dlsc.gemsfx.showcase;

import com.dlsc.gemsfx.demo.AvatarViewApp;
import com.dlsc.gemsfx.demo.BeforeAfterViewApp;
import com.dlsc.gemsfx.demo.CalendarPickerApp;
import com.dlsc.gemsfx.demo.CalendarViewApp;
import com.dlsc.gemsfx.demo.CircleProgressIndicatorApp;
import com.dlsc.gemsfx.demo.DateRangePickerApp;
import com.dlsc.gemsfx.demo.DateRangeViewApp;
import com.dlsc.gemsfx.demo.DayOfWeekPickerApp;
import com.dlsc.gemsfx.demo.DialogPaneApp;
import com.dlsc.gemsfx.demo.DrawerStackPaneApp;
import com.dlsc.gemsfx.demo.DurationPickerApp;
import com.dlsc.gemsfx.demo.EmailFieldApp;
import com.dlsc.gemsfx.demo.EnhancedLabelApp;
import com.dlsc.gemsfx.demo.EnhancedPasswordFieldApp;
import com.dlsc.gemsfx.demo.ExpandingTextAreaApp;
import com.dlsc.gemsfx.demo.FilterViewApp;
import com.dlsc.gemsfx.demo.GridTableViewApp;
import com.dlsc.gemsfx.demo.HiddenSidesPaneApp;
import com.dlsc.gemsfx.demo.HistoryManagerApp;
import com.dlsc.gemsfx.demo.InfoCenterApp;
import com.dlsc.gemsfx.demo.LimitedTextAreaApp;
import com.dlsc.gemsfx.demo.LoadingPaneApp;
import com.dlsc.gemsfx.demo.MultiColumnListViewApp;
import com.dlsc.gemsfx.demo.PagingControlsApp;
import com.dlsc.gemsfx.demo.PagingGridTableViewApp;
import com.dlsc.gemsfx.demo.PagingListViewApp;
import com.dlsc.gemsfx.demo.PaymentOptionApp;
import com.dlsc.gemsfx.demo.PhotoViewApp;
import com.dlsc.gemsfx.demo.PopOverApp;
import com.dlsc.gemsfx.demo.PowerPaneApp;
import com.dlsc.gemsfx.demo.ResizableTextAreaApp;
import com.dlsc.gemsfx.demo.ResponsivePaneApp;
import com.dlsc.gemsfx.demo.SVGImageViewApp;
import com.dlsc.gemsfx.demo.ScreensViewApp;
import com.dlsc.gemsfx.demo.SearchFieldApp;
import com.dlsc.gemsfx.demo.SearchTextFieldApp;
import com.dlsc.gemsfx.demo.SegmentedBarApp;
import com.dlsc.gemsfx.demo.SelectionBoxApp;
import com.dlsc.gemsfx.demo.SemiCircleProgressIndicatorApp;
import com.dlsc.gemsfx.demo.SimpleFilterViewApp;
import com.dlsc.gemsfx.demo.SkeletonPaneApp;
import com.dlsc.gemsfx.demo.SpacerApp;
import com.dlsc.gemsfx.demo.StretchingTilePaneApp;
import com.dlsc.gemsfx.demo.StripViewApp;
import com.dlsc.gemsfx.demo.TableViewExample;
import com.dlsc.gemsfx.demo.TagsFieldApp;
import com.dlsc.gemsfx.demo.TextViewApp;
import com.dlsc.gemsfx.demo.ThreeItemsPaneApp;
import com.dlsc.gemsfx.demo.TimePickerApp;
import com.dlsc.gemsfx.demo.TimeRangePickerApp;
import com.dlsc.gemsfx.demo.TreeNodeViewApp;
import com.dlsc.gemsfx.demo.YearMonthPickerApp;
import com.dlsc.gemsfx.demo.YearMonthViewApp;
import com.dlsc.gemsfx.demo.YearPickerApp;
import com.dlsc.gemsfx.demo.YearViewApp;
import javafx.application.Application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The registry of all controls presented by the showcase application. Every entry is backed by
 * one of the PDF manuals found in the "docs/manuals" folder of the project. Entries may or may
 * not have a demo application attached to them.
 */
public final class ShowcaseRegistry {

    public static final String CALENDAR = "Calendar & Date / Time";
    public static final String TEXT_AND_INPUT = "Text & Input";
    public static final String LISTS_AND_TABLES = "Lists & Tables";
    public static final String LAYOUT = "Layout";
    public static final String MEDIA_AND_GRAPHICS = "Media & Graphics";
    public static final String PROGRESS = "Progress";
    public static final String OVERLAYS_AND_DIALOGS = "Overlays & Dialogs";
    public static final String UTILITIES = "Utilities";

    /**
     * All controls of the showcase, in category / alphabetical order.
     */
    public static final List<ShowcaseEntry> ALL_ENTRIES = List.of(

            // --- Calendar & Date / Time -----------------------------------------
            entry(CALENDAR, "Calendar Picker", "calendar-picker", CalendarPickerApp::new),
            entry(CALENDAR, "Calendar View", "calendar-view", CalendarViewApp::new),
            entry(CALENDAR, "Date Range Picker", "date-range-picker", DateRangePickerApp::new),
            entry(CALENDAR, "Date Range View", "date-range-view", DateRangeViewApp::new),
            entry(CALENDAR, "Day Of Week Picker", "day-of-week-picker", DayOfWeekPickerApp::new),
            entry(CALENDAR, "Duration Picker", "duration-picker", DurationPickerApp::new),
            entry(CALENDAR, "Time Picker", "time-picker", TimePickerApp::new),
            entry(CALENDAR, "Time Range Picker", "time-range-picker", TimeRangePickerApp::new),
            entry(CALENDAR, "Year Month Picker", "year-month-picker", YearMonthPickerApp::new),
            entry(CALENDAR, "Year Month View", "year-month-view", YearMonthViewApp::new),
            entry(CALENDAR, "Year Picker", "year-picker", YearPickerApp::new),
            entry(CALENDAR, "Year View", "year-view", YearViewApp::new),

            // --- Text & Input ---------------------------------------------------
            entry(TEXT_AND_INPUT, "Chip View", "chip-view", null),
            entry(TEXT_AND_INPUT, "Chips View Container", "chips-view-container", null),
            entry(TEXT_AND_INPUT, "Email Field", "email-field", EmailFieldApp::new),
            entry(TEXT_AND_INPUT, "Enhanced Label", "enhanced-label", EnhancedLabelApp::new),
            entry(TEXT_AND_INPUT, "Enhanced Password Field", "enhanced-password-field", EnhancedPasswordFieldApp::new),
            entry(TEXT_AND_INPUT, "Expanding Text Area", "expanding-text-area", ExpandingTextAreaApp::new),
            entry(TEXT_AND_INPUT, "History Button", "history-button", HistoryManagerApp::new),
            entry(TEXT_AND_INPUT, "Limited Text Area", "limited-text-area", LimitedTextAreaApp::new),
            entry(TEXT_AND_INPUT, "Resizable Text Area", "resizable-text-area", ResizableTextAreaApp::new),
            entry(TEXT_AND_INPUT, "Search Field", "search-field", SearchFieldApp::new),
            entry(TEXT_AND_INPUT, "Search Text Field", "search-text-field", SearchTextFieldApp::new),
            entry(TEXT_AND_INPUT, "Selection Box", "selection-box", SelectionBoxApp::new),
            entry(TEXT_AND_INPUT, "Tags Field", "tags-field", TagsFieldApp::new),
            entry(TEXT_AND_INPUT, "Text View", "text-view", TextViewApp::new),

            // --- Lists & Tables -------------------------------------------------
            entry(LISTS_AND_TABLES, "Advanced Table View", "advanced-table-view", TableViewExample::new),
            entry(LISTS_AND_TABLES, "Autoscroll List View", "autoscroll-list-view", null),
            entry(LISTS_AND_TABLES, "Filter View", "filter-view", FilterViewApp::new),
            entry(LISTS_AND_TABLES, "Filter View (Simple)", "simple-filter-view", SimpleFilterViewApp::new),
            entry(LISTS_AND_TABLES, "Grid Table View", "grid-table-view", GridTableViewApp::new),
            entry(LISTS_AND_TABLES, "Multi Column List View", "multi-column-list-view", MultiColumnListViewApp::new),
            entry(LISTS_AND_TABLES, "Paging Controls", "paging-controls", PagingControlsApp::new),
            entry(LISTS_AND_TABLES, "Paging Grid Table View", "paging-grid-table-view", PagingGridTableViewApp::new),
            entry(LISTS_AND_TABLES, "Paging List View", "paging-list-view", PagingListViewApp::new),
            entry(LISTS_AND_TABLES, "Strip View", "strip-view", StripViewApp::new),

            // --- Layout ---------------------------------------------------------
            entry(LAYOUT, "Drawer Stack Pane", "drawer-stack-pane", DrawerStackPaneApp::new),
            entry(LAYOUT, "Hidden Sides Pane", "hidden-sides-pane", HiddenSidesPaneApp::new),
            entry(LAYOUT, "Loading Pane", "loading-pane", LoadingPaneApp::new),
            entry(LAYOUT, "Masked View", "masked-view", null),
            entry(LAYOUT, "Power Pane", "power-pane", PowerPaneApp::new),
            entry(LAYOUT, "Responsive Pane", "responsive-pane", ResponsivePaneApp::new),
            entry(LAYOUT, "Skeleton", "skeleton", SkeletonPaneApp::new),
            entry(LAYOUT, "Spacer", "spacer", SpacerApp::new),
            entry(LAYOUT, "Stretching Tile Pane", "stretching-tile-pane", StretchingTilePaneApp::new),
            entry(LAYOUT, "Three Items Pane", "three-items-pane", ThreeItemsPaneApp::new),

            // --- Media & Graphics -----------------------------------------------
            entry(MEDIA_AND_GRAPHICS, "Avatar View", "avatar-view", AvatarViewApp::new),
            entry(MEDIA_AND_GRAPHICS, "Before / After View", "before-after-view", BeforeAfterViewApp::new),
            entry(MEDIA_AND_GRAPHICS, "Payment Option View", "payment-option-view", PaymentOptionApp::new),
            entry(MEDIA_AND_GRAPHICS, "Photo View", "photo-view", PhotoViewApp::new),
            entry(MEDIA_AND_GRAPHICS, "Segmented Bar", "segmented-bar", SegmentedBarApp::new),
            entry(MEDIA_AND_GRAPHICS, "SVG Image View", "svg-image-view", SVGImageViewApp::new),

            // --- Progress -------------------------------------------------------
            entry(PROGRESS, "Arc Progress Indicator", "arc-progress-indicator", null),
            entry(PROGRESS, "Circle Progress Indicator", "circle-progress-indicator", CircleProgressIndicatorApp::new),
            entry(PROGRESS, "Semi-Circle Progress Indicator", "semi-circle-progress-indicator", SemiCircleProgressIndicatorApp::new),

            // --- Overlays & Dialogs ---------------------------------------------
            entry(OVERLAYS_AND_DIALOGS, "Dialog Pane", "dialog-pane", DialogPaneApp::new),
            entry(OVERLAYS_AND_DIALOGS, "Glass Pane", "glass-pane", null),
            entry(OVERLAYS_AND_DIALOGS, "Info Center Pane", "info-center-pane", InfoCenterApp::new),
            entry(OVERLAYS_AND_DIALOGS, "Pop Over", "pop-over", PopOverApp::new),

            // --- Utilities ------------------------------------------------------
            entry(UTILITIES, "Screens View", "screens-view", ScreensViewApp::new),
            entry(UTILITIES, "Tree Node View", "tree-node-view", TreeNodeViewApp::new)
    );

    private ShowcaseRegistry() {
    }

    private static ShowcaseEntry entry(String category, String name, String manual, Supplier<Application> factory) {
        return new ShowcaseEntry(category, name, manual, factory);
    }

    /**
     * Groups the given entries by their category, preserving the order in which the categories
     * appear inside {@link #ALL_ENTRIES}.
     *
     * @param entries the entries to group
     * @return a map of category names to the entries belonging to that category
     */
    public static Map<String, List<ShowcaseEntry>> groupByCategory(List<ShowcaseEntry> entries) {
        Map<String, List<ShowcaseEntry>> result = new LinkedHashMap<>();
        entries.forEach(entry -> result.computeIfAbsent(entry.category(), category -> new java.util.ArrayList<>()).add(entry));
        return result;
    }

    /**
     * Looks up an entry by its display name.
     *
     * @param name the name of the control
     * @return the matching entry or {@code null}
     */
    public static ShowcaseEntry findByName(String name) {
        return ALL_ENTRIES.stream().filter(entry -> entry.name().equals(name)).findFirst().orElse(null);
    }
}
