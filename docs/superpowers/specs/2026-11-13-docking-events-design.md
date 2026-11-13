# Docking Events Design

## Purpose and scope

Allow applications to react to dockable lifecycle and selection changes across
one `DockingContext`. Events are notifications after changes, not requests to
approve them. The existing dockable close-request callback and context close
policy remain responsible for close vetoes.

The first version does not introduce separate window, layout-restored, or
layout-persistence events, nor a public tab-group identity.

## Public API

`DockingContext` provides JavaFX-style `addEventHandler` and
`removeEventHandler` methods. Handlers register for JavaFX `EventType`s and
receive JavaFX `Event` subclasses. The lifecycle event family has an `ANY`
type for observing all lifecycle events.

The lifecycle event family has types for:

- A dockable opened in the context.
- A dockable closed and removed from the context.
- A dockable moved, including floating, docking, moving within a pane, and
  reordering tabs.

Each lifecycle event identifies the affected `Dockable`. Open and close events
also identify its pane. Move events identify the old and new panes; these can
be the same pane. A whole-group move produces an individual move event for each
affected dockable.

`DockingContext.getSelectionModel()` exposes a docking-specific selection model.
Its unmodifiable observable selected-items list contains the selected dockable
in each nonempty tab group across all panes, including floating panes. Selecting
through the model selects the dockable in its group and makes it active in its
pane without changing the selections of other groups. Selection changes are
observed through the list rather than selection events. Tab-group identity is
not part of the public API.

## Delivery and behavior

Events are delivered synchronously on the JavaFX application thread for
successful user and programmatic operations. No events are emitted for changes
made by layout restoration. A failed, rejected, or vetoed operation does not
produce a lifecycle event for a change that did not occur. Opening an already
open dockable activates it rather than opening it again, so it does not produce
an opened event.

The selection model reflects changes caused by opening, closing, moving,
selecting, and restoring a layout. Lifecycle events for a compound operation
are delivered as individual changes occur, rather than being held until the
compound operation completes.

Close notifications occur after removal from the context and pane but before
the removed dockable is disposed, allowing handlers to inspect its live state.
The framework still completes required disposal and window cleanup. Reopening
the same dockable instance while it is closing is rejected; it may be reopened
after disposal finishes.

Handlers may synchronously perform additional docking operations. Nested
operations must not corrupt docking state or prevent required cleanup. Because
one handler may change the workspace before another runs, handlers cannot
assume that the current workspace still matches the state described by an
earlier event; the event's own data describes the change it reports.
If a nested operation invalidates the remaining work of a group move, the
outer move stops safely and retains the changes already completed.

Exceptions thrown by handlers are reported without stopping delivery to other
handlers or preventing required cleanup. They do not veto or undo the change.

## Verification

Focused tests cover opening, closing, moving between panes, same-pane moves and
reordering, floating, group moves, and selection across groups and panes,
including selection changes caused by other operations, group creation or
removal, and layout restore. Tests also cover programmatic operations, close
vetoes, unsuccessful operations, lifecycle-event restore suppression, handler
registration and removal, handler exceptions and cleanup, and nested operations.
