### TagsField

This field is a specialization of the `SearchField` control and supports
the additional feature of using the selected object as a tag. Tags are shown in front
of the text input field. The control provides an observable list of the currently
added tags. In addition, the field also allows the user to select one or more of
the tags. The selection state is provided by the selection model. The field adds
and removes tags via undoable commands, which means that, for example, a deleted tag
can be recovered by pressing the standard undo (or redo) shortcut.
