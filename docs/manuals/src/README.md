# GemsFX developer manuals

Sources of the per-control developer manuals published as PDFs in
`docs/manuals/`. Every manual is generated from Python, illustrated with
generated cartoon-style SVG graphics rather than screenshots.

## Layout

```
src/
  manualkit/        shared look and feel - all layout lives here
    theme.py        page geometry, palette, fonts
    content.py      the content model (Manual, Chapter, blocks)
    document.py     rendering to PDF (cover, TOC, headers, tables, figures)
    svgstyle.py     the cartoon drawing vocabulary
    raster.py       SVG -> PNG conversion for embedding
  manuals/
    <control>.py           content of one manual (no styling)
    <control>_graphics.py  generates the SVGs of that manual
  graphics/
    STYLE.md               drawing rules
    <control>/*.svg        generated graphics (committed)
  build.py          entry point
```

## Setup

Requires Python 3.9+ and an SVG converter - `rsvg-convert` is recommended
(`brew install librsvg`); `cairosvg` and `inkscape` also work.

```bash
cd docs/manuals/src
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

## Building

```bash
.venv/bin/python build.py                    # all manuals
.venv/bin/python build.py drawer_stack_pane  # one manual
```

The PDFs are written to `docs/manuals/`, the rasterized graphics are cached in
`.raster-cache/` (git-ignored). This is a standalone toolchain - it is not part
of the Maven build.

## Adding a manual for a new control

1. Create `manuals/<control>_graphics.py` with a `generate()` function that
   writes the SVGs into `graphics/<control>/`. Follow `graphics/STYLE.md`, and
   build every shape with the helpers from `manualkit.svgstyle`.
2. Create `manuals/<control>.py` exposing a `MANUAL` object. Use the same
   chapter skeleton as the existing manuals: Introduction, Getting started,
   Anatomy, Control API, layout / behaviour chapters, Styling, Localization,
   Recipes, See also.
3. Verify every documented default against the control source, its CSS file and
   its resource bundle - the manual is a reference, not a guess.
4. Run `build.py <control>` and link the resulting PDF from the control card in
   `docs/index.html`.
