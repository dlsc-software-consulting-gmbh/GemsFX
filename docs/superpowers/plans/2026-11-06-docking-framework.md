# Docking Framework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an IDE-style, multi-window docking framework for GemsFX with custom dockable title tabs, split layouts, drag-and-drop between panes, and app-owned persistence.

**Architecture:** `DockingContext` coordinates `DockingPane`s, the dockable factory, floating Stage factory, and context-wide layout state. Each `DockingPane` owns a split/tab tree rendered by `DockingPaneSkin` and an internal custom `DockingTabPane`; no JavaFX `TabPane` is used. A data-only `DockingLayout` captures all panes and floating Stage geometry for application-controlled serialization.

**Tech Stack:** Java 11, JavaFX 17, Maven, JUnit 4, GemsFX control/skin/CSS conventions, `ResourceBundleManager`, the existing Python manual builder.

**Spec:** `docs/superpowers/specs/2026-11-06-docking-framework-design.md`

## Global Constraints

- The library implementation must remain compatible with the gemsfx module's Java 11 and JavaFX 17 baseline.
- The design adds no external docking dependency.
- Docking tabs must use a GemsFX-owned custom component rather than JavaFX `TabPane`.
- The application owns snapshot serialization, schema versioning, and migrations.
- Dock operations and factory-created JavaFX content run on the JavaFX application thread.
- Accessibility roles and localized role/value text use the library's accessibility utilities and resource bundles.
- Leave all changes uncommitted; the user handles commits.

---

### Task 1: Define Dockables and the Context Layout DTO

**Files:**
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/Dockable.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingPosition.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingLayout.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingRestoreResult.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingLayoutNode.java`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockableTest.java`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingLayoutTest.java`

**Interfaces:**
- Consumes: none.
- Produces:
  - `Dockable(String id, String title, Node content)` and `Dockable(String id, String title, Node graphic, Node content)`, with `getId()`, `getTitle()`, `getGraphic()`, and `getContent()`.
  - `DockingPosition` values `CENTER`, `LEFT`, `RIGHT`, `TOP`, and `BOTTOM`.
  - Immutable `DockingLayout(String primaryPaneId, List<PaneLayout> panes)`.
  - `DockingLayoutNode` is an interface with immutable nested classes `Empty`, `Tabs(List<String> dockableIds, String selectedId)`, and `Split(Orientation orientation, double dividerPosition, DockingLayoutNode first, DockingLayoutNode second)`. Implement these as Java 11 classes, not records. Use an `Empty` root for an empty primary pane.
  - `DockingLayout.PaneLayout(String paneId, DockingLayoutNode root, StageBounds stageBounds)`; `stageBounds == null` identifies the primary pane. `DockingLayout.StageBounds(double x, double y, double width, double height)` contains only numbers.
  - `DockingRestoreResult` exposing the IDs that the dockable factory could not resolve.

- [ ] **Step 1: Write failing value-object tests in test classes extending `FxTestBase`.** Cover required IDs/titles/content, optional graphics, `DockingPosition` values, immutable snapshot collections, floating bounds, and unresolved-ID results.

```java
@Test
public void dockableRetainsIdentityAndContent() {
    Dockable dockable = invoke(() -> {
        Label content = new Label("Editor");
        return new Dockable("editor:main", "Editor", content);
    });

    assertEquals("editor:main", dockable.getId());
    assertEquals("Editor", dockable.getTitle());
    assertSame(content, dockable.getContent());
}
```

- [ ] **Step 2: Run the focused tests and verify they fail because the types do not exist.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockableTest,DockingLayoutTest`

Expected: test compilation fails on the missing docking types.

- [ ] **Step 3: Implement the immutable public values.** Reject null or blank IDs, null required content, non-finite Stage coordinates, and non-positive Stage dimensions. Copy incoming collections so callers cannot mutate a saved snapshot through an input list.

