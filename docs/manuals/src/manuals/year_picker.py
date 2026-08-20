"""Content of the YearPicker developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "year-picker"

MANUAL = Manual(
    control="YearPicker",
    package="com.dlsc.gemsfx",
    subtitle="An editable combo-box for selecting a year",
    abstract=("YearPicker combines a numeric TextField editor with a popup YearView. It is compact, keyboard-friendly and suitable for forms that need a single year value."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Generated cartoon overview of YearPicker.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>YearPicker</b> is part of the GemsFX module <font face='Courier'>com.dlsc.gemsfx</font>. This manual documents only behavior verified in the control, skin, CSS, resource bundles and demo app."),
            Section("Key features"),
            Bullets(['Extends CustomComboBox<Year> and is editable by default.', 'Editor accepts numeric input and truncates to four digits.', 'Popup content is a YearView bound bidirectionally to value.', 'Arrow keys adjust the selected year while editing.', 'Read-only year property exposes the selected year as Integer.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("Create the control, set the properties that define its valid values, and listen to the value or selection property that the control owns."),
            Code("""YearPicker picker = new YearPicker();
picker.setValue(Year.of(2026));
picker.getYearView().setEarliestYear(Year.of(2000));
picker.getYearView().setLatestYear(Year.of(2030));

picker.valueProperty().addListener((obs, oldYear, newYear) -> {
    System.out.println("year = " + newYear);
});""", caption="A compact setup for <font face='Courier'>YearPicker</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control in a simple application window."),
        ]),
        Chapter("Anatomy", [
            Para("The skin builds the visible nodes below the public control. The table lists the style classes and nodes that are useful when reading the source or writing CSS."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Style / node", "Description"], [
                        ["Root", "year-picker text-input", "CustomComboBox root style classes."],
                        ["Box", "box", "HBox containing editor and trigger button."],
                        ["Editor", "TextField", "Numeric editor returned by getEditor()."],
                        ["Edit button", "edit-button", "Calendar icon button that shows or hides the popup."],
                        ["Popup", "YearView", "Created lazily by getYearView() and bound to value."],
                        ["Year property", "year", "Read-only Integer mirror of the selected value."]
            ], widths=[18, 28, 54]),
        ]),
        Chapter("Control API", [
            Section("Selection and editor"),
            PropertyTable([
                        Property("value", "ObjectProperty&lt;Year&gt;", "Year.now()", "Inherited ComboBoxBase value; updated by editor commits and YearView selection."),
                        Property("year", "ReadOnlyObjectProperty&lt;Integer&gt;", "current year", "Integer mirror of value, or null when value is null."),
                        Property("editor", "TextField", "created in constructor", "Text field used for manual input; available through getEditor()."),
                        Property("editable", "BooleanProperty", "true", "Inherited from ComboBoxBase; bound to editor.editableProperty().")
            ]),
            Section("Popup and button"),
            PropertyTable([
                        Property("buttonDisplay", "ObjectProperty&lt;CustomComboBox.ButtonDisplay&gt;", "RIGHT", "Inherited styleable property controlling LEFT, RIGHT, BUTTON_ONLY or FIELD_ONLY button placement."),
                        Property("yearView", "YearView", "lazy", "Popup view returned by getYearView(); configure its rows, columns and bounds before showing if needed.")
            ]),
        ]),
        Chapter("Behaviour", [
            Section("Committing text"),
            Para("The editor commits on action, focus loss and touch-press-before-show. Non-blank parseable text becomes Year.of(value); blank text leaves the current value unchanged."),
            Figure(f"{G}/behaviour.svg", "Interaction and value-flow behaviour."),
            Section("Keyboard adjustment"),
            Para("UP subtracts one year and DOWN adds one year. When the picker is not editable, LEFT also subtracts and RIGHT adds."),
            Section("Popup selection"),
            Para("Selecting a different year in the popup updates value and hides the popup through YearPickerSkin."),
        ]),
        Chapter("Styling", [
            Para("The stylesheet is loaded by the control or its base class. The rows below list style hooks verified in the CSS and skin source."),
            Figure(f"{G}/styling.svg", "Style classes and pseudo-class states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["year-picker", "Root style class."],
                        ["text-input", "Additional root style class set by the constructor."],
                        ["box", "HBox containing editor and button."],
                        ["edit-button", "Calendar trigger button."],
                        ["edit-icon / ikonli-font-icon", "Calendar icon inside the button."],
                        ["year-view", "Popup YearView, styled by year-view.css and shadowed when inside the picker."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class / marker", "Meaning"], [
                        ["left", "buttonDisplay = LEFT."],
                        ["right", "buttonDisplay = RIGHT; active by default."],
                        ["button-only", "buttonDisplay = BUTTON_ONLY."],
                        ["field-only", "buttonDisplay = FIELD_ONLY."],
                        ["focused", "Mirrors editor focus for picker styling."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["-fx-button-display", "ButtonDisplay", "RIGHT", "Inherited from CustomComboBox. Valid enum values: LEFT, RIGHT, BUTTON_ONLY, FIELD_ONLY."]
            ], widths=[28,18,20,34]),
            Code(""".year-picker {
    -fx-button-display: left;
}
.year-picker > .box > .edit-button > .ikonli-font-icon {
    -fx-icon-color: -fx-accent;
}""", caption="Example CSS."),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets AccessibleRole.COMBO_BOX and binds accessible text to value.toString(), or null when no year is selected."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section("Open the popup from another button"),
            Code("""Button button = new Button("Choose year");
button.setOnAction(evt -> picker.show());"""),
            Section("Hide the trigger button"),
            Code("""picker.setButtonDisplay(CustomComboBox.ButtonDisplay.FIELD_ONLY);"""),
            Section("Use popup bounds"),
            Code("""picker.getYearView().setEarliestYear(Year.of(1990));
picker.getYearView().setLatestYear(Year.of(2050));"""),
            Section("Read the integer year"),
            Code("""Integer year = picker.getYear();"""),
            Section("Checklist"),
            Numbered(['Configure getYearView() before first show when you need bounds.', 'Do not expect more than four digits in the editor.', 'Use valueProperty for Year and yearProperty for Integer.', 'Remember popup selection hides the popup automatically.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>YearPickerApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.YearPickerApp"),
            Bullets([
                "Related GemsFX controls: YearView, YearMonthView, YearMonthPicker.",
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
