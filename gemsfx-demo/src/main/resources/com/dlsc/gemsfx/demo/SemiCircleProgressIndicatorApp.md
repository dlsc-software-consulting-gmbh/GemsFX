### SemiCircleProgressIndicator

SemiCircleProgressIndicator is a visual control used to indicate the progress of a task.
It represents progress in a semi-circular form, with options to show determinate or indeterminate states.

In a determinate state, the semi-circle fills up based on the progress value, which ranges from 0.0 to 1.0,
where 0.0 indicates no progress and 1.0 indicates completion.

In an indeterminate state, the semi-circle shows a cyclic animation, indicating that progress is ongoing
but the exact status is unknown. This state is useful for tasks where the progress cannot be determined.

The control also supports displaying a text or graphic inside the semi-circle to provide additional
information or visual feedback to the user.

Usage examples include file downloads, file transfers, or any long-running tasks where
visual feedback on progress is beneficial.

**Pseudo class:** Beyond the **inherited**, **indeterminate**, and **determinate** pseudo-classes
from ProgressIndicator, SemiCircleProgressIndicator introduces a **completed** pseudo-class.
This pseudo-class can be used in CSS to apply custom styles when the progress reaches 1.0 (100%).

**Tips:** If you prefer not to instantiate the animation object during initialization,
pass a 0.0 as the initial progress. This setup indicates no progress but avoids entering
the indeterminate state, which would otherwise instantiate and start the animation.

Usage examples:

```
// Initializes with no progress and no animation.
SemiCircleProgressIndicator progressIndicator = new SemiCircleProgressIndicator(0.0);
```
