"""Cartoon drawing vocabulary shared by all GemsFX manual graphics.

The illustrations in the manuals are *generated*, never screenshotted. Every
shape is drawn through the helpers in this module so that all manuals share one
hand-drawn look: wobbly outlines, thick dark strokes, flat pastel fills and soft
offset shadows. See ``graphics/STYLE.md`` for the design rules.

All randomness is seeded, so regenerating a graphic always produces byte
identical output.
"""

import math
import random
from typing import List, Optional, Sequence, Tuple

Point = Tuple[float, float]

# ---------------------------------------------------------------------------
# Palette (kept in sync with manualkit/theme.py)
# ---------------------------------------------------------------------------

PAPER = "#FFFFFF"
INK = "#1F2937"
INK_SOFT = "#475569"
MUTED = "#94A3B8"

INDIGO = "#4F46E5"
INDIGO_PALE = "#E7E9FF"
TEAL = "#0D9488"
TEAL_PALE = "#D8F5F0"
AMBER = "#D97706"
AMBER_PALE = "#FDEBCF"
ROSE = "#E11D48"
ROSE_PALE = "#FCE0E6"
SLATE_PALE = "#EDF1F6"
GLASS = "#334155"

# The handwriting-flavoured font used for annotations. The stack degrades
# gracefully on systems without the macOS fonts.
HAND_FONT = "Chalkboard SE, Comic Sans MS, Comic Neue, Segoe Print, Bradley Hand, cursive"

STROKE = 3.0
STROKE_THIN = 2.0
WOBBLE = 1.5

# Type sizes for annotations. Graphics are authored on a canvas of roughly
# 1000 x 500 units, which the manual prints at ~470 pt width. These sizes are
# calibrated so that the labels stay readable at that scale.
TITLE_SIZE = 25.0
LABEL_SIZE = 20.0
TEXT_SIZE = 18.0
SMALL_SIZE = 16.0


