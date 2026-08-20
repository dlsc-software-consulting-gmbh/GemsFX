"""Content of the StretchingTilePane developer manual."""
from manualkit import Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para, Property, PropertyTable, Section, Table

G = 'stretching-tile-pane'

MANUAL = Manual(
    control='StretchingTilePane',
    package='com.dlsc.gemsfx',
    subtitle='A tile grid that stretches every row to the full width',
    abstract='StretchingTilePane lays out managed Region children in rows and stretches every tile in a row to the same width so the row fills the pane.',
    cover_svg=f'{G}/cover.svg',
    cover_caption='A generated cartoon overview of StretchingTilePane.',
    chapters=[
        Chapter('Introduction', [
                Para('<b>StretchingTilePane</b> arranges managed Region children in rows. It computes how many preferred-width tiles fit, subtracts gaps and stretches each tile so the row fills all available width.'),
                Section('Key features'),
                Bullets(['Horizontal content bias: preferred height depends on width.', 'Column count comes from maximum preferred tile width plus hgap.', 'Tiles share one stretched width and the maximum preferred height.', 'Managed children only participate.', 'hgap and vgap are styleable CSS properties.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('StretchingTilePane pane = new StretchingTilePane(10, 10);\npane.setPadding(new Insets(20));\nfor (int i = 0; i < 12; i++) {\n    Label tile = new Label("Tile " + (i + 1));\n    tile.setPrefSize(150, 100);\n    pane.getChildren().add(tile);\n}', caption='A row-stretching tile grid.'),
                Figure(f'{G}/states.svg', 'Column count changes with available width.')
            ]),
        Chapter('Anatomy', [
                Figure(f'{G}/anatomy.svg', 'Insets, gaps and managed tile children.'),
                PropertyTable([Property('hgap', 'DoubleProperty', '0', 'Horizontal gap; styleable with -fx-hgap.'), Property('vgap', 'DoubleProperty', '0', 'Vertical gap; styleable with -fx-vgap.'), Property('contentBias', 'Orientation', 'HORIZONTAL', 'Reports horizontal content bias.')])
            ]),
        Chapter('Column geometry', [
                Figure(f'{G}/layout.svg', 'The row width is divided after gaps are subtracted.'),
                Para("The pane finds maximum preferred tile width, then computes <font face='Courier'>columnCount = (int) (contentWidth / (preferredTileWidth + hgap))</font>. If the count is zero, layout returns without placing children."),
                Code('availableWidth = contentWidth - (columnCount - 1) * hgap\ntileWidth = availableWidth / columnCount\ntileHeight = max(child.prefHeight(contentWidth))')
            ]),
        Chapter('Preferred height', [
                Figure(f'{G}/interaction.svg', 'Layout is recomputed when width changes.'),
                Para('Preferred height is rows times maximum preferred tile height plus vertical gaps and insets. Minimum height delegates to preferred height.'),
                Table(['Input', 'Effect'], [['Wider width', 'May increase columns and reduce rows.'], ['Larger hgap', 'Can reduce column count.'], ['Larger vgap', 'Increases preferred height between rows.']], widths=[30, 70])
            ]),
        Chapter('Styling', [
                Para('No user agent stylesheet or root style class is added, but hgap and vgap are styleable.'),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-hgap</font>", 'number', '0'], ["<font face='Courier'>-fx-vgap</font>", 'number', '0']], widths=[45, 25, 30]),
                Code('.stretching-tile-pane {\n    -fx-hgap: 12;\n    -fx-vgap: 12;\n}')
            ]),
        Chapter('Managed children and Region assumptions', [
                Para("The layout loop uses <font face='Courier'>getManagedChildren()</font>. Unmanaged children remain in the scene graph but are ignored by column count, preferred height and placement."),
                Para("The preferred tile height computation casts each managed child to <font face='Courier'>Region</font>. In normal use tiles are Labels, panes or controls, all of which are Region subclasses. Wrap non-Region nodes before adding them."),
                Table(['Child kind', 'Recommendation'], [['Label / Button / Control', 'Safe: they are Region subclasses.'], ['Pane / StackPane / VBox', 'Safe and useful for card tiles.'], ['Canvas / Group / Shape', 'Wrap in StackPane before adding.'], ['Temporarily hidden item', 'Set managed false as well as visible false.']], widths=[34, 66]),
                Callout('A managed non-Region child can fail during preferred-height computation because of the source cast.', kind='warning')
            ]),
        Chapter('Edge cases and zero columns', [
                Para("When available width is smaller than one preferred tile width plus hgap, <font face='Courier'>columnCount</font> becomes zero. Both preferred-height computation and layout return zero / do nothing for that pass."),
                Table(['Case', 'Source behaviour'], [['No managed children', 'Preferred height is 0 and layout returns.'], ['columnCount == 0', 'Preferred height is 0 and layout returns.'], ['Negative hgap or vgap', 'Accepted by the property; the source does not clamp it.'], ['Insets larger than width', 'contentWidth can become small or negative, leading to zero columns.']], widths=[34, 66]),
                Para('For predictable layout, keep gaps non-negative and choose tile preferred widths that allow at least one column in the pane sizes your application supports.')
            ]),
        Chapter('Comparing with TilePane', [
                Para('JavaFX TilePane keeps tiles at their tile width and may leave unused space at the end of a row. StretchingTilePane first decides how many columns fit, then divides the whole row width by that count. This makes dashboards and card grids align cleanly with the container edge.'),
                Table(['Aspect', 'TilePane', 'StretchingTilePane'], [['Row end', 'May leave unused remainder', 'Remainder is distributed into tile widths'], ['Tile size', 'Configured tile width / height', 'Computed equal width and max preferred height'], ['Preferred height', 'Depends on tile settings', 'Depends on current width due horizontal content bias']], widths=[25, 35, 40]),
                Code('''// TilePane-like fixed cards are not the goal; set pref size
// and let StretchingTilePane stretch the row.
tile.setPrefSize(150, 100);''')
            ]),
        Chapter('Card grid design patterns', [
                Para('A row-stretching grid is ideal for dashboards, settings cards and launchers where columns should align to the pane edges. Give every card a consistent preferred size, and put flexible content inside each card rather than varying the tile nodes wildly.'),
                Table(['Concern', 'Recommended practice'], [['Preferred sizes', 'Set explicit preferred sizes for important children so the layout maths has stable inputs.'], ['Insets and padding', 'Remember that pane insets are part of the available area calculation or child placement.'], ['Managed state', 'Only managed children should be expected to participate in layout decisions.'], ['Runtime changes', 'Property invalidation calls requestLayout, so batch related changes where possible.']], widths=[34, 66]),
                Para('The pane uses the maximum preferred tile width to choose columns. One unusually wide card therefore reduces the column count for the whole grid. Keep preferred widths consistent for predictable responsive behaviour.'),
                Code('''for (Node card : pane.getChildren()) {
    ((Region) card).setPrefSize(180, 120);
}
pane.setHgap(12);
pane.setVgap(12);''')
            ]),
        Chapter('Height-for-width integration', [
                Para("Because <font face='Courier'>getContentBias()</font> returns <font face='Courier'>Orientation.HORIZONTAL</font>, parent layouts should ask for preferred height using the current width. This is important in scroll panes and resizable dashboards."),
                Table(['Parent', 'Guidance'], [['ScrollPane', 'Let the pane compute preferred height for the viewport width.'], ['VBox', 'Give the tile pane a width through fillWidth or max width settings.'], ['BorderPane center', 'The center width drives the number of rows.'], ['Fixed-size parent', 'Choose preferred tile width so at least one column fits.']], widths=[34, 66]),
                Para('If the parent asks with an invalid or very small width, the source can report zero preferred height because no columns fit. This is expected from the implementation.')
            ]),
        Chapter('Responsive dashboard recipe', [
                Para('For dashboards, place the StretchingTilePane in a ScrollPane and let width changes drive the number of rows. The pane computes preferred height from width, so the scroll pane can scroll vertically without horizontal scrolling.'),
                Table(['Step', 'Reason'], [['Set tile preferred size', 'Defines the column-count baseline.'], ['Set hgap and vgap', 'Creates stable gutters between cards.'], ['Wrap in ScrollPane', 'Lets total height exceed viewport height.'], ['Keep cards managed', 'Only managed cards participate in the grid.']], widths=[28, 72]),
                Code('''ScrollPane scroll = new ScrollPane(pane);
scroll.setFitToWidth(true);
pane.setHgap(12);
pane.setVgap(12);''')
            ]),
        Chapter('Common mistakes', [
                Para('Most layout surprises come from inconsistent preferred sizes. Since the maximum preferred tile width controls the column count, one oversized child can turn a four-column grid into a three-column grid for every row.'),
                Bullets(['Avoid mixing very wide and very narrow tile preferred widths.', 'Avoid negative gaps even though the properties do not reject them.', 'Do not add managed non-Region nodes directly.', 'Do not expect individual rows to choose different column counts.']),
                Para('When a design needs masonry-style columns or variable-height cards, use a different layout. StretchingTilePane deliberately keeps rows regular and predictable.'),
                Code('''StackPane wrapper = new StackPane(shapeNode);
wrapper.setPrefSize(160, 100);
pane.getChildren().add(wrapper);''')
            ]),
        Chapter('Recipes', [
                Section('Ignore a child'),
                Code('node.setManaged(false);\nnode.setVisible(false);'),
                Section('Checklist'),
                Numbered(['Use Region children for preferred-height calculation.', 'Set meaningful preferred tile sizes.', 'Use padding for outer margins.', 'Expect equal tile sizes per layout pass.'])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.StretchingTilePaneApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.StretchingTilePaneApp</font>)", "JavaFX <font face='Courier'>TilePane</font> - standard non-stretching alternative.", "<font face='Courier'>ResponsivePane</font> - another width-sensitive layout.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