- [ ] **Step 4: Run the focused tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockableTest,DockingLayoutTest`

Expected: PASS.

### Task 2: Add DockingContext and Factory Contracts

**Files:**
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockableFactory.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingStageFactory.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingContext.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingPane.java` (control shell)
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingContextTest.java`

**Interfaces:**
- Consumes: `Dockable`, `DockingPosition`, `DockingLayout`, `DockingRestoreResult`.
- Produces:
  - `DockableFactory.create(String id)` returning `Optional<Dockable>`.
  - `DockingStageFactory.create(DockingPane pane)` returning the Stage that hosts the supplied pane.
  - `DockingContext(DockableFactory dockableFactory, DockingStageFactory stageFactory)`.
  - `DockingContext.createPane()`, `Optional<Dockable> openDockable(DockingPane pane, String id, DockingPosition position)`, and `getDockables()`. The `getLayout()` and `restoreLayout(DockingLayout)` APIs are introduced in Task 7 with the complete pane-tree and transactional restore behavior.
  - `DockingPane.getDockingContext()` and `getActiveDockable()`.
  - `DockingContext.setCanCloseDockable(Predicate<Dockable> policy)`; `true` from the policy means the dockable may close.

- [ ] **Step 1: Write failing context tests in a class extending `FxTestBase`.** Verify that a context stores both factories, creates panes attached to itself, rejects a pane belonging to another context in an open request, opens an ID once, rejects a factory-created dockable whose ID differs from the requested ID without changing state, and returns `Optional.empty()` without changing context or pane state when the factory cannot resolve an ID.

```java
@Test
public void openingAnExistingIdActivatesRatherThanDuplicates() {
    invoke(() -> {
        Dockable editor = new Dockable("editor:main", "Editor", new Label());
        DockingContext context = new DockingContext(
                id -> id.equals(editor.getId()) ? Optional.of(editor) : Optional.empty(),
                pane -> {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(pane));
                    return stage;
                });
        DockingPane pane = context.createPane();

        context.openDockable(pane, "editor:main", DockingPosition.CENTER);
        context.openDockable(pane, "editor:main", DockingPosition.CENTER);

        assertEquals(1, context.getDockables().size());
        assertSame(editor, pane.getActiveDockable());
        return null;
    });
}
```

- [ ] **Step 2: Run the focused context test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingContextTest`

Expected: test compilation fails on missing context/factory APIs.

- [ ] **Step 3: Implement the factory interfaces and context shell.** Keep all model changes on the JavaFX application thread. Invoke the dockable factory only on an explicit open or restore request; do not pre-register items. Reject a factory result whose dockable ID does not match the requested ID.

- [ ] **Step 4: Run the focused context test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingContextTest`

Expected: PASS.

### Task 3: Implement the Pane Tree and Invariants

**Files:**
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTree.java` (internal to the module; do not export the package)
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTabGroup.java` (internal to the module; do not export the package)
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingSplit.java` (internal to the module; do not export the package)
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/internal/DockingTreeTest.java`

**Interfaces:**
- Consumes: `Dockable`, `DockingPosition`, pane-tree values from `DockingLayout`.
- Produces:
  - Internal `DockingTree()` methods `addToRoot(Dockable)`, `splitAt(String existingId, Dockable inserted, DockingPosition edge, double dividerPosition)`, `remove(String id)`, `dockableIds()`, and `root()`, implemented as public types inside the unexported `com.dlsc.gemsfx.docking.internal` package so `DockingPaneSkin` can use them without making them exported API.
  - An internal tree API to select/reorder tabs, move dockables, collapse empty groups/splits, and create a validated immutable snapshot.
  - A tree invariant: each dockable ID occurs at most once in the entire `DockingContext`, while each pane owns exactly one root (empty or populated).

- [ ] **Step 1: Write failing model tests.** Test center insertion, all four split edges and orientations, selected-tab changes, tab reordering, removal, and collapsing one-child splits. Create each JavaFX content node through `FxTestBase.invoke`; the tree implementation itself remains independent of the toolkit.

```java
@Test
public void removingLastItemCollapsesItsTabGroupAndSplit() {
    DockingTree tree = new DockingTree();
    Dockable left = invoke(() -> new Dockable("left", "Left", new Label()));
    Dockable right = invoke(() -> new Dockable("right", "Right", new Label()));
    tree.addToRoot(left);
    tree.splitAt("left", right, DockingPosition.RIGHT, 0.5);

    tree.remove("left");

    assertEquals(List.of("right"), tree.dockableIds());
    assertFalse(tree.root() instanceof DockingSplit);
}
```

- [ ] **Step 2: Run the model test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingTreeTest`

Expected: test compilation fails on missing tree types.

- [ ] **Step 3: Implement the tree operations with validation.** Keep tree nodes independent of JavaFX `Node`s; they refer to dockables by ID. Reject a duplicate ID and any divider proportion outside the closed interval `[0, 1]` rather than repairing the input silently.

