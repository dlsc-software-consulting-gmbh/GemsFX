# Cartoon graphics style guide

All illustrations in the GemsFX developer manuals are **generated**, never
screenshotted. They are produced by Python code that uses the drawing
vocabulary in [`manualkit/svgstyle.py`](../manualkit/svgstyle.py), which keeps
every manual in the same visual language.

## Rules

1. **Hand-drawn, not technical.** Every outline is drawn through `Sketch.shape`,
   which jitters the points and strokes the path twice. Nothing is a plain
   rectangle.
2. **Thick dark outlines, flat pastel fills.** Ink is `#1F2937`, fills come from
   the pale palette constants (`INDIGO_PALE`, `TEAL_PALE`, `AMBER_PALE`,
   `SLATE_PALE`). No gradients.
3. **Soft offset shadow.** Elements that "float" above the page (windows,
   drawers, popups) pass `shadow=True`.
4. **Handwriting-flavoured labels.** All text uses the `HAND_FONT` stack
   (`Chalkboard SE`, `Comic Sans MS`, `Comic Neue`, `Segoe Print`, `cursive`) and
   degrades gracefully on systems without those fonts.
5. **Seeded randomness.** Every `Sketch` gets a fixed `seed`, so regenerating a
   graphic produces byte identical SVG.

## Shared motifs

| Motif | Helper | Used for |
| --- | --- | --- |
| Application window | `Sketch.window()` | The frame every control is shown in |
| Mouse pointer | `Sketch.cursor()` | User interaction |
| Spark | `Sketch.sparkle()` | "Something happens here" |
| Numbered badge | `Sketch.badge()` | Keys of an anatomy legend |
| Dashed measurement | `Sketch.dimension()` | Layout maths, paddings, sizes |
| Curved arrow | `Sketch.arrow()` | Leader lines, motion, transitions |

## Colour semantics

| Colour | Meaning |
| --- | --- |
| Indigo | The control itself and its primary parts |
| Teal | Application-supplied content |
| Amber | Motion, animation, highlights |
| Rose | Measurements and drag gestures |
| Slate | Passive background content |

## Canvas and type sizes

Graphics are authored on a canvas of roughly **1000 x 500 units** and printed at
about 470 pt width. Use the size constants from `svgstyle` so that annotations
stay readable in print:

* `TITLE_SIZE` (25) - the one dominant word in a drawing
* `LABEL_SIZE` (20) - captions inside the drawing
* `TEXT_SIZE` (18) - legend entries
* `SMALL_SIZE` (16) - secondary remarks

Never let a drawing carry the same caption as the figure caption in the manual;
the caption belongs to the PDF, not to the SVG.

## Anatomy drawings

Leader lines must never cross. Order the legend entries by the *y* coordinate of
their target so that all leaders stay roughly horizontal, and let the arrow head
touch the part it describes.
