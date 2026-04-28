### PhotoView

The photo view is mostly used to display a user profile picture.

## Features
- control can be used as read-only view or as an editor (see `editableProperty()`)
- picture can moved around by dragging it
- picture can be resized by pinch zoom (touch) or via scroll wheel
- control provides a cropped "read only" version of the original image (see `croppedImageProperty()`). This is ideal
for saving memory when saving the image to the server / a database
- applications can set a custom "photo supplier" to replace the built-in file chooser (see `photoSupplierProperty()`)
- drag and drop an image file onto the view
- circular and rectangle shape (see `setClipShape(ClipShape)`)
- customizable maximum zoom value
- keyboard support: backspace and delete keys delete the picture, space or enter trigger the file supplier (default: show the file chooser)
- pseudo class support: "empty" if the `photoProperty()` is null
- an effect can be applied directly to the image (see `photoEffectProperty()`)

**Note: the values for the zoom and translate properties will all be reset when a new photo is set.**
