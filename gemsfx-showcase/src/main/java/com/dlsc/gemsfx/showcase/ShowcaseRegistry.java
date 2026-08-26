package com.dlsc.gemsfx.showcase;

import com.dlsc.gemsfx.demo.ArcProgressIndicatorApp;
import com.dlsc.gemsfx.demo.AutoscrollListViewApp;
import com.dlsc.gemsfx.demo.AvatarViewApp;
import com.dlsc.gemsfx.demo.BeforeAfterViewApp;
import com.dlsc.gemsfx.demo.CalendarPickerApp;
import com.dlsc.gemsfx.demo.CalendarViewApp;
import com.dlsc.gemsfx.demo.CircleProgressIndicatorApp;
import com.dlsc.gemsfx.demo.ChipViewApp;
import com.dlsc.gemsfx.demo.ChipsViewContainerApp;
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
import com.dlsc.gemsfx.demo.GlassPaneApp;
import com.dlsc.gemsfx.demo.GridTableViewApp;
import com.dlsc.gemsfx.demo.HiddenSidesPaneApp;
import com.dlsc.gemsfx.demo.HistoryManagerApp;
import com.dlsc.gemsfx.demo.InfoCenterApp;
import com.dlsc.gemsfx.demo.LimitedTextAreaApp;
import com.dlsc.gemsfx.demo.LoadingPaneApp;
import com.dlsc.gemsfx.demo.MaskedViewApp;
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

/**
 * The registry of all controls presented by the showcase application. Every entry is backed by
 * one of the PDF manuals found in the "docs/manuals" folder of the project. Entries may or may
 * not have a demo application attached to them.
 */
public final class ShowcaseRegistry {

    public static final String CALENDAR = ShowcaseBundle.get("category.calendar");
    public static final String TEXT_AND_INPUT = ShowcaseBundle.get("category.text");
    public static final String LISTS_AND_TABLES = ShowcaseBundle.get("category.lists");
    public static final String LAYOUT = ShowcaseBundle.get("category.layout");
    public static final String MEDIA_AND_GRAPHICS = ShowcaseBundle.get("category.media");
    public static final String PROGRESS = ShowcaseBundle.get("category.progress");
    public static final String OVERLAYS_AND_DIALOGS = ShowcaseBundle.get("category.overlays");
    public static final String UTILITIES = ShowcaseBundle.get("category.utilities");

