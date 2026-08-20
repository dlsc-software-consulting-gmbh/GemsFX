"""Content of the CircleProgressIndicator developer manual."""

from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, PageBreak, Para, Property, PropertyTable, Section, Table

G = "circle-progress-indicator"

MANUAL = Manual(
    control="CircleProgressIndicator",
    package="com.dlsc.gemsfx",
    subtitle="A circular determinate and indeterminate progress indicator",
    abstract=("CircleProgressIndicator is a concrete arc-based ProgressIndicator that draws a full circular track, a foreground progress arc and an optional centered text or graphic. It inherits the shared ArcProgressIndicator API and adds a startAngle property."),
    cover_svg=f"{G}/cover.svg",
    cover_caption="A full circular progress indicator with a centered label.",
    chapters=[
        Chapter("Introduction", [
            Para("<b>CircleProgressIndicator</b> extends <font face='Courier'>ArcProgressIndicator</font>. It is the concrete choice for download progress, import jobs, synchronization and other tasks where compact circular feedback fits the surrounding layout."),
            Bullets(["Default constructor starts indeterminate.", "The <font face='Courier'>CircleProgressIndicator(double)</font> constructor sets the initial progress.", "Minimum size is set to 26 x 26 pixels.", "The root style classes are <font face='Courier'>arc-progress-indicator</font> and <font face='Courier'>circle-progress-indicator</font>."]),
            Code("""CircleProgressIndicator indicator = new CircleProgressIndicator(0.0);
indicator.setProgress(0.35);
indicator.setStartAngle(90);
indicator.setStyleType(ArcProgressIndicator.StyleType.BOLD);"""),
        ]),
        Chapter("Getting started", [
            Para("Bind the inherited progress property to a task or service. Progress below zero makes the indicator indeterminate; progress from 0 to 1 draws that fraction of the circle."),
            Code("""Service<Void> service = createService();
CircleProgressIndicator indicator = new CircleProgressIndicator();
indicator.progressProperty().bind(service.progressProperty());
service.start();"""),
            Figure(f"{G}/states.svg", "Indeterminate, determinate and completed circle states."),
            Callout("Use <font face='Courier'>new CircleProgressIndicator(0.0)</font> when you want an empty determinate indicator instead of a running indeterminate animation.", kind="tip"),
        ]),
        Chapter("Anatomy", [
            Figure(f"{G}/anatomy.svg", "The parts rendered by CircleProgressIndicatorSkin."),
            Table(["Part", "Node", "Description"], [["Track", "Arc", "Full 360 degree background track, length 360."], ["Progress", "Arc", "Foreground arc; length is -360 * progress for determinate values."], ["Label", "Label", "Centered in the circular interior and bound to converter / graphic."], ["Rotation", "Rotate", "Applied to the progress arc while indeterminate."]], widths=[20, 20, 60]),
            PageBreak(),
        ]),
        Chapter("Control API", [
            PropertyTable([
                Property("startAngle", "DoubleProperty", "90.0", "Start angle of the progress arc, in degrees. 90 degrees puts the origin at the top of the circle."),
                Property("converter", "ObjectProperty&lt;StringConverter&lt;Double&gt;&gt;", "percent converter", "Inherited. Produces the centered label text."),
                Property("graphic", "ObjectProperty&lt;Node&gt;", "null", "Inherited. Custom graphic shown by the centered label."),
                Property("progressArcType", "ObjectProperty&lt;ArcType&gt;", "OPEN", "Inherited and styleable via -fx-progress-arc-type."),
                Property("trackArcType", "ObjectProperty&lt;ArcType&gt;", "CHORD", "Inherited and styleable via -fx-track-arc-type."),
                Property("styleType", "ObjectProperty&lt;StyleType&gt;", "DEFAULT", "Inherited. DEFAULT, BOLD, THIN or SECTOR."),
            ]),
        ]),
        Chapter("Behaviour and states", [
            Figure(f"{G}/styles.svg", "The built-in style types in the default CSS."),
            Para("The skin creates its indeterminate timeline lazily. The timeline rotates the progress arc from 0 to 360 degrees over 1.5 seconds while the arc length grows from 45 to 180 and back to 45 degrees."),
            Table(["Progress", "Label from default converter", "Pseudo-classes"], [["&lt; 0", "empty string", "inherited indeterminate"], ["0.0 ... 0.999", "floored percent, e.g. 99.8 becomes 99%", "inherited determinate"], ["1.0", "localized Completed", "determinate + completed"]], widths=[22, 48, 30]),
        ]),
        Chapter("Layout and sizing", [
            Figure(f"{G}/layout.svg", "The circular radius is derived from the smaller control dimension."),
            Para("The radius binding uses <font face='Courier'>(min(width, height) - max(insets) - maxStrokeWidth) / 2</font>. The label gets the full inner diameter for both width and height, then is centered on the arc center."),
        ]),
        Chapter("Styling with CSS", [
            Table(["Selector", "Effect"], [["<font face='Courier'>.circle-progress-indicator</font>", "Root class loaded from circle-progress-indicator.css."], ["<font face='Courier'>.track-circle</font>", "Light grey track in the concrete stylesheet."], ["<font face='Courier'>.progress-arc</font>", "Uses -fx-accent unless overridden."], ["<font face='Courier'>:bold-style</font>", "Track 10px, progress 5px."], ["<font face='Courier'>:thin-style</font>", "Track and progress 1px."], ["<font face='Courier'>:sector-style</font>", "Progress arc type ROUND and filled sector styling."], ["<font face='Courier'>:completed</font>", "Additional hook for progress == 1.0."]], widths=[44, 56]),
            Table(["CSS property", "Type", "Default"], [["<font face='Courier'>-fx-style-type</font>", "StyleType", "DEFAULT"], ["<font face='Courier'>-fx-progress-arc-type</font>", "ArcType", "OPEN"], ["<font face='Courier'>-fx-track-arc-type</font>", "ArcType", "CHORD"]], widths=[48, 26, 26]),
            Code(""".circle-progress-indicator {
    -fx-style-type: thin;
}

.circle-progress-indicator .progress-arc {
    -fx-stroke: -fx-accent;
}"""),
        ]),
        Chapter("Localization", [
            Para("CircleProgressIndicator inherits the arc-progress-indicator resource bundle."),
            Table(["Key", "English text"], [["<font face='Courier'>status.completed</font>", "Completed"], ["<font face='Courier'>accessible.text.loading</font>", "loading"], ["<font face='Courier'>accessible.text.percent</font>", "{0} percent"]], widths=[45, 55]),
        ]),
        Chapter("Accessibility", [
            Para("The inherited constructor sets <font face='Courier'>AccessibleRole.PROGRESS_INDICATOR</font> and binds accessible text to progress. The automatic binding yields after application code calls <font face='Courier'>setAccessibleText</font>."),
            PageBreak(),
        ]),
        Chapter("Recipes and demo", [
            Section("Start angle slider"), Code("""Slider slider = new Slider(0, 360, 90);
indicator.startAngleProperty().bind(slider.valueProperty());"""),
            Section("Custom converter from the demo"), Code("""StringConverter<Double> converter = new StringConverter<>() {
    @Override public String toString(Double p) {
        if (p == null || p < 0.0) return "Connecting";
        if (p.intValue() == 1) return "Download Complete";
        return String.format("Downloading %.0f%%", p * 100);
    }
    @Override public Double fromString(String text) { return null; }
};
indicator.setConverter(converter);"""),
            Numbered(["Create the indicator.", "Bind progress to a Service or Task.", "Choose StyleType from the combo box, as shown in the demo app.", "Optionally bind startAngle to a slider."]),
            PageBreak()
        ]),
        Chapter("Troubleshooting", [Bullets(["If nothing appears, check that the control has non-zero width and height.", "If text is too large, style <font face='Courier'>.progress-label</font> or provide a compact converter.", "If the animation keeps running off-screen, make the control invisible; the skin pauses when visibility is false."])]),
    ],
)
