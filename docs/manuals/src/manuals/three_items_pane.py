"""Content of the ThreeItemsPane developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'three-items-pane'

MANUAL = Manual(
    control='ThreeItemsPane',
    package='com.dlsc.gemsfx',
    subtitle='A pane for left / center / right or top / center / bottom items',
    abstract='ThreeItemsPane manages up to three nodes and positions them as edge, center and opposite-edge items in a horizontal or vertical orientation.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of ThreeItemsPane.',
    chapters=[
        Chapter('Introduction', [
                Para('<b>ThreeItemsPane</b> manages up to three item nodes and positions them as edge, center and opposite-edge items in horizontal or vertical orientation. It is useful for headers, toolbars and status bars.'),
                Section('Key features'),
                Bullets(['item1, item2 and item3 are the public node slots.', 'Horizontal: left, center, right.', 'Vertical: top, center, bottom.', 'Spacing prevents later items from overlapping earlier ones.', 'Children list is rebuilt from non-null item properties.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('ThreeItemsPane pane = new ThreeItemsPane();\npane.setSpacing(20);\npane.setItem1(new Label("Application"));\npane.setItem2(new Label("Center"));\npane.setItem3(new Label("User"));', caption='A three-part header.'),
                Figure(f'{G}/states.svg', 'Horizontal and vertical orientations.')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'The three semantic item positions.'),
                PropertyTable([Property('orientation', 'ObjectProperty&lt;Orientation&gt;', 'Orientation.HORIZONTAL', 'Chooses horizontal or vertical.'), Property('item1', 'ObjectProperty&lt;Node&gt;', 'null', 'First item: left or top.'), Property('item2', 'ObjectProperty&lt;Node&gt;', 'null', 'Center item.'), Property('item3', 'ObjectProperty&lt;Node&gt;', 'null', 'Third item: right or bottom.'), Property('spacing', 'DoubleProperty', '0', 'Separation between managed items.')])
            ]),
        Chapter('Layout algorithm', [
                Figure(f'{G}/layout.svg', 'Horizontal positioning with overlap protection.'),
                Para('Item1 is placed at the leading inset. Item2 is centered around half the pane but shifted after item1 if needed. Item3 is aligned to the trailing inset but shifted after item2 if needed.'),
                Code('x2 = max(minimumX, width / 2 - item2.prefWidth / 2)\nx3 = max(minimumXAfterItem2, width - rightInset - item3.prefWidth)')
            ]),
        Chapter('Sizing and children', [
                Figure(f'{G}/interaction.svg', 'Item properties rebuild the children list.'),
                Para('The pane clears and repopulates its children whenever item1, item2 or item3 changes. Preferred size uses child preferred sizes and insets; content bias is inherited from children.'),
                Callout("The source horizontal pref / min width expression is precedence-sensitive: <font face='Courier'>getChildren().size() - 1 * getSpacing()</font>. This manual documents observed slot layout rather than relying on that expression.", kind='note')
            ]),
        Chapter('Styling', [
                Para('ThreeItemsPane adds no style class, no user agent stylesheet and no styleable CSS properties. Add an application style class to the pane or to individual items.'),
                Code('pane.getStyleClass().add("app-header");\npane.getItem2().getStyleClass().add("header-title");')
            ]),
        Chapter('Vertical layout details', [
                Para('Vertical layout mirrors the horizontal algorithm. Item1 is placed at the top inset, item2 is vertically centered but not above the minimum y after item1, and item3 is bottom aligned but not above the minimum y after item2.'),
                Table(['Item', 'Vertical y coordinate'], [['item1', "<font face='Courier'>insets.getTop()</font>"], ['item2', "<font face='Courier'>max(minimumY, height / 2 - prefHeight / 2)</font>"], ['item3', "<font face='Courier'>max(minimumY, height - bottomInset - prefHeight)</font>"]], widths=[24, 76]),
                Para('All three items are horizontally centered in vertical orientation. Width computations use each child preferred width with available height as the argument.'),
                Code('''pane.setOrientation(Orientation.VERTICAL);
pane.setSpacing(12);''')
            ]),
        Chapter('Null items and managed children', [
                Para('The pane supports any combination of null and non-null item properties. The children list contains only non-null items, in item order. Layout methods still query the three properties directly, so replacing one property is the supported way to change a slot.'),
                Table(['Slots set', 'Children list'], [['item1 only', 'item1'], ['item2 only', 'item2'], ['item1 + item3', 'item1, item3'], ['all three', 'item1, item2, item3']], widths=[34, 66]),
                Para("Because the class extends Pane, callers can technically mutate <font face='Courier'>getChildren()</font>. Do not use that as the main API: the next property change clears and rebuilds the children list from the item properties."),
                Callout('Use item properties as the source of truth. Direct child-list edits are fragile with this pane.', kind='warning')
            ]),
        Chapter('Size computations by orientation', [
                Para('Preferred and minimum size methods compute each child size first. In horizontal orientation, height is the maximum child height and width is intended to be the sum of child widths plus spacing and insets. In vertical orientation, width is the maximum child width and height is the sum of child heights plus spacing and insets.'),
                Table(['Orientation', 'Preferred width', 'Preferred height'], [['HORIZONTAL', 'sum of child preferred widths plus horizontal insets and spacing expression from source', 'max child preferred height plus vertical insets'], ['VERTICAL', 'max child preferred width plus horizontal insets', 'sum of child preferred heights plus vertical insets and spacing']], widths=[24, 38, 38]),
                Para("Maximum size is unbounded along the layout axis: horizontal orientation returns <font face='Courier'>Double.MAX_VALUE</font> for max width, while vertical orientation returns <font face='Courier'>Double.MAX_VALUE</font> for max height.")
            ]),
        Chapter('Toolbar design patterns', [
                Para('The center item is visually stable as long as enough room exists between the edge items. This makes ThreeItemsPane useful for title bars where the title should remain centered in the whole window, not merely centered in the remaining space.'),
                Table(['Concern', 'Recommended practice'], [['Preferred sizes', 'Set explicit preferred sizes for important children so the layout maths has stable inputs.'], ['Insets and padding', 'Remember that pane insets are part of the available area calculation or child placement.'], ['Managed state', 'Only managed children should be expected to participate in layout decisions.'], ['Runtime changes', 'Property invalidation calls requestLayout, so batch related changes where possible.']], widths=[34, 66]),
                Para('When the edge items become too large, the source protects against overlap by shifting the center and trailing items. That is preferable to clipping, but applications should still keep header controls compact.'),
                Code('''pane.setItem1(navigationButtons);
pane.setItem2(titleLabel);
pane.setItem3(accountMenu);
pane.setSpacing(16);''')
            ]),
        Chapter('Choosing between alternatives', [
                Para('Use ThreeItemsPane when positions have meaning. Use HBox with Spacer when the goal is simply to consume extra space. Use BorderPane when five regions and resizable center content are needed.'),
                Table(['Requirement', 'Best fit'], [['Left / center / right header', 'ThreeItemsPane'], ['Push right controls to the edge', 'HBox plus Spacer'], ['Top / bottom / left / right / center application layout', 'BorderPane'], ['Responsive navigation rail', 'ResponsivePane']], widths=[42, 58]),
                Para('This distinction keeps layout code easy to read: item properties communicate intent directly, while generic child lists require readers to infer meaning from order and spacer nodes.')
            ]),
        Chapter('Practical sizing checklist', [
                Para('A three-slot pane is usually placed in a constrained header or footer. The clearest results come from making each child report a realistic preferred size and letting the pane preserve the semantic positions.'),
                Table(['Child type', 'Sizing advice'], [['Text label', 'Let the label compute its preferred size; avoid unnecessary max width.'], ['Button cluster', 'Wrap related buttons in an HBox and use that HBox as one item.'], ['Avatar or status node', 'Set an explicit preferred size so the trailing edge is predictable.'], ['Long title', 'Consider ellipsis or a maximum width before it collides with edge items.']], widths=[34, 66]),
                Para('If the center item must never shift, constrain the edge items externally. The source protects from overlap by shifting nodes, not by clipping or compressing children.')
            ]),
        Chapter('Common mistakes', [
                Para('The most common mistake is to treat ThreeItemsPane like a generic Pane and add children directly. That may appear to work until an item property changes and updateChildren clears and rebuilds the children list.'),
                Bullets(["Do not add unrelated nodes through <font face='Courier'>getChildren()</font>.", 'Do not rely on child order after setting an item property.', 'Do not expect the pane to resize or compress child nodes; it uses preferred sizes.', 'Do not use it as a replacement for BorderPane when a resizable center region is required.']),
                Para('A good rule is simple: if a node is not item1, item2 or item3, it belongs outside this pane or inside one of the three item nodes as a nested layout.'),
                Code('''HBox leftCluster = new HBox(backButton, titleIcon);
pane.setItem1(leftCluster);
// not: pane.getChildren().add(titleIcon);''')
            ]),
        Chapter('Recipes', [
                Section('Centered title bar'),
                Code('pane.setItem1(backButton);\npane.setItem2(new Label("Details"));\npane.setItem3(settingsButton);'),
                Section('Checklist'),
                Numbered(['Use item properties instead of direct child mutations.', 'Give each item a sensible preferred size.', 'Increase spacing when edge items approach the center item.', 'Use null to omit a slot.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.ThreeItemsPaneApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ThreeItemsPaneApp</font>)", "<font face='Courier'>Spacer</font> - flexible gap for HBox / VBox.", "<font face='Courier'>ResponsivePane</font> - adaptive sidebar layout.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
