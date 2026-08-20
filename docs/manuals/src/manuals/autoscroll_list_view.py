"""Content of the AutoscrollListView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "autoscroll-list-view"

MANUAL = Manual(
    control='AutoscrollListView',
    package='com.dlsc.gemsfx',
    subtitle='A ListView that scrolls automatically during drag and drop',
    abstract='AutoscrollListView extends JavaFX ListView and starts a daemon scroll loop when a drag operation reaches the top or bottom hot zone of the visible list area.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of AutoscrollListView.',
    chapters=[
        Chapter("Introduction", [
            Para('<b>AutoscrollListView</b> is a vertical ListView specialization for drag-and-drop UIs. It accepts transfer modes during drag-over, computes a vertical scroll delta near the clipped container edges, and scrolls the VirtualFlow until the drag leaves, drops or finishes.'),
            Section("Key features"),
            Bullets([
                'Constructors mirror ListView: empty or with an ObservableList of items.',
                'Uses a 20-pixel proximity hot zone at the top and bottom.',
                'Accepts TransferMode.ANY during drag-over.',
                'Starts a daemon ScrollThread with an initial 300 ms delay.',
                'Stops scrolling on DRAG_EXITED, DRAG_DROPPED and DRAG_DONE.'
            ]),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx</font>."),
        ]),
        Chapter("Getting started", [
            Para("The snippet below uses only APIs verified in the source and demo code."),
            Code('AutoscrollListView<String> list = new AutoscrollListView<>();\nlist.getItems().setAll("One", "Two", "Three", "Four", "Five");\n\nlist.setCellFactory(view -> {\n    ListCell<String> cell = new ListCell<>() {\n        @Override protected void updateItem(String item, boolean empty) {\n            super.updateItem(item, empty);\n            setText(empty ? null : item);\n        }\n    };\n    // install your drag source / drop target logic here\n    return cell;\n});', caption="Minimal setup for <font face='Courier'>AutoscrollListView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['AutoscrollListView', 'ListView subclass', 'Owns the items and standard ListView selection model.'],
                        ['DRAG_OVER filter', 'Event filter', 'Accepts transfer modes and computes scroll direction.'],
                        ['VirtualFlow', 'skin internals', 'Scrolled by pixel deltas from the background thread.'],
                        ['clipped-container', 'Region', 'Preferred hot-region for detecting top and bottom proximity.'],
                        ['ScrollThread', 'daemon Thread', 'Posts Platform.runLater scrollPixels calls every 15 ms.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Constructors'),
            PropertyTable([
                        Property('AutoscrollListView()', 'constructor', 'empty observable list', 'Creates a list with FXCollections.observableArrayList().'),
                        Property('AutoscrollListView(ObservableList&lt;T&gt;)', 'constructor', 'items from argument', 'Creates a list with the provided items.')
            ]),
            Section('Implementation constants'),
            PropertyTable([
                        Property('proximity', 'double field', '20', 'Distance in pixels from top or bottom that triggers autoscroll.'),
                        Property('scrollThread', 'ScrollThread field', 'null', 'Created lazily while a drag is inside a hot zone and cleared on drag end.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Hot-zone scrolling'),
            Para('During DRAG_OVER the control finds the clipped container of the VirtualFlow. If the cursor is within 20 pixels of the top, it scrolls up; if it is within 20 pixels of the bottom, it scrolls down.'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Thread lifecycle'),
            Para('The ScrollThread sleeps 300 ms before its first scroll, then posts scrollPixels(yOffset) about every 15 ms. It is stopped by drag exit, drop and done events.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Vertical-only implementation'),
            Para('The source explicitly documents vertical orientation only. There is no horizontal autoscroll path.'),
        ]),
        Chapter("Styling", [
            Para("The style hooks below were verified in the control, skin and CSS sources."),
            Figure(f"{G}/styling.svg", "Style hooks and visual states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ["none", "No dedicated GemsFX stylesheet or style classes were found."]
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class", "Meaning"], [
                        ["none", "No pseudo classes are set by this control."]
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ["none", "", "", "No styleable CSS properties are declared by this control."]
            ], widths=[28,16,18,38]),
            Code('/* AutoscrollListView inherits normal ListView styling. */\n.autoscroll-list-view .list-cell {\n    -fx-padding: 0.5em;\n}', caption="Example CSS."),
        ]),
        Chapter("Implementation notes", [
            Section("VirtualFlow lookup"),
            Para("The implementation uses <font face='Courier'>lookup(\"VirtualFlow\")</font> and then scans the flow children for the <font face='Courier'>clipped-container</font> style class. This is why the control is tied to the standard JavaFX ListView skin structure."),
            Section("Empty-list fallback"),
            Para("If the clipped container has no width, the code falls back to the list view itself. If that fallback still has no width, autoscrolling is stopped for the current drag event."),
            Section("Scroll delta"),
            Code("""double delta = evt.getSceneY() - hotRegion.localToScene(0, 0).getY();
if (delta < proximity) {
    yOffset = -(proximity - delta);
}

delta = hotRegion.localToScene(0, 0).getY() + hotRegion.getHeight() - evt.getSceneY();
if (delta < proximity) {
    yOffset = proximity - delta;
}""", caption="The signed delta is larger the deeper the cursor is inside a hot zone."),
            Callout("Because the scroll loop is an implementation detail, applications should not try to start or stop it directly. Use normal drag events; the control stops the daemon thread when the drag exits, drops or finishes.", kind="tip"),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Use as a kanban column list'),
            Code('AutoscrollListView<Task> list = new AutoscrollListView<>();\nlist.setCellFactory(view -> new TaskCell());'),
            Section('Provide items in the constructor'),
            Code('ObservableList<String> items = FXCollections.observableArrayList("A", "B", "C");\nAutoscrollListView<String> list = new AutoscrollListView<>(items);'),
            Section('Stop work on drag completion'),
            Code('list.addEventHandler(DragEvent.DRAG_DONE, evt -> {\n    // AutoscrollListView stops its scroll thread automatically.\n});'),
            Section('Combine with MultiColumnListView'),
            Code('multiColumnListView.setListViewFactory(view -> new AutoscrollListView<>());'),
            Section("Checklist"),
            Numbered(['Use it for vertical ListView drag-and-drop scenarios.', 'Install your own drag source/drop target logic; this class only handles edge scrolling.', 'Do not rely on a public proximity property; it is a fixed field.', 'No dedicated CSS, bundle or accessibility setup exists.']),
        ]),
        Chapter("See also", [
            Para("No dedicated demo app was found in <font face='Courier'>gemsfx-demo</font> for this control."),
            Bullets([
                'Related GemsFX controls: MultiColumnListView, ListView.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
