# GemsFX Docking Framework Design

## Status

Updated after review feedback; awaiting user approval. This document describes
the intended first version; it is not an implementation plan.

## Purpose

Add an IDE-style docking workspace to the GemsFX library. Applications can open
panels, arrange them as tabs and nested splits, drag them between dock groups,
and detach them into separate windows. Applications own persistence of the
workspace layout.

## Goals

- Provide a docking context that coordinates multiple panes and floating
  Stages.
- Support tab groups, horizontal and vertical splits, drag-and-drop docking,
  and floating JavaFX windows.
- Allow the application to create panels on demand by stable ID, without
  pre-registering panel instances.
- Expose a data-only multi-pane and multi-Stage layout snapshot that the
  application can serialize and restore.
- Render tabs with a GemsFX-owned component rather than JavaFX `TabPane`.
- Follow the library's control, skin, CSS, accessibility, and Java 11 /
  JavaFX 17 conventions.

## Non-goals

- Choosing or providing a persistence format, storage backend, or automatic
  persistence.
- Supporting distinct document and tool-window models; all dockables use one
  common model.
- Reproducing or embedding another docking library.
- Providing custom operating-system window decorations for floating windows
  in the first version.

## Component model

`DockingContext` is the public coordinator for one docking session. It owns the
ID-based dockable factory, the shared Stage factory, the set of panes and
floating Stages, and the context-wide layout snapshot. Panes may exchange
dockables only when they belong to the same context.

`DockingPane` is the public workspace control for one window and owns that
window's runtime split/tab tree. It follows the GemsFX control/skin/CSS
pattern. Dockables can move freely between panes that belong to the same
context; panes in different contexts do not exchange dockables.

Each panel is represented by a `Dockable` with a stable, context-unique ID,
a title, an optional graphic, and JavaFX content. A dockable is created on
demand by the context's application-supplied ID-based factory; the application
does not pre-register dockable instances. The same factory is used when the
application opens a panel by ID and when a saved layout is restored.

Each tab group is rendered by an internal custom `DockingTabPane`. It does not
use JavaFX `TabPane`. Each dockable is represented by an individual title
control, visually consistent with the title of a single dockable. Selecting a
title displays that dockable's content. The tab pane is an implementation
detail rather than a separately supported public control.

`DockingContext.tabFactoryProperty()` is a `Callback<Dockable, Node>` that
creates each complete tab header, including any close button. The default
factory creates the public `DockingTab` component with the existing title,
graphic, truncation tooltip, and fading close button. A null factory restores
this default. Custom factories must return a fresh, unparented, non-null node
for every invocation; headers can be recreated whenever the layout changes.
The framework continues to provide selection styling, accessibility, keyboard
actions, and docking drag-and-drop. Custom close buttons request closure
through `DockingContext.requestCloseDockable`. Changing the factory refreshes
tabs in every pane belonging to the context, including floating panes.

Applications can set `Dockable.onHelpProperty()` to a `Runnable` to provide
help for an individual dockable. The default `DockingTab` then shows a help
button immediately before Close. Both buttons fade in and out over 150ms
when the tab or its displayed content area is hovered; other dockables are
unaffected. Hover changes during a fade reverse from the current opacity. A null
callback hides the button without reserving layout space. Setting, replacing,
or removing the callback updates existing tabs immediately; activating Help
invokes the current callback on the JavaFX application thread without selecting
or closing the dockable. The tooltip and title-aware accessible text are
localized, and the icon is sized through CSS.

Each pane maintains a runtime tree of split nodes and tab groups. Split nodes
record orientation and divider proportions; tab groups record dockable IDs,
order, and selection. The context coordinates moving content between pane
trees and windows, respecting JavaFX's single-parent rule for a `Node`.

## Package layout

The public docking API and controls live in `com.dlsc.gemsfx.docking`,
including `Dockable`, the factory interfaces, `DockingContext`, `DockingPane`,
`DockingPanePlaceholder`, `DockingTab`, and the layout snapshot types.

The mutable runtime layout model lives in `com.dlsc.gemsfx.docking.model`:
`DockingTree`, `DockingSplit`, and `DockingTabGroup`. It is shared by the
workspace control, context, and skin, but remains an unexported implementation
detail rather than supported public API.

`DockingPaneSkin` and the package-private `DockingTabPane` live in the shared
`com.dlsc.gemsfx.skins` package. There is no separate docking skin package.
The docking control stylesheets live alongside their controls under
`gemsfx/src/main/resources/com/dlsc/gemsfx/docking/`.

Docking tests mirror the API and model packages under `src/test/java`;
JavaFX integration tests import the shared `com.dlsc.gemsfx.FxTestBase`.

## Docking interactions and lifecycle

