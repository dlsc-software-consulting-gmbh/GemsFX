"""Content of the ScreensView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "screens-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('shadow', 'ObjectProperty<DropShadow>', 'DropShadow radius 2, THREE_PASS_BOX', 'Shadow applied to the screen group when showShadow is true.'), ('reflection', 'ObjectProperty<Reflection>', 'fraction .25, topOffset 5', 'Reflection applied when showReflection is true.'), ('shapes', 'ObservableList<Shape>', 'empty list', 'Extra debug shapes added to the unified bounds.'), ('showShadow', 'BooleanProperty', 'true', 'Enables shadow effect.'), ('showReflection', 'BooleanProperty', 'true', 'Enables reflection effect.'), ('showWallpaper', 'BooleanProperty', 'true', 'Uses wallpaperProvider images.'), ('showWindows', 'BooleanProperty', 'false', 'Adds live WindowView nodes.'), ('wallpaperProvider', 'ObjectProperty<Callback<Screen, Image>>', 'screen -> DEFAULT_WALLPAPER', 'Supplies wallpaper image per screen.'), ('enableWindowDragging', 'BooleanProperty', 'true', 'Allows dragging live miniature windows.')]
CSS_PROPS = []
SELECTORS = ['.screens-view', '.container', '.screen', '.screen.no-wallpaper', '.screen .label', '.glass', '.visible-area', '.window', '.window .label']
LOC = [('window.title', 'Screens'), ('label.screen-index', 'Screen {0}'), ('label.primary', 'Primary'), ('accessible.role-description', 'screens')]

MANUAL = Manual(
    control="ScreensView",
    package="com.dlsc.gemsfx",
    subtitle='Scaled view of screens, windows and virtual desktop geometry',
    abstract='ScreensView visualizes the current JavaFX Screen list using real screen bounds, optional wallpapers, visual-bounds overlays, live windows and arbitrary debug shapes.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of ScreensView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>ScreensView</b> ScreensView visualizes the current JavaFX Screen list using real screen bounds, optional wallpapers, visual-bounds overlays, live windows and arbitrary debug shapes."),
            Section("Key features"),
            Bullets(['Uses Screen.getScreens() and preserves relative virtual-desktop geometry.', 'Default wallpaper is wallpaper.jpg from the control resources.', 'showShadow, showReflection, showWallpaper and showWindows are true, true, true and false by default.', 'Optional WindowView nodes bind to live Window x, y, width and height.', 'enableWindowDragging defaults to true and lets users move live miniature windows.', 'ScreensView.show() opens a utility Stage titled from the resource bundle.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('ScreensView view = new ScreensView();\nview.setShowWindows(true);\nview.setShowWallpaper(false);\nview.setWallpaperProvider(screen -> myWallpaperFor(screen));\nview.getShapes().add(debugRectangle);\nview.setEnableWindowDragging(true);', caption="A compact setup for ScreensView."),
            Figure(f"{G}/cover.svg", "A generated overview of ScreensView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of ScreensView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.screens-view</font> is added by the constructor."], ['Stylesheet', "User-agent stylesheet <font face='Courier'>screens-view.css</font> is returned by the control."], ['Screen model', "Skin reads <font face='Courier'>Screen.getScreens()</font> and <font face='Courier'>Window.getWindows()</font>."]], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of ScreensView."),
            Bullets(['The skin computes the union of all Screen bounds plus extra shapes.', 'Each screen group contains background, screen label, visible-area overlay and glass overlay.', 'The scaling group uses min(width / totalWidth, height / totalHeight) * .75.', 'If showWindows is true, WindowView nodes bind to live windows and stage titles.', 'Dragging a WindowView moves the real Window by screen delta divided by the scale.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through ScreensView."),
        ]),
        Chapter("Layout and rendering", [
            Para('Rendering preserves virtual desktop geometry. Screens, optional shapes and optional windows are placed in real coordinate space, then scaled into the control.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for ScreensView."),
            Table(["Concern", "Rule"], [['Bounds', 'Union includes all screens and custom shapes.'], ['Scale', 'min(width / totalWidth, height / totalHeight) * .75.'], ['Windows', 'WindowView binds layout and size to live Window properties.']], widths=[32,68]),
        ]),
        Chapter("Styling", [
            Para('The user-agent stylesheet is screens-view.css. The table lists selectors and pseudo classes that exist in source, skin or stylesheet.'),
            Figure(f"{G}/styling.svg", "Style hooks for ScreensView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.screens-view .screen.no-wallpaper {\n    -fx-background-color: lightblue;\n}\n.screens-view .window {\n    -fx-border-style: dashed;\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for ScreensView."),
        ]),
        Chapter("Accessibility", [
            Para('ScreensView sets AccessibleRole.NODE with localized role description "screens". No automatic accessible text for individual screens is bound.'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('ScreensView view = new ScreensView();\nview.setShowWindows(true);\nview.setShowWallpaper(false);\nview.setWallpaperProvider(screen -> myWallpaperFor(screen));\nview.getShapes().add(debugRectangle);\nview.setEnableWindowDragging(true);'),
            Section("Practical checklist"),
            Numbered(['Enable showWindows only for debugging or tooling views.', 'Use wallpaperProvider for per-screen backgrounds.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('ScreensView declares no styleable CSS properties; its options are JavaFX properties only.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.ScreensViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ScreensViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
