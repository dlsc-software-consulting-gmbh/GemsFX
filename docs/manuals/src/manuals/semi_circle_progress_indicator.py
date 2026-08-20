"""Content of the SemiCircleProgressIndicator developer manual."""

from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, PageBreak, Para, Property, PropertyTable, Section, Table

G = "semi-circle-progress-indicator"

MANUAL = Manual(
    control="SemiCircleProgressIndicator",
    package="com.dlsc.gemsfx",
    subtitle="A half-circle progress indicator for compact status panels",
    abstract=("SemiCircleProgressIndicator is a concrete ArcProgressIndicator that renders a 180 degree track with the label above the arc center. It supports the same converter, graphic, style type, CSS and accessibility behaviour as the circular variant."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="A semi-circular progress indicator with inherited arc progress behaviour.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>SemiCircleProgressIndicator</b> extends <font face='Courier'>ArcProgressIndicator</font> and uses <font face='Courier'>SemiCircleProgressIndicatorSkin</font>. It is suited to dashboard cards, headers and other layouts where a full circle would consume too much vertical space."),
            Bullets(["Default constructor starts indeterminate.", "The <font face='Courier'>SemiCircleProgressIndicator(double)</font> constructor sets initial progress.", "Minimum size is 26 x 26 pixels.", "Root style classes are <font face='Courier'>arc-progress-indicator</font> and <font face='Courier'>semi-circle-progress-indicator</font>."]),
            Code("""SemiCircleProgressIndicator indicator = new SemiCircleProgressIndicator(0.0);
indicator.setProgress(0.60);
indicator.setStyleType(ArcProgressIndicator.StyleType.DEFAULT);"""),
        ]),
        Chapter("Getting started", [
            Para("The API is the same as for the circular variant, except there is no startAngle property. The half circle always uses a 180 degree track."),
            Code("""Task<Void> task = createTask();
SemiCircleProgressIndicator indicator = new SemiCircleProgressIndicator();
indicator.progressProperty().bind(task.progressProperty());
new Thread(task, "import-task").start();"""),
            Figure(f"{G}/states.svg", "The semi-circle in indeterminate, determinate and completed states."),
            Callout("The default converter is inherited from ArcProgressIndicator, so completed progress shows the localized Completed text.", kind="note"),
        ]),
        Chapter("Anatomy", [
            Figure(f"{G}/anatomy.svg", "The parts rendered by SemiCircleProgressIndicatorSkin."),
            Table(["Part", "Node", "Description"], [["Track", "Arc", "Start angle 0, length 180."], ["Progress", "Arc", "Foreground arc; length is -180 * progress."], ["Label", "Label", "Positioned above the arc center and constrained to half the diameter in height."], ["Animation", "Timeline", "Animates start angle 180 -> 90 -> 0 and length 0 -> -60 -> 0."]], widths=[20, 20, 60]),
            PageBreak(),
        ]),
        Chapter("Control API", [
            PropertyTable([
                Property("converter", "ObjectProperty&lt;StringConverter&lt;Double&gt;&gt;", "percent converter", "Inherited. Converts progress to text for the label."),
                Property("graphic", "ObjectProperty&lt;Node&gt;", "null", "Inherited. Optional graphic displayed by the label."),
                Property("progressArcType", "ObjectProperty&lt;ArcType&gt;", "OPEN", "Inherited and styleable."),
                Property("trackArcType", "ObjectProperty&lt;ArcType&gt;", "CHORD", "Inherited and styleable."),
                Property("styleType", "ObjectProperty&lt;StyleType&gt;", "DEFAULT", "Inherited. DEFAULT, BOLD, THIN or SECTOR."),
            ]),
        ]),
        Chapter("Behaviour and states", [
            Figure(f"{G}/styles.svg", "The same style types are available for the semi-circle."),
            Table(["State", "Condition", "Skin behaviour"], [["indeterminate", "progress &lt; 0", "Timeline sweeps a short arc across the half-circle."], ["determinate", "0.0 .. 1.0", "Progress length is -180 times the progress value."], ["completed", "progress == 1.0", "The :completed pseudo-class is active."]], widths=[24, 25, 51]),
        ]),
        Chapter("Layout and sizing", [
            Figure(f"{G}/layout.svg", "The half-circle radius uses width and twice the available height."),
            Para("The radius binding uses the smaller of available width and twice the available height, then divides by two. The arc center is below the vertical center so the visible half-circle fits the content area."),
            Code("""// Useful in compact cards:
indicator.setPrefSize(180, 90);
indicator.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);"""),
        ]),
        Chapter("Styling with CSS", [
            Table(["Selector", "Effect"], [["<font face='Courier'>.semi-circle-progress-indicator</font>", "Root class loaded from semi-circle-progress-indicator.css."], ["<font face='Courier'>.track-circle</font>", "Light grey track."], ["<font face='Courier'>.progress-arc</font>", "Foreground arc uses -fx-accent."], ["<font face='Courier'>:bold-style</font>", "Track 10px, progress 5px."], ["<font face='Courier'>:thin-style</font>", "Track and progress 1px."], ["<font face='Courier'>:sector-style</font>", "Uses ROUND progress arc type and filled sector styling."], ["<font face='Courier'>:completed</font>", "Hook when progress is exactly 1.0."]], widths=[45, 55]),
            Table(["CSS property", "Type", "Default"], [["<font face='Courier'>-fx-style-type</font>", "StyleType", "DEFAULT"], ["<font face='Courier'>-fx-progress-arc-type</font>", "ArcType", "OPEN"], ["<font face='Courier'>-fx-track-arc-type</font>", "ArcType", "CHORD"]], widths=[48, 26, 26]),
            Code(""".semi-circle-progress-indicator {
    -fx-style-type: bold;
}

.semi-circle-progress-indicator .track-circle {
    -fx-stroke: #e5e7eb;
}"""),
        ]),
        Chapter("Localization", [
            Para("SemiCircleProgressIndicator inherits the arc-progress-indicator resource bundle."),
            Table(["Key", "English text"], [["<font face='Courier'>status.completed</font>", "Completed"], ["<font face='Courier'>accessible.text.loading</font>", "loading"], ["<font face='Courier'>accessible.text.percent</font>", "{0} percent"]], widths=[45, 55]),
        ]),
        Chapter("Accessibility", [
            Para("The inherited constructor sets <font face='Courier'>AccessibleRole.PROGRESS_INDICATOR</font>. Accessible text follows progress as localized loading text or a rounded percentage until the application sets its own accessible text."),
            PageBreak(),
        ]),
        Chapter("Recipes and demo", [
            Section("Use the demo style combo"), Code("""ComboBox<ArcProgressIndicator.StyleType> styles = new ComboBox<>();
styles.getItems().addAll(ArcProgressIndicator.StyleType.values());
styles.valueProperty().bindBidirectional(indicator.styleTypeProperty());"""),
            Section("Domain-specific label"), Code("""indicator.setConverter(new StringConverter<>() {
    @Override public String toString(Double p) {
        return p == null || p < 0 ? "Connecting" : String.format("%.0f%%", p * 100);
    }
    @Override public Double fromString(String text) { return null; }
});"""),
            Numbered(["Choose the semi-circle when vertical space is limited.", "Reserve enough width for the label.", "Use styleType for broad visual changes, CSS selectors for precise colors."]),
            PageBreak()
        ]),
        Chapter("Troubleshooting", [Bullets(["There is no startAngle property on this control.", "If the label is clipped, increase the control height or use a shorter converter string.", "Use exactly 1.0 to enable :completed styling."])]),
    ],
)
