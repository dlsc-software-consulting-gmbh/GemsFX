### SearchField

The search field is a standard text field with auto suggest capabilities
and a selection model for a specific type of object. This type is defined by the
generic type argument. The main difference to other auto-suggest text fields is that
the main outcome of this field is an object and not just the text entered by the
user. Another difference is how the text field automatically finds and selects the
first object that matches the text typed by the user so far. A third feature of
this control is its ability to create new instances of the specified object type if
no matching object can be found in the list of objects returned by the suggestion
provider. This last feature allows an application to let the user either pick an
existing object or to create a new one on-the-fly (but only if a new item producer
has been set).

The search field requires proper configuration to work correctly:

- **Suggestion Provider** - a callback that returns a collection of items for a given search field suggestion request. The suggestion provider is invoked asynchronously via JavaFX concurrency API (service & task). The suggestion provider gets invoked slightly delayed whenever the user types some text into the field. If the user types again the current search gets cancelled and a new search gets initiated. As long as the user types fast enough the actual search will not be performed.
- **Converter** - the converter is used to convert the items found in the suggestions list to text. This is just a standard StringConverter instance (only the toString() method needs to be implemented).
- **Cell Factory** - a standard list cell factory / callback used for the ListView instance shown in the popup that presents the suggested items. The default cell factory should be sufficient for most use cases. It simply displays the name of the items via the help of the string converter. However, it also underlines the text match in the name.
- **Matcher** - a function taking two arguments that will be applied to the suggested items to find "perfect matches" for the given search text (entered by the user). The function takes an item and the search text as input and returns a boolean. The first perfect match found will be used to autocomplete the text of the search field.
- **New Item Producer** - a callback that returns a new item instance of the type supported by the search field. This callback is used if the field is configured to create items "on-the-fly", meaning the typed text does not match anything in the suggested list of items.
- **Comparator** - a standard comparator used to perform a first sorting of the suggested items. However, internally the field wraps this comparator to place some items higher up in the dropdown list as they are better matches for the current search text.

The history manager is disabled by default, but it can be enabled using the `setHistoryManager(HistoryManager)` method.
We have implemented a local history manager, `StringHistoryManager`, which uses the Java Preferences API to store history records.
You can enable it via the `setHistoryManager(HistoryManager)` method.
