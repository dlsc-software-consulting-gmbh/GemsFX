### ObservableValuesListBinding

This class binds to an `ObservableList` of `ObservableValue` objects and updates its value based on
the current values of these observable values. It reevaluates its value whenever any of the
observable values change. The computed value is determined by applying a transformation function
to the list of current values.
