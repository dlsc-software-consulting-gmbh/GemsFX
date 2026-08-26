### AutoscrollListView

AutoscrollListView is a specialization of ListView that automatically starts to scroll up or
down whenever the mouse cursor comes close to the top or the bottom edge of the view while a
drag and drop operation is in progress. This makes it possible to drag an item across a long
list without having to use the scrollbar.

The view currently only supports the vertical orientation.

The demo shows two lists. Items can be dragged from one list to the other one or to a new
position within the same list. Moving the mouse cursor towards the upper or the lower edge of a
list starts the automatic scrolling.

Usage examples:
```

    AutoscrollListView<String> listView = new AutoscrollListView<>();
    listView.getItems().setAll("Item 1", "Item 2", "Item 3");

```