- [ ] **Step 4: Run the model test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingTreeTest`

Expected: PASS.

### Task 4: Build DockingPane and the Custom Title-Tab Skin

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingContext.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingPane.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTree.java`
- Create: `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java`
- Create: `gemsfx/src/main/resources/com/dlsc/gemsfx/docking-pane.css`
- Create: `gemsfx/src/main/resources/docking-pane.properties`
- Create: `gemsfx/src/main/resources/docking-pane_{ar,da,de,en,es,fi,fr,it,ja,nl,no,pt,sv,uk,zh}.properties`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingPaneTest.java`
- Modify: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingContextTest.java`
- Modify: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/internal/DockingTreeTest.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/util/ResourceBundleManager.java`
- Modify: `gemsfx/src/test/java/com/dlsc/gemsfx/AccessibilityTest.java`

**Interfaces:**
- Consumes: `DockingContext`, `DockingTree`, and `Dockable`.
- Produces:
  - `DockingPane extends Control`, associated with exactly one context and owning one `DockingTree`. It exposes the active dockable plus `ReadOnlyObjectProperty<DockingLayoutNode> layoutRootProperty()` / `getLayoutRoot()` snapshots and `selectDockable(String id)`; `DockingContext.openDockable` inserts into its tree at the requested position. The public skin-facing API must not expose the unexported tree types.
  - Dragging a divider updates the tree's stored divider proportion and the pane's layout snapshot, so later snapshots preserve the visible split layout.
  - `DockingPaneSkin extends SkinBase<DockingPane>`.
  - A package-private custom `DockingTabPane` that renders individual title controls; it must not instantiate JavaFX `TabPane`.
  - Localized close-button and accessibility strings through a new `BundleType.DOCKING_PANE`.
  - `DockingContext.requestCloseDockable(Dockable)` removes the dockable only when its configured close policy permits it and returns whether removal occurred; a veto leaves the pane and context unchanged. The title close affordance calls this method.

- [ ] **Step 1: Add failing control, context, and tree tests.** Check pane/tree integration, requested-position insertion, divider proportion updates including endpoints, the pane's context, accessible role, stylesheet, skin creation, keyboard title selection/close, and that selecting an individual title displays only its content. Verify an accepted close removes the dockable and a veto leaves it in place.

```java
@Test
public void dockingPaneCreatesItsCustomSkin() {
    invoke(() -> {
        DockingContext context = new DockingContext(
                id -> Optional.empty(),
                pane -> {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(pane));
                    return stage;
                });
        DockingPane pane = context.createPane();
        DockingPane laidOutPane = layout(pane);

        assertTrue(laidOutPane.getSkin() instanceof DockingPaneSkin);
        return null;
    });
}
```

- [ ] **Step 2: Add the pane to `AccessibilityTest` and run the focused tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingPaneTest,DockingContextTest,DockingTreeTest,AccessibilityTest`

Expected: the new pane's role and skin assertions fail before implementation.

- [ ] **Step 3: Implement the control, skin, and custom title strip.** Match the selected visual direction: each dockable has its own title-like surface, selected state, and close affordance. Define kebab-case CSS classes rooted under `.docking-pane`; add localized role descriptions and close-button text with English fallback, with bundle files for the locale set already supported by GemsFX. Implement keyboard-operable title selection and close through the context's close policy. Keep divider proportions synchronized with user-driven divider changes without rebuilding the active drag target on every intermediate update.

- [ ] **Step 4: Run the focused control and accessibility tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingPaneTest,DockingContextTest,DockingTreeTest,AccessibilityTest`

Expected: PASS.

### Task 5: Add In-Pane Drag-and-Drop Docking

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingPane.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTree.java`
- Modify: `gemsfx/src/main/resources/com/dlsc/gemsfx/docking-pane.css`
- Modify: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingPaneTest.java`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/docking/internal/DockingDropTargetTest.java`

**Interfaces:**
- Consumes: the custom title controls and tree operations from Tasks 3–4.
- Produces: drag handlers that reorder a title within its group, move it to the center of another group, or create a left/right horizontal or top/bottom vertical split. Empty groups and redundant split nodes collapse after a move.

