"""Content of the PowerPane developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'power-pane'

MANUAL = Manual(
    control='PowerPane',
    package='com.dlsc.gemsfx',
    subtitle='An application shell composed from GemsFX overlay panes',
    abstract='PowerPane combines InfoCenterPane, DialogPane, DrawerStackPane and HiddenSidesPane into one StackPane shell and delegates features through getters for the composed panes.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of PowerPane.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>PowerPane</b> is a convenience shell that composes <font face='Courier'>InfoCenterPane</font>, <font face='Courier'>DialogPane</font>, <font face='Courier'>DrawerStackPane</font> and <font face='Courier'>HiddenSidesPane</font>. It delegates behaviour through final getters."),
                Section('Key features'),
                Bullets(['Direct child is an InfoCenterPane.', 'DialogPane is stacked with DrawerStackPane inside the info center content.', 'DrawerStackPane contains HiddenSidesPane.', 'HiddenSidesPane content is bound to PowerPane content.', 'Protected factory methods create the composed panes.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('PowerPane powerPane = new PowerPane(mainView);\npowerPane.getDialogPane().showInformation("Info", "Ready");\npowerPane.getDrawerStackPane().setDrawerContent(settingsView);\npowerPane.getHiddenSidesPane().setLeft(navigationRail);', caption='Using the composed panes through getters.'),
                Figure(f'{G}/states.svg', 'The overlay families composed by PowerPane.')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'How PowerPane nests the composed panes.'),
                Table(['Part', 'Getter', 'Role'], [['Info center', "<font face='Courier'>getInfoCenterPane()</font>", 'Outermost notification surface.'], ['Dialog pane', "<font face='Courier'>getDialogPane()</font>", 'Dialog overlay.'], ['Drawer stack', "<font face='Courier'>getDrawerStackPane()</font>", 'Bottom drawer layer.'], ['Hidden sides', "<font face='Courier'>getHiddenSidesPane()</font>", 'Edge tray layer.'], ['Content', "<font face='Courier'>content</font>", 'Main application UI.']], widths=[22, 34, 44])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('content', 'ObjectProperty&lt;Node&gt;', 'null', 'Main UI bound into HiddenSidesPane.'), Property('infoCenterPane', 'InfoCenterPane', 'new InfoCenterPane()', 'Returned by final getter.'), Property('dialogPane', 'DialogPane', 'new DialogPane()', 'Returned by final getter.'), Property('drawerStackPane', 'DrawerStackPane', 'new DrawerStackPane()', 'Returned by final getter.'), Property('hiddenSidesPane', 'HiddenSidesPane', 'new HiddenSidesPane()', 'Returned by final getter.')])
            ]),
        Chapter('Composition and layout', [
                Figure(f'{G}/layout.svg', 'Nested panes fill the PowerPane area.'),
                Para('The constructor binds hiddenSidesPane.contentProperty() to contentProperty(), adds HiddenSidesPane to DrawerStackPane, wraps DrawerStackPane and DialogPane in a StackPane, sets that StackPane as InfoCenterPane content and finally adds InfoCenterPane as the only child.'),
                Code('hiddenSidesPane.contentProperty().bind(contentProperty());\ndrawerStackPane.getChildren().add(hiddenSidesPane);\ninfoCenterPane.setContent(new StackPane(drawerStackPane, dialogPane));\ngetChildren().add(infoCenterPane);', caption='The composition graph from the constructor.')
            ]),
        Chapter('Delegation patterns', [
                Figure(f'{G}/interaction.svg', 'Applications configure the composed panes.'),
                Table(['Need', 'Use'], [['Show a dialog', "<font face='Courier'>getDialogPane()</font>"], ['Open a drawer', "<font face='Courier'>getDrawerStackPane()</font>"], ['Install side trays', "<font face='Courier'>getHiddenSidesPane()</font>"], ['Show notifications', "<font face='Courier'>getInfoCenterPane()</font>"]], widths=[34, 66])
            ]),
        Chapter('Subclassing and styling', [
                Para('Override the protected create methods to provide custom subclasses during construction. The composed pane fields are final after construction.'),
                Code('public class MyPowerPane extends PowerPane {\n    @Override\n    protected DialogPane createDialogPane() {\n        DialogPane pane = new DialogPane();\n        pane.setAnimationDuration(Duration.millis(120));\n        return pane;\n    }\n}'),
                Para("PowerPane adds style class <font face='Courier'>power-pane</font> but has no user agent stylesheet and no styleable CSS properties.")
            ]),
        Chapter('Delegated property map', [
                Para('PowerPane intentionally exposes only its own content property. Everything else is configured on the composed pane that actually owns the feature. This keeps PowerPane small and avoids duplicating the APIs of DialogPane, DrawerStackPane, HiddenSidesPane and InfoCenterPane.'),
                Table(['Feature', 'Configure on'], [['Drawer content, title, animation and height', "<font face='Courier'>getDrawerStackPane()</font>"], ['Hidden left / right / top / bottom trays', "<font face='Courier'>getHiddenSidesPane()</font>"], ['Dialog animation, converter and dialog display', "<font face='Courier'>getDialogPane()</font>"], ['Notification groups and info-center visibility', "<font face='Courier'>getInfoCenterPane()</font> and its InfoCenterView"]], widths=[44, 56]),
                Code('''powerPane.getDrawerStackPane().setShowDrawerTitle(true);
powerPane.getHiddenSidesPane().setTriggerDistance(24);
powerPane.getDialogPane().setAnimationDuration(Duration.millis(150));''')
            ]),
        Chapter('Construction sequence', [
                Para('The constructor order matters for subclassing. First the four create methods are called. Then the content binding and nested containment are established. Finally the InfoCenterPane is added as the one direct child of the PowerPane.'),
                Numbered(["<font face='Courier'>createInfoCenterPane()</font>", "<font face='Courier'>createDialogPane()</font>", "<font face='Courier'>createDrawerStackPane()</font>", "<font face='Courier'>createHiddenSidesPane()</font>", 'Bind hidden sides content to PowerPane content.', 'Nest HiddenSidesPane inside DrawerStackPane.', 'Set InfoCenterPane content to a StackPane containing drawer and dialog panes.']),
                Callout("Because factory methods run from the constructor, overrides should not depend on subclass fields initialized after <font face='Courier'>super()</font>.", kind='warning')
            ]),
        Chapter('Application shell strategy', [
                Para('PowerPane works best as the center of the primary scene or the center of a root BorderPane. Treat it as the owner of application overlays: route dialogs, drawers, side trays and notifications through its child panes instead of creating parallel overlay stacks elsewhere.'),
                Table(['Concern', 'Recommended practice'], [['Preferred sizes', 'Set explicit preferred sizes for important children so the layout maths has stable inputs.'], ['Insets and padding', 'Remember that pane insets are part of the available area calculation or child placement.'], ['Managed state', 'Only managed children should be expected to participate in layout decisions.'], ['Runtime changes', 'Property invalidation calls requestLayout, so batch related changes where possible.']], widths=[34, 66]),
                Para('Because the content property is bound into the nested HiddenSidesPane, replacing content updates the main application view without reconstructing the overlay infrastructure.'),
                Code('''PowerPane shell = new PowerPane();
shell.setContent(loginView);
// later
shell.setContent(mainApplicationView);''')
            ]),
        Chapter('Layering order in practice', [
                Para('The composed layers are arranged so each specialized pane can do its normal job. Hidden sides and drawer content live inside DrawerStackPane; DialogPane is stacked above that drawer stack; InfoCenterPane hosts the entire stack and can show notifications over the application shell.'),
                Table(['Layer', 'Typical visual result'], [['HiddenSidesPane', 'Edge trays around the main content.'], ['DrawerStackPane', 'Bottom drawer over the content / hidden-side layer.'], ['DialogPane', 'Dialog overlay above drawer and content.'], ['InfoCenterPane', 'Notification center surface around the whole shell.']], widths=[34, 66]),
                Para('When debugging a complex shell, inspect the individual composed panes first. PowerPane itself only creates and connects the composition graph.')
            ]),
        Chapter('Recipes', [
                Section('Install all hidden sides'),
                Code('HiddenSidesPane sides = powerPane.getHiddenSidesPane();\nsides.setLeft(leftTray);\nsides.setRight(rightTray);\nsides.setTop(topTray);\nsides.setBottom(bottomTray);'),
                Section('Checklist'),
                Numbered(['Set content on PowerPane, not directly on the nested HiddenSidesPane.', 'Use getters to configure child panes.', 'Override factory methods only in subclasses.', 'Read composed pane manuals for delegated properties.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.PowerPaneApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.PowerPaneApp</font>)", "<font face='Courier'>InfoCenterPane</font> - notification surface.", "<font face='Courier'>DialogPane</font> - dialog layer.", "<font face='Courier'>DrawerStackPane</font> - drawer layer.", "<font face='Courier'>HiddenSidesPane</font> - edge tray layer.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
