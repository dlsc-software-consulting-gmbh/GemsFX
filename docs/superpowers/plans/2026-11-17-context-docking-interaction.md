# Context-Level Docking Interaction Toggle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a context-wide flag that disables user-initiated docking interactions without changing programmatic move APIs.

**Architecture:** `DockingContext` owns a `dockingEnabled` JavaFX boolean property, defaulting to `true`. `DockingPaneSkin` consults it only for existing-dockable drag gestures and drops, while the built-in group “Dock Group” menu item reflects the same setting. The move eligibility and mutation APIs remain unchanged.

**Tech Stack:** Java 11, JavaFX 17, JUnit 4, Maven.

**Spec:** `docs/superpowers/specs/2026-11-17-context-docking-interaction-design.md`

## Global Constraints

- “The scope is the whole context; panes do not have individual overrides.”
- “Programmatic operations such as `moveDockable`, `moveDockableToPaneEdge`, and `moveTabGroup` remain available and retain their current behavior.”
- “This setting does not affect opening or closing dockables, external-content drops that create dockables, maximize/restore actions, or internal-floating actions.”
- “The existing `floatingEnabled` setting continues to govern its current floating behavior.”

---

## File Map

- `gemsfx/src/main/java/com/dlsc/gemsfx/docking/DockingContext.java` — own and expose the context-wide property.
- `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java` — gate user drag starts, previews, and drops for existing dockables while leaving external-content drops alone.
- `gemsfx/src/main/java/com/dlsc/gemsfx/docking/DockingTabPaneHeader.java` — disable the built-in “Dock Group” menu action while docking is disabled; leave floating actions alone.
- `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingContextTest.java` — test the default, runtime toggle, and unchanged programmatic move behavior.
- `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingPaneTest.java` — test tab/group drag starts and drops, plus the built-in group menu state.
- `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingContextDropTest.java` — verify external-content drops still work while the flag is disabled.

## Task 1: Add the Context API Without Changing Move APIs

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/DockingContext.java`
- Test: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingContextTest.java`

**Interfaces:**
- Produces `BooleanProperty dockingEnabledProperty()`, `boolean isDockingEnabled()`, and `void setDockingEnabled(boolean enabled)` on `DockingContext`.
- The property defaults to `true` and is not consulted by `canMoveDockable`, `canMoveDockableToPaneEdge`, `canMoveTabGroup`, or their move methods.

- [ ] **Step 1: Write a failing contract test**

Add a test to `DockingContextTest`. Use the existing `invoke(...)` JavaFX-thread helper and `STAGE_FACTORY` in that class:

```java
@Test
public void disablingDockingInteractionsDoesNotDisableProgrammaticMoves() {
    invoke(() -> {
        DockingContext context = new DockingContext(
                id -> Optional.of(new Dockable(id, id, new Label(id))), STAGE_FACTORY);
        DockingPane source = context.createPane();
        context.openDockable(source, "a", DockingPosition.CENTER);
        context.openDockable(source, "b", DockingPosition.CENTER);
        context.openDockable(source, "c", DockingPosition.RIGHT);
        DockingPane target = context.createPane();
        context.openDockable(target, "x", DockingPosition.CENTER);
        DockingPane groupTarget = context.createPane();

        assertTrue(context.isDockingEnabled());
        context.setDockingEnabled(false);
        assertFalse(context.isDockingEnabled());

        assertTrue(context.canMoveDockable(target, "a", "x", DockingPosition.CENTER));
        assertTrue(target.moveDockable("a", "x", DockingPosition.CENTER));
        assertTrue(source.moveDockableToPaneEdge("b", DockingPosition.LEFT));
        assertTrue(context.moveTabGroup(groupTarget, "c", null, DockingPosition.CENTER));
        return null;
    });
}
```

- [ ] **Step 2: Run the test and confirm it fails because the API is absent**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingContextTest test`

Expected: test compilation fails on the missing `isDockingEnabled` and `setDockingEnabled` methods.

- [ ] **Step 3: Add the property and accessors**

In `DockingContext`, add a `BooleanProperty` named `dockingEnabled`, initialized to `true`, and implement:

```java
public final BooleanProperty dockingEnabledProperty() {
    return dockingEnabled;
}

public final boolean isDockingEnabled() {
    return dockingEnabled.get();
}

