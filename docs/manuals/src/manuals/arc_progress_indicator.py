"""Content of the ArcProgressIndicator developer manual."""

from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, PageBreak, Para, Property, PropertyTable, Section, Table

G = "arc-progress-indicator"

MANUAL = Manual(
    control="ArcProgressIndicator",
    package="com.dlsc.gemsfx",
    subtitle="The abstract base class for arc-shaped progress indicators",
    abstract=(
        "ArcProgressIndicator is the common ProgressIndicator base used by the circle and semi-circle variants. "
        "It contributes the shared properties, converter, graphic slot, CSS metadata, pseudo-classes, localization "
        "and accessibility behaviour; concrete subclasses provide the actual skin geometry."
    ),
    cover_svg=f"{G}/cover.svg",
    cover_caption="The shared arc progress model renders a track, progress arc and central label or graphic.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>ArcProgressIndicator</b> extends <font face='Courier'>javafx.scene.control.ProgressIndicator</font>. It is declared <b>abstract</b>, so applications normally create <font face='Courier'>CircleProgressIndicator</font> or <font face='Courier'>SemiCircleProgressIndicator</font>. The class is still important because all arc-based indicators inherit its API."),
            Bullets(["Determinate progress uses values from <font face='Courier'>0.0</font> to <font face='Courier'>1.0</font>.", "Indeterminate progress uses the inherited <font face='Courier'>ProgressIndicator.INDETERMINATE_PROGRESS</font> value.", "A central label is produced by a <font face='Courier'>StringConverter&lt;Double&gt;</font>.", "A custom <font face='Courier'>graphic</font> can replace or accompany the label node.", "The <font face='Courier'>completed</font> pseudo-class is active when progress is exactly <font face='Courier'>1.0</font>."]),
            Code("""private void configure(ArcProgressIndicator indicator) {
    indicator.setProgress(0.35);
    indicator.setStyleType(ArcProgressIndicator.StyleType.BOLD);
    indicator.setProgressArcType(ArcType.OPEN);
}""", caption="Configure the shared API on any concrete arc progress indicator."),
            Callout("Use a concrete subclass when creating UI. The base class has no direct default skin of its own.", kind="warning"),
        ]),
        Chapter("Getting started", [
            Para("Start with a concrete subclass, then configure it through the inherited base properties. Passing <font face='Courier'>0.0</font> avoids creating the indeterminate animation during construction."),
            Code("""CircleProgressIndicator indicator = new CircleProgressIndicator(0.0);
indicator.setProgress(0.42);
indicator.setConverter(new StringConverter<>() {
    @Override
    public String toString(Double value) {
        return value == null || value < 0 ? "Working" : String.format("%.0f%%", value * 100);
    }

    @Override
    public Double fromString(String text) {
        return null;
    }
});"""),
            Figure(f"{G}/states.svg", "The three progress states inherited from ProgressIndicator plus the completed pseudo-class."),
        ]),
        Chapter("Anatomy", [
            Para("The base skin used by the subclasses lays out three children: a track arc, a progress arc and a label. The label binds to both <font face='Courier'>converter</font> and <font face='Courier'>graphic</font>."),
            Figure(f"{G}/anatomy.svg", "Shared parts contributed by the arc progress base implementation."),
            Table(["Part", "Style class", "Description"], [["Track arc", "<font face='Courier'>track-circle</font>", "The full background arc; its type is bound to <font face='Courier'>trackArcType</font>."], ["Progress arc", "<font face='Courier'>progress-arc</font>", "The foreground arc; its length is computed from <font face='Courier'>progress</font>."], ["Progress label", "<font face='Courier'>progress-label</font>", "Shows converter text and binds its graphic to <font face='Courier'>graphic</font>."]], widths=[23, 30, 47]),
            PageBreak(),
        ]),
        Chapter("Control API", [
            PropertyTable([
                Property("converter", "ObjectProperty&lt;StringConverter&lt;Double&gt;&gt;", "percent converter", "Converts progress to label text. Negative or null progress maps to an empty string; 1.0 maps to localized 'Completed'; other values are floored percentages."),
                Property("graphic", "ObjectProperty&lt;Node&gt;", "null", "Custom node shown by the label. The label remains visible when a graphic is present."),
                Property("progressArcType", "ObjectProperty&lt;ArcType&gt;", "OPEN", "Arc type used for the foreground progress arc. Styleable."),
                Property("trackArcType", "ObjectProperty&lt;ArcType&gt;", "CHORD", "Arc type used for the background track arc. Styleable."),
                Property("styleType", "ObjectProperty&lt;StyleType&gt;", "DEFAULT", "Visual style: DEFAULT, BOLD, THIN or SECTOR. Styleable and reflected by pseudo-classes."),
            ]),
            Section("Inherited progress"),
            Para("The inherited <font face='Courier'>progressProperty()</font> drives the arc length. Values below zero are indeterminate. A value of <font face='Courier'>1.0</font> activates <font face='Courier'>:completed</font>."),
        ]),
        Chapter("Behaviour and states", [
            Figure(f"{G}/styles.svg", "The StyleType enum changes pseudo-classes that the concrete CSS files style."),
            Table(["State", "Condition", "Effect"], [["indeterminate", "progress &lt; 0", "Subclass animation timeline is created lazily and played while visible."], ["determinate", "0.0 .. 1.0", "Animation stops and progress arc length becomes maxLength * progress."], ["completed", "progress == 1.0", "<font face='Courier'>:completed</font> pseudo-class is active and default text is localized."]], widths=[24, 25, 51]),
        ]),
        Chapter("Layout and sizing", [
            Figure(f"{G}/layout.svg", "Subclasses compute their radius from the available control bounds."),
            Para("The base skin positions arcs manually in <font face='Courier'>layoutChildren()</font>. The concrete skin supplies the radius binding, arc center and label position. Insets and stroke widths reduce the usable drawing radius."),
        ]),
        Chapter("Styling", [
            Para("The base stylesheet is <font face='Courier'>arc-progress-indicator.css</font>. Concrete controls load their own stylesheets and repeat the same child style classes."),
            Table(["Selector", "Purpose"], [["<font face='Courier'>.arc-progress-indicator</font>", "Root style class inherited by concrete controls."], ["<font face='Courier'>.track-circle</font>", "Track stroke; default stroke width 3px, stroke -fx-box-border, transparent fill."], ["<font face='Courier'>.progress-arc</font>", "Progress stroke; default stroke width 3px, stroke -fx-accent, transparent fill."], ["<font face='Courier'>.progress-label</font>", "Centered text and graphic label."]], widths=[43, 57]),
            Table(["CSS property", "Type", "Default"], [["<font face='Courier'>-fx-progress-arc-type</font>", "ArcType", "OPEN"], ["<font face='Courier'>-fx-track-arc-type</font>", "ArcType", "CHORD"], ["<font face='Courier'>-fx-style-type</font>", "StyleType", "DEFAULT"]], widths=[48, 26, 26]),
            Code(""".circle-progress-indicator {
    -fx-style-type: bold;
    -fx-progress-arc-type: open;
    -fx-track-arc-type: chord;
}

.circle-progress-indicator .progress-arc {
    -fx-stroke: #22c55e;
}"""),
        ]),
        Chapter("Localization", [
            Para("The base class uses <font face='Courier'>ResourceBundleManager.BundleType.ARC_PROGRESS_INDICATOR</font>."),
            Table(["Key", "English text", "Used for"], [["<font face='Courier'>status.completed</font>", "Completed", "Default converter text at progress 1.0."], ["<font face='Courier'>accessible.text.loading</font>", "loading", "Accessible text while indeterminate."], ["<font face='Courier'>accessible.text.percent</font>", "{0} percent", "Accessible determinate progress text."]], widths=[36, 28, 36]),
        ]),
        Chapter("Accessibility", [
            Para("The constructor sets <font face='Courier'>AccessibleRole.PROGRESS_INDICATOR</font>. Its accessible text is bound to progress until application code supplies its own text. Indeterminate progress reads the localized loading text; determinate progress reads a localized rounded percentage."),
            Code("""indicator.setAccessibleText("Synchronization progress");
// Application-set text intentionally takes over from the automatic binding."""),
                    PageBreak(),
        ]),
        Chapter("Recipes", [
            Section("Show domain text"), Code("""indicator.setConverter(new StringConverter<>() {
    @Override public String toString(Double p) {
        return p == null || p < 0 ? "Connecting" : String.format("Downloading %.0f%%", p * 100);
    }
    @Override public Double fromString(String text) { return null; }
});"""),
            Section("Use a graphic instead of text"), Code("""FontIcon icon = new FontIcon("mdi-cloud-download");
indicator.setConverter(null);
indicator.setGraphic(icon);"""),
            PageBreak(),
        ]),
        Chapter("Troubleshooting", [
            Bullets(["Do not instantiate <font face='Courier'>ArcProgressIndicator</font> directly; it is abstract.", "Use <font face='Courier'>new CircleProgressIndicator(0.0)</font> when you need no initial animation.", "If a CSS style type appears to do nothing, make sure you are styling the concrete root class as well as the inherited child classes.", "Set progress to exactly <font face='Courier'>1.0</font> if you rely on <font face='Courier'>:completed</font>."])
        ]),
    ],
)
