from pathlib import Path
from manualkit.svgstyle import (
    AMBER, AMBER_PALE, INDIGO, INDIGO_PALE, INK, INK_SOFT, MUTED, PAPER,
    ROSE, ROSE_PALE, SLATE_PALE, TEAL, TEAL_PALE, Sketch, TEXT_SIZE, LABEL_SIZE
)

OUT = Path(__file__).resolve().parent.parent / "graphics" / "year-month-picker"
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

def _ym_view(s,x,y,w,h,year="2026",sel="Mar"):
    s.box(x,y,w,h,fill=PAPER,stroke=INK,radius=12,shadow=True); s.box(x+14,y+14,w-28,48,fill=INDIGO_PALE,stroke=INDIGO,radius=9); s.text(x+w/2,y+45,year,size=23,fill=INDIGO,bold=True)
    months=["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]
    for i,m in enumerate(months): xx=x+25+(i%3)*(w-50)/3; yy=y+88+(i//3)*42; fill=AMBER_PALE if m==sel else SLATE_PALE; stroke=AMBER if m==sel else LINE; s.box(xx,yy,(w-80)/3,30,fill=fill,stroke=stroke,radius=7); s.text(xx+(w-80)/6,yy+21,m,size=16)

def cover():
    s=Sketch(1000,480,seed=151); s.window(60,35,880,405,title="Budget period"); _picker(s,150,105,360,"March 2026"); _ym_view(s,510,95,300,285,"2026","Mar"); s.cursor(710,210,1.2); return s

def anatomy():
    s=Sketch(1120,560,seed=152); _picker(s,80,85,380,"March 2026"); _ym_view(s,115,175,330,310,"2026","Mar")
    keys=[("1",120,113,"TextField editor",100),("2",430,113,"edit-button with mdi-calendar",160),("3",270,210,"YearMonthView popup",235),("4",260,305,"value bound to selected month",315),("5",150,113,"converter formats MMMM yyyy",395),("6",140,120,"prompt text from bundle",470)]
    for k,x,y,t,ly in keys: s.badge(560,ly,k); s.text(592,ly+6,t,size=19,anchor="start"); s.arrow(538,ly,x,y,bend=.04,stroke=MUTED,width=1.6,head=9)
    return s

def editing():
    s=Sketch(1000,420,seed=153); _picker(s,90,90,290,"March 2026"); _picker(s,355,90,290,"April 2026"); _picker(s,620,90,290,"",icon="down")
    s.arrow(250,170,250,235,bend=0,stroke=AMBER,width=2.5,head=10); s.text(250,270,"DOWN = plusMonths(1)",size=18,fill=AMBER); s.arrow(515,170,515,235,bend=0,stroke=ROSE,width=2.5,head=10); s.text(515,270,"UP = minusMonths(1)",size=18,fill=ROSE); s.text(765,245,"invalid parse returns null",size=19,fill=INK_SOFT); return s

def button_display():
    s=Sketch(1000,420,seed=154); labels=[("LEFT",90),("RIGHT",320),("BUTTON_ONLY",550),("FIELD_ONLY",780)]
    for name,x in labels:
       if name=="LEFT": _picker(s,x,120,180,"Mar 2026")
       elif name=="RIGHT": _picker(s,x,120,180,"Mar 2026")
       elif name=="BUTTON_ONLY": _picker(s,x,120,80,"",icon="down")
       else: s.box(x,120,180,56,fill=PAPER,stroke=INK,radius=8,shadow=True); s.text(x+16,155,"Mar 2026",size=18,anchor="start")
       s.label(x+90,225,name,size=18,fill=INDIGO,bg=INDIGO_PALE)
    return s
DRAWINGS={"cover.svg":cover,"anatomy.svg":anatomy,"editing.svg":editing,"button-display.svg":button_display}
def generate(): save_all(DRAWINGS)
if __name__=="__main__": generate()
