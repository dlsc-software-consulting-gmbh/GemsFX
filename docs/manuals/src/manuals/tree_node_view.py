"""Content of the TreeNodeView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "tree-node-view"

MANUAL = Manual(
    control='TreeNodeView',
    package='com.dlsc.gemsfx.treeview',
    subtitle='A layout control for visual tree diagrams',
    abstract='TreeNodeView renders a TreeNode model as a node-link diagram with configurable layout direction, regular or compact layout, cell factory, spacing, alignment and link strategies.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of TreeNodeView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>TreeNodeView</b> lives in <font face='Courier'>com.dlsc.gemsfx.treeview</font>. It does not use JavaFX TreeItem; instead, its model is <font face='Courier'>TreeNode</font>, which stores children, optional linked nodes, expansion state and optional per-node dimensions."),
            Section("Key features"),
            Bullets([
                'Supports TOP_TO_BOTTOM, BOTTOM_TO_TOP, LEFT_TO_RIGHT and RIGHT_TO_LEFT layout directions.',
                'Supports REGULAR and COMPACT layout algorithms.',
                'Default cells are TreeNodeCell instances with label and disclosure arrow.',
                'Links are pluggable through LinkStrategy implementations.',
                'TreeNode.name creates deterministic style classes for node links.'
            ]),
            Section("Maven dependency"),
            Code("""<dependency>
    <groupId>com.dlsc.gemsfx</groupId>
    <artifactId>gemsfx</artifactId>
    <version>4.4.1</version>
