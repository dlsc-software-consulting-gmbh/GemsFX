"""Generates cartoon illustrations for the SemiCircleProgressIndicator manual."""

from pathlib import Path
import math

from manualkit.svgstyle import (
    AMBER, AMBER_PALE, GLASS, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED,
    PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
    TITLE_SIZE, LABEL_SIZE, TEXT_SIZE, SMALL_SIZE,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "semi-circle-progress-indicator"
IS_CIRCLE = False
IS_SEMI = True
TITLE = "SemiCircleProgressIndicator"


def _arc_path(cx, cy, r, start, end):
    a0 = math.radians(start)
    a1 = math.radians(end)
    x0, y0 = cx + r * math.cos(a0), cy - r * math.sin(a0)
    x1, y1 = cx + r * math.cos(a1), cy - r * math.sin(a1)
    large = 1 if abs(end - start) > 180 else 0
    sweep = 0 if end < start else 1
    return f"M {x0:.1f} {y0:.1f} A {r:.1f} {r:.1f} 0 {large} {sweep} {x1:.1f} {y1:.1f}"


def _gauge(s, cx, cy, r, progress=.68, label="68%", style="default", semi=None, start_angle=90):
    if semi is None:
        semi = IS_SEMI
    if semi:
        base_start, base_end = 180, 0
        prog_end = 180 - 180 * progress
    else:
        base_start, base_end = start_angle, start_angle - 359.9
        prog_end = start_angle - 360 * progress
    track_w = 16 if style == "bold" else 5 if style == "thin" else 10
    prog_w = 8 if style == "bold" else 5 if style == "thin" else 10
    if style == "sector":
        if semi:
            path = _arc_path(cx, cy, r, 180, prog_end) + f" L {cx:.1f} {cy:.1f} Z"
        else:
            path = _arc_path(cx, cy, r, start_angle, prog_end) + f" L {cx:.1f} {cy:.1f} Z"
        s.raw(f'<path d="{path}" fill="{INDIGO_PALE}" stroke="{INDIGO}" stroke-width="3"/>')
        return
    if semi:
        s.raw(f'<path d="{_arc_path(cx, cy, r, base_start, base_end)}" fill="none" stroke="#D1D5DB" stroke-width="{track_w}" stroke-linecap="round"/>')
    else:
        s.raw(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="#D1D5DB" stroke-width="{track_w}"/>')
    s.raw(f'<path d="{_arc_path(cx, cy, r, base_start, prog_end)}" fill="none" stroke="{INDIGO}" stroke-width="{prog_w}" stroke-linecap="round"/>')
    if label:
        s.text(cx, cy + (8 if not semi else -8), label, size=TITLE_SIZE if len(label) < 8 else LABEL_SIZE, fill=INK)


def _mini_window(s, x, y, w, h):
    s.window(x, y, w, h, title="Task", shadow=True)
    for i in range(4):
        length = w - 90 - (i % 2) * 80
        s.line(x + 45, y + 65 + i * 30, x + 45 + length, y + 65 + i * 30, stroke="#B7C2D0", width=2.4)


def cover():
    s = Sketch(1000, 480, seed=121)
    _mini_window(s, 80, 38, 840, 400)
    s.box(340, 122, 320, 260, fill=PAPER, radius=18, shadow=True)
    _gauge(s, 500, 250 if not IS_SEMI else 280, 96, .72, "72%")
    s.text(500, 95, TITLE, size=29, fill=INDIGO)
    s.label(500, 410, "ProgressIndicator skin + CSS styling hooks", size=LABEL_SIZE, fill=INK_SOFT)
    s.sparkle(660, 180, size=18, fill=AMBER)
    return s


def anatomy():
    s = Sketch(1120, 540, seed=122)
    cx, cy, r = 360, 265 if not IS_SEMI else 295, 145
    _gauge(s, cx, cy, r, .64, "64%")
    s.box(cx - 88, cy - (46 if not IS_SEMI else 88), 176, 92, fill=TEAL_PALE, stroke=TEAL, radius=16, dash="8 6")
    s.text(cx, cy + (8 if not IS_SEMI else -45), "label / graphic", size=LABEL_SIZE, fill=TEAL)
    entries = [
        ("1", cx + r * .40, cy - r * .92, "track arc (.track-circle)", 120),
        ("2", cx + r * .86, cy - r * .40, "progress arc (.progress-arc)", 205),
        ("3", cx, cy + (5 if not IS_SEMI else -42), "center label from converter", 290),
        ("4", cx - r * .70, cy + (r * .45 if not IS_SEMI else -8), "ProgressIndicator progress", 375),
        ("5", cx + r * .10, cy + (r + 10 if not IS_SEMI else 8), "completed pseudo-class at 1.0", 460),
    ]
    lx = 670
    for key, tx, ty, text, ly in entries:
        s.badge(lx, ly, key, fill=INDIGO, size=16)
        s.text(lx + 30, ly + 5, text, size=TEXT_SIZE, anchor="start", fill=INK)
        s.arrow(lx - 24, ly, tx, ty, bend=.04, stroke=MUTED, width=1.8, head=9)
    return s


def states():
    s = Sketch(1000, 400, seed=123)
    labels = [("indeterminate", -1, "loading"), ("determinate", .42, "42%"), ("completed", 1.0, "Completed")]
    for i, (caption, prog, text) in enumerate(labels):
        x = 70 + i * 310
        s.box(x, 48, 240, 220, fill=PAPER, radius=18, shadow=True)
        if prog < 0:
            _gauge(s, x + 120, 158 if not IS_SEMI else 184, 70, .30, "", semi=IS_SEMI)
            s.arrow(x + 88, 86, x + 178, 106, bend=.45, stroke=AMBER, width=2.8, head=12)
        else:
            _gauge(s, x + 120, 158 if not IS_SEMI else 184, 70, prog, text, semi=IS_SEMI)
        s.label(x + 120, 310, caption, size=LABEL_SIZE, fill=INK_SOFT)
    s.label(500, 365, "progress < 0 starts the timeline; progress == 1.0 enables :completed", size=TEXT_SIZE, fill=MUTED)
    return s


def styles():
    s = Sketch(1030, 430, seed=124)
    styles = [("DEFAULT", "default"), ("BOLD", "bold"), ("THIN", "thin"), ("SECTOR", "sector")]
    for i, (name, style) in enumerate(styles):
        x = 78 + i * 230
        s.box(x, 58, 180, 220, fill=SLATE_PALE if i % 2 else PAPER, radius=14, shadow=True)
        _gauge(s, x + 90, 170 if not IS_SEMI else 195, 60, .62, "", style=style, semi=IS_SEMI)
        s.label(x + 90, 318, name, size=LABEL_SIZE, fill=INDIGO if style != "sector" else TEAL)
    s.label(515, 382, "styleType toggles :bold-style, :thin-style and :sector-style", size=TEXT_SIZE, fill=INK_SOFT)
    return s


def layout():
    s = Sketch(1050, 500, seed=125)
    cx, cy = 420, 230 if not IS_SEMI else 260
    radius = 145 if not IS_SEMI else 120
    radius_end_x = cx + (126 if not IS_SEMI else radius)
    radius_end_y = cy - (72 if not IS_SEMI else 0)
    s.box(210, 70, 420, 320, fill=PAPER, radius=12)
    s.dimension(220, 420, 620, 420, "control width", stroke=ROSE, size=SMALL_SIZE, offset=22)
    s.dimension(660, 80, 660, 380, "control height", stroke=ROSE, size=SMALL_SIZE, offset=28)
    s.dimension(cx, cy, radius_end_x, radius_end_y, "radius", stroke=TEAL, size=SMALL_SIZE, offset=-18)
    _gauge(s, cx, cy, radius, .55, "55%", semi=IS_SEMI)
    s.text(735, 150, "radius = min(width, height)", size=TEXT_SIZE, fill=INK, anchor="start")
    if IS_SEMI:
        s.text(735, 190, "height counts twice for a half arc", size=TEXT_SIZE, fill=INK_SOFT, anchor="start")
        s.text(735, 230, "label sits above the arc center", size=TEXT_SIZE, fill=INK_SOFT, anchor="start")
    else:
        s.text(735, 190, "label is centered in the circle", size=TEXT_SIZE, fill=INK_SOFT, anchor="start")
        s.text(735, 230, "startAngle chooses the origin", size=TEXT_SIZE, fill=INK_SOFT, anchor="start")
    s.label(520, 470, "Insets and stroke width reduce the usable radius", size=TEXT_SIZE, fill=MUTED)
    return s


DRAWINGS = {
    "cover.svg": cover,
    "anatomy.svg": anatomy,
    "states.svg": states,
    "styles.svg": styles,
    "layout.svg": layout,
}


def generate() -> None:
    for name, factory in DRAWINGS.items():
        factory().save(OUT / name)


if __name__ == "__main__":
    generate()
    print(f"wrote {len(DRAWINGS)} graphics to {OUT}")
