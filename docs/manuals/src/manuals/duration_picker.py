"""Content of the DurationPicker developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "duration-picker"

MANUAL = Manual(
    control="DurationPicker",
    package="com.dlsc.gemsfx",
    subtitle="An editable combo-box for java.time.Duration",
    abstract=("DurationPicker edits a Duration through configurable duration unit fields and a PickerFX popup. It supports minimum and maximum durations, labels, zero padding and linked rollover behavior."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of DurationPicker.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>DurationPicker</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Default duration is Duration.ZERO.', 'Constructor configures fields to DAYS, HOURS, MINUTES, SECONDS and MILLIS.', 'minimumDuration defaults to Duration.ZERO; maximumDuration is set to seven days by the constructor.', 'Fields can be relabeled, hidden, padded and reordered through the fields list.', 'Popup trigger button can be shown, hidden or repositioned through CustomComboBox.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""DurationPicker picker = new DurationPicker();
picker.setDuration(Duration.ofHours(2).plusMinutes(30));
picker.setMinimumDuration(Duration.ZERO);
picker.setMaximumDuration(Duration.ofDays(1));
picker.getFields().setAll(ChronoUnit.HOURS, ChronoUnit.MINUTES);

picker.durationProperty().addListener((obs, oldDuration, newDuration) -> {
    System.out.println("duration = " + newDuration);
});""", caption="A compact setup for <font face='Courier'>DurationPicker</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "duration-picker text-input", "CustomComboBox root style classes."],
                        ["Box", "box", "HBox containing fields, spacer and arrow button."],
                        ["Fields", "fields-box", "DurationUnitField labels for configured ChronoUnit values."],
                        ["Unit field", "unit-field days/hours/minutes/seconds/millis", "Focusable label that accepts digits and arrow keys."],
                        ["Separator", "separator", "Created by separatorFactory between adjacent units."],
                        ["Popup", "popup / picker", "PickerFX duration picker bound bidirectionally to duration."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("Value and limits"),
            PropertyTable([
                        Property("duration", "ObjectProperty&lt;Duration&gt;", "Duration.ZERO", "Current duration. Constrained when value/minimum/maximum changes."),
                        Property("minimumDuration", "ObjectProperty&lt;Duration&gt;", "Duration.ZERO", "Minimum allowed duration. Must not be null or negative."),
                        Property("maximumDuration", "ObjectProperty&lt;Duration&gt;", "Duration.ofDays(7) after constructor", "Maximum allowed duration; null means no upper limit.")
            ]),
            Section("Fields and labels"),
            PropertyTable([
                        Property("fields", "ListProperty&lt;ChronoUnit&gt;", "DAYS, HOURS, MINUTES, SECONDS, MILLIS after constructor", "Displayed units. Supported by field logic: WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLIS."),
                        Property("labelType", "ObjectProperty&lt;LabelType&gt;", "SHORT", "NONE, SHORT or LONG unit labels. Styleable."),
                        Property("fillDigits", "BooleanProperty", "true", "Pads hours/minutes/seconds to two digits and millis to three digits. Styleable."),
                        Property("separatorFactory", "ObjectProperty&lt;Callback&lt;Pair&lt;ChronoUnit, ChronoUnit&gt;, Node&gt;&gt;", "Label \":\"", "Creates separator nodes between fields.")
            ]),
            Section("Interaction and popup"),
            PropertyTable([
                        Property("linkingFields", "BooleanProperty", "true", "Rollover increments/decrements the previous field. Styleable."),
                        Property("rollover", "BooleanProperty", "true", "Fields wrap at boundaries instead of clamping. Styleable."),
                        Property("showPopupTriggerButton", "BooleanProperty", "true", "Shows the arrow button. Styleable."),
                        Property("buttonDisplay", "ObjectProperty&lt;CustomComboBox.ButtonDisplay&gt;", "RIGHT", "Inherited button placement."),
                        Property("onShowPopup", "ObjectProperty&lt;Consumer&lt;DurationPicker&gt;&gt;", "picker -> show()", "Consumer used to show alternate duration input UI.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Constraining durations"),
            Para("Whenever duration, minimumDuration or maximumDuration changes, the control clamps the duration to the configured interval. Negative minimum durations and null minimum durations throw IllegalArgumentException."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Unit field editing"),
            Para("Digit input edits the focused unit, BACK_SPACE removes a digit, SPACE/RIGHT moves forward, LEFT moves back, and UP/DOWN increment or decrement. Hours max at 23, minutes/seconds at 59, millis at 999."),
            Section("Popup binding"),
            Para("The popup creates a PickerFX DurationPicker whose value, fields, minimum and maximum are bound to the GemsFX control."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["duration-picker", "Root style class."],
                        ["text-input", "Additional root style class."],
                        ["box", "Main HBox."],
                        ["fields-box", "Container for fields and separators."],
                        ["unit-field no-label short-label long-label", "Field label and label-type classes."],
                        ["days hours minutes seconds millis", "Unit-specific classes on fields."],
                        ["separator", "Separator nodes."],
                        ["arrow-button arrow", "Popup trigger and icon region."],
                        ["popup", "Popup HBox."],
                        ["picker container segment segment-cell colon indicator shadow segment-separator", "PickerFX popup classes styled by duration-picker.css."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["left/right/button-only/field-only", "Inherited from CustomComboBox buttonDisplay."],
                        ["focused", "Used on arrow button and focused unit field."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-button-display", "ButtonDisplay", "RIGHT", "Inherited from CustomComboBox."],
                        ["-fx-show-popup-trigger-button", "Boolean", "true", "Shows the popup trigger button."],
                        ["-fx-linking-fields", "Boolean", "true", "Links rollover to the previous field."],
                        ["-fx-rollover", "Boolean", "true", "Wraps fields at boundaries."],
                        ["-fx-label-type", "LabelType", "SHORT", "NONE, SHORT or LONG labels."],
                        ["-fx-fill-digits", "Boolean", "true", "Pads lower units with leading zeroes."]
            ], widths=[28,18,20,34]),
            Code(""".duration-picker {
    -fx-label-type: long;
    -fx-fill-digits: true;
}
.duration-picker > .box > .fields-box > .unit-field:focused {
    -fx-background-color: -fx-accent;
}""", caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("This control uses <font face='Courier'>ResourceBundleManager</font> for the following keys."),
            Table(["Key", "English default"], [
                        ["unit.short.days", "d"],
                        ["unit.short.hours", "h"],
                        ["unit.short.minutes", "m"],
                        ["unit.short.seconds", "s"],
                        ["unit.short.millis", "ms"],
                        ["unit.long.days", "days"],
                        ["unit.long.hours", "hours"],
                        ["unit.long.minutes", "minutes"],
                        ["unit.long.seconds", "seconds"],
                        ["unit.long.millis", "millis"],
                        ["popup.unit.title.days", "Days"],
                        ["popup.unit.title.hours", "Hours"],
                        ["popup.unit.title.minutes", "Minutes"],
                        ["popup.unit.title.seconds", "Seconds"],
                        ["popup.unit.title.millis", "Millis"],
                        ["format.separator.time", ":"]
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets AccessibleRole.COMBO_BOX and binds accessible text to duration.toString(), or null when duration is null."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Hours and minutes only"),
            Code("""DurationPicker picker = new DurationPicker();
picker.getFields().setAll(ChronoUnit.HOURS, ChronoUnit.MINUTES);"""),
            Section("Cap to one work day"),
            Code("""picker.setMinimumDuration(Duration.ZERO);
picker.setMaximumDuration(Duration.ofHours(8));"""),
            Section("Use long labels"),
            Code("""picker.setLabelType(DurationPicker.LabelType.LONG);"""),
            Section("Remove leading zeroes"),
            Code("""picker.setFillDigits(false);"""),
            Section("Custom separator factory"),
            Code("""picker.setSeparatorFactory(pair -> new Label(" | "));"""),
            Section("Checklist"),
            Numbered(['Do not set minimumDuration to null or a negative value.', 'Set maximumDuration to null only when no upper limit is wanted.', 'Use ChronoUnit values supported by DurationUnitField.', 'Remember the constructor replaces the field list with five units.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>DurationPickerApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.DurationPickerApp"),
            Bullets([
                "Related GemsFX controls: TimePicker, TimeRangePicker.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
