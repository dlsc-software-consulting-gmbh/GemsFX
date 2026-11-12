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
    package="com.dlsc.gemsfx.docking",
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
                        "Custom tab strip with title, optional graphic, toolbar, help, menu, and close controls.",
                        "Multi-child horizontal/vertical splits with ordered divider positions in [0, 1].",
                        "Drag-and-drop within one pane and across panes in the same context.",
                        "Shift-drag to move an entire tab group; drop onto an empty pane's placeholder to populate it.",
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
                    caption="DockingPane lives in module <font face='Courier'>com.dlsc.gemsfx</font>, package <font face='Courier'>com.dlsc.gemsfx.docking</font>.",
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
                            "An orientation, at least two ordered children, and one fewer normalized divider positions than children.",
                        ],
                        [
                            "Empty-pane placeholder",
                            "Node",
                            "Application-replaceable content shown in an empty pane; accepts dockable drops from the same context.",
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
                            "canMoveTabGroup(...)",
                            "boolean",
                            "false if invalid",
                            "Checks whether a tab group can move into another group, beside a pane, or into an empty pane.",
                        ),
                        Property(
                            "moveTabGroup(...)",
                            "boolean",
                            "false if invalid",
                            "Moves the entire source group while preserving tab order and selection.",
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
                            "Additional veto hook used by close buttons, keyboard close, and floating stage close.",
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
                            "Immutable snapshot of this pane tree, updated on structure, selection, and divider changes.",
                        ),
                        Property(
                            "placeholder",
                            "ObjectProperty&lt;Node&gt;",
                            "DockingPanePlaceholder",
                            "Content shown in an empty pane. Replace it with a custom node, or set null for a blank pane.",
                        ),
                        Property(
                            "selectDockable(id)",
                            "void",
                            "n/a",
                            "Activates a dockable by ID.",
                        ),
                        Property(
                            "updateDividerPositions(path, positions)",
                            "void",
                            "n/a",
                            "Atomically replaces a split's entire divider vector and publishes one layout snapshot.",
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
                Section("Dockable"),
                PropertyTable(
                    [
                        Property(
                            "closeable",
                            "BooleanProperty",
                            "true",
                            "Controls normal close requests and the default tab's close button visibility.",
                        ),
                    ]
                ),
                Para(
                    "Override the protected <font face='Courier'>dispose()</font> method to release resources "
                    "when a dockable is removed from its context. Moving a dockable between panes does not "
                    "dispose it. Layout restore replaces the current layout regardless of close permissions "
                    "and disposes removed or replaced instances, but not reused instances. Hiding an already "
                    "open floating window also removes and disposes its dockables."
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
                        ["Drop on an empty pane's placeholder", "Moves the dockable into the empty pane and selects it. Placeholder child nodes also accept drops."],
                        ["Shift-drag a title", "Moves its entire tab group, preserving tab order and the group's selected tab."],
                        ["ESC during drag", "Cancels external floating; no detachment happens."],
                    ],
                    widths=[43, 57],
                ),
                Para(
                    "For example, with two groups side by side, drag a tab onto the bottom outer-edge "
                    "indicator to place it below both groups. The preview spans the entire pane width. "
                    "The source tab is removed first and empty branches collapse. The new group is added "
                    "to a matching-orientation split, or a new split is created when orientations differ. "
                    "The same operation works when dragging from a floating pane "
                    "in the same context. Indicators disappear after dropping, leaving the pane, or canceling."
                ),
                Code(
                    'pane.moveDockableToPaneEdge("console", DockingPosition.BOTTOM);',
                    caption="Place the console below the pane's entire remaining layout.",
                ),
                Para(
                    "Consecutive splits with the same orientation are flattened into one multi-child split. "
                    "Moving a divider therefore resizes the two adjacent items rather than a nested same-axis "
                    "subtree, subject to the items' minimum and maximum sizes. Opposite-orientation splits "
                    "remain nested and resize as a unit. Inserting a group beside a dockable subdivides only "
                    "that group's existing interval."
                ),
                Para(
                    "Minimum and preferred sizes honor the selected content's bias throughout the split hierarchy. "
                    "Horizontally biased content is measured at each group's allocated width; vertically biased "
                    "content is measured at its allocated height. This also applies to an empty pane's placeholder. "
                    "Switching tabs updates the propagated bias to match the newly selected content."
                ),
                Para(
                    "A normal drag onto an empty pane moves only the dragged dockable; Shift-drag moves its "
                    "whole tab group. Both show a full-pane preview and work over the placeholder itself or "
                    "any of its child nodes. The source and destination must belong to the same DockingContext."
                ),
                Code(
                    'pane.setPlaceholder(new DockingPanePlaceholder("Drop dockables here"));',
                    caption="Customize the default illustrated placeholder's text, or supply any other Node.",
                ),
                Para(
                    "Keyboard interactions of the title controls: LEFT/RIGHT switches selected tab, "
                    "ENTER or SPACE selects the focused title, DELETE or shortcut+W requests close."
                ),
                Para(
                    "Clicking or focusing a dockable's content or title makes it active. If another dockable is "
                    "activated while focus is in a dockable, focus moves into the newly active content "
                    "or, if it has no focusable node, to its title. Focus outside the pane is unchanged."
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
                Section("Multi-child split snapshots"),
                Para(
                    "<font face='Courier'>DockingLayoutNode.Split</font> exposes "
                    "<font face='Courier'>getOrientation()</font>, <font face='Courier'>getChildren()</font>, "
                    "and <font face='Courier'>getDividerPositions()</font>. The child and divider lists are "
                    "immutable defensive copies. For N children there must be exactly N - 1 divider positions, "
                    "in nondecreasing order within [0, 1]. NaN and infinite values are rejected; equal positions "
                    "and the endpoints 0 and 1 are valid and represent collapsed intervals."
                ),
                Code(
                    """DockingLayoutNode.Split root = new DockingLayoutNode.Split(
        Orientation.HORIZONTAL,
        List.of(
                new DockingLayoutNode.Tabs(List.of("editor"), "editor"),
                new DockingLayoutNode.Tabs(List.of("console"), "console"),
                new DockingLayoutNode.Tabs(List.of("inspector"), "inspector")),
        List.of(0.35, 0.8));""",
                    caption="Three groups occupying 35%, 45%, and 20% of a horizontal split.",
                ),
                Callout(
                    "This is a breaking snapshot/API change. Split nodes use children and dividerPositions, "
                    "not first, second, or dividerPosition. There are no legacy constructors, accessors, or "
                    "automatic format adapters; update application-owned serializers accordingly.",
                    kind="note",
                ),
                Section("Divider updates"),
                Para(
                    "<font face='Courier'>updateDividerPositions(path, positions)</font> validates the entire "
                    "divider vector before changing the model and publishes one new snapshot per accepted update. "
                    "Invalid vectors leave the previous layout unchanged. The path is a list of ordered child "
                    "indices: an empty list addresses the root; <font face='Courier'>List.of(2)</font> addresses "
                    "a split in the root's third child. Path indices are not limited to 0 and 1."
                ),
                Code(
                    "pane.updateDividerPositions(List.of(), List.of(0.35, 0.8));",
                    caption="Update both dividers of an existing three-child root split atomically.",
                ),
                Section("Capture and restore"),
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
                Para(
                    "Restoration accepts same-orientation nesting and canonicalizes it into a flat split, "
                    "transforming nested divider positions into their parent intervals. Opposite-orientation "
                    "children remain nested. Captured layouts use the same canonical structure as the renderer, "
                    "with one JavaFX SplitPane per split node."
                ),
                Para(
                    "Unresolved dockables are reported and removed from the restored layout. Empty groups "
                    "are removed and one-child splits collapse. A removed group's interval goes to its preceding "
                    "sibling, or to the following sibling when the first group is removed; other boundaries stay "
                    "unchanged. The same space-allocation rule applies when dockables are closed or moved away."
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
                        ["<font face='Courier'>.docking-tab-pane:active</font>", "Tab group containing the pane's active dockable."],
                        ["<font face='Courier'>.docking-tab</font>", "Custom title tab shape and hover/selected visuals."],
                        ["<font face='Courier'>.docking-tab:selected</font>", "The selected tab in each tab group."],
                        ["<font face='Courier'>.docking-tab-actions</font>", "Shared fading container for the toolbar, help, menu, and close controls."],
                        ["<font face='Courier'>.docking-tab-help-button</font>", "Help action supplied by the dockable's onHelp callback."],
                        ["<font face='Courier'>.docking-tab-menu-button</font>", "Menu populated from the dockable's menuItems list."],
                        ["<font face='Courier'>.docking-tab-close-button</font>", "Close affordance and icon."],
                        ["<font face='Courier'>.docking-split</font>", "SplitPane wrappers created for split layout nodes."],
                        ["<font face='Courier'>.docking-drop-leaf-preview</font>", "Outline of target leaf during drag."],
                        ["<font face='Courier'>.docking-drop-zone-preview</font>", "Filled drop zone preview."],
                        ["<font face='Courier'>.docking-pane-edge-target</font>", "Pane-wide drop indicator; :active highlights the hovered target."],
                        ["<font face='Courier'>.pane-edge-left/right/top/bottom</font>", "Direction-specific style classes of the outer indicators."],
                    ],
                    widths=[46, 54],
                ),
                Para(
                    "Toolbar, help, menu, and close controls share one hover fade. The actions container becomes "
                    "unmanaged when its opacity reaches zero, releasing width for the title without changing tab "
                    "height. Configure optional actions through <font face='Courier'>Dockable.setToolbar(...)</font>, "
                    "<font face='Courier'>setOnHelp(...)</font>, and <font face='Courier'>getMenuItems()</font>."
                ),
                Para(
                    "The default tab height is fixed by CSS at 2.5em, so it scales with the font size rather than "
                    "toolbar content. Child controls are vertically centered, use their preferred widths, and are "
                    "constrained to the available height. Override the preferred height to change all tabs consistently."
                ),
                Code(
                    ".docking-pane .docking-tab {\n"
                    "    -fx-pref-height: 2.75em;\n"
                    "    -fx-min-height: -fx-pref-height;\n"
                    "    -fx-max-height: -fx-pref-height;\n"
                    "}",
                    caption="Keep minimum and maximum heights tied to the font-scaled preferred height.",
                ),
                Code(
                    ".docking-pane .docking-tab:selected .docking-tab-title {\n"
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
