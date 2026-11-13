# Docking Events Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver context-wide JavaFX-style lifecycle notifications and an observable selection model.

**Architecture:** Add a lifecycle event class, a context-owned handler registry, and a selection model. Update the model at pane/model mutation boundaries and publish lifecycle notifications at context operation boundaries. Suppress lifecycle events during restoration and guard compound operations against reentrant changes.

**Tech Stack:** Java 11, JavaFX 17, Maven, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-11-13-docking-events-design.md`

## Global Constraints

- Do not commit or push; leave changes in the working tree.
- Deliver on the JavaFX application thread; allow nested docking operations.
- Deliver group-move events as each dockable moves; stop safely if nested changes invalidate the remaining work.
- Report handler exceptions without interrupting other handlers or required cleanup.
- Suppress lifecycle events for layout restoration while updating the selection model; preserve existing close vetoes.

---

### Task 1: Event API and dispatch

**Files:** Create `gemsfx/src/main/java/com/dlsc/gemsfx/docking/DockingEvent.java`; modify `DockingContext.java`; test in `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingEventTest.java`.

**Interfaces:** `DockingEvent` defines `ANY`, `OPENED`, `CLOSED`, `MOVED`, and dockable/old-pane/new-pane getters. Context accepts and removes typed `EventHandler`s.

- [ ] Write an integration test that registers handlers for `OPENED` and `ANY`, opens a dockable, verifies both payloads, removes a handler, and opens another.
- [ ] Run `./mvnw test -pl gemsfx -Dtest=DockingContextTest`; confirm the new test fails because the API is absent.
- [ ] Implement immutable event payloads and handler dispatch; match parent event types, snapshot registrations for nested dispatch, and log individual handler failures while continuing.
- [ ] Run the same command and confirm the test passes.

### Task 2: Lifecycle operations and reentrancy

**Files:** Modify `DockingContext.java` and `DockingContextTest.java`.

**Interfaces:** Use Task 1's lifecycle events at the existing open, drop, close, move, float, and group-move entry points.

- [ ] Write tests for close-before-disposal, veto/no-op suppression, same-pane reorder, cross-pane move, floating, group moves, and an event handler that invalidates a later group member.
- [ ] Run the focused test class and confirm missing notifications and invalidation protection fail.
- [ ] Emit events after successful individual changes with correct old/new panes; emit close before disposal inside cleanup-protected control flow. Revalidate each pending group member and destination after every callback; stop on invalidation without rolling back completed moves.
- [ ] Run the focused test class and confirm the tests pass.

### Task 3: Selection transitions and restore

**Files:** Create `DockingSelectionModel.java`; modify `DockingPane.java` and `DockingContext.java`; add `DockingSelectionModelTest.java`.

**Interfaces:** `DockingContext.getSelectionModel()` exposes an observable selected-items list and a `select(Dockable)` operation. Model group identities are internal only.

- [ ] Write tests for group creation/removal, selection within a continuing group, selection side effects of lifecycle operations, and selection updates during restore.
- [ ] Run the focused test class and confirm the new cases fail.
- [ ] Collect the selected dockable in each nonempty group across all context panes. Update the observable list after changes, including restore, and support selecting an open dockable in its pane.
- [ ] Run focused tests and confirm they pass.

### Task 4: Hardening and verification

**Files:** Modify the above classes and tests only as required; document public API in Javadocs.

- [ ] Write tests proving handler exceptions do not prevent later handlers or disposal and nested operations cannot corrupt the context or prevent cleanup.
- [ ] Run the focused tests and confirm failures before making the necessary corrections.
- [ ] Implement the minimal corrections, then run `./mvnw test -pl gemsfx -Dtest=DockingContextTest,DockingPaneTest,DockingLayoutTest`.
- [ ] Run the existing project verification command `./mvnw -B verify` when feasible; record any environmental blockers rather than claiming success.
- [ ] Review the final diff for unrelated changes, reentrancy, listener lifecycle, API Javadocs, and compliance with the design.
