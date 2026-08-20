"""Content of the YearMonthView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "year-month-view"

MANUAL = Manual(
    control="YearMonthView",
    package="com.dlsc.gemsfx",
    subtitle="A two-column month grid for selecting YearMonth values",
    abstract=("YearMonthView displays the twelve months of a year as selectable cells, optionally bounded by earliest and latest months. It is useful as a standalone month selector and as popup content inside richer date controls."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of YearMonthView.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>YearMonthView</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Selects a java.time.YearMonth value, initialized to YearMonth.now().', 'Header with previous / next year buttons can be shown or hidden.', 'Month names are formatted by a StringConverter<Month>.', 'Optional earliestMonth and latestMonth disable months outside the valid range.', 'Selected and current months receive pseudo-class state in the skin.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""YearMonthView view = new YearMonthView();
view.setEarliestMonth(YearMonth.now().minusYears(1));
view.setLatestMonth(YearMonth.now());
view.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

view.valueProperty().addListener((obs, oldValue, newValue) -> {
    System.out.println("selected month = " + newValue);
});""", caption="A compact setup for <font face='Courier'>YearMonthView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "year-month-view", "Control root and stylesheet hook."],
                        ["Container", "container", "VBox containing the optional header and month grid."],
                        ["Header", "header", "Left arrow, year label and right arrow; hidden when showYear is false."],
                        ["Grid", "grid-pane", "Six rows and two month columns separated by a divider."],
                        ["Month cell", "month-box / month-label", "Selectable month label created from converter.toString(month)."],
                        ["Indicator", "indicator", "Visible for the selected month via the selected pseudo class."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("Selection"),
            PropertyTable([
                        Property("value", "ObjectProperty&lt;YearMonth&gt;", "YearMonth.now()", "Selected year and month. Programmatic changes are constrained to earliestMonth/latestMonth when those bounds exist."),
                        Property("earliestMonth", "ObjectProperty&lt;YearMonth&gt;", "null", "Earliest selectable month; earlier month boxes are disabled."),
                        Property("latestMonth", "ObjectProperty&lt;YearMonth&gt;", "null", "Latest selectable month; later month boxes are disabled.")
            ]),
            Section("Display"),
            PropertyTable([
                        Property("showYear", "BooleanProperty", "true", "Shows the year header. Styleable via -fx-show-year."),
                        Property("converter", "ObjectProperty&lt;StringConverter&lt;Month&gt;&gt;", "full month name for default Locale", "Formats the month names shown in each month box. fromString returns null in the default converter.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Bounded month selection"),
            Para("Changing value, earliestMonth or latestMonth keeps the selected value inside the allowed interval. The arrows are disabled at the boundary year and individual month boxes are disabled if their YearMonth is outside the range."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Month formatting"),
            Para("The skin creates one label per Month by calling the converter. Use this for abbreviations, uppercase month names or domain-specific labels."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["year-month-view", "Root control style class."],
                        ["container", "VBox wrapping header and grid."],
                        ["header", "Year navigation row."],
                        ["arrow-button left-button right-button", "Navigation buttons."],
                        ["arrow left-arrow right-arrow", "Arrow glyph regions."],
                        ["grid-pane", "GridPane containing month cells and divider."],
                        ["month-box", "VBox for one month."],
                        ["month-label", "Label created by converter."],
                        ["indicator", "Selection indicator below each month."],
                        ["divider", "Vertical divider between odd and even month columns."],
                        ["popover", "Optional root style class with popup-specific border/background tweaks."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["selected", "Applied to the selected month-box."],
                        ["current", "Applied to the current month-box when the displayed year is the current year."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-show-year", "Boolean", "true", "Shows or hides the header."]
            ], widths=[28,18,20,34]),
            Code(""".year-month-view {
    -fx-show-year: false;
}
.year-month-view > .container > .grid-pane .month-box:selected > .indicator {
    -fx-background-color: -fx-accent;
}""", caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("This control uses <font face='Courier'>ResourceBundleManager</font> for the following keys."),
            Table(["Key", "English default"], [
                        ["accessible.role-description", "month selector"]
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets AccessibleRole.DATE_PICKER through AccessibilityUtil.setRole and uses the localized role description \"month selector\"."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Limit to the last twelve months"),
            Code("""YearMonthView view = new YearMonthView();
view.setEarliestMonth(YearMonth.now().minusMonths(11));
view.setLatestMonth(YearMonth.now());"""),
            Section("Use abbreviated month names"),
            Code("""view.setConverter(new StringConverter<>() {
    @Override public String toString(Month month) {
        return month.getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
    @Override public Month fromString(String text) { return null; }
});"""),
            Section("Hide the year header"),
            Code("""view.setShowYear(false);"""),
            Section("Bind another control to the selection"),
            Code("""Label label = new Label();
label.textProperty().bind(Bindings.convert(view.valueProperty()));"""),
            Section("Checklist"),
            Numbered(['Set value before applying tight bounds if you want a specific initial month.', 'Use a converter for all visible month text.', 'Do not select disabled months in custom event handlers.', 'Keep the control at USE_PREF_SIZE when using it as compact popup content.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>YearMonthViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.YearMonthViewApp"),
            Bullets([
                "Related GemsFX controls: YearMonthPicker, YearPicker, YearView, CalendarView.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
