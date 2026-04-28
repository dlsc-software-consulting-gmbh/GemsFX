### AggregatedListBinding

Binds an `ObservableList` of items to a computed value based on the elements of their associated nested `ObservableList`.

This binding listens for changes not only in the top-level list but also in the nested lists of each item.
It aggregates or computes values dynamically as the lists change, making it ideal for applications where the data model
is highly dynamic.

This class is designed to be used with a consistent source of `ObservableList`s provided by the `itemToListFunction`.
The function must return the same `ObservableList` instance for any given item throughout its lifecycle to ensure
correct behavior and prevent memory leaks.

**Important Considerations:**

- **Stable List References:** The `itemToListFunction` should not return different instances of `ObservableList`
  over time for the same item. If the lists are stored in properties such as `ObjectProperty` or `SimpleListProperty`,
  ensure that these properties are not reassigned new lists during the lifecycle of an item, as this can lead to erratic
  behavior and potential
  memory leaks if listeners are not removed from old lists.
- **Managing Listeners:** If using properties to store the lists, manage listeners carefully. Ensure that any list set
  on a property
  has listeners appropriately added or removed to prevent memory leaks. This may require overriding property setters or
  using property
  change listeners to add/remove list listeners when the property's value changes.
