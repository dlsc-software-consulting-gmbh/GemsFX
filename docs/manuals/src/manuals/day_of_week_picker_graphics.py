from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TEXT_SIZE, LABEL_SIZE
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "day-of-week-picker"
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

def _dow_popup(s,x,y,w,h,multi=True):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True); buttons=["Clear","All Days","Weekdays","Weekends"] if multi else ["Clear","Today"]
    for i,b in enumerate(buttons): s.box(x+14,y+14+i*31,w-28,24,fill=TEAL_PALE if i else SLATE_PALE,stroke=LINE,radius=5); s.text(x+28,y+31+i*31,b,size=14,anchor="start")
    days=["Mon","Tue","Wed","Thu","Fri","Sat","Sun"]
    off=y+24+len(buttons)*31
    for i,d in enumerate(days): s.box(x+18,off+i*28,18,18,fill=INDIGO_PALE if (multi and i<5) or (not multi and i==2) else PAPER,stroke=INDIGO if (multi and i<5) or (not multi and i==2) else LINE,radius=4); s.text(x+48,off+15+i*28,d,size=16,anchor="start")

def cover():
    s=Sketch(1000,480,seed=141); s.window(70,40,860,390,title="Schedule filter"); _picker(s,160,105,320,"Weekdays",icon="down"); _dow_popup(s,210,175,260,270,True); s.cursor(465,300,1.2); s.sparkle(450,285,14,AMBER); return s

def anatomy():
    s=Sketch(1120,560,seed=142); _picker(s,80,80,340,"Weekdays",icon="down"); _dow_popup(s,110,165,280,300,True)
    keys=[("1",115,108,"SelectionBox display-label",95),("2",385,108,"arrow-button",155),("3",170,192,"extra-buttons-box",230),("4",175,330,"localized DayOfWeek items",310),("5",225,250,"currentSelectionMode controls buttons",390),("6",210,108,"summary converter",470)]
    for k,x,y,t,ly in keys: s.badge(520,ly,k); s.text(552,ly+6,t,size=19,anchor="start"); s.arrow(500,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def modes():
    s=Sketch(1000,430,seed=143); _picker(s,120,70,260,"Wednesday",icon="down"); _dow_popup(s,140,145,220,230,False); s.label(250,405,"SINGLE: Clear + Today",size=19,fill=INK_SOFT)
    _picker(s,600,70,260,"Weekdays",icon="down"); _dow_popup(s,620,145,220,230,True); s.label(730,405,"MULTIPLE: All / weekdays / weekends",size=19,fill=INK_SOFT); return s

def summaries():
    s=Sketch(1000,420,seed=144); items=[("All seven days","All Days"),("Mon-Fri only","Weekdays"),("localized weekend","Weekends"),("Mon Tue Wed","Mon ~ Wed"),("Mon Wed Fri","Mon, Wed, Fri")]
    for i,(left,right) in enumerate(items): y=65+i*62; s.box(110,y,300,38,fill=SLATE_PALE,stroke=LINE,radius=8); s.text(130,y+25,left,size=18,anchor="start"); s.arrow(430,y+19,555,y+19,bend=0,stroke=AMBER,width=2.4,head=10); s.box(590,y,270,38,fill=INDIGO_PALE,stroke=INDIGO,radius=8); s.text(725,y+25,right,size=18,fill=INDIGO,bold=True)
    return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"modes.svg":modes,"summaries.svg":summaries}
def generate(): save_all(DRAWINGS)
if __name__=="__main__": generate()
