"""Content of the DockingPane developer manual."""

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

G = "docking-pane"


MANUAL = Manual(
    control="DockingPane",
    package="com.dlsc.gemsfx",
    subtitle="A tab-and-split docking control with floating windows and layout snapshots",
    abstract=(
        "DockingPane provides IDE-style docking with draggable tabs, split layouts, "
        "optional floating windows, and complete context snapshots through DockingContext. "
        "This manual covers structure, interaction, stage hosting, persistence hooks, "
        "styling, accessibility, and a practical application recipe."
    ),
    cover_svg=f"{G}/cover.svg",
    cover_caption="Dockables can be grouped as tabs, split into regions, and moved into floating stages.",
    chapters=[
        Chapter(
            "Introduction",
            [
                Para(
                    "<b>DockingPane</b> is a JavaFX <font face='Courier'>Control</font> that renders "
                    "dockables as custom title tabs (not a JavaFX <font face='Courier'>TabPane</font>). "
                    "A <font face='Courier'>DockingContext</font> owns one or more panes, resolves "
                    "dockables by ID, coordinates cross-pane movement, and creates floating windows "
                    "through an application-supplied <font face='Courier'>DockingStageFactory</font>."
                ),
                Section("Key features"),
                Bullets(
                    [
                        "Custom tab strip with title, optional graphic, and close button per dockable.",
                        "Split layouts with horizontal/vertical panes and divider positions in [0, 1].",
                        "Drag-and-drop within one pane and across panes in the same context.",
                        "Four outer-edge indicators for docking beside the entire pane layout.",
                        "Detaching tabs into app-created floating stages.",
                        "Context-wide snapshot and restore via <font face='Courier'>getLayout()</font> and <font face='Courier'>restoreLayout()</font>.",
                        "Close policy hook via <font face='Courier'>setCanCloseDockable(...)</font>.",
                    ]
                ),
                Section("Maven dependency"),
                Code(
                    """<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.7.0</version>
</dependency>""",
                    caption="DockingPane lives in module <font face='Courier'>com.dlsc.gemsfx</font>, package <font face='Courier'>com.dlsc.gemsfx</font>.",
                ),
            ],
        ),
        Chapter(
            "Getting started",
            [
                Para(
                    "A docking setup starts with two factories: one that creates dockables by ID "
                    "and one that returns a stage hosting each floating pane."
                ),
                Code(
                    """DockableFactory dockableFactory = id -> {
    if ("editor".equals(id)) {
        return Optional.of(new Dockable("editor", "Editor", new TextArea()));
    }
    if ("inspector".equals(id)) {
        return Optional.of(new Dockable("inspector", "Inspector", new ListView<>()));
    }
    return Optional.empty();
};

DockingStageFactory stageFactory = pane -> {
    Stage stage = new Stage();
    stage.setTitle("Floating Dock");
    stage.setScene(new Scene(new BorderPane(pane), 420, 280));
    stage.show();
    return stage;
};

DockingContext context = new DockingContext(dockableFactory, stageFactory);
DockingPane pane = context.createPane();
context.openDockable(pane, "editor", DockingPosition.CENTER);
context.openDockable(pane, "inspector", DockingPosition.RIGHT);""",
                    caption="Minimal setup with an ID-based DockableFactory and a shared DockingStageFactory.",
                ),
                Figure(
                    f"{G}/interactions.svg",
                    "Dragging a title reorders tabs, creates splits, or moves a dockable into another pane.",
                ),
                Callout(
                    "DockingContext methods mutate model state and are expected to run on the JavaFX application thread.",
                    kind="note",
                ),
            ],
        ),
        Chapter(
            "Anatomy",
            [
                Figure(f"{G}/anatomy.svg", "Main structural parts of DockingPane."),
                Table(
                    ["Part", "Type", "Description"],
                    [
                        [
                            "DockingPane",
                            "Control",
                            "Root control with style class <font face='Courier'>docking-pane</font> and stylesheet <font face='Courier'>docking-pane.css</font>.",
                        ],
                        [
                            "DockingContext",
                            "Model coordinator",
                            "Tracks open dockables, pane membership, close policy, floating stages, and snapshot/restore.",
                        ],
                        [
                            "Tab group",
                            "Layout node",
                            "A set of dockable IDs plus one selected ID.",
                        ],
                        [
                            "Split node",
                            "Layout node",
                            "Two child nodes with orientation and divider position.",
                        ],
                        [
                            "Floating pane",
                            "DockingPane",
                            "Created by the context when a dockable is detached to a stage.",
                        ],
                    ],
                    widths=[20, 24, 56],
                ),
                Para(
                    "The public layout tree is represented by immutable <font face='Courier'>DockingLayoutNode</font> "
                    "variants: <font face='Courier'>Empty</font>, <font face='Courier'>Tabs</font>, and "
                    "<font face='Courier'>Split</font>."
                ),
            ],
        ),
        Chapter(
            "Control API",
            [
                Section("DockingContext"),
                PropertyTable(
                    [
                        Property(
                            "createPane()",
                            "DockingPane",
                            "new pane",
                            "Creates a pane attached to the context.",
                        ),
                        Property(
                            "openDockable(pane, id, position)",
                            "Optional&lt;Dockable&gt;",
                            "n/a",
                            "Opens the requested dockable ID or activates the existing one if already open.",
                        ),
                        Property(
                            "floatDockable(id, x, y)",
                            "DockingPane",
                            "n/a",
                            "Moves one open dockable into a new floating pane and positions its stage.",
                        ),
                        Property(
                            "getLayout()",
                            "DockingLayout",
                            "snapshot",
                            "Captures all panes and floating stage bounds.",
                        ),
                        Property(
                            "restoreLayout(layout)",
                            "DockingRestoreResult",
                            "n/a",
                            "Rebuilds the context from a snapshot and reports unresolved IDs.",
                        ),
                        Property(
                            "canCloseDockable policy",
                            "Predicate&lt;Dockable&gt;",
                            "always true",
                            "Veto hook used by close buttons, keyboard close, and floating stage close.",
                        ),
                    ]
                ),
                Section("DockingPane"),
                PropertyTable(
                    [
                        Property(
                            "activeDockable",
                            "ReadOnlyObjectProperty&lt;Dockable&gt;",
                            "null",
                            "Currently active dockable of this pane.",
                        ),
                        Property(
                            "layoutRoot",
                            "ReadOnlyObjectProperty&lt;DockingLayoutNode&gt;",
                            "Empty",
                            "Immutable snapshot of this pane tree, updated on structure and selection changes.",
                        ),
                        Property(
                            "selectDockable(id)",
                            "void",
                            "n/a",
                            "Activates a dockable by ID.",
                        ),
                        Property(
                            "updateDividerPosition(path, position)",
                            "void",
                            "n/a",
                            "Stores divider movement for snapshot fidelity.",
                        ),
                        Property(
                            "moveDockable(...)",
                            "boolean",
                            "false if invalid",
                            "Moves a dockable by ID relative to a target dockable.",
                        ),
                        Property(
                            "canMoveDockableToPaneEdge(id, edge)",
                            "boolean",
                            "false if invalid",
                            "Checks whether a dockable can move beside the entire pane layout.",
                        ),
                        Property(
                            "moveDockableToPaneEdge(id, edge)",
                            "boolean",
                            "false if invalid",
                            "Moves a dockable to an outer LEFT, RIGHT, TOP, or BOTTOM edge, spanning all existing groups.",
                        ),
                    ]
                ),
            ],
        ),
        Chapter(
            "Tab and split interactions",
            [
                Para(
                    "Dragging a title inside the pane resolves a target from cursor position and the "
                    "currently hovered title. Center drops merge into tabs, while local edge drops split "
                    "the targeted group. Four additional indicators appear near the outer edges during a "
                    "valid docking drag. Dropping on one of these indicators docks beside the entire pane "
                    "layout, not just the group under the pointer."
                ),
                Table(
                    ["Gesture", "Result"],
                    [
                        ["Drop on center zone", "Dockable joins target tab group and becomes selected."],
                        ["Drop on left/right zone", "Horizontal split is created around the target group."],
                        ["Drop on top/bottom zone", "Vertical split is created around the target group."],
                        ["Drop on outer top/bottom indicator", "New group spans the full pane width above/below all remaining groups."],
                        ["Drop on outer left/right indicator", "New group spans the full pane height beside all remaining groups."],
                        ["Drop on another pane (same context)", "Cross-pane move; source pane may become empty."],
                        ["ESC during drag", "Cancels external floating; no detachment happens."],
                    ],
                    widths=[43, 57],
                ),
                Para(
                    "For example, with two groups side by side, drag a tab onto the bottom outer-edge "
                    "indicator to place it below both groups. The preview spans the entire pane width. "
                    "The source tab is removed first, empty branches collapse, and the remaining layout "
                    "is wrapped in a new split. The same operation works when dragging from a floating pane "
                    "in the same context. Indicators disappear after dropping, leaving the pane, or canceling."
                ),
                Code(
                    'pane.moveDockableToPaneEdge("console", DockingPosition.BOTTOM);',
                    caption="Place the console below the pane's entire remaining layout.",
                ),
                Para(
                    "Keyboard interactions of the title controls: LEFT/RIGHT switches selected tab, "
                    "ENTER or SPACE selects the focused title, DELETE or shortcut+W requests close."
                ),
            ],
        ),
        Chapter(
            "Floating stages",
            [
                Figure(
                    f"{G}/floating-stage.svg",
                    "A detached dockable is hosted by a stage produced by the shared stage factory.",
                ),
                Para(
                    "When a drag ends outside all context panes, or when the app calls "
                    "<font face='Courier'>floatDockable(...)</font>, the context creates a fresh pane and asks "
                    "the stage factory to host it. The factory must return a stage that already contains the supplied pane."
                ),
                Bullets(
                    [
                        "Floating stages are tracked per host stage and close automatically when the host closes.",
                        "If the last dockable leaves a floating pane, its stage is closed automatically.",
                        "Close-veto policy is respected before a floating stage closes.",
                    ]
                ),
            ],
        ),
        Chapter(
            "Persistence",
            [
                Figure(
                    f"{G}/persistence.svg",
                    "Applications own persistence: capture, serialize externally, deserialize, then restore.",
                ),
                Para(
                    "DockingContext intentionally keeps serialization/versioning outside the library. "
                    "Use <font face='Courier'>getLayout()</font> to capture a DTO graph, then serialize it in your own format."
                ),
                Code(
                    """DockingLayout layout = context.getLayout();
String json = mySerializer.write(layout); // application-owned

DockingLayout restored = mySerializer.read(json);
DockingRestoreResult result = context.restoreLayout(restored);
if (!result.getUnresolvedDockableIds().isEmpty()) {
    log.warn("Missing dockables: {}", result.getUnresolvedDockableIds());
}""",
                    caption="Snapshot and restore without introducing a library-side serializer.",
                ),
                Callout(
                    "Restore validates the layout shape and divider range, and requires exactly one primary pane (the pane layout with null stage bounds).",
                    kind="note",
                ),
            ],
        ),
        Chapter(
            "Styling",
            [
                Figure(
                    f"{G}/styling.svg",
                    "Main style classes for tabs, split panes, and drop previews.",
                ),
                Table(
                    ["Selector", "Meaning"],
                    [
                        ["<font face='Courier'>.docking-pane</font>", "Control root background and border."],
                        ["<font face='Courier'>.docking-tab-strip</font>", "Tab strip container and spacing."],
                        ["<font face='Courier'>.docking-tab</font>", "Custom title tab shape and hover/selected visuals."],
                        ["<font face='Courier'>.docking-tab-close-button</font>", "Close affordance and icon."],
                        ["<font face='Courier'>.docking-split</font>", "SplitPane wrappers created for split layout nodes."],
                        ["<font face='Courier'>.docking-drop-leaf-preview</font>", "Outline of target leaf during drag."],
                        ["<font face='Courier'>.docking-drop-zone-preview</font>", "Filled drop zone preview."],
                        ["<font face='Courier'>.docking-pane-edge-target</font>", "Pane-wide drop indicator; :active highlights the hovered target."],
                        ["<font face='Courier'>.pane-edge-left/right/top/bottom</font>", "Direction-specific style classes of the outer indicators."],
                    ],
                    widths=[46, 54],
                ),
                Code(
                    ".docking-pane .docking-tab.selected .docking-tab-title {\n"
                    "    -fx-font-weight: bold;\n"
                    "}\n"
                    ".docking-pane .docking-drop-zone-preview {\n"
                    "    -fx-background-color: -fx-accent;\n"
                    "    -fx-opacity: 0.35;\n"
                    "}",
                    caption="Excerpt from <font face='Courier'>docking-pane.css</font>.",
                ),
            ],
        ),
        Chapter(
            "Accessibility and localization",
            [
                Para(
                    "DockingPane sets <font face='Courier'>AccessibleRole.NODE</font> and a localized role "
                    "description from resource key <font face='Courier'>accessible.role-description</font>."
                ),
                Bullets(
                    [
                        "Each title tab uses <font face='Courier'>AccessibleRole.TAB_ITEM</font> and binds accessible text to the dockable title.",
                        "Close buttons use localized tooltip text (<font face='Courier'>tooltip.close</font>).",
                        "Close buttons format accessible text through <font face='Courier'>accessible.text.close</font> (for example: <i>Close Inspector</i>).",
                    ]
                ),
                Table(
                    ["Resource key", "Default value"],
                    [
                        ["<font face='Courier'>accessible.role-description</font>", "Docking pane"],
                        ["<font face='Courier'>accessible.text.close</font>", "Close {0}"],
                        ["<font face='Courier'>tooltip.close</font>", "Close"],
                    ],
                    widths=[52, 48],
                ),
            ],
        ),
        Chapter(
            "Basic app recipe",
            [
                Numbered(
                    [
                        "Define a stable ID catalog for all dockables your app can create.",
                        "Implement <font face='Courier'>DockableFactory</font> that maps IDs to new Dockable instances.",
                        "Implement one shared <font face='Courier'>DockingStageFactory</font> that wraps supplied panes in app-specific stage chrome.",
                        "Create one DockingContext, one primary DockingPane, and open the initial dockables.",
                        "Persist <font face='Courier'>DockingLayout</font> snapshots using your own serializer and restore on startup.",
                    ]
                ),
                Code(
                    """DockingContext context = new DockingContext(dockableFactory, stageFactory);
DockingPane rootPane = context.createPane();

context.openDockable(rootPane, "editor", DockingPosition.CENTER);
context.openDockable(rootPane, "navigator", DockingPosition.LEFT);
context.openDockable(rootPane, "console", DockingPosition.BOTTOM);

// later
DockingLayout snapshot = context.getLayout();
context.restoreLayout(snapshot);""",
                    caption="Minimal recipe aligned with the DockingPane demo application.",
                ),
            ],
        ),
        Chapter(
            "See also",
            [
                Bullets(
                    [
                        "Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.DockingPaneApp</font> (run: <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.DockingPaneApp</font>).",
                        "Related APIs: <font face='Courier'>DockingContext</font>, <font face='Courier'>DockingLayout</font>, <font face='Courier'>DockingLayoutNode</font>, <font face='Courier'>DockingRestoreResult</font>.",
                        "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
                    ]
                )
            ],
        ),
    ],
)