</dependency>""", caption="Use package <font face='Courier'>com.dlsc.gemsfx.treeview</font>."),
        ]),
        Chapter("Getting started", [
            Para("The snippet below uses only APIs verified in the source and demo code."),
            Code('TreeNode<String> root = new TreeNode<>("Root");\nroot.getChildren().addAll(new TreeNode<>("A"), new TreeNode<>("B"));\n\nTreeNodeView<String> view = new TreeNodeView<>(root);\nview.setLayoutDirection(TreeNodeView.LayoutDirection.TOP_TO_BOTTOM);\nview.setLayoutType(TreeNodeView.LayoutType.REGULAR);\nview.setLinkStrategy(new CurvedLineLink<>());', caption="Minimal setup for <font face='Courier'>TreeNodeView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['TreeNodeView', 'tree-node-view', 'Control root and layout properties.'],
                        ['TreeNode', 'model node', 'Value, children, linkedNodes, expanded, width, height and name.'],
                        ['TreeNodeCell', 'tree-node-cell', 'Default cell with label and disclosure arrow.'],
                        ['tree-content', 'Group', 'Skin content group containing cells and link nodes.'],
                        ['LinkStrategy', 'link-line/path/curve/arrow', 'Draws parent-child and extra links.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Tree and cells'),
            PropertyTable([
                        Property('root', 'ObjectProperty&lt;TreeNode&lt;T&gt;&gt;', 'null', 'Root node; null shows placeholder.'),
                        Property('cellFactory', 'ObjectProperty&lt;Callback&lt;T, TreeNodeCell&lt;T&gt;&gt;&gt;', 'TreeNodeCell::new', 'Creates cells for node values.'),
                        Property('placeholder', 'ObjectProperty&lt;Node&gt;', 'Label "No tree root."', 'Shown when root is null.'),
                        Property('refresh()', 'method', 'skin rebuild', 'Rebuilds visual tree from current model and properties.')
            ]),
            Section('Layout'),
            PropertyTable([
                        Property('cellWidth', 'DoubleProperty', '60', 'Default cell width; styleable.'),
                        Property('cellHeight', 'DoubleProperty', '30', 'Default cell height; styleable.'),
                        Property('hgap', 'DoubleProperty', '20', 'Horizontal gap; styleable.'),
                        Property('vgap', 'DoubleProperty', '50', 'Vertical gap; styleable.'),
                        Property('nodeLineGap', 'DoubleProperty', '10', 'Gap between cell and connecting lines; styleable.'),
                        Property('rowAlignment', 'ObjectProperty&lt;VPos&gt;', 'CENTER', 'Alignment for same-level nodes in vertical directions; styleable.'),
                        Property('columnAlignment', 'ObjectProperty&lt;HPos&gt;', 'CENTER', 'Alignment for same-level nodes in horizontal directions; styleable.'),
                        Property('layoutType', 'ObjectProperty&lt;LayoutType&gt;', 'REGULAR', 'REGULAR or COMPACT; styleable.'),
                        Property('layoutDirection', 'ObjectProperty&lt;LayoutDirection&gt;', 'TOP_TO_BOTTOM', 'Tree growth direction; styleable.')
            ]),
            Section('TreeNode'),
            PropertyTable([
                        Property('value', 'ObjectProperty&lt;T&gt;', 'null', 'User value.'),
                        Property('children', 'ObservableList&lt;TreeNode&lt;T&gt;&gt;', 'empty', 'Hierarchical child nodes; parent is maintained automatically.'),
                        Property('linkedNodes', 'ObservableList&lt;TreeNode&lt;T&gt;&gt;', 'empty', 'Additional non-hierarchical links.'),
                        Property('expanded', 'BooleanProperty', 'true', 'Controls visibility of descendants.'),
                        Property('width / height', 'DoubleProperty', 'USE_TREE_CELL_SIZE', 'Per-node dimensions; sentinel uses view defaults.'),
                        Property('name', 'String', 'null', 'Optional style identifier for node/link style classes.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Layout algorithms'),
            Para('REGULAR layout sizes each subtree from child totals. COMPACT layout lays out levels breadth-first using the widest row or tallest column, which can reduce space.'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Expansion and rebuilding'),
            Para('TreeNodeCell binds its expanded property bidirectionally to TreeNode.expanded. Changing expansion toggles descendant visibility and updates the tree.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Links and style names'),
            Para('The skin asks linkStrategy to draw parent-child links and extra linkedNodes. If names exist, links receive classes such as link-parent-child or link-extra-source-target.'),
        ]),
        Chapter("Styling", [
            Para("The style hooks below were verified in the control, skin and CSS sources."),
            Figure(f"{G}/styling.svg", "Style hooks and visual states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ['tree-node-view', 'Root control style class.'],
                        ['tree-content', 'Group containing cells and links.'],
                        ['tree-node-cell', 'Default cell.'],
                        ['tree-node-cell-label', 'Label inside the default cell.'],
                        ['arrow-wrapper disclosure-arrow', 'Disclosure arrow shown when children exist.'],
                        ['link-arrow link-line link-path link-curve link-circle', 'Nodes returned by link strategies.'],
                        ['link-X-Y / link-extra-X-Y', 'Generated when TreeNode names are present.']
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class", "Meaning"], [
                        ['ltr rtl ttb btt', 'Direction pseudo classes on TreeNodeView.'],
                        ['expanded collapsed', 'TreeNodeCell expansion state.']
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ['-fx-cell-width', 'Number', '60', 'Default cell width.'],
                        ['-fx-cell-height', 'Number', '30', 'Default cell height.'],
                        ['-fx-hgap', 'Number', '20', 'Horizontal gap.'],
                        ['-fx-vgap', 'Number', '50', 'Vertical gap.'],
                        ['-fx-node-line-gap', 'Number', '10', 'Gap between node and link.'],
                        ['-fx-row-alignment', 'VPos', 'CENTER', 'TOP, CENTER, BOTTOM or BASELINE.'],
                        ['-fx-column-alignment', 'HPos', 'CENTER', 'LEFT, CENTER or RIGHT.'],
                        ['-fx-layout-type', 'LayoutType', 'REGULAR', 'REGULAR or COMPACT.'],
                        ['-fx-layout-direction', 'LayoutDirection', 'TOP_TO_BOTTOM', 'LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM or BOTTOM_TO_TOP.']
            ], widths=[28,16,18,38]),
            Code('.tree-node-view {\n    -fx-layout-type: compact;\n    -fx-layout-direction: left-to-right;\n}\n.tree-node-view .link-line {\n    -fx-stroke: -fx-accent;\n}', caption="Example CSS."),
        ]),
        Chapter("Localization", [
            Para("The following keys are read via <font face='Courier'>ResourceBundleManager</font>."),
            Table(["Key", "English default"], [
                        ['placeholder.no-root', 'No tree root.']
            ], widths=[55,45]),
        ]),
        Chapter("Accessibility", [
            Para('TreeNodeView sets AccessibleRole.TREE_VIEW. The default TreeNodeCell does not set an explicit accessible role in its constructor.'),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Use compact left-to-right layout'),
            Code('view.setLayoutType(TreeNodeView.LayoutType.COMPACT);\nview.setLayoutDirection(TreeNodeView.LayoutDirection.LEFT_TO_RIGHT);'),
            Section('Per-node size'),
            Code('TreeNode<String> big = new TreeNode<>("Wide");\nbig.setSize(140, 50);'),
            Section('Add extra links'),
            Code('source.getLinkedNodes().add(target);\nsource.setName("source");\ntarget.setName("target");'),
            Section('Custom cell factory'),
            Code('view.setCellFactory(value -> {\n    TreeNodeCell<String> cell = new TreeNodeCell<>(value);\n    cell.getStyleClass().add("org-node");\n    return cell;\n});'),
            Section('Refresh after model-side changes'),
            Code('view.refresh();'),
            Section("Checklist"),
            Numbered(['Use TreeNode, not TreeItem.', 'Set TreeNode.name when you need stable link style classes.', 'Wrap the view in ScrollPane for large trees.', 'Choose rowAlignment only for vertical directions and columnAlignment for horizontal directions.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>TreeNodeViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.TreeNodeViewApp"),
            Bullets([
                'Related GemsFX controls: TreeNode, TreeNodeCell, link strategies, TreeView.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
