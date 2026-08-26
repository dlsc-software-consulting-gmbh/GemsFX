### ArcProgressIndicator

ArcProgressIndicator is the abstract base class of all progress indicators of GemsFX that
visualize their progress with an arc. GemsFX ships with two implementations:
CircleProgressIndicator and SemiCircleProgressIndicator.

The base class defines the properties that both of them share: the style type (default, bold,
thin, sector), the arc type used for the progress and for the track, an optional graphic shown
inside the indicator, and a string converter that turns the current progress into the text
displayed by the indicator.

Just like the standard ProgressIndicator the control supports a determinate state, where the arc
is filled according to a progress value between 0.0 and 1.0, and an indeterminate state, where
an animation indicates that the progress can not be determined.

The demo shows both implementations side by side so that the effect of the inherited properties
can be compared directly.
