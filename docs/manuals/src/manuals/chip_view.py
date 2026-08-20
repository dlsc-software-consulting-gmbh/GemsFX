"""Content of the ChipView developer manual."""
from manualkit import Bullets, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "chip-view"
MANUAL = Manual(
    control="ChipView", package="com.dlsc.gemsfx", subtitle='A compact removable badge for one value', abstract='A small pill-shaped Control representing one model object. The skin displays text and an optional graphic, and shows a close icon only when an onClose consumer is set.',
    cover_svg=f"{G}/cover.svg", cover_caption='Generated cartoon overview of ChipView.',
    chapters=[
        Chapter("Introduction", [Para("<b>ChipView</b> A small pill-shaped Control representing one model object. The skin displays text and an optional graphic, and shows a close icon only when an onClose consumer is set."), Section("Key features"), Bullets(['The close icon is visible and managed only when onClose is non-null.', 'Clicking close invokes the consumer with getValue().', 'API and styling details below are verified against the control, skin, CSS and resource bundles.']), Section("Maven dependency"), Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""")]),
        Chapter("Getting started", [Code('ChipView<Filter> chip = new ChipView<>();\nchip.setValue(filter);\nchip.setText(filter.label());\nchip.setGraphic(new FontIcon(MaterialDesign.MDI_FILTER));\nchip.setOnClose(value -> activeFilters.remove(value));', caption="A compact setup for ChipView."), Figure(f"{G}/cover.svg", "ChipView in a typical application context.")]),
        Chapter("Anatomy", [Para("The diagram identifies the nodes and state holders created by the implementation."), Figure(f"{G}/anatomy.svg", "The parts of ChipView."), Table(["Topic","Verified source detail"], [["Stylesheet","<font face='Courier'>com/dlsc/gemsfx/chip-view.css</font>"],["Root style class","<font face='Courier'>.chip-view</font>"],["Graphics","Generated SVGs; no screenshots."]], widths=[30,70])]),
        Chapter("Control API", [Section("Properties and callbacks"), PropertyTable([Property(n,t,d,desc) for n,t,d,desc in [('value', 'ObjectProperty&lt;T&gt;', 'null', 'Model object represented by the chip.'), ('text', 'StringProperty', 'localized "Untitled"', 'Text shown by the chip.'), ('graphic', 'ObjectProperty&lt;Node&gt;', 'null', 'Optional label graphic.'), ('contentDisplay', 'ObjectProperty&lt;ContentDisplay&gt;', 'LEFT', 'Graphic/text placement; styleable.'), ('onClose', 'ObjectProperty&lt;Consumer&lt;T&gt;&gt;', 'null', 'Called with value when the close icon is clicked.')]])]),
        Chapter("Behaviour", [Figure(f"{G}/states.svg", "Important runtime states of ChipView."), Bullets(['The close icon is visible and managed only when onClose is non-null.', 'Clicking close invokes the consumer with getValue().', 'The control does not remove itself; update the owning model in the callback.']), Figure(f"{G}/flow.svg", "How application data flows through ChipView.")]),
        Chapter("Styling", [Figure(f"{G}/styling.svg", "Style hooks for ChipView."), Section("Style classes and pseudo classes"), Table(["Selector / pseudo class","Purpose"], [[f"<font face='Courier'>{s}</font>", "Verified in source, skin or CSS."] for s in ['.chip-view', '> .chip-container', '> .chip-container > .label', '.close-icon', '.close-icon:hover', '.close-icon:pressed', '.close-icon > .ikonli-font-icon']], widths=[50,50]), Section("Styleable CSS properties"), Table(["CSS property","Type","Default"], [[f"<font face='Courier'>{p}</font>",t,d] for p,t,d in [('-fx-content-display', 'ContentDisplay', 'LEFT')]], widths=[48,26,26]) if True else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."), Code(""".chip-view {
    /* start with the documented root selector */
}
""")]),
        Chapter("Localization", [Table(["Key","English text"], [[f"<font face='Courier'>{k}</font>",v] for k,v in [('default.text.untitled', 'Untitled'), ('accessible.role-description', 'chip')]], widths=[45,55]) if True else Para("The verified source does not use ResourceBundleManager for ChipView.")]),
        Chapter("Accessibility", [Para('Sets AccessibleRole.BUTTON with localized role description chip. No accessible text binding is installed.')]),
        Chapter("Recipes", [Section("Programmatic configuration"), Code('ChipView<Filter> chip = new ChipView<>();\nchip.setValue(filter);\nchip.setText(filter.label());\nchip.setGraphic(new FontIcon(MaterialDesign.MDI_FILTER));\nchip.setOnClose(value -> activeFilters.remove(value));'), Section("Checklist"), Numbered(["Use the public properties listed in the API chapter.", "Style through documented selectors and CSS properties only.", "Keep application model updates in callbacks such as onClose, onClear or onItemSelected.", "Do not depend on private skin nodes except for documented style selectors."])]),

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
        Chapter("See also", [Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.SimpleFilterViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.SimpleFilterViewApp</font>)", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])
    ])
