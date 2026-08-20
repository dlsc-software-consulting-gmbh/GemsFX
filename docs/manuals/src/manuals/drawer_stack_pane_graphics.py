"""Generates the cartoon illustrations for the DrawerStackPane manual.

Run indirectly via ``build.py drawer_stack_pane``; the SVGs are written to
``graphics/drawer-stack-pane/``.
"""

from pathlib import Path

from manualkit.svgstyle import (
    AMBER,
    AMBER_PALE,
    GLASS,
    INDIGO,
    INDIGO_PALE,
    INK,
    INK_SOFT,
    MUTED,
    PAPER,
    ROSE,
    SLATE_PALE,
    TEAL,
    TEAL_PALE,
    Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "drawer-stack-pane"

LINE = "#B7C2D0"


# ---------------------------------------------------------------------------
# small motifs reused by several drawings
# ---------------------------------------------------------------------------


def _content_lines(s: Sketch, x, y, w, rows=4, colour=LINE, gap=26, width=2.6):
    for i in range(rows):
       length = w * (1.0 if i % 3 != 2 else 0.62)
       s.line(x, y + i * gap, x + length, y + i * gap, stroke=colour, width=width)


def _glass(s: Sketch, x, y, w, h, opacity=0.3):
    """The dark, semi transparent pane that blocks input while the drawer is open."""
    s.box(x, y, w, h, fill=GLASS, stroke=None, radius=4, opacity=opacity)


def _handle(s: Sketch, cx, y, half=26, gap=6):
    for i in range(3):
       s.line(cx - half, y + i * gap, cx + half, y + i * gap, stroke=MUTED, width=2.6)


def _toolbar_button(s: Sketch, x, y, w, text, size=17.5):
    """A drawer toolbar button - dark, with the rounded lower corners of the CSS."""
    s.box(x, y, w, 30, fill="#3B424C", stroke=INK, radius=7)
    s.text(x + w / 2, y + 20, text, size=size, fill=PAPER)


# ---------------------------------------------------------------------------
# 1 - cover
# ---------------------------------------------------------------------------


def cover() -> Sketch:
    s = Sketch(1000, 480, seed=11)

    s.window(60, 34, 880, 420, title="My Application")
    top = 60
    _content_lines(s, 100, top + 40, 800, rows=3, colour=LINE, gap=30)
    s.box(100, top + 140, 360, 150, fill=SLATE_PALE, radius=10, stroke=LINE, width=2.0)
    s.box(500, top + 140, 400, 150, fill=SLATE_PALE, radius=10, stroke=LINE, width=2.0)

    _glass(s, 62, top, 876, 452 - top)

    dx, dy, dw, dh = 175, 215, 650, 239
    s.box(dx, dy, dw, dh, fill=PAPER, radius=12, shadow=True)
    _handle(s, dx + dw / 2, dy + 14)
    s.text(dx + 24, dy + 60, "Details", size=28.5, anchor="start", fill=INK)
    _toolbar_button(s, dx + dw - 106, dy + 34, 82, "Close", size=19.0)
    s.line(dx + 16, dy + 82, dx + dw - 16, dy + 82, stroke="#E2E5E9", width=2.4)
    _content_lines(s, dx + 24, dy + 116, dw - 48, rows=4, colour=LINE, gap=28)

    s.arrow(880, 432, 880, 262, bend=0.0, stroke=AMBER, width=3.0, head=14)
    s.arrow(120, 432, 120, 262, bend=0.0, stroke=AMBER, width=3.0, head=14)
    return s


# ---------------------------------------------------------------------------
# 2 - anatomy
# ---------------------------------------------------------------------------


def anatomy() -> Sketch:
    s = Sketch(1150, 560, seed=23)

    x, y, w, h = 45, 40, 600, 480
    s.window(x, y, w, h, title="DrawerStackPane")
    top = y + 26

    _content_lines(s, x + 40, top + 50, w - 80, rows=3, colour=LINE, gap=32)
    s.box(x + 40, top + 152, w - 80, 74, fill=SLATE_PALE, radius=10, stroke=LINE, width=2.0)
    _glass(s, x + 2, top, w - 4, h - 28)

    dx, dy, dw, dh = x + 62, y + 246, w - 124, h - 248
    s.box(dx, dy, dw, dh, fill=PAPER, radius=12, shadow=True)
    _handle(s, dx + dw / 2, dy + 13, half=24, gap=6)
    s.text(dx + 20, dy + 66, "Details", size=25.5, anchor="start", fill=INK)
    _toolbar_button(s, dx + dw - 190, dy + 42, 84, "Refresh")
    _toolbar_button(s, dx + dw - 98, dy + 42, 78, "Close")
    s.line(dx + 14, dy + 88, dx + dw - 14, dy + 88, stroke="#E2E5E9", width=2.4)
    s.box(dx + 22, dy + 104, dw - 44, dh - 124, fill=TEAL_PALE, radius=8, dash="9 7", stroke=TEAL, width=2.4)
    s.text(dx + dw / 2, dy + 104 + (dh - 124) / 2 + 6, "drawer content", size=21.5, fill=TEAL)

    # Ordered top to bottom: near horizontal leader lines then never cross.
    keys = [
       ("1", x + 330, top + 50, "children of the stack pane", 108),
       ("2", x + 250, top + 186, "GlassPane \u2013 blocks user input", 190),
       ("3", dx + dw / 2 + 34, dy + 16, "drag handle \u2013 resizes the drawer", 262),
       ("4", dx + dw - 58, dy + 52, "toolbarItems + built-in Close button", 322),
       ("5", dx + 96, dy + 60, "drawerTitle (showDrawerTitle)", 380),
       ("6", dx + dw / 2 + 130, dy + 150, "drawerContent", 440),
       ("7", dx + 12, dy + dh - 30, "drawer container (VBox)", 500),
    ]

    legend_x = 700
    for key, kx, ky, text, ly in keys:
       s.badge(legend_x, ly, key, fill=INDIGO, size=16)
       s.text(legend_x + 28, ly + 5, text, size=19.0, anchor="start", fill=INK)
       s.arrow(legend_x - 22, ly, kx, ky, bend=0.05, stroke=MUTED, width=1.6, head=9)

    return s


# ---------------------------------------------------------------------------
# 3 - states
# ---------------------------------------------------------------------------


def states() -> Sketch:
    s = Sketch(1000, 400, seed=31)

    frames = [
       (40, "showDrawer = false", 0.0),
       (355, "animating \u2026", 0.45),
       (670, "showDrawer = true", 0.9),
    ]

    w, h, y, bar = 290, 250, 40, 22

    for x, caption, fraction in frames:
       s.window(x, y, w, h, title="", titlebar=bar, shadow=True)
       top = y + bar
       _content_lines(s, x + 26, top + 34, w - 52, rows=3, colour=LINE, gap=26)

       if fraction > 0:
           _glass(s, x + 3, top, w - 6, h - bar - 3, opacity=0.16 + 0.2 * fraction)
           available = h - bar - 26
           drawer_h = available * fraction
           dy = y + h - 4 - drawer_h
           s.box(x + 30, dy, w - 60, drawer_h, fill=PAPER, radius=10, shadow=True)
           _handle(s, x + w / 2, dy + 11, half=20, gap=5)
           if fraction > 0.6:
               s.text(x + 46, dy + 52, "Details", size=20.0, anchor="start", fill=INK)
               _content_lines(s, x + 46, dy + 82, w - 92, rows=3, colour=LINE, gap=22, width=2.2)

       s.label(x + w / 2, y + h + 40, caption, size=20.0, fill=INK_SOFT)

    for x in (338, 653):
       s.arrow(x, 165, x + 26, 165, bend=0.0, stroke=AMBER, width=2.8, head=12)

    s.label(500, 352, "animateDrawer = true \u2192 animationDuration (250 ms, EASE_BOTH)", size=20.0, fill=AMBER)
    return s


# ---------------------------------------------------------------------------
# 4 - layout maths
# ---------------------------------------------------------------------------


def layout() -> Sketch:
    s = Sketch(1060, 600, seed=43)

    x, y, w, h = 190, 70, 590, 420
    s.box(x, y, w, h, fill=PAPER, radius=10)
    s.text(x + w / 2, y - 16, "DrawerStackPane", size=20.0, fill=MUTED)

    top_padding = 62
    side_padding = 110
    baseline = y + top_padding

    dx = x + side_padding
    dw = w - 2 * side_padding
    dy = y + top_padding + 116
    dh = y + h - dy

    s.line(x, baseline, x + w, baseline, stroke=MUTED, width=1.8, dash="7 6")
    s.box(dx, dy, dw, dh, fill=INDIGO_PALE, radius=10, stroke=INDIGO)
    s.text(dx + dw / 2, dy + 46, "drawer", size=25.5, fill=INDIGO)

    # vertical measures on the left / right of the pane
    s.dimension(x - 46, y, x - 46, baseline, "", stroke=ROSE)
    s.text(x - 58, (y + baseline) / 2, "topPadding", size=17.5, fill=ROSE, anchor="middle",
          rotate=-90)
    s.dimension(x + w + 46, baseline, x + w + 46, y + h, "", stroke=INK_SOFT)
    s.text(x + w + 58, (baseline + y + h) / 2, "availableHeight", size=17.5, fill=INK_SOFT,
          anchor="middle", rotate=-90)
    s.dimension(x + w + 128, dy, x + w + 128, y + h, "", stroke=INDIGO)
    s.text(x + w + 140, (dy + y + h) / 2, "visible drawer", size=17.5,
          fill=INDIGO, anchor="middle", rotate=-90)

    # horizontal measures below the pane and above the drawer
    s.dimension(x, y + h + 40, dx, y + h + 40, "sidePadding", stroke=ROSE, offset=20)
    s.dimension(dx + dw, y + h + 40, x + w, y + h + 40, "sidePadding", stroke=ROSE, offset=20)
    s.dimension(dx, dy - 26, dx + dw, dy - 26, "preferredDrawerWidth", stroke=TEAL, offset=-18)

    # min / max drawer height as fractions of the available height
    available = h - top_padding
    s.line(x + 8, y + h - available * 0.1, x + w - 8, y + h - available * 0.1,
          stroke=AMBER, width=1.8, dash="6 6")
    s.label(x + 116, y + h - available * 0.1 - 14, "minDrawerHeight = 0.1", size=17.5, fill=AMBER, bg=PAPER)
    s.label(x + 116, baseline + 20, "maxDrawerHeight = 1.0", size=17.5, fill=AMBER, bg=PAPER)

    s.label(530, 578, "availableHeight = height \u2212 topPadding   \u2022   the drawer is always centred horizontally",
           size=20.0, fill=INK_SOFT)
    return s


# ---------------------------------------------------------------------------
# 5 - interaction
# ---------------------------------------------------------------------------


def interaction() -> Sketch:
    s = Sketch(1000, 440, seed=57)

    w, h, y, bar = 430, 300, 40, 22

    # left panel: auto hide
    x = 45
    s.window(x, y, w, h, title="", titlebar=bar, shadow=True)
    top = y + bar
    _content_lines(s, x + 30, top + 44, w - 60, rows=3, colour=LINE, gap=28)
    _glass(s, x + 3, top, w - 6, h - bar - 3)
    s.box(x + 55, y + 170, w - 110, h - 174, fill=PAPER, radius=10, shadow=True)
    _handle(s, x + w / 2, y + 182, half=22, gap=6)
    s.text(x + 80, y + 224, "Details", size=20.0, anchor="start", fill=INK)
    _content_lines(s, x + 80, y + 250, w - 160, rows=2, colour=LINE, gap=24, width=2.2)

    s.sparkle(x + 104, top + 60, size=15, fill=AMBER)
    s.cursor(x + 104, top + 60, scale=1.1)
    s.label(x + w / 2, y + h + 36, "autoHide: click the glass pane to close", size=18, fill=INK_SOFT)
    s.label(x + w / 2, y + h + 64, "\u2026 and so does the ESCAPE key", size=17, fill=MUTED)

    # right panel: drag to resize plus persistence
    x2 = 525
    s.window(x2, y, w, h, title="", titlebar=bar, shadow=True)
    top2 = y + bar
    _content_lines(s, x2 + 30, top2 + 44, w - 60, rows=2, colour=LINE, gap=28)
    _glass(s, x2 + 3, top2, w - 6, h - bar - 3)
    s.box(x2 + 55, y + 145, w - 110, h - 149, fill=PAPER, radius=10, shadow=True)
    _handle(s, x2 + w / 2, y + 157, half=22, gap=6)
    s.text(x2 + 80, y + 200, "Details", size=20.0, anchor="start", fill=INK)
    _content_lines(s, x2 + 80, y + 228, w - 190, rows=2, colour=LINE, gap=24, width=2.2)

    s.arrow(x2 + 100, y + 210, x2 + 100, y + 130, bend=0.0, stroke=ROSE, width=2.6, head=12)
    s.arrow(x2 + 100, y + 210, x2 + 100, y + 288, bend=0.0, stroke=ROSE, width=2.6, head=12)
    s.cursor(x2 + w / 2 + 30, y + 152, scale=1.1)

    s.box(x2 + w - 140, y + h - 92, 124, 66, fill=TEAL_PALE, stroke=TEAL, radius=10)
    s.text(x2 + w - 78, y + h - 64, "Preferences", size=17.5, fill=TEAL)
    s.text(x2 + w - 78, y + h - 44, "drawer.height", size=16.0, fill=TEAL)

    s.label(x2 + w / 2, y + h + 36, "drag the header to resize the drawer", size=18, fill=INK_SOFT)
    s.label(x2 + w / 2, y + h + 64, "\u2026 the height is remembered", size=17, fill=MUTED)
    return s


DRAWINGS = {
    "cover.svg": cover,
    "anatomy.svg": anatomy,
    "states.svg": states,
    "layout.svg": layout,
    "interaction.svg": interaction,
}


def generate() -> None:
    for name, factory in DRAWINGS.items():
       factory().save(OUT / name)


if __name__ == "__main__":
    generate()
    print(f"wrote {len(DRAWINGS)} graphics to {OUT}")
