"""Content of the ResizableTextArea developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "resizable-text-area"

MANUAL = Manual(
    control='ResizableTextArea',
    package='com.dlsc.gemsfx',
    subtitle='A TextArea with a draggable resize corner',
    abstract='ResizableTextArea extends TextArea with a resize handle that changes prefWidth and/or prefHeight according to two direction properties.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of ResizableTextArea.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>ResizableTextArea</b> is a <font face='Courier'>TextArea</font> with a skin-managed resize corner in the lower-right corner. Dragging the handle changes preferred size, so parent layouts can react normally."),
                Section('Key features'),
                Bullets(['Vertical resize enabled by default.', 'Horizontal resize disabled by default.', "Handle cursor follows <font face='Courier'>h-resize</font>, <font face='Courier'>v-resize</font>, <font face='Courier'>both-resize</font> or <font face='Courier'>no-resize</font>.", 'Drag respects positive max width and max height values.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('ResizableTextArea area = new ResizableTextArea("Resizable text area ...");\narea.setWrapText(true);\narea.setMinHeight(100);\narea.setResizeHorizontal(true);\narea.setResizeVertical(true);', caption='A complete setup with both resize directions enabled.'),
                Figure(f"{G}/cover.svg", 'The resize handle sits in the lower-right corner.')
            ]),
        Chapter('Anatomy', [
                Figure(f"{G}/anatomy.svg", 'Parts of a ResizableTextArea skin.'),
                Table(['Part', 'Node', 'Description'], [['Root', "<font face='Courier'>.resizable-text-area</font>", 'TextArea subclass.'], ['Content pane', "<font face='Courier'>.content-pane</font>", 'StackPane containing scroll pane and resize corner.'], ['Resize corner', "<font face='Courier'>.resize-corner</font>", 'Receives mouse press and drag events.'], ['Resize icon', "<font face='Courier'>.resize-icon</font>", 'Region with the bottom-right SVG shape.']], widths=[24, 30, 46])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('resizeVertical', 'BooleanProperty', 'true', "If true, dragging changes <font face='Courier'>prefHeight</font>."), Property('resizeHorizontal', 'BooleanProperty', 'false', "If true, dragging changes <font face='Courier'>prefWidth</font>."), Property('prefWidth', 'DoubleProperty', 'inherited', 'Updated during drag when horizontal resize is enabled.'), Property('prefHeight', 'DoubleProperty', 'inherited', 'Updated during drag when vertical resize is enabled.')])
            ]),
        Chapter('Resize interaction', [
                Figure(f"{G}/flow.svg", 'Mouse deltas are converted into preferred sizes.'),
                Para("On mouse press the skin remembers screen coordinates and current width and height. During drag it computes width and height from the screen delta, caps them to positive <font face='Courier'>maxWidth</font> and <font face='Courier'>maxHeight</font>, then writes enabled dimensions to preferred size."),
                Figure(f"{G}/states.svg", 'Resize-direction pseudo classes on the handle.')
            ]),
        Chapter('Sizing boundaries', [
                Para('Dragging changes preferred size; it does not directly relocate the node. Parent layout panes still decide final placement from normal JavaFX layout rules.'),
                Table(['Constraint', 'How it is used'], [["<font face='Courier'>maxWidth</font>", 'If positive, the computed dragged width is capped to this value.'], ["<font face='Courier'>maxHeight</font>", 'If positive, the computed dragged height is capped to this value.'], ['Minimum size', 'The skin does not explicitly clamp to min size before writing preferred dimensions. Use parent constraints when a hard minimum matters.']], widths=[34, 66]),
                Code('''area.setResizeHorizontal(true);
area.setMaxWidth(500);
area.setMaxHeight(260);
area.setMinHeight(100);'''),
                Callout("Because drag writes preferred size, clearing the preferred size later with <font face='Courier'>Region.USE_COMPUTED_SIZE</font> hands sizing back to the parent layout.", kind='tip')
            ]),
        Chapter('Styling', [
                Section('Style classes and pseudo classes'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.resizable-text-area</font>", 'root control'], ["<font face='Courier'>.content-pane</font>", 'skin content stack'], ["<font face='Courier'>.resize-corner:h-resize</font>", 'horizontal-only handle'], ["<font face='Courier'>.resize-corner:v-resize</font>", 'vertical-only handle'], ["<font face='Courier'>.resize-corner:both-resize</font>", 'both directions'], ["<font face='Courier'>.resize-corner:no-resize</font>", 'hidden icon / no resize']], widths=[46, 54]),
                Para('The control defines no styleable CSS properties.'),
                Code('.resizable-text-area .resize-corner .resize-icon {\n    -fx-background-color: -fx-accent;\n}\n\n.resizable-text-area .resize-corner:hover .resize-icon {\n    -fx-background-color: grey;\n}')
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.TEXT_AREA</font>. The resize corner is mouse-oriented; expose alternative sizing controls in applications that need keyboard resizing.")
            ]),
        Chapter('Recipes', [
                Section('Vertical only'),
                Code('area.setResizeVertical(true);\narea.setResizeHorizontal(false);'),
                Section('Cap the drag size'),
                Code('area.setMaxWidth(600);\narea.setMaxHeight(320);'),
                Section('Checklist'),
                Numbered(['Use preferred-size constraints in the parent layout.', 'Set both resize properties to false to hide the icon.', 'Use max width / height when the field must stay within a form.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.ResizableTextAreaApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ResizableTextAreaApp</font>)", "<font face='Courier'>LimitedTextArea</font> - subclass with character limits.", "<font face='Courier'>ExpandingTextArea</font> - automatic height instead of dragging.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
