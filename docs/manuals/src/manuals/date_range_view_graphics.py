from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TEXT_SIZE, LABEL_SIZE
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "date-range-view"
LINE = "#B7C2D0"


def _lines(s, x, y, w, rows=3, gap=24, colour=LINE):
    for i in range(rows):
       s.line(x, y + i * gap, x + w * (0.65 if i % 3 == 2 else 1), y + i * gap, stroke=colour, width=2.4)


def _calendar_grid(s, x, y, w, h, month="March", selected=None, range_mode=False, weeknums=False):
    s.box(x, y, w, h, fill=PAPER, radius=13, shadow=True)
    s.box(x+12, y+14, w-24, 56, fill=INDIGO_PALE, stroke=INDIGO, radius=10)
    s.text(x+w/2, y+49, month, size=23, fill=INDIGO, bold=True)
    s.arrow(x+42, y+42, x+25, y+42, bend=0, stroke=MUTED, width=2.2, head=9)
    s.arrow(x+w-42, y+42, x+w-25, y+42, bend=0, stroke=MUTED, width=2.2, head=9)
    cols = 8 if weeknums else 7
    cell_w = (w-38) / cols
    cell_h = (h-102) / 7
    start_x = x + 19
    start_y = y + 88
    names = (["#"] if weeknums else []) + ["M","T","W","T","F","S","S"]
    for c, name in enumerate(names):
       s.text(start_x + c*cell_w + cell_w/2, start_y, name, size=15, fill=INK_SOFT, bold=True)
    n=24
    for r in range(6):
       if weeknums:
           s.text(start_x+cell_w/2, start_y+30+r*cell_h, str(10+r), size=13, fill=TEAL)
       for c in range(7):
           cx = start_x + (c + (1 if weeknums else 0))*cell_w + cell_w/2
           cy = start_y + 30 + r*cell_h
           day = ((n-1) % 31) + 1
           fill=None; stroke=LINE; textfill=INK
           if c >= 5:
               fill=SLATE_PALE
           if selected and day in selected:
               fill=INDIGO_PALE; stroke=INDIGO; textfill=INDIGO
           if range_mode and 8 <= day <= 14:
               fill=AMBER_PALE; stroke=AMBER; textfill=INK
           if day == 19:
               stroke=ROSE
           s.box(cx-cell_w*.38, cy-cell_h*.36, cell_w*.76, cell_h*.72, fill=fill, stroke=stroke, radius=8, width=1.6)
           s.text(cx, cy+5, str(day), size=14.5, fill=textfill)
           n += 1


def _picker(s, x, y, w, text="March 19, 2026", icon="cal"):
    s.box(x, y, w, 56, fill=PAPER, radius=8, shadow=True)
    s.text(x+20, y+35, text, size=20, anchor="start", fill=INK)
    s.line(x+w-56, y+6, x+w-56, y+50, stroke=LINE, width=2)
    s.box(x+w-45, y+14, 28, 28, fill=INDIGO_PALE, stroke=INDIGO, radius=5)
    if icon == "down":
       s.arrow(x+w-31, y+26, x+w-31, y+36, bend=0, stroke=INDIGO, width=2, head=7)
    else:
       s.line(x+w-39, y+22, x+w-23, y+22, stroke=INDIGO, width=1.8)
       s.line(x+w-39, y+28, x+w-23, y+28, stroke=INDIGO, width=1.8)
       s.line(x+w-39, y+34, x+w-23, y+34, stroke=INDIGO, width=1.8)


def _preset_panel(s, x, y, w, h, title="QUICK SELECT"):
    s.box(x, y, w, h, fill=TEAL_PALE, stroke=TEAL, radius=12, shadow=True)
    s.text(x+18, y+34, title, size=18, anchor="start", fill=TEAL, bold=True)
    for i, label in enumerate(["Today", "Yesterday", "This Week", "This Month", "Last Month"]):
       yy=y+74+i*43
       s.text(x+22, yy, label, size=18, anchor="start", fill=INK)
       if i<4: s.line(x+16, yy+16, x+w-16, yy+16, stroke=LINE, width=1.5)
    s.box(x+18, y+h-52, (w-48)/2, 34, fill=INDIGO_PALE, stroke=INDIGO, radius=7)
    s.text(x+18+(w-48)/4, y+h-30, "APPLY", size=15, fill=INDIGO, bold=True)
    s.box(x+30+(w-48)/2, y+h-52, (w-48)/2, 34, fill=PAPER, stroke=LINE, radius=7)
    s.text(x+30+3*(w-48)/4, y+h-30, "CANCEL", size=15, fill=INK_SOFT)


def save_all(drawings):
    for name, fn in drawings.items():
       fn().save(OUT / name)

def cover():
    s=Sketch(1000,480,seed=131); _preset_panel(s,80,80,240,310); _calendar_grid(s,365,80,250,310,"March",range_mode=True); _calendar_grid(s,635,80,250,310,"April",range_mode=True); s.badge(625,250,"TO",fill=AMBER,size=22); return s

def anatomy():
    s=Sketch(1120,560,seed=132); _preset_panel(s,65,100,230,330); _calendar_grid(s,340,100,245,320,"Start",range_mode=True); _calendar_grid(s,610,100,245,320,"End",range_mode=True); s.badge(598,260,"TO",fill=AMBER,size=21)
    keys=[("1",150,130,"presetsBox with presetTitle",105),("2",185,365,"apply / cancel button bar",170),("3",462,160,"startCalendarView",245),("4",598,260,"toText label",315),("5",735,160,"endCalendarView",390),("6",512,255,"shared DATE_RANGE selection model",465)]
    for k,x,y,t,ly in keys: s.badge(890,ly,k); s.text(922,ly+6,t,size=19,anchor="start"); s.arrow(870,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def orientation():
    s=Sketch(1000,480,seed=133); _preset_panel(s,40,95,185,275); _calendar_grid(s,260,95,220,275,"Start",range_mode=True); _calendar_grid(s,500,95,220,275,"End",range_mode=True); s.text(380,415,"HORIZONTAL",size=20,fill=INDIGO,bold=True)
    _calendar_grid(s,765,40,190,185,"Start",range_mode=True); _calendar_grid(s,765,250,190,185,"End",range_mode=True); s.text(860,465,"VERTICAL",size=20,fill=INDIGO,bold=True); return s

def flow():
    s=Sketch(1000,420,seed=134); _preset_panel(s,70,70,220,250); _calendar_grid(s,380,70,250,250,"Pick",range_mode=True); s.box(725,120,190,90,fill=INDIGO_PALE,stroke=INDIGO,radius=12,shadow=True); s.text(820,155,"value",size=24,fill=INDIGO,bold=True); s.text(820,185,"DateRange",size=20,fill=INDIGO)
    s.arrow(290,170,380,180,bend=0,stroke=AMBER,width=2.7,head=12); s.arrow(630,185,725,165,bend=0,stroke=AMBER,width=2.7,head=12); s.label(500,365,"preset or cell selection updates the selection model; APPLY sets value",size=20,fill=INK_SOFT); return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"orientation.svg":orientation,"flow.svg":flow}
def generate(): save_all(DRAWINGS)
if __name__=="__main__": generate()