- Dragging a title onto the center of a tab group adds the dockable to that
  group. Dropping on the left or right edge creates a horizontal split;
  dropping on the top or bottom edge creates a vertical split.
- Titles can be reordered within a group and moved between dock groups.
- Dragging a title outside all panes creates a new `DockingPane` containing
  only that dockable. The context passes the new pane to the shared application
  Stage factory, which returns a Stage that hosts it. The context positions the
  Stage at the screen coordinates of the drop event.
- Dockables can move freely between any panes in the same context. Floating
  panes use the same custom tab pane and docking targets.
- If moving the last dockable out of a floating pane leaves it empty, the
  context closes that pane's Stage. A non-floating host pane may remain empty.
- If the Stage factory fails while creating a floating pane, the dockable
  remains in its source pane.
- Floating windows use standard JavaFX Stage decorations in the first version.
- Each default title has an individual close affordance; custom tab factories
  can replace or omit it. Closing requests removal from
  the workspace; an application close-request handler can veto the removal.
  A veto leaves that dockable in its current location. Closing a floating Stage
  requests removal of all its dockables; if any request is vetoed, the Stage
  remains open with its dockables unchanged.
- When a dockable is removed or moved, empty tab groups and redundant split
  nodes are collapsed.
- Closing the host window closes its floating Stages.

## Layout snapshots and restore

`DockingLayout` is a data-only snapshot of a context. It contains the pane
trees, stable dockable IDs, split orientation and proportions, tab order and
selection, pane-to-Stage associations, and floating-window bounds. It never
contains JavaFX `Node`s or live `Stage` instances. The application is
responsible for serializing and storing this data.

Restoring a layout invokes the context's application dockable factory with
each saved dockable ID and recreates floating Stages through the shared
application Stage factory. Each Stage factory call receives the new
`DockingPane` it must host. If the dockable factory cannot resolve an ID,
restore continues with the remaining panels and returns the unresolved IDs in
a `RestoreResult`; it does not silently discard that information. Empty
groups, splits, and floating panes are collapsed after unresolved IDs are
removed. If a factory throws, the failure is surfaced to the caller.

The application can open a dockable by ID into a pane at runtime through the
same factory, without registering instances up front. An open request
specifies a docking target; opening into an empty pane creates its root group.
Opening an ID already present in the context activates the existing dockable
instead of creating a duplicate. IDs must be unique within a context, and a
factory-created dockable must match the requested ID.

The dockable factory indicates an unresolvable ID with an empty result.
Factory exceptions and invalid snapshots fail restore explicitly. Restore
resolves dockables and creates replacement Stages before replacing the current
layout; if a factory throws, the current layout remains unchanged and any
replacement Stages created for the failed restore are closed. An empty
dockable-factory result is different: the snapshot is restored with that ID
omitted, empty groups, splits, and floating panes are collapsed, and the ID is
returned in the result. Applications own snapshot serialization, schema
versioning, and migrations; the library does not prescribe a storage format
or provide automatic migration.

## Styling and accessibility

The custom title controls and docking targets are styled through GemsFX CSS
with style classes scoped to the workspace. Selection, close, and drag
feedback must be visually distinguishable and themeable.

Custom tab selection and close actions must be keyboard-operable. The controls
must expose appropriate JavaFX accessibility roles and localized role/value
text using the library's accessibility utilities and resource bundles.

## Errors and invariants

- Duplicate dockable IDs and invalid layout structures are rejected explicitly.
- Factory misses are represented in the restore result; factory exceptions
  are surfaced rather than converted to success-shaped fallbacks. Failed
  restores do not replace the current layout.
- Dock operations and factory-created JavaFX content run on the JavaFX
  application thread.
- A dockable's content node is managed by one workspace location at a time.

## Testing

Tests should cover:

- Tab insertion, selection, reordering, and removal.
- Split creation, divider state, moving dockables, and collapsing empty groups.
- Docking-target behavior for center, edges, and floating windows.
- Inter-pane moves between panes in the same context.
- Floating pane creation through the Stage factory, positioning at drop
  coordinates, and automatic Stage closure when the last dockable is moved
  out.
- Close-request veto and successful removal, including closing a floating
  Stage.
- Context snapshot round-trip for multiple pane trees, tab order/selection,
  Stage associations, and floating bounds.
- Restore with unresolved IDs, and explicit propagation of factory failures.
- Accessibility roles and accessible text for the public control and
  title controls.

The layout model should be testable independently where possible; JavaFX
integration tests should cover the custom skin, drag-and-drop, and Stage
lifecycle. No new dependency is required.

## Compatibility

The library implementation must remain compatible with the gemsfx module's
Java 11 and JavaFX 17 baseline. The design adds no external docking dependency.
