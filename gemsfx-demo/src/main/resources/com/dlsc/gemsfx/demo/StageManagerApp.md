### StageManager

A manager for storing the location and dimension of a stage across user sessions.
Installing this manager on a stage will ensure that a stage will present itself at
the same location and in the same size that it had when the user closed it the last
time. This manager also works with multiple screens and will ensure that the window
becomes visible if the last used screen is no longer available. In that case the stage
will be shown centered on the primary screen with the specified min width and min
height.
