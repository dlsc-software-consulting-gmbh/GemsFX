### Spacer

The Spacer class extends the Region class and provides functionality
to create flexible spaces in layouts such as `HBox` and `VBox`. It is primarily
used to push adjacent nodes apart or together by filling up available space. 

The Spacer can be toggled between active and inactive states. When active,
it tries to grow as much as possible within its parent container. When
inactive, it collapses and doesn't take up any space. 

The growth direction of the **Spacer** (horizontal or vertical) is determined
based on its parent container. For instance, when placed inside an `HBox`, the
Spacer will grow horizontally. Conversely, inside a `VBox`, it will grow vertically. 

The active state of the Spacer can also be controlled through CSS with the
`"-fx-active"` property.
