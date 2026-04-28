### FilterView

A view for presenting a set of predefined filter groups, each one with a list of filters.
The user can select one or more filters from each group. Elements found in the resulting
filtered list have to match ALL filters. The selected filters will be shown as "chips"
(see `ChipView`).

Items can be added via the `getItems()` list. Table or list views have to use
the `getFilteredItems()` list. This filtered list can also be wrapped via a
`SortedList` and then added to a table or list view.

An input field for filtering based on text input will appear as soon as a text filter provider
has been defined. See `setTextFilterProvider(Callback)`.

Applications with additional filtering needs can utilize the `additionalFilterPredicateProperty()`.
