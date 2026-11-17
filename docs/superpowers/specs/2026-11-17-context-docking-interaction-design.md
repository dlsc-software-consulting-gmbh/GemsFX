# Context-Level Docking Interaction Design

## Status

Approved; implementation has not started.

## Purpose

Allow applications to turn off user-initiated rearrangement of existing
dockables within a `DockingContext`, without disabling the context's
programmatic docking API.

## Proposed API

Add a context-wide `BooleanProperty dockingEnabled` to `DockingContext`,
defaulting to `true`, with `dockingEnabledProperty()`,
`isDockingEnabled()`, and `setDockingEnabled(boolean)` accessors. The scope is
the whole context; panes do not have individual overrides.

## Behavior

When enabled, docking interactions continue to work as they do now. When
disabled:

- Users cannot drag a tab or tab group to reorder it, split a pane, or move it
  into another pane.
- The built-in “Dock Group” action is disabled.
- Drag previews and drop handlers must not accept or complete these docking
  interactions.
- Programmatic operations such as `moveDockable`,
  `moveDockableToPaneEdge`, and `moveTabGroup` remain available and retain
  their current behavior.

This setting does not affect opening or closing dockables, external-content
drops that create dockables, maximize/restore actions, or internal-floating
actions. The existing `floatingEnabled` setting continues to govern its
current floating behavior. Applications that add their own user-facing move
actions are responsible for binding those controls to `dockingEnabled`.

## Implementation boundaries

The context property is presentation/interaction policy, not a move
validation rule. Do not add it to the eligibility checks used by public
programmatic move methods. The drag-and-drop UI should consult the property
when starting and accepting existing-dockable drags; the built-in group dock
menu item should reflect it and guard its action. External-dockable creation
and floating interaction paths remain independent.

## Verification

- Verify the property defaults to enabled and can be changed at runtime.
- Verify disabled user interactions do not offer docking previews or complete
  tab/group drops, and the built-in “Dock Group” action is unavailable.
- Verify programmatic move APIs still work when the property is disabled.
- Verify external drops, closing, and existing floating actions retain their
  behavior.
- Run the focused docking tests and the demo tests that cover the affected
  interactions.
