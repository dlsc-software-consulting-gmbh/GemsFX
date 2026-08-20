"""Content of the LimitedTextArea developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "limited-text-area"

MANUAL = Manual(
    control='LimitedTextArea',
    package='com.dlsc.gemsfx',
    subtitle='Resizable text input with character range feedback',
    abstract='LimitedTextArea builds on ResizableTextArea and adds a bottom bar with tips, a remaining-character label, a circular progress indicator and warning/error states.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of LimitedTextArea.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>LimitedTextArea</b> is a <font face='Courier'>ResizableTextArea</font> specialized for text length guidance. It does not block typing beyond the configured range; instead it reports and styles normal, warning and error states."),
                Section('Key features'),
                Bullets(['Character range limit with minimum and maximum.', 'Bottom bar with optional tips and a length indicator.', 'Circular progress indicator up to the maximum length.', 'Warning and error pseudo classes.', 'Automatic removal of configured excluded text fragments.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('LimitedTextArea area = new LimitedTextArea();\narea.setWrapText(true);\narea.setCharacterRangeLimit(new IntegerRange(0, 140));\narea.setTips("Write a short summary.");\narea.setLengthDisplayMode(LimitedTextArea.LengthDisplayMode.ALWAYS_SHOW);\n\narea.outOfRangeProperty().addListener((obs, old, out) -> saveButton.setDisable(out));', caption='A complete limited text area setup.'),
                Figure(f"{G}/states.svg", 'Normal, warning and error length states.')
            ]),
        Chapter('Anatomy', [
                Figure(f"{G}/anatomy.svg", 'Parts added by LimitedTextAreaSkin.'),
                Table(['Part', 'Node', 'Description'], [['Content', 'inherited resizable content pane', 'TextArea with resize corner.'], ['Bottom box', "<font face='Courier'>.bottom-box</font>", 'Contains tips, spacer and length indicator.'], ['Tips', "<font face='Courier'>.tips</font>", "Label bound to <font face='Courier'>tips</font>, hidden when empty."], ['Length indicator', "<font face='Courier'>.length-indicator</font>", 'Stacks label and CircleProgressIndicator.']], widths=[24, 32, 44])
            ]),
        Chapter('Control API', [
                Section('Limits and feedback'),
                PropertyTable([Property('characterRangeLimit', 'ObjectProperty&lt;IntegerRange&gt;', 'null', 'Minimum and maximum allowed length. Null or max &lt;= 0 disables range feedback.'), Property('outOfRange', 'ReadOnlyBooleanProperty', 'false', 'True when text length is below min or above max.'), Property('warningThreshold', 'DoubleProperty', '0.9', 'Warning starts at max * clamped threshold, unless already in error.'), Property('lengthDisplayMode', 'ObjectProperty&lt;LengthDisplayMode&gt;', 'AUTO', 'AUTO, ALWAYS_SHOW or ALWAYS_HIDE.')]),
                Section('Bottom bar and filtering'),
                PropertyTable([Property('showBottom', 'BooleanProperty', 'true', 'Shows the bottom box. Styleable.'), Property('tips', 'StringProperty', 'null', 'Hint text displayed at the bottom left.'), Property('excludedItems', 'ObservableList&lt;String&gt;', 'empty list', "Fragments removed from text using <font face='Courier'>replaceAll</font>."), Property('resizeVertical', 'BooleanProperty', 'true', 'Inherited from ResizableTextArea.'), Property('resizeHorizontal', 'BooleanProperty', 'false', 'Inherited from ResizableTextArea.')])
            ]),
        Chapter('Length display modes', [
                Figure(f"{G}/flow.svg", 'Filtering and range checking flow.'),
                Table(['Mode', 'Indicator behaviour'], [["<font face='Courier'>AUTO</font>", 'Label appears only for warning or error; progress appears while max is known and text is not over max.'], ["<font face='Courier'>ALWAYS_SHOW</font>", 'Label is always visible; progress follows the max.'], ["<font face='Courier'>ALWAYS_HIDE</font>", 'Label and progress are hidden.']], widths=[30, 70]),
                Para('Without a positive maximum the label displays the current text length only when the selected mode makes it visible. With a maximum it displays remaining characters, abbreviated with k, M, G, T, P or E for large values.')
            ]),
        Chapter('Filtering and state rules', [
                Para("The skin updates text and pseudo classes whenever text, excluded items, warning threshold or character range changes. Filtering runs with <font face='Courier'>Platform.runLater</font> so it happens after the current edit event."),
                Table(['Rule', 'Result'], [['No range or max &lt;= 0', 'Progress is zero and warning / error pseudo classes are false.'], ['Length below min', "<font face='Courier'>:error</font> and <font face='Courier'>outOfRange=true</font>."], ['Length at max * threshold', "<font face='Courier'>:warning</font> unless the field is already in error."], ['Length above max', "<font face='Courier'>:error</font>; progress indicator is hidden."]], widths=[38, 62]),
                Code('''area.setCharacterRangeLimit(new IntegerRange(10, 120));
area.setWarningThreshold(0.85);
area.getExcludedItems().addAll("\t", " {2,}");'''),
                Callout("Excluded items are joined with <font face='Courier'>|</font> and passed to <font face='Courier'>String.replaceAll</font>, so escape regular-expression metacharacters when you want literal matching.", kind='warning')
            ]),
        Chapter('Styling', [
                Section('Style classes and pseudo classes'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.limited-text-area</font>", 'root control'], ["<font face='Courier'>.limited-text-area:warning</font>", 'text length at or above warning threshold'], ["<font face='Courier'>.limited-text-area:error</font>", 'text length below min or above max'], ["<font face='Courier'>.content-box</font>", 'VBox assembled by the skin'], ["<font face='Courier'>.bottom-box</font>", 'bottom status bar'], ["<font face='Courier'>.length-label</font>", 'remaining / count label']], widths=[46, 54]),
                Section('Styleable CSS properties'),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-show-bottom</font>", 'boolean', 'true'], ["<font face='Courier'>-fx-warning-threshold</font>", 'number', '0.9'], ["<font face='Courier'>-fx-length-display-mode</font>", 'LengthDisplayMode', 'AUTO']], widths=[50, 25, 25]),
                Code('.limited-text-area {\n    -fx-show-bottom: true;\n    -fx-warning-threshold: .8;\n    -fx-length-display-mode: always-show;\n}\n\n.limited-text-area:error > .content-box > .content-pane {\n    -fx-background-color: #ff5700, -fx-text-box-border, -fx-control-inner-background;\n}')
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.TEXT_AREA</font>. The visual length indicator is not automatically announced, so bind your own accessible help text if remaining count is critical to the workflow.")
            ]),
        Chapter('Recipes', [
                Section('Disallow line breaks'),
                Code('area.getExcludedItems().setAll("\\r", "\\n");'),
                Section('Hide the bottom bar'),
                Code('area.setShowBottom(false);'),
                Section('Warn earlier'),
                Code('area.setWarningThreshold(0.75);'),
                Section('Checklist'),
                Numbered(['Remember that limits style the field; they do not prevent typing.', "Treat <font face='Courier'>excludedItems</font> values as regular-expression fragments.", "Observe <font face='Courier'>outOfRange</font> before saving.", 'Use inherited resize settings when layout must be fixed.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.LimitedTextAreaApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.LimitedTextAreaApp</font>)", "<font face='Courier'>ResizableTextArea</font> - superclass providing the resize corner.", "<font face='Courier'>ExpandingTextArea</font> - automatic height text area.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
