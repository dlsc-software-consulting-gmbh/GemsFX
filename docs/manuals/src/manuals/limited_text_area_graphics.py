"""Generates the cartoon illustrations for the LimitedTextArea manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "limited-text-area"
LINE = "#B7C2D0"


def _field(s, x, y, w, h=58, text="", prompt="", left=True, right=False, fill=PAPER):
    s.box(x, y, w, h, fill=fill, radius=12, shadow=True)
    if left:
       s.blob(x + 34, y + h / 2, 14, 14, fill=INDIGO_PALE, stroke=INDIGO, width=2.4)
    if prompt:
       s.text(x + (64 if left else 24), y + h / 2 + 7, prompt, size=20, anchor="start", fill=MUTED)
    if text:
       s.text(x + (64 if left else 24), y + h / 2 + 7, text, size=21, anchor="start", fill=INK)
    if right:
       s.blob(x + w - 32, y + h / 2, 13, 13, fill=ROSE_PALE, stroke=ROSE, width=2.2)
       s.line(x + w - 38, y + h / 2 - 6, x + w - 26, y + h / 2 + 6, stroke=ROSE, width=2.2)
       s.line(x + w - 26, y + h / 2 - 6, x + w - 38, y + h / 2 + 6, stroke=ROSE, width=2.2)


def _area(s, x, y, w, h, lines=4, resize=False, bottom=False, fill=PAPER):
    s.box(x, y, w, h, fill=fill, radius=12, shadow=True)
    for i in range(lines):
       length = w - 54 - (i % 3) * 50
       s.line(x + 26, y + 34 + i * 28, x + 26 + length, y + 34 + i * 28, stroke=LINE, width=2.6)
    if resize:
       for i in range(3):
           s.line(x + w - 46 + i * 12, y + h - 18, x + w - 18, y + h - 46 + i * 12, stroke=INDIGO, width=2.5)
    if bottom:
       s.line(x + 12, y + h - 46, x + w - 12, y + h - 46, stroke=LINE, width=2)
       s.text(x + 30, y + h - 17, "tips", size=17, anchor="start", fill=MUTED)
       s.blob(x + w - 34, y + h - 24, 20, 20, fill=AMBER_PALE, stroke=AMBER, width=2.4)


def cover():
    s = Sketch(1000, 480, seed=141)
    s.window(62, 36, 876, 408, title="Form")
    
    _area(s, 270, 90, 460, 280, lines=6, resize=True, bottom=True, fill=PAPER)
    s.blob(690, 347, 21, 21, fill=AMBER_PALE, stroke=AMBER, width=2.5)
    s.text(690, 354, "12", size=17, fill=AMBER)
    s.label(500, 425, "text length, tips and resize handle", size=23, fill=INK_SOFT)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=142)
    
    _area(s, 80, 80, 580, 360, lines=6, resize=True, bottom=True)
    keys = [("1", 180, 120, "resizable text content", 115), ("2", 118, 407, "tips label", 190), ("3", 576, 415, "length label", 265), ("4", 622, 415, "CircleProgressIndicator", 340), ("5", 620, 372, "resize corner", 415)]
    lx = 720
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=143)
    
    for x, cap, fill, count in [(80, "normal", TEAL_PALE, "18"), (380, "warning", AMBER_PALE, "3"), (700, "error", ROSE_PALE, "-5")]:
       _area(s, x, 75, 230, 210, lines=5, resize=True, bottom=True, fill=fill)
       s.blob(x + 195, 260, 18, 18, fill=PAPER, stroke=INK_SOFT, width=2.2)
       s.text(x + 195, 267, count, size=15, fill=INK_SOFT)
       s.label(x + 115, 340, cap, size=19, fill=INK_SOFT)
    s.label(500, 390, "AUTO display appears for warning and error", size=20, fill=AMBER)

    return s


def flow():
    s = Sketch(1040, 500, seed=144)
    
    s.box(70, 80, 210, 95, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(175, 121, "text", size=22, fill=TEAL)
    s.box(360, 80, 250, 95, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(485, 116, "excludedItems", size=21, fill=AMBER)
    s.text(485, 143, "replaceAll", size=18, fill=AMBER)
    s.box(700, 80, 260, 95, fill=INDIGO_PALE, stroke=INDIGO, radius=12)
    s.text(830, 116, "range check", size=21, fill=INDIGO)
    s.text(830, 143, "warning/error", size=18, fill=INDIGO)
    s.arrow(285, 128, 354, 128, bend=0, stroke=INK_SOFT, width=2.4)
    s.arrow(615, 128, 694, 128, bend=0, stroke=INK_SOFT, width=2.4)
    _area(s, 315, 270, 410, 150, lines=4, resize=True, bottom=True)
    s.arrow(830, 175, 700, 314, bend=.1, stroke=INDIGO, width=2.4)

    return s


DRAWINGS = {
    "cover.svg": cover,
    "anatomy.svg": anatomy,
    "states.svg": states,
    "flow.svg": flow,
}


def generate() -> None:
    for name, factory in DRAWINGS.items():
       factory().save(OUT / name)


if __name__ == "__main__":
    generate()
    print(f"wrote {len(DRAWINGS)} graphics to {OUT}")
