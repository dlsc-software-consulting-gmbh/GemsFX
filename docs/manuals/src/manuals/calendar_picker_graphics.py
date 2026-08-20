from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TEXT_SIZE, LABEL_SIZE
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "calendar-picker"
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
    s=Sketch(1000,480,seed=101); s.window(55,35,890,400,title="Booking form")
    _lines(s,100,105,360,4); _picker(s,105,235,360,"Mar 19, 2026")
    _calendar_grid(s,545,82,330,315,"March 2026",selected={19})
    s.cursor(805,270,1.3); s.sparkle(790,250,14,AMBER); return s

def anatomy():
    s=Sketch(1120,560,seed=102); _picker(s,70,90,420,"March 19, 2026"); _calendar_grid(s,96,185,360,330,"March",selected={19})
    keys=[("1",120,118,"TextField editor",105), ("2",438,118,"arrow-button with calendar glyph",165), ("3",278,210,"CalendarView popup",250), ("4",280,412,"selectedDate bound to value",330), ("5",210,500,"dateFilter disables cells",430)]
    for k,x,y,t,ly in keys: s.badge(590,ly,k); s.text(622,ly+6,t,size=19,anchor="start"); s.arrow(570,ly,x,y,bend=.04,stroke=MUTED,width=1.7,head=9)
    return s

def interaction():
    s=Sketch(1000,420,seed=103); 
    for x,t,d in [(50,"type + ENTER","2026-03-19"),(365,"arrow keys","up/down days"),(680,"choose popup","click cell")]:
       _picker(s,x,80,260,t); _calendar_grid(s,x+20,170,220,190,"Mar",selected={19}); s.label(x+130,380,d,size=19,fill=INK_SOFT)
    s.arrow(328,210,360,210,bend=0,stroke=AMBER,width=2.5,head=11); s.arrow(640,210,672,210,bend=0,stroke=AMBER,width=2.5,head=11); return s

def filtering():
    s=Sketch(1000,450,seed=104); _calendar_grid(s,100,55,360,335,"Business days",selected={18}); _calendar_grid(s,540,55,360,335,"Filtered",selected={18})
    for cx in [786,832,694,740]: s.line(cx-14,305,cx+14,333,stroke=ROSE,width=3); s.line(cx+14,305,cx-14,333,stroke=ROSE,width=3)
    s.label(500,410,"dateFilter returns false → cell disabled, but range interiors may still include disabled dates",size=19,fill=INK_SOFT); return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"interaction.svg":interaction,"filtering.svg":filtering}
def generate(): save_all(DRAWINGS)
if __name__=="__main__": generate()
