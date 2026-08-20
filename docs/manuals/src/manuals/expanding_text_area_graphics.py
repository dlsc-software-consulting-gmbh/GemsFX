"""Generates the cartoon illustrations for the ExpandingTextArea manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "expanding-text-area"
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
    s = Sketch(1000, 480, seed=121)
    s.window(62, 36, 876, 408, title="Form")
    
    _area(s, 300, 95, 400, 84, lines=2)
    s.arrow(500, 190, 500, 250, bend=0, stroke=AMBER, width=3)
    _area(s, 300, 260, 400, 150, lines=5, fill=INDIGO_PALE)
    s.label(500, 448, "prefHeight follows wrapped text", size=23, fill=INK_SOFT)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=122)
    
    _area(s, 80, 100, 560, 300, lines=7, fill=PAPER)
    s.box(100, 122, 520, 245, fill=None, stroke=TEAL, radius=8, dash="9 7")
    keys = [("1", 120, 104, "TextArea root + expanding-text-area", 120), ("2", 365, 170, "internal Text node", 200), ("3", 620, 255, "scroll bars forced to NEVER", 280), ("4", 365, 368, "computed prefHeight", 360)]
    lx = 710
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=123)
    
    for x, h, rows, cap in [(80, 90, 2, "one line"), (380, 150, 4, "wrapped text"), (700, 230, 7, "more lines")]:
       _area(s, x, 80, 230, h, lines=rows, fill=INDIGO_PALE)
       s.label(x + 115, 345, cap, size=19, fill=INK_SOFT)
    s.arrow(320, 150, 365, 170, bend=.08, stroke=AMBER, width=2.6)
    s.arrow(630, 190, 685, 220, bend=.08, stroke=AMBER, width=2.6)

    return s


def flow():
    s = Sketch(1040, 500, seed=124)
    
    s.box(70, 75, 220, 90, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(180, 115, "text layout", size=22, fill=TEAL)
    s.box(405, 75, 230, 90, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(520, 115, "offsets", size=22, fill=AMBER)
    s.text(520, 142, "insets + viewport", size=17, fill=AMBER)
    s.box(750, 75, 220, 90, fill=INDIGO_PALE, stroke=INDIGO, radius=12)
    s.text(860, 115, "prefHeight", size=22, fill=INDIGO)
    s.arrow(295, 120, 398, 120, bend=0, stroke=INK_SOFT, width=2.4)
    s.arrow(640, 120, 744, 120, bend=0, stroke=INK_SOFT, width=2.4)
    _area(s, 292, 260, 450, 145, lines=5)
    s.dimension(760, 260, 760, 405, "computed height", stroke=ROSE, offset=18)

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
