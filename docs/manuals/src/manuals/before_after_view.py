"""Content of the BeforeAfterView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "before-after-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('before', 'ObjectProperty<Node>', 'localized red Before Label', 'Node shown on the before side.'), ('after', 'ObjectProperty<Node>', 'localized green After Label', 'Node shown on the after side.'), ('dividerPosition', 'DoubleProperty', '0.5', 'Divider position from 0.0 to 1.0; styleable.'), ('orientation', 'ObjectProperty<Orientation>', 'HORIZONTAL', 'HORIZONTAL or VERTICAL; styleable.')]
CSS_PROPS = [('-fx-orientation', 'Orientation', 'HORIZONTAL'), ('-fx-divider-position', 'size', '0.5')]
SELECTORS = ['.before-after-view', ':horizontal', ':vertical', '> .content', '> .divider', '> .handle', '.handle .ikonli-font-icon']
LOC = [('placeholder.before', 'Before'), ('placeholder.after', 'After'), ('accessible.role-description', 'before after comparison')]

MANUAL = Manual(
    control="BeforeAfterView",
    package="com.dlsc.gemsfx",
    subtitle='Draggable before/after comparison slider',
    abstract='BeforeAfterView layers two nodes and clips them around a draggable divider, so users can reveal more of the before or after state horizontally or vertically.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of BeforeAfterView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>BeforeAfterView</b> BeforeAfterView layers two nodes and clips them around a draggable divider, so users can reveal more of the before or after state horizontally or vertically."),
            Section("Key features"),
            Bullets(['before and after can be any Node; image constructor wraps ImageView.', 'Default placeholders are localized labels Before and After.', 'dividerPosition defaults to 0.5 and is clamped to 0..1 during dragging.', 'Orientation.HORIZONTAL is the default; VERTICAL is supported.', 'Before and after nodes are made mouseTransparent when set.', 'Accessible role is SLIDER with localized role description.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('BeforeAfterView view = new BeforeAfterView(beforeImage, afterImage);\nview.setDividerPosition(.35);\nview.setOrientation(Orientation.HORIZONTAL);\nview.dividerPositionProperty().addListener((obs, old, pos) -> updateStatus(pos));', caption="A compact setup for BeforeAfterView."),
            Figure(f"{G}/cover.svg", "A generated overview of BeforeAfterView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of BeforeAfterView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.before-after-view</font> is added by the constructor."], ['Stylesheet', "User-agent stylesheet <font face='Courier'>before-after-view.css</font> is returned by the control."]], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of BeforeAfterView."),
            Bullets(['The skin layers before and after wrappers in a clipped content StackPane.', 'For HORIZONTAL, before width is dividerPosition * width and after begins at that x.', 'For VERTICAL, before height is dividerPosition * height and after begins at that y.', 'Mouse drag updates dividerPosition by delta / width or delta / height and clamps to 0..1.', 'The divider and handle are visual only and mouseTransparent; the control handles drag events.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through BeforeAfterView."),
        ]),
        Chapter("Layout and rendering", [
            Para('Rendering is based on two clipped wrappers. The divider position determines how much of the before node and after node is visible.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for BeforeAfterView."),
            Table(["Concern", "Rule"], [['Horizontal', 'Divider x = width * dividerPosition.'], ['Vertical', 'Divider y = height * dividerPosition.'], ['Preferred size', 'Skin returns max preferred size of before and after nodes.']], widths=[32,68]),
        ]),

        Chapter("Divider mechanics", [
            Para("The divider is not a child splitter. It is a visual overlay whose position is stored in <font face='Courier'>dividerPosition</font>; the actual reveal is produced by two clips on wrapper panes."),
            Table(["Orientation", "Clip calculation"], [["Horizontal before", "width = wrapper.width × dividerPosition"], ["Horizontal after", "x = wrapper.width × dividerPosition; width = wrapper.width − x"], ["Vertical before", "height = wrapper.height × dividerPosition"], ["Vertical after", "y = wrapper.height × dividerPosition; height = wrapper.height − y"]], widths=[35,65]),
            Code("""view.setOnMouseDragged(evt -> {
    double delta = evt.getX() - startX;
    view.setDividerPosition(Math.min(1, Math.max(0,
            view.getDividerPosition() + delta / view.getWidth())));
});""", caption="Simplified horizontal drag logic from the skin."),
            Callout("The source makes before and after nodes mouseTransparent, so pointer input belongs to the comparison control instead of the compared content.", kind="tip"),
        ]),
        Chapter("Styling", [
            Para('The user-agent stylesheet is before-after-view.css. The table lists selectors and pseudo classes that exist in source, skin or stylesheet.'),
            Figure(f"{G}/styling.svg", "Style hooks for BeforeAfterView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.before-after-view {\n    /* start with the documented root selector */\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for BeforeAfterView."),
        ]),
        Chapter("Accessibility", [
            Para('BeforeAfterView sets AccessibleRole.SLIDER with localized role description "before after comparison". It does not bind an accessible value to dividerPosition.'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('BeforeAfterView view = new BeforeAfterView(beforeImage, afterImage);\nview.setDividerPosition(.35);\nview.setOrientation(Orientation.HORIZONTAL);\nview.dividerPositionProperty().addListener((obs, old, pos) -> updateStatus(pos));'),
            Section("Practical checklist"),
            Numbered(['Clamp programmatic dividerPosition values yourself if they come from user data.', 'Set both before and after nodes before showing the control.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('No keyboard handling for divider movement is implemented.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.BeforeAfterViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.BeforeAfterViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
