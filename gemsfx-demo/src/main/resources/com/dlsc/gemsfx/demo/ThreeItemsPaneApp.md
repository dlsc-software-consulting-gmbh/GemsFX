### ThreeItemsPane

A custom layout container that arranges up to three child nodes in either a horizontal
or vertical orientation. This pane allows spacing between the nodes and provides methods
to control the alignment and orientation of the child nodes.

The container manages three possible child nodes, identified as item1, item2, and item3.
The layout updates dynamically whenever the nodes or properties such as orientation or
spacing are modified.

Features include:
- Dynamic management of child nodes: up to three nodes can be added and arranged.
- Adjustable orientation: supports horizontal and vertical alignment through the
  orientation property.
- Customizable spacing: allows setting the spacing between child nodes.

Override methods provide computed sizes for use during layouts, including preferred,
minimum, and maximum widths and heights.
