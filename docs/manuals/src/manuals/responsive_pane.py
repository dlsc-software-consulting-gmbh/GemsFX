"""Content of the ResponsivePane developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'responsive-pane'

MANUAL = Manual(
    control='ResponsivePane',
    package='com.dlsc.gemsfx',
    subtitle='A content pane with adaptive small and large sidebars',
    abstract='ResponsivePane is a StackPane-based responsive layout that chooses no sidebar, a compact sidebar or a large sidebar from available size and preferred node sizes.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of ResponsivePane.',
    chapters=[
        Chapter('Introduction', [
                Para('<b>ResponsivePane</b> chooses between no sidebar, a compact sidebar and a large sidebar from the available width or height. LEFT and RIGHT layouts compare widths; TOP and BOTTOM compare heights.'),
                Section('Key features'),
                Bullets(["Root style class <font face='Courier'>responsive-pane</font> and user agent stylesheet <font face='Courier'>responsive-pane.css</font>.", "Styleable <font face='Courier'>side</font> and <font face='Courier'>gap</font> properties.", 'State pseudo classes indicate which sidebar is active.', 'Forced large display uses an internal GlassPane that closes on click.', 'Node properties keep the children list synchronized.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('ResponsivePane pane = new ResponsivePane();\npane.setContent(contentView);\npane.setSmallSidebar(compactRail);\npane.setLargeSidebar(fullNavigation);\npane.setSide(Side.LEFT);\npane.setGap(10);', caption='A content area with small and large sidebars.'),
                Figure(f'{G}/states.svg', 'The pane selects none, small or large from available space.')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'Content, sidebars and the glass pane.'),
                Table(['Part', 'Property / node', 'Description'], [['Content', "<font face='Courier'>content</font>", 'Main node resized into remaining space.'], ['Small sidebar', "<font face='Courier'>smallSidebar</font>", 'Compact node used when the large sidebar does not fit.'], ['Large sidebar', "<font face='Courier'>largeSidebar</font>", 'Full sidebar used when it fits or is forced.'], ['Glass pane', 'GlassPane', 'Internal overlay over content while large sidebar is forced.']], widths=[22, 30, 48])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('content', 'ObjectProperty&lt;Node&gt;', 'null', 'Main content.'), Property('smallSidebar', 'ObjectProperty&lt;Node&gt;', 'null', 'Compact sidebar.'), Property('largeSidebar', 'ObjectProperty&lt;Node&gt;', 'null', 'Large sidebar.'), Property('side', 'ObjectProperty&lt;Side&gt;', 'Side.LEFT', 'Sidebar side; styleable with -fx-side.'), Property('gap', 'DoubleProperty', '0', 'Gap between sidebar and content; styleable with -fx-gap and clamped to non-negative in layout.'), Property('forceLargeSidebarDisplay', 'BooleanProperty', 'false', 'Forces large sidebar when small sidebar is active.'), Property('largeSidebarCoversSmall', 'BooleanProperty', 'false', 'Controls whether forced large covers or sits next to small.')])
            ]),
        Chapter('Breakpoint geometry', [
                Figure(f'{G}/layout.svg', 'Width and gap measurements in a LEFT layout.'),
                Para("For horizontal sides the pane tests <font face='Courier'>insideWidth</font> against preferred content width, sidebar width and gap. For vertical sides it repeats the same logic with heights."),
                Code('if insideWidth >= prefContentWidth + largeSidebarWidth + gap:\n    active = largeSidebar\nelif insideWidth > prefContentWidth + smallSidebarWidth + gap:\n    active = smallSidebar\nelse:\n    active = null', caption='Simplified horizontal breakpoint logic.'),
                Callout('The source uses &gt;= for the large-sidebar threshold and &gt; for the small-sidebar threshold.', kind='note')
            ]),
        Chapter('Forced large sidebar', [
                Figure(f'{G}/interaction.svg', 'Forced display adds the large sidebar and a glass pane.'),
                Para('Forced display only has an effect while the small sidebar is the active sidebar. The large sidebar is made visible, moved to the front and accompanied by a glass pane over the content area.'),
                Table(['largeSidebarCoversSmall', 'Placement'], [['false', 'Large sidebar is placed next to the small sidebar.'], ['true', 'Large sidebar starts at the same edge and covers the small sidebar.']], widths=[42, 58])
            ]),
        Chapter('Styling', [
                Table(['Selector', 'Description'], [["<font face='Courier'>.responsive-pane:left/right/top/bottom</font>", 'side state'], ["<font face='Courier'>:showing-none</font>", 'no sidebar active'], ["<font face='Courier'>:showing-small</font>", 'small sidebar active'], ["<font face='Courier'>:showing-large</font>", 'large sidebar active'], ["<font face='Courier'>:forced</font>", 'forced large visible'], ["<font face='Courier'>:covering</font>", 'forced large covers small']], widths=[50, 50]),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-side</font>", 'Side', 'LEFT'], ["<font face='Courier'>-fx-gap</font>", 'number', '0']], widths=[45, 25, 30]),
                Code('.responsive-pane {\n    -fx-side: left;\n    -fx-gap: 10;\n}\n.responsive-pane:forced .overview {\n    -fx-effect: dropshadow(gaussian, rgba(0,0,0,.26), 10, 0, 5, 0);\n}')
            ]),
        Chapter('Child ownership and z-order', [
                Para("ResponsivePane stores content and sidebars in custom node properties. Assigning a property removes the old node from <font face='Courier'>getChildren()</font> and adds the new node. Removing a node directly from the children list also clears the matching property."),
                Table(['Operation', 'Effect'], [["<font face='Courier'>setContent(node)</font>", 'Old content is removed; new content is added.'], ['Remove content from children', 'The content property is set to null.'], ['Layout content', 'Content is sent to the back when necessary.'], ['Forced large sidebar', 'Large sidebar is brought to the front.']], widths=[38, 62]),
                Para('The internal glass pane is added by the constructor before application nodes. During layout it is resized over the content area and its hide property follows whether a forced large sidebar needs display.'),
                Code('''glassPane.relocate(contentStartX, contentStartY);
glassPane.resize(contentWidth, contentHeight);''')
            ]),
        Chapter('Vertical side layouts', [
                Para("When <font face='Courier'>side</font> is TOP or BOTTOM the same selection algorithm uses heights. The active sidebar stretches across the inside width; content height is reduced by sidebar height plus gap."),
                Table(['Side', 'Active sidebar placement', 'Content adjustment'], [['TOP', 'x = left inset, y = top inset', 'contentStartY += sidebarHeight + gap'], ['BOTTOM', 'x = left inset, y = bottom edge - sidebarHeight', 'contentHeight -= sidebarHeight + gap']], widths=[22, 38, 40]),
                Para("Forced large display also supports top and bottom. If <font face='Courier'>largeSidebarCoversSmall</font> is false, the large sidebar sits next to the small sidebar along the vertical axis; otherwise it covers it from the same edge."),
                Code('''pane.setSide(Side.BOTTOM);
pane.setForceLargeSidebarDisplay(true);
pane.setLargeSidebarCoversSmall(false);''')
            ]),
        Chapter('Designing breakpoints deliberately', [
                Para('ResponsivePane does not use hard-coded breakpoint numbers. The breakpoint is an emergent result of content preferred size, sidebar preferred size and gap. This makes the pane robust when fonts, localization or user scaling change preferred sizes.'),
                Table(['Concern', 'Recommended practice'], [['Preferred sizes', 'Set explicit preferred sizes for important children so the layout maths has stable inputs.'], ['Insets and padding', 'Remember that pane insets are part of the available area calculation or child placement.'], ['Managed state', 'Only managed children should be expected to participate in layout decisions.'], ['Runtime changes', 'Property invalidation calls requestLayout, so batch related changes where possible.']], widths=[34, 66]),
                Para('If a sidebar appears too late or too early, adjust preferred size of the content or sidebar rather than adding external width listeners. The source selection logic already re-runs during layout.'),
                Code('''content.setPrefWidth(420);
smallSidebar.setPrefWidth(56);
largeSidebar.setPrefWidth(220);
pane.setGap(12);''')
            ]),
        Chapter('Recipes', [
                Section('Open overlay navigation'),
                Code('pane.setForceLargeSidebarDisplay(true);'),
                Section('Vertical sidebars'),
                Code('pane.setSide(Side.TOP);\npane.setGap(8);'),
                Section('Checklist'),
                Numbered(['Set preferred sizes on content and both sidebars.', 'Use side to choose width-driven or height-driven breakpoints.', 'Style pseudo classes instead of adding width listeners.', 'Let the glass pane click close forced display.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.ResponsivePaneApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.ResponsivePaneApp</font>)", "<font face='Courier'>HiddenSidesPane</font> - mouse edge overlays.", "<font face='Courier'>PowerPane</font> - application shell composition.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
