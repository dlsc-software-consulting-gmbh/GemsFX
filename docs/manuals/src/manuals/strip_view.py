"""Content of the StripView developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "strip-view"

MANUAL = Manual(
    control='StripView',
    package='com.dlsc.gemsfx',
    subtitle='A horizontally scrolling strip of selectable cells',
    abstract='StripView lays out a fixed sequence of items horizontally, fades the edges through MaskedView and shows arrow buttons when content overflows.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='Generated cartoon overview of StripView.',
    chapters=[
        Chapter("Introduction", [
            Para("<b>StripView</b> is a compact horizontal selector. The skin creates one <font face='Courier'>StripCell</font> per item, places them in an HBox inside a MaskedView, and scrolls the container with mouse, keyboard or explicit scrollTo calls."),
            Section("Key features"),
            Bullets([
                'Default preferred size is 400 x 50.',
                'Default cell factory creates StripCell labels.',
                'Selected item can auto-scroll into view and optionally stay centered.',
                'Scroll buttons fade in when content is hidden to the left or right.',
                'Keyboard selection supports LEFT, RIGHT, ENTER and TAB.'
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
            Code('StripView<String> strip = new StripView<>();\nstrip.getItems().setAll("Item 1", "Item 2", "Item 3", "Item 4");\nstrip.setSelectedItem("Item 2");\nstrip.selectedItemProperty().addListener((obs, oldItem, newItem) -> {\n    System.out.println("selected = " + newItem);\n});', caption="Minimal setup for <font face='Courier'>StripView</font>."),
            Figure(f"{G}/cover.svg", "A first look at the control."),
        ]),
        Chapter("Anatomy", [
            Para("The diagram and table identify the nodes, model objects and style classes that matter when using or styling the control."),
            Figure(f"{G}/anatomy.svg", "The main parts of the control."),
            Table(["Part", "Type / style", "Description"], [
                        ['StripView', 'strip-view', 'Root control and stylesheet owner.'],
                        ['MaskedView', 'masked-view', 'Fades content at left/right edges.'],
                        ['HBox', 'container', 'Holds cells horizontally.'],
                        ['StripCell', 'strip-cell', 'Default selectable Label cell.'],
                        ['Scroller buttons', 'scroller left / right', 'Arrow regions shown when overflow exists.']
            ], widths=[20,30,50]),
        ]),
        Chapter("Control API", [
            Section('Items and selection'),
            PropertyTable([
                        Property('items', 'ListProperty&lt;T&gt;', 'empty list', 'Model items shown in order.'),
                        Property('selectedItem', 'ObjectProperty&lt;T&gt;', 'null', 'Currently selected item.'),
                        Property('cellFactory', 'ObjectProperty&lt;Callback&lt;StripView&lt;T&gt;, StripCell&lt;T&gt;&gt;&gt;', 'new StripCell', 'Creates one cell per item.'),
                        Property('autoScrolling', 'BooleanProperty', 'true', 'Automatically calls scrollTo(selectedItem) when selection changes.')
            ]),
            Section('Scrolling and layout'),
            PropertyTable([
                        Property('alwaysCenter', 'BooleanProperty', 'true', 'Selected item is centered if possible; styleable.'),
                        Property('fadingSize', 'DoubleProperty', '120', 'Fade width on both sides; styleable.'),
                        Property('animateScrolling', 'BooleanProperty', 'true', 'Animates scroll and button fades; styleable.'),
                        Property('animationDuration', 'ObjectProperty&lt;Duration&gt;', '200 ms', 'Duration for scroll-to-item animation; styleable.'),
                        Property('loopSelection', 'BooleanProperty', 'true', 'Keyboard selection wraps around; styleable.'),
                        Property('scrollTo(T item)', 'method', 'property marker', 'Requests skin scrolling via the scroll.to property marker.')
            ]),
            Section('StripCell'),
            PropertyTable([
                        Property('stripView', 'ObjectProperty&lt;StripView&lt;T&gt;&gt;', 'set by skin', 'Back-reference to owning strip.'),
                        Property('item', 'ObjectProperty&lt;T&gt;', 'set by skin', 'Cell item.'),
                        Property('selected', 'BooleanProperty', 'false', 'Updates selected pseudo class and accessible selected attribute.')
            ]),
        ]),
        Chapter("Behaviour", [
            Section('Selection and scrolling'),
            Para('Clicking a cell selects it, requests focus and scrolls it into view. If alwaysCenter is true, scrollTo centers the item where possible.'),
            Figure(f"{G}/behaviour.svg", "The main runtime behaviour."),
            Section('Keyboard and mouse navigation'),
            Para('RIGHT, ENTER and TAB select the next item; LEFT selects the previous item. With loopSelection true, navigation wraps at both ends. Mouse wheel adjusts the horizontal translate value.'),
            Figure(f"{G}/behaviour.svg", "Data and interaction flow."),
            Section('Overflow buttons and masking'),
            Para('The skin clamps translateX so content cannot scroll past its bounds. The left/right scroller opacity follows whether hidden content exists on that side.'),
        ]),
        Chapter("Styling", [
            Para("The style hooks below were verified in the control, skin and CSS sources."),
            Figure(f"{G}/styling.svg", "Style hooks and visual states."),
            Section("Style classes"),
            Table(["Style class", "Where used"], [
                        ['strip-view', 'Root control style class.'],
                        ['masked-view', 'Nested MaskedView.'],
                        ['container', 'HBox content container.'],
                        ['strip-cell', 'Default cell style class.'],
                        ['scroller left right', 'Scroll arrow regions.']
            ], widths=[35,65]),
            Section("Pseudo classes"),
            Table(["Pseudo class", "Meaning"], [
                        ['selected', 'Applied to StripCell when its item is selected.']
            ], widths=[30,70]),
            Section("Styleable CSS properties"),
            Table(["Property", "Type", "Default", "Description"], [
                        ['-fx-always-center', 'Boolean', 'true', 'Center selected item when scrolling.'],
                        ['-fx-animate-scrolling', 'Boolean', 'true', 'Animate scroll transitions and button fades.'],
                        ['-fx-animation-duration', 'Duration', '200 ms', 'Scroll-to-item duration.'],
                        ['-fx-fading-size', 'Number', '120', 'Edge fade size.'],
                        ['-fx-loop-selection', 'Boolean', 'true', 'Keyboard navigation wraps around.']
            ], widths=[28,16,18,38]),
            Code('.strip-view {\n    -fx-fading-size: 160;\n    -fx-loop-selection: false;\n}\n.strip-view .strip-cell:selected {\n    -fx-background-color: -fx-accent;\n}', caption="Example CSS."),
        ]),
        Chapter("Accessibility", [
            Para('StripView sets AccessibleRole.LIST_VIEW. StripCell notifies AccessibleAttribute.SELECTED when its selected property changes.'),
        ]),
        Chapter("Recipes", [
            Figure(f"{G}/recipes.svg", "Common configuration recipes."),
            Section('Use custom cells'),
            Code('strip.setCellFactory(view -> new StripView.StripCell<>() {\n    @Override protected void updateItem(String item) {\n        super.setText(item == null ? "" : item.toUpperCase());\n    }\n});'),
            Section('Scroll without selecting'),
            Code('strip.scrollTo("Item 12");'),
            Section('Disable wrapping'),
            Code('strip.setLoopSelection(false);'),
            Section('Tune the fade'),
            Code('strip.setFadingSize(200);'),
            Section('Turn off animation'),
            Code('strip.setAnimateScrolling(false);'),
            Section("Checklist"),
            Numbered(['Use selectedItemProperty as the selection model.', 'Call scrollTo only for items that exist in getItems().', 'Set cellFactory before populating when custom visuals are needed.', 'Large fadingSize values reduce the fully visible center area.']),
        ]),
        Chapter("See also", [
            Para("Demo app: <font face='Courier'>StripViewApp</font>. Run it with:"),
            Code("mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.StripViewApp"),
            Bullets([
                'Related GemsFX controls: MaskedView, AutoscrollListView, Carousel-like controls.',
                "API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/",
            ]),
        ]),
    ],
)
