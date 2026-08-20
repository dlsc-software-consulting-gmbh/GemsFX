from manualkit import (Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table)

G="date-range-picker"
MANUAL=Manual(control="DateRangePicker", package="com.dlsc.gemsfx.daterange", subtitle="A popup picker for titled DateRange values", abstract="DateRangePicker is a ComboBoxBase for DateRange objects. It summarizes the selected range in the button area and opens a DateRangeView with presets, two calendars and apply/cancel behaviour.", cover_svg=f"{G}/cover.svg", cover_caption="DateRangePicker opens a DateRangeView popup for choosing a start and end date.", chapters=[
Chapter("Introduction",[Para("<b>DateRangePicker</b> extends <font face='Courier'>ComboBoxBase&lt;DateRange&gt;</font>. Its value is bound to an embedded <font face='Courier'>DateRangeView</font>, while the skin renders a title label, range label, optional icon and arrow button."),Section("Key features"),Bullets(["Starts with the embedded DateRangeView's default value, which is the Today preset.","Optional calendar icon and optional preset/custom title in the button area.","Single-line small mode or two-line display mode.","Formatter property controls rendered dates.","Accessible text combines the title and formatted range." ]),Section("Maven dependency"),Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="DateRangePicker is in package <font face='Courier'>com.dlsc.gemsfx.daterange</font>.")]),
Chapter("Getting started",[Code("""DateRangePicker picker = new DateRangePicker();
picker.setValue(new DateRange("Sprint", LocalDate.now(), LocalDate.now().plusDays(13)));
picker.setFormatter(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));

picker.valueProperty().addListener((obs, oldRange, range) -> {
    if (range != null) {
        loadReport(range.getStartDate(), range.getEndDate());
    }
});""", caption="A complete DateRangePicker setup."),Figure(f"{G}/cover.svg","A DateRangePicker with its popup open.")]),
Chapter("Anatomy",[Figure(f"{G}/anatomy.svg","The parts of a DateRangePicker."),Table(["Part","Style class","Description"],[["Root","date-range-picker","ComboBoxBase root with accessible role DATE_PICKER."],["Outer container","outer-range-container","HBox with range content and arrow button."],["Inner range container","inner-range-container","VBox or HBox depending on small."],["Title label","title-label","Preset title, customRangeText or hidden when showPresetTitle is false."],["Range label","range-label","Formatted start/end date text; owns optional icon button."],["Icon","icon-button, icon","Visible when showIcon is true."],["Popup","DateRangeView","Returned by getDateRangeView(); value is bound bidirectionally."],["Arrow","arrow-button, arrow","Shows and hides popup."]], widths=[21,31,48])]),
Chapter("Control API",[PropertyTable([Property("value","ObjectProperty&lt;DateRange&gt;","DateRangeView default Today range","The selected start/end date range inherited from ComboBoxBase."),Property("showIcon","BooleanProperty","true","Shows the calendar icon in front of the range label."),Property("showPresetTitle","BooleanProperty","true","Shows preset title or customRangeText when promptText is empty."),Property("small","BooleanProperty","true","Uses one row with a divider instead of a two-line VBox."),Property("formatter","ObjectProperty&lt;DateTimeFormatter&gt;","localized MEDIUM date","Formats the date or range text in the button."),Property("customRangeText","StringProperty","\"Date Range\"","Localized fallback title when a selected DateRange has no title."),Property("promptText","StringProperty","null","Inherited; when non-blank it replaces title and range labels with the prompt.")]),Section("Popup access"),Code("""DateRangeView view = picker.getDateRangeView();
view.setShowPresets(false);
view.setPresetsLocation(Side.RIGHT);""")]),
Chapter("Display modes",[Figure(f"{G}/layout.svg","The small property switches between one-line and two-line display."),Table(["Property state","Rendered result"],[["small = true","Title, divider and range label are placed in an HBox. This is the default."],["small = false","Title label and range label are stacked in a VBox."],["showIcon = false","The icon button is unmanaged and invisible."],["showPresetTitle = false","The title label and divider are unmanaged unless promptText is shown."]], widths=[30,70])]),
Chapter("Presets and commit flow",[Figure(f"{G}/presets.svg","Preset clicks stage a selection; APPLY commits it to value."),Para("The skin binds picker value and view value bidirectionally. It also installs <font face='Courier'>view.setOnClose(this::hide)</font>, so DateRangeView's apply or cancel path hides the popup."),Code("""picker.getDateRangeView().getPresets().setAll(
    new DateRangePreset("Next 7 days", () ->
        new DateRange("Next 7 days", LocalDate.now(), LocalDate.now().plusDays(6)))
);""")]),
Chapter("Styling",[Section("Style classes"),Table(["Selector","Meaning"],[[".date-range-picker","Root."],[".popup","Popup container effect."],[".outer-range-container","HBox root of skin."],[".inner-range-container","Title/range content pane."],[".inner-range-container.small","One-line mode."],[".divider","Divider between title and range in small mode."],[".title-label","Preset/custom title."],[".range-label","Formatted dates."],[".icon-button > .icon","Calendar glyph."],[".arrow-button > .arrow","Popup arrow."]], widths=[45,55]),Section("Styleable CSS properties"),Para("DateRangePicker declares no own styleable CSS properties. It uses only standard JavaFX CSS plus its style classes."),Code(""".date-range-picker > .outer-range-container > .inner-range-container > .title-label {
    -fx-font-weight: bold;
}

.date-range-picker > .outer-range-container > .inner-range-container > .range-label {
    -fx-text-fill: -fx-accent;
}""")]),
Chapter("Localization",[Table(["Key","English default"],[["label.custom-range","Date Range"]], widths=[42,58])]),
Chapter("Accessibility",[Para("DateRangePicker sets <font face='Courier'>AccessibleRole.DATE_PICKER</font>. Its accessible text is derived from value, formatter and customRangeText: title plus formatted range, or just the formatted range when no title is available.")]),
Chapter("Recipes",[Section("Hide the icon"),Code("picker.setShowIcon(false);") ,Section("Use short dates"),Code("picker.setFormatter(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));"),Section("Two-line display"),Code("picker.setSmall(false);") ,Section("Custom title for ad hoc ranges"),Code("picker.setCustomRangeText(\"Custom period\");"),Section("Checklist"),Numbered(["Use DateRange titles for meaningful presets.","Use promptText only for the empty/placeholder state.","Change presets through getDateRangeView().getPresets().","Keep formatter non-null when using the built-in skin's toString method."])]),
Chapter("See also",[Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.DateRangePickerApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.DateRangePickerApp</font>)","Related controls: <font face='Courier'>DateRangeView</font>, <font face='Courier'>CalendarView</font>, <font face='Courier'>CalendarPicker</font>.","API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])])
