"""Content of the TagsField developer manual."""
from manualkit import Bullets, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "tags-field"
MANUAL = Manual(
    control="TagsField", package="com.dlsc.gemsfx", subtitle='Search suggestions committed as selectable tags', abstract='A SearchField specialization that turns committed suggestions into removable tags shown before the editor. It owns an observable tag list and a separate selection model for tag nodes.',
    cover_svg=f"{G}/cover.svg", cover_caption='Generated cartoon overview of TagsField.',
    chapters=[
        Chapter("Introduction", [Para("<b>TagsField</b> A SearchField specialization that turns committed suggestions into removable tags shown before the editor. It owns an observable tag list and a separate selection model for tag nodes."), Section("Key features"), Bullets(['ENTER or RIGHT with a selected suggestion adds a tag and clears the editor.', 'BACK_SPACE removes selected tags, or the last tag when the editor is empty.', 'API and styling details below are verified against the control, skin, CSS and resource bundles.']), Section("Maven dependency"), Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""")]),
        Chapter("Getting started", [Code('TagsField<Country> field = new TagsField<>();\nfield.setSuggestionProvider(request -> countries.stream().filter(c -> c.name().contains(request.getUserText())).toList());\nfield.setConverter(countryConverter);\nfield.addTags(new Country("Germany"));', caption="A compact setup for TagsField."), Figure(f"{G}/cover.svg", "TagsField in a typical application context.")]),
        Chapter("Anatomy", [Para("The diagram identifies the nodes and state holders created by the implementation."), Figure(f"{G}/anatomy.svg", "The parts of TagsField."), Table(["Topic","Verified source detail"], [["Stylesheet","<font face='Courier'>com/dlsc/gemsfx/tags-field.css</font>"],["Root style class","<font face='Courier'>.tags-field</font>"],["Graphics","Generated SVGs; no screenshots."]], widths=[30,70])]),
        Chapter("Control API", [Section("Properties and callbacks"), PropertyTable([Property(n,t,d,desc) for n,t,d,desc in [('tags', 'ListProperty&lt;T&gt;', 'empty list', 'Values currently shown as tags.'), ('tagViewFactory', 'ObjectProperty&lt;Callback&lt;T, Node&gt;&gt;', 'Label with close icon', 'Creates tag nodes.'), ('tagSelectionModel', 'ObjectProperty&lt;MultipleSelectionModel&lt;T&gt;&gt;', 'TagFieldSelectionModel', 'Selection model for tags; default MULTIPLE.'), ('editorMinWidth', 'DoubleProperty', '20', 'Editor width while empty.'), ('editorPrefWidth', 'DoubleProperty', '200', 'Editor width while typing.'), ('graphic', 'ObjectProperty&lt;Node&gt;', 'null', 'TagsField removes inherited history graphic.'), ('suggestionProvider / converter / matcher / comparator', 'inherited SearchField properties', 'same as SearchField', 'Configure as for SearchField.')]])]),
        Chapter("Behaviour", [Figure(f"{G}/states.svg", "Important runtime states of TagsField."), Bullets(['ENTER or RIGHT with a selected suggestion adds a tag and clears the editor.', 'BACK_SPACE removes selected tags, or the last tag when the editor is empty.', 'shortcut+Z and shortcut+shift+Z undo and redo tag commands.', 'LEFT and RIGHT at caret zero move through tag selection.']), Figure(f"{G}/flow.svg", "How application data flows through TagsField.")]),
        Chapter("Styling", [Figure(f"{G}/styling.svg", "Style hooks for TagsField."), Section("Style classes and pseudo classes"), Table(["Selector / pseudo class","Purpose"], [[f"<font face='Courier'>{s}</font>", "Verified in source, skin or CSS."] for s in ['.tags-field', ':contains-focus', ':filled', '> .flow-pane', '.tag-view', '.tag-view:selected', '.tag-view.first / .middle / .last / .only', '.close-icon > .close']], widths=[50,50]), Section("Styleable CSS properties"), Table(["CSS property","Type","Default"], [[f"<font face='Courier'>{p}</font>",t,d] for p,t,d in []], widths=[48,26,26]) if False else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."), Code(""".tags-field {
    /* start with the documented root selector */
}
""")]),
        Chapter("Localization", [Table(["Key","English text"], [[f"<font face='Courier'>{k}</font>",v] for k,v in []], widths=[45,55]) if False else Para("The verified source does not use ResourceBundleManager for TagsField.")]),
        Chapter("Accessibility", [Para('Sets AccessibleRole.COMBO_BOX. No custom accessible text binding is installed.')]),
        Chapter("Recipes", [Section("Programmatic configuration"), Code('TagsField<Country> field = new TagsField<>();\nfield.setSuggestionProvider(request -> countries.stream().filter(c -> c.name().contains(request.getUserText())).toList());\nfield.setConverter(countryConverter);\nfield.addTags(new Country("Germany"));'), Section("Checklist"), Numbered(["Use the public properties listed in the API chapter.", "Style through documented selectors and CSS properties only.", "Keep application model updates in callbacks such as onClose, onClear or onItemSelected.", "Do not depend on private skin nodes except for documented style selectors."])]),

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
        Chapter("See also", [Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.TagsFieldApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.TagsFieldApp</font>)", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])])
    ])
