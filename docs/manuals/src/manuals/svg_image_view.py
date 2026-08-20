"""Content of the SVGImageView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "svg-image-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('svgUrl', 'StringProperty', 'null', 'Validated URL or resource path for the SVG.'), ('fitWidth', 'DoubleProperty', '0', 'Requested fit box width; 0 uses intrinsic width.'), ('fitHeight', 'DoubleProperty', '0', 'Requested fit box height; 0 uses intrinsic height.'), ('preserveRatio', 'BooleanProperty', 'true', 'Bound to ImageView.preserveRatio.'), ('smooth', 'BooleanProperty', 'true', 'Bound to ImageView.smooth.'), ('backgroundLoading', 'BooleanProperty', 'false', 'Loads through a Service when true.')]
CSS_PROPS = [('-fx-svg-url', 'URL/String', 'null'), ('-fx-fit-width', 'size', '0'), ('-fx-fit-height', 'size', '0'), ('-fx-preserve-ratio', 'boolean', 'true'), ('-fx-smooth', 'boolean', 'true'), ('-fx-background-loading', 'boolean', 'false')]
SELECTORS = ['.svg-image-view (root style class only)']
LOC = []

MANUAL = Manual(
    control="SVGImageView",
    package="com.dlsc.gemsfx",
    subtitle='High-definition SVG rendering through jsvg',
    abstract='SVGImageView renders SVG files into JavaFX Images via com.github.weisj:jsvg and displays them in an ImageView with fit width, fit height, smoothing and aspect-ratio controls.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of SVGImageView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>SVGImageView</b> SVGImageView renders SVG files into JavaFX Images via com.github.weisj:jsvg and displays them in an ImageView with fit width, fit height, smoothing and aspect-ratio controls."),
            Section("Key features"),
            Bullets(['Uses com.github.weisj.jsvg through SVGUtil.', 'svgUrl accepts absolute URLs or classpath resources and validates them.', 'fitWidth and fitHeight default to 0, meaning intrinsic SVG size is used.', 'preserveRatio and smooth default to true.', 'backgroundLoading defaults to false; true uses a JavaFX Service.', 'The skin reloads when URL, fit size or window render scale changes.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('SVGImageView icon = new SVGImageView();\nicon.setSvgUrl(getClass().getResource("microphone.svg").toExternalForm());\nicon.setFitWidth(96);\nicon.setFitHeight(96);\nicon.setPreserveRatio(true);\nicon.setBackgroundLoading(false);', caption="A compact setup for SVGImageView."),
            Figure(f"{G}/cover.svg", "A generated overview of SVGImageView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of SVGImageView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.svg-image-view</font> is added by the constructor."], ['Stylesheet', 'No user-agent stylesheet is returned by the control source.'], ['Renderer', "Skin uses <font face='Courier'>SVGUtil</font>, which renders through <font face='Courier'>com.github.weisj.jsvg</font> to a JavaFX Image."]], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of SVGImageView."),
            Bullets(['validateUrl rejects null, blank and unresolved resource strings.', 'The skin binds ImageView fitWidth, fitHeight, preserveRatio and smooth to the control.', 'Synchronous loading calls SVGUtil.parseSVGFromUrl with fit size multiplied by window render scale.', 'Background loading cancels any running service and restarts it.', 'SVGUtil renders with jsvg to BufferedImage, then converts through SwingFXUtils.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through SVGImageView."),
        ]),
        Chapter("Layout and rendering", [
            Para('The SVG document is parsed by jsvg, rendered to a BufferedImage at the requested fit size and window render scale, then converted to a JavaFX Image for an ImageView.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for SVGImageView."),
            Table(["Concern", "Rule"], [['Intrinsic size', 'fitWidth / fitHeight <= 0 keeps intrinsic SVG dimensions in SVGUtil.'], ['Aspect ratio', 'SVGUtil derives the missing requested dimension from SVG aspect ratio; ImageView also binds preserveRatio.'], ['Render scale', 'Window renderScaleX/Y triggers reload for HiDPI output.']], widths=[32,68]),
        ]),
        Chapter("Styling", [
            Para('SVGImageView does not return a user-agent stylesheet in the verified source, but it still exposes a root style class and styleable CSS properties where listed below.'),
            Figure(f"{G}/styling.svg", "Style hooks for SVGImageView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.svg-image-view {\n    -fx-fit-width: 96px;\n    -fx-fit-height: 96px;\n    -fx-preserve-ratio: true;\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for SVGImageView."),
        ]),
        Chapter("Accessibility", [
            Para('SVGImageView sets AccessibleRole.IMAGE_VIEW and focusTraversable false. The source does not bind accessible text.'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('SVGImageView icon = new SVGImageView();\nicon.setSvgUrl(getClass().getResource("microphone.svg").toExternalForm());\nicon.setFitWidth(96);\nicon.setFitHeight(96);\nicon.setPreserveRatio(true);\nicon.setBackgroundLoading(false);'),
            Section("Practical checklist"),
            Numbered(['Pass classpath resources or fully qualified URLs to setSvgUrl.', 'Use backgroundLoading for remote or slow SVGs.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('No user-agent CSS file and no resource bundle exist for SVGImageView.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.SVGImageViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.SVGImageViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
