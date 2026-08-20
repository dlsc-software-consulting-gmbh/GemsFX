"""Content of the SearchTextField developer manual."""

from manualkit import (
    Bullets, Callout, Chapter, Code, Figure, Manual, Numbered, Para,
    Property, PropertyTable, Section, Table,
)

G = "search-text-field"

MANUAL = Manual(
    control='SearchTextField',
    package='com.dlsc.gemsfx',
    subtitle='Search input with clear icon and optional persisted history',
    abstract='SearchTextField extends CustomTextField with a localized prompt, a search/history button, a clear icon and optional HistoryManager integration.',
    cover_svg=f"{G}/cover.svg",
    cover_caption='A generated cartoon overview of SearchTextField.',
    chapters=[
        Chapter('Introduction', [
                Para("<b>SearchTextField</b> is a search-oriented <font face='Courier'>CustomTextField</font>. It shows a search icon on the left, a clear icon on the right when text is present and can connect to a <font face='Courier'>HistoryManager&lt;String&gt;</font> for a popup of previous queries."),
                Section('Key features'),
                Bullets(['Localized default prompt and empty-history placeholder.', "Optional persisted history via <font face='Courier'>StringHistoryManager</font>.", 'Automatic history insertion on Enter and/or focus lost.', "Styleable rounded appearance through <font face='Courier'>-fx-round</font>.", 'Clear icon visible only when the field has text.']),
                Section('Maven dependency'),
                Code('<dependency>\n    <groupId>com.dlsc.gemsfx</groupId>\n    <artifactId>gemsfx</artifactId>\n    <version>4.4.1</version>\n</dependency>', caption='Maven coordinates for the GemsFX control library.')
            ]),
        Chapter('Getting started', [
                Code('SearchTextField field = new SearchTextField();\nPreferences prefs = Preferences.userNodeForPackage(MyApp.class);\nfield.setHistoryManager(new StringHistoryManager(prefs, "main-search"));\nfield.setRound(true);\n\nfield.setOnAction(evt -> runSearch(field.getText()));', caption='A complete search field with persisted string history.'),
                Figure(f"{G}/states.svg", 'Disabled history, clear icon and open history states.')
            ]),
        Chapter('Anatomy', [
                Figure(f"{G}/anatomy.svg", 'Parts of a SearchTextField.'),
                Table(['Part', 'Node / property', 'Description'], [['Root', "<font face='Courier'>.search-text-field</font>", 'CustomTextField subclass.'], ['History button', "<font face='Courier'>HistoryButton&lt;String&gt;</font>", 'Left node; opens the popup when a manager exists.'], ['Clear icon', "<font face='Courier'>.clear-icon-wrapper</font>", 'Right node; clears text on click.'], ['Popup', "<font face='Courier'>HistoryPopup</font>", 'Owned by HistoryButton and styled by history-button.css.']], widths=[24, 32, 44])
            ]),
        Chapter('Control API', [
                PropertyTable([Property('historyManager', 'ObjectProperty&lt;HistoryManager&lt;String&gt;&gt;', 'null', 'Enables popup history when non-null.'), Property('historyPlaceholder', 'ObjectProperty&lt;Node&gt;', 'Label("No items.")', 'Placeholder node for an empty history list.'), Property('addingItemToHistoryOnEnter', 'BooleanProperty', 'true', 'Adds non-blank text to history on action events.'), Property('addingItemToHistoryOnFocusLost', 'BooleanProperty', 'true', 'Adds non-blank text to history when focus is lost.'), Property('round', 'BooleanProperty', 'false', "Toggles the style class <font face='Courier'>round</font>. Styleable."), Property('promptText', 'StringProperty', 'Search...', 'Localized constructor default.')])
            ]),
        Chapter('History behaviour', [
                Figure(f"{G}/flow.svg", 'How searches are stored and restored.'),
                Para("The field never creates a history manager by itself. When <font face='Courier'>historyManager</font> is null, the left button is visually reduced to a search icon and <font face='Courier'>showPopup()</font> returns without opening a popup."),
                Para("When the manager is present, pressing Enter or losing focus calls <font face='Courier'>addToHistory()</font> if the corresponding property is true and the text is not blank. Selecting an item in the popup copies it into the field and hides the popup."),
                Code('field.setAddingItemToHistoryOnEnter(false);\nfield.setAddingItemToHistoryOnFocusLost(true);\nfield.getHistoryManager().setMaxHistorySize(30);')
            ]),
        Chapter('Popup customization', [
                Para("History display is delegated to <font face='Courier'>HistoryButton</font>. SearchTextField binds the button placeholder and history manager to its own properties, then copies selected values back into the text field."),
                Table(['Extension point', 'Description'], [["<font face='Courier'>historyPlaceholder</font>", 'Node displayed by the popup list when the manager has no entries.'], ["<font face='Courier'>HistoryManager</font>", 'Controls persistence, maximum size and ordering of history entries.'], ["Inherited <font face='Courier'>left/right</font>", 'Can be replaced, but doing so removes the built-in history or clear affordance.']], widths=[36, 64]),
                Code('''Label empty = new Label("Nothing searched yet");
field.setHistoryPlaceholder(empty);
field.getHistoryManager().setMaxHistorySize(10);'''),
                Callout('The clear icon is a managed node only while text is non-empty, so the field does not reserve right-side space for empty input.', kind='note')
            ]),
        Chapter('Styling', [
                Section('Style classes and pseudo classes'),
                Table(['Selector', 'Description'], [["<font face='Courier'>.search-text-field</font>", 'root field'], ["<font face='Courier'>.search-text-field.round</font>", 'round root style class'], ["<font face='Courier'>.history-button</font>", 'left button'], ["<font face='Courier'>.history-button:disabled-popup</font>", 'button while history manager is null'], ["<font face='Courier'>.history-button:popup-showing</font>", 'button while popup is visible'], ["<font face='Courier'>.clear-icon-wrapper</font>", 'right clear wrapper'], ["<font face='Courier'>.history-popup .history-list-view</font>", 'popup list from HistoryButton stylesheet']], widths=[48, 52]),
                Section('Styleable CSS properties'),
                Table(['CSS property', 'Type', 'Default'], [["<font face='Courier'>-fx-round</font>", 'boolean', 'false']], widths=[50, 25, 25]),
                Code('.search-text-field {\n    -fx-pref-width: 320px;\n    -fx-round: true;\n}\n\n.search-text-field > .right-pane > .clear-icon-wrapper {\n    -fx-padding: 0 8px;\n}')
            ]),
        Chapter('Localization', [
                Para("The constructor loads two strings through <font face='Courier'>ResourceBundleManager</font> from <font face='Courier'>search-text-field.properties</font>."),
                Table(['Key', 'English text'], [["<font face='Courier'>prompt.search</font>", 'Search...'], ["<font face='Courier'>placeholder.history-empty</font>", 'No items.']], widths=[44, 56])
            ]),
        Chapter('Accessibility', [
                Para("The constructor sets <font face='Courier'>AccessibleRole.TEXT_FIELD</font>. The embedded <font face='Courier'>HistoryButton</font> has button semantics; the clear icon is a clickable graphic, so applications with strict keyboard requirements may replace the right node with a focusable button.")
            ]),
        Chapter('Recipes', [
                Section('Disable automatic history on Enter'),
                Code('field.setAddingItemToHistoryOnEnter(false);'),
                Section('Use a custom placeholder'),
                Code('field.setHistoryPlaceholder(new Label("No recent searches"));'),
                Section('Clear history'),
                Code('Optional.ofNullable(field.getHistoryManager()).ifPresent(HistoryManager::clear);'),
                Section('Checklist'),
                Numbered(["Create and set a <font face='Courier'>HistoryManager</font> to enable the popup.", 'Use a stable preferences key for persisted history.', "Bind or style <font face='Courier'>round</font> for pill-shaped search boxes.", "Run the search in the normal <font face='Courier'>onAction</font> handler."])
            ]),
        Chapter('See also', [
                Bullets(["Demo application: <font face='Courier'>com.dlsc.gemsfx.demo.SearchTextFieldApp</font> (run with <font face='Courier'>mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.SearchTextFieldApp</font>)", "<font face='Courier'>CustomTextField</font> - superclass for left / right embedded nodes.", "<font face='Courier'>HistoryButton</font> - reusable popup button used by the field.", "<font face='Courier'>EmailField</font> - text field with validation and suggestions.", 'API documentation: https://dlsc-software-consulting-gmbh.github.io/GemsFX/api/'])
            ])
    ],
)
