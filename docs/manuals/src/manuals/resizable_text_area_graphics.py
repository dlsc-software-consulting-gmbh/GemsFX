"""Generates the cartoon illustrations for the ResizableTextArea manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "resizable-text-area"
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
    s = Sketch(1000, 480, seed=131)
    s.window(62, 36, 876, 408, title="Form")
    
    _area(s, 285, 105, 430, 235, lines=5, resize=True, fill=INDIGO_PALE)
    s.cursor(710, 333, scale=1.4)
    s.arrow(698, 322, 790, 400, bend=0, stroke=ROSE, width=3)
    s.label(500, 420, "drag the corner to resize", size=23, fill=INK_SOFT)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=132)
    
    _area(s, 80, 90, 570, 330, lines=6, resize=True)
    keys = [("1", 170, 120, "standard TextArea scroll pane", 120), ("2", 620, 395, "resize-corner StackPane", 220), ("3", 608, 374, "resize-icon Region", 300), ("4", 535, 420, "scroll bar compensation", 380)]
    lx = 710
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=133)
    
    modes = [(55, "v-resize", False, True), (290, "h-resize", True, False), (525, "both-resize", True, True), (760, "no-resize", False, False)]
    for x, cap, h, v in modes:
       _area(s, x, 90, 190, 140, lines=3, resize=h or v, fill=INDIGO_PALE if h or v else SLATE_PALE)
       s.label(x + 95, 280, cap, size=18, fill=INK_SOFT)
    s.label(500, 365, "pseudo classes live on .resize-corner", size=21, fill=AMBER)

    return s


def flow():
    s = Sketch(1040, 500, seed=134)
    
    _area(s, 80, 230, 280, 150, lines=3, resize=True)
    s.cursor(340, 360, scale=1.2)
    s.arrow(355, 365, 500, 420, bend=0, stroke=ROSE, width=3)
    s.box(430, 60, 240, 115, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(550, 105, "mouse delta", size=22, fill=AMBER)
    s.text(550, 134, "screenX/screenY", size=18, fill=AMBER)
    s.arrow(365, 280, 430, 150, bend=-.1, stroke=AMBER, width=2.4)
    s.box(730, 235, 250, 145, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(855, 280, "prefWidth", size=22, fill=TEAL)
    s.text(855, 315, "prefHeight", size=22, fill=TEAL)
    s.arrow(672, 120, 812, 235, bend=.1, stroke=TEAL, width=2.4)

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
