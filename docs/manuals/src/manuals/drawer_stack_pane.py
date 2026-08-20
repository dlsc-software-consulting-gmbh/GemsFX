"""Content of the DrawerStackPane developer manual.

Only content lives here - all layout and styling decisions are made by
:mod:`manualkit.document`.
"""

from manualkit import (
    Bullets,
    Callout,
    Chapter,
    Code,
    Figure,
    Manual,
    Numbered,
    Para,
    Property,
    PropertyTable,
    Section,
    Table,
)

G = "drawer-stack-pane"


MANUAL = Manual(
    control="DrawerStackPane",
    package="com.dlsc.gemsfx",
    subtitle="A stack pane with a drawer sliding in from the bottom",
    abstract=(
        "DrawerStackPane is a StackPane that can slide a resizable drawer in from the bottom edge. "
        "While the drawer is open the rest of the pane is covered by a semi-transparent glass pane "
        "that blocks user input. This manual explains the anatomy of the control, its API, its "
        "layout rules, its styling hooks and the most common usage patterns."
    ),
    cover_svg=f"{G}/cover.svg",
    cover_caption="The drawer slides in over a dimmed application window.",
    chapters=[
        # ------------------------------------------------------------------
        Chapter(
            "Introduction",
            [
                Para(
                    "<b>DrawerStackPane</b> extends <font face='Courier'>javafx.scene.layout.StackPane</font> "
                    "and adds a second, optional layer to it: a <i>drawer</i>. The drawer is a panel that "
                    "slides in from the bottom edge of the pane, covering part of the regular content. "
                    "It is a good fit for detail views, log output, filter panels, wizards or anything else "
                    "that should temporarily take over the lower part of a window without opening a "
                    "separate dialog."
                ),
                Para(
                    "The regular content is added the usual way, through the children list of the stack pane. "
                    "The drawer receives its own content through the "
                    "<font face='Courier'>drawerContent</font> property."
                ),
                Section("Key features"),
                Bullets(
                    [
                        "The user can resize the drawer by dragging the header at its top.",
                        "Dragging the header below the lower bounds of the pane closes the drawer completely.",
                        "Opening and closing can be animated (see <font face='Courier'>animateDrawer</font>).",
                        "While the drawer is open, a dark semi-transparent glass pane blocks input to the content below.",
                        "The glass pane can fade in and out.",
                        "The drawer can be given its own preferred width (see <font face='Courier'>preferredDrawerWidth</font>).",
                        "The drawer height can be persisted automatically via the Java preferences API "
                        "(see <font face='Courier'>preferencesKey</font>).",
                        "Auto hiding: the drawer closes when the user clicks the background, and when ESCAPE is pressed.",
                        "An optional title bar with a title, an extra node and a toolbar.",
                    ]
                ),
                Section("Maven dependency"),
                Code(
                    """<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""",
                    caption="The control lives in the module <font face='Courier'>com.dlsc.gemsfx</font>, package <font face='Courier'>com.dlsc.gemsfx</font>.",
                ),
                Callout(
                    "DrawerStackPane is a <i>layout</i>, not a <font face='Courier'>Control</font>. It has no skin "
                    "class; the drawer is assembled directly by the pane and styled through its own user agent "
                    "stylesheet <font face='Courier'>drawer-stackpane.css</font>.",
                    kind="note",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Getting started",
            [
                Para(
                    "The minimal setup consists of three steps: create the pane, add the regular content, "
                    "and set the content of the drawer. The drawer is then shown and hidden by toggling "
                    "<font face='Courier'>showDrawer</font>."
                ),
                Code(
                    """DrawerStackPane pane = new DrawerStackPane();

// regular content: added like on any other stack pane
Button showButton = new Button("Show Drawer");
pane.getChildren().add(showButton);

// drawer content
Label details = new Label("Lorem ipsum dolor sit amet ...");
details.setWrapText(true);
pane.setDrawerContent(new ScrollPane(details));

// title bar (optional)
pane.setShowDrawerTitle(true);
pane.setDrawerTitle("Details");

// open the drawer
showButton.setOnAction(evt -> pane.setShowDrawer(true));""",
                    caption="A complete, runnable setup of a DrawerStackPane.",
                ),
                Figure(
                    f"{G}/states.svg",
                    "Closed, animating and open: setting <font face='Courier'>showDrawer</font> triggers the transition.",
                ),
                Para(
                    "The drawer always spans the full remaining height of the pane below "
                    "<font face='Courier'>topPadding</font>; how much of it is actually visible is "
                    "controlled by <font face='Courier'>drawerHeight</font>, a value between "
                    "<font face='Courier'>0</font> and <font face='Courier'>1</font>. Opening the drawer "
                    "animates that value from <font face='Courier'>0</font> to the last height the user chose."
                ),
                Callout(
                    "A close button is added to the drawer toolbar automatically. You never have to build one "
                    "yourself, and the drawer is always closable via ESCAPE.",
                    kind="tip",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Anatomy",
            [
                Para(
                    "The pane stacks three layers on top of each other: the regular children, the glass pane "
                    "and the drawer. The drawer itself is a <font face='Courier'>VBox</font> with a header "
                    "area on top and the application-provided content below."
                ),
                Figure(f"{G}/anatomy.svg", "The parts of a DrawerStackPane."),
                Table(
                    ["Part", "Node", "Description"],
                    [
                        [
                            "Content",
                            "children of the pane",
                            "Everything added via <font face='Courier'>getChildren()</font>. Laid out by the standard stack pane algorithm.",
                        ],
                        [
                            "Glass pane",
                            "<font face='Courier'>GlassPane</font>",
                            "Dark, semi-transparent overlay. Blocks mouse input to the content and closes the drawer when clicked (see <font face='Courier'>autoHide</font>).",
                        ],
                        [
                            "Drawer",
                            "<font face='Courier'>VBox</font>",
                            "The sliding panel. Unmanaged and positioned manually by <font face='Courier'>layoutChildren()</font>.",
                        ],
                        [
                            "Top container",
                            "<font face='Courier'>StackPane</font>",
                            "Stacks the header box and the drag handle on top of each other.",
                        ],
                        [
                            "Drag handle",
                            "<font face='Courier'>StackPane</font> / <font face='Courier'>VBox</font>",
                            "Three short separators indicating that the drawer can be resized. Mouse transparent.",
                        ],
                        [
                            "Header",
                            "<font face='Courier'>HBox</font>",
                            "Carries the title label, the optional extra node and the toolbar. This is the area the user drags.",
                        ],
                        [
                            "Toolbar",
                            "<font face='Courier'>ToolBar</font>",
                            "Bound to <font face='Courier'>toolbarItems</font>; always contains the built-in close button.",
                        ],
                        [
                            "Drawer content",
                            "any <font face='Courier'>Node</font>",
                            "Set via <font face='Courier'>drawerContent</font>. Gets <font face='Courier'>Priority.ALWAYS</font> for vertical growth.",
                        ],
                    ],
                    widths=[16, 22, 62],
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Control API",
            [
                Section("Content and visibility"),
                PropertyTable(
                    [
                        Property(
                            "drawerContent",
                            "ObjectProperty&lt;Node&gt;",
                            "null",
                            "The node shown inside the drawer. Replacing it removes the previous node from the drawer.",
                        ),
                        Property(
                            "showDrawer",
                            "BooleanProperty",
                            "false",
                            "Opens (<font face='Courier'>true</font>) or closes (<font face='Courier'>false</font>) the drawer, with animation if enabled.",
                        ),
                        Property(
                            "autoHide",
                            "BooleanProperty",
                            "true",
                            "When true, a primary-button click on the glass pane triggers <font face='Courier'>onCloseRequest</font>.",
                        ),
                    ]
                ),
                Section("Title bar and toolbar"),
                PropertyTable(
                    [
                        Property(
                            "showDrawerTitle",
                            "BooleanProperty",
                            "false",
                            "Shows the title label. When false the header content is aligned to the right instead of the left. Styleable.",
                        ),
                        Property(
                            "drawerTitle",
                            "StringProperty",
                            '"Untitled"',
                            "The text of the title label. The default value is localized.",
                        ),
                        Property(
                            "drawerTitleExtra",
                            "ObjectProperty&lt;Node&gt;",
                            "null",
                            "An additional node appended to the header box, for example a status indicator.",
                        ),
                        Property(
                            "toolbarItems",
                            "ListProperty&lt;Node&gt;",
                            "[close button]",
                            "The items of the drawer toolbar. The built-in close button is already part of this list.",
                        ),
                    ]
                ),
                Section("Animation"),
                PropertyTable(
                    [
                        Property(
                            "animateDrawer",
                            "BooleanProperty",
                            "true",
                            "Animates opening and closing. Also drives the fading of the glass pane. Styleable.",
                        ),
                        Property(
                            "animationDuration",
                            "ObjectProperty&lt;Duration&gt;",
                            "250 ms",
                            "Duration of the slide animation and of the glass pane fade. Styleable.",
                        ),
                        Property(
                            "fadeInOut",
                            "BooleanProperty",
                            "true",
                            "Whether the glass pane appears and disappears smoothly. Styleable.",
                        ),
                    ]
                ),
                Section("Callbacks"),
                Para(
                    "Closing the drawer is routed through a single callback. This makes it easy to ask the "
                    "user for confirmation, or to close the drawer from somewhere else in the application."
                ),
                PropertyTable(
                    [
                        Property(
                            "onCloseRequest",
                            "ObjectProperty&lt;Runnable&gt;",
                            "sets showDrawer to false",
                            "Invoked by the close button, by a click on the glass pane, by ESCAPE and when the drawer is dragged out of view.",
                        ),
                        Property(
                            "onDrawerClose",
                            "ObjectProperty&lt;Runnable&gt;",
                            "null",
                            "Invoked <i>after</i> the drawer has become invisible, so after the closing animation has finished.",
                        ),
                    ]
                ),
                Code(
                    """pane.setOnCloseRequest(() -> {
    if (editor.isDirty()) {
        showConfirmationDialog();
    } else {
        pane.setShowDrawer(false);
    }
});

pane.setOnDrawerClose(() -> editor.reset());""",
                    caption="Intercepting the close request and reacting to the finished close.",
                ),
                Callout(
                    "If you replace <font face='Courier'>onCloseRequest</font>, make sure some code path still "
                    "calls <font face='Courier'>setShowDrawer(false)</font> - otherwise the drawer can no longer "
                    "be closed by the user.",
                    kind="warning",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Layout and sizing",
            [
                Para(
                    "The drawer is not managed by the stack pane layout. Instead "
                    "<font face='Courier'>layoutChildren()</font> positions it explicitly, horizontally "
                    "centred and anchored to the bottom edge."
                ),
                Figure(f"{G}/layout.svg", "How the drawer bounds are derived from the properties of the pane."),
                Code(
                    """availableHeight = getHeight() - getTopPadding()
maxDrawerWidth  = getWidth() - 2 * getSidePadding()

x = (getWidth() - drawerWidth) / 2
y = getHeight() - getDrawerHeight() * availableHeight

drawer.resizeRelocate(x, y, drawerWidth, availableHeight * getDrawerHeight())""",
                    caption="The layout algorithm in pseudo code.",
                ),
                Section("Sizing properties"),
                PropertyTable(
                    [
                        Property(
                            "drawerHeight",
                            "DoubleProperty",
                            "0.7 (persisted)",
                            "The visible fraction of the available height. Animated while opening and closing, and changed by dragging.",
                        ),
                        Property(
                            "minDrawerHeight",
                            "DoubleProperty",
                            "0.1",
                            "Lower bound for dragging, as a fraction. Must not be negative. Styleable.",
                        ),
                        Property(
                            "maxDrawerHeight",
                            "DoubleProperty",
                            "1.0",
                            "Upper bound for dragging, as a fraction. Must not be greater than 1. Styleable.",
                        ),
                        Property(
                            "preferredDrawerWidth",
                            "DoubleProperty",
                            "Double.MAX_VALUE",
                            "Width of the drawer in pixels. The default makes the drawer as wide as the pane minus the side paddings. Styleable.",
                        ),
                        Property(
                            "topPadding",
                            "DoubleProperty",
                            "20",
                            "Space kept free above a fully opened drawer, in pixels. Must not be negative. Styleable.",
                        ),
                        Property(
                            "sidePadding",
                            "DoubleProperty",
                            "100",
                            "Space kept free left and right of the drawer, in pixels. Must not be negative. Styleable.",
                        ),
                    ]
                ),
                Para(
                    "Setting <font face='Courier'>preferredDrawerWidth</font> to "
                    "<font face='Courier'>Region.USE_PREF_SIZE</font> makes the drawer exactly as wide as its "
                    "own preferred width, which is useful for narrow forms. Any other value is capped at "
                    "the width of the pane minus twice the side padding."
                ),
                Code(
                    """pane.setPreferredDrawerWidth(Region.USE_PREF_SIZE); // as wide as the content needs
pane.setPreferredDrawerWidth(600);                 // 600 pixels, capped by sidePadding
pane.setSidePadding(0);                            // full width drawer
pane.setTopPadding(60);                            // never cover the toolbar""",
                ),
                Callout(
                    "<font face='Courier'>minDrawerHeight</font>, <font face='Courier'>maxDrawerHeight</font>, "
                    "<font face='Courier'>topPadding</font> and <font face='Courier'>sidePadding</font> validate "
                    "their values and throw an <font face='Courier'>IllegalArgumentException</font> for values "
                    "outside their legal range.",
                    kind="warning",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Interaction and persistence",
            [
                Figure(f"{G}/interaction.svg", "Closing by clicking the glass pane, and resizing by dragging the header."),
                Section("Gestures and keys"),
                Table(
                    ["Gesture", "Effect"],
                    [
                        ["Drag the header up or down", "Resizes the drawer within the min / max bounds."],
                        [
                            "Drag the header below the bottom edge",
                            "Triggers <font face='Courier'>onCloseRequest</font>, so the drawer closes.",
                        ],
                        ["Double click the header", "Maximises the drawer, or closes it when it is already maximised."],
                        [
                            "Primary click on the glass pane",
                            "Triggers <font face='Courier'>onCloseRequest</font> if <font face='Courier'>autoHide</font> is true.",
                        ],
                        ["ESCAPE", "Triggers <font face='Courier'>onCloseRequest</font> while the drawer is open."],
                        ["Close button in the toolbar", "Triggers <font face='Courier'>onCloseRequest</font>."],
                    ],
                    widths=[34, 66],
                ),
                Section("Persisting the drawer height"),
                Para(
                    "Whenever the user finishes a drag, the current "
                    "<font face='Courier'>drawerHeight</font> is written to the Java preferences of the "
                    "package of <font face='Courier'>DrawerStackPane</font>, under the key "
                    "<font face='Courier'>&lt;preferencesKey&gt;.drawer.height</font>. The next time the "
                    "drawer opens, that value is restored and clamped to the range "
                    "<font face='Courier'>[0.1, 1.0]</font>."
                ),
                PropertyTable(
                    [
                        Property(
                            "preferencesKey",
                            "StringProperty",
                            '"drawer.stackpane"',
                            "Prefix of the preferences key. Set it to null to disable persistence, or to a unique value if an application uses several drawers.",
                        )
                    ]
                ),
                Code(
                    """// give every drawer of the application its own remembered height
pane.setPreferencesKey("customer.details");

// or opt out of persistence completely
pane.setPreferencesKey(null);""",
                ),
                Callout(
                    "If the drawer height was never persisted, the drawer opens at "
                    "<font face='Courier'>0.7</font> of the available height.",
                    kind="note",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Styling",
            [
                Para(
                    "The control brings its own user agent stylesheet, "
                    "<font face='Courier'>drawer-stackpane.css</font>. All style classes below are nested "
                    "inside the root class <font face='Courier'>.drawer-stackpane</font>."
                ),
                Section("Style classes"),
                Table(
                    ["Selector", "Node"],
                    [
                        ["<font face='Courier'>.drawer-stackpane</font>", "the pane itself"],
                        ["<font face='Courier'>&gt; .drawer</font>", "the sliding drawer container"],
                        ["<font face='Courier'>&gt; .drawer &gt; .top</font>", "container of header and drag handle"],
                        ["<font face='Courier'>&gt; .top &gt; .header</font>", "the draggable header box"],
                        ["<font face='Courier'>&gt; .header &gt; .title-label</font>", "the drawer title"],
                        ["<font face='Courier'>&gt; .header &gt; .tool-bar</font>", "the toolbar of the drawer"],
                        [
                            "<font face='Courier'>&gt; .tool-bar &gt; .container &gt; .button</font>",
                            "toolbar buttons, including the close button",
                        ],
                        ["<font face='Courier'>.close-button</font>", "the built-in close button"],
                        ["<font face='Courier'>&gt; .top &gt; .drag-handle</font>", "the drag handle area"],
                        ["<font face='Courier'>&gt; .drag-handle &gt; .handle</font>", "the three separator lines"],
                        ["<font face='Courier'>.glass-pane</font>", "the input-blocking overlay"],
                    ],
                    widths=[46, 54],
                ),
                Section("Styleable CSS properties"),
                Table(
                    ["CSS property", "Type", "Default"],
                    [
                        ["<font face='Courier'>-fx-animate-drawer</font>", "boolean", "true"],
                        ["<font face='Courier'>-fx-animation-duration</font>", "Duration", "250ms"],
                        ["<font face='Courier'>-fx-drawer-side-padding</font>", "number", "100"],
                        ["<font face='Courier'>-fx-drawer-top-padding</font>", "number", "20"],
                        ["<font face='Courier'>-fx-fade-in-out</font>", "boolean", "true"],
                        ["<font face='Courier'>-fx-max-drawer-height</font>", "number", "1.0"],
                        ["<font face='Courier'>-fx-min-drawer-height</font>", "number", "0.1"],
                        ["<font face='Courier'>-fx-preferred-drawer-width</font>", "number", "Double.MAX_VALUE"],
                        ["<font face='Courier'>-fx-show-drawer-title</font>", "boolean", "false"],
                    ],
                    widths=[48, 26, 26],
                ),
                Code(
                    """.drawer-stackpane {
    -fx-drawer-side-padding: 40;
    -fx-drawer-top-padding: 60;
    -fx-min-drawer-height: 0.25;
    -fx-animation-duration: 400ms;
    -fx-show-drawer-title: true;
}

.drawer-stackpane > .drawer {
    -fx-background-color: -fx-box-border, -fx-background;
    -fx-background-radius: 8 8 0 0;
}""",
                    caption="Styling the drawer through CSS instead of Java code.",
                ),
                Callout(
                    "Because these are styleable properties, values set from Java code win over values coming "
                    "from a stylesheet unless the stylesheet uses <font face='Courier'>!important</font>.",
                    kind="note",
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Localization",
            [
                Para(
                    "The two strings of the control, the label of the close button and the default drawer "
                    "title, are loaded through <font face='Courier'>ResourceBundleManager</font> from the "
                    "bundle <font face='Courier'>drawer-stack-pane.properties</font>."
                ),
                Table(
                    ["Key", "English text"],
                    [
                        ["<font face='Courier'>button.close</font>", "Close"],
                        ["<font face='Courier'>title.untitled</font>", "Untitled"],
                    ],
                    widths=[40, 60],
                ),
                Para(
                    "Translations ship for Arabic, Chinese, Danish, Dutch, Finnish, French, German, Italian, "
                    "Japanese, Norwegian, Portuguese, Spanish, Swedish and Ukrainian. To override a text, "
                    "put a bundle with the same base name earlier on the class path, or simply set "
                    "<font face='Courier'>drawerTitle</font> yourself."
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "Recipes",
            [
                Section("A drawer without a title bar"),
                Para(
                    "Leaving <font face='Courier'>showDrawerTitle</font> at its default hides the title label "
                    "and aligns the toolbar to the right edge of the header."
                ),
                Code("""pane.setShowDrawerTitle(false); // default"""),
                Section("Custom toolbar items"),
                Para(
                    "Items are added to <font face='Courier'>toolbarItems</font>. Because the built-in close "
                    "button is already part of that list, add your own items at the front to keep the close "
                    "button on the right."
                ),
                Code(
                    """Button refresh = new Button("Refresh");
pane.getToolbarItems().add(0, refresh);"""
                ),
                Section("An extra node in the header"),
                Code(
                    """ProgressIndicator indicator = new ProgressIndicator();
indicator.setPrefSize(16, 16);
pane.setDrawerTitleExtra(indicator);"""
                ),
                Section("Opening the drawer without animation"),
                Code(
                    """pane.setAnimateDrawer(false);
pane.setShowDrawer(true);"""
                ),
                Section("Checklist"),
                Numbered(
                    [
                        "Add the regular content via <font face='Courier'>getChildren()</font>, not via <font face='Courier'>drawerContent</font>.",
                        "Give every drawer of the application its own <font face='Courier'>preferencesKey</font>.",
                        "Wrap long drawer content in a <font face='Courier'>ScrollPane</font>.",
                        "Keep <font face='Courier'>topPadding</font> large enough that the user can still see where the drawer came from.",
                        "If you override <font face='Courier'>onCloseRequest</font>, keep a path that closes the drawer.",
                    ]
                ),
            ],
        ),
        # ------------------------------------------------------------------
        Chapter(
            "See also",
            [
                Bullets(
                    [
                        "Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.DrawerStackPaneApp</font> "
                        "(run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml "
                        "-Dmain.class=com.dlsc.gemsfx.demo.DrawerStackPaneApp</font>)",
                        "<font face='Courier'>GlassPane</font> - the overlay used by this control, also usable standalone.",
                        "<font face='Courier'>HiddenSidesPane</font> - for panels sliding in from any of the four sides.",
                        "<font face='Courier'>DialogPane</font> - when a modal dialog is the better fit.",
                        "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
                    ]
                )
            ],
        ),
    ],
)
