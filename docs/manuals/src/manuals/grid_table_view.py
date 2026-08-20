"""Content of the GridTableView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "grid-table-view"

MANUAL = Manual(
    control='GridTableView',
    package='com.dlsc.gemsfx.gridtable',
    subtitle='A lightweight GridPane-based table control',
    abstract='GridTableView renders rows and columns with GridPane rather than VirtualFlow, making it useful for compact tables with custom row headers, footers and loading overlays.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of GridTableView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>GridTableView</b> lives in <font face='Courier'>com.dlsc.gemsfx.gridtable</font>. Rows come from <font face='Courier'>items</font>, columns are <font face='Courier'>GridTableColumn</font> objects, and cells are <font face='Courier'>GridTableCell</font> instances created by each column."),
            Section("Key features"),
            Bullets([
                'GridPane layout with column headers and row backgrounds.',
                'Optional row header and row footer factories per item.',
                'Placeholder shown when columns exist but items is empty.',
                'LoadingPane overlay with status, size, progress indicator and commit delay.',
                'Double-click open callback and context menu callback for rows.'
            ]),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx.gridtable</font>."),
        ]),
        Chapter("Getting started", [
            Para("The snippet below uses only APIs verified in the source and demo code."),
            Code('GridTableView<Student> table = new GridTableView<>();\nGridTableColumn<Student, String> name = new GridTableColumn<>("Name");\nname.setCellValueFactory(new GridTablePropertyValueFactory<>("name"));\nGridTableColumn<Student, Integer> age = new GridTableColumn<>("Age");\nage.setCellValueFactory(new GridTablePropertyValueFactory<>("age"));\n\nname.setPercentWidth(70);\nage.setPercentWidth(30);\ntable.getColumns().setAll(name, age);\ntable.getItems().setAll(new Student("Tom", 12), new Student("Lucy", 11));', caption="Minimal setup for <font face='Courier'>GridTableView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['GridTableView', 'grid-table-view', 'Control root and stylesheet owner.'],
                        ['GridPane', 'grid-pane', 'Skin layout containing headers, body rows and LoadingPane.'],
                        ['GridTableColumn', 'ColumnConstraints', 'Header, width constraints, value factory and cell factory.'],
                        ['GridTableCell', 'grid-table-cell', 'Cell for one row/column value.'],
                        ['LoadingPane', 'loading-pane', 'Overlay shown while loadingStatus is not OK.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Rows and columns'),
            PropertyTable([
                        Property('items', 'ListProperty&lt;S&gt;', 'empty list', 'Rows displayed in the table body.'),
                        Property('columns', 'ListProperty&lt;GridTableColumn&lt;S, ?&gt;&gt;', 'empty list', 'Column definitions and GridPane column constraints.'),
                        Property('minNumberOfRows', 'IntegerProperty', '0', 'Minimum number of body rows; extra rows are empty.'),
                        Property('placeholder', 'ObjectProperty&lt;Node&gt;', 'Label "No items"', 'Shown when columns exist but items is empty.')
            ]),
            Section('Factories and callbacks'),
            PropertyTable([
                        Property('rowHeaderFactory', 'ObjectProperty&lt;Callback&lt;S, Node&gt;&gt;', 'null', 'Creates an optional row header above the cells for an item.'),
                        Property('rowFooterFactory', 'ObjectProperty&lt;Callback&lt;S, Node&gt;&gt;', 'null', 'Creates an optional row footer below the cells for an item.'),
                        Property('onOpenItem', 'ObjectProperty&lt;Consumer&lt;S&gt;&gt;', 'null', 'Called on primary double-click when set.'),
                        Property('onContextMenuForItemRequested', 'ObjectProperty&lt;Callback&lt;S, ContextMenu&gt;&gt;', 'null', 'Creates a context menu for a row item.')
            ]),
            Section('Loading'),
            PropertyTable([
                        Property('loadingStatus', 'ObjectProperty&lt;LoadingPane.Status&gt;', 'OK', 'Controls the LoadingPane overlay.'),
                        Property('loadingStatusSize', 'ObjectProperty&lt;LoadingPane.Size&gt;', 'MEDIUM', 'Size of the loading status indicator.'),
                        Property('progressIndicator', 'ObjectProperty&lt;ProgressIndicator&gt;', 'CircleProgressIndicator', 'Indicator used by the LoadingPane.'),
                        Property('commitLoadStatusDelay', 'LongProperty', '400', 'Delay in milliseconds before committed loading status is shown.')
            ]),
            Section('GridTableColumn and cell'),
            PropertyTable([
                        Property('text', 'StringProperty', '"Header" default for no-arg column', 'Header text.'),
                        Property('graphic', 'ObjectProperty&lt;Node&gt;', 'null', 'Header graphic.'),
                        Property('contentDisplay', 'ObjectProperty&lt;ContentDisplay&gt;', 'LEFT / constructor-specific', 'Header graphic/text placement.'),
                        Property('converter', 'ObjectProperty&lt;StringConverter&lt;T&gt;&gt;', 'SimpleStringConverter', 'Converts cell values to text.'),
                        Property('cellValueFactory', 'ObjectProperty&lt;Callback&lt;S,T&gt;&gt;', 'null', 'Extracts the cell value from a row item.'),
                        Property('cellFactory', 'ObjectProperty&lt;Callback&lt;GridTableView&lt;S&gt;, GridTableCell&lt;S,T&gt;&gt;&gt;', 'new GridTableCell', 'Creates cells.'),
                        Property('transparent', 'StyleableBooleanProperty', 'false', 'GridTableCell CSS bridge for mouseTransparent.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Rebuilding the grid'),
            Para('The skin clears and recreates the GridPane whenever items, columns, row header/footer factories or the refresh-items marker changes.'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Rows, headers and footers'),
            Para('Each item can produce a row header and footer. Row backgrounds span the cell row plus header/footer rows so striping remains continuous.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Cell creation'),
            Para('Each column creates a cell by reading the row item, applying its cellValueFactory, calling its cellFactory, setting rowItem/index/column and installing context-menu support.'),
        ]),
        Chapter("Styling", [
            Para("The style hooks below were verified in the control, skin and CSS sources."),
            Figure(f"{G}/styling.svg", "Style hooks and visual states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ['grid-table-view', 'Root control style class.'],
                        ['grid-pane', 'Skin GridPane.'],
                        ['column-header column-header-background', 'Header nodes and background.'],
                        ['row-background odd even first middle last only', 'Row background regions.'],
                        ['grid-table-cell first middle last row-first row-middle row-last col-index-N row-index-N', 'Cells created by columns.'],
                        ['loading-pane error-pane error-label progress-indicator-wrapper', 'LoadingPane overlay classes.']
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class", "Meaning"], [
                        ['focused', 'Root focus styling.'],
                        ['hover', 'Applied to row background while a cell is hovered.'],
                        ['odd / even', 'GridTableCell pseudo classes based on row index.']
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ['-fx-mouse-transparent', 'Boolean', 'false', 'Styleable property on GridTableCell only.']
            ], widths=[28,16,18,38]),
            Code('.grid-table-view > .grid-pane > .grid-table-cell {\n    -fx-padding: 8px;\n}\n.grid-table-view > .grid-pane > .grid-table-cell:even {\n    -fx-background-color: -fx-control-inner-background-alt;\n}', caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("The following keys are read via <font face='Courier'>ResourceBundleManager</font>."),
            Table(["Key", "English default"], [
                        ['placeholder.no-items', 'No items']
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para('GridTableView sets AccessibleRole.TABLE_VIEW and requests focus on primary single-click.'),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Add row headers'),
            Code('table.setRowHeaderFactory(student -> new Label("Header for " + student.getName()));'),
            Section('Show loading overlay'),
            Code('table.setLoadingStatus(LoadingPane.Status.LOADING);\ntable.setCommitLoadStatusDelay(400);'),
            Section('Open on double click'),
            Code('table.setOnOpenItem(student -> openEditor(student));'),
            Section('Context menu per row'),
            Code('table.setOnContextMenuForItemRequested(student -> new ContextMenu(new MenuItem("Edit")));'),
            Section('Custom cell'),
            Code('ageColumn.setCellFactory(view -> new GridTableCell<>());'),
            Section("Checklist"),
            Numbered(['Create columns before expecting placeholder/body rows.', 'Use minNumberOfRows for fixed-height compact tables.', 'Call refresh() after changes that do not update observable lists.', 'GridTablePropertyValueFactory reads fields by reflection, not JavaFX properties.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>GridTableViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.GridTableViewApp"),
            Bullets([
                'Related GemsFX controls: AdvancedTableView, MultiColumnListView, TableView.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
