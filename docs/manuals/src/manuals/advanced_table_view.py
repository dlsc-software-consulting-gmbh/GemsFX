"""Content of the AdvancedTableView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "advanced-table-view"

MANUAL = Manual(
    control='AdvancedTableView',
    package='com.dlsc.gemsfx',
    subtitle='A TableView variant with programmatic column auto-sizing',
    abstract='AdvancedTableView is a small extension of JavaFX TableView that installs a custom header stack and exposes an API for resizing all columns to fit their content.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of AdvancedTableView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>AdvancedTableView</b> extends <font face='Courier'>TableView</font>. It keeps the standard JavaFX table API and skin behaviour, but replaces the header row classes so every leaf header is an <font face='Courier'>AdvancedTableColumnHeader</font>. The only public GemsFX addition is <font face='Courier'>autoResizeAllColumns(...)</font>."),
            Section("Key features"),
            Bullets([
                'Constructors mirror TableView: empty or with an ObservableList of items.',
                'Uses AdvancedTableViewSkin, AdvancedTableHeaderRow and AdvancedNestedTableColumnHeader.',
                'autoResizeAllColumns() measures the first 100 rows.',
                'autoResizeAllColumns(int rows) ignores non-positive row counts.',
                'If called before a skin exists, the resize request is deferred until skin creation.'
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
            Code('AdvancedTableView<Person> table = new AdvancedTableView<>();\nTableColumn<Person, String> first = new TableColumn<>("First");\nfirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));\nTableColumn<Person, String> last = new TableColumn<>("Last");\nlast.setCellValueFactory(new PropertyValueFactory<>("lastName"));\ntable.getColumns().setAll(first, last);\ntable.getItems().setAll(people);\n\n// after items / columns are available\ntable.autoResizeAllColumns();', caption="Minimal setup for <font face='Courier'>AdvancedTableView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['AdvancedTableView', 'TableView subclass', 'Keeps the standard TableView item, column, selection and sorting APIs.'],
                        ['AdvancedTableViewSkin', 'TableViewSkin', 'Installs the advanced table header row.'],
                        ['AdvancedTableHeaderRow', 'TableHeaderRow', 'Creates the advanced root nested header.'],
                        ['AdvancedNestedTableColumnHeader', 'NestedTableColumnHeader', 'Creates nested or leaf advanced column headers.'],
                        ['AdvancedTableColumnHeader', 'TableColumnHeader', 'Exposes resizeColumnToFitContent for each leaf column.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Constructors and methods'),
            PropertyTable([
                        Property('AdvancedTableView()', 'constructor', 'empty TableView', 'Creates an empty table and installs deferred auto-resize support.'),
                        Property('AdvancedTableView(ObservableList&lt;T&gt;)', 'constructor', 'items from argument', 'Creates a table with initial items.'),
                        Property('autoResizeAllColumns()', 'void', '100 rows', 'Resizes all leaf columns by measuring content in the first 100 rows.'),
                        Property('autoResizeAllColumns(int rows)', 'void', 'caller supplied', 'Resizes all leaf columns using the given row limit; rows <= 0 do nothing.')
            ]),
            Section('Deferred state'),
            PropertyTable([
                        Property('autoResizeAllColumns', 'boolean field', 'false', 'Internal flag storing whether a resize call happened before skin creation.'),
                        Property('autoResizeRows', 'int field', '0', 'Internal row count used by the deferred resize path.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Column auto-sizing'),
            Para('The resize method retrieves the skin root header and recursively walks nested headers. Leaf AdvancedTableColumnHeader instances delegate to JavaFX resizeColumnToFitContent(rows).'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Skin timing'),
            Para('If the skin is null, the control records that all columns should be resized later. A listener on skinProperty performs the resize once a skin is available.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Standard TableView behaviour'),
            Para('Sorting, selection, columns, row factories and cell factories remain the standard JavaFX TableView APIs; GemsFX does not replace them.'),
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
            Code('/* AdvancedTableView uses the standard JavaFX TableView stylesheet hooks. */\n.advanced-table-view .column-header {\n    -fx-font-weight: bold;\n}', caption="Example CSS."),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Resize after loading data'),
            Code('table.getItems().setAll(loadPeople());\ntable.autoResizeAllColumns(200);'),
            Section('Resize after adding columns'),
            Code('table.getColumns().setAll(firstNameColumn, lastNameColumn, emailColumn);\ntable.autoResizeAllColumns();'),
            Section('Ignore tiny samples'),
            Code('// rows <= 0 is intentionally ignored\ntable.autoResizeAllColumns(0);'),
            Section('Use normal TableView APIs'),
            Code('table.getSelectionModel().selectedItemProperty().addListener((obs, old, person) -> {\n    System.out.println(person);\n});'),
            Section("Checklist"),
            Numbered(['Populate columns before calling autoResizeAllColumns.', 'Pass a row count large enough to represent typical content.', 'Use ordinary TableView CSS and APIs for everything except auto-resizing.', 'There is no GemsFX-specific CSS file, resource bundle or accessibility setup.']),
        ]),
        Chapter("See also", [
            Para("No dedicated demo app was found in <font face='Courier'>gemsfx-demo</font> for this control."),
            Bullets([
                'Related GemsFX controls: GridTableView, TableView, MultiColumnListView.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
