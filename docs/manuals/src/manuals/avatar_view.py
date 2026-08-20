"""Content of the AvatarView developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table
G = "avatar-view"

def esc(value):
    return value.replace("<", "&lt;").replace(">", "&gt;")

PROPS = [('initials', 'StringProperty', 'null', 'Initials shown when no fully-loaded image is available.'), ('image', 'ObjectProperty<Image>', 'null', 'Avatar image; shown once loaded.'), ('numberOfStyles', 'IntegerProperty', '5', 'Modulo base for style0 ... styleN classes.'), ('arcSize', 'DoubleProperty', '10', 'Corner arc for square avatars; styleable.'), ('size', 'DoubleProperty', '50', 'Avatar width and height in pixels; styleable.'), ('avatarShape', 'ObjectProperty<AvatarShape>', 'SQUARE', 'SQUARE or ROUND clipping; styleable.'), ('magicNumber', 'IntegerProperty', '-1, private', 'Derived from initials and used to select a style class.')]
CSS_PROPS = [('-fx-avatar-shape', 'AvatarShape', 'SQUARE'), ('-fx-avatar-arc-size', 'size', '10'), ('-fx-avatar-size', 'size', '50')]
SELECTORS = ['.avatar-view', '.avatar-view.style0 ... .style4', '> .icon-wrapper', '> .icon-wrapper .icon', '> .text-wrapper', '> .text-wrapper > .initials-text', '> .image-wrapper', '> .wrapper-stack-pane']
LOC = [('accessible.text.avatar', 'avatar'), ('accessible.text.avatar-of', 'avatar of {0}')]

MANUAL = Manual(
    control="AvatarView",
    package="com.dlsc.gemsfx",
    subtitle='Initials, image and fallback avatar display',
    abstract='AvatarView is an image-oriented control for user avatars. It displays a loaded image when available, falls back to initials, and finally shows a default person icon.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of AvatarView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>AvatarView</b> AvatarView is an image-oriented control for user avatars. It displays a loaded image when available, falls back to initials, and finally shows a default person icon."),
            Section("Key features"),
            Bullets(['Fallback chain: loaded image, then initials, then a default icon.', 'Initials produce deterministic style classes style0 through style4 by default.', 'AvatarShape.SQUARE is the default; AvatarShape.ROUND clips to a circle.', 'size defaults to 50 and is bound to pref/min/max width and height.', 'arcSize defaults to 10 and rounds square avatars.', 'Accessible text is localized and changes with initials.']),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="The control lives in module <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The following snippet uses only public API verified in the control source."),
            Code('AvatarView avatar = new AvatarView("LD");\navatar.setImage(userImage);          // image wins once loaded\navatar.setAvatarShape(AvatarShape.ROUND);\navatar.setSize(96);\navatar.setArcSize(18);              // used only for square avatars', caption="A compact setup for AvatarView."),
            Figure(f"{G}/cover.svg", "A generated overview of AvatarView in use."),
        ]),
        Chapter("Anatomy", [
            Para("The anatomy diagram identifies the implementation pieces that matter when configuring, styling or debugging the control."),
            Figure(f"{G}/anatomy.svg", "The parts of AvatarView."),
            Table(["Part", "Verified detail"], [['Root', "Style class <font face='Courier'>.avatar-view</font> is added by the constructor."], ['Stylesheet', "User-agent stylesheet <font face='Courier'>avatar-view.css</font> is returned by the control."]], widths=[32,68]),
        ]),
        Chapter("Control API", [
            Section("Properties and callbacks"),
            PropertyTable([Property(name, esc(type_), esc(default), desc) for name, type_, default, desc in PROPS]),
            Callout("Defaults and property names in this table were checked against the Java source for this batch.", kind="note"),
        ]),
        Chapter("Behaviour", [
            Figure(f"{G}/states.svg", "Important runtime states of AvatarView."),
            Bullets(['AvatarViewSkin shows imageWrapper if image is non-null and fully loaded.', 'If no loaded image exists and initials are not blank, textWrapper is shown.', 'If both image and initials are absent, iconWrapper is shown.', 'Changing initials recalculates the magic number and replaces style classes with avatar-view plus one styleN class.', 'All three wrapper panes receive clips based on avatarShape.']),
            Figure(f"{G}/flow.svg", "How data and geometry flow through AvatarView."),
        ]),
        Chapter("Layout and rendering", [
            Para('Rendering is a simple fallback chain. A loaded image fills the avatar; otherwise initials are painted; otherwise a CSS-shaped icon is shown. Each visible wrapper is clipped to ROUND or SQUARE geometry.'),
            Figure(f"{G}/layout.svg", "Rendering and sizing rules for AvatarView."),
            Table(["Concern", "Rule"], [['Size', 'pref/min/max width and height are bound to size.'], ['Shape', 'ROUND uses a Circle clip; SQUARE uses a Rectangle clip with arcSize.'], ['Image scaling', 'ImageView scale is size divided by the smaller image dimension.']], widths=[32,68]),
        ]),
        Chapter("Styling", [
            Para('The user-agent stylesheet is avatar-view.css. The table lists selectors and pseudo classes that exist in source, skin or stylesheet.'),
            Figure(f"{G}/styling.svg", "Style hooks for AvatarView."),
            Section("Style classes and pseudo classes"),
            Table(["Selector / pseudo class", "Purpose"], [[f"<font face='Courier'>{selector}</font>", "Verified in source, skin or CSS."] for selector in SELECTORS], widths=[48,52]),
            Section("Styleable CSS properties"),
            Table(["CSS property", "Type", "Default"], [[f"<font face='Courier'>{prop}</font>", type_, default] for prop, type_, default in CSS_PROPS], widths=[48,26,26]) if CSS_PROPS else Para("This control declares no additional styleable CSS properties beyond inherited JavaFX properties."),
            Code('.avatar-view {\n    /* start with the documented root selector */\n}', caption="CSS example using documented hooks."),
        ]),
        Chapter("Localization", [
            Table(["Key", "English text"], [[f"<font face='Courier'>{key}</font>", text] for key, text in LOC], widths=[45,55]) if LOC else Para("The verified source has no ResourceBundleManager keys for AvatarView."),
        ]),
        Chapter("Accessibility", [
            Para('AvatarView sets AccessibleRole.IMAGE_VIEW. AccessibilityUtil.bindAccessibleText uses localized accessible.text.avatar when initials are blank, otherwise formats accessible.text.avatar-of with the initials, for example "avatar of LD".'),
        ]),
        Chapter("Recipes", [
            Section("Programmatic configuration"),
            Code('AvatarView avatar = new AvatarView("LD");\navatar.setImage(userImage);          // image wins once loaded\navatar.setAvatarShape(AvatarShape.ROUND);\navatar.setSize(96);\navatar.setArcSize(18);              // used only for square avatars'),
            Section("Practical checklist"),
            Numbered(['Set initials as a fallback even when an image is usually present.', 'Keep numberOfStyles aligned with CSS styleN rules.', 'Use the public properties listed in the API chapter.', 'Style only through documented selectors and styleable properties.', 'Do not depend on private skin node structure except for documented CSS selectors.']),
        ]),
        Chapter("Integration notes", [
            Para('No pseudo classes are set by the source.'),
            Table(["Topic", "Recommendation"], [["Threading", "Keep image loading and expensive rendering off the UI path when the control exposes a background option."], ["Styling", "Scope selectors under the documented root style class."], ["Accessibility", "Preserve the source-defined accessible role and add app-specific text when the control does not bind it."], ["State", "Prefer public properties over skin node lookup."]], widths=[30,70]),
        ]),
        Chapter("See also", [
            Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.AvatarViewApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.AvatarViewApp</font>)", "Related GemsFX media controls: <font face='Courier'>AvatarView</font>, <font face='Courier'>PhotoView</font>, <font face='Courier'>SVGImageView</font>, <font face='Courier'>BeforeAfterView</font>, <font face='Courier'>MaskedView</font>, <font face='Courier'>ScreensView</font>.", "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/"])
        ]),
    ],
)
