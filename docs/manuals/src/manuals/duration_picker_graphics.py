"""Generates cartoon illustrations for the DurationPicker manual."""

from pathlib import Path
from manualkit.svgstyle import AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER, ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch

OUT = Path(__file__).resolve().parent.parent / "graphics" / "duration-picker"
LINE = "#B7C2D0"
CONTROL = "DurationPicker"
TITLE = "2d 10h 23m"
ITEMS = ['02d', '10h', '23m', '55s', '000ms']
LEGEND = ['duration', 'fields', 'labels', 'limits']

def _lines(s, x, y, w, rows=3, gap=24):
    for i in range(rows):
       s.line(x, y + i * gap, x + w * (1 if i % 3 != 2 else .62), y + i * gap, stroke=LINE, width=2.2)

def _field(s, x, y, w, text, selected=False):
    s.box(x, y, w, 46, fill=INDIGO_PALE if selected else PAPER, radius=7, shadow=selected, stroke=INDIGO if selected else INK)
    s.text(x + w / 2, y + 30, text, size=21, fill=INDIGO if selected else INK)

def _button(s, x, y, w, h, label, fill=SLATE_PALE):
    s.box(x, y, w, h, fill=fill, radius=8)
    s.text(x + w / 2, y + h / 2 + 7, label, size=18, fill=INK)

