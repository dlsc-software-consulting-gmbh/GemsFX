"""Content of the TimeRangePicker developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "time-range-picker"

MANUAL = Manual(
    control="TimeRangePicker",
    package="com.dlsc.gemsfx",
    subtitle="A SelectionBox specialized for LocalTime ranges",
    abstract=("TimeRangePicker is a SelectionBox of TimeRange items. It ships with common two-hour ranges, clear/select-all popup buttons and display text that merges consecutive selected ranges."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of TimeRangePicker.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>TimeRangePicker</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Extends SelectionBox<TimeRangePicker.TimeRange>.', 'Default items are 10:00-12:00, 12:00-14:00, 14:00-16:00 and 16:00-18:00.', 'Supports SINGLE and MULTIPLE selection through the selection model.', 'Clear and Select All buttons are added to the popup top area.', 'Selected consecutive ranges are merged in the displayed text.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""TimeRangePicker picker = new TimeRangePicker(
    new TimeRangePicker.TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0)),
    new TimeRangePicker.TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)),
    new TimeRangePicker.TimeRange(LocalTime.of(13, 0), LocalTime.of(15, 0))
);
picker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
picker.getSelectionModel().selectIndices(0, 1);""", caption="A compact setup for <font face='Courier'>TimeRangePicker</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "combo-box-base combo-box selection-box time-range-picker", "SelectionBox root plus TimeRangePicker style class."],
                        ["Display label", "display-label", "Shows prompt text or converted selected ranges."],
                        ["Arrow button", "arrow-button / arrow", "Opens the SelectionPopup."],
                        ["Top area", "extra-buttons-box", "Clear and Select All buttons."],
                        ["Options", "options-box", "Radio buttons in SINGLE mode or check boxes in MULTIPLE mode."],
                        ["TimeRange", "startTime/endTime", "Immutable value object; constructor swaps endpoints if start is after end."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("TimeRangePicker additions"),
            PropertyTable([
                        Property("items", "ListProperty&lt;TimeRange&gt;", "four default ranges", "Inherited items list populated by the constructor."),
                        Property("selectedItemsConverter", "ObjectProperty&lt;StringConverter&lt;List&lt;TimeRange&gt;&gt;&gt;", "merging converter", "Formats selected ranges; multiple consecutive ranges are merged."),
                        Property("top", "ObjectProperty&lt;Node&gt;", "extra buttons box", "Popup top node with Clear and Select All buttons.")
            ]),
            Section("SelectionBox API"),
            PropertyTable([
                        Property("selectionModel", "ObjectProperty&lt;MultipleSelectionModel&lt;TimeRange&gt;&gt;", "CustomMultipleSelectionModel", "Controls selected item(s) and SelectionMode."),
                        Property("currentSelectionMode", "ReadOnlyObjectProperty&lt;SelectionMode&gt;", "selection model mode", "Convenience read-only mirror of the model mode."),
                        Property("promptText", "StringProperty", "null", "Text shown when no range is selected."),
                        Property("itemConverter", "ObjectProperty&lt;StringConverter&lt;TimeRange&gt;&gt;", "null", "Formats individual ranges for popup and fallback display."),
                        Property("graphic", "ObjectProperty&lt;Node&gt;", "null", "Optional graphic in the display label."),
                        Property("placeholder", "ObjectProperty&lt;Node&gt;", "null", "Shown in popup when items is empty."),
                        Property("autoHideOnSelection", "BooleanProperty", "true", "Controls whether popup actions hide the popup."),
                        Property("readOnly", "BooleanProperty", "false", "Prevents interaction and hides arrow when true. Styleable."),
                        Property("animationEnabled", "BooleanProperty", "false", "Fades popup open/closed when true. Styleable.")
            ]),
            Section("TimeRange value object"),
            PropertyTable([
                        Property("startTime", "LocalTime", "constructor argument", "Start of the range."),
                        Property("endTime", "LocalTime", "constructor argument", "End of the range."),
                        Property("toString()", "String", "start + \" ~ \" + end", "Default text when no item converter is installed.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Selection modes"),
            Para("SINGLE mode creates radio buttons and hides the Select All button. MULTIPLE mode creates check boxes and shows Select All."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Merging display text"),
            Para("For multiple selections, selected ranges are sorted by start time. Adjacent ranges whose endTime equals the next startTime are merged before display."),
            Section("Popup lifecycle"),
            Para("SelectionBox exposes show(), hide() and WindowEvent handlers for showing, shown, hiding and hidden. The popup auto-fixes, auto-hides and hides on ESCAPE."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["time-range-picker", "Additional root class set by TimeRangePicker."],
                        ["selection-box combo-box combo-box-base", "Inherited SelectionBox root classes."],
                        ["display-label", "Main display label."],
                        ["arrow-button arrow", "Popup arrow."],
                        ["selection-popup", "Popup root."],
                        ["content", "Popup BorderPane."],
                        ["extra-buttons-box extra-button clear-button select-all-button", "Quick action buttons."],
                        ["options-scroll-pane options-box", "Scrollable options container."],
                        ["item single-item multiple-item item-N", "Radio/check option classes."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["empty", "No selected item(s)."],
                        ["showing", "Popup is showing."],
                        ["single", "Selection mode is SINGLE."],
                        ["multiple", "Selection mode is MULTIPLE."],
                        ["readonly", "Read-only state in SelectionBoxSkin source."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-read-only", "Boolean", "false", "Inherited from SelectionBox."],
                        ["-fx-animation-enabled", "Boolean", "false", "Inherited from SelectionBox."]
            ], widths=[28,18,20,34]),
            Code(""".time-range-picker {
    -fx-read-only: false;
    -fx-animation-enabled: true;
}
.selection-popup > .content .extra-buttons-box .clear-button {
    -fx-font-weight: bold;
}""", caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("This control uses <font face='Courier'>ResourceBundleManager</font> for the following keys."),
            Table(["Key", "English default"], [
                        ["action.clear", "Clear"],
                        ["action.select-all", "Select All"]
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para("SelectionBox sets AccessibleRole.COMBO_BOX and binds accessible text to the selected ranges, or promptText when the selection is empty. TimeRangePicker also sets AccessibleRole.COMBO_BOX."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Use custom ranges"),
            Code("""TimeRangePicker picker = new TimeRangePicker(
    new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)),
    new TimeRangePicker.TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0))
);"""),
            Section("Enable multiple selection"),
            Code("""picker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);"""),
            Section("Customize range text"),
            Code("""picker.setItemConverter(new StringConverter<>() {
    @Override public String toString(TimeRangePicker.TimeRange range) {
        return range.startTime() + " - " + range.endTime();
    }
    @Override public TimeRangePicker.TimeRange fromString(String text) { return null; }
});"""),
            Section("Set a placeholder"),
            Code("""picker.setPromptText("Choose a time range");"""),
            Section("Read selected ranges"),
            Code("""List<TimeRangePicker.TimeRange> ranges =
    new ArrayList<>(picker.getSelectionModel().getSelectedItems());"""),
            Section("Checklist"),
            Numbered(['Use the selection model to switch SINGLE/MULTIPLE.', 'Consecutive selected ranges merge only for display text.', 'The TimeRange constructor requires non-null endpoints and swaps reversed endpoints.', 'There is no time-range-picker.css; styling comes from selection-box.css plus the extra root class.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>TimeRangePickerApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.TimeRangePickerApp"),
            Bullets([
                "Related GemsFX controls: TimePicker, DurationPicker, SelectionBox.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
