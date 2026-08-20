"""Content of the MultiColumnListView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "multi-column-list-view"

MANUAL = Manual(
    control='MultiColumnListView',
    package='com.dlsc.gemsfx',
    subtitle='A multi-column drag-and-drop list board',
    abstract='MultiColumnListView displays several ListView columns with headers, separators, drag-and-drop markers, a placeholder and a LoadingPane overlay.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of MultiColumnListView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>MultiColumnListView</b> is a board-style control. Each <font face='Courier'>ListViewColumn</font> owns user items and internal <font face='Courier'>ColumnItem</font> wrappers so the control can insert From/To placeholders during drag and drop."),
            Section("Key features"),
            Bullets([
                'Columns contain a header node, item list and userObject.',
                'Default list view factory creates AutoscrollListView instances.',
                'Drag and drop can be disabled or filtered through callbacks.',
                'Events report drag start, drag-over, veto and item moved steps.',
                'LoadingPane and placeholder support empty/loading states.'
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
            Code('MultiColumnListView<String> board = new MultiColumnListView<>();\nListViewColumn<String> todo = new ListViewColumn<>();\ntodo.setHeader(new Label("To Do"));\ntodo.getItems().setAll("Write docs", "Review PR");\nListViewColumn<String> done = new ListViewColumn<>();\ndone.setHeader(new Label("Done"));\n\nboard.getColumns().setAll(todo, done);\nboard.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, evt -> {\n    System.out.println(evt.getDraggedItem() + " moved");\n});', caption="Minimal setup for <font face='Courier'>MultiColumnListView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['MultiColumnListView', 'multi-column-list-view', 'Root control and API owner.'],
                        ['LoadingPane', 'loading-pane', 'Wraps the grid and shows loading/error states.'],
                        ['GridPane', 'grid-pane', 'One list column per ListViewColumn, plus optional separators.'],
                        ['ListViewColumn', 'model object', 'Header, user items, wrappers and userObject.'],
                        ['ColumnListCell', 'column-list-cell', 'ListCell with drag-and-drop placeholder handling.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Columns and rendering'),
            PropertyTable([
                        Property('columns', 'ListProperty&lt;ListViewColumn&lt;T&gt;&gt;', 'empty list', 'Columns displayed by the view.'),
                        Property('showHeaders', 'BooleanProperty', 'true', 'Shows each column header; styleable.'),
                        Property('separatorFactory', 'ObjectProperty&lt;Callback&lt;Integer, Node&gt;&gt;', 'column-separator Region', 'Creates separator nodes between columns; null disables separators.'),
                        Property('listViewFactory', 'ObjectProperty&lt;Callback&lt;MultiColumnListView&lt;T&gt;, ListView&lt;ColumnItem&lt;T&gt;&gt;&gt;', 'new AutoscrollListView', 'Creates the ListView for each column.'),
                        Property('cellFactory', 'ObjectProperty&lt;Callback&lt;MultiColumnListView&lt;T&gt;, ColumnListCell&lt;T&gt;&gt;&gt;', 'ColumnListCell::new', 'Creates list cells.')
            ]),
            Section('Drag and drop'),
            PropertyTable([
                        Property('disableDragAndDrop', 'BooleanProperty', 'false', 'Disables dragging; styleable.'),
                        Property('dragPossibleCallback', 'ObjectProperty&lt;Callback&lt;T, Boolean&gt;&gt;', 'item -> true', 'Allows or rejects drag start for an item.'),
                        Property('dropPossibleCallback', 'ObjectProperty&lt;Callback&lt;DropParameter&lt;T&gt;, Boolean&gt;&gt;', 'param -> true', 'Allows or rejects a drop target.'),
                        Property('draggedItem', 'ObjectProperty&lt;T&gt;', 'null', 'Currently dragged user object.'),
                        Property('draggedItems', 'ObservableList&lt;T&gt;', 'empty', 'Selected user objects involved in the drag.'),
                        Property('fromPlaceholder / toPlaceholder', 'ColumnItem&lt;T&gt;', 'internal singletons', 'Marker wrappers inserted during drag operations.')
            ]),
            Section('Loading and placeholder'),
            PropertyTable([
                        Property('placeholder', 'ObjectProperty&lt;Node&gt;', 'Label "No columns defined."', 'Shown when there are no columns and loadingStatus is OK.'),
                        Property('loadingStatus', 'ObjectProperty&lt;LoadingPane.Status&gt;', 'OK', 'Controls the LoadingPane.'),
                        Property('loadingStatusSize', 'ObjectProperty&lt;LoadingPane.Size&gt;', 'MEDIUM', 'Loading indicator size.'),
                        Property('progressIndicator', 'ObjectProperty&lt;ProgressIndicator&gt;', 'CircleProgressIndicator', 'Indicator used by LoadingPane.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Building columns'),
            Para('The skin creates equal-width GridPane columns, optional header row, ListView instances and optional separators. Empty columns get a placeholder label that accepts valid drops.'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Drag and drop markers'),
            Para('When a drag starts, the dragged wrapper is replaced by the from placeholder. While hovering, the to placeholder is inserted above or below the candidate cell.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Events and vetoes'),
            Para('The control fires DRAG_STARTED, DRAG_NOT_POSSIBLE, DRAG_OVER, DROP_NOT_POSSIBLE and ITEM_MOVED with the dragged item, column and index.'),
        ]),
        Chapter("Styling", [
            Para("The style hooks below were verified in the control, skin and CSS sources."),
            Figure(f"{G}/styling.svg", "Style hooks and visual states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ['multi-column-list-view', 'Root style class.'],
                        ['placeholder', 'Root placeholder and list placeholder.'],
                        ['loading-pane grid-pane', 'Loading wrapper and grid.'],
                        ['column-background-region column-foreground-region', 'Per-column overlay regions.'],
                        ['column-separator', 'Default separator node.'],
                        ['column-list-cell content-pane content-label', 'Default cell visuals.'],
                        ['from to', 'Pseudo states on ColumnListCell placeholders.'],
                        ['drag-over', 'Pseudo state on a column placeholder.']
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class", "Meaning"], [
                        ['from', 'ColumnListCell shows the source marker.'],
                        ['to', 'ColumnListCell shows the target marker.'],
                        ['drag-over', 'ListView placeholder is a valid drag target.'],
                        ['hover', 'Column foreground follows ListView hover/drag hover.']
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ['-fx-show-headers', 'Boolean', 'true', 'Shows column headers.'],
                        ['-fx-disable-drag-and-drop', 'Boolean', 'false', 'Disables user drag-and-drop editing.']
            ], widths=[28,16,18,38]),
            Code('.multi-column-list-view {\n    -fx-show-headers: true;\n    -fx-disable-drag-and-drop: false;\n}\n.multi-column-list-view .column-list-cell:to > .content-pane {\n    -fx-border-color: -fx-accent;\n}', caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("The following keys are read via <font face='Courier'>ResourceBundleManager</font>."),
            Table(["Key", "English default"], [
                        ['column.header.default', 'Column Header'],
                        ['placeholder.from', 'From'],
                        ['placeholder.to', 'To'],
                        ['placeholder.no-columns', 'No columns defined.']
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para('The constructor sets AccessibleRole.LIST_VIEW and sets focusTraversable to false.'),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Disable editing'),
            Code('board.setDisableDragAndDrop(true);'),
            Section('Restrict drops'),
            Code('board.setDropPossibleCallback(param -> param.getColumn().getItems().size() < 10);'),
            Section('Custom list views'),
            Code('board.setListViewFactory(view -> new AutoscrollListView<>());'),
            Section('Hide headers'),
            Code('board.setShowHeaders(false);'),
            Section('Listen for moves'),
            Code('board.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, evt -> saveBoard());'),
            Section("Checklist"),
            Numbered(['Use ListViewColumn.getItems(), not itemWrappers, for application data.', 'Override ColumnListCell.updateUserObject rather than updateItem.', 'Return false from callbacks to veto drags or drops.', 'The old PDF is intentionally replaced by this generated manual.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>MultiColumnListViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.MultiColumnListViewApp"),
            Bullets([
                'Related GemsFX controls: AutoscrollListView, GridTableView, SelectionBox.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
