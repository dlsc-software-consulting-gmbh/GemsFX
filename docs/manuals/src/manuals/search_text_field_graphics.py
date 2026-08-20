"""Generates the cartoon illustrations for the SearchTextField manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "search-text-field"
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
    s = Sketch(1000, 480, seed=151)
    s.window(62, 36, 876, 408, title="Form")
    
    _field(s, 220, 130, 560, text="quarterly report", left=True, right=True)
    s.box(238, 198, 330, 150, fill=PAPER, radius=10, shadow=True)
    for i, item in enumerate(["quarterly report", "quotes", "query parser"]):
       s.text(266, 240 + i * 36, item, size=20, anchor="start", fill=INK_SOFT)
    s.label(500, 410, "search prompt, clear icon and optional history", size=23, fill=INK_SOFT)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=152)
    
    _field(s, 80, 145, 600, text="customers", left=True, right=True)
    s.box(100, 214, 310, 150, fill=PAPER, radius=10, shadow=True)
    for i, item in enumerate(["customers", "contacts", "contracts"]):
       s.text(130, 255 + i * 35, item, size=19, anchor="start", fill=INK_SOFT)
    keys = [("1", 112, 174, "HistoryButton / search icon", 110), ("2", 340, 174, "text field editor", 185), ("3", 648, 174, "clear icon wrapper", 260), ("4", 250, 225, "HistoryPopup", 335), ("5", 250, 294, "history-list-view", 410)]
    lx = 735
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=153)
    
    _field(s, 70, 100, 270, prompt="Search...", left=True, right=False)
    s.label(205, 205, "historyManager=null", size=18, fill=INK_SOFT)
    _field(s, 365, 100, 270, text="alpha", left=True, right=True)
    s.label(500, 205, "clear icon visible", size=18, fill=INK_SOFT)
    _field(s, 660, 100, 270, text="alpha", left=True, right=True)
    s.box(680, 168, 220, 96, fill=PAPER, radius=10, shadow=True)
    s.text(706, 207, "alpha", size=18, anchor="start", fill=INK_SOFT)
    s.text(706, 237, "alpine", size=18, anchor="start", fill=INK_SOFT)
    s.label(795, 310, "history enabled", size=18, fill=INK_SOFT)
    s.label(500, 375, "Enter or focus loss can add non-blank text to history", size=20, fill=AMBER)

    return s


def flow():
    s = Sketch(1040, 500, seed=154)
    
    _field(s, 80, 80, 280, text="invoice", left=True, right=True)
    s.arrow(365, 110, 500, 110, bend=0.05, stroke=AMBER, width=2.5)
    s.box(505, 60, 230, 105, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(620, 102, "addToHistory()", size=21, fill=AMBER)
    s.text(620, 130, "if not blank", size=18, fill=AMBER)
    s.arrow(740, 110, 875, 110, bend=0.05, stroke=AMBER, width=2.5)
    s.box(785, 230, 200, 95, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(885, 270, "HistoryManager", size=20, fill=TEAL)
    s.arrow(885, 165, 885, 225, bend=0, stroke=TEAL, width=2.5)
    s.box(115, 300, 310, 90, fill=INDIGO_PALE, stroke=INDIGO, radius=12)
    s.text(270, 337, "popup selects a value", size=21, fill=INDIGO)
    s.arrow(785, 270, 430, 338, bend=.1, stroke=INDIGO, width=2.5)

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
