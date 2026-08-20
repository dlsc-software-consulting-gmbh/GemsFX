"""Content of the HiddenSidesPane developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'hidden-sides-pane'

MANUAL = Manual(
    control='HiddenSidesPane',
    package='com.dlsc.gemsfx',
    subtitle='A content pane with four slide-in edge overlays',
    abstract='HiddenSidesPane lays out a full-size content node and up to four hidden side nodes that slide in from pane edges on mouse proximity or when pinned.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of HiddenSidesPane.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>HiddenSidesPane</b> is a <font face='Courier'>Pane</font> with one full-size content node and up to four side nodes initially positioned outside the clipped pane bounds. The side nodes slide in when the mouse enters an edge trigger band or when a side is pinned."),
                Section('Key features'),
                Bullets(['Content fills the complete pane and drives min / pref / max size.', 'Top, right, bottom and left side nodes are optional overlays.', "<font face='Courier'>triggerDistance</font> defaults to 16 and disables hover when zero or negative.", "<font face='Courier'>pinnedSide</font> keeps one side visible.", 'Show and hide use configurable delay and duration.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Para("Set a content node before the pane is laid out. Side nodes are cast to <font face='Courier'>Region</font> when installed, so use Region subclasses for the hidden sides."),
                Code('HiddenSidesPane pane = new HiddenSidesPane();\nLabel content = new Label("Content");\ncontent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);\npane.setContent(content);\n\nLabel right = new Label("Details");\nright.setPrefSize(220, 200);\npane.setRight(right);\npane.setTriggerDistance(20);', caption='A complete right-side overlay setup.'),
                Figure(f'{G}/states.svg', 'Hidden, sliding and visible states for a side node.'),
                Callout("Layout calls <font face='Courier'>getContent().resizeRelocate(...)</font> without a null check. Do not leave content null once the pane is displayed.", kind='warning')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'The content node plus the four optional side nodes.'),
                Table(['Part', 'Property', 'Description'], [['Content', "<font face='Courier'>content</font>", 'Full-size primary node and source of computed sizes.'], ['Top', "<font face='Courier'>top</font>", 'Region sliding down from above.'], ['Right', "<font face='Courier'>right</font>", 'Region sliding in from the right edge.'], ['Bottom', "<font face='Courier'>bottom</font>", 'Region sliding up from below.'], ['Left', "<font face='Courier'>left</font>", 'Region sliding in from the left edge.'], ['Clip', 'Rectangle', 'Bound to pane width and height to hide off-pane portions.']], widths=[20, 24, 56])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('content', 'ObjectProperty&lt;Node&gt;', 'null', 'Full-size content node.'), Property('top / right / bottom / left', 'ObjectProperty&lt;Node&gt;', 'null', 'Optional side nodes. In practice use Region subclasses.'), Property('triggerDistance', 'DoubleProperty', '16', 'Edge distance that triggers hover display; &lt;= 0 disables mouse-triggered display.'), Property('pinnedSide', 'ObjectProperty&lt;Side&gt;', 'null', 'Side that remains visible.'), Property('animationDelay', 'ObjectProperty&lt;Duration&gt;', '300 ms', 'Delay before show / hide timeline starts; null falls back to 300 ms.'), Property('animationDuration', 'ObjectProperty&lt;Duration&gt;', '200 ms', 'Timeline duration; null falls back to 200 ms.')])
            ]),
        Chapter('Layout geometry', [
                Figure(f'{G}/layout.svg', 'Visibility transforms preferred side size into visible overlay thickness.'),
                Para("The content is resized to <font face='Courier'>0,0,width,height</font>. Horizontal side nodes use their preferred width and pane height; vertical side nodes use pane width and their preferred height."),
                Code('rightPref = right.prefWidth(contentHeight)\noffset = rightPref * visibility[RIGHT]\nright.resizeRelocate(contentWidth - offset, 0, rightPref, contentHeight)', caption='Simplified placement for the right side.'),
                Para('Each side has a visibility property in the range 0 to 1. Values greater than zero make the node visible; zero hides it.')
            ]),
        Chapter('Interaction and animation', [
                Figure(f'{G}/interaction.svg', 'Mouse activation and pinning.'),
                Table(['Trigger', 'Result'], [['Mouse in edge band', 'Shows that side if mouse is enabled and no side is pinned.'], ['Mouse outside visible sides', 'Hides all sides.'], ['Mouse exited side node', 'Hides unless a side is pinned or mouse is pressed.'], ['Set pinnedSide', 'Shows the pinned side and suppresses hover switching.']], widths=[34, 66]),
                Para('Showing a side stops a running hide timeline and animates the selected side to 1 while all others animate to 0. Hiding animates every side to 0.')
            ]),
        Chapter('Styling', [
                Para('HiddenSidesPane adds no root style class, exposes no styleable CSS properties and has no user agent stylesheet. Style the content and side nodes directly.'),
                Code('VBox drawer = new VBox();\ndrawer.getStyleClass().add("edge-drawer");\npane.setLeft(drawer);'),
                Code('.edge-drawer {\n    -fx-background-color: white;\n    -fx-effect: dropshadow(gaussian, rgba(0,0,0,.25), 14, 0, 0, 6);\n}')
            ]),
        Chapter('Sizing contracts and node requirements', [
                Para("The pane delegates all size computations to the content node. Minimum, preferred and maximum width / height return the corresponding value of <font face='Courier'>content</font>, or zero when content is null. Side nodes do not contribute to these computations because they are transient overlays."),
                Table(['Computation', 'Source'], [["<font face='Courier'>computePrefWidth(height)</font>", "<font face='Courier'>content.prefWidth(height)</font>"], ["<font face='Courier'>computePrefHeight(width)</font>", "<font face='Courier'>content.prefHeight(width)</font>"], ['Side thickness', 'The side node preferred width or preferred height during layout.'], ['Content bias', 'First managed child content bias, preferring horizontal if present.']], widths=[42, 58]),
                Para("When side properties change, <font face='Courier'>updateStackPane()</font> clears and rebuilds the children list. For every side node it also sets the maximum size in the sliding direction to <font face='Courier'>Region.USE_PREF_SIZE</font> and the cross-axis maximum to <font face='Courier'>Double.MAX_VALUE</font>."),
                Code('''((Region) getRight()).setMaxWidth(Region.USE_PREF_SIZE);
((Region) getRight()).setMaxHeight(Double.MAX_VALUE);''', caption='Right and left sides keep preferred width and stretch vertically.'),
                Callout('The side-node casts mean a plain Canvas or Group is not a safe side node unless wrapped in a Region such as StackPane.', kind='warning')
            ]),
        Chapter('Event ordering and edge precedence', [
                Para("The private <font face='Courier'>getSide(MouseEvent)</font> method checks the left edge first, then right, then top, then bottom. In a corner where trigger bands overlap, horizontal sides therefore win over vertical sides."),
                Table(['Mouse condition', 'Chosen side'], [['x &lt;= triggerDistance', 'LEFT'], ['x &gt; width - triggerDistance', 'RIGHT'], ['y &lt;= triggerDistance', 'TOP'], ['y &gt; height - triggerDistance', 'BOTTOM'], ['outside bounds', 'none']], widths=[44, 56]),
                Para('Mouse release re-evaluates the pointer position. This avoids a side staying open after a press / drag sequence ends away from all trigger bands.'),
                Code('''pane.setTriggerDistance(24);
pane.setPinnedSide(null);''')
            ]),
        Chapter('Production usage notes', [
                Para('Hidden side panes are most useful for temporary tools, inspectors and contextual palettes. They are not modal: the content remains visible underneath and the side overlays do not reserve layout space.'),
                Table(['Concern', 'Recommended practice'], [['Preferred sizes', 'Set explicit preferred sizes for important children so the layout maths has stable inputs.'], ['Insets and padding', 'Remember that pane insets are part of the available area calculation or child placement.'], ['Managed state', 'Only managed children should be expected to participate in layout decisions.'], ['Runtime changes', 'Property invalidation calls requestLayout, so batch related changes where possible.']], widths=[34, 66]),
                Para('For touch-first or keyboard-first applications, prefer pinning from explicit buttons over relying on the mouse trigger band. A trigger distance of zero gives a completely programmatic pane while preserving the same slide-in layout.'),
                Code('''pane.setTriggerDistance(0);
openInspector.setOnAction(evt -> pane.setPinnedSide(Side.RIGHT));
closeInspector.setOnAction(evt -> pane.setPinnedSide(null));''')
            ]),
        Chapter('Recipes', [
                Section('Pin from a button'),
                Code('pinLeft.setOnAction(evt -> pane.setPinnedSide(Side.LEFT));\nunpin.setOnAction(evt -> pane.setPinnedSide(null));'),
                Section('Disable hover activation'),
                Code('pane.setTriggerDistance(0);'),
                Section('Checklist'),
                Numbered(['Set content before showing the pane.', 'Use Region subclasses for side nodes.', 'Keep side preferred sizes realistic.', 'Use pinnedSide for programmatic trays.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.HiddenSidesPaneApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.HiddenSidesPaneApp</font>)", "<font face='Courier'>PowerPane</font> - composes a HiddenSidesPane.", "<font face='Courier'>DrawerStackPane</font> - bottom drawer alternative.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
