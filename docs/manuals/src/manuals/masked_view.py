"""Content of the MaskedView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "masked-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('content', 'SimpleObjectProperty<Node>', 'null', 'Node shown inside the masked view.'), ('fadingSize', 'DoubleProperty', '120', 'Width of left and right fade areas; styleable.')]
CSS_PROPS = [('-fx-fading-size', 'size', '120')]
SELECTORS = ['.masked-view', '.container']
LOC = []

MANUAL = Manual(
    control="MaskedView",
    package="com.dlsc.gemsfx",
    subtitle='Content clipping with fading side masks',
    abstract='MaskedView wraps one content node and clips it with left, center and right rectangles. The edge clips become gradients when translated content overflows.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of MaskedView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>MaskedView</b> MaskedView wraps one content node and clips it with left, center and right rectangles. The edge clips become gradients when translated content overflows."),
            Section("Key features"),
            Bullets(['Stores a single content Node.', 'Used internally by StripView for side-faded scrolling content.', 'fadingSize defaults to 120 and is styleable.', 'Left edge fades only when content.translateX is negative.', 'Right edge fades only when content.translateX + prefWidth exceeds the view width.', 'The content translateX is observed through a WeakInvalidationListener.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('HBox strip = new HBox(cards);\nMaskedView masked = new MaskedView(strip);\nmasked.setFadingSize(80);\nstrip.setTranslateX(-120);', caption="A compact setup for MaskedView."),
            Figure(f"{G}/cover.svg", "A generated overview of MaskedView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of MaskedView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.masked-view</font> is added by the constructor."], ['Stylesheet', 'No user-agent stylesheet is returned by the control source.'], ['Clip group', 'Skin clips content with left, center and right rectangles.']], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of MaskedView."),
            Bullets(['The skin clips a StackPane with a Group containing left, center and right rectangles.', 'The center clip is always solid black.', 'The left clip is transparent-to-black when content is shifted left; otherwise solid.', 'The right clip is black-to-transparent when content overflows the right edge; otherwise solid.', 'layoutChildren caps fadingSize at half the content width.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through MaskedView."),
        ]),
        Chapter("Layout and rendering", [
            Para('Rendering uses a StackPane clipped by three rectangles. Edge rectangles switch to transparent gradients only when the content overflows that side.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for MaskedView."),
            Table(["Concern", "Rule"], [['Fade width', 'layoutChildren uses min(contentWidth / 2, fadingSize).'], ['Left fade', 'Enabled when content.translateX < 0.'], ['Right fade', 'Enabled when translated content extends past view width.']], widths=[32,68]),
        ]),

        Chapter("Fade-mask mechanics", [
            Para("MaskedView does not blur pixels and does not paint an overlay. It clips the content with three rectangles. In JavaFX clipping, black means opaque and transparent means clipped away, so the gradients become soft reveal masks."),
            Table(["Content position", "Left clip", "Right clip"], [["content.translateX &lt; 0", "transparent → black gradient", "depends on right overflow"], ["content.translateX ≥ 0", "solid black", "depends on right overflow"], ["translated right edge &gt; view width", "depends on left overflow", "black → transparent gradient"], ["translated right edge ≤ view width", "depends on left overflow", "solid black"]], widths=[36,32,32]),
            Code("""double fadingSize = Math.min(contentWidth / 2, getSkinnable().getFadingSize());
leftClip.resizeRelocate(x, y, fadingSize, contentHeight);
centerClip.resizeRelocate(x + fadingSize, y, contentWidth - 2 * fadingSize, contentHeight);
rightClip.resizeRelocate(x + contentWidth - fadingSize, y, fadingSize, contentHeight);""", caption="The three clip rectangles laid out by the skin."),
            Callout("Only horizontal overflow is handled. The verified skin listens to content.translateX and the control width, not to vertical movement.", kind="note"),
        ]),
        Chapter("Styling", [
            Para('MaskedView does not return a user-agent stylesheet in the verified source, but it still exposes a root style class and styleable CSS properties where listed below.'),
            Figure(f"{G}/styling.svg", "Style hooks for MaskedView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.masked-view {\n    -fx-fading-size: 80px;\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for MaskedView."),
        ]),
        Chapter("Accessibility", [
            Para('MaskedView sets AccessibleRole.IMAGE_VIEW and focusTraversable false. No accessible text is bound.'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('HBox strip = new HBox(cards);\nMaskedView masked = new MaskedView(strip);\nmasked.setFadingSize(80);\nstrip.setTranslateX(-120);'),
            Section("Practical checklist"),
            Numbered(['Move the content with translateX to activate edge fades.', 'Keep fadingSize no larger than half the useful viewport.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('No user-agent stylesheet and no resource bundle exist for MaskedView.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.StripViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.StripViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