- [ ] **Step 1: Write failing drag-target tests.** Cover center, left/right, and top/bottom target resolution including nested splits; same-group title reordering through the drag resolver; successful edge moves with correct orientation/order and collapse of empty groups/redundant splits; source retention for rejected or cancelled drops; and the UI event-to-resolver path plus preview cleanup after an unsuccessful drag.

- [ ] **Step 2: Run the targeted tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingPaneTest,DockingDropTargetTest`

Expected: target-resolution and move assertions fail before drag handlers exist.

- [ ] **Step 3: Implement the drag handlers and drop preview.** Use `DragEvent.getScreenX()` and `getScreenY()` only for external-window placement in Task 6; use pane-local coordinates to resolve in-pane targets. Show a themeable preview on the resolved tab group/drop zone rather than only on the whole pane. Consume only accepted drop events and clear preview after rejected or completed drops.

- [ ] **Step 4: Run the targeted tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingPaneTest,DockingDropTargetTest`

Expected: PASS.

### Task 6: Add Floating Panes and Cross-Pane Transfers

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingContext.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingPane.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/skins/DockingPaneSkin.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTree.java`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingContextWindowTest.java`

**Interfaces:**
- Consumes: `DockingStageFactory`, context membership checks, and in-pane drag support.
- Produces:
  - Dragging outside all panes creates a new pane containing only the dragged dockable.
  - The shared Stage factory receives that pane and returns its host Stage; the context places the Stage at the drop event's screen x/y.
  - A dockable can move between any panes in the same context.
  - A floating Stage closes when its pane becomes empty; closing a Stage requests closure of its dockables and respects the close veto policy.
  - If Stage creation fails, the dragged dockable remains in its source pane.

- [ ] **Step 1: Write failing window-lifecycle tests.** Cover Stage-factory invocation with the newly created pane, exact screen-coordinate placement, cross-pane moves, empty floating Stage closure, host-window closure of floating Stages, Stage-factory exceptions, and a close veto retaining the floating pane.

- [ ] **Step 2: Run the window-lifecycle test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingContextWindowTest`

Expected: test compilation fails on the missing cross-window behavior.

- [ ] **Step 3: Implement floating-pane creation and transfer.** Keep the source pane unchanged until the Stage factory returns a Stage that hosts the new pane. Register the new pane and Stage with the context only after successful creation.

- [ ] **Step 4: Run the window-lifecycle test.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingContextWindowTest`

Expected: PASS.

### Task 7: Implement Context Snapshot and Restore

**Files:**
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingContext.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingLayout.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/DockingRestoreResult.java`
- Modify: `gemsfx/src/main/java/com/dlsc/gemsfx/docking/internal/DockingTree.java`
- Create: `gemsfx/src/test/java/com/dlsc/gemsfx/DockingContextRestoreTest.java`

**Interfaces:**
- Consumes: all context panes, the dockable factory, Stage factory, and immutable pane-tree values.
- Produces:
  - `DockingContext.getLayout()` capturing every pane tree, selected tab, split proportion, Stage association, and floating Stage bounds.
  - `DockingContext.restoreLayout(DockingLayout)` using the dockable factory by stable ID and the Stage factory for floating panes.
  - Partial restore for unresolved IDs, returned in `DockingRestoreResult`; invalid layouts and factory exceptions fail explicitly without replacing the current context layout.

- [ ] **Step 1: Write failing round-trip and error tests in a class extending `FxTestBase`.** Cover multiple panes, tab order/selection, split proportions, Stage bounds, unknown IDs, invalid duplicate IDs and divider proportions outside `[0, 1]`, dockable-factory exceptions, and Stage-factory exceptions.

```java
@Test
public void restoreReportsUnresolvedDockableIds() {
    DockingContext context = new DockingContext(
            id -> id.equals("editor:main")
                    ? Optional.of(new Dockable(id, "Editor", new Label()))
                    : Optional.empty(),
            pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane));
                return stage;
            });
    DockingLayout layout = new DockingLayout("main", List.of(
            new DockingLayout.PaneLayout("main",
                    new DockingLayoutNode.Tabs(
                            List.of("editor:main", "tool:removed"), "editor:main"),
                    null)));
    DockingRestoreResult result = invoke(() -> context.restoreLayout(layout));

    assertEquals(Set.of("tool:removed"), result.getUnresolvedDockableIds());
    assertTrue(context.getDockables().stream()
            .anyMatch(dockable -> dockable.getId().equals("editor:main")));
}
```

- [ ] **Step 2: Run the restore tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockingContextRestoreTest`

