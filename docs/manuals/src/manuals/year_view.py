"""Content of the YearView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "year-view"

MANUAL = Manual(
    control="YearView",
    package="com.dlsc.gemsfx",
    subtitle="A paged grid for selecting java.time.Year values",
    abstract=("YearView displays a configurable grid of years, marks the current and selected year, and pages through year ranges with arrow buttons."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of YearView.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>YearView</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Selects a java.time.Year value, initialized to Year.now().', 'Read-only year property exposes the selected year as an int or -1 when value is null.', 'Rows and columns define how many years are shown per page.', 'Optional earliestYear and latestYear disable out-of-range years.', 'The skin shows a range label such as 2020-2039.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""YearView view = new YearView();
view.setCols(4);
view.setRows(5);
view.setEarliestYear(Year.of(2000));
view.setLatestYear(Year.now().plusYears(5));

view.valueProperty().addListener((obs, oldYear, newYear) -> {
    System.out.println("selected year = " + newYear);
});""", caption="A compact setup for <font face='Courier'>YearView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "year-view", "Control root and stylesheet hook."],
                        ["Header", "header", "Left button, year-range label and right button."],
                        ["Range label", "year-range-label", "Shows first and last visible year on the current page."],
                        ["Grid", "grid-pane", "GridPane with rows x cols equally sized cells."],
                        ["Year box", "year-box", "Selectable VBox for one year."],
                        ["Selection indicator", "selection-indicator", "Small line shown when the box matches value/year."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("Selection"),
            PropertyTable([
                        Property("value", "ObjectProperty&lt;Year&gt;", "Year.now()", "Selected year."),
                        Property("year", "ReadOnlyIntegerProperty", "current year", "Integer value derived from value, or -1 if value is null."),
                        Property("earliestYear", "ObjectProperty&lt;Year&gt;", "null", "Earliest selectable year; earlier cells are disabled."),
                        Property("latestYear", "ObjectProperty&lt;Year&gt;", "null", "Latest selectable year; later cells are disabled.")
            ]),
            Section("Grid"),
            PropertyTable([
                        Property("cols", "IntegerProperty", "4", "Number of year columns. Must be at least 1. Styleable via -fx-cols."),
                        Property("rows", "IntegerProperty", "5", "Number of year rows. Must be at least 1. Styleable via -fx-rows.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Paged ranges"),
            Para("The first visible year is calculated from the selected year or the current year, the visible cell count, and an internal page offset. Clicking arrows increments or decrements the offset."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Range constraints"),
            Para("Year boxes bind their disabled state to earliestYear and latestYear. Clicking a disabled box is prevented by JavaFX disabled-node behavior."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["year-view", "Root control style class."],
                        ["popover", "Optional root class for popup-specific padding and border."],
                        ["header", "Navigation row."],
                        ["year-range-label", "Header label showing the visible range."],
                        ["arrow-button left-button right-button", "Navigation buttons."],
                        ["arrow left-arrow right-arrow", "Arrow glyph regions."],
                        ["grid-pane", "GridPane containing year boxes."],
                        ["year-box", "Selectable year cell."],
                        ["year-label", "Label for the year number."],
                        ["selection-indicator", "Indicator below selected year."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["focused", "Border switches to -fx-accent."],
                        ["selected", "Style class, not pseudo class, added to selected year-box."],
                        ["current", "Style class, not pseudo class, added to current year-box."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-cols", "Integer", "4", "Number of columns; positive integers only."],
                        ["-fx-rows", "Integer", "5", "Number of rows; positive integers only."]
            ], widths=[28,18,20,34]),
            Code(""".year-view {
    -fx-cols: 3;
    -fx-rows: 4;
}
.year-view > .grid-pane > .year-box > .selection-indicator {
    -fx-background-color: -fx-accent;
}""", caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("This control uses <font face='Courier'>ResourceBundleManager</font> for the following keys."),
            Table(["Key", "English default"], [
                        ["accessible.role-description", "year selector"]
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets AccessibleRole.DATE_PICKER through AccessibilityUtil.setRole and uses the localized role description \"year selector\"."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Show twelve years per page"),
            Code("""YearView view = new YearView();
view.setCols(3);
view.setRows(4);"""),
            Section("Restrict to a product lifetime"),
            Code("""view.setEarliestYear(Year.of(2020));
view.setLatestYear(Year.of(2030));"""),
            Section("Mirror the integer year"),
            Code("""Label label = new Label();
label.textProperty().bind(Bindings.convert(view.yearProperty()));"""),
            Section("Clear the selected value"),
            Code("""view.setValue(null); // yearProperty becomes -1"""),
            Section("Checklist"),
            Numbered(['Keep cols and rows >= 1.', 'Use earliestYear/latestYear for selectable limits.', 'Remember that selected/current are style classes on cells.', 'Use the value property for Year objects and yearProperty for int display.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>YearViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.YearViewApp"),
            Bullets([
                "Related GemsFX controls: YearPicker, YearMonthView, YearMonthPicker.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
