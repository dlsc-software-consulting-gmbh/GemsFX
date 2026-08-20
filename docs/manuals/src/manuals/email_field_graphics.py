"""Generates the cartoon illustrations for the EmailField manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "email-field"
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
    s = Sketch(1000, 480, seed=101)
    s.window(62, 36, 876, 408, title="Form")
    
    _field(s, 180, 145, 640, text="alex@pro", left=True, right=True)
    s.box(438, 214, 260, 150, fill=PAPER, radius=10, shadow=True)
    for i, name in enumerate(["protonmail.com", "proton.me", "provider.net"]):
       s.text(465, 255 + i * 36, name, size=20, anchor="start", fill=INK if i == 0 else INK_SOFT)
    s.sparkle(802, 150, size=16, fill=AMBER)
    s.label(500, 410, "validated address + domain suggestions", size=22, fill=INK_SOFT)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=102)
    
    _field(s, 70, 130, 600, text="alex@example.com", left=True, right=True)
    s.box(330, 200, 260, 155, fill=PAPER, radius=10, shadow=True)
    for i, name in enumerate(["gmail.com", "outlook.com", "icloud.com"]):
       s.text(360, 244 + i * 36, name, size=19, anchor="start", fill=INK_SOFT)
    keys = [("1", 112, 159, "mail-icon-wrapper", 125), ("2", 308, 160, "editor CustomTextField", 195), ("3", 640, 160, "validation-icon-wrapper", 265), ("4", 458, 224, "suggestion-popup", 335), ("5", 458, 296, "suggestion-list-view", 405)]
    lx = 735
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=103)
    
    frames = [(45, "optional blank", "", False, TEAL_PALE), (365, "bad address", "alex@", True, ROSE_PALE), (685, "valid", "alex@example.com", False, INDIGO_PALE)]
    for x, cap, text, bad, fill in frames:
       _field(s, x, 118, 270, text=text, prompt="email" if not text else "", left=True, right=bad, fill=fill)
       s.label(x + 135, 220, cap, size=19, fill=INK_SOFT)
    s.arrow(324, 150, 356, 150, bend=0, stroke=AMBER, width=2.8)
    s.arrow(644, 150, 676, 150, bend=0, stroke=AMBER, width=2.8)
    s.label(500, 345, "required=false lets an empty field be valid; required=true does not", size=21, fill=INK_SOFT)

    return s


def flow():
    s = Sketch(1040, 500, seed=104)
    
    _field(s, 70, 82, 300, text="a@pro", left=True)
    s.arrow(385, 112, 515, 112, bend=0.05, stroke=AMBER, width=2.5)
    s.box(520, 60, 210, 105, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(625, 105, "split at @", size=22, fill=AMBER)
    s.text(625, 132, "filter domains", size=18, fill=AMBER)
    s.arrow(735, 112, 865, 112, bend=0.05, stroke=AMBER, width=2.5)
    s.box(780, 205, 190, 95, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(875, 246, "validProperty", size=21, fill=TEAL)
    s.arrow(255, 152, 785, 232, bend=0.2, stroke=TEAL, width=2.5)
    s.box(95, 300, 300, 88, fill=INDIGO_PALE, stroke=INDIGO, radius=12)
    s.text(245, 337, "emailAddress or list", size=21, fill=INDIGO)
    s.arrow(875, 300, 410, 344, bend=-0.18, stroke=INDIGO, width=2.5)

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
