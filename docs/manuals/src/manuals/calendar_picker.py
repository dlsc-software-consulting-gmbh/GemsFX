from manualkit import (Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table)

G="calendar-picker"
MANUAL=Manual(
 control="CalendarPicker", package="com.dlsc.gemsfx", subtitle="A date picker backed by the GemsFX CalendarView",
 abstract="CalendarPicker is an editable CustomComboBox for selecting a LocalDate. Its popup is a CalendarView, so applications get direct month and year navigation, date filtering and the same cell styling hooks as the standalone calendar view.",
 cover_svg=f"{G}/cover.svg", cover_caption="CalendarPicker combines a text editor, a calendar trigger and a CalendarView popup.",
 chapters=[
 Chapter("Introduction",[
  Para("<b>CalendarPicker</b> extends <font face='Courier'>CustomComboBox&lt;LocalDate&gt;</font>. It is a replacement-style date picker whose popup content is an actual <font face='Courier'>CalendarView</font>. The control is editable by default; typed text is parsed by the converter and the popup selection is bound to the picker value."),
  Section("Key features"), Bullets(["Editable text field with a non-null <font face='Courier'>LocalDateStringConverter</font> by default.","Popup calendar created by <font face='Courier'>getCalendarView()</font> and configurable before first use.","Calendar popup defaults to showing today and a Today button.","Keyboard day stepping: UP/DOWN in the editor, LEFT/RIGHT when not editable.","Date filtering via <font face='Courier'>dateFilter</font>, passed through to the popup CalendarView.","Button placement inherited from <font face='Courier'>CustomComboBox</font>."]),
  Section("Maven dependency"), Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="CalendarPicker is in module <font face='Courier'>com.dlsc.gemsfx</font>, package <font face='Courier'>com.dlsc.gemsfx</font>."),
 ]),
 Chapter("Getting started",[
  Para("Create the picker, set or bind its value and use the inherited <font face='Courier'>show()</font> and <font face='Courier'>hide()</font> methods when the popup must be controlled programmatically."),
  Code("""CalendarPicker picker = new CalendarPicker();
picker.setValue(LocalDate.now());
picker.setPromptText("Choose a date");

picker.valueProperty().addListener((obs, oldDate, newDate) -> {
    if (newDate != null) {
        System.out.println("selected " + picker.getConverter().toString(newDate));
    }
});""", caption="A complete minimal CalendarPicker setup."),
  Figure(f"{G}/cover.svg","A CalendarPicker in a small booking form."),
  Callout("The constructor calls <font face='Courier'>setEditable(true)</font>. Setting <font face='Courier'>editable</font> to false keeps the field visible but prevents manual typing; LEFT and RIGHT then step by one day.", kind="tip"),
 ]),
 Chapter("Anatomy",[
  Para("The skin places the editor and arrow button in an <font face='Courier'>HBox</font>. On first popup display it obtains the CalendarView, sets it to single-date mode and binds the selection model's selected date bidirectionally to the picker value."),
  Figure(f"{G}/anatomy.svg","The parts of a CalendarPicker."),
  Table(["Part","Source","Description"],[["Root","CalendarPicker","Style classes <font face='Courier'>calendar-picker</font> and <font face='Courier'>text-input</font>; accessible role DATE_PICKER."],["Editor","TextField","Returned by <font face='Courier'>getEditor()</font>; prompt text and editable state are bound to the picker."],["Arrow button","StackPane / Region","Uses style classes <font face='Courier'>arrow-button</font> and <font face='Courier'>arrow</font>; opens the popup."],["Popup","CalendarView","Returned by <font face='Courier'>getCalendarView()</font>; shows today and the Today button by default."],["Selection model","CalendarView.SelectionModel","Forced to <font face='Courier'>SINGLE_DATE</font> and bound to <font face='Courier'>value</font>."],["Filter","Callback&lt;LocalDate,Boolean&gt;","The picker's <font face='Courier'>dateFilter</font> is bound into the popup view."]], widths=[22,28,50])
 ]),
 Chapter("Control API",[
  Section("Picker properties"), PropertyTable([Property("value","ObjectProperty&lt;LocalDate&gt;","null","The selected date inherited from ComboBoxBase."),Property("editable","BooleanProperty","true","Controls whether the editor accepts typing."),Property("promptText","StringProperty","null","Inherited prompt text; bound bidirectionally to the editor prompt."),Property("converter","ObjectProperty&lt;StringConverter&lt;LocalDate&gt;&gt;","LocalDateStringConverter","Parses and formats the editor text. Attempts to set it to null are rejected by restoring the old converter."),Property("dateFilter","ObjectProperty&lt;Callback&lt;LocalDate,Boolean&gt;&gt;","null","Returns true for selectable dates and false for disabled dates in the popup."),Property("buttonDisplay","ObjectProperty&lt;CustomComboBox.ButtonDisplay&gt;","RIGHT","Styleable inherited property: LEFT, RIGHT, BUTTON_ONLY or FIELD_ONLY.")]),
  Section("CalendarView access"), Para("Override or call <font face='Courier'>getCalendarView()</font> to customize the popup calendar. The picker constructor already sets <font face='Courier'>showToday</font> and <font face='Courier'>showTodayButton</font> to true and binds the date filter."),
  Code("""CalendarPicker picker = new CalendarPicker();
picker.getCalendarView().setShowWeekNumbers(true);
picker.getCalendarView().setMonthDisplayMode(CalendarView.MonthDisplayMode.TEXT_AND_DROPDOWN);
picker.setDateFilter(date -> date.getDayOfWeek().getValue() < 6);"""),
 ]),
 Chapter("Editing and popup behaviour",[
  Figure(f"{G}/interaction.svg","The editor, keyboard stepping and popup selection all write the same value."),
  Table(["Action","Effect"],[["Press ENTER in editor","Calls the converter and writes <font face='Courier'>value</font>."],["Editor loses focus","Commits the text."],["Touch press","Commits current text and shows the popup."],["Select a date in popup","Updates <font face='Courier'>value</font> and hides the popup."],["UP / DOWN","Minus / plus one day, or today if the current value is null."],["LEFT / RIGHT while not editable","Minus / plus one day."]], widths=[34,66]),
 ]),
 Chapter("Filtering dates",[
  Para("The filter is evaluated by CalendarView cells. If it returns false, the cell is disabled and cannot be used as the start or end point of a range selection. CalendarPicker itself uses single-date selection."),
  Figure(f"{G}/filtering.svg","A date filter disables cells in the popup calendar."),
  Code("""picker.setDateFilter(date -> {
    boolean weekend = date.getDayOfWeek().getValue() >= 6;
    boolean holiday = holidays.contains(date);
    return !weekend && !holiday;
});"""),
 ]),
 Chapter("Styling",[
  Para("The user agent stylesheet is <font face='Courier'>calendar-picker.css</font>. CalendarPicker also inherits the styleable <font face='Courier'>-fx-button-display</font> property from CustomComboBox."),
  Section("Style classes and pseudo classes"), Table(["Selector","Meaning"],[["<font face='Courier'>.calendar-picker</font>","Root control."],["<font face='Courier'>.text-input</font>","Additional root class set by the constructor."],["<font face='Courier'>&gt; .box</font>","HBox containing editor and arrow button."],["<font face='Courier'>&gt; .box &gt; .arrow-button</font>","Popup trigger button."],["<font face='Courier'>&gt; .box &gt; .arrow-button &gt; .arrow</font>","Calendar glyph region."],["<font face='Courier'>&gt; .box &gt; .text-field</font>","The editor."],["<font face='Courier'>:left</font>, <font face='Courier'>:right</font>, <font face='Courier'>:button-only</font>, <font face='Courier'>:field-only</font>","Button display states inherited from CustomComboBox."],["<font face='Courier'>:focused</font>","Mirrors editor focus." ]], widths=[52,48]),
  Section("Styleable CSS properties"), Table(["CSS property","Type","Default"],[["<font face='Courier'>-fx-button-display</font>","ButtonDisplay","RIGHT"]]),
  Code(""".calendar-picker {
    -fx-button-display: left;
}

.calendar-picker > .box > .arrow-button > .arrow {
    -fx-background-color: -fx-accent;
}""", caption="Moving and recolouring the calendar trigger."),
 ]),
 Chapter("Accessibility",[Para("CalendarPicker sets <font face='Courier'>AccessibleRole.DATE_PICKER</font>. <font face='Courier'>AccessibilityUtil.bindAccessibleText</font> derives the accessible text from the current value and converter; a null value produces no automatic text. The binding yields once application code sets accessibleText manually.")]),
 Chapter("Recipes",[
  Section("Disable weekends"), Code("""picker.setDateFilter(date -> date.getDayOfWeek().getValue() < 6);"""),
  Section("Use ISO text"), Code("""picker.setConverter(new StringConverter<>() {
    public String toString(LocalDate date) { return date == null ? "" : date.toString(); }
    public LocalDate fromString(String text) { return text == null || text.isBlank() ? null : LocalDate.parse(text); }
});"""),
  Section("Button only"), Code("""picker.setButtonDisplay(CustomComboBox.ButtonDisplay.BUTTON_ONLY);"""),
  Section("Customize the popup calendar"), Code("""picker.getCalendarView().setShowWeekNumbers(true);
picker.getCalendarView().setHeaderLayout(CalendarView.HeaderLayout.LEFT);"""),
  Section("Checklist"), Numbered(["Keep the converter non-null.","Prefer <font face='Courier'>dateFilter</font> over post-selection validation when dates are unavailable.","Customize <font face='Courier'>getCalendarView()</font> before the first popup is shown.","Set <font face='Courier'>buttonDisplay</font> from CSS when the choice is purely visual."])
 ]),
 Chapter("See also",[Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.CalendarPickerApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.CalendarPickerApp</font>)","Related controls: <font face='Courier'>CalendarView</font>, <font face='Courier'>DateRangePicker</font>, <font face='Courier'>YearMonthPicker</font>.","API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])
 ])
