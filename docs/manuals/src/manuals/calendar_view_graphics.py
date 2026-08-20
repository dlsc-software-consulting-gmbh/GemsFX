from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TEXT_SIZE, LABEL_SIZE
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "calendar-view"
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
    s=Sketch(1000,480,seed=111); s.window(70,30,860,420,title="CalendarView")
    _calendar_grid(s,290,70,420,350,"March 2026",selected={10,11,12,13,14},range_mode=True,weeknums=True)
    s.badge(720,110,"1",fill=AMBER); s.text(752,116,"week numbers",size=19,anchor="start"); s.badge(720,178,"2",fill=INDIGO); s.text(752,184,"header navigation",size=19,anchor="start"); s.badge(720,246,"3",fill=TEAL); s.text(752,252,"selectable cells",size=19,anchor="start"); return s

def anatomy():
    s=Sketch(1120,560,seed=112); _calendar_grid(s,70,55,450,400,"March",selected={19},range_mode=True,weeknums=True)
    keys=[("1",294,92,"header: month, year, arrows",92),("2",130,145,"weekday-grid-pane",160),("3",105,230,"week-number-label column",228),("4",300,285,"DateCell instances",296),("5",350,340,"today style class",365),("6",330,252,"range-start / range-date / range-end",434),("7",304,462,"today button footer",500)]
    s.box(110,445,370,46,fill=SLATE_PALE,stroke=LINE,radius=8); s.text(295,474,"Today",size=20,fill=INK_SOFT)
    for k,x,y,t,ly in keys: s.badge(600,ly,k); s.text(632,ly+6,t,size=19,anchor="start"); s.arrow(578,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def modes():
    s=Sketch(1000,430,seed=113); _calendar_grid(s,50,60,270,260,"Date",selected={19});
    s.box(383,83,234,210,fill=INDIGO_PALE,stroke=INDIGO,radius=12,shadow=True); s.text(500,123,"Month view",size=24,fill=INDIGO,bold=True)
    for i,m in enumerate(["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]): s.box(405+(i%3)*64,145+(i//3)*34,54,25,fill=PAPER,stroke=LINE,radius=6); s.text(432+(i%3)*64,163+(i//3)*34,m,size=14)
    s.box(696,83,234,210,fill=TEAL_PALE,stroke=TEAL,radius=12,shadow=True); s.text(813,123,"Year view",size=24,fill=TEAL,bold=True)
    for i,y in enumerate(range(2021,2033)): s.box(718+(i%3)*64,145+(i//3)*34,54,25,fill=PAPER,stroke=LINE,radius=6); s.text(745+(i%3)*64,163+(i//3)*34,str(y),size=14)
    s.arrow(320,190,378,190,bend=0,stroke=AMBER,width=2.7,head=12); s.arrow(620,190,690,190,bend=0,stroke=AMBER,width=2.7,head=12); s.label(500,375,"click labels when monthSelectionViewEnabled / yearSelectionViewEnabled are true",size=19,fill=INK_SOFT); return s

def selection():
    s=Sketch(1000,430,seed=114); 
    for x,t,sel,range_mode in [(50,"SINGLE_DATE",{19},False),(365,"MULTIPLE_DATES",{7,12,19,25},False),(680,"DATE_RANGE",set(),True)]:
       _calendar_grid(s,x,70,270,260,t,selected=sel,range_mode=range_mode); s.label(x+135,370,t,size=19,fill=INK_SOFT)
    return s

DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"modes.svg":modes,"selection.svg":selection}
def generate(): save_all(DRAWINGS)
if __name__=="__main__": generate()
