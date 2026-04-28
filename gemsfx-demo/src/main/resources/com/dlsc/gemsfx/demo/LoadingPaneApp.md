### LoadingPane

A custom pane that supports the visual feedback for data loading / refreshing its content. The pane is a wrapper
around a given content node. This node will be hidden and a progress indicator shown instead when the status of
this pane changes to `loading`. Once the status changes back to `ok` the node will be
shown again. If anything goes wrong while loading new data or refreshing the content a third state called
`error` can be applied resulting in an error icon and error text being shown. The pane also supports
a `progressProperty()` for detailed progress feedback.
