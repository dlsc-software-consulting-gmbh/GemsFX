"""Generates the cartoon illustrations for the EnhancedPasswordField manual."""

from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch,
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "enhanced-password-field"
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
    s = Sketch(1000, 480, seed=111)
    s.window(62, 36, 876, 408, title="Form")
    
    _field(s, 175, 150, 650, text="••••••••••", left=False, right=False)
    s.blob(780, 179, 19, 13, fill=INDIGO_PALE, stroke=INDIGO, width=2.5)
    s.text(500, 305, "toggle between masked and plain text", size=25, fill=INK_SOFT)
    s.sparkle(790, 144, size=14, fill=AMBER)

    return s


def anatomy():
    s = Sketch(1120, 560, seed=112)
    
    _field(s, 80, 180, 620, text="••••••••", left=True, right=False)
    s.blob(664, 209, 18, 12, fill=INDIGO_PALE, stroke=INDIGO, width=2.5)
    keys = [("1", 112, 210, "left node", 160), ("2", 330, 210, "password text", 230), ("3", 664, 210, "right node / eye toggle", 300), ("4", 456, 262, "echoChar masks every character", 370)]
    lx = 755
    for key, tx, ty, label, ly in keys:
       s.badge(lx, ly, key, fill=INDIGO, size=16)
       s.text(lx + 28, ly + 5, label, size=19, anchor="start", fill=INK)
       s.arrow(lx - 22, ly, tx, ty, bend=0.04, stroke=MUTED, width=1.6, head=9)

    return s


def states():
    s = Sketch(1000, 440, seed=113)
    
    _field(s, 80, 120, 360, text="★★★★★★★★", left=False, right=False)
    s.blob(405, 149, 18, 12, fill=INDIGO_PALE, stroke=INDIGO, width=2.4)
    s.label(260, 225, "showPassword=false", size=20, fill=INK_SOFT)
    _field(s, 560, 120, 360, text="swordfish", left=False, right=False)
    s.blob(885, 149, 18, 12, fill=ROSE_PALE, stroke=ROSE, width=2.4)
    s.line(872, 136, 898, 162, stroke=ROSE, width=2.4)
    s.label(740, 225, "showPassword=true", size=20, fill=INK_SOFT)
    s.arrow(455, 150, 545, 150, bend=0, stroke=AMBER, width=3)
    s.label(500, 340, ":showing-password changes the icon shape", size=21, fill=AMBER)

    return s


def flow():
    s = Sketch(1040, 500, seed=114)
    
    s.box(80, 75, 240, 110, fill=TEAL_PALE, stroke=TEAL, radius=12)
    s.text(200, 118, "textProperty", size=22, fill=TEAL)
    s.text(200, 145, "\"secret\"", size=20, fill=TEAL)
    s.box(400, 75, 240, 110, fill=AMBER_PALE, stroke=AMBER, radius=12)
    s.text(520, 118, "maskText()", size=22, fill=AMBER)
    s.text(520, 145, "show + echo", size=19, fill=AMBER)
    s.box(720, 75, 240, 110, fill=INDIGO_PALE, stroke=INDIGO, radius=12)
    s.text(840, 118, "Text node", size=22, fill=INDIGO)
    s.text(840, 145, "display string", size=19, fill=INDIGO)
    s.arrow(325, 130, 395, 130, bend=0, stroke=INK_SOFT, width=2.4)
    s.arrow(645, 130, 715, 130, bend=0, stroke=INK_SOFT, width=2.4)
    _field(s, 255, 285, 530, text="■■■■■■", left=True, right=False)
    s.label(520, 395, "the skin rebinds the internal text node", size=21, fill=INK_SOFT)

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