public final void setDockingEnabled(boolean enabled) {
    dockingEnabled.set(enabled);
}
```

Use `public final` for all three accessors, matching the JavaFX property-method convention. Put the property Javadoc on `dockingEnabledProperty()`. Document that this flag controls user interaction and does not restrict programmatic moves. Do not reference this property from move-eligibility or move-implementation methods.

- [ ] **Step 4: Run the focused contract test**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingContextTest test`

Expected: `DockingContextTest` passes, including all three programmatic move operations with the flag set to `false`.

## Task 2: Prevent New User Docking Drags

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java`
- Test: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingPaneTest.java`

**Interfaces:**
- Consumes `DockingContext.isDockingEnabled()` from Task 1.

- [ ] **Step 1: Write a failing test that disabled contexts do not start tab or group drags**

In `DockingPaneTest`, use its existing `EventTestDockingPaneSkin`, `renderWithEventSkin`, `fireDragDetected`, and `title` helpers:

```java
@Test
public void disabledDockingContextDoesNotStartTabOrGroupDrags() {
    runFx(() -> {
        DockingContext context = new DockingContext(id -> Optional.of(dockable(id)), pane -> new Stage());
        DockingPane pane = context.createPane();
        Dockable a = context.openDockable(pane, "a", DockingPosition.CENTER).orElseThrow();
        context.openDockable(pane, "b", DockingPosition.CENTER);
        renderWithEventSkin(pane);
        context.setDockingEnabled(false);

        fireDragDetected(title(pane, "a"));
        fireDragDetected(title(pane, "a"), true);

        EventTestDockingPaneSkin skin = (EventTestDockingPaneSkin) pane.getSkin();
        assertEquals(0, skin.dragStarts);
        assertTrue(pane.lookupAll(".docking-pane-edge-target").stream().noneMatch(Node::isVisible));
        assertSame(pane, a.getDockingPane());
    });
}
```

- [ ] **Step 2: Run the focused test and confirm it fails**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#disabledDockingContextDoesNotStartTabOrGroupDrags test`

Expected: test compilation succeeds and the drag-start assertion fails because the skin still starts both gestures.

- [ ] **Step 3: Gate drag start in the skin**

In `DockingPaneSkin.installTitleDragHandlers`, check `getSkinnable().getDockingContext().isDockingEnabled()` before setting `dragWholeGroup`, setting the group marker, or calling `startDockableDrag`. Return without starting a drag when disabled.

- [ ] **Step 4: Run the focused test and confirm it passes**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#disabledDockingContextDoesNotStartTabOrGroupDrags test`

Expected: the test passes; no drag starts and no pane-edge indicators are shown.

## Task 3: Reject Active Docking Drops Without Blocking External Drops

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java`
- Test: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingPaneTest.java`
- Test: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingContextDropTest.java`

Add `javafx.scene.input.DragEvent` and `javafx.scene.input.TransferMode` imports to `DockingPaneTest` when adding the active-drag test.

- [ ] **Step 1: Write a failing test for a drag disabled before its drop**

In `DockingPaneTest`, use the existing drag event helpers to start the drag while enabled, disable docking before the target handles it, then assert the preview and move are rejected:

```java
@Test
public void disablingDockingDuringADragRejectsTheDrop() {
    runFx(() -> {
        DockingContext context = new DockingContext(id -> Optional.of(dockable(id)), pane -> {
            throw new AssertionError("A rejected docking drop must not create a floating window");
        });
        DockingPane source = context.createPane();
        Dockable a = context.openDockable(source, "a", DockingPosition.CENTER).orElseThrow();
        renderWithEventSkin(source);
        DockingPane target = context.createPane();
        context.openDockable(target, "x", DockingPosition.CENTER);
        renderWithEventSkin(target);

        Node dragged = title(source, "a");
        fireDragDetected(dragged);
        context.setDockingEnabled(false);
        fireDragEvent(target, DragEvent.DRAG_OVER, dragged, target.getWidth() / 2, target.getHeight() / 2,
                400, 300, TransferMode.MOVE, title(target, "x"));

        assertFalse(target.lookup(".docking-drop-zone-preview").isVisible());
        DragEvent dropped = fireDragEvent(target, DragEvent.DRAG_DROPPED, dragged,
                target.getWidth() / 2, target.getHeight() / 2, 400, 300, TransferMode.MOVE, title(target, "x"));
        assertFalse(dropped.isDropCompleted());
        assertSame(source, a.getDockingPane());
        fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 400, 300, TransferMode.MOVE, dragged);
    });
}
```

- [ ] **Step 2: Run the focused test and confirm it fails**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#disablingDockingDuringADragRejectsTheDrop test`

