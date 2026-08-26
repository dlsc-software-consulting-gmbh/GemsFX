### MaskedView

MaskedView takes a content node and applies advanced clipping to it so that its left and its
right hand side "fade out". This is especially useful for horizontally scrolling content, for
example a row of items, as items that are only partially visible do not appear to be cut off.
The StripView control uses a masked view inside its own skin.

The size of the two fading areas is controlled by the "fading size" property, which can also be
set via CSS using -fx-fading-size.

The demo shows a long row of items inside a masked view. The row can be scrolled with the mouse
wheel or with the two buttons below it, and the size of the fading areas can be changed with the
slider.

Usage examples:
```

    MaskedView maskedView = new MaskedView(content);
    maskedView.setFadingSize(200);

```
