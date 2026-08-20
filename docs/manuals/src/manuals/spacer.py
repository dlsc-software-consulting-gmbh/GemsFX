"""Content of the Spacer developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'spacer'

MANUAL = Manual(
    control='Spacer',
    package='com.dlsc.gemsfx',
    subtitle='A styleable flexible gap for HBox and VBox',
    abstract='Spacer is a small Region subclass that grows in HBox or VBox and can collapse itself by binding visible and managed to a styleable active property.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of Spacer.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>Spacer</b> is a tiny <font face='Courier'>Region</font> that sets HBox and VBox grow priority to ALWAYS. Its styleable active property controls whether it is visible and managed."),
                Section('Key features'),
                Bullets(["Style class <font face='Courier'>spacer</font>.", 'active defaults to true.', 'visible is bound to active.', 'managed is bound to visible.', 'HBox and VBox grow priorities are ALWAYS.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('HBox row = new HBox(8, new Label("Hello"), new Spacer(), new Label("World"));\nVBox column = new VBox(new Label("Top"), new Spacer(), new Label("Bottom"));', caption='Spacer in horizontal and vertical boxes.'),
                Figure(f'{G}/states.svg', 'Active and inactive spacer states.')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'A Spacer between two sibling nodes.'),
                PropertyTable([Property('active', 'BooleanProperty', 'true', 'Controls visible and managed state. Styleable with -fx-active.')]),
                Table(['Constructor action', 'Effect'], [['style class spacer', 'Application selector.'], ['managed.bind(visible)', 'Collapsed spacer reserves no space.'], ['visible.bind(active)', 'active drives visibility.'], ['HBox/VBox grow ALWAYS', 'Consumes extra space in common box containers.']], widths=[44, 56])
            ]),
        Chapter('Layout behaviour', [
                Figure(f'{G}/layout.svg', 'The same class grows horizontally in HBox and vertically in VBox.'),
                Para('Spacer does not inspect its parent. It simply sets both grow constraints; the parent layout that understands one of them uses it.'),
                Figure(f'{G}/interaction.svg', 'Binding active controls visibility and layout management.')
            ]),
        Chapter('Styling', [
                Para("There is no user agent stylesheet. The CSS metadata exposes <font face='Courier'>-fx-active</font>."),
                Table(['CSS property', 'Type', 'Default in metadata / property'], [["<font face='Courier'>-fx-active</font>", 'boolean', 'metadata false, property true']], widths=[52, 24, 24]),
                Code('.debug .spacer {\n    -fx-background-color: rgba(255, 0, 128, .15);\n}\n.spacer {\n    -fx-active: true;\n}'),
                Callout('The CSS metadata default is false, while the property instance starts true. A stylesheet value wins when CSS is applied.', kind='note')
            ]),
        Chapter('Recipes', [
                Section('Toggle a group of spacers'),
                Code('CheckBox active = new CheckBox("Spacer active");\nspacer.activeProperty().bind(active.selectedProperty());'),
                Section('Toolbar right alignment'),
                Code('toolbar.getChildren().addAll(leftButtons, new Spacer(), rightButtons);'),
                Section('Checklist'),
                Numbered(['Use Spacer primarily in HBox and VBox.', 'Bind active rather than manually changing managed and visible.', 'Use temporary backgrounds for layout debugging.', 'Do not use Spacer when fixed padding is the real requirement.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.SpacerApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.SpacerApp</font>)", "<font face='Courier'>ThreeItemsPane</font> - semantic three-slot alternative.", "JavaFX <font face='Courier'>Region</font> - Spacer superclass.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
