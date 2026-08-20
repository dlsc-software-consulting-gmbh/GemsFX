"""Content of the HistoryButton developer manual."""
from manualkit import Bullets, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "history-button"
MANUAL = Manual(
    control="HistoryButton", package="com.dlsc.gemsfx", subtitle='A button that shows and edits persisted history entries', abstract='A Button that opens a popup ListView bound to a HistoryManager. It can be embedded in an input field or used standalone.',
    cover_svg=f"{G}/cover.svg", cover_caption='Generated cartoon overview of HistoryButton.',
    chapters=[
        Chapter("Introduction", [Para("<b>HistoryButton</b> A Button that opens a popup ListView bound to a HistoryManager. It can be embedded in an input field or used standalone."), Section("Key features"), Bullets(['Clicking the button toggles the popup.', 'If owner is set and not focused, owner.requestFocus() runs before showing.', 'API and styling details below are verified against the control, skin, CSS and resource bundles.']), Section("Maven dependency"), Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""")]),
        Chapter("Getting started", [Code('HistoryButton<String> button = new HistoryButton<>(textField);\nStringHistoryManager manager = new StringHistoryManager(Preferences.userNodeForPackage(MyApp.class), "recent-searches");\nbutton.setHistoryManager(manager);\nbutton.setOnItemSelected(textField::setText);', caption="A compact setup for HistoryButton."), Figure(f"{G}/cover.svg", "HistoryButton in a typical application context.")]),
        Chapter("Anatomy", [Para("The diagram identifies the nodes and state holders created by the implementation."), Figure(f"{G}/anatomy.svg", "The parts of HistoryButton."), Table(["Topic","Verified source detail"], [["Stylesheet","<font face='Courier'>com/dlsc/gemsfx/history-button.css</font>"],["Root style class","<font face='Courier'>.history-button</font>"],["Graphics","Generated SVGs; no screenshots."]], widths=[30,70])]),
        Chapter("Control API", [Section("Properties and callbacks"), PropertyTable([Property(n,t,d,desc) for n,t,d,desc in [('historyManager', 'ObjectProperty&lt;HistoryManager&lt;T&gt;&gt;', 'null', 'Source of popup entries; null disables popup.'), ('owner', 'ObjectProperty&lt;Node&gt;', 'null', 'Optional owner that receives focus first.'), ('placeholder', 'ObjectProperty&lt;Node&gt;', 'null', 'History ListView placeholder.'), ('onItemSelected', 'ObjectProperty&lt;Consumer&lt;T&gt;&gt;', 'null', 'Called on primary click or ENTER.'), ('popupShowing', 'ReadOnlyBooleanProperty', 'false', 'True while popup is open.'), ('listDecorationLeft/right/top/bottom', 'ObjectProperty&lt;Node&gt;', 'null', 'Popup BorderPane decorations.'), ('cellFactory', 'ObjectProperty&lt;Callback&lt;ListView&lt;T&gt;, ListCell&lt;T&gt;&gt;&gt;', 'RemovableListCell', 'Factory for history cells.')]])]),
        Chapter("Behaviour", [Figure(f"{G}/states.svg", "Important runtime states of HistoryButton."), Bullets(['Clicking the button toggles the popup.', 'If owner is set and not focused, owner.requestFocus() runs before showing.', 'The popup is auto-fixing, auto-hiding and closes on ESCAPE.', 'Primary click or ENTER confirms the selected ListView item.', 'The default remove button calls historyManager.remove(item).']), Figure(f"{G}/flow.svg", "How application data flows through HistoryButton.")]),
        Chapter("Styling", [Figure(f"{G}/styling.svg", "Style hooks for HistoryButton."), Section("Style classes and pseudo classes"), Table(["Selector / pseudo class","Purpose"], [[f"<font face='Courier'>{s}</font>", "Verified in source, skin or CSS."] for s in ['.history-button', ':disabled-popup', ':popup-showing', '.history-popup', '.content-pane', '.history-list-view', '.removable-list-cell', '.remove-button', '.history-popup.round']], widths=[50,50]), Section("Styleable CSS properties"), Table(["CSS property","Type","Default"], [[f"<font face='Courier'>{p}</font>",t,d] for p,t,d in []], widths=[48,26,26]) if False else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."), Code(""".history-button {
    /* start with the documented root selector */
}
""")]),
        Chapter("Localization", [Table(["Key","English text"], [[f"<font face='Courier'>{k}</font>",v] for k,v in []], widths=[45,55]) if False else Para("The verified source does not use ResourceBundleManager for HistoryButton.")]),
        Chapter("Accessibility", [Para('Sets AccessibleRole.BUTTON. No custom accessible text binding is installed.')]),
        Chapter("Recipes", [Section("Programmatic configuration"), Code('HistoryButton<String> button = new HistoryButton<>(textField);\nStringHistoryManager manager = new StringHistoryManager(Preferences.userNodeForPackage(MyApp.class), "recent-searches");\nbutton.setHistoryManager(manager);\nbutton.setOnItemSelected(textField::setText);'), Section("Checklist"), Numbered(["Use the public properties listed in the API chapter.", "Style through documented selectors and CSS properties only.", "Keep application model updates in callbacks such as onClose, onClear or onItemSelected.", "Do not depend on private skin nodes except for documented style selectors."])]),

        Chapter("Integration notes", [
            Section("Use public API boundaries"),
            Para("The implementation exposes enough properties for normal integration. Keep application state in observable lists, converters and callbacks instead of querying skin children."),
            Section("Validation checklist"),
            Table(["Concern", "Recommendation"], [["Model updates", "Perform them in the documented callback or observable list."], ["Styling", "Start at the root style class and keep selectors scoped."], ["Localization", "Override the documented resource bundle keys only when they exist."], ["Accessibility", "Preserve the role set by the control and add application-specific accessible text when needed."]], widths=[30,70]),
            Code("""// Integration pattern
// 1. Configure model properties.
// 2. Configure callbacks.
// 3. Style through documented selectors.
// 4. Leave skin internals private."""),
        ]),
        Chapter("See also", [Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.HistoryManagerApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.HistoryManagerApp</font>)", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])
    ])
