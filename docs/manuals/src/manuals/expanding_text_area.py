"""Content of the ExpandingTextArea developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "expanding-text-area"

MANUAL = Manual(
    control='ExpandingTextArea',
    package='com.dlsc.gemsfx',
    subtitle='A TextArea that grows to fit its content',
    abstract='ExpandingTextArea is a TextArea subclass that wraps text, suppresses scroll bars and binds its preferred height to the measured text height.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of ExpandingTextArea.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>ExpandingTextArea</b> is a customized <font face='Courier'>TextArea</font> for form layouts where all text should stay visible. It does not introduce new public properties; instead it configures and observes the standard text-area skin."),
                Section('Key features'),
                Bullets(["Automatically calls <font face='Courier'>setWrapText(true)</font>.", "Forces the internal scroll pane horizontal and vertical scroll bars to <font face='Courier'>NEVER</font>.", "Binds <font face='Courier'>prefHeight</font> to the measured text node height plus insets.", 'Keeps the normal TextArea API for text, prompt text and selection.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('ExpandingTextArea area = new ExpandingTextArea();\narea.setPromptText("Notes");\narea.setMaxWidth(400);\narea.textProperty().addListener((obs, old, text) -> saveDraft(text));', caption='A complete expanding text area setup.'),
                Figure(f"{G}/states.svg", 'The preferred height increases as wrapped text needs more lines.')
            ]),
        Chapter('Anatomy', [
                Figure(f"{G}/anatomy.svg", 'Parts involved in height computation.'),
                Table(['Part', 'Source', 'Description'], [['Root', "<font face='Courier'>.expanding-text-area</font>", 'Style class added by the constructor.'], ['Scroll pane', "lookup <font face='Courier'>.scroll-pane</font>", 'Its bars are disabled.'], ['Viewport / content', 'lookup nodes', 'Insets are added to the height calculation.'], ['Text node', "<font face='Courier'>Text</font> whose parent is a <font face='Courier'>Group</font>", 'Its layout bounds drive the preferred height.']], widths=[24, 32, 44])
            ]),
        Chapter('Control API', [
                Para("The control adds no new JavaFX properties. Use the inherited <font face='Courier'>TextArea</font> API."),
                PropertyTable([Property('text', 'StringProperty', 'empty string', 'Inherited text content.'), Property('promptText', 'StringProperty', 'empty string', 'Inherited prompt shown when text is empty.'), Property('wrapText', 'BooleanProperty', 'true', 'Set to true by the constructor.'), Property('prefHeight', 'DoubleProperty', 'bound after skin setup', 'Computed from the internal text node and insets.')])
            ]),
        Chapter('Height computation', [
                Figure(f"{G}/flow.svg", 'How text layout becomes preferred height.'),
                Para("Binding starts when both a scene and a skin are available. The implementation then waits for the scroll pane skin, finds the real content text node and binds <font face='Courier'>prefHeight</font> to a computation using <font face='Courier'>localToScreen(text.getLayoutBounds())</font> plus top and bottom offsets."),
                Callout("Because <font face='Courier'>prefHeight</font> becomes bound by the control, do not bind or set that property for sizing. Put the area in a scroll pane or constrain its parent instead.", kind='warning')
            ]),
        Chapter('Layout guidance', [
                Figure(f"{G}/cover.svg", 'The area grows vertically instead of showing scroll bars.'),
                Para('The control works best in forms where width is constrained and height can grow. Since wrapping is enabled in the constructor, the chosen width directly determines how many visual lines the text occupies and therefore the computed preferred height.'),
                Table(['Container', 'Guidance'], [["<font face='Courier'>VBox</font>", 'Let the VBox use the area preferred height and avoid setting a fixed row height.'], ["<font face='Courier'>ScrollPane</font>", 'Put the whole form in a scroll pane when long text can make the page taller than the viewport.'], ['Grid layouts', 'Avoid binding prefHeight; use row constraints or surrounding scroll panes instead.']], widths=[30, 70]),
                Code('''ExpandingTextArea notes = new ExpandingTextArea();
notes.setMaxWidth(420);
VBox.setVgrow(notes, Priority.NEVER);'''),
                Callout('The implementation depends on skin lookup of standard TextArea internals. Test custom themes that replace the standard skin structure.', kind='note')
            ]),
        Chapter('Styling', [
                Para('There is no dedicated user agent CSS file and no custom CSS properties. Style it like a normal JavaFX text area plus the root style class below.'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.expanding-text-area</font>", 'root text area style class'], ['standard TextArea selectors', 'inherited from JavaFX / application stylesheets']], widths=[45, 55]),
                Code('.expanding-text-area {\n    -fx-font-size: 14px;\n}\n\n.expanding-text-area .content {\n    -fx-padding: 8px;\n}')
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.TEXT_AREA</font>. No generated accessible text is bound; screen readers use the normal text-area semantics.")
            ]),
        Chapter('Recipes', [
                Section('Use in a scrollable form'),
                Code('VBox form = new VBox(10, nameField, new ExpandingTextArea());\nScrollPane page = new ScrollPane(form);\npage.setFitToWidth(true);'),
                Section('Limit horizontal growth'),
                Code('area.setMaxWidth(400);\nparent.setFillWidth(false);'),
                Section('Checklist'),
                Numbered(['Do not expect scroll bars inside the control.', "Use inherited <font face='Courier'>TextArea</font> selection and text APIs.", 'Constrain width so wrapping creates predictable height.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.ExpandingTextAreaApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ExpandingTextAreaApp</font>)", "<font face='Courier'>ResizableTextArea</font> - manual user resizing instead of automatic height.", "<font face='Courier'>LimitedTextArea</font> - text area with length feedback.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
