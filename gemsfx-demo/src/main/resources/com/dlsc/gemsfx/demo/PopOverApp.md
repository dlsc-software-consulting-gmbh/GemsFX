### PopOver

A control that is intended to provide detailed information about
an owning node in a popup window. The popup window has a lightweight
appearance (no default window decorations) and an arrow pointing at the owner.
Due to the nature of popup windows, the popover will move around with the parent
window when the user drags it.

The Popover can be detached from the owning node by dragging it away from the
owner.

Example

```
`var textFlow = new TextFlow(new Text("Some content"));
textFlow.setPrefWidth(300);

var popover = new Popover(textFlow);
var ownerLink = new Hyperlink("Show popover");
ownerLink.setOnAction(e -> popover.show(ownerLink));
`
```
