### SimpleFilterView

A control for creating filters based on various other controls such as the `SelectionBox`,
the `DateRangePicker`, the `SearchTextField`, etc...

The control automatically manages a list of `ChipView` instances based on the current selection.
These chip views can be displayed by the `ChipsViewContainer`. To do so bind the observable list
of chips views of the filter view with the one provided by the `ChipsViewContainer`.