Expected: restore and round-trip assertions fail before the implementation.

- [ ] **Step 3: Implement snapshot/restore transactionally.** Resolve all dockables and create replacement Stages before swapping the live context state. On a thrown factory exception, close replacement Stages and retain the prior state. For an empty dockable-factory result, omit that ID, collapse empty structures, and return the unresolved ID.

- [ ] **Step 4: Run the restore tests and all docking tests.**

Run: `./mvnw -B test -pl gemsfx -Dtest=DockableTest,DockingLayoutTest,DockingTreeTest,DockingContextTest,DockingPaneTest,DockingDropTargetTest,DockingContextWindowTest,DockingContextRestoreTest,AccessibilityTest`

Expected: PASS.

### Task 8: Add the Demo, Showcase Entry, and Manual

**Files:**
- Create: `gemsfx-demo/src/main/java/com/dlsc/gemsfx/demo/DockingPaneApp.java`
- Modify: `gemsfx-showcase/src/main/java/com/dlsc/gemsfx/showcase/ShowcaseRegistry.java`
- Create: `docs/manuals/src/manuals/docking_pane.py`
- Create: `docs/manuals/src/manuals/docking_pane_graphics.py`
- Create: `docs/manuals/src/graphics/docking-pane/` generated SVGs
- Create: `docs/manuals/docking-pane.pdf`
- Modify: `docs/index.html`

**Interfaces:**
- Consumes: the public API from Tasks 1–7.
- Produces: a runnable demo showing a root pane, tabs, splits, an app-created floating Stage, cross-pane movement, and context snapshot/restore. The showcase entry links to the generated manual and launches the demo.

- [ ] **Step 1: Create a demo that builds the context and opens sample dockables.** Supply an ID-based factory and a Stage factory; demonstrate moving tabs between panes and restoring a saved `DockingLayout`.

- [ ] **Step 2: Run the demo entry point.**

Run: `mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.DockingPaneApp`

Expected: the demo opens with a populated root pane; dragging a title outside creates a second pane in a Stage at the pointer location.

- [ ] **Step 3: Add the manual source and showcase registry entry.** Follow `docs/manuals/src/README.md` and the chapter structure of an existing pane manual. Cover API anatomy, tab/split interactions, Stage factory, persistence, styling, accessibility, and a basic app recipe. Register the `docking-pane` manual under Layout with `DockingPaneApp`.

- [ ] **Step 4: Build and verify the manual and showcase resources.**

Run: `cd docs/manuals/src && .venv/bin/python build.py docking_pane`

Expected: `docs/manuals/docking-pane.pdf` is generated and the showcase can load the entry and PDF.

### Task 9: Final Integration Verification

**Files:**
- No new files; verify the changes from Tasks 1–8.

**Interfaces:**
- Consumes: the complete docking framework, demo, and manual.
- Produces: a passing full Maven verification and a clean manual build.

- [ ] **Step 1: Run the entire Maven verification.**

Run: `./mvnw -B verify`

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Rebuild the docking manual.**

Run: `cd docs/manuals/src && .venv/bin/python build.py docking_pane`

Expected: the docking manual builds without errors.

- [ ] **Step 3: Inspect the worktree and leave it uncommitted.**

Run: `git diff --check && git status --short`

Expected: no whitespace errors; all implementation changes remain in the working tree for the user to commit.

## Self-Review

- **Spec coverage:** Tasks 1–3 cover IDs, dockable factories, context/pane state, and tree invariants. Tasks 4–5 cover the custom title tabs, CSS, accessibility, selection, and in-pane docking. Task 6 covers the shared Stage factory, pointer positioning, cross-pane movement, close vetoes, and automatic closure of empty floating panes. Task 7 covers full-context snapshots, unresolved IDs, and transactional restore errors. Task 8 covers the demo, manual, and showcase registration. Task 9 verifies the integrated build.
- **Placeholder scan:** No TODO/TBD implementation steps; each test command includes the concrete target and expected result.
- **Type consistency:** `DockingContext`, `DockingPane`, `DockableFactory`, `DockingStageFactory`, `DockingLayout`, `DockingRestoreResult`, and `DockingPosition` are named consistently across tasks. The factory resolves stable IDs to `Optional<Dockable>`; the Stage factory accepts a `DockingPane` and returns its host `Stage`.
- **Commit policy:** The plan intentionally contains no commit steps because the user retains commit ownership.
