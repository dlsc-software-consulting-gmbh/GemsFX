### DrawerStackPane

A custom stackpane that supports a drawer view sliding in from bottom to top. The content of the drawer gets added
in the normal way via the childrens list. The content for the drawer has to be added by calling `setDrawerContent(Node)`.

Features

    - User can resize the drawer via a handle at the top
    - The drawer automatically closes completely if the user drags the resize handle below the lower bounds of the stackpane
    - Opening and closing can be animated (see `setAnimateDrawer(boolean)`)
    - When the drawer is open the content of the stackpane will be blocked from user input via a dark semi-transparent glass pane
    - The glass pane fades in / out
    - The drawer can have its own preferred width (see `setPreferredDrawerWidth(double)`)
    - The control can automatically persist the drawer height via the Java preferences API (see `setPreferencesKey(String)`)
    - Auto hiding: drawer will close when the user clicks into the background (onto the glass pane)