def _esc(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def _fmt(value: float) -> str:
    return f"{value:.2f}".rstrip("0").rstrip(".")


class Sketch:
    """An SVG canvas offering hand-drawn primitives."""

    def __init__(self, width: float, height: float, seed: int = 7, background: Optional[str] = PAPER):
        self.width = width
        self.height = height
        self.background = background
        self._rng = random.Random(seed)
        self._body: List[str] = []

    # -- low level ---------------------------------------------------------

    def raw(self, markup: str) -> None:
        self._body.append(markup)

    def _jitter(self, points: Sequence[Point], amount: float) -> List[Point]:
        out = []
        for x, y in points:
            out.append((x + self._rng.uniform(-amount, amount), y + self._rng.uniform(-amount, amount)))
        return out

    @staticmethod
    def _smooth(points: Sequence[Point], closed: bool) -> str:
        """Build a cubic bezier path through ``points`` (Catmull-Rom)."""
        pts = list(points)
        if len(pts) < 3:
            return "M " + " L ".join(f"{_fmt(x)} {_fmt(y)}" for x, y in pts)

        count = len(pts)

        def at(index: int) -> Point:
            if closed:
                return pts[index % count]
            return pts[max(0, min(count - 1, index))]

        commands = [f"M {_fmt(pts[0][0])} {_fmt(pts[0][1])}"]
        last = count if closed else count - 1
        for i in range(last):
            p0, p1, p2, p3 = at(i - 1), at(i), at(i + 1), at(i + 2)
            c1 = (p1[0] + (p2[0] - p0[0]) / 6.0, p1[1] + (p2[1] - p0[1]) / 6.0)
            c2 = (p2[0] - (p3[0] - p1[0]) / 6.0, p2[1] - (p3[1] - p1[1]) / 6.0)
            commands.append(
                f"C {_fmt(c1[0])} {_fmt(c1[1])} {_fmt(c2[0])} {_fmt(c2[1])} {_fmt(p2[0])} {_fmt(p2[1])}"
            )
        if closed:
            commands.append("Z")
        return " ".join(commands)

    # -- shape outlines ----------------------------------------------------

    @staticmethod
    def rect_outline(x: float, y: float, w: float, h: float, radius: float = 10.0, step: float = 26.0) -> List[Point]:
        """Sample the perimeter of a rounded rectangle."""
        radius = max(0.0, min(radius, min(w, h) / 2.0))
        points: List[Point] = []

        def edge(x0: float, y0: float, x1: float, y1: float) -> None:
            length = math.hypot(x1 - x0, y1 - y0)
            segments = max(1, int(length / step))
            for i in range(segments):
                t = i / segments
                points.append((x0 + (x1 - x0) * t, y0 + (y1 - y0) * t))

        def corner(cx: float, cy: float, start: float) -> None:
            segments = 4
            for i in range(segments + 1):
                angle = math.radians(start + 90.0 * i / segments)
                points.append((cx + radius * math.cos(angle), cy + radius * math.sin(angle)))

        edge(x + radius, y, x + w - radius, y)
        corner(x + w - radius, y + radius, -90)
        edge(x + w, y + radius, x + w, y + h - radius)
        corner(x + w - radius, y + h - radius, 0)
        edge(x + w - radius, y + h, x + radius, y + h)
        corner(x + radius, y + h - radius, 90)
        edge(x, y + h - radius, x, y + radius)
        corner(x + radius, y + radius, 180)
        return points

    @staticmethod
    def ellipse_outline(cx: float, cy: float, rx: float, ry: float, segments: int = 24) -> List[Point]:
        return [
            (cx + rx * math.cos(2 * math.pi * i / segments), cy + ry * math.sin(2 * math.pi * i / segments))
            for i in range(segments)
        ]

    # -- hand drawn primitives --------------------------------------------

    def shape(
        self,
        points: Sequence[Point],
        fill: Optional[str] = None,
        stroke: Optional[str] = INK,
        width: float = STROKE,
        wobble: float = WOBBLE,
        closed: bool = True,
        shadow: bool = False,
        opacity: float = 1.0,
        dash: Optional[str] = None,
    ) -> None:
        """Draw a hand-drawn shape: optional shadow, flat fill, double stroke."""
        if shadow:
            shifted = [(x + 5, y + 6) for x, y in points]
            path = self._smooth(self._jitter(shifted, wobble), closed)
            self.raw(f'<path d="{path}" fill="{INK}" opacity="0.13"/>')

        if fill:
            path = self._smooth(self._jitter(points, wobble * 0.6), closed)
            self.raw(f'<path d="{path}" fill="{fill}" opacity="{_fmt(opacity)}"/>')

        if stroke:
            dash_attr = f' stroke-dasharray="{dash}"' if dash else ""
            for pass_index, alpha in ((0, 1.0), (1, 0.45)):
                jittered = self._jitter(points, wobble * (1.0 if pass_index == 0 else 1.5))
                path = self._smooth(jittered, closed)
                self.raw(
                    f'<path d="{path}" fill="none" stroke="{stroke}" '
                    f'stroke-width="{_fmt(width * (1.0 if pass_index == 0 else 0.7))}" '
                    f'stroke-linecap="round" stroke-linejoin="round" opacity="{alpha}"{dash_attr}/>'
                )

    def box(
        self,
        x: float,
        y: float,
        w: float,
        h: float,
        fill: Optional[str] = None,
        stroke: Optional[str] = INK,
        radius: float = 10.0,
        width: float = STROKE,
        shadow: bool = False,
        dash: Optional[str] = None,
        opacity: float = 1.0,
    ) -> None:
        self.shape(
            self.rect_outline(x, y, w, h, radius),
            fill=fill,
            stroke=stroke,
            width=width,
            shadow=shadow,
            dash=dash,
            opacity=opacity,
        )

    def blob(self, cx: float, cy: float, rx: float, ry: float, fill: str, stroke: Optional[str] = INK,
             shadow: bool = False, width: float = STROKE) -> None:
        # Small circles must stay recognisable as circles, so the wobble is
        # scaled down with the radius.
        wobble = max(0.35, min(2.0, min(rx, ry) * 0.09))
        self.shape(
            self.ellipse_outline(cx, cy, rx, ry, segments=max(14, int(min(rx, ry)))),
            fill=fill,
            stroke=stroke,
            shadow=shadow,
            wobble=wobble,
            width=width,
        )

    def line(self, x0: float, y0: float, x1: float, y1: float, stroke: str = INK,
             width: float = STROKE_THIN, dash: Optional[str] = None) -> None:
        steps = max(2, int(math.hypot(x1 - x0, y1 - y0) / 24))
        points = [(x0 + (x1 - x0) * i / steps, y0 + (y1 - y0) * i / steps) for i in range(steps + 1)]
        self.shape(points, fill=None, stroke=stroke, width=width, closed=False, dash=dash)

    def arrow(
        self,
        x0: float,
        y0: float,
        x1: float,
        y1: float,
        bend: float = 0.25,
        stroke: str = INK,
        width: float = STROKE_THIN,
        head: float = 11.0,
    ) -> None:
        """A curved annotation arrow with a fat triangular head."""
        mx, my = (x0 + x1) / 2.0, (y0 + y1) / 2.0
        dx, dy = x1 - x0, y1 - y0
        length = math.hypot(dx, dy) or 1.0
        nx, ny = -dy / length, dx / length
        cx, cy = mx + nx * bend * length, my + ny * bend * length

        points = []
        for i in range(17):
            t = i / 16.0
            bx = (1 - t) ** 2 * x0 + 2 * (1 - t) * t * cx + t ** 2 * x1
            by = (1 - t) ** 2 * y0 + 2 * (1 - t) * t * cy + t ** 2 * y1
            points.append((bx, by))

        self.shape(points[:-1], fill=None, stroke=stroke, width=width, closed=False, wobble=0.9)

        tip = points[-1]
        prev = points[-4]
        angle = math.atan2(tip[1] - prev[1], tip[0] - prev[0])
        left = (tip[0] - head * math.cos(angle - 0.42), tip[1] - head * math.sin(angle - 0.42))
        right = (tip[0] - head * math.cos(angle + 0.42), tip[1] - head * math.sin(angle + 0.42))
        self.raw(
            f'<path d="M {_fmt(tip[0])} {_fmt(tip[1])} L {_fmt(left[0])} {_fmt(left[1])} '
            f'L {_fmt(right[0])} {_fmt(right[1])} Z" fill="{stroke}"/>'
        )

    def text(
        self,
        x: float,
        y: float,
        value: str,
        size: float = 16.0,
        anchor: str = "middle",
        fill: str = INK,
        bold: bool = False,
        italic: bool = False,
        family: str = HAND_FONT,
        rotate: Optional[float] = None,
    ) -> None:
        transform = f' transform="rotate({_fmt(rotate)} {_fmt(x)} {_fmt(y)})"' if rotate is not None else ""
        weight = ' font-weight="bold"' if bold else ""
        style = ' font-style="italic"' if italic else ""
        self.raw(
            f'<text x="{_fmt(x)}" y="{_fmt(y)}" font-family="{family}" font-size="{_fmt(size)}" '
            f'fill="{fill}" text-anchor="{anchor}"{weight}{style}{transform}>{_esc(value)}</text>'
        )

    def label(self, x: float, y: float, value: str, size: float = 15.0, fill: str = INK,
              anchor: str = "middle", bg: Optional[str] = None) -> None:
        """A short annotation, optionally sitting on a pastel sticker."""
        if bg:
            width = 0.62 * size * len(value) + 18
            left = {"middle": x - width / 2, "start": x - 9, "end": x - width + 9}[anchor]
            self.box(left, y - size * 1.05, width, size * 1.7, fill=bg, stroke=None, radius=size * 0.7)
        self.text(x, y + size * 0.34, value, size=size, anchor=anchor, fill=fill)

    def dimension(
        self,
        x0: float,
        y0: float,
        x1: float,
        y1: float,
        value: str,
        stroke: str = ROSE,
        size: float = 13.0,
        offset: float = 0.0,
    ) -> None:
        """A dashed measurement line with end ticks and a label."""
        self.line(x0, y0, x1, y1, stroke=stroke, width=1.8, dash="7 5")
        dx, dy = x1 - x0, y1 - y0
        length = math.hypot(dx, dy) or 1.0
        nx, ny = -dy / length * 6, dx / length * 6
        for px, py in ((x0, y0), (x1, y1)):
            self.line(px - nx, py - ny, px + nx, py + ny, stroke=stroke, width=1.8)
        mx, my = (x0 + x1) / 2.0, (y0 + y1) / 2.0
        ox, oy = (-dy / length * offset, dx / length * offset)
        if value:
            self.label(mx + ox, my + oy, value, size=size, fill=stroke, bg=PAPER)

    # -- composite motifs shared across manuals ----------------------------

    def window(self, x: float, y: float, w: float, h: float, title: str = "", fill: str = PAPER,
               titlebar: float = 26.0, shadow: bool = True) -> None:
        """The recurring "application window" frame motif."""
        self.box(x, y, w, h, fill=fill, radius=12, shadow=shadow)
        self.line(x + 2, y + titlebar, x + w - 2, y + titlebar, stroke=INK, width=2.0)
        for i, colour in enumerate((ROSE, AMBER, TEAL)):
            self.blob(x + 20 + i * 19, y + titlebar / 2.0, 6.0, 6.0, fill=colour, stroke=INK, width=1.8)
        if title:
            self.text(x + w / 2.0, y + titlebar / 2.0 + 5, title, size=13, fill=INK_SOFT)

    def cursor(self, x: float, y: float, scale: float = 1.0, fill: str = INK) -> None:
        """A cartoon mouse pointer."""
        pts = [(0, 0), (0, 24), (6, 18), (10, 27), (14, 25), (10, 16), (17, 15)]
        path = " ".join(
            f"{'M' if i == 0 else 'L'} {_fmt(x + px * scale)} {_fmt(y + py * scale)}" for i, (px, py) in enumerate(pts)
        )
        self.raw(f'<path d="{path} Z" fill="{PAPER}" stroke="{fill}" stroke-width="2.6" stroke-linejoin="round"/>')

    def sparkle(self, x: float, y: float, size: float = 12.0, fill: str = AMBER) -> None:
        """A little "something happens here" spark."""
        for angle in (0, 45, 90, 135):
            rad = math.radians(angle)
            self.line(
                x - math.cos(rad) * size, y - math.sin(rad) * size,
                x + math.cos(rad) * size, y + math.sin(rad) * size,
                stroke=fill, width=2.4,
            )

    def badge(self, x: float, y: float, value: str, fill: str = INDIGO, size: float = 15.0) -> None:
        """A numbered / lettered circular badge used to key annotations."""
        self.raw(
            f'<circle cx="{_fmt(x)}" cy="{_fmt(y)}" r="{_fmt(size)}" fill="{fill}" '
            f'stroke="{INK}" stroke-width="2"/>'
        )
        self.text(x, y + size * 0.36, value, size=size * 1.05, fill=PAPER, bold=True)

    # -- output ------------------------------------------------------------

    def render(self) -> str:
        background = (
            f'<rect width="{_fmt(self.width)}" height="{_fmt(self.height)}" fill="{self.background}"/>'
            if self.background
            else ""
        )
        body = "\n  ".join(self._body)
        return (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{_fmt(self.width)}" '
            f'height="{_fmt(self.height)}" viewBox="0 0 {_fmt(self.width)} {_fmt(self.height)}">\n'
            f"  {background}\n  {body}\n</svg>\n"
        )

    def save(self, path) -> None:
        from pathlib import Path

        target = Path(path)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(self.render(), encoding="utf-8")
