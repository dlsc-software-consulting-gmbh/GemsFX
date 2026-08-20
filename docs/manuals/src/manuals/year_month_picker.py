from manualkit import (Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table)

G="year-month-picker"
MANUAL=Manual(control="YearMonthPicker", package="com.dlsc.gemsfx", subtitle="An editable picker for YearMonth values", abstract="YearMonthPicker is a CustomComboBox for selecting a YearMonth. It uses a text editor, a calendar icon button and a YearMonthView popup, with a converter that defaults to full month name plus year.", cover_svg=f"{G}/cover.svg", cover_caption="YearMonthPicker opens a YearMonthView popup for month selection.", chapters=[
Chapter("Introduction",[Para("<b>YearMonthPicker</b> extends <font face='Courier'>CustomComboBox&lt;YearMonth&gt;</font>. It starts at <font face='Courier'>YearMonth.now()</font>, formats values as <font face='Courier'>MMMM yyyy</font> and parses invalid text to null."),Section("Key features"),Bullets(["Editable TextField returned by getEditor().","YearMonthView popup bound bidirectionally to the picker value.","Calendar trigger button using an Ikonli mdi-calendar icon.","UP and DOWN keys step the current value by one month.","Button placement inherited from CustomComboBox." ]),Section("Maven dependency"),Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="YearMonthPicker is in package <font face='Courier'>com.dlsc.gemsfx</font>.")]),
Chapter("Getting started",[Code("""YearMonthPicker picker = new YearMonthPicker();
picker.setValue(YearMonth.of(2026, 3));

picker.valueProperty().addListener((obs, oldMonth, month) -> {
    if (month != null) {
        System.out.println("period starts " + month.atDay(1));
    }
});""", caption="A complete YearMonthPicker setup."),Figure(f"{G}/cover.svg","YearMonthPicker in a budget-period form."),Callout("The default editor prompt is localized from <font face='Courier'>prompt.example-month-year</font> and reads <i>Example: March 2023</i> in English.", kind="note")]),
Chapter("Anatomy",[Figure(f"{G}/anatomy.svg","The parts of a YearMonthPicker."),Table(["Part","Style class","Description"],[["Root","year-month-picker, text-input","CustomComboBox root with accessible role COMBO_BOX."],["Editor","TextField","Editable state is bound to the picker; action and focus loss commit text."],["Button","edit-button","Popup trigger, position controlled by buttonDisplay."],["Icon","edit-icon / ikonli-font-icon","mdi-calendar icon from Ikonli."],["Popup","YearMonthView","Returned by getYearMonthView(); value is bidirectionally bound on first show."],["Converter","converter","Formats and parses YearMonth values." ]], widths=[22,32,46])]),
Chapter("Control API",[PropertyTable([Property("value","ObjectProperty&lt;YearMonth&gt;","YearMonth.now()","Selected month inherited from ComboBoxBase."),Property("editable","BooleanProperty","true (ComboBoxBase default, editor bound)","Controls whether text can be typed."),Property("converter","ObjectProperty&lt;StringConverter&lt;YearMonth&gt;&gt;","MMMM yyyy converter","Formats YearMonth; invalid default parsing returns null. Attempts to set converter to null restore the old converter."),Property("buttonDisplay","ObjectProperty&lt;CustomComboBox.ButtonDisplay&gt;","RIGHT","Styleable inherited property: LEFT, RIGHT, BUTTON_ONLY, FIELD_ONLY."),Property("promptText","StringProperty","ComboBoxBase default null","Not bound to the editor; the editor receives its localized prompt directly."),Property("getEditor()","TextField","created in constructor","Manual input field."),Property("getYearMonthView()","YearMonthView","lazy new YearMonthView()","Popup month view.")]),Code("""picker.setConverter(new StringConverter<>() {
    public String toString(YearMonth ym) { return ym == null ? "" : ym.toString(); }
    public YearMonth fromString(String text) { return YearMonth.parse(text); }
});""")]),
Chapter("Editing behaviour",[Figure(f"{G}/editing.svg","Text commits and arrow keys all update the YearMonth value."),Table(["Action","Effect"],[["ENTER in editor","Calls commit() and parses text through converter."],["Editor loses focus","Commits the text."],["Touch press","Commits current text and shows popup."],["DOWN key","Sets value to <font face='Courier'>getValue().plusMonths(1)</font>."],["UP key","Sets value to <font face='Courier'>getValue().minusMonths(1)</font>."],["Popup month click","Updates value and hides popup when the value changes."]], widths=[34,66]),Callout("The UP/DOWN key handler assumes the value is not null. If your converter may return null, guard keyboard usage or restore a non-null value after invalid input.", kind="warning")]),
Chapter("Button display",[Figure(f"{G}/button-display.svg","The inherited buttonDisplay property changes which nodes are visible."),Para("The skin rebuilds its HBox when <font face='Courier'>buttonDisplay</font> changes. LEFT places the edit button before the editor, RIGHT after it, BUTTON_ONLY hides the editor and FIELD_ONLY hides the button."),Code("""picker.setButtonDisplay(CustomComboBox.ButtonDisplay.LEFT);
// or in CSS: -fx-button-display: button-only;""")]),
Chapter("Styling",[Section("Style classes and pseudo classes"),Table(["Selector","Meaning"],[[".year-month-picker","Root."],[".text-input","Additional root class set by constructor."],[".year-month-view","Popup view, styled with a drop shadow while inside the picker."],[".box","HBox containing editor and edit button."],[".edit-button","Calendar trigger button."],[".edit-button > .ikonli-font-icon","Calendar icon, style class edit-icon."],[".text-field","Editor."],[":left, :right, :button-only, :field-only","Inherited button display pseudo classes."],[":focused","Mirrors editor focus." ]], widths=[52,48]),Section("Styleable CSS properties"),Table(["CSS property","Type","Default"],[["-fx-button-display","ButtonDisplay","RIGHT"]]),Code(""".year-month-picker {
    -fx-button-display: left;
}

.year-month-picker > .box > .edit-button > .ikonli-font-icon {
    -fx-icon-color: -fx-accent;
}""")]),
Chapter("Localization",[Table(["Key","English default"],[["prompt.example-month-year","Example: March 2023"]], widths=[48,52])]),
Chapter("Accessibility",[Para("YearMonthPicker sets <font face='Courier'>AccessibleRole.COMBO_BOX</font>. Its automatic accessible text is the current value formatted by converter, or null when the value is null. The binding follows AccessibilityUtil semantics and yields if application code sets accessibleText manually.")]),
Chapter("Recipes",[Section("ISO year-month strings"),Code("""picker.setConverter(new StringConverter<>() {
    public String toString(YearMonth ym) { return ym == null ? "" : ym.toString(); }
    public YearMonth fromString(String text) { return text == null || text.isBlank() ? null : YearMonth.parse(text); }
});"""),Section("Button-only chooser"),Code("picker.setButtonDisplay(CustomComboBox.ButtonDisplay.BUTTON_ONLY);") ,Section("Preset current quarter start"),Code("""YearMonth now = YearMonth.now();
int quarterStart = ((now.getMonthValue() - 1) / 3) * 3 + 1;
picker.setValue(YearMonth.of(now.getYear(), quarterStart));"""),Section("Customize popup"),Code("""picker.getYearMonthView().setShowYear(true);"""),Section("Checklist"),Numbered(["Keep value non-null when relying on UP/DOWN keyboard stepping.","Use a converter that matches the prompt shown to users.","Set buttonDisplay through CSS for visual-only changes.","Customize getYearMonthView() before opening the popup."])]),
Chapter("See also",[Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.YearMonthPickerApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.YearMonthPickerApp</font>)","Related controls: <font face='Courier'>YearMonthView</font>, <font face='Courier'>CalendarPicker</font>, <font face='Courier'>CalendarView</font>.","API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])])
