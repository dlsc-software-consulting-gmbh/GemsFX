"""Content of the ChipsViewContainer developer manual."""
from manualkit import Bullets, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "chips-view-container"
MANUAL = Manual(
    control="ChipsViewContainer", package="com.dlsc.gemsfx", subtitle='A wrapping FlowPane for active chips', abstract='A FlowPane that displays ChipView instances and, when configured, a clear Hyperlink. It manages its own visibility from the chip list.',
    cover_svg=f"{G}/cover.svg", cover_caption='Generated cartoon overview of ChipsViewContainer.',
    chapters=[
        Chapter("Introduction", [Para("<b>ChipsViewContainer</b> A FlowPane that displays ChipView instances and, when configured, a clear Hyperlink. It manages its own visibility from the chip list."), Section("Key features"), Bullets(['When chips becomes empty the container is invisible and unmanaged.', 'updateChips clears children and adds every ChipView from chips.', 'API and styling details below are verified against the control, skin, CSS and resource bundles.']), Section("Maven dependency"), Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""")]),
        Chapter("Getting started", [Code('ChipsViewContainer container = new ChipsViewContainer();\ncontainer.getChips().setAll(statusChip, ownerChip);\ncontainer.setOnClear(() -> activeFilters.clear());\ncontainer.setClearText("Reset filters");', caption="A compact setup for ChipsViewContainer."), Figure(f"{G}/cover.svg", "ChipsViewContainer in a typical application context.")]),
        Chapter("Anatomy", [Para("The diagram identifies the nodes and state holders created by the implementation."), Figure(f"{G}/anatomy.svg", "The parts of ChipsViewContainer."), Table(["Topic","Verified source detail"], [["Stylesheet","<font face='Courier'>com/dlsc/gemsfx/chips-view-container.css</font>"],["Root style class","<font face='Courier'>.chips-view-container</font>"],["Graphics","Generated SVGs; no screenshots."]], widths=[30,70])]),
        Chapter("Control API", [Section("Properties and callbacks"), PropertyTable([Property(n,t,d,desc) for n,t,d,desc in [('chips', 'ListProperty&lt;ChipView&lt;?&gt;&gt;', 'empty list', 'Chip controls shown in the FlowPane.'), ('onClear', 'ObjectProperty&lt;Runnable&gt;', 'null', 'Invoked by the clear Hyperlink.'), ('clearText', 'StringProperty', 'localized "Clear"', 'Text of the clear Hyperlink.'), ('visible', 'BooleanProperty', 'bound to !chips.empty', 'Container visibility.'), ('managed', 'BooleanProperty', 'bound to visible', 'Container managed state.')]])]),
        Chapter("Behaviour", [Figure(f"{G}/states.svg", "Important runtime states of ChipsViewContainer."), Bullets(['When chips becomes empty the container is invisible and unmanaged.', 'updateChips clears children and adds every ChipView from chips.', 'A Hyperlink is appended only when there are chip children.', 'The Hyperlink text is bound to clearText and its action calls onClear.run().']), Figure(f"{G}/flow.svg", "How application data flows through ChipsViewContainer.")]),
        Chapter("Styling", [Figure(f"{G}/styling.svg", "Style hooks for ChipsViewContainer."), Section("Style classes and pseudo classes"), Table(["Selector / pseudo class","Purpose"], [[f"<font face='Courier'>{s}</font>", "Verified in source, skin or CSS."] for s in ['.chips-view-container']], widths=[50,50]), Section("Styleable CSS properties"), Table(["CSS property","Type","Default"], [[f"<font face='Courier'>{p}</font>",t,d] for p,t,d in []], widths=[48,26,26]) if False else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."), Code(""".chips-view-container {
    /* start with the documented root selector */
}
""")]),
        Chapter("Localization", [Table(["Key","English text"], [[f"<font face='Courier'>{k}</font>",v] for k,v in [('action.clear', 'Clear')]], widths=[45,55]) if True else Para("The verified source does not use ResourceBundleManager for ChipsViewContainer.")]),
        Chapter("Accessibility", [Para('No explicit AccessibleRole and no AccessibilityUtil usage in the source.')]),
        Chapter("Recipes", [Section("Programmatic configuration"), Code('ChipsViewContainer container = new ChipsViewContainer();\ncontainer.getChips().setAll(statusChip, ownerChip);\ncontainer.setOnClear(() -> activeFilters.clear());\ncontainer.setClearText("Reset filters");'), Section("Checklist"), Numbered(["Use the public properties listed in the API chapter.", "Style through documented selectors and CSS properties only.", "Keep application model updates in callbacks such as onClose, onClear or onItemSelected.", "Do not depend on private skin nodes except for documented style selectors."])]),


        Chapter("Layout and visibility details", [
            Para("ChipsViewContainer is deliberately small: layout is inherited from FlowPane, and all dynamic behaviour comes from the chips list listener and the visible / managed bindings."),
            Figure(f"{G}/states.svg", "Empty, one-chip and wrapped states."),
            Table(["State", "Result"], [["chips empty", "The container is invisible and unmanaged, so parent layouts reclaim the space."], ["chips non-empty and onClear null", "Only ChipView children are shown; no active clear link is managed."], ["chips non-empty and onClear set", "A Hyperlink with clearText is appended after the chips."], ["chips list replaced", "The listener on the ListProperty calls updateChips and rebuilds the children."]], widths=[35,65]),
            Code("""container.visibleProperty().bind(container.chipsProperty().emptyProperty().not());
container.managedProperty().bind(container.visibleProperty());""", caption="The visibility rule used by the implementation."),
        ]),
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