Expected: test compilation succeeds and the synthetic drop moves the dockable instead of being rejected.

- [ ] **Step 3: Reject only existing-dockable drags while disabled**

In `DockingPaneSkin.handleDragOver` and `handleDragDropped`, after resolving a non-null existing dockable ID, check the context property. When false, clear the preview and pane-edge targets; for a drop, record the unsuccessful drop, set it incomplete, consume it, and return. Keep the `draggedId == null` external-content path unchanged.

- [ ] **Step 4: Run the active-drag test**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#disablingDockingDuringADragRejectsTheDrop test`

Expected: the active dockable drop is rejected and the dockable remains in its source pane.

- [ ] **Step 5: Verify external-content drops remain enabled**

In `DockingContextDropTest`, add a test using the existing `context()` and `dropEvent()` helpers:

```java
@Test
public void externalContentDropIsUnaffectedByDockingToggle() {
    runFx(() -> {
        DockingContext context = context();
        DockingPane pane = context.createPane();
        context.setDockingEnabled(false);
        DragEvent event = dropEvent();
        Dockable created = dockable("new");
        context.setDockableDropPredicate(candidate -> candidate == event);
        context.setDockableDropFactory(candidate -> created);

        assertTrue(context.canAcceptDockableDrop(event));
        assertEquals(Optional.of(created),
                context.dropDockable(pane, event, null, DockingPosition.CENTER));
        assertSame(pane, created.getDockingPane());
    });
}
```

Run: `./mvnw -B -pl gemsfx -Dtest=DockingContextDropTest test`

Expected: all external-content drop tests pass while docking is disabled.

## Task 4: Disable the Built-In “Dock Group” Menu Action

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/DockingTabPaneHeader.java`
- Test: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/DockingPaneTest.java`

- [ ] **Step 1: Write a failing test for the menu action’s enabled state**

Use the existing `groupMenuItem` helper in `DockingPaneTest`:

```java
@Test
public void dockGroupMenuActionTracksDockingEnabled() {
    runFx(() -> {
        DockingContext context = context(dockable("a"));
        DockingPane pane = context.createPane();
        context.openDockable(pane, "a", DockingPosition.CENTER);
        context.floatDockableInternally("a", new Rectangle2D(25, 30, 300, 180));
        layout(pane);
        MenuItem dockGroup = groupMenuItem(pane, "a", "docking-group-dock");

        assertFalse(dockGroup.isDisable());
        context.setDockingEnabled(false);
        assertTrue(dockGroup.isDisable());
        context.setDockingEnabled(true);
        assertFalse(dockGroup.isDisable());
    });
}
```

- [ ] **Step 2: Run the menu test and confirm it fails**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#dockGroupMenuActionTracksDockingEnabled test`

Expected: the test compiles and fails because the menu item does not observe `dockingEnabled`.

- [ ] **Step 3: Bind and guard the built-in menu action**

In `DockingTabPaneHeader`, bind the Dock Group item's disabled state to the context property and the existing maximized-pane condition:

```java
dockGroup.disableProperty().bind(context.dockingEnabledProperty().not()
        .or(pane.maximizedDockableProperty().isNotNull()));
```

Also check `context.isDockingEnabled()` in the item's action handler before calling `context.moveTabGroup(...)`. Do not modify the floating or internal-floating menu actions.

- [ ] **Step 4: Run the menu test and confirm it passes**

Run: `./mvnw -B -pl gemsfx -Dtest=DockingPaneTest#dockGroupMenuActionTracksDockingEnabled test`

Expected: the menu action follows the property in both directions.

## Task 5: Run the Docking Module Regression Suite

**Files:** No additional files.

- [ ] **Step 1: Run all GemsFX library tests**

Run: `./mvnw -B test -pl gemsfx`

Expected: the `gemsfx` module test suite passes with no failures or errors.

- [ ] **Step 2: Review the final diff**

Inspect only the six files listed in the file map. Confirm the implementation does not add the property to programmatic move eligibility, does not alter external drops or floating behavior, and leaves pre-existing worktree changes untouched. Do not commit the changes.
