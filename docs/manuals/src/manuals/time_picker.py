"""Content of the TimePicker developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "time-picker"

MANUAL = Manual(
    control="TimePicker",
    package="com.dlsc.gemsfx",
    subtitle="An editable combo-box for java.time.LocalTime",
    abstract=("TimePicker edits a LocalTime through separate hour, minute, second and millisecond fields plus an optional popup of list views. It supports bounds, minute step sizes and linked rollover behavior."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of TimePicker.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>TimePicker</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Supports HOURS_MINUTES, HOURS_MINUTES_SECONDS and HOURS_MINUTES_SECONDS_MILLIS formats.', 'Bounds input with earliestTime and latestTime.', 'Minute field can step by 1 to 60 minutes.', 'Fields can roll over and link to adjacent fields.', 'Popup trigger button can be shown, hidden or repositioned through CustomComboBox.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""TimePicker picker = new TimePicker();
picker.setFormat(TimePicker.Format.HOURS_MINUTES_SECONDS);
picker.setEarliestTime(LocalTime.of(8, 0));
picker.setLatestTime(LocalTime.of(18, 0));
picker.setStepRateInMinutes(15);

picker.timeProperty().addListener((obs, oldTime, newTime) -> {
    System.out.println("time = " + newTime);
});""", caption="A compact setup for <font face='Courier'>TimePicker</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "time-picker text-input", "CustomComboBox root style classes."],
                        ["Box", "box", "HBox containing fields, spacer and edit button."],
                        ["Fields", "fields-box", "Hour, minute, optional second and optional millisecond fields."],
                        ["Separators", "separator", "Nodes between fields; defaults are \":\", \":\" and \".\"."],
                        ["Edit button", "edit-button", "Clock icon trigger for popup."],
                        ["Popup", "time-picker-popup", "HBox containing hour/minute/second/millisecond ListViews."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("Value and bounds"),
            PropertyTable([
                        Property("time", "ObjectProperty&lt;LocalTime&gt;", "LocalTime.now() at construction", "Current time. setTime truncates seconds/nanos according to format."),
                        Property("earliestTime", "ObjectProperty&lt;LocalTime&gt;", "LocalTime.MIN", "Earliest allowed time; must not be after latestTime."),
                        Property("latestTime", "ObjectProperty&lt;LocalTime&gt;", "LocalTime.MAX", "Latest allowed time; must not be before earliestTime."),
                        Property("adjusted", "ReadOnlyBooleanProperty", "false", "True after adjust() changed the value to satisfy bounds or step size.")
            ]),
            Section("Format and editing"),
            PropertyTable([
                        Property("format", "ObjectProperty&lt;TimePicker.Format&gt;", "HOURS_MINUTES", "Controls visible fields and truncation: minutes only, seconds, or milliseconds."),
                        Property("stepRateInMinutes", "IntegerProperty", "1", "Minute increment for arrow keys and popup minute list; valid 1 through 60."),
                        Property("clockType", "ObjectProperty&lt;ClockType&gt;", "TWENTY_FOUR_HOUR_CLOCK", "Styleable enum. Source contains TWELVE_HOUR_CLOCK but popup has TODO for AM/PM support."),
                        Property("linkingFields", "BooleanProperty", "true", "Rollover increments/decrements the previous field."),
                        Property("rollover", "BooleanProperty", "true", "Fields wrap at their min/max instead of clamping.")
            ]),
            Section("Popup and separators"),
            PropertyTable([
                        Property("showPopupTriggerButton", "BooleanProperty", "true", "Shows the edit button. Styleable."),
                        Property("buttonDisplay", "ObjectProperty&lt;CustomComboBox.ButtonDisplay&gt;", "RIGHT", "Inherited button placement."),
                        Property("onShowPopup", "ObjectProperty&lt;Consumer&lt;TimePicker&gt;&gt;", "picker -> show()", "Consumer invoked by F4/ENTER and default button behavior."),
                        Property("hoursSeparator", "ObjectProperty&lt;Node&gt;", "Label \":\"", "Node between hours and minutes."),
                        Property("minutesSeparator", "ObjectProperty&lt;Node&gt;", "Label \":\"", "Node between minutes and seconds."),
                        Property("secondsSeparator", "ObjectProperty&lt;Node&gt;", "Label \".\"", "Node between seconds and milliseconds.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Adjusting values"),
            Para("Calling adjust() first clamps the current time to earliestTime/latestTime, then snaps minutes to the nearest stepRateInMinutes. When a change happens, adjusted becomes true until the next editing cycle clears it."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Field navigation"),
            Para("Digit input replaces field text up to its digit width, BACK_SPACE removes a digit, SPACE/RIGHT moves to the next field, LEFT moves to the previous field, and UP/DOWN increment or decrement."),
            Section("Popup lists"),
            Para("The popup lists hours from earliestTime.hour to latestTime.hour, minutes by stepRateInMinutes, seconds 0-59 and milliseconds 0-999. It scrolls to the current selection when shown."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["time-picker", "Root style class."],
                        ["text-input", "Additional root style class."],
                        ["box", "Main HBox."],
                        ["fields-box", "Container for time fields and separators."],
                        ["time-field digits-field hour minute second millisecond", "Editable unit fields."],
                        ["separator", "Separator nodes."],
                        ["edit-button", "Clock trigger button."],
                        ["time-picker-popup", "Popup root."],
                        ["time-list-view hour-list minute-list second-list millisecond-list", "Popup lists."],
                        ["time-cell time-label", "Popup list cell and label."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["left/right/button-only/field-only", "Inherited from CustomComboBox buttonDisplay."],
                        ["focused", "Set while any unit field or edit button has focus."],
                        ["empty", "Applied to fields and separators when time is null."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-button-display", "ButtonDisplay", "RIGHT", "Inherited from CustomComboBox."],
                        ["-fx-show-popup-trigger-button", "Boolean", "true", "Shows the popup trigger button."],
                        ["-fx-linking-fields", "Boolean", "true", "Links rollover to the previous field."],
                        ["-fx-rollover", "Boolean", "true", "Wraps fields at boundaries."],
                        ["-fx-clock-type", "ClockType", "TWENTY_FOUR_HOUR_CLOCK", "TWENTY_FOUR_HOUR_CLOCK or TWELVE_HOUR_CLOCK."],
                        ["-fx-format", "Format", "HOURS_MINUTES", "Visible time fields."],
                        ["-fx-step-rate-in-minutes", "Integer", "1", "Minute step, 1 to 60."]
            ], widths=[28,18,20,34]),
            Code(""".time-picker {
    -fx-format: hours-minutes-seconds;
    -fx-step-rate-in-minutes: 15;
}
.time-picker > .box > .fields-box > .time-field:focused {
    -fx-background-color: -fx-accent;
}""", caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("This control uses <font face='Courier'>ResourceBundleManager</font> for the following keys."),
            Table(["Key", "English default"], [
                        ["format.separator.hour-minute", ":"],
                        ["format.separator.minute-second", ":"],
                        ["format.separator.second-fraction", "."]
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets AccessibleRole.COMBO_BOX and binds accessible text to time.toString(), or null when time is null."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Business-hours picker"),
            Code("""TimePicker picker = new TimePicker();
picker.setEarliestTime(LocalTime.of(8, 0));
picker.setLatestTime(LocalTime.of(18, 0));
picker.setStepRateInMinutes(15);"""),
            Section("Seconds precision"),
            Code("""picker.setFormat(TimePicker.Format.HOURS_MINUTES_SECONDS);"""),
            Section("Field-only editor"),
            Code("""picker.setButtonDisplay(CustomComboBox.ButtonDisplay.FIELD_ONLY);"""),
            Section("Custom separators"),
            Code("""picker.setHoursSeparator(new Label("h"));
picker.setMinutesSeparator(new Label("m"));"""),
            Section("Validate after programmatic update"),
            Code("""picker.setTime(LocalTime.now());
picker.adjust();
if (picker.isAdjusted()) {
    System.out.println("snapped to allowed value");
}"""),
            Section("Checklist"),
            Numbered(['Keep earliestTime <= latestTime.', 'Keep stepRateInMinutes between 1 and 60.', 'Call adjust() after programmatic changes if you need snapping immediately.', 'Use format to control visible fields and truncation.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>TimePickerApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.TimePickerApp"),
            Bullets([
                "Related GemsFX controls: DurationPicker, TimeRangePicker.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