def _year_popup(s, x, y, w, h):
    s.box(x, y, w, h, fill=PAPER, radius=12, shadow=True)
    s.box(x, y, w, 45, fill=SLATE_PALE, radius=12)
    s.text(x + w / 2, y + 30, "2020-2039", size=18)
    cols = 4; rows = 3; cellw = (w - 40) / cols; cellh = (h - 62) / rows
    for i, m in enumerate(ITEMS[:12]):
       _field(s, x + 16 + (i % cols) * cellw, y + 55 + (i // cols) * cellh, cellw - 8, m, m == TITLE)

def _control_body(s, x, y, w, h, popup=False):
    if CONTROL == "YearMonthView":
       s.box(x, y, w, h, fill=PAPER, radius=12, shadow=True)
       s.box(x, y, w, 58, fill=SLATE_PALE, radius=12)
       s.text(x + 42, y + 38, "‹", size=32, fill=INK_SOFT)
       s.text(x + w / 2, y + 37, TITLE, size=24, fill=INK)
       s.text(x + w - 42, y + 38, "›", size=32, fill=INK_SOFT)
       cellw = (w - 72) / 2; cellh = (h - 92) / 6
       for i, m in enumerate(ITEMS):
           cx = x + 26 + (0 if i % 2 == 0 else 1) * (cellw + 20); cy = y + 74 + (i // 2) * cellh
           sel = i == 7
           _field(s, cx, cy, cellw, m, sel)
           if sel: s.line(cx + 18, cy + 39, cx + cellw - 18, cy + 39, stroke=INDIGO, width=3)
       s.line(x + w / 2, y + 72, x + w / 2, y + h - 16, stroke=LINE, width=2)
    elif CONTROL == "YearView":
       s.box(x, y, w, h, fill=PAPER, radius=12, shadow=True)
       s.box(x, y, w, 58, fill=SLATE_PALE, radius=12)
       s.text(x + 42, y + 38, "‹", size=32, fill=INK_SOFT)
       s.text(x + w / 2, y + 37, TITLE, size=22, fill=INK)
       s.text(x + w - 42, y + 38, "›", size=32, fill=INK_SOFT)
       cols = 4; rows = 5; cellw = (w - 54) / cols; cellh = (h - 90) / rows
       for i, m in enumerate(ITEMS[:20]):
           cx = x + 18 + (i % cols) * cellw; cy = y + 76 + (i // cols) * cellh
           sel = m == "2026"
           _field(s, cx, cy, cellw - 10, m, sel)
           if sel: s.line(cx + 16, cy + 39, cx + cellw - 26, cy + 39, stroke=INDIGO, width=3)
    elif CONTROL == "YearPicker":
       s.box(x, y, w, 58, fill=PAPER, radius=9, shadow=True)
       _field(s, x + 12, y + 7, w - 76, TITLE, False)
       s.box(x + w - 56, y + 7, 44, 44, fill=AMBER_PALE, radius=7)
       s.text(x + w - 34, y + 37, "▣", size=24, fill=AMBER)
       if popup: _year_popup(s, x + 20, y + 76, w - 40, 220)
    elif CONTROL == "TimePicker":
       s.box(x, y, w, 58, fill=PAPER, radius=9, shadow=True)
       px = x + 20
       for p in TITLE.replace(':', ' : ').split():
           if p == ':': s.text(px + 5, y + 36, ':', size=24); px += 22
           else: _field(s, px, y + 8, 50, p, p == '30'); px += 58
       s.box(x + w - 56, y + 7, 44, 44, fill=AMBER_PALE, radius=7); s.text(x + w - 34, y + 37, "◷", size=25, fill=AMBER)
       if popup:
           s.box(x + 40, y + 78, w - 80, 210, fill=PAPER, radius=12, shadow=True)
           for col, vals in enumerate((["12", "13", "14", "15"], ["00", "15", "30", "45"])):
               for i, v in enumerate(vals): _field(s, x + 72 + col * 105, y + 105 + i * 42, 68, v, v in ('14', '30'))
    elif CONTROL == "DurationPicker":
       s.box(x, y, w, 58, fill=PAPER, radius=9, shadow=True)
       px = x + 15
       for it in ITEMS[:4]: _field(s, px, y + 8, 68, it, it == '10h'); px += 78
       s.box(x + w - 56, y + 7, 44, 44, fill=AMBER_PALE, radius=7); s.text(x + w - 34, y + 37, "◴", size=25, fill=AMBER)
       if popup:
           s.box(x + 35, y + 80, w - 70, 210, fill=PAPER, radius=12, shadow=True)
           for col, it in enumerate(ITEMS[:5]):
               s.box(x + 58 + col * 78, y + 105, 56, 152, fill=SLATE_PALE, radius=8)
               s.text(x + 86 + col * 78, y + 184, it, size=18, fill=INK)
    else:
       s.box(x, y, w, 58, fill=PAPER, radius=9, shadow=True)
       s.text(x + 24, y + 37, TITLE, size=22, anchor="start", fill=INK)
       s.box(x + w - 54, y + 8, 40, 42, fill=SLATE_PALE, radius=7); s.text(x + w - 34, y + 35, "⌄", size=23)
       if popup:
           s.box(x + 8, y + 76, w - 16, 250, fill=PAPER, radius=12, shadow=True)
           _button(s, x + 24, y + 94, w - 48, 34, "Clear", ROSE_PALE)
           _button(s, x + 24, y + 134, w - 48, 34, "Select All", TEAL_PALE)
           for i, it in enumerate(ITEMS):
               s.box(x + 32, y + 184 + i * 34, w - 64, 28, fill=INDIGO_PALE if i in (1, 2) else PAPER, radius=5)
               s.text(x + 60, y + 204 + i * 34, "☑" if i in (1, 2) else "☐", size=18, anchor="start", fill=INDIGO)
               s.text(x + 96, y + 204 + i * 34, it, size=18, anchor="start", fill=INK)

def cover():
    s = Sketch(1000, 480, seed=101 + len(CONTROL))
    s.window(60, 35, 880, 410, title="GemsFX")
    _lines(s, 105, 100, 350, 4)
    s.box(105, 230, 300, 130, fill=SLATE_PALE, radius=12, stroke=LINE, width=2)
    _control_body(s, 500, 115, 330, 300)
    s.badge(835, 126, "API", fill=INDIGO, size=18)
    s.sparkle(856, 238, size=17, fill=AMBER)
    s.cursor(805, 325, scale=1.15)
    return s

def anatomy():
    s = Sketch(1100, 560, seed=121 + len(CONTROL))
    _control_body(s, 70, 70, 520, 380, popup=True)
    legend_x = 665
    targets = [(LEGEND[0], 170, 104), (LEGEND[1], 340, 120), (LEGEND[2], 510, 125), (LEGEND[3], 260, 220)]
    for i, (txt, tx, ty) in enumerate(targets, 1):
       ly = 95 + i * 72
       s.badge(legend_x, ly, str(i), fill=INDIGO, size=16)
       s.text(legend_x + 32, ly + 6, txt, size=20, anchor="start", fill=INK)
       s.arrow(legend_x - 22, ly, tx, ty, bend=.05, stroke=MUTED, width=1.7, head=9)
    return s

def behaviour():
    s = Sketch(1000, 430, seed=155 + len(CONTROL))
    for n, x in enumerate((45, 365, 685)):
       s.window(x, 45, 270, 255, title="", titlebar=22, shadow=True)
       _control_body(s, x + 25, 95 if n != 1 else 85, 220, 160, popup=(n == 1))
       if n == 2: s.sparkle(x + 210, 125, size=15, fill=TEAL)
       s.label(x + 135, 340, ["initial value", "user opens / edits", "constrained result"][n], size=20, fill=INK_SOFT)
    s.arrow(322, 175, 356, 175, bend=0, stroke=AMBER, width=3, head=12)
    s.arrow(642, 175, 676, 175, bend=0, stroke=AMBER, width=3, head=12)
    return s

def styling():
    s = Sketch(1000, 470, seed=177 + len(CONTROL))
    _control_body(s, 80, 90, 360, 260)
    s.box(540, 80, 360, 300, fill=SLATE_PALE, radius=14, shadow=True)
    s.text(570, 128, "CSS hooks", size=25, anchor="start", fill=INK)
    for i, txt in enumerate(LEGEND):
       y = 170 + i * 42
       s.badge(575, y, str(i + 1), fill=TEAL, size=14)
       s.text(605, y + 6, txt, size=19, anchor="start", fill=INK)
    s.dimension(100, 385, 430, 385, "preferred size", stroke=ROSE, size=17, offset=18)
    return s

def recipes():
    s = Sketch(1000, 470, seed=191 + len(CONTROL))
    s.window(70, 45, 860, 360, title="Recipe board")
    for i, (txt, fill) in enumerate((("bind", INDIGO_PALE), ("limit", TEAL_PALE), ("style", AMBER_PALE), ("listen", ROSE_PALE))):
       x = 115 + (i % 2) * 390; y = 110 + (i // 2) * 135
       s.box(x, y, 320, 95, fill=fill, radius=14, shadow=True)
       s.text(x + 28, y + 38, txt, size=25, anchor="start", fill=INK)
       _lines(s, x + 30, y + 62, 245, 2, gap=20)
    s.cursor(835, 342, scale=1.2)
    return s

def generate():
    OUT.mkdir(parents=True, exist_ok=True)
    for name, fn in {"cover": cover, "anatomy": anatomy, "behaviour": behaviour, "styling": styling, "recipes": recipes}.items():
       fn().save(OUT / f"{name}.svg")