    /**
     * All controls of the showcase, in category / alphabetical order.
     */
    public static final List<ShowcaseEntry> ALL_ENTRIES = List.of(

            // --- Calendar & Date / Time -----------------------------------------
            entry(CALENDAR, "Calendar Picker", "calendar-picker", CalendarPickerApp.class),
            entry(CALENDAR, "Calendar View", "calendar-view", CalendarViewApp.class),
            entry(CALENDAR, "Date Range Picker", "date-range-picker", DateRangePickerApp.class),
            entry(CALENDAR, "Date Range View", "date-range-view", DateRangeViewApp.class),
            entry(CALENDAR, "Day Of Week Picker", "day-of-week-picker", DayOfWeekPickerApp.class),
            entry(CALENDAR, "Duration Picker", "duration-picker", DurationPickerApp.class),
            entry(CALENDAR, "Time Picker", "time-picker", TimePickerApp.class),
            entry(CALENDAR, "Time Range Picker", "time-range-picker", TimeRangePickerApp.class),
            entry(CALENDAR, "Year Month Picker", "year-month-picker", YearMonthPickerApp.class),
            entry(CALENDAR, "Year Month View", "year-month-view", YearMonthViewApp.class),
            entry(CALENDAR, "Year Picker", "year-picker", YearPickerApp.class),
            entry(CALENDAR, "Year View", "year-view", YearViewApp.class),

            // --- Text & Input ---------------------------------------------------
            entry(TEXT_AND_INPUT, "Chip View", "chip-view", ChipViewApp.class),
            entry(TEXT_AND_INPUT, "Chips View Container", "chips-view-container", ChipsViewContainerApp.class),
            entry(TEXT_AND_INPUT, "Email Field", "email-field", EmailFieldApp.class),
            entry(TEXT_AND_INPUT, "Enhanced Label", "enhanced-label", EnhancedLabelApp.class),
            entry(TEXT_AND_INPUT, "Enhanced Password Field", "enhanced-password-field", EnhancedPasswordFieldApp.class),
            entry(TEXT_AND_INPUT, "Expanding Text Area", "expanding-text-area", ExpandingTextAreaApp.class),
            entry(TEXT_AND_INPUT, "History Button", "history-button", HistoryManagerApp.class),
            entry(TEXT_AND_INPUT, "Limited Text Area", "limited-text-area", LimitedTextAreaApp.class),
            entry(TEXT_AND_INPUT, "Resizable Text Area", "resizable-text-area", ResizableTextAreaApp.class),
            entry(TEXT_AND_INPUT, "Search Field", "search-field", SearchFieldApp.class),
            entry(TEXT_AND_INPUT, "Search Text Field", "search-text-field", SearchTextFieldApp.class),
            entry(TEXT_AND_INPUT, "Selection Box", "selection-box", SelectionBoxApp.class),
            entry(TEXT_AND_INPUT, "Tags Field", "tags-field", TagsFieldApp.class),
            entry(TEXT_AND_INPUT, "Text View", "text-view", TextViewApp.class),

            // --- Lists & Tables -------------------------------------------------
            entry(LISTS_AND_TABLES, "Advanced Table View", "advanced-table-view", TableViewExample.class),
            entry(LISTS_AND_TABLES, "Autoscroll List View", "autoscroll-list-view", AutoscrollListViewApp.class),
            entry(LISTS_AND_TABLES, "Filter View", "filter-view", FilterViewApp.class),
            entry(LISTS_AND_TABLES, "Filter View (Simple)", "simple-filter-view", SimpleFilterViewApp.class),
            entry(LISTS_AND_TABLES, "Grid Table View", "grid-table-view", GridTableViewApp.class),
            entry(LISTS_AND_TABLES, "Multi Column List View", "multi-column-list-view", MultiColumnListViewApp.class),
            entry(LISTS_AND_TABLES, "Paging Controls", "paging-controls", PagingControlsApp.class),
            entry(LISTS_AND_TABLES, "Paging Grid Table View", "paging-grid-table-view", PagingGridTableViewApp.class),
            entry(LISTS_AND_TABLES, "Paging List View", "paging-list-view", PagingListViewApp.class),
            entry(LISTS_AND_TABLES, "Strip View", "strip-view", StripViewApp.class),

            // --- Layout ---------------------------------------------------------
            entry(LAYOUT, "Drawer Stack Pane", "drawer-stack-pane", DrawerStackPaneApp.class),
            entry(LAYOUT, "Hidden Sides Pane", "hidden-sides-pane", HiddenSidesPaneApp.class),
            entry(LAYOUT, "Loading Pane", "loading-pane", LoadingPaneApp.class),
            entry(LAYOUT, "Masked View", "masked-view", MaskedViewApp.class),
            entry(LAYOUT, "Power Pane", "power-pane", PowerPaneApp.class),
            entry(LAYOUT, "Responsive Pane", "responsive-pane", ResponsivePaneApp.class),
            entry(LAYOUT, "Skeleton", "skeleton", SkeletonPaneApp.class),
            entry(LAYOUT, "Spacer", "spacer", SpacerApp.class),
            entry(LAYOUT, "Stretching Tile Pane", "stretching-tile-pane", StretchingTilePaneApp.class),
            entry(LAYOUT, "Three Items Pane", "three-items-pane", ThreeItemsPaneApp.class),

            // --- Media & Graphics -----------------------------------------------
            entry(MEDIA_AND_GRAPHICS, "Avatar View", "avatar-view", AvatarViewApp.class),
            entry(MEDIA_AND_GRAPHICS, "Before / After View", "before-after-view", BeforeAfterViewApp.class),
            entry(MEDIA_AND_GRAPHICS, "Payment Option View", "payment-option-view", PaymentOptionApp.class),
            entry(MEDIA_AND_GRAPHICS, "Photo View", "photo-view", PhotoViewApp.class),
            entry(MEDIA_AND_GRAPHICS, "Segmented Bar", "segmented-bar", SegmentedBarApp.class),
            entry(MEDIA_AND_GRAPHICS, "SVG Image View", "svg-image-view", SVGImageViewApp.class),

            // --- Progress -------------------------------------------------------
            entry(PROGRESS, "Arc Progress Indicator", "arc-progress-indicator", ArcProgressIndicatorApp.class),
            entry(PROGRESS, "Circle Progress Indicator", "circle-progress-indicator", CircleProgressIndicatorApp.class),
            entry(PROGRESS, "Semi-Circle Progress Indicator", "semi-circle-progress-indicator", SemiCircleProgressIndicatorApp.class),

            // --- Overlays & Dialogs ---------------------------------------------
            entry(OVERLAYS_AND_DIALOGS, "Dialog Pane", "dialog-pane", DialogPaneApp.class),
            entry(OVERLAYS_AND_DIALOGS, "Glass Pane", "glass-pane", GlassPaneApp.class),
            entry(OVERLAYS_AND_DIALOGS, "Info Center Pane", "info-center-pane", InfoCenterApp.class),
            entry(OVERLAYS_AND_DIALOGS, "Pop Over", "pop-over", PopOverApp.class),

            // --- Utilities ------------------------------------------------------
            entry(UTILITIES, "Screens View", "screens-view", ScreensViewApp.class),
            entry(UTILITIES, "Tree Node View", "tree-node-view", TreeNodeViewApp.class)
    );

    private ShowcaseRegistry() {
    }

    private static ShowcaseEntry entry(String category, String name, String manual, Class<? extends Application> demoClass) {
        return new ShowcaseEntry(category, name, manual, demoClass);
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
