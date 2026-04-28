### SearchTextField

A custom text field specifically designed for search functionality. This class enhances a text field with features
such as a history of search terms, an optional history popup, and custom icons for search and clear operations.

The history manager is disabled by default, but it can be enabled using the `setHistoryManager(HistoryManager)` method.
We have implemented a local history manager, `StringHistoryManager`, which uses the Java Preferences API to store history records.
You can enable it via the `setHistoryManager(HistoryManager)` method.

By default, when the field loses its focus or the user presses the "enter" key (triggering the onAction event), the
text is added to the history. This behavior can be disabled by setting the `addingItemToHistoryOnEnterProperty()`
and / or the `addingItemToHistoryOnEnterProperty()` to false.

Additionally, history can be manually added based on user actions, such as after typing text and selecting an item
from a ListView or TableView that displays results, or through other interactions, by calling the `getHistoryManager()`
method to access the `StringHistoryManager` instance. then calling the `add(Object)`} method.
